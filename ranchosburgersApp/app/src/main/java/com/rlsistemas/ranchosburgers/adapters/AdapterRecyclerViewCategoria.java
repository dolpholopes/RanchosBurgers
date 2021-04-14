package com.rlsistemas.ranchosburgers.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rlsistemas.ranchosburgers.R;
import com.rlsistemas.ranchosburgers.model.Categoria;

import java.util.ArrayList;
import java.util.List;

public class AdapterRecyclerViewCategoria extends RecyclerView.Adapter<AdapterRecyclerViewCategoria.ViewHolder> {

    private Context context;
    private List<Categoria> categorias = new ArrayList<Categoria>();

    private CategoriaClick categoriaClick;

    public AdapterRecyclerViewCategoria(Context context, List<Categoria> categorias, CategoriaClick categoriaClick) {
        this.context = context;
        this.categorias = categorias;
        this.categoriaClick = categoriaClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_categoria, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Categoria categoria = categorias.get(position);

        holder.textView_nome.setText(categoria.getNome());
        Glide.with(context).load(categoria.getUrl_imagem()).into(holder.imageView);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoriaClick.categoriaOnClick(categoria);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    public interface CategoriaClick{
        void categoriaOnClick(Categoria categoria);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;
        private ImageView imageView;
        private TextView textView_nome;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView_item_categoria);
            imageView = itemView.findViewById(R.id.imageView_item_categoriaImagem);
            textView_nome = itemView.findViewById(R.id.textView_item_categoriaNome);
        }
    }


}
