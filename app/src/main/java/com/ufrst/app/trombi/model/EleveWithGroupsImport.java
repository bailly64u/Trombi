package com.ufrst.app.trombi.model;

import com.ufrst.app.trombi.database.Eleve;

import java.util.List;

public class EleveWithGroupsImport {

    private List<String> groups;
    private Eleve eleve;

    public EleveWithGroupsImport(List<String> groups, Eleve eleve){
        this.groups = groups;
        this.eleve = eleve;
    }

    public List<String> getGroups(){
        return groups;
    }

    public Eleve getEleve(){
        return eleve;
    }
}
