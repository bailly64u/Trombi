package com.ufrst.app.trombi.database;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

// Utilise les annotations de Room 2.2.0 pour créer des relations many to many
// Ces objets contiendront un groupe et sa liste d'élèves.
public class EleveWithGroups {
    @Embedded
    Eleve eleve;                                          // Les champs de l'objet Eleve sont inclus

    @Relation(
            parentColumn = "id_eleve",
            entity = Groupe.class,
            entityColumn = "id_groupe",
            associateBy = @Junction(
                    value = EleveGroupeJoin.class,
                    parentColumn = "join_id_eleve",
                    entityColumn = "join_id_groupe"
            )
    )
    List<Groupe> groupes;                                     // Relation entre le groupes et les élèves, avec comme table de jointure EleveGroupJoin

    public Eleve getEleves() { return eleve; }
    public List<Groupe> getGroupes() { return groupes; }
}