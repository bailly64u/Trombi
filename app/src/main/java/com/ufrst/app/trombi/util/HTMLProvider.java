package com.ufrst.app.trombi.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import com.ufrst.app.trombi.database.Eleve;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Classe utilitaire pour générer le HTML et charger les images en parallèle. Builder Pattern
public class HTMLProvider {

    // Nombre recommandé de Thread pour le calcul des images.
    // Chaque Thread va contenir un bitmap en mémoire, donc ne pas abuser dessus.
    private final static int NUMBER_OF_THREADS = 3;

    private final List<Eleve> listeEleves;
    private final boolean withDescription;
    private final String descTrombi;
    private final String nomTrombi;
    private final int nbCols;

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
                .append("html, body{width: 210mm;}")                         // <html>, <body>
                //.append("img{width: ").append(100 / (nbCols + 1))
                .append("img{width: 100")
                .append("%;margin-right: auto; margin-left: auto;}")          // <img> transform:rotate(90deg);
                .append("h1{font-size: 3em; text-align: center;}")           // <h1>
                .append("h2{font-size: 2.8em; text-align: center;}")         // <h2>
                .append("table{width: 100%;}")                              // <table>
                .append("td{padding-bottom: 20px}")
                .append("td > *{display: block;}")
                .append("#table td{text-align: center; font-size: 2em}")
                .append("</style></head>")
                .append("<body><h1>").append(nomTrombi).append("</h1>");

        if(withDescription){
            sb.append("<h2>").append(descTrombi).append("</h2>");
        }

        sb.append("<table id=\"table\">");

        // Lignes
        while(!isLastRow){
            sb.append("<tr>");

            // Colonnes
            for(int j = 0; j < nbCols + 1; j++){
                try{
                    EleveImage eleveImage = listeBase64.get(index);

                    // Si l'élève à une image, on ajoute une balise
                    if(eleveImage != null){
                        sb.append("<td>");

                        if(eleveImage.getBase64Image() != null && !eleveImage.getBase64Image().trim().isEmpty()){
                            sb.append("<img src=\"data:image/jpg;base64,")
                                    .append(eleveImage.getBase64Image())
                                    .append("\" />");
                        }

                        sb.append(eleveImage.getNomPrenom())
                                .append("</td>");
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

        Logger.logV("HTML", sb.toString());

        return sb.toString();
    }

    // Charge les images sous forme base64 et retourne une liste ordonnée des images
    private List<EleveImage> loadHTLMImages(){
        // Stream parallélisé pour générer les images des élèves
        /*Stream<EleveImage> stream = */ return listeEleves.stream()
                //.parallel()
                .map(eleve -> new EleveImage(convertToBase64(eleve), eleve.getNomPrenom()))
                .collect(Collectors.toList());
//        try{
//            return processStream(stream);
//        } catch(InterruptedException | ExecutionException e){
//            Logger.handleException(e);
//            return Collections.emptyList();
//        }
    }

    // Execute la méthode terminale du stream dans une FJP défini pour éviter d'utiliser trop de threads
    private List<EleveImage> processStream(Stream<EleveImage> stream)
            throws ExecutionException, InterruptedException{
        // Contrôle du nombre de threads dans lesquels le stream va opérer
        ForkJoinPool pool = new ForkJoinPool(NUMBER_OF_THREADS);

        // Opération du stream dans la pool
        return pool.submit(() -> stream.collect(Collectors.toList()))
                .get();
    }

    // Convertit l'image d'un élève en image Base64, pour l'afficher dans le HTML
    private String convertToBase64(Eleve eleve){
        if(!eleve.getPhoto().trim().isEmpty()){
            // Récupération de l'URI sous une autre forme
            String filePath = Uri.parse(eleve.getPhoto()).getPath();
            Bitmap bm = BitmapFactory.decodeFile(filePath);

            /*Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);*/

            if(bm != null){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 10, baos);
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
    private class EleveImage {

        String base64Image;
        String nomPrenom;

        EleveImage(String base64Image, String nomPrenom){
            this.base64Image = base64Image;
            this.nomPrenom = nomPrenom;
        }

        String getBase64Image(){
            return base64Image;
        }

        String getNomPrenom(){
            return nomPrenom;
        }
    }
}
