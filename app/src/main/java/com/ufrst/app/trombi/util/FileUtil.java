package com.ufrst.app.trombi.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.webkit.WebView;

import androidx.annotation.WorkerThread;

import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.EleveWithGroups;
import com.ufrst.app.trombi.database.Groupe;
import com.ufrst.app.trombi.database.Trombinoscope;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

// Classe qui abstrait le traitement des images et aide à la création des dossier pour trier les frichiers
// générés par l'application
public class FileUtil {

    private static final String EXPORT_DIRECTORY = "/export/";
    private static final String PHOTO_DIRECTORY = "/photos/";
    private static final String IMG_DIRECTORY = "/images/";
    private static final String PDF_DIRECTORY = "/pdfs/";
    static final String LIST_DIRECTORY = "/listes/";
    private static final String JPEG = ".jpeg";
    private static final String TXT = ".txt";
    private static final String PDF = ".pdf";

    private String externalDirPath;
    private String nomTrombi;

    public FileUtil(Context context, String nomTrombi){
        this.externalDirPath = context.getExternalFilesDir(null).getPath();
        this.nomTrombi = nomTrombi;
    }

    // Crée un répertoire s'il n'exite pas
    private static boolean checkDirectory(File directory){
        if(!directory.exists()){
            return !directory.mkdirs();
        }

        return false;
    }

    // Donne le chemin pour un fichier de photo selon un élève
    public String getPathNameForEleve(Eleve eleve){
        String directory = externalDirPath + PHOTO_DIRECTORY + File.separator + nomTrombi;
        String filename = eleve.getNomPrenom() + "-"
                + eleve.getIdEleve() + System.currentTimeMillis() + JPEG;

        // En cas de problèmes de création de répertoire, la photo est stockée directement
        // dans le stockage externe
        if(checkDirectory(new File(directory)))
            return externalDirPath + File.separator + filename;

        return directory + File.separator + filename;
    }

    // Donne le chemin pour un fichier d'export de liste de noms
    public String getPathForExportedList(){
        @SuppressLint("SimpleDateFormat")
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();

        String directory = externalDirPath + LIST_DIRECTORY;
        String filename = nomTrombi + "-" + df.format(calendar.getTime()) + TXT;

        // En cas de problèmes de création de répertoire, la liste est stockée directement
        // dans le stockage externe
        if(checkDirectory(new File(directory)))
            return externalDirPath + File.separator + filename;

        return directory + File.separator + filename;
    }

    public String getPathForExportedTrombi(){
        String directory = externalDirPath + EXPORT_DIRECTORY;
        String filename = nomTrombi + TXT;

        // En cas de problèmes de création de répertoire, la liste est stockée directement
        // dans le stockage externe
        if(checkDirectory(new File(directory)))
            return externalDirPath + File.separator + filename;

        return directory + File.separator + filename;
    }

    public String getPathForExportedPDF(){
        String directory = externalDirPath + EXPORT_DIRECTORY;
        String filename = nomTrombi + PDF;

        // En cas de problèmes de création de répertoire, la liste est stockée directement
        // dans le stockage externe
        if(checkDirectory(new File(directory)))
            return externalDirPath + File.separator + filename;

        return directory + File.separator + filename;
    }

    // Sauvegarde une image de la webview. Ne pas exécuter sur l'UI
    public boolean saveImageFromWebview(WebView webView, String nomTrombi){
        Bitmap bitmap = Bitmap.createBitmap(webView.getWidth(),
                webView.getHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        webView.draw(canvas);

        try{
            String directory = externalDirPath + IMG_DIRECTORY;

            // La création du répertoire à échouée
            if(checkDirectory(new File(directory)))
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

    // Sauvegarde une photo pour un élève donné à une certaine position. Ne pas exécuter sur l'UI
    public File savePhotoForEleve(Bitmap bitmap, Eleve eleve){
        File f = new File(getPathNameForEleve(eleve));

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

    // Ecrit une liste d'élèves avec leurs groupes
    // dans un fichier situé dans le stockage externe de l'app
    // Retourne true si le fichier existe déjà
    public boolean writeExportedTrombi(List<EleveWithGroups> eleves,
                                       boolean doErase){
        // Récupération du nom du fichier
        String path = getPathForExportedTrombi();

        // Le fichier est réécrit, on doit supprimer son contenu actuel
        if(doErase){
            try{
                PrintWriter writer = new PrintWriter(path);
                writer.print("");
                writer.close();
            } catch(FileNotFoundException e){
                Logger.handleException(e);
                return false;
            }
        }

        // Ecriture du fichier
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(path, true),
                                    StandardCharsets.UTF_8))){
            // Pour chaque élève on écrit le nom prénom
            for(EleveWithGroups eleve : eleves){
                writer.write(eleve.getEleves().getNomPrenom());

                // Pour chaque groupe de cet élève on écrit son nom
                for(Groupe g : eleve.getGroupes()){
                    writer.write("|" + g.getNomGroupe());
                }

                writer.write("\n");
            }
        } catch(IOException e){
            Logger.handleException(e);
            return false;
        }

        return true;
    }

    // Exporte une liste de noms, sans les groupes
    public boolean writeExportedList(List<Eleve> eleves){
        // Récupération du nom du fichier
        String path = getPathForExportedList();

        // Ecriture du fichier
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(path, true),
                                    StandardCharsets.UTF_8))){
            // Pour chaque élève on écrit le nom prénom
            for(Eleve eleve : eleves){
                writer.write(eleve.getNomPrenom());
                writer.write("\n");
            }
        } catch(IOException e){
            Logger.handleException(e);
            return false;
        }

        return true;
    }
}