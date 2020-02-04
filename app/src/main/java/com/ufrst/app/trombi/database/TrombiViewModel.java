package com.ufrst.app.trombi.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TrombiViewModel extends AndroidViewModel {

    private TrombiRepository repository;

    // Cet attribut va nous permettre de changer les données contenues dans groupesWithEleves
    // selon sa valeur avant de proposer le contenu aux observeurs. Ainsi nous n'avons besoin que
    // d'un seul observeur au lieu d'en créer un dynamiquement avec chacun des groupes selon le choix
    // de l'utilisateur. Pour plus d'informations: https://developer.android.com/topic/libraries/architecture/livedata#transform_livedata
    private final MutableLiveData<Long> requestedIdGroupe = new MutableLiveData<>();
    public final LiveData<GroupeWithEleves> groupesWithEleves =
            Transformations.switchMap(requestedIdGroupe, this::getGroupeByIdWithEleves);

    public TrombiViewModel(@NonNull Application application){
        super(application);

        repository = new TrombiRepository(application);
    }

    // Permet de changer l'id du groupe observé : cf groupesWithEleves
    public void setIdGroup(long idGroupe){
        requestedIdGroupe.setValue(idGroupe);
    }


    // Trombinoscope________________________________________________________________________________
    public void insert(Trombinoscope trombi){repository.insert(trombi);}
    public void update(Trombinoscope trombi){repository.update(trombi);}
    public void delete(Trombinoscope trombi){repository.delete(trombi);}

    public LiveData<List<Trombinoscope>> getAllTrombis(){return repository.getAllTrombis();}
    public LiveData<Trombinoscope> getTrombiById(long idTrombi){return repository.getTrombiById(idTrombi);}
    public void softDeleteTrombi(long idTrombi){repository.softDeleteTrombi(idTrombi);}
    public void deleteSoftDeletedTrombis(){repository.deleteSoftDeletedTrombis();}

    public long insertAndRetrieveId(Trombinoscope trombi){return repository.insertAndRetrieveId(trombi);}


    // Groupe_______________________________________________________________________________________
    public void insert(Groupe groupe){repository.insert(groupe);}
    public void update(Groupe groupe){repository.update(groupe);}
    public void delete(Groupe groupe){repository.delete(groupe);}

    public LiveData<List<Groupe>> getAllGroupes(){return repository.getAllGroupes();}
    public LiveData<List<Groupe>> getGroupesByTrombi(long idTrombi){return repository.getGroupesByTrombi(idTrombi);}
    public void deleteGroupesForTrombi(long idTrombi){repository.deleteGroupesForTrombi(idTrombi);}


    // Eleve________________________________________________________________________________________
    public void insert(Eleve eleve){repository.insert(eleve);}
    public void update(Eleve eleve){repository.update(eleve);}
    public void delete(Eleve eleve){repository.delete(eleve);}

    public LiveData<List<Eleve>> getAllEleves(){return repository.getAllEleves();}
    public LiveData<List<Eleve>> getElevesByTrombi(long idTrombi){return repository.getElevesByTrombi(idTrombi);}
    public LiveData<Eleve> getEleveById(long idEleve){return repository.getEleveById(idEleve);}
    public void deleteElevesForTrombi(long idTrombi){repository.deleteElevesForTrombi(idTrombi);}
    public void softDeleteEleve(long idEleve){repository.softDeleteEleve(idEleve);}
    public void softDeleteElevesForTrombi(long idTrombi){repository.softDeleteElevesForTrombi(idTrombi);}
    public void deleteSoftDeletedEleves(){repository.deleteSoftDeletedEleves();}

    public int getElevesNumberByTrombi(long idTrombi){return repository.getElevesNumberByTrombi(idTrombi);}
    public long insertAndRetrieveId(Eleve eleve){return repository.insertAndRetrieveId(eleve);}


    // Eleve Groupe Join____________________________________________________________________________
    public void insert(EleveGroupeJoin eleveGroupeJoin){repository.insert(eleveGroupeJoin);}
    public void update(EleveGroupeJoin eleveGroupeJoin){repository.update(eleveGroupeJoin);}
    public void delete(EleveGroupeJoin eleveGroupeJoin){repository.delete(eleveGroupeJoin);}

    public LiveData<EleveWithGroups> getEleveByIdWithGroups(long idEleve){return repository.getEleveByIdWithGroups(idEleve);}
    public LiveData<List<GroupeWithEleves>> getGroupesWithEleves(){return repository.getGroupesWithEleves(); }

    public EleveWithGroups getEleveByIdWithGroupsNotLive(long idEleve){return repository.getEleveByIdWithGroupsNotLive(idEleve);}

    // Cette méthode va modifier le contenu des LiveData avant de propager aux observeurs
    // pour retirer les élèves qui ont étés soft delete
    public LiveData<GroupeWithEleves> getGroupeByIdWithEleves(long idGroupe) {
        LiveData<GroupeWithEleves> liveGroup = repository.getGroupeByIdWithEleves(idGroupe);

        return Transformations.map(liveGroup, group -> {
            List<Eleve> newEleves = group.getEleves().stream()
                    .filter(eleve -> !eleve.isDeleted())
                    .sorted(Comparator.comparing(Eleve::getNomPrenom))
                    .collect(Collectors.toList());

            GroupeWithEleves newGroup = new GroupeWithEleves();
            newGroup.eleves = newEleves;
            newGroup.groupe = group.getGroupe();

            return newGroup;
        });
    }
}
