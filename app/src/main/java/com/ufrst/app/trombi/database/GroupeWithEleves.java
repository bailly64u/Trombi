package com.ufrst.app.trombi.database;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

public class GroupeWithEleves {
    @Embedded
    Groupe groupe;                                          // Les champs de l'objet groupes sont inclus

    @Relation(
            parentColumn = "id_groupe",
            entity = Eleve.class,
            entityColumn = "id_eleve",
            associateBy = @Junction(
                    value = EleveGroupeJoin.class,
                    parentColumn = "join_id_groupe",
                    entityColumn = "join_id_eleve"
            )
    )
    List<Eleve> eleves;                                     // Relation entre le groupes et les élèves, avec comme table de jointure EleveGroupJoin

    public Groupe getGroupe() { return groupe; }
    public List<Eleve> getEleves() { return eleves; }
}
