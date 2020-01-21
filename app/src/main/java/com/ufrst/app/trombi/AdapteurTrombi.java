package com.ufrst.app.trombi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ufrst.app.trombi.database.Trombinoscope;

import java.util.List;

// ListeAdapter s'apparente à un RecyclerView, mais avec des méthodes pour gére les animations
// d'insertions, suppresion etc aux bons endroits dans la liste
public class AdapteurTrombi extends ListAdapter<Trombinoscope, AdapteurTrombi.TrombiHolder> {

    private OnItemClickListener listener;                   // Interface

    public AdapteurTrombi(){
        super(DIFF_CALLBACK);
    }

    // Création de la logique de comparaison des items de la liste pour application des animations
    private static final DiffUtil.ItemCallback<Trombinoscope> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Trombinoscope>() {
        @Override
        public boolean areItemsTheSame(@NonNull Trombinoscope oldItem, @NonNull Trombinoscope newItem){
            return oldItem.getIdTrombi() == newItem.getIdTrombi();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Trombinoscope oldItem, @NonNull Trombinoscope newItem){
            return oldItem.getNomTrombi().equals(newItem.getNomTrombi()) &&
                    oldItem.getDescription().equals(newItem.getDescription());
        }
    };

    @NonNull
    @Override
    public TrombiHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trombi_item, parent, false);

        return new TrombiHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TrombiHolder holder, int position){
        Trombinoscope currentTrombi = getItem(position);
        holder.textViewNom.setText(currentTrombi.getNomTrombi() + " - " + currentTrombi.getIdTrombi());
        holder.textViewDesc.setText(currentTrombi.getDescription());

        // Récupérer le nombre d'élèves, VM étant un viewModel passé par le constructeur
        // Cancer ?
        /*Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run(){
                int i = vm.getElevesNumberByTrombi(currentTrombi.getIdTrombi());
                holder.mTextViewNombre.post(() -> holder.mTextViewNombre.setText(String.valueOf(i)));
            }
        });*/
    }

    // Retourne le Trombinoscope d'une certaine position
    public Trombinoscope getTrombiAt(int pos){
        return getItem(pos);
    }

    // Classe interne permettant de contenir les informations à afficher dans la liste
    class TrombiHolder extends RecyclerView.ViewHolder{
        private TextView textViewNom;           // Nom du trombi
        private TextView textViewDesc;          // Description du trombi
        private ImageButton ibNombre;           // Nb d'élèves dans le trombi

        TrombiHolder(View itemView){
            super(itemView);

            // Gestion des vues
            textViewNom = itemView.findViewById(R.id.TROMBIITEM_nom);
            textViewDesc = itemView.findViewById(R.id.TROMBIITEM_description);
            ibNombre = itemView.findViewById(R.id.TROMBIITEM_action);

            // Gestion des listeners
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

            ibNombre.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    int pos = getAdapterPosition();             // Récupération position item cliqué

                    if(listener != null && pos != RecyclerView.NO_POSITION){
                        listener.onEditClick(getItem(pos));     // Récupération objet dans la liste
                    }
                }
            });
        }
    }

    @Override
    public void submitList(@Nullable List<Trombinoscope> list){
        super.submitList(list);
    }

    // Interface permettant de gérer le clique dans l'activité principale et son contexte
    public interface OnItemClickListener{
        void onItemClick(Trombinoscope trombi);
        void onItemLongClick(Trombinoscope trombi);
        void onEditClick(Trombinoscope trombi);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
