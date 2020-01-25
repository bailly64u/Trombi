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
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.TrombiViewModel;

import java.util.List;

import static com.ufrst.app.trombi.ActivityMain.EXTRA_ID;
import static com.ufrst.app.trombi.ActivityMain.EXTRA_ID_E;

public class ActivityListEleve extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;
    private TrombiViewModel trombiViewModel;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private AdapteurEleve adapteur;
    private TextView tvEmpty;
    private Toolbar toolbar;

    private long idTrombi;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_eleve);

        setTitle(R.string.LISTe_title);

        getExtras();
        findViews();
        setListeners();
        setRecyclerViewAndViewModel();

        // Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void getExtras(){
        Intent intent = getIntent();
        idTrombi = intent.getLongExtra(EXTRA_ID, -1);

        if(idTrombi == -1){
            Toast.makeText(this, R.string.LISTe_fatalError, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void findViews(){
        tvEmpty = findViewById(R.id.LISTe_emptyRecyclerView);
        coordinatorLayout = findViewById(R.id.LISTe_coordinator);
        recyclerView = findViewById(R.id.LISTe_recyclerView);
        toolbar = findViewById(R.id.LISTe_toolbar);
        fab = findViewById(R.id.LISTe_fab);
    }

    private void setListeners(){
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent intent = new Intent(ActivityListEleve.this, ActivityAjoutEleve.class);
                intent.putExtra(EXTRA_ID, idTrombi);
                startActivity(intent);
            }
        });
    }

    private void setRecyclerViewAndViewModel(){
        // Définir l'adapteur du RecyclerView
        adapteur = new AdapteurEleve();

        // Mise en place du RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapteur);

        // Récupérer le ViewModel et observer la liste d'Eleves
        trombiViewModel = ViewModelProviders.of(this).get(TrombiViewModel.class);
        trombiViewModel.getElevesByTrombi(idTrombi).observe(this, new Observer<List<Eleve>>() {
            @Override
            public void onChanged(List<Eleve> eleves){
                adapteur.submitList(eleves);

                // Afficher le placeholder en cas de liste vide
                if(eleves.isEmpty()){
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
                final long idEleveSuppr = adapteur.getEleveAt(viewHolder.getAdapterPosition()).getIdEleve();
                trombiViewModel.softDeleteEleve(idEleveSuppr);
                Log.v("_________________", "ID: " + idEleveSuppr + " NOM: " + adapteur.getEleveAt(viewHolder.getAdapterPosition()).getNomPrenom());
                // TODO: Gérer les cross reference à supprimer

                // Snackbar avec possibilité d'annuler
                Snackbar.make(coordinatorLayout, R.string.LISTe_eleveSuppr, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.U_annuler, new View.OnClickListener() {
                            @Override
                            public void onClick(View v){
                                trombiViewModel.softDeleteEleve(idEleveSuppr);
                            }
                        })
                        .setActionTextColor(ContextCompat.getColor(ActivityListEleve.this, R.color.colorAccent))
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

        // Implémentation de notre interface pour gérer les cliques
        adapteur.setOnItemClickListener(new AdapteurEleve.OnItemClickListener() {
            @Override
            public void onItemClick(Eleve eleve){

            }

            @Override
            public void onItemLongClick(Eleve eleve){

            }

            @Override
            public void onGroupClick(Eleve eleve){

            }

            @Override
            public void onPhotoClick(Eleve eleve){
                Intent intent = new Intent(ActivityListEleve.this, ActivityCapture.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return super.onSupportNavigateUp();
    }
}
