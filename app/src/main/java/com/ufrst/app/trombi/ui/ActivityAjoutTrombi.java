package com.ufrst.app.trombi.ui;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.ufrst.app.trombi.ImportAlertDialog;
import com.ufrst.app.trombi.R;
import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.EleveGroupeJoin;
import com.ufrst.app.trombi.database.Groupe;
import com.ufrst.app.trombi.database.TrombiViewModel;
import com.ufrst.app.trombi.database.Trombinoscope;
import com.ufrst.app.trombi.model.EleveWithGroupsImport;
import com.ufrst.app.trombi.util.FileUtil;
import com.ufrst.app.trombi.util.ImportUtil;
import com.ufrst.app.trombi.util.ZipUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_DESC;
import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_ID;
import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_NOM;

public class ActivityAjoutTrombi extends AppCompatActivity implements ImportAlertDialog.ImportDialogListener {

    public final int PICKFILE_RESULT_CODE = 0;
    public final int PICKZIP_RESULT_CODE = 1;

    private ExtendedFloatingActionButton valider;
    private CoordinatorLayout coordinatorLayout;
    private RelativeLayout editRelativeLayout;
    private TextInputEditText etNom, etDesc;
    private TrombiViewModel trombiViewModel;
    private TextView tvNbEleve, tvNbGroupe;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.AppThemeDark);
        } else{
            setTheme(R.style.AppTheme);
        }

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
        valider = findViewById(R.id.AJOUTTROMBI_btnValider);
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
            valider.setText(R.string.U_enregistrer);

            // Récupération de l'id
            long idTrombi = intent.getLongExtra(EXTRA_ID, -1);

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
        valider.setOnClickListener(new View.OnClickListener() {
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
            etNom.setError(getResources().getString(R.string.U_required));
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

    private void importTrombiFile(){
        /*Intent intent = new Intent(ActivityAjoutTrombi.this, ActivityBDTest.class);
        startActivity(intent);*/

        Intent fileintent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        fileintent.addCategory(Intent.CATEGORY_OPENABLE);
        fileintent.setType("text/plain");

        try{
            startActivityForResult(fileintent, PICKFILE_RESULT_CODE);
        } catch(ActivityNotFoundException e){
            Snackbar.make(coordinatorLayout,
                    R.string.AJOUTTROMBI_fichierImporteErr,
                    Snackbar.LENGTH_LONG).show();
        }
    }

    // Traite le texte saisi par l'utilisateur lors de l'import d'un trombinoscope
    private void importTrombiText(final String nomTrombi, final String descTrombi, final String liste){
        Executors.newSingleThreadExecutor().execute(() -> {
            String[] split = liste.split("\n");
            Trombinoscope trombi = new Trombinoscope(nomTrombi, descTrombi);
            long id = trombiViewModel.insertAndRetrieveId(trombi);

            for(String eleve : split){
                trombiViewModel.insert(new Eleve(eleve, id, ""));
            }

            showToast(getResources().getText(R.string.AJOUTTROMBI_listeImportee));
        });

        finish();
    }

    // Importe un trombinoscope depuis un fichier zip
    // TODO: Changer le nom des dossiers quand le nom du trombi change dans l'application
    private void importTrombiFromZip(){
        Intent fileintent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        fileintent.addCategory(Intent.CATEGORY_OPENABLE);
        fileintent.setType("application/zip");

        try{
            startActivityForResult(fileintent, PICKZIP_RESULT_CODE);
        } catch(ActivityNotFoundException e){
            Snackbar.make(coordinatorLayout,
                    R.string.AJOUTTROMBI_fichierImporteErr,
                    Snackbar.LENGTH_LONG).show();
        }
    }

    private void triggerTrombiFromZipImport(String trombiName){
        ZipUtil zipUtil = new ZipUtil(this, trombiName);
        FileUtil fileUtil = new FileUtil(this, trombiName);

        zipUtil.unzip(fileUtil.getPathForExportedTrombi());
    }

    // Crée des Toast
    private void showToast(CharSequence text){
        runOnUiThread(() -> Toast.makeText(this, text, Toast.LENGTH_LONG).show());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        Intent intent = getIntent();

        // Cas de la modification. Le menu d'import n'est pas affiché
        if(!intent.hasExtra(EXTRA_ID)){
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.add_trombi_menu, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch(item.getItemId()){
            case R.id.AJOUTTROMBI_importerTrombi:
                return true;

            case R.id.AJOUTTROMBI_importerTexte:
                importTrombiFile();
                //importTrombiFromZip(); Non fonctionnel pour le moment
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
    public void onDialogPositiveClick(DialogFragment dialog){
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICKFILE_RESULT_CODE){
            if(resultCode == RESULT_OK && data.getData() != null &&
                    data.getData().getLastPathSegment() != null){

                Executors.newSingleThreadExecutor().execute(() -> {

                    // Récupération du nom du fichier
                    String[] split = data.getData().getLastPathSegment().split("/");
                    String filename = split[split.length - 1];

                    // Récupération du nom du trombinoscope sans fioritures
                    String nameTrombi = "Mon trombi";

                    if(filename != null){
                        nameTrombi = filename.split("-")[0];
                    }

                    // Création du trombinoscope en BD et récupération de son id
                    Trombinoscope trombi = new Trombinoscope(nameTrombi, "");
                    long idTrombi = trombiViewModel.insertAndRetrieveId(trombi);

                    // Récupération de la liste des noms des groupes et des élèves
                    ImportUtil importUtil = new ImportUtil(
                            getExternalFilesDir(null).getPath(), filename, idTrombi);
                    Set<String> uniqueGroups = importUtil.getUniqueGroups();
                    List<EleveWithGroupsImport> eleveWithGroups = importUtil.getEleveWithGroups();

                    // Liste des groupes pour ce trombinoscope
                    List<Groupe> groupes = new ArrayList<>();

                    // Instanciation des groupes et récupération de leurs ids dans la BD
                    for(String groupName : uniqueGroups){
                        Groupe g = new Groupe(groupName, idTrombi);
                        long idGroupe = trombiViewModel.insertAndRetrieveId(g);
                        g.setIdGroupe(idGroupe);
                        groupes.add(g);
                    }

                    for(EleveWithGroupsImport eleveWithGroup : eleveWithGroups){
                        long idEleve = trombiViewModel.insertAndRetrieveId(eleveWithGroup.getEleve());

                        // Itération sur les noms du groupe d'un élève
                        for(String s : eleveWithGroup.getGroups()){
                            for(Groupe g : groupes){
                                // L'élève appartient au groupe g
                                if(g.getNomGroupe().equals(s)){
                                    trombiViewModel.insert(
                                            new EleveGroupeJoin(idEleve, g.getIdGroupe(), idTrombi));
                                }
                            }
                        }
                    }

                    showToast(getResources().getText(R.string.AJOUTTROMBI_fichierImporte));
                });

                finish();
            }
        }

        if(requestCode == PICKZIP_RESULT_CODE){
            if(resultCode == RESULT_OK && data.getData() != null){
                Executors.newSingleThreadExecutor().execute(() ->
                        triggerTrombiFromZipImport(data.getData().toString()));
            }
        }
    }
}
