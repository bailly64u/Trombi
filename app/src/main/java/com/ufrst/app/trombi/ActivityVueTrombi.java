package com.ufrst.app.trombi;

import android.os.Bundle;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

public class ActivityVueTrombi extends AppCompatActivity {

    private BottomSheetBehavior mBottomSheetBehavior;
    private CoordinatorLayout mCoordinatorLayout;
    private ChipGroup mChipGroup;
    private View bottomSheet;
    private WebView mWebView;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vue_trombi);

        findViews();
        setListeners();
        showHTML();
        setChips();

        // Toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Met en place la Bottom Sheet persistante, qui contindra les filtres
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
    }

    private void findViews(){
        mCoordinatorLayout = findViewById(R.id.VUETROMBI_coordinator);
        bottomSheet = findViewById(R.id.VUETROMBI_bottomSheet);
        mChipGroup = findViewById(R.id.VUETROMBI_chipsGroup);
        mToolbar = findViewById(R.id.VUETROMBI_toolbar);
        mWebView = findViewById(R.id.VUETROMBI_webView);
    }

    private void setListeners(){
        FloatingActionButton fab = findViewById(R.id.VUETROMBI_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // Bottom sheeet dialog
            }
        });
    }

    // Insère le HTML dans la WebView
    private void showHTML(){
        String summary = "<html><body>You scored <b>192</b> points.</body></html>";
        mWebView.loadData(summary, "text/html", "UTF-8");
    }

    // Instancie positionne, et met en place les listeners des chips dans la bottom sheet
    private void setChips(){
        // Instancitaion des chips selon les groupes disponibles
        for(int i=0; i < 10; i++){
            Chip c = (Chip) this.getLayoutInflater().inflate(R.layout.chips_choice, mChipGroup, false);
            c.setText("TD " + i);

            // Ajout de la chips dans le groupe
            mChipGroup.addView(c);

            // Listeners
            mChipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(ChipGroup chipGroup, int i){
                    Chip chip = findViewById(i);
                    if(chip != null){
                        Toast.makeText(ActivityVueTrombi.this, "Chip text is: " + chip.getText(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return super.onSupportNavigateUp();
    }
}
