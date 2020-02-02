package com.ufrst.app.trombi.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.ufrst.app.trombi.database.Eleve;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

// Classe utilitaire pour générer le HTML et charger les images en parallèle. Builder Pattern
public class HTMLProvider {

    private List<Eleve> listeEleves;
    private boolean withDescription;
    private String descTrombi;
    private String nomTrombi;
    private int nbCols;

    private HTMLProvider(final Builder builder){
        withDescription = builder.withDescription;
        listeEleves = builder.listeEleves;
        descTrombi = builder.descTrombi;
        nomTrombi = builder.nomTrombi;
        nbCols = builder.nbCols;
    }

    // Seule méthode publique de cet objet qui permet de retourner le HTML à afficher pour un trombi
    public String doHTML(){
        return generateHTML(loadHTLMImages());
    }

    // Ecrit les balises HTML en s'adaptant aux attributs de classe
    private String generateHTML(List<EleveImage> listeBase64){
        boolean isLastRow = false;              // Détermine si la dernière ligne affichée était la dernière
        int index = 0;                          // Indexe a chosir dans la liste d'élèves

        // Ne peut pas se produire, sauf si l'API du téléphone est inférieure à 26
        // Les APIs inférieures à 26 ne supportent pas l'attribut "min" des Seekbar
        if(nbCols == -1){
            return "Erreur nombre de colonnes";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html>")
                .append("<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                .append("<style>")
                .append("@page {size:A4; margin:1cm;}")                     // Page
                .append("html, body{width:210mm;}")                         // <html>, <body>
                .append("img{width:").append(100 / (nbCols + 1)).append("%;}")  // <img>
                .append("h1{font-size: 3em; text-align:center;}")           // <h1>
                .append("h2{font-size: 2.8em; text-align:center;}")         // <h2>
                .append("table{width: 100%;}")                              // <table>
                .append("td{background-color:red;}")
                .append("</style></head>")
                .append("<body><h1>").append(nomTrombi).append("</h1>");

        if(withDescription){
            sb.append("<h2>").append(descTrombi).append("</h2>");
        }

        sb.append("<table>");

        // Lignes
        while(!isLastRow){
            sb.append("<tr>");

            // Colonnes
            for(int j = 0; j < nbCols + 1; j++){
                try{
                    EleveImage eleveImage = listeBase64.get(index);

                    if(eleveImage != null){
                        sb.append("<td>").append(eleveImage.getNomPrenom());

                        // Si l'élève à une image, on ajoute une balise
                        if(eleveImage.getBase64Image() != null && !eleveImage.getBase64Image().trim().isEmpty()){
                            sb.append("<img src=\"data:image/jpg;base64,")
                                    .append(eleveImage.getBase64Image())
                                    .append("\" />");
                        }

                        sb.append("</td>");
                    }
                } catch(IndexOutOfBoundsException e){              // Fin de la liste atteinte, sortie
                    //Logger.handleException(e);
                    isLastRow = true;
                    break;
                }

                index++;
            }

            sb.append("</tr>");
        }

        sb.append("</table>");
        sb.append("</body></html>");

        return sb.toString();
    }

    // Charge les images sous forme base64 et retourne une liste ordonnée des images
    private List<EleveImage> loadHTLMImages(){
        Log.v("__________________", Thread.currentThread().toString());

        // Stream parallélisé pour générer les images des élèves
        return listeEleves.stream()
                .parallel()
                .map(eleve -> new EleveImage(convertToBase64(eleve), eleve.getNomPrenom()))
                .collect(Collectors.toList());
    }

    // Convertit l'image d'un élève en image Base64, pour l'afficher dans le HTML
    private String convertToBase64(Eleve eleve){
        // Récupération de l'URI sous une autre forme
        String filePath = Uri.parse(eleve.getPhoto()).getPath();
        Bitmap bm = BitmapFactory.decodeFile(filePath);

        if(bm != null){
            Log.v("_________________________", Thread.currentThread().toString());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 10, baos);
            try{
                baos.close();
            } catch(IOException e){
                Logger.handleException(e);
            }
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        }

        // Image vide
        return "";
    }

    public static class Builder {
        private List<Eleve> listeEleves;
        private boolean withDescription;
        private String descTrombi;
        private String nomTrombi;
        private int nbCols;

        public Builder setNbCols(int nbCols){
            this.nbCols = nbCols;
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

            return new HTMLProvider(this);
        }
    }

    // Objet contenant le nomPrenom d'un élève et sa photo en base 64
    // Permet de classer les élèves pour récupérer les photos dans l'ordre
    private class EleveImage implements Comparable<EleveImage> {
        String base64Image;
        String nomPrenom;

        EleveImage(String base64Image, String nomPrenom){
            this.base64Image = base64Image;
            this.nomPrenom = nomPrenom;
        }

        @Override
        public int compareTo(EleveImage o){
            return this.nomPrenom.compareTo(o.getNomPrenom());
        }

        String getBase64Image(){
            return base64Image;
        }

        String getNomPrenom(){
            return nomPrenom;
        }
    }
}
