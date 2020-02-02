package com.ufrst.app.trombi.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.ufrst.app.trombi.database.Eleve;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        List<String> list = loadHTLMImages();
        return generateHTML(list);
    }

    // TODO: remplacer avec un stream ?
    private String generateHTML(List<String> listeBase64){
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
                Eleve eleve;

                try{
                    eleve = listeEleves.get(index);

                    if(eleve != null){
                        sb.append("<td>").append(eleve.getNomPrenom());

                        if(eleve.getPhoto() != null && !eleve.getPhoto().trim().isEmpty()){
                            sb.append("<img src=\"data:image/jpg;base64,")
                                    .append(listeBase64.get(index))
                                    .append("\" />");
                        }

                        sb.append("</td>");
                    }
                } catch(IndexOutOfBoundsException e){              // Fin de la liste atteinte, sortie
                    Logger.handleException(e);
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


    // TODO: optimiser, bannir la mutabilité partagée sur listNamePhoto,
    //  retirer l'objet EleveImage et créer une map à la place
    // Charge les images sous forme base64 et retourne une liste ordonnée des images
    private List<String> loadHTLMImages(){
        Log.v("__________________", Thread.currentThread().toString());

        // ArrayList contenant des objets pouvant être triés
        ArrayList<EleveImage> listNameAndPhotoBase64 = new ArrayList<>();

        // Stream parallèlisé pour les performances. Pour chaque eleve, on crée un objet EleveImage
        // qui peut être trié. Cette opération nous retourne les images dans le désordre
        listeEleves.stream()
                .parallel()                                             // Parallèlisé
                .filter(eleve -> !eleve.getPhoto().trim().isEmpty())    // L'élève a une photo
                .forEach(eleve -> listNameAndPhotoBase64.add(convertToBase64(eleve)));

        // Tri de la liste pour mettre les photos dans l'ordre
        listNameAndPhotoBase64.sort(EleveImage::compareTo);

        // Résultat
        ArrayList<String> base64List = new ArrayList<>();
        listNameAndPhotoBase64.forEach(eleveImage -> base64List.add(eleveImage.getBase64Image()));

        return base64List;
    }

    // TODO: vérifier si les images peuvent effectivement etre vides
    // Convertit l'image d'un élève en image Base64, pour l'afficher dans le HTML
    private EleveImage convertToBase64(Eleve eleve){
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
            return new EleveImage(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT), eleve.getNomPrenom());
        }

        // Image vide
        Log.v("_____________________________", "image vide");
        return new EleveImage("", eleve.getNomPrenom());
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
