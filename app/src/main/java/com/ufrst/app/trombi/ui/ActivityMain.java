package com.ufrst.app.trombi.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.ufrst.app.trombi.adapter.AdapteurTrombi;
import com.ufrst.app.trombi.R;
import com.ufrst.app.trombi.database.TrombiViewModel;
import com.ufrst.app.trombi.database.Trombinoscope;

public class ActivityMain extends AppCompatActivity {

    public static final int REQUETE_AJOUT_TROMBI = 1;
    public static final int REQUETE_EDITE_TROMBI = 2;

    public static final int STATE_SOFT_DELETED = 1;
    public static final int STATE_NOT_DELETED = 0;

    public static final String EXTRA_ID = "com.ufrst.app.trombi.EXTRA_ID";
    public static final String EXTRA_NOM = "com.ufrst.app.trombi.EXTRA_NOM";
    public static final String EXTRA_DESC = "com.ufrst.app.trombi.EXTRA_DESC";

    public static final String EXTRA_ID_E = "com.ufrst.app.trombi.EXTRA_ID_E";
    public static final String EXTRA_NOM_E = "com.ufrst.app.trombi.EXTRA_NOM_E";
    public static final String EXTRA_PHOTO_E = "com.ufrst.app.trombi.EXTRA_PHOTO_E";

    public static final String EXTRA_ID_G = "com.ufrst.app.trombi.EXTRA_ID_G";
    public static final String EXTRA_NOM_G = "com.ufrst.app.trombi.EXTRA_NOM_G";

    public static final String PREFS_NBCOLS = "com.ufrst.app.trombi.PREFS_NBCOLS";
    public static final String PREFS_FIXED_RATIO = "com.ufrst.app.trombi.PREFS_FIXED_RATIO";
    public static final String PREFS_NIGHT_MODE = "com.ufrst.app.trombi.PREFS_NIGHT_MODE";
    public static final String PREFS_MULTI_THREADING = "com.ufrst.app.trombi.PREFS_MULTI_THREADING";
    public static final String PREFS_QUALITY_OR_LATENCY =
            "com.ufrst.app.trombi.PREFS_QUALITY_OR_LATENCY";

    private CoordinatorLayout coordinatorLayout;
    //private NavigationView navigationView;
    private TrombiViewModel trombiViewModel;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private AdapteurTrombi adapteur;
    private TextView tvEmpty;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        SharedPreferences prefs =
                getSharedPreferences("com.ufrst.app.trombi", Context.MODE_PRIVATE);
        boolean night = prefs.getBoolean(PREFS_NIGHT_MODE, false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(R.string.MAIN_title);

        findViews();
        setListeners();
        setRecyclerViewAndViewModel();

        // Toolbar
        setSupportActionBar(toolbar);
    }

    // Désérialise les vues dont on aura besoin depuis le XML
    private void findViews(){
        coordinatorLayout = findViewById(R.id.MAIN_coordinator);
        recyclerView = findViewById(R.id.MAIN_recyclerView);
        tvEmpty = findViewById(R.id.MAIN_emptyRecyclerView);
        toolbar = findViewById(R.id.MAIN_toolbar);
        fab = findViewById(R.id.MAIN_fab);
    }

    // Applique des listeners sur certains éléments
    private void setListeners(){
        fab.setOnClickListener(view -> {
            // Démarre une activité. Au retour, onActivityResult() sera déclenchée
            Intent intent = new Intent(ActivityMain.this, ActivityAjoutTrombi.class);
            startActivityForResult(intent, REQUETE_AJOUT_TROMBI);
        });
    }

    // Met en place l'observeur et le RecyclerView
    public void setRecyclerViewAndViewModel(){
        // Définir l'adapteur du RecyclerView
        adapteur = new AdapteurTrombi();

        // Mise en place du RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));  // Affiche les items les uns en dessous des autres
        recyclerView.setHasFixedSize(true);                                    // Meilleures performances si le RV ne change pas de taille
        recyclerView.setAdapter(adapteur);

        // Récupérer le ViewModel et observer la liste de Trombinoscopes
        trombiViewModel = ViewModelProviders.of(this).get(TrombiViewModel.class);
        trombiViewModel.getAllTrombis().observe(this, trombis -> {
            adapteur.submitList(trombis);

            // Afficher le placeholder en cas de liste vide
            if(trombis.isEmpty()){
                tvEmpty.setVisibility(View.VISIBLE);
            } else{
                tvEmpty.setVisibility(View.GONE);
            }
        });

        // Gestion des swipes sur le RecyclerView
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target){
                return false;
            }

            @Override
            // Animation de transaparence lors du swipe
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive){
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                    float width = (float) viewHolder.itemView.getWidth();
                    float alpha = 1.0f - Math.abs(dX) / width;
                    viewHolder.itemView.setAlpha(alpha);
                    viewHolder.itemView.setTranslationX(dX);
                } else{
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction){
                final long idTrombiSuppr =
                        adapteur.getTrombiAt(viewHolder.getAdapterPosition()).getIdTrombi();
                setTrombiState(idTrombiSuppr, STATE_SOFT_DELETED);

                // Snackbar avec possibilité d'annuler
                Snackbar.make(
                        coordinatorLayout, R.string.MAIN_trombiSuppr, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.U_annuler, v -> setTrombiState(idTrombiSuppr, STATE_NOT_DELETED))
                        .setActionTextColor(ContextCompat.getColor(
                                ActivityMain.this, R.color.trombi_blue))
                        .setDuration(8000)
                        .show();
            }
        }).attachToRecyclerView(recyclerView);

        // ScrollListener, pour cacher ou révéler le fab en temps voulu
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy){
                if(dy > 0 && fab.isShown()){
                    fab.hide();
                }

                if(dy < 0 && !fab.isShown()){
                    fab.show();
                }
            }
        });

        // Implémentation de notre interface (voire AdapteurTrombi)
        // On peut gérer le clique tout en ayant le contexte de l'activité principale,
        // en particulier le ViewModel pour modifier la base de données
        adapteur.setOnItemClickListener(new AdapteurTrombi.OnItemClickListener() {
            @Override
            public void onItemClick(Trombinoscope trombi){
                Intent intent = new Intent(ActivityMain.this, ActivityVueTrombi.class);

                // Informations à passer pour afficher dans le HTML
                intent.putExtra(EXTRA_ID, trombi.getIdTrombi());
                intent.putExtra(EXTRA_NOM, trombi.getNomTrombi());
                intent.putExtra(EXTRA_DESC, trombi.getDescription());

                startActivity(intent);
            }

            @Override
            public void onItemLongClick(Trombinoscope trombi){
                Intent intent = new Intent(ActivityMain.this, ActivityAjoutTrombi.class);

                // On passe les infos, à remettre dans les champs sujets à modifications
                intent.putExtra(EXTRA_ID, trombi.getIdTrombi());
                intent.putExtra(EXTRA_NOM, trombi.getNomTrombi());
                intent.putExtra(EXTRA_DESC, trombi.getDescription());

                startActivityForResult(intent, REQUETE_EDITE_TROMBI);
            }
        });
    }

    // Soft delete ou annule le soft delete de tous les objets d'un trombinoscope
    private void setTrombiState(long idTrombi, int state){
        trombiViewModel.softDeleteTrombi(idTrombi);
        trombiViewModel.softDeleteElevesForTrombi(idTrombi, state);
        trombiViewModel.softDeleteGroupesForTrombi(idTrombi, state);
        trombiViewModel.softDeleteXRefsByTrombi(idTrombi, state);
    }

    @Override
    protected void onDestroy(){
        trombiViewModel.deleteSoftDeletedGroupes();
        trombiViewModel.deleteSoftDeletedTrombis();
        trombiViewModel.deleteSoftDeletedEleves();
        trombiViewModel.deleteSoftDeletedXRefs();

        super.onDestroy();
    }

    @Override
    // Déclenchée au retour de l'activité d'ajout de trombinoscope
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUETE_AJOUT_TROMBI && resultCode == RESULT_OK){
            if(data != null){
                String nom = data.getStringExtra(EXTRA_NOM);
                String desc = data.getStringExtra(EXTRA_DESC);

                Trombinoscope trombi = new Trombinoscope(nom, desc);
                trombiViewModel.insert(trombi);

                Snackbar.make(coordinatorLayout, R.string.MAIN_trombiAjoute, Snackbar.LENGTH_LONG).show();
            } else{
                Snackbar.make(coordinatorLayout, R.string.U_erreur, Snackbar.LENGTH_LONG).show();
            }
        } else if(requestCode == REQUETE_EDITE_TROMBI && resultCode == RESULT_OK){
            if(data != null){
                // On précise l'id afin d'updater la bonne valeur dans la BD
                long id = data.getLongExtra(EXTRA_ID, -1);

                // Un problème est survenu. Ne peut normalement pas se dérouler
                if(id == -1){
                    Snackbar.make(coordinatorLayout, R.string.U_erreur, Snackbar.LENGTH_LONG).show();
                    return;
                }

                String nom = data.getStringExtra(EXTRA_NOM);
                String desc = data.getStringExtra(EXTRA_DESC);

                Trombinoscope trombi = new Trombinoscope(nom, desc);
                trombi.setIdTrombi(id);
                trombiViewModel.update(trombi);

                Snackbar.make(coordinatorLayout, R.string.MAIN_trombiModifie, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getItemId() == R.id.MAIN_settings){
            Intent intent = new Intent(ActivityMain.this, ActivityParametre.class);
            startActivity(intent);
        }

        return true;
    }
}
