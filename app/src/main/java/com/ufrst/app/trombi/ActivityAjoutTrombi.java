package com.ufrst.app.trombi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.Groupe;
import com.ufrst.app.trombi.database.TrombiViewModel;
import com.ufrst.app.trombi.database.Trombinoscope;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;

import static com.ufrst.app.trombi.ActivityMain.EXTRA_DESC;
import static com.ufrst.app.trombi.ActivityMain.EXTRA_ID;
import static com.ufrst.app.trombi.ActivityMain.EXTRA_NOM;

public class ActivityAjoutTrombi extends AppCompatActivity implements ImportAlertDialog.ImportDialogListener {

    public final int PICKFILE_RESULT_CODE = 0;

    private CoordinatorLayout coordinatorLayout;
    private RelativeLayout editRelativeLayout;
    private TextInputEditText etNom, etDesc;
    private TrombiViewModel trombiViewModel;
    private TextView tvNbEleve, tvNbGroupe;
    private Toolbar toolbar;
    private Button button;

    private long idTrombi;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_trombi);

        findViews();
        updateData();
        setListeners();

        // Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void findViews(){
        coordinatorLayout = findViewById(R.id.AJOUTTROMBI_coordinator);
        editRelativeLayout = findViewById(R.id.AJOUTTROMBI_infoLayout);
        etDesc = findViewById(R.id.AJOUTTROMBI_entrerDescription);
        tvNbGroupe = findViewById(R.id.AJOUTTROMBI_nbGroupe);
        button = findViewById(R.id.AJOUTTROMBI_btnValider);
        tvNbEleve = findViewById(R.id.AJOUTTROMBI_nbEleve);
        etNom = findViewById(R.id.AJOUTTROMBI_entrerNom);
        toolbar = findViewById(R.id.AJOUTTROMBI_toolbar);
    }

    // Actualise certaines données selon la raison pour laquelle cette activité est lancée
    private void updateData(){
        Intent intent = getIntent();

        // Récupération du ViewModel
        trombiViewModel = ViewModelProviders.of(this).get(TrombiViewModel.class);

        // Cas de la modification. Voire ActivityMain#setRecyclerViewAndViewModel
        if(intent.hasExtra(EXTRA_ID)){
            setTitle(R.string.AJOUTTROMBI_titleVar);

            // Récupération de l'id
            idTrombi = intent.getLongExtra(EXTRA_ID, -1);

            // Les champs d'édition doivent refléter le trombinoscope à modifier
            etNom.setText(intent.getStringExtra(EXTRA_NOM));
            etDesc.setText(intent.getStringExtra(EXTRA_DESC));

            // Affichage des composants permettant la modification d'un trombinoscope
            editRelativeLayout.setVisibility(View.VISIBLE);

            // Insertion des valeurs pour les groupes et les élèves
            trombiViewModel.getElevesByTrombi(idTrombi).observe(this, new Observer<List<Eleve>>() {
                @Override
                public void onChanged(List<Eleve> eleves){
                    tvNbEleve.setText(String.valueOf(eleves.size()));
                }
            });

            trombiViewModel.getGroupesByTrombi(idTrombi).observe(this, new Observer<List<Groupe>>() {
                @Override
                public void onChanged(List<Groupe> groupes){
                    tvNbGroupe.setText(String.valueOf(groupes.size()));
                }
            });

            // Permet de ne pas observer la valeur, mais performances réduites
            /*Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run(){
                    int i = trombiViewModel.getElevesNumberByTrombi(idTrombi);
                    tvNbEleve.post(() -> tvNbEleve.setText(String.valueOf(i)));
                }
            });*/

        } else{
            setTitle(R.string.AJOUTTROMBI_title);
        }
    }

    private void setListeners(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                saveTrombi();
            }
        });
    }

    // Prépare la sauvegarde du trombi qui sera finalisée dans ActivityMain
    // TODO: Ne pas rediriger vers le menu principal, mais vers la page de modification du trombi
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
        /*Intent intent = new Intent(ActivityAjoutTrombi.this, ActivityBDTest.class);
        startActivity(intent);*/

        Intent fileintent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        fileintent.addCategory(Intent.CATEGORY_OPENABLE);
        fileintent.setType("text/plain");

        try{
            startActivityForResult(fileintent, PICKFILE_RESULT_CODE);
        } catch(ActivityNotFoundException e){
            Snackbar.make(coordinatorLayout, R.string.AJOUTTROMBI_fichierImporteErr, Snackbar.LENGTH_LONG).show();
        }
    }

    // Traite le texte saisi par l'utilisateur lors de l'import d'un trombinoscope
    private void importTrombiText(final String nomTrombi, final String descTrombi, final String liste){
        String[] split = liste.split("\n");
        Trombinoscope trombi = new Trombinoscope(nomTrombi, descTrombi);
        long id = trombiViewModel.insertAndRetrieveId(trombi);

        for(String eleve : split){
            trombiViewModel.insert(new Eleve(eleve, id, ""));
        }

        Toast.makeText(this, R.string.AJOUTTROMBI_listeImportee, Toast.LENGTH_SHORT).show();
        finish();
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
        EditText etImportNom;
        EditText etImportDesc;
        EditText etImportListe;

        if(d != null){
            etImportNom = d.findViewById(R.id.AJOUTTROMBI_editTextImportNom);
            etImportDesc = d.findViewById(R.id.AJOUTTROMBI_editTextImportDesc);
            etImportListe = d.findViewById(R.id.AJOUTTROMBI_editTextImportListe);

            final String nomTrombi = etImportNom.getText().toString();
            final String descTrombi = etImportDesc.getText().toString();
            final String liste = etImportListe.getText().toString();

            if(nomTrombi.trim().isEmpty()){
                Snackbar.make(coordinatorLayout,
                        R.string.AJOUTTROMBI_listeImporteeErr,
                        Snackbar.LENGTH_LONG).show();

                return;
            }

            importTrombiText(nomTrombi, descTrombi, liste);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICKFILE_RESULT_CODE){
            if(resultCode == RESULT_OK && data.getData() != null){
                String filename = null;

                // Récupération du nom du fichier
                if(data.getData().getLastPathSegment() != null){
                    String[] split = data.getData().getLastPathSegment().split("/");
                    filename = split[split.length - 1];
                }

                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(
                                             new FileInputStream(
                                                     getExternalFilesDir(null) +
                                                     "/" + filename
                                             )
                                     )
                             )
                ){
                    trombiViewModel = ViewModelProviders.of(this).get(TrombiViewModel.class);
                    String nomTrombi = null;

                    // Récupère le nom du fichier sans fioritures
                    if(data.getData().getLastPathSegment() != null && filename != null){
                        nomTrombi = filename.split("-")[0];
                    }

                    Trombinoscope trombi = new Trombinoscope(nomTrombi, "");
                    long id = trombiViewModel.insertAndRetrieveId(trombi);

                    String line;
                    while((line = reader.readLine()) != null){
                        trombiViewModel.insert(new Eleve(line, id, ""));
                    }

                    Snackbar.make(coordinatorLayout,
                            R.string.AJOUTTROMBI_listeImportee,
                            Snackbar.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Snackbar.make(coordinatorLayout,
                            R.string.AJOUTTROMBI_fichierImporteErr,
                            Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }
}
