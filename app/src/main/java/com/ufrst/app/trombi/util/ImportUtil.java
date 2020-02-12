package com.ufrst.app.trombi.util;

import androidx.annotation.WorkerThread;

import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.EleveGroupeJoin;
import com.ufrst.app.trombi.database.Groupe;
import com.ufrst.app.trombi.model.ImportedTrombi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

// Une classe pour importer un Trombinoscope, ses Elèves et ses Groupes
public class ImportUtil {

    private String filename;
    private String externalDirPath;
    private String nomTrombi;
    private List<String> elevesName;
    private Set<String> groupesName;

    public ImportUtil(String externalDirPath, String filename){
        this.externalDirPath = externalDirPath;
        this.filename = filename;

        init();
    }


    // Lit les données du fichier pour se préparer à l'importation
    private void init(){
        String filepath = externalDirPath + FileUtil.LIST_DIRECTORY + filename;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filepath)))){
            String line;

            // Préparation du Trombinoscope
            nomTrombi = filename.split("-")[0];

            while((line = reader.readLine()) != null){
                String[] lineWithGroups = line.split("\\|");

                elevesName.add(lineWithGroups[0]);

                // Spécification des groupes
                Arrays.stream(lineWithGroups)
                        .skip(1)
                        .forEach(groupesName::add);
            }
        } catch(IOException e){
            Logger.handleException(e);
        }
    }

    public String getNomTrombi(){
        return nomTrombi;
    }

    public List<String> getElevesName(){
        return elevesName;
    }

    public Set<String> getGroupesName(){
        return groupesName;
    }
}
