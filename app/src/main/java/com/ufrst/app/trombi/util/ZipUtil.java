package com.ufrst.app.trombi.util;

import android.content.Context;

import com.ufrst.app.trombi.database.EleveWithGroups;
import com.ufrst.app.trombi.database.Groupe;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    private static final int BUFFER_SIZE = 1024;
    private FileUtil fileUtil;

    public ZipUtil(Context context, String nomTrombi){
        fileUtil = new FileUtil(context, nomTrombi);
    }

    // Exporte un trombinoscope en zip
    public void exportToZip(List<EleveWithGroups> eleves){
        BufferedInputStream origin;

        String zipFile = fileUtil.getPathForExportedTrombi();

        try(ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))){
            byte[] data = new byte[BUFFER_SIZE];

            File photoDirectory = new File(fileUtil.getDirectoryForTrombiPhoto());

            if(photoDirectory.exists() && photoDirectory.isDirectory()){
                for(File file : Objects.requireNonNull(photoDirectory.listFiles())){
                    String filename = file.getName();
                    FileInputStream fi = new FileInputStream(file.getPath());
                    origin = new BufferedInputStream(fi, BUFFER_SIZE);

                    try{
                        ZipEntry entry = new ZipEntry(
                                filename.substring(filename.lastIndexOf(File.separator) + 1));
                        out.putNextEntry(entry);

                        int count;

                        while((count = origin.read(data, 0, BUFFER_SIZE)) != -1){
                            out.write(data, 0, count);
                        }
                    } finally{
                        origin.close();
                    }
                }

                // Fichier texte contenant les élèves et leurs groupes
                ZipEntry entry = new ZipEntry("trombi.txt");
                out.putNextEntry(entry);

                // Pour chaque élève on écrit le nom prénom
                for(EleveWithGroups eleve : eleves){
                    out.write(eleve.getEleves().getNomPrenom().getBytes());

                    // Pour chaque groupe de cet élève on écrit son nom
                    for(Groupe g : eleve.getGroupes()){
                        out.write(("|" + g.getNomGroupe()).getBytes());
                    }

                    out.write("\n".getBytes());
                }
            }
        } catch(IOException e){
            Logger.handleException(e);
        }
    }

    public void unzip(String exportedTrombiPath){
        try{
            ZipFile zipFile = new ZipFile(exportedTrombiPath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();

                String destPath = fileUtil.getPathForImportedTrombi() + File.separator + entry.getName();

                try(InputStream inputStream = zipFile.getInputStream(entry);
                    FileOutputStream outputStream = new FileOutputStream(destPath)
                ){
                    int data = inputStream.read();
                    while(data != -1){
                        outputStream.write(data);
                        data = inputStream.read();
                    }
                }
            }
        } catch(IOException e){
            Logger.handleException(e);
        }
    }

    public void importFromExtractedZip(String exportedTrombiPath){

    }
}
