package com.ufrst.app.trombi.util;

import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.Groupe;
import com.ufrst.app.trombi.database.Trombinoscope;

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
    private long idTrombi;

    private List<Groupe> groupesForTrombi;

    public ImportUtil(String externalDirPath, String filename, long idTrombi){
        this.externalDirPath = externalDirPath;
        this.filename = filename;
        this.idTrombi = idTrombi;

        init();
    }

    //
    //
    // ImportUtil doit retourner une liste d'objet contenant un élève et une liste de ses groupes
    // Il doit aussi retourner une list des groupes
    // Il faut ensuite que l'activité demande la liste des objets contenants un élève et sa liste de groupe
    // et insert les objets
    //

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

                // Créer un objet contenant un élève et sa liste de groupes
                // Stocker les groupes s'il n'existent pas


                // Spécification des groupes dans une liste, sans doublons
                /*Arrays.stream(lineWithGroups)
                        .skip(1)
                        .filter(groupeName -> !groupesName.contains(groupeName))
                        .forEach(groupesName::add);*/
            }
        } catch(IOException e){
            Logger.handleException(e);
        }
    }
}
