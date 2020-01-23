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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.EleveGroupeJoin;
import com.ufrst.app.trombi.database.Groupe;
import com.ufrst.app.trombi.database.TrombiViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static com.ufrst.app.trombi.ActivityMain.EXTRA_ID;
import static com.ufrst.app.trombi.ActivityMain.EXTRA_ID_E;
import static com.ufrst.app.trombi.ActivityMain.EXTRA_NOM_E;

public class ActivityAjoutEleve extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;
    private TrombiViewModel trombiViewModel;
    private TextInputEditText inputEditText;
    private MaterialButton button;
    private ChipGroup chipGroup;
    private Toolbar toolbar;

    private long idTrombi;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_eleve);

        getExtras();
        findViews();
        setListeners();
        setData();
        observeGroups();

        // Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.AJOUTELEVE_title);
    }

    private void getExtras(){
        Intent intent = getIntent();
        idTrombi = intent.getLongExtra(EXTRA_ID, -1);
    }

    private void findViews(){
        coordinatorLayout = findViewById(R.id.AJOUTELEVE_coordinator);
        inputEditText = findViewById(R.id.AJOUTELEVE_entrerNom);
        chipGroup = findViewById(R.id.AJOUTELEVE_chipsGroup);
        button = findViewById(R.id.AJOUTELEVE_btnValider);
        toolbar = findViewById(R.id.AJOUTELEVE_toolbar);
    }

    private void setListeners(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                saveEleve();
            }
        });
    }

    private void setData(){


        //TODO: Updater les données en cas d'édition, et changer le titre
    }

    private void observeGroups(){
        trombiViewModel = ViewModelProviders.of(this).get(TrombiViewModel.class);

        trombiViewModel.getGroupesByTrombi(idTrombi).observe(this, new Observer<List<Groupe>>() {
            @Override
            public void onChanged(List<Groupe> groupes){
                Executors.newSingleThreadExecutor().execute(() -> {
                    for(Groupe g : groupes){
                        setChips(g);
                    }
                });

                // On a besoin des valeurs une seule fois
                trombiViewModel.getGroupesByTrombi(idTrombi).removeObserver(this);
            }
        });
    }

    private void setChips(Groupe g){
        // Inflate la chips d'après mon layout customisé, afin de pouvoir changer l'apparence de la chips lors de la sélection
        Chip c = (Chip) getLayoutInflater()
                .inflate(R.layout.chips_choice, chipGroup, false);

        // Ajout d'un tag pour lier un objet à cette vue
        c.setTag(R.string.TAG_CHIPS_ID, g);

        // Texte de la chips
        c.setText(g.getNomGroupe()); //A changer: enlever + idGroupe

        // Ajout de la chips dans le groupe de chips
        chipGroup.post(new Runnable() {
            @Override
            public void run(){
                chipGroup.addView(c);
            }
        });
    }

    private void saveEleve(){
        String nom = inputEditText.getText().toString();

        if(nom.trim().isEmpty()){
            inputEditText.setError(getResources().getString(R.string.AJOUTELEVE_empty));
            return;
        }

        // Instanciation d'un élève
        Eleve eleve = new Eleve(nom, idTrombi, "");

        //TODO: insérer les groupes

        // Insertion d'un élève et récupération de son ID dans la BD
        long idEleve = trombiViewModel.insertAndRetrieveId(eleve);

        // On récupère les groupes associés aux chips sélectionnées et insère des XRef dans la BD
        for(int i=0; i < chipGroup.getChildCount(); i++){
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if(chip.isChecked()){
                Groupe g = (Groupe) chip.getTag(R.string.TAG_CHIPS_ID);

                if(g != null){
                    // XRef entre groupe et élève
                    trombiViewModel.insert(new EleveGroupeJoin(idEleve, g.getIdGroupe()));
                }
            }
        }

        //A changer: a suppr si on n'utilise plus de activityForResult
        Intent data = new Intent();
        data.putExtra(EXTRA_NOM_E, nom);

        // Récupération de l'id.
        // Si on ne récupère pas d'"EXTRA_ID", on prend -1 et on sait en prime que l'activité
        // a été lancée dans le but d'ajouter un élève et non de le modifier.
        // Si on a un EXTRA_ID, alors on modifie un éllève et nous changerons la bonne entrée dans la BD en conséquence
        long id = getIntent().getLongExtra(EXTRA_ID_E, -1);

        // On édite un trombi
        if(id != -1){
            data.putExtra(EXTRA_ID_E, id);
        }

        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return super.onSupportNavigateUp();
    }
}
