package com.ufrst.app.trombi.util;

import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.model.EleveWithGroupsImport;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Une classe pour importer un Trombinoscope, ses Elèves et ses Groupes
public class ImportUtil {

    private List<EleveWithGroupsImport> eleveWithGroups = new ArrayList<>();
    private Set<String> uniqueGroups = new HashSet<>();
    private String filename;
    private String externalDirPath;
    private long idTrombi;

    public ImportUtil(String externalDirPath, String filename, long idTrombi){
        this.externalDirPath = externalDirPath;
        this.filename = filename;
        this.idTrombi = idTrombi;

        init();
    }

    // Lit les données du fichier pour se préparer à l'importation
    private void init(){
        String filepath = externalDirPath + FileUtil.LIST_DIRECTORY + filename;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filepath)))){
            String line;

            while((line = reader.readLine()) != null){
                List<String> lineWithGroups = Arrays.asList(line.split("\\|"));

                String eleveName = lineWithGroups.get(0);
                List<String> groupesNames = lineWithGroups.subList(1, lineWithGroups.size());

                // Création d'un objet contenant un élève et ses groupes
                Eleve e = new Eleve(eleveName, idTrombi, "");
                EleveWithGroupsImport eleve = new EleveWithGroupsImport(groupesNames, e);
                eleveWithGroups.add(eleve);

                // Les groupes sont ajoutés dans le set, éliminant ainsi les doublons
                uniqueGroups.addAll(groupesNames);
            }
        } catch(IOException e){
            Logger.handleException(e);
        }
    }

    public List<EleveWithGroupsImport> getEleveWithGroups(){
        return eleveWithGroups;
    }

    public Set<String> getUniqueGroups(){
        return uniqueGroups;
    }

}
