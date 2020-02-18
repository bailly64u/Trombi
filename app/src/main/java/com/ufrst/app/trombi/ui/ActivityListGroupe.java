package com.ufrst.app.trombi.ui;

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

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.ufrst.app.trombi.adapter.AdapteurGroupe;
import com.ufrst.app.trombi.R;
import com.ufrst.app.trombi.database.Groupe;
import com.ufrst.app.trombi.database.TrombiViewModel;

import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_ID;
import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_ID_G;
import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_NOM_G;
import static com.ufrst.app.trombi.ui.ActivityMain.STATE_NOT_DELETED;
import static com.ufrst.app.trombi.ui.ActivityMain.STATE_SOFT_DELETED;

public class ActivityListGroupe extends AppCompatActivity {

    public static final int REQUETE_AJOUT_GROUPE = 1;
    public static final int REQUETE_EDITE_GROUPE = 2;

    private CoordinatorLayout coordinatorLayout;
    private TrombiViewModel trombiViewModel;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private AdapteurGroupe adapteur;
    private TextView tvEmpty;
    private Toolbar toolbar;

    private long idTrombi;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.AppThemeDark);
        } else{
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_groupe);

        findViews();
        getExtras();
        setListeners();
        setRecyclerViewAndViewModel();

        // Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.LISTg_title);
    }

    private void findViews(){
        coordinatorLayout = findViewById(R.id.LISTg_coordinator);
        tvEmpty = findViewById(R.id.LISTg_emptyRecyclerView);
        recyclerView = findViewById(R.id.LISTg_recyclerView);
        toolbar = findViewById(R.id.LISTg_toolbar);
        fab = findViewById(R.id.LISTg_fab);
    }

    private void getExtras(){
        Intent intent = getIntent();
        idTrombi = intent.getLongExtra(EXTRA_ID, -1);
    }

    private void setListeners(){
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(ActivityListGroupe.this, ActivityAjoutGroupe.class);
            intent.putExtra(EXTRA_ID, idTrombi);

            startActivityForResult(intent, REQUETE_AJOUT_GROUPE);
        });
    }

    private void setRecyclerViewAndViewModel(){
        // Définir l'adapteur du RecyclerView
        adapteur = new AdapteurGroupe();

        // Mise en place du RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapteur);

        // Récupérer le ViewModel et observer la liste de Groupes
        trombiViewModel = ViewModelProviders.of(this).get(TrombiViewModel.class);
        trombiViewModel.getGroupesByTrombi(idTrombi).observe(this, groupes -> {
            adapteur.submitList(groupes);

            // Afficher le placeholder en cas de liste vide
            if(groupes.isEmpty()){
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
                final long id = adapteur.getGroupeAt(viewHolder.getAdapterPosition()).getIdGroupe();
                setGroupeState(id, STATE_SOFT_DELETED);

                // Snackbar avec possibilité d'annuler
                Snackbar.make(
                        coordinatorLayout, R.string.LISTg_groupeSuppr, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.U_annuler, v -> setGroupeState(id, STATE_NOT_DELETED))
                        .setActionTextColor(ContextCompat.getColor(
                                ActivityListGroupe.this, R.color.colorAccent))
                        .setDuration(8000)
                        .show();
            }
        }).attachToRecyclerView(recyclerView);

        // ScrollListener, pour cacher ou révéler le fab en temps voulu
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0  && fab.isShown()){
                    fab.hide();
                }

                if(dy < 0  && !fab.isShown()){
                    fab.show();
                }
            }
        });

        adapteur.setOnItemClickListener(new AdapteurGroupe.OnItemClickListener() {
            @Override
            public void onItemClick(Groupe groupe){
                onItemLongClick(groupe);
            }

            @Override
            public void onItemLongClick(Groupe groupe){
                Intent intent = new Intent(ActivityListGroupe.this, ActivityAjoutGroupe.class);
                intent.putExtra(EXTRA_ID, idTrombi);
                intent.putExtra(EXTRA_ID_G, groupe.getIdGroupe());
                intent.putExtra(EXTRA_NOM_G, groupe.getNomGroupe());

                startActivityForResult(intent, REQUETE_EDITE_GROUPE);
            }
        });
    }

    // Soft delete ou annule le soft delete d'un groupe et des ses XRefs
    private void setGroupeState(long idGroupe, int state){
        trombiViewModel.softDeleteGroupe(idGroupe);
        trombiViewModel.softDeleteXRefsByGroupe(idGroupe, state);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null){
            if(requestCode == REQUETE_AJOUT_GROUPE && resultCode == RESULT_OK){
                Snackbar.make(coordinatorLayout,
                        R.string.LISTg_groupeAjoute,
                        Snackbar.LENGTH_LONG).show();
            } else if(requestCode == REQUETE_EDITE_GROUPE && resultCode == RESULT_OK){
                Snackbar.make(coordinatorLayout,
                        R.string.LISTg_groupeModifie,
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return super.onSupportNavigateUp();
    }
}
