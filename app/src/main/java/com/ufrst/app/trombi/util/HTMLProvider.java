package com.ufrst.app.trombi.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.ufrst.app.trombi.database.Eleve;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ufrst.app.trombi.ui.ActivityMain.PREFS_MULTI_THREADING;

// Classe utilitaire pour générer le HTML et charger les images en parallèle. Builder Pattern
public class HTMLProvider {

    private final List<Eleve> listeEleves;
    private final boolean withDescription;
    private final String descTrombi;
    private final String nomTrombi;
    private final Context context;
    private final int nbCols;

    private HTMLProvider(final Builder builder){
        withDescription = builder.withDescription;
        listeEleves = builder.listeEleves;
        descTrombi = builder.descTrombi;
        nomTrombi = builder.nomTrombi;
        context = builder.context;
        nbCols = builder.nbCols;
    }

    // Ecrit les balises HTML en s'adaptant aux attributs de classe
    public String doHTML(){
        // Ne peut pas se produire, sauf si l'API du téléphone est inférieure à 26
        // Les APIs inférieures à 26 ne supportent pas l'attribut "min" des Seekbar
        if(nbCols == -1){
            return "<h1>Erreur nombre de colonnes</h1>";
        }

        StringBuilder sb = new StringBuilder();
        appendHTMLStyle(sb);

        if(withDescription)
            sb.append("<h2>").append(descTrombi).append("</h2>");

        sb.append("<table id=\"table\">");

        appendHTMLEleves(sb);

        sb.append("</table>");
        sb.append("</body></html>");

        return sb.toString();
    }

    // Crée une balise <td> avec la photo d'un élève ou un placeholder
    private String addElevePhoto(@NonNull Eleve eleve){
        String beginning = "<td><img src=\"data:image/jpg;base64,";
        String end = "\" /></td>";

        return eleve.getPhoto().trim().equals("") ?
                beginning + Base64Placeholder.getInstance(context).getBase64Placeholder() + end :
                beginning + convertToBase64(eleve) + end;
    }

    // Calcule le nombre de colonne selon le nombre d'élèves et de colonnes voulues par l'utilisateur
    private int computeRowsNumber(int nbEleve, int nbCols){
        return nbEleve % nbCols > 0 ? (nbEleve / nbCols) + 1 : nbEleve / nbCols;
    }

    // Calcule la position de l'élève à récupérer dans la liste selon la ligne et la colonne
    private Eleve mapToEleve(int rowIndex, int colIndex){
        try{
            return listeEleves.get(colIndex + (rowIndex * nbCols));
        } catch(IndexOutOfBoundsException e){
            return null;
        }
    }

    // Retourne un stream d'élèves
    private Stream<Eleve> processEleves(int rowIndex){
        //noinspection ConstantConditions, non null confirmé par filter()
        return IntStream.range(0, nbCols)
                .mapToObj(colIndex -> mapToEleve(rowIndex, colIndex))
                .filter(Objects::nonNull);
    }

    // Convertit l'image d'un élève en image Base64, pour l'afficher dans le HTML
    private String convertToBase64(@NonNull Eleve eleve){
        if(!eleve.getPhoto().trim().isEmpty()){
            // Récupération de l'URI sous une autre forme
            String filePath = Uri.parse(eleve.getPhoto()).getPath();
            Bitmap bm = BitmapFactory.decodeFile(filePath);

            if(bm != null){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                try{
                    baos.close();
                } catch(IOException e){
                    Logger.handleException(e);
                }

                return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            }
        }

        // Image vide
        return "";
    }

    private boolean isMultiThreading(){
        SharedPreferences prefs =
                context.getSharedPreferences("com.ufrst.app.trombi", Context.MODE_PRIVATE);

        return prefs.getBoolean(PREFS_MULTI_THREADING, true);
    }

    // Ajoute le style de la page et les balises de début de document
    private void appendHTMLStyle(StringBuilder sb){
        sb.append("<html>")
            .append("<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            .append("<style>")
            .append("@page {size:A4; margin:1cm;}")
            .append("html, body{width: 210mm;}")
            .append("img{width: 100%; margin-right: auto; margin-left: auto;}")
            .append("h1{font-size: 3em; text-align: center;}")
            .append("h2{font-size: 2.8em; text-align: center;}")
            .append("table{width: 100%; margin-bottom:100px; table-layout:fixed;}")
            .append("td{padding-bottom: 10px; vertical-align: top; word-wrap:break-word;}")
            .append(".name{padding-bottom: 40px;}")
            .append("td > *{display: block;}")
            .append("#table td{text-align: center; font-size: 2em}")
            .append("</style></head>")
            .append("<body><h1>").append(nomTrombi).append("</h1>");
    }

    private void appendHTMLEleves(StringBuilder sb){
        final int nbRows = computeRowsNumber(listeEleves.size(), nbCols);

        for(int i = 0; i < nbRows; i++){
            // Nouvelle ligne pour les photos
            sb.append("<tr>");

            // Stream 1: Aligner les images
            Stream<Eleve> eleves = processEleves(i);

            if(isMultiThreading())
                eleves = eleves.parallel();

            eleves.forEachOrdered(eleve -> sb.append(addElevePhoto(eleve)));

            // Nouvelle ligne pour les noms
            sb.append("</tr><tr>");

            // Stream 2: Aligner les noms
            eleves = processEleves(i);
            eleves.forEach(eleve -> sb.append("<td class=\"name\">")
                    .append(eleve.getNomPrenom())
                    .append("</td>"));

            sb.append("</tr>");
        }
    }

    public static class Builder {

        private List<Eleve> listeEleves;
        private boolean withDescription;
        private String descTrombi;
        private String nomTrombi;
        private Context context;
        private int nbCols;

        public Builder setNbCols(int nbCols){
            // Le switch considère que 0 est une valeur possible, donc on ajoute 1 pour éviter le 0
            this.nbCols = nbCols + 1;
            return this;
        }

        public Builder hasDescription(boolean withDescription){
            this.withDescription = withDescription;
            return this;
        }


        public Builder setListeEleves(List<Eleve> listeEleves){
            this.listeEleves = listeEleves;
            return this;
        }

        public Builder setNomTrombi(String nomTrombi){
            this.nomTrombi = nomTrombi;
            return this;
        }

        public Builder setDescTrombi(String descTrombi){
            this.descTrombi = descTrombi;
            return this;
        }

        public Builder setContext(Context context){
            this.context = context;
            return this;
        }

        public HTMLProvider build(){
            if(this.nomTrombi == null || this.nomTrombi.trim().isEmpty()){
                throw new IllegalStateException("Le nom ne peut pas être vide !");
            }

            if(this.nbCols < 0){
                throw new IllegalStateException("Le nombre de colonnes doit être supérieur à 0");
            }

            if(this.listeEleves == null){
                throw new IllegalStateException("La liste d'élèves ne peut pas être null !");
            }

            if(this.context == null){
                throw new IllegalStateException("Le contexte ne peut pas être null !");
            }

            return new HTMLProvider(this);
        }
    }
}
