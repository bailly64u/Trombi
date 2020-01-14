package com.ufrst.app.trombi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.TrombiViewModel;
import com.ufrst.app.trombi.database.Trombinoscope;

import java.util.List;

import static com.ufrst.app.trombi.ActivityMain.EXTRA_DESC;
import static com.ufrst.app.trombi.ActivityMain.EXTRA_ID;
import static com.ufrst.app.trombi.ActivityMain.EXTRA_NOM;

public class ActivityAjoutTrombi extends AppCompatActivity implements ImportAlertDialog.ImportDialogListener {

    private TextInputEditText etNom, etDesc;
    private CoordinatorLayout mCoordinatorLayout;
    private Toolbar mToolbar;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_trombi);

        findViews();
        updateData();
        setListeners();

        // Toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void findViews(){
        mCoordinatorLayout = findViewById(R.id.AJOUTTROMBI_coordinator);
        etDesc = findViewById(R.id.AJOUTTROMBI_entrerDescription);
        mButton = findViewById(R.id.AJOUTTROMBI_btnValider);
        mToolbar = findViewById(R.id.AJOUTTROMBI_toolbar);
        etNom = findViewById(R.id.AJOUTTROMBI_entrerNom);
    }

    // Actualise certaines données selon la raison pour laquelle cette activité est lancée
    private void updateData(){
        Intent intent = getIntent();

        // Cas de la modification. Voire ActivityMain#setRecyclerViewAndViewModel
        if(intent.hasExtra(EXTRA_ID)){
            setTitle(R.string.AJOUTTROMBI_titleVar);

            // Les champs d'édition doivent refléter le trombinoscope à modifier
            etNom.setText(intent.getStringExtra(EXTRA_NOM));
            etDesc.setText(intent.getStringExtra(EXTRA_DESC));
        } else{
            setTitle(R.string.AJOUTTROMBI_title);
        }
    }

    private void setListeners(){
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                saveTrombi();
            }
        });
    }

    // Prépare la sauvegarde du trombi qui sera finalisée dans ActivityMain
    private void saveTrombi(){
        String nom = etNom.getText().toString();
        String desc = etDesc.getText().toString();

        if(nom.trim().isEmpty()){
            etNom.setError(getResources().getString(R.string.AJOUTTROMBI_empty));
            return;
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_NOM, nom);
        data.putExtra(EXTRA_DESC, desc);

        // Récupération de l'id.
        // Si on ne récupère pas d'"EXTRA_ID", on prend -1 et on sait en prime que l'activité
        // a été lancée dans le but d'ajouter un trombi.
        // Si on a un EXTRA_ID, alors on modifie un trombi et nous changerons la bonne entrée dans la BD en conséquence
        long id = getIntent().getLongExtra(EXTRA_ID, -1);

        // On édite un trombi
        if(id != -1){
            data.putExtra(EXTRA_ID, id);
        }

        setResult(RESULT_OK, data);
        finish();
    }

    private void importTrombiFile(){ //A changer
        Intent intent = new Intent(ActivityAjoutTrombi.this, ActivityBDTest.class);
        startActivity(intent);
    }

    // Traite le texte saisi par l'utilisateur lors de l'import d'un trombinoscope
    private void parseText(final String text){
        String[] split = text.split("\n");
        TrombiViewModel trombiViewModel = ViewModelProviders.of(this).get(TrombiViewModel.class);
        Trombinoscope trombi = new Trombinoscope("Trombinoscope importé",
                "Modifiez moi en me maintenant");
        long id = trombiViewModel.insertAndRetrieveId(trombi);

        for(String eleve : split){
            trombiViewModel.insert(new Eleve(eleve, id, ""));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_trombi_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch(item.getItemId()){
            case R.id.AJOUTTROMBI_importerTrombi:
                return true;

            case R.id.AJOUTTROMBI_importerTexte:
                Toast.makeText(this, "bruh2", Toast.LENGTH_SHORT).show();
                importTrombiFile();
                return true;

            case R.id.AJOUTTROMBI_importerFichier:
                ImportAlertDialog dialog = new ImportAlertDialog();
                dialog.show(getSupportFragmentManager(), "import");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return super.onSupportNavigateUp();
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Dialog d = dialog.getDialog();
        EditText etImport;

        if(d != null){
            etImport = d.findViewById(R.id.AJOUTTROMBI_editTextImport);
            String text = etImport.getText().toString();
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            parseText(text);
        }
    }
}
