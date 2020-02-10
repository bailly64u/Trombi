package com.ufrst.app.trombi.ui

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import android.widget.RelativeLayout
import android.widget.Switch
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.ufrst.app.trombi.R
import com.ufrst.app.trombi.ui.ActivityMain.PREFS_FIXED_RATIO
import com.ufrst.app.trombi.ui.ActivityMain.PREFS_NBCOLS
import com.ufrst.app.trombi.util.Logger

class ActivityParametre : AppCompatActivity() {

    private val switchRatioLayout : RelativeLayout by bind(R.id.PARA_switchNbColLayout)
    private val switchFixedRatio : Switch by bind(R.id.PARA_switchFixedRatio)
    private val numberPicker : NumberPicker by bind(R.id.PARA_npCol)
    private val toolbar : Toolbar by bind(R.id.PARA_toolbar)
    private lateinit var prefs : SharedPreferences

    private var nbCols = -1
    private var isFixedRatio = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parametre)

        retrieveSharedPreferences()
        initializeComponent()
        setListeners()

        // Toolbar
        setSupportActionBar(toolbar)
        setTitle(R.string.PARA_title)
    }

    private fun retrieveSharedPreferences(){
        prefs = getSharedPreferences("com.ufrst.app.trombi", Context.MODE_PRIVATE)
        nbCols = prefs.getInt(PREFS_NBCOLS, 4)
        isFixedRatio = prefs.getBoolean(PREFS_FIXED_RATIO, true)
    }

    // Actualise la valeur des composants selon les SharedPreferences
    private fun initializeComponent(){
        with(numberPicker){
            minValue = 1
            maxValue = 6
            value = nbCols
        }

        switchFixedRatio.isChecked = isFixedRatio
    }

    private fun setListeners(){
        switchRatioLayout.setOnClickListener {
            switchFixedRatio.isChecked = !switchFixedRatio.isChecked
        }
    }

    // Fonction d'extention simplifiant les findViewById et permet de rendre les composants lazy-evaluated
    private fun <T : View> Activity.bind(@IdRes res : Int) : Lazy<T> {
        return lazy { findViewById<T>(res) }
    }

    override fun onPause() {
        prefs.edit().apply{
            putInt(PREFS_NBCOLS, numberPicker.value)
            putBoolean(PREFS_FIXED_RATIO, switchFixedRatio.isChecked)
            apply()
        }

        super.onPause()
    }
}