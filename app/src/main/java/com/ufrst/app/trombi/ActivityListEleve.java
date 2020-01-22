package com.ufrst.app.trombi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.TrombiViewModel;

import java.util.List;

import static com.ufrst.app.trombi.ActivityMain.EXTRA_ID;

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
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return super.onSupportNavigateUp();
    }
}
