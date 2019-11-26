package com.ufrst.app.trombi;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.ufrst.app.trombi.database.TrombiViewModel;
import com.ufrst.app.trombi.database.Trombinoscope;

import java.util.List;

public class ActivityMain extends AppCompatActivity {

    public static final int REQUETE_AJOUT_TROMBI = 1;

    private CoordinatorLayout mCoordinatorLayout;
    private TrombiViewModel mTrombiViewModel;
    //private NavigationView mNavigationView;
    private RecyclerView mRecyclerView;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        setListeners();
        setRecyclerViewAndViewModel();
    }

    // Désérialise les vues dont on aura besoin depuis le XML
    private void findViews(){
        //mNavigationView = findViewById(R.id.NAV_navigationView);
        mCoordinatorLayout = findViewById(R.id.MAIN_coordinator);
        tvEmpty = findViewById(R.id.MAIN_emptyRecyclerView);
    }

    // Applique des listeners sur certains éléments
    private void setListeners(){
        // Menu hamburger latéral
        /*mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item){
                switch(item.getItemId()){
                    case R.id.NAV_item1:
                        Toast.makeText(ActivityMain.this, "Salut1", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.NAV_item2:
                        Toast.makeText(ActivityMain.this, "Salut2", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.NAV_item3:
                        Toast.makeText(ActivityMain.this, "Salut3", Toast.LENGTH_SHORT).show();
                        break;
                }

                return false;
            }
        });*/

        // Floating Action Button, désérialisation ici car on s'en servira que ici
        FloatingActionButton fab = findViewById(R.id.MAIN_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Démarre une activité. Au retour, onActivityResult() sera déclenchée
                Intent intent = new Intent(ActivityMain.this, ActivityAjoutTrombi.class);
                startActivityForResult(intent, REQUETE_AJOUT_TROMBI);
            }
        });
    }

    // Met en place l'observeur et le RecyclerView
    public void setRecyclerViewAndViewModel(){
        mRecyclerView = findViewById(R.id.MAIN_recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));  // Affiche les items les uns en dessous des autres
        mRecyclerView.setHasFixedSize(true);                                    // Meilleures performances si le RV ne change pas de taille

        // Définir l'adapteur du RecyclerView
        final AdapteurTrombi adapteur = new AdapteurTrombi();
        mRecyclerView.setAdapter(adapteur);

        // Récupére le ViewModel et observer la liste de Trombinoscopes
        mTrombiViewModel = ViewModelProviders.of(this).get(TrombiViewModel.class);
        mTrombiViewModel.getAllTrombis().observe(this, new Observer<List<Trombinoscope>>() {
            @Override
            public void onChanged(List<Trombinoscope> trombis){
                adapteur.setTrombis(trombis);

                // Afficher le placeholder en cas de liste vide
                if(trombis.isEmpty()){
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
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target){
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction){
                final Trombinoscope trombiSuppr = adapteur.getTrombiAt(viewHolder.getAdapterPosition());
                mTrombiViewModel.delete(trombiSuppr);

                // Snackbar avec possibilité d'annuler
                Snackbar.make(mCoordinatorLayout, R.string.MAIN_trombiSuppr, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.U_annuler, new View.OnClickListener() {
                            @Override
                            public void onClick(View v){
                                Toast.makeText(ActivityMain.this, "bruh", Toast.LENGTH_SHORT).show(); //A changer
                            }
                        })
                        .setDuration(8000)
                        .show();
            }
        }).attachToRecyclerView(mRecyclerView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);


    }
}
