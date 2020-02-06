package com.ufrst.app.trombi.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.ufrst.app.trombi.database.Eleve;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// Classe qui abstrait le traitement des images
public class ImageUtil {

    // Donne le nom pour un fichier selon un élève
    public static String getPathNameForEleve(String externalDirPath, Eleve eleve){
        return externalDirPath + "/" + eleve.getNomPrenom() + "-"
                + eleve.getIdEleve() + System.currentTimeMillis() + ".jpeg";
    }

    // Sauvegarde une image pour un élève donné à une certaine position. Ne pas exécuter sur l'UI
    public File saveImage(Bitmap bitmap, String externalDirPath, Eleve eleve){
        File f = new File(getPathNameForEleve(externalDirPath, eleve));

        /*Matrix matrix = new Matrix();
        matrix.postRotate(90);

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);*/

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
