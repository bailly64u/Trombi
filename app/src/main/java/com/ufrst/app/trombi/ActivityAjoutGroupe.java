package com.ufrst.app.trombi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.ufrst.app.trombi.database.Groupe;
import com.ufrst.app.trombi.database.TrombiViewModel;

import static com.ufrst.app.trombi.ActivityMain.EXTRA_GROUPE_E;
import static com.ufrst.app.trombi.ActivityMain.EXTRA_ID;
import static com.ufrst.app.trombi.ActivityMain.EXTRA_ID_E;
import static com.ufrst.app.trombi.ActivityMain.EXTRA_ID_G;
import static com.ufrst.app.trombi.ActivityMain.EXTRA_NOM_E;
import static com.ufrst.app.trombi.ActivityMain.EXTRA_NOM_G;

public class ActivityAjoutGroupe extends AppCompatActivity {

    private ExtendedFloatingActionButton fab;
    private TextInputEditText inputEditText;
    private TrombiViewModel trombiViewModel;
    private Toolbar toolbar;

    boolean isEditMode = false;                 // false: ajout, true: modification élève
    private String nomGroupe;
    private long idTrombi;
    private long idGroupe;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_groupe);

        findViews();
        getExtras();
        getViewModel();
        setListeners();

        // Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.AJOUTGROUPE_title);
    }

    private void findViews(){
        inputEditText = findViewById(R.id.AJOUTGROUPE_entrerNom);
        toolbar = findViewById(R.id.AJOUTGROUPE_toolbar);
        fab = findViewById(R.id.AJOUTGROUPE_btnValider);
    }

    private void getExtras(){
        Intent intent = getIntent();
        idTrombi = intent.getLongExtra(EXTRA_ID, -1);
        idGroupe = intent.getLongExtra(EXTRA_ID_G, -1);

        if(intent.hasExtra(EXTRA_NOM_G)){
            isEditMode = true;
            nomGroupe = intent.getStringExtra(EXTRA_NOM_G);
            inputEditText.setText(nomGroupe);
        } else{
            nomGroupe = null;
        }
    }

    private void getViewModel(){
        trombiViewModel = ViewModelProviders.of(this).get(TrombiViewModel.class);
    }

    private void setListeners(){
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                saveGroup();
            }
        });
    }

    private void saveGroup(){
        String nom = inputEditText.getText().toString();

        if(nom.trim().isEmpty()){
            inputEditText.setError(getResources().getString(R.string.U_required));
            return;
        }

        // Instanciation d'un groupe
        Groupe groupe = new Groupe(nom, idTrombi);

        // Cas de l'ajout du groupe, puis de la modification
        if(!isEditMode){
            // Insertion d'un groupe
            trombiViewModel.insert(groupe);
        } else {
            groupe.setIdGroupe(idGroupe);
            trombiViewModel.update(groupe);
        }

        // Retour à l'activitré appelante
        Intent data = new Intent();
        setResult(RESULT_OK, data);

        finish();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return super.onSupportNavigateUp();
    }
}
