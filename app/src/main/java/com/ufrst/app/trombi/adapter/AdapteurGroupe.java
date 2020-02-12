package com.ufrst.app.trombi.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ufrst.app.trombi.R;
import com.ufrst.app.trombi.database.Groupe;

import java.util.List;

// ListeAdapter s'apparente à un RecyclerView, mais avec des méthodes pour gérer les animations
// d'insertions, suppresion etc aux bons endroits dans la liste
public class AdapteurGroupe extends ListAdapter<Groupe, AdapteurGroupe.GroupeHolder> {

    private OnItemClickListener listener;                   // Interface
    private LayoutInflater inflater;

    public AdapteurGroupe(){
        super(DIFF_CALLBACK);
    }

    // Création de la logique de comparaison des items de la liste pour application des animations
    private static final DiffUtil.ItemCallback<Groupe> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Groupe>() {
                @Override
                public boolean areItemsTheSame(@NonNull Groupe oldItem, @NonNull Groupe newItem){
                    return oldItem.getIdGroupe() == newItem.getIdGroupe();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Groupe oldItem, @NonNull Groupe newItem){
                    return oldItem.getNomGroupe().equals(newItem.getNomGroupe()) &&
                            oldItem.getIdGroupe() == newItem.getIdGroupe() &&
                            oldItem.getIdTrombi() == newItem.getIdTrombi();
                }
            };

    @NonNull
    @Override
    public GroupeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        if(inflater == null){
            inflater = LayoutInflater.from(parent.getContext());
        }

        View itemView = inflater.inflate(R.layout.groupe_item, parent, false);

        return new GroupeHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupeHolder holder, int position){
        Groupe currentGroupe = getItem(position);
        holder.textViewNom.setText(currentGroupe.getNomGroupe());
    }

    // Retourne le Groupe d'une certaine position
    public Groupe getGroupeAt(int pos){
        return getItem(pos);
    }

    // Classe interne permettant de contenir les informations à afficher dans la liste
    class GroupeHolder extends RecyclerView.ViewHolder{
        private TextView textViewNom;           // Nom du groupe

        GroupeHolder(View itemView){
            super(itemView);

            // Gestion des vues
            textViewNom = itemView.findViewById(R.id.GROUPEITEM_nom);

            // Gestion des listeners
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();             // Récupération position item cliqué

                if(listener != null && pos != RecyclerView.NO_POSITION){
                    listener.onItemClick(getItem(pos));     // Récupération objet dans la liste
                }
            });

            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();

                if(listener != null && pos != RecyclerView.NO_POSITION){
                    listener.onItemLongClick(getItem(pos));
                }

                return true;
            });
        }
    }

    @Override
    public void submitList(@Nullable List<Groupe> list){
        super.submitList(list);
    }

    // Interface permettant de gérer le clique dans l'activité principale et son contexte
    public interface OnItemClickListener{
        void onItemClick(Groupe groupe);
        void onItemLongClick(Groupe groupe);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
