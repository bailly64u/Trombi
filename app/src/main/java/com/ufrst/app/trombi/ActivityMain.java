package com.ufrst.app.trombi;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class ActivityMain extends AppCompatActivity {

    public static final int REQUETE_AJOUT_TROMBI = 1;

    private NavigationView mNavigationView;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        setListeners();
    }

    // Désérialise les vues dont on aura besoin depuis le XML
    private void findViews(){
        mNavigationView = findViewById(R.id.NAV_navigationView);
    }

    // Applique des listeners sur certains éléments
    private void setListeners(){

        // Menu hamburger latéral
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
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
        });

        // Floating Action Button, désérialisation ici car on s'en servira que ici
        FloatingActionButton fab = findViewById(R.id.MAIN_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(ActivityMain.this, bruh);
                //startActivityForResult(intent, REQUETE_AJOUT_TROMBI)
            }
        });
    }
}
