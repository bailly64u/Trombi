package com.ufrst.app.trombi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.Groupe;
import com.ufrst.app.trombi.database.GroupeWithEleves;
import com.ufrst.app.trombi.database.TrombiViewModel;
import com.ufrst.app.trombi.database.Trombinoscope;

import java.util.List;

public class ActivityBDTest extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bdtest);

        TrombiViewModel mTrombiViewModel = ViewModelProviders.of(this).get(TrombiViewModel.class);
        mTrombiViewModel.getAllTrombis().observe(this, new Observer<List<Trombinoscope>>() {
            @Override
            public void onChanged(List<Trombinoscope> trombis){
                StringBuilder sb = new StringBuilder();

                for(Trombinoscope trombi :trombis){
                    sb.append(trombi.getNomTrombi() + " | ");
                }

                TextView tv1 = findViewById(R.id.TEST_tv1);
                tv1.setText(sb);
            }
        });

        mTrombiViewModel.getAllGroupes().observe(this, new Observer<List<Groupe>>() {
            @Override
            public void onChanged(List<Groupe> groupes) {
                StringBuilder sb = new StringBuilder();

                for(Groupe gr : groupes){
                    sb.append(gr.getNomGroupe() + " " + gr.getIdGroupe() + " | ");
                }

                TextView tv2 = findViewById(R.id.TEST_tv2);
                tv2.setText(sb);
            }
        });

        mTrombiViewModel.getAllEleves().observe(this, new Observer<List<Eleve>>() {
            @Override
            public void onChanged(List<Eleve> eleves) {
                StringBuilder sb = new StringBuilder();

                for(Eleve el : eleves){
                    sb.append(el.getNomPrenom() + el.getIdEleve() + " | ");
                }

                TextView tv3 = findViewById(R.id.TEST_tv3);
                tv3.setText(sb);
            }
        });

        mTrombiViewModel.getGroupesByTrombi(3).observe(this, new Observer<List<Groupe>>() {
            @Override
            public void onChanged(List<Groupe> groupes) {
                StringBuilder sb = new StringBuilder();

                for(Groupe groupe : groupes){
                    sb.append(groupe.getNomGroupe() + " | ");
                }

                TextView tv4 = findViewById(R.id.TEST_tv4);
                tv4.setText(sb);
            }
        });

        mTrombiViewModel.getGroupeWithEleves().observe(this, new Observer<List<GroupeWithEleves>>() {
            @Override
            public void onChanged(List<GroupeWithEleves> groupeWithEleves) {
                StringBuilder sb = new StringBuilder();

                for(GroupeWithEleves o : groupeWithEleves){
                    String nomGroupe = o.getGroupe().getNomGroupe();
                    sb.append("|" + nomGroupe + ": ");

                    Log.v("____________E_____________", "Nb eleve: " + o.getEleves().size());


                    for(Eleve eleve : o.getEleves()){
                        Log.v("_________E_______", "Un élève a été trouvé !");
                        sb.append(eleve.getNomPrenom() + " ");
                    }
                }

                TextView tv5 = findViewById(R.id.TEST_tv5);
                tv5.setText(sb);
            }
        });
    }
}
