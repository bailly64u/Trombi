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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

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

    // Spécifie l'ordre des traitements à l'aide d'un CompletableFuture
    public CompletableFuture<String> doHTML(){
        /*CompletableFuture.supplyAsync(this::loadImages)
                .exceptionally(throwable -> {
                    Logger.handleException(throwable);
                    return null;
                })
                .thenAccept(this::generateHTML);*/

        CompletableFuture<String> future = new CompletableFuture<>();
        future.supplyAsync(this::loadHTLMImages)
                .exceptionally(throwable -> {
                    Logger.handleException(throwable);
                    return null;
                })
                .thenApply(this::generateHTML)
                .thenAccept(s -> Log.v("______________________", s));

        return future;
    }

    public String generateHTML(List<String> listeBase64){
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
                    eleve = listeEleves.get(index++);

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
                    isLastRow = true;
                    break;
                }
            }

            sb.append("</tr>");
        }

        sb.append("</table>");
        sb.append("</body></html>");

        return sb.toString();
    }

    // DOit retrouner un completableFuture ?
    // Charge les images sous forme base64
    public List<String> loadHTLMImages(){
        Log.v("__________________", Thread.currentThread().toString());

        ArrayList<String> base64List = new ArrayList<>();

        listeEleves.forEach(eleve -> Executors.newSingleThreadExecutor().execute(() -> {
                    Log.v("_________Computing_____", Thread.currentThread().toString());
                    if(!eleve.getPhoto().trim().isEmpty()){
                        // Récupération de l'URI sous une autre forme
                        String filePath = Uri.parse(eleve.getPhoto()).getPath();
                        Bitmap bm = BitmapFactory.decodeFile(filePath);

                        if(bm != null){
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bm.compress(Bitmap.CompressFormat.JPEG, 50, baos); //50 OK, voire plus
                            try{
                                baos.close();
                            } catch(IOException e){
                                //handleException(e);
                            }

                            bm = null;
                            base64List.add(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
                        }
                    }

                    base64List.add("image error");
                }));


        try{
            Thread.sleep(5000);
        } catch(InterruptedException e){
            e.printStackTrace();
        }

        return base64List;
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

            if(this.nbCols < 1){
                throw new IllegalStateException("Le nombre de colonnes doit être supérieur à 0");
            }

            if(this.listeEleves == null || this.listeEleves.isEmpty()){
                throw new IllegalStateException("La liste d'élèves ne peut pas être vide !");
            }

            return new HTMLProvider(this);
        }
    }
}
