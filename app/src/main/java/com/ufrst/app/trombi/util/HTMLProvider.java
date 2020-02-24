package com.ufrst.app.trombi.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import com.ufrst.app.trombi.database.Eleve;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

// Classe utilitaire pour générer le HTML et charger les images en parallèle. Builder Pattern
public class HTMLProvider {

    // Nombre recommandé de Thread pour le calcul des images.
    // Chaque Thread va contenir un bitmap en mémoire, donc ne pas abuser dessus.
    private final static int NUMBER_OF_THREADS = 3;

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
            return "Erreur nombre de colonnes";
        }

        StringBuilder sb = new StringBuilder();

        // Style et métadonnées
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

        if(withDescription)
            sb.append("<h2>").append(descTrombi).append("</h2>");

        sb.append("<table id=\"table\">");

        final int nbRows = computeRowsNumber(listeEleves.size(), nbCols);

        // Lignes
        for(int i = 0; i < nbRows; i++){
            sb.append("<tr>");

            // Boucle 1: Aligner les images
            for(int j = 0; j < nbCols; j++){
                Eleve eleve;

                try{
                    eleve = listeEleves.get(j + (i * nbCols));
                } catch(IndexOutOfBoundsException e){
                    // On est sur la dernière ligne, et il reste moins d'élèves que de colonnes
                    break;
                }

                if(eleve != null){
                    String base64image = convertToBase64(eleve);

                    // L'image est vide ou null, on met le placeholder
                    if(base64image == null || base64image.trim().equals("")){
                        Base64Placeholder placeholder = Base64Placeholder.getInstance(context);
                        sb.append(addElevePhoto(placeholder.getBase64Placeholder()));
                    } else{
                        sb.append(addElevePhoto(base64image));
                    }
                }
            }

            // Nouvelle ligne pour les noms
            sb.append("</tr><tr>");

            // Boucle 2: Aligner les noms
            for(int k = 0; k < nbCols; k++){
                Eleve eleve;

                try{
                    eleve = listeEleves.get(k + (i * nbCols));
                } catch(IndexOutOfBoundsException e){
                    // On est sur la dernière ligne, et il reste moins d'élèves que de colonnes
                    break;
                }

                if(eleve != null){
                    sb.append("<td class=\"name\">")
                            .append(eleve.getNomPrenom())
                            .append("</td>");
                }
            }

            sb.append("</tr>");
        }

        sb.append("</table>");
        sb.append("</body></html>");

        return sb.toString();
    }

    // Crée un <TD> avec l'image fournie
    private String addElevePhoto(String base64Image){
        return "<td><img src=\"data:image/jpg;base64," + base64Image + "\" /></td>";
    }

    private int computeRowsNumber(int nbEleve, int nbCols){
        return nbEleve % nbCols > 0 ? (nbEleve / nbCols) + 1 : nbEleve / nbCols;
    }

    // Charge les images sous forme base64 et retourne une liste ordonnée des images
    /*private List<EleveImage> loadHTLMImages(){
        // Stream parallélisé pour générer les images des élèves
        return listeEleves.stream()
                //.parallel()
                .map(eleve -> new EleveImage(convertToBase64(eleve), eleve.getNomPrenom()))
                .collect(Collectors.toList());

        //return processStream(stream);
    }*/

    // Execute la méthode terminale du stream dans une FJP défini pour éviter d'utiliser trop de threads
    /*private List<EleveImage> processStream(Stream<EleveImage> stream){
        // Contrôle du nombre de threads dans lesquels le stream va opérer
        ForkJoinPool pool = new ForkJoinPool(NUMBER_OF_THREADS);

        // Opération du stream dans la pool
        try{
            return pool.submit(() -> stream.collect(Collectors.toList())).get();
        } catch(ExecutionException | InterruptedException e){
            Logger.handleException(e);
            return Collections.emptyList();
        }
    }*/

    // Convertit l'image d'un élève en image Base64, pour l'afficher dans le HTML
    private String convertToBase64(Eleve eleve){
        if(!eleve.getPhoto().trim().isEmpty()){
            // Récupération de l'URI sous une autre forme
            String filePath = Uri.parse(eleve.getPhoto()).getPath();
            Bitmap bm = BitmapFactory.decodeFile(filePath);

            // Rotation de l'image, entraîne des problèmes de performances
            /*Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);*/

            if(bm != null){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 50, baos);
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

    // Objet contenant le nomPrenom d'un élève et sa photo en base 64
    // Permet de classer les élèves pour récupérer les photos dans l'ordre
    /*private class EleveImage {

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
    }*/
}
