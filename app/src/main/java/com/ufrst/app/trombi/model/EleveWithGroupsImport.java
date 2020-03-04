package com.ufrst.app.trombi.model;

import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.EleveWithGroups;
import com.ufrst.app.trombi.database.Groupe;

import java.util.ArrayList;
import java.util.List;

public class EleveWithGroupsImport {

    private List<String> groupes;               // Liste des noms des groupes
    private Eleve eleve;

    public EleveWithGroupsImport(List<String> groups, Eleve eleve){
        this.groupes = groups;
        this.eleve = eleve;
    }

    public List<String> getGroups(){
        return groupes;
    }

    public Eleve getEleve(){
        return eleve;
    }
}
