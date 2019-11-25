package com.ufrst.app.trombi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;

// Simule le comportement d'un Floating Action Button (Décalement lors de l'apparition d'une snackbar par exemple)
public class FABBehavior extends CoordinatorLayout.Behavior<RelativeLayout> {

    public FABBehavior(Context context, AttributeSet attrs){

    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull RelativeLayout child, @NonNull View dependency){
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull RelativeLayout child, @NonNull View dependency){
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);

        return true;
    }
}
