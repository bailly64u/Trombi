package com.ufrst.app.trombi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.ufrst.app.trombi.database.Eleve;

import java.util.List;

public class AdapteurEleve extends ListAdapter<Eleve, AdapteurEleve.EleveHolder>{

    private LayoutInflater inflater;

    public AdapteurEleve(){
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Eleve> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Eleve>() {
                @Override
                public boolean areItemsTheSame(@NonNull Eleve oldItem, @NonNull Eleve newItem){
                    return oldItem.getIdEleve() == newItem.getIdEleve();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Eleve oldItem, @NonNull Eleve newItem){
                    return oldItem.getNomPrenom().equals(newItem.getNomPrenom()) &&
                            oldItem.getPhoto().equals(newItem.getPhoto()) &&
                            oldItem.getIdTrombi() == newItem.getIdTrombi();
                }
            };

    @NonNull
    @Override
    public EleveHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        if(inflater == null){
            inflater = LayoutInflater.from(parent.getContext());
        }

        View itemView = inflater.inflate(R.layout.eleve_item, parent, false);

        return new EleveHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EleveHolder holder, int position){
        Eleve currentEleve = getItem(position);
        holder.tvNomPrenom.setText(currentEleve.getNomPrenom());

        //TODO: mettre l'image à l'aide de Glide
    }

    class EleveHolder extends RecyclerView.ViewHolder{
        private MaterialButton b1, b2;
        private ImageView ivPortrait;
        private TextView tvNomPrenom;

        EleveHolder(View itemView){
            super(itemView);

            //TODO: gérer les cliques et les findview
            tvNomPrenom = itemView.findViewById(R.id.ELEVEITEM_nomPrenom);

        }
    }

    @Override
    public void submitList(@Nullable List<Eleve> list){
        super.submitList(list);
    }
}