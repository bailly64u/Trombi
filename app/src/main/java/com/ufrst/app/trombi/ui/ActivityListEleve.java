package com.ufrst.app.trombi.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.ufrst.app.trombi.adapter.AdapteurEleve;
import com.ufrst.app.trombi.R;
import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.TrombiViewModel;

import java.util.List;

import static com.ufrst.app.trombi.ui.ActivityCapture.EXTRA_MODE;
import static com.ufrst.app.trombi.ui.ActivityCapture.TAKE_ALL_PHOTO_MODE;
import static com.ufrst.app.trombi.ui.ActivityCapture.TAKE_PHOTO_MODE;
import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_ID;
import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_ID_E;
import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_NOM;
import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_NOM_E;
import static com.ufrst.app.trombi.ui.ActivityMain.EXTRA_PHOTO_E;
import static com.ufrst.app.trombi.ui.ActivityMain.STATE_NOT_DELETED;
import static com.ufrst.app.trombi.ui.ActivityMain.STATE_SOFT_DELETED;

public class ActivityListEleve extends AppCompatActivity {

    public static final int REQUETE_AJOUT_ELEVE = 1;
    public static final int REQUETE_EDITE_ELEVE = 2;

    private CoordinatorLayout coordinatorLayout;
    private TrombiViewModel trombiViewModel;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private AdapteurEleve adapteur;
    private TextView tvEmpty;
    private Toolbar toolbar;

    private String nomTrombi;
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

        nomTrombi = intent.getStringExtra(EXTRA_NOM);
    }

    private void findViews(){
        tvEmpty = findViewById(R.id.LISTe_emptyRecyclerView);
        coordinatorLayout = findViewById(R.id.LISTe_coordinator);
        recyclerView = findViewById(R.id.LISTe_recyclerView);
        toolbar = findViewById(R.id.LISTe_toolbar);
        fab = findViewById(R.id.LISTe_fab);
    }

    private void setListeners(){
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(ActivityListEleve.this, ActivityAjoutEleve.class);
            intent.putExtra(EXTRA_ID, idTrombi);

            startActivityForResult(intent, REQUETE_AJOUT_ELEVE);
        });
    }

    private void setRecyclerViewAndViewModel(){
        // Définir l'adapteur du RecyclerView
        adapteur = new AdapteurEleve(Glide.with(this));

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
                final long id = adapteur.getEleveAt(viewHolder.getAdapterPosition()).getIdEleve();
                setEleveState(id, STATE_SOFT_DELETED);

                // Snackbar avec possibilité d'annuler
                Snackbar.make(
                        coordinatorLayout, R.string.LISTe_eleveSuppr, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.U_annuler, v -> setEleveState(id, STATE_NOT_DELETED))
                        .setActionTextColor(ContextCompat.getColor(
                                ActivityListEleve.this, R.color.trombi_blue))
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
                onItemLongClick(eleve);
            }

            @Override
            public void onItemLongClick(Eleve eleve){
                Intent intent = new Intent(ActivityListEleve.this, ActivityAjoutEleve.class);
                intent.putExtra(EXTRA_ID, idTrombi);
                intent.putExtra(EXTRA_NOM_E, eleve.getNomPrenom());
                intent.putExtra(EXTRA_ID_E, eleve.getIdEleve());
                intent.putExtra(EXTRA_PHOTO_E, eleve.getPhoto());

                startActivityForResult(intent, REQUETE_EDITE_ELEVE);
            }

            @Override
            public void onPhotoClick(Eleve eleve){
                Intent intent = new Intent(ActivityListEleve.this, ActivityCapture.class);
                intent.putExtra(EXTRA_ID_E, eleve.getIdEleve());
                intent.putExtra(EXTRA_ID, idTrombi);
                intent.putExtra(EXTRA_NOM, nomTrombi);
                intent.putExtra(EXTRA_MODE, TAKE_PHOTO_MODE); //A changer: takephotomode

                startActivity(intent);
            }
        });
    }

    // Soft delete ou annule le soft delete d'un élève et de ses XRefs
    private void setEleveState(long idEleve, int state){
        trombiViewModel.softDeleteEleve(idEleve);
        trombiViewModel.softDeleteXRefsByEleve(idEleve, state);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.list_eleve_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch(item.getItemId()){
            case R.id.LISTe_gererGroupe:
                Intent intent = new Intent(ActivityListEleve.this, ActivityListGroupe.class);
                intent.putExtra(EXTRA_ID, idTrombi);
                startActivity(intent);

                return true;

            case R.id.LISTe_modeClassePhoto:
                Intent intent1 = new Intent(ActivityListEleve.this, ActivityCapture.class);
                intent1.putExtra(EXTRA_ID, idTrombi);
                intent1.putExtra(EXTRA_NOM, nomTrombi);
                intent1.putExtra(EXTRA_MODE, TAKE_ALL_PHOTO_MODE);
                startActivity(intent1);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null){
            if(requestCode == REQUETE_AJOUT_ELEVE && resultCode == RESULT_OK){
                Snackbar.make(coordinatorLayout,
                        R.string.LISTe_eleveAjoute,
                        Snackbar.LENGTH_LONG).show();
            } else if(requestCode == REQUETE_EDITE_ELEVE && resultCode == RESULT_OK){
                Snackbar.make(coordinatorLayout,
                        R.string.LISTe_eleveModifie,
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
