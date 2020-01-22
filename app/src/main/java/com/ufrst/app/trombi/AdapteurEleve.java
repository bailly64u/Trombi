package com.ufrst.app.trombi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.ufrst.app.trombi.database.Eleve;
import com.ufrst.app.trombi.database.Trombinoscope;

import java.util.List;

public class AdapteurEleve extends ListAdapter<Eleve, AdapteurEleve.EleveHolder>{

    private OnItemClickListener listener;
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

    // Retourne l'Eleve d'une certaine position
    public Eleve getEleveAt(int pos){
        return getItem(pos);
    }

    class EleveHolder extends RecyclerView.ViewHolder{
        private ImageButton bGroupe, bPhoto;
        private ImageView ivPortrait;
        private TextView tvNomPrenom;

        EleveHolder(View itemView){
            super(itemView);

            tvNomPrenom = itemView.findViewById(R.id.ELEVEITEM_nomPrenom);
            ivPortrait = itemView.findViewById(R.id.ELEVEITEM_portrait);
            bGroupe = itemView.findViewById(R.id.ELEVEITEM_gererGroupe);
            bPhoto = itemView.findViewById(R.id.ELEVEITEM_prendrePhoto);

            // Listeners
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    int pos = getAdapterPosition();             // Récupération position item cliqué

                    if(listener != null && pos != RecyclerView.NO_POSITION){
                        listener.onItemClick(getItem(pos));     // Récupération objet dans la liste
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v){
                    int pos = getAdapterPosition();

                    if(listener != null && pos != RecyclerView.NO_POSITION){
                        listener.onItemLongClick(getItem(pos));
                    }

                    return true;
                }
            });

            bPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    int pos = getAdapterPosition();                 // Récupération position item cliqué

                    if(listener != null && pos != RecyclerView.NO_POSITION){
                        listener.onPhotoClick(getItem(pos));        // Récupération objet dans la liste
                    }
                }
            });

            bGroupe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    int pos = getAdapterPosition();                 // Récupération position item cliqué

                    if(listener != null && pos != RecyclerView.NO_POSITION){
                        listener.onGroupClick(getItem(pos));        // Récupération objet dans la liste
                    }
                }
            });
        }
    }

    @Override
    public void submitList(@Nullable List<Eleve> list){
        super.submitList(list);
    }

    // Interface permettant de gérer le clique dans l'activité principale et son contexte
    public interface OnItemClickListener{
        void onItemClick(Eleve eleve);
        void onItemLongClick(Eleve eleve);
        void onGroupClick(Eleve eleve);
        void onPhotoClick(Eleve eleve);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}