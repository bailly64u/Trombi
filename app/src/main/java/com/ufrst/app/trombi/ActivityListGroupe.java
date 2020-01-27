package com.ufrst.app.trombi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.Groupe;
import com.ufrst.app.trombi.database.TrombiViewModel;

import java.util.List;

import static com.ufrst.app.trombi.ActivityMain.EXTRA_ID;

public class ActivityListGroupe extends AppCompatActivity {

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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent intent = new Intent(ActivityListGroupe.this, ActivityAjoutGroupe.class);
                intent.putExtra(EXTRA_ID, idTrombi);

                startActivity(intent);
            }
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
        trombiViewModel.getGroupesByTrombi(idTrombi).observe(this, new Observer<List<Groupe>>() {
            @Override
            public void onChanged(List<Groupe> groupes){
                adapteur.submitList(groupes);

                // Afficher le placeholder en cas de liste vide
                if(groupes.isEmpty()){
                    tvEmpty.setVisibility(View.VISIBLE);
                } else{
                    tvEmpty.setVisibility(View.GONE);
                }
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
                final Groupe groupeSuppr = adapteur.getGroupeAt(viewHolder.getAdapterPosition());
                trombiViewModel.delete(groupeSuppr);

                // TODO: Gérer les cross reference à supprimer

                // Snackbar avec possibilité d'annuler
                Snackbar.make(coordinatorLayout, R.string.LISTg_groupeSuppr, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.U_annuler, new View.OnClickListener() {
                            @Override
                            public void onClick(View v){
                                trombiViewModel.insert(groupeSuppr);
                            }
                        })
                        .setActionTextColor(ContextCompat.getColor(ActivityListGroupe.this, R.color.colorAccent))
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

            }

            @Override
            public void onItemLongClick(Groupe groupe){

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return super.onSupportNavigateUp();
    }
}
