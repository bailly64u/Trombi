package com.ufrst.app.trombi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.EleveGroupeJoin;
import com.ufrst.app.trombi.database.EleveWithGroups;
import com.ufrst.app.trombi.database.Groupe;
import com.ufrst.app.trombi.database.TrombiViewModel;

import java.util.List;
import java.util.concurrent.Executors;

import static com.ufrst.app.trombi.ActivityMain.EXTRA_ID;
import static com.ufrst.app.trombi.ActivityMain.EXTRA_ID_E;
import static com.ufrst.app.trombi.ActivityMain.EXTRA_NOM_E;

public class ActivityAjoutEleve extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;
    private ExtendedFloatingActionButton fab;
    private TrombiViewModel trombiViewModel;
    private TextInputEditText inputEditText;
    private MaterialButton buttonGroup;
    private LinearLayout linearLayout;
    private TextView emptyTextView;
    private ChipGroup chipGroup;
    private Toolbar toolbar;

    private String nomPrenomEleve;
    boolean isEditMode = false;                 // false: ajout, true: modification élève
    private long idTrombi;
    private long idEleve;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_eleve);

        getExtras();
        findViews();
        setListeners();
        getGroupesForEleve();
        updateData();

        // Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.AJOUTELEVE_title);
    }

    private void getExtras(){
        Intent intent = getIntent();
        idTrombi = intent.getLongExtra(EXTRA_ID, -1);
        if(intent.hasExtra(EXTRA_NOM_E) && intent.hasExtra(EXTRA_ID_E)){
            isEditMode = true;
            nomPrenomEleve = intent.getStringExtra(EXTRA_NOM_E);
            idEleve = intent.getLongExtra(EXTRA_ID_E, -1);
        } else{
            nomPrenomEleve = null;
        }
    }

    private void findViews(){
        coordinatorLayout = findViewById(R.id.AJOUTELEVE_coordinator);
        linearLayout = findViewById(R.id.AJOUTELEVE_chipsLayout);
        buttonGroup = findViewById(R.id.AJOUTELEVE_buttonGroup);
        inputEditText = findViewById(R.id.AJOUTELEVE_entrerNom);
        chipGroup = findViewById(R.id.AJOUTELEVE_chipsGroup);
        emptyTextView = findViewById(R.id.AJOUTELEVE_empty);
        toolbar = findViewById(R.id.AJOUTELEVE_toolbar);
        fab = findViewById(R.id.AJOUTELEVE_btnValider);
    }

    private void setListeners(){
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                saveEleve();
            }
        });

        buttonGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent intent = new Intent(ActivityAjoutEleve.this, ActivityListGroupe.class);
                intent.putExtra(EXTRA_ID, idTrombi);

                startActivity(intent);
            }
        });
    }

    // TODO: AntiPettern, observer d'observer, la liste des groupes fait n'importe quoi car
    //  elle est observé en cascade dès qu'il y a une modification. Remplacer par une transformation

    // Récupère les groupes auxquels l'élève appartient
    private void getGroupesForEleve(){
        trombiViewModel = ViewModelProviders.of(this).get(TrombiViewModel.class);

        trombiViewModel.getEleveByIdWithGroups(idEleve).observe(this, new Observer<EleveWithGroups>() {
            @Override
            public void onChanged(EleveWithGroups eleveWithGroups){
                if(eleveWithGroups != null){
                    List<Groupe> eleveGroups = eleveWithGroups.getGroupes();
                    observeGroupesForTrombi(eleveGroups);
                } else{
                    observeGroupesForTrombi(null);
                }

                // On a besoin des valeurs une seule fois
                trombiViewModel.getEleveByIdWithGroups(idEleve).removeObserver(this);
            }
        });
    }

    // GetGroupesForEleve appelle cette fonction, et lui passe la liste des groupes
    // auxquels l'élève appartient
    private void observeGroupesForTrombi(List<Groupe> groupsToCheck){
        trombiViewModel.getGroupesByTrombi(idTrombi).observe(this, new Observer<List<Groupe>>() {
            @Override
            public void onChanged(List<Groupe> groupes){
                // La liste des groupes n'est pas vide
                if(groupes.size() != 0){
                    Executors.newSingleThreadExecutor().execute(() ->
                            setChips(groupes, groupsToCheck));
                } else{
                    emptyTextView.setVisibility(View.VISIBLE);
                }

                // On a besoin des valeurs une seule fois
                trombiViewModel.getGroupesByTrombi(idTrombi).removeObserver(this);
            }
        });
    }

    // Appellé par observeGroupesForTrombi en passant la liste des groupes du trombi
    // et la liste de groupes auxquels l'élève appartient
    // Ne pas exécuter sur le ThreadUI
    private void setChips(List<Groupe> groups, List<Groupe> groupsToCheck){
        for(Groupe g : groups){
            Log.v("____________________", "bruh");

            // Inflate la chips d'après mon layout customisé, afin de pouvoir changer l'apparence de la chips lors de la sélection
            Chip c = (Chip) getLayoutInflater()
                    .inflate(R.layout.chips_choice, chipGroup, false);

            // Ajout d'un tag pour lier un objet à cette vue
            c.setTag(R.string.TAG_CHIPS_ID, g);

            // Check la chips si l'émève appartient au groupe
            if(groupsToCheck != null && groupsToCheck.contains(g)){
                c.post( () -> c.setChecked(true));  // View#post permet de donner un executable dans les thread UI
            }

            // Texte de la chips
            c.setText(g.getNomGroupe()); //A changer: enlever + idGroupe

            // Ajout de la chips dans le groupe de chips
            chipGroup.post(() -> chipGroup.addView(c));
        }
    }

    private void updateData(){
        // Cas de la création d'élève puis de la modification
        if(nomPrenomEleve == null){
            fab.setText(R.string.U_valider);
        } else{
            inputEditText.setText(nomPrenomEleve);
        }
    }

    // Enregistre un nouvel élève dans la BD, avec ses groupes s'il y en a
    private void saveEleve(){
        String nom = inputEditText.getText().toString();

        if(nom.trim().isEmpty()){
            inputEditText.setError(getResources().getString(R.string.AJOUTELEVE_empty));
            return;
        }

        // Instanciation d'un élève
        Eleve eleve = new Eleve(nom, idTrombi, "");

        // Cas de l'ajout de l'élève, puis de la modification
        if(!isEditMode){
            // Insertion d'un élève et récupération de son ID dans la BD
            long idEleve = trombiViewModel.insertAndRetrieveId(eleve);
            setGroupForEleve(idEleve);
        } else {
            eleve.setIdEleve(idEleve);
            trombiViewModel.update(eleve);
            setGroupForEleve(idEleve);
        }

        // Retour à l'activitré appelante
        Intent data = new Intent();
        setResult(RESULT_OK, data);

        finish();
    }

    // Alimente la cross ref entre groupe et élèves
    private void setGroupForEleve(long idEleve){
        // On récupère les groupes associés aux chips sélectionnées et insère des XRef dans la BD
        for(int i=0; i < chipGroup.getChildCount(); i++){
            Chip chip = (Chip) chipGroup.getChildAt(i);
            Groupe g = (Groupe) chip.getTag(R.string.TAG_CHIPS_ID);

            if(g != null){
                // Ajout de la XRef pour toutes les chips checkées (si il existe déja il sera ignoré
                // cf EleveGroupJoin#insert pour plus de détails
                // puis retrait de la XRef pour toutes les chips pas checkées
                // si Room ne trouve pas d'item à supprimer, il ne fait rien
                if(chip.isChecked()){
                    trombiViewModel.insert(new EleveGroupeJoin(idEleve, g.getIdGroupe()));
                } else {
                    trombiViewModel.delete(new EleveGroupeJoin(idEleve, g.getIdGroupe()));
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return super.onSupportNavigateUp();
    }
}
