package com.ufrst.app.trombi.model;

import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.EleveWithGroups;
import com.ufrst.app.trombi.database.Groupe;

import java.util.ArrayList;
import java.util.List;

public class EleveWithGroupsImport {

    private List<Groupe> dbGroupes;             // Liste des groupes avec leurs ids en DB
    private List<String> groupes;               // Liste des noms des groupes
    private Eleve eleve;

    public EleveWithGroupsImport(List<String> groups, Eleve eleve){
        this.groupes = groups;
        this.eleve = eleve;
    }

    // Retourne des entités Room pour les insérer dans la BD
//    public List<EleveWithGroups> getEleveWithGroups(List<Groupe> dbGroupes){
//        List<EleveWithGroups> eleveWithGroups = new ArrayList<>();
//
//        for(String groupName : groupes){
//            if(groupes.contains())
//            eleveWithGroups.add()
//        }
//    }

    public List<String> getGroups(){
        return groupes;
    }

    public Eleve getEleve(){
        return eleve;
    }

    public void setDbGroupes(List<Groupe> dbGroupes){
        this.dbGroupes = dbGroupes;
    }
}
