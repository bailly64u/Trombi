package com.ufrst.app.trombi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.Groupe;
import com.ufrst.app.trombi.database.GroupeWithEleves;
import com.ufrst.app.trombi.database.TrombiViewModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

import static com.ufrst.app.trombi.ActivityMain.EXTRA_DESC;
import static com.ufrst.app.trombi.ActivityMain.EXTRA_ID;
import static com.ufrst.app.trombi.ActivityMain.EXTRA_NOM;
import static com.ufrst.app.trombi.ActivityMain.PREFS_NBCOLS;

public class ActivityVueTrombi extends AppCompatActivity {

    private BottomSheetBehavior bottomSheetBehavior;
    private CoordinatorLayout coordinatorLayout;
    private ChipGroup chipGroup;
    private TextView tvNbCols;
    private View bottomSheet;
    private WebView webView;
    private Toolbar toolbar;
    private SeekBar seekBar;

    private Observer<List<Eleve>> observerEleve;
    private TrombiViewModel trombiViewModel;
    //private Trombinoscope currentTrombi;
    private List<Eleve> listeEleves;
    private SharedPreferences prefs;
    private String descTrombi;
    private String nomTrombi;
    private long idTrombi;
    private int nbCols;                               // Nombre de colonnes à afficher dans le webview


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vue_trombi);


        getExtras();
        retrieveSharedPrefs();
        findViews();
        getGroupesAndEleves();
        setListeners();

        // Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(nomTrombi);

        // Met en place la Bottom Sheet persistante, qui contiendra les filtres
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Applique la valeur par défaut de nbCols à la SeekBar
        seekBar.setProgress(nbCols);
        tvNbCols.setText(String.valueOf(nbCols + 1));
    }

    // Récupération des extras
    private void getExtras(){
        Intent intent = getIntent();
        idTrombi = intent.getLongExtra(EXTRA_ID, -1);
        nomTrombi = intent.getStringExtra(EXTRA_NOM);
        descTrombi = intent.getStringExtra(EXTRA_DESC);
    }

    // Récupération des valeures prédéfinies par l'utilisateur
    private void retrieveSharedPrefs(){
        prefs = getSharedPreferences("com.ufrst.app.trombi", Context.MODE_PRIVATE);
        nbCols = prefs.getInt(PREFS_NBCOLS, 3);
    }

    // Désérialise les vues dont on aura besoin depuis le XML
    private void findViews(){
        coordinatorLayout = findViewById(R.id.VUETROMBI_coordinator);
        bottomSheet = findViewById(R.id.VUETROMBI_bottomSheet);
        chipGroup = findViewById(R.id.VUETROMBI_chipsGroup);
        tvNbCols = findViewById(R.id.VUETROMBI_nbCols);
        seekBar = findViewById(R.id.VUETROMBI_seekBar);
        toolbar = findViewById(R.id.VUETROMBI_toolbar);
        webView = findViewById(R.id.VUETROMBI_webView);
    }

    // Récupère les informations sur le trombinoscope sélectionné dans l'activité précédente
    private void getGroupesAndEleves(){
        // Création et référenciation d'un observeur qui sera supprimé/ajouté lors de la sélection
        // ou déselection d'un groupe
        observerEleve = eleves -> {
            listeEleves = eleves;
            showHTML();
        };

        trombiViewModel = ViewModelProviders.of(this).get(TrombiViewModel.class);
        trombiViewModel.getElevesByTrombi(idTrombi).observe(this, observerEleve);
        trombiViewModel.getGroupesByTrombi(idTrombi).observe(this, new Observer<List<Groupe>>() {
            @Override
            public void onChanged(List<Groupe> groupes){
                // Insertion des chips pour le choix des groupes a afficher
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

    // Applique des listeners sur certains éléments
    private void setListeners(){
        // Floating action button
        /*FloatingActionButton fab = findViewById(R.id.VUETROMBI_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                ModalBottomSheet modalBottomSheet = new ModalBottomSheet();
                modalBottomSheet.show(getSupportFragmentManager(), modalBottomSheet.getTag());
            }
        });*/

        // Seekbar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b){
                tvNbCols.setText(String.valueOf(i + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar){

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar){
                if(seekBar.getProgress() != nbCols){
                    nbCols = seekBar.getProgress();
                    showHTML();
                }
            }
        });

        // Groupe de chips
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup chipGroup, int i){
                //
                // SOLUTION TEMPORAIRE au problème du changement d'id des chips lors de la reprise de l'activité
                //
                /*int pos = i - 1;    // Représente la position du groupe dans la liste listeGroupe

                while(pos > chipGroup.getChildCount() - 1){
                    pos -= chipGroup.getChildCount();
                }*/

                // Une chips est sélectionnée
                if(i != ChipGroup.NO_ID){
                    // Récupération du groupe associé à la chips: cf setChips()
                    Chip c = findViewById(i);
                    Groupe currentGroupe = (Groupe) c.getTag(R.string.TAG_CHIPS_ID);

                    // Changement de l'ID voulu pour la récupération de la méthode getGroupByIdWithEleves
                    trombiViewModel.setIdGroup(currentGroupe.getIdGroupe());

                    // Observation des élèves du Groupe de la chips sélectionnée
                    trombiViewModel.groupesWithEleves
                            .observe(ActivityVueTrombi.this, new Observer<GroupeWithEleves>() {
                                @Override
                                public void onChanged(GroupeWithEleves groupeWithEleves){
                                    listeEleves = groupeWithEleves.getEleves();
                                    showHTML();
                                }
                            });
                } else{
                    // On observe la liste de tous les élèves du trombi à nouveau
                    trombiViewModel.getElevesByTrombi(idTrombi)
                            .observe(ActivityVueTrombi.this, observerEleve);
                }
            }
        });
    }

    // Instancie positionne, et met en place les chips dans la bottom sheet.
    private void setChips(Groupe g){
        // Inflate la chips d'après mon layout customisé, afin de pouvoir changer l'apparence de la chips lors de la sélection
        Chip c = (Chip) getLayoutInflater()
                .inflate(R.layout.chips_choice, chipGroup, false);

        // Ajout d'un tag pour lier un objet à cette vue
        c.setTag(R.string.TAG_CHIPS_ID, g);

        // Texte de la chips
        c.setText(g.getNomGroupe() + "-" + g.getIdGroupe()); //A changer: enlever + idGroupe

        // Ajout de la chips dans le groupe de chips
        chipGroup.post(new Runnable() {
            @Override
            public void run(){
                chipGroup.addView(c);
            }
        });
    }

    // Insère le HTML dans la WebView
    private void showHTML(){
        boolean isLastRow = false;              // Détermine si la dernière ligne affichée était la dernière
        int index = 0;                          // Indexe a chosir dans la liste d'élèves

        // Ne peut pas se produire, sauf si l'API du téléphone est inférieure à 26
        // Les APIs inférieures à 26 ne supportent pas l'attribut "min" des Seekbar
        if(nbCols == -1){
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>")
                .append("<h1>").append(nomTrombi).append("</h1>")
                .append("<h2>").append(descTrombi).append("</h2>")
                .append("<table>");

        //A changer; supprimer
        for(int k = 0; k < listeEleves.size(); k++){
            Log.v("_________E__________", listeEleves.get(k).getNomPrenom());
        }

        // Lignes
        while(!isLastRow){
            sb.append("<tr>");

            // Colonnes
            for(int j = 0; j < nbCols + 1; j++){
                Eleve eleve;

                try{
                    eleve = listeEleves.get(index++);

                    if(eleve.getPhoto() != null){
                        sb.append("<td>").append(eleve.getNomPrenom()).append("</td>");
                    }
                } catch(IndexOutOfBoundsException e){              // Fin de la liste atteinte, sortie
                    isLastRow = true;
                    break;
                }
            }

            sb.append("</tr>");
        }

        sb.append("</table>");
        sb.append("</body></html>");

        webView.loadData(sb.toString(), "text/html", "UTF-8");
    }

    // Demande à l'utilisateur si la liste doit être exportée
    // Un fichier correspondant à cette liste peut déjà exister
    private void checkWriteExportedList(){
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        final String filename = nomTrombi + "-" + df.format(calendar.getTime());

        // Observation des élèves du trombi à exporter
        trombiViewModel.getElevesByTrombi(idTrombi).observe(this, new Observer<List<Eleve>>() {
            @Override
            public void onChanged(List<Eleve> eleves){
                File file = new File(getExternalFilesDir(null) + "/" + filename + ".txt");

                // Si le fichier existe, on demande à le remplacer, sinon on le créer.
                if(file.exists()){
                    // Snackbar avec action
                    Snackbar.make(coordinatorLayout, R.string.VUETROMBI_fichierExiste, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.U_remplacer, new View.OnClickListener() {
                                @Override
                                public void onClick(View v){
                                    writeExportedList(filename, eleves, true);
                                }
                            })
                            .setDuration(8000)
                            .show();
                } else{
                    writeExportedList(filename, eleves, false);
                }
            }
        });

        trombiViewModel.getElevesByTrombi(idTrombi).removeObservers(this);
    }

    // Ecrit une liste d'élèves dans un fichier situé dans le stockage externe de l'app
    private void writeExportedList(final String filename, final List<Eleve> eleves,
                                   final boolean doErase){
        // Le fichier est réécrit, on doit supprimer son contenu actuel
        if(doErase){
            try{
                PrintWriter writer = new PrintWriter(getExternalFilesDir(null) +
                        "/" + filename + ".txt");
                writer.print("");
                writer.close();
            } catch(FileNotFoundException e){
                e.printStackTrace();
            }
        }

        // Ecriture du fichier
        try(BufferedWriter writer =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(getExternalFilesDir(null) +
                                            "/" + filename + ".txt",
                                            true),
                                    StandardCharsets.UTF_8)
                    )
        ){
            for(Eleve eleve : eleves){
                writer.write(eleve.getNomPrenom() + "\n");
            }

            Snackbar.make(coordinatorLayout, R.string.VUETROMBI_listeExportee, Snackbar.LENGTH_LONG)
                    .show();
        } catch(IOException e){
            Snackbar.make(coordinatorLayout,
                    R.string.VUETROMBI_listeExporteeErr,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.export_trombi_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch(item.getItemId()){
            case R.id.VUETROMBI_exporterTrombi:
                return true;

            case R.id.VUETROMBI_exporterPdf:
                Toast.makeText(this, "bruh1", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.VUETROMBI_exporterImg:
                Toast.makeText(this, "bruh2", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.VUETROMBI_exporterListe:
                Toast.makeText(this, "bruh3", Toast.LENGTH_SHORT).show();
                checkWriteExportedList();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
