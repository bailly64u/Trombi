package com.ufrst.app.trombi.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.webkit.WebView;

import com.ufrst.app.trombi.database.Eleve;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// Classe qui abstrait le traitement des images
public class ImageUtil {

    private static final String PHOTO_DIRECTORY = "/photos/";
    private static final String IMG_DIRECTORY = "/images/";
    private static final String JPEG = ".jpeg";

    // Donne le nom pour un fichier de photo selon un élève
    public static String getPathNameForEleve(String externalDirPath, Eleve eleve){
        String directory = externalDirPath + PHOTO_DIRECTORY;
        String filename = eleve.getNomPrenom() + "-"
                + eleve.getIdEleve() + System.currentTimeMillis() + JPEG;

        // En cas de problèmes de création de répertoire, la photo est stockée directement
        // dans le stockage externe
        if(!checkDirectory(new File(directory)))
            return externalDirPath + "/" + filename;

        return directory + "/" + filename;
    }

    // Sauvegarde une image de la webview. Ne pas exécuter sur l'UI
    public static boolean saveImageFromWebview(WebView webView, String externalDirPath,
                                            String nomTrombi){
        Bitmap bitmap = Bitmap.createBitmap(webView.getWidth(),
                webView.getHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        webView.draw(canvas);

        try{
            String directory = externalDirPath + IMG_DIRECTORY;

            // La création du répertoire à échouée
            if(!checkDirectory(new File(directory)))
                return false;

            FileOutputStream fos = new FileOutputStream(directory  + nomTrombi + JPEG);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            return true;
        } catch(IOException e){
            Logger.handleException(e);

            return false;
        }
    }

    // Crée un répertoire s'il n'exite pas
    private static boolean checkDirectory(File directory){
        if(!directory.exists()){
            return directory.mkdirs();
        }

        return true;
    }

    // Sauvegarde une photo pour un élève donné à une certaine position. Ne pas exécuter sur l'UI
    public File savePhotoForEleve(Bitmap bitmap, String externalDirPath, Eleve eleve){
        File f = new File(getPathNameForEleve(externalDirPath, eleve));

        try{
            FileOutputStream fos = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            return f;
        } catch(IOException e){
            Logger.handleException(e);

            return null;
        }
    }
}
