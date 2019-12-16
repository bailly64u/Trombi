package com.ufrst.app.trombi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.Groupe;
import com.ufrst.app.trombi.database.GroupeWithEleves;
import com.ufrst.app.trombi.database.TrombiViewModel;
import com.ufrst.app.trombi.database.Trombinoscope;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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

    private ArrayList<Chip> listeChips = new ArrayList<>();
    private Observer<List<Eleve>> observerEleve;
    private TrombiViewModel trombiViewModel;
    //private Trombinoscope currentTrombi;
    private List<Groupe> listeGroupes;
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
        toolbar.setTitle(nomTrombi);

        // Met en place la Bottom Sheet persistante, qui contiendra les filtres
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Applique la valeur par défaut de nbCols à la SeekBar
        seekBar.setProgress(nbCols);
        tvNbCols.setText(String.valueOf(nbCols));


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
        nbCols = prefs.getInt(PREFS_NBCOLS, 4);
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
        observerEleve = new Observer<List<Eleve>>() {
            @Override
            public void onChanged(List<Eleve> eleves) {
                listeEleves = eleves;
                showHTML();
            }
        };

        trombiViewModel = ViewModelProviders.of(this).get(TrombiViewModel.class);
        trombiViewModel.getGroupesByTrombi(idTrombi).observe(this, new Observer<List<Groupe>>() {
            @Override
            public void onChanged(List<Groupe> groupes) {
                listeGroupes = groupes;

                // Insertion des chips pour le choix des groupes a afficher
                for(Groupe g : groupes){
                    setChips(g);
                }
            }
        });

        trombiViewModel.getElevesByTrombi(idTrombi).observe(this, observerEleve);
    }

    // Applique des listeners sur certains éléments
    private void setListeners(){
        // Floating action button
        FloatingActionButton fab = findViewById(R.id.VUETROMBI_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // Bottom sheeet dialog
            }
        });

        // Seekbar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvNbCols.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
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
                //Chip c = findViewById(chipGroup.getCheckedChipId());
                Chip c = (Chip) chipGroup.getChildAt(i);

                if(c != null){
                    long index = listeGroupes.get(i - 1).getIdGroupe();

                    Log.v("______________________", String.valueOf(chipGroup.getCheckedChipId()));

                    // Une des chips est sélectionnée
                    if(chipGroup.getCheckedChipId() != ChipGroup.NO_ID){
                        // Observation des élèves du Groupe de la chips sélectionnée
                        trombiViewModel.getGroupeByIdWithEleves(index)
                                .observe(ActivityVueTrombi.this, new Observer<GroupeWithEleves>() {
                            @Override
                            public void onChanged(GroupeWithEleves groupeWithEleves) {
                                listeEleves = groupeWithEleves.getEleves();
                                showHTML();
                            }
                        });
                    }
                } else{
                    // On observe la liste de tous les élèves du trombi à nouveau
                    trombiViewModel.getElevesByTrombi(idTrombi)
                            .observe(ActivityVueTrombi.this, observerEleve);
                }
            }
        });
    }

    // Instancie positionne, et met en place les listeners des chips dans la bottom sheet.
    private void setChips(Groupe g){
        // Inflate la chips d'après mon layout customisé, afin de pouvoir changer l'apparence de la chips lors de la sélection
        Chip c = (Chip) ActivityVueTrombi.this
                .getLayoutInflater()
                .inflate(R.layout.chips_choice, chipGroup, false);

        c.setText(g.getNomGroupe() + "-" + g.getIdGroupe());                                            // Texte de la chips
        chipGroup.addView(c);                                                   // Ajout de la chips dans le groupe de chips
        listeChips.add(c);                                                      // Et aussi dans une liste pour traquer le Groupe correspondant
    }

    // Insère le HTML dans la WebView
    private void showHTML(){
        boolean isLastRow = false;              // Détermine si la dernière ligne affichée était la dernière
        int index = 0;                          // Indexe a chosir dans la liste d'élèves

        // Ne peut pas se produire, sauf si l'API du téléphone est inférieure à 26
        // Les APIs inférieures à 26 ne supportent pas l'attribut "min" des Seekbar
        if(nbCols == 0){
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
            for(int j = 0; j < nbCols; j++){
                Eleve eleve;

                try {
                    eleve = listeEleves.get(index++);

                    if(eleve.getPhoto() != null){
                        sb.append("<td>").append(eleve.getNomPrenom()).append("</td>");
                    }
                } catch (IndexOutOfBoundsException e){              // Fin de la liste atteinte, sortie
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

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return super.onSupportNavigateUp();
    }
}
