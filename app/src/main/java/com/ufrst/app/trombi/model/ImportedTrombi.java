package com.ufrst.app.trombi.model;

import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.EleveGroupeJoin;
import com.ufrst.app.trombi.database.Groupe;

import java.util.List;

// Représente un trombinoscope importé, avec ses élèves, ses groupes et les associations entre
// ces derniers
public class ImportedTrombi {

    private String nomTrombi;
    private List<Eleve> eleves;
    private List<Groupe> groupes;
    private List<EleveGroupeJoin> joins;

    public ImportedTrombi(String nomTrombi, List<Eleve> eleves, List<Groupe> groupes, List<EleveGroupeJoin> joins){
        this.nomTrombi = nomTrombi;
        this.eleves = eleves;
        this.groupes = groupes;
        this.joins = joins;
    }

    public String getNomTrombi(){
        return nomTrombi;
    }

    public List<Eleve> getEleves(){
        return eleves;
    }

    public List<Groupe> getGroupes(){
        return groupes;
    }

    public List<EleveGroupeJoin> getJoins(){
        return joins;
    }
}
