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
import com.rlsistemas.ranchosburgers.model.Produto;

import java.util.ArrayList;
import java.util.List;

public class AdapterRecyclerViewProduto extends RecyclerView.Adapter<AdapterRecyclerViewProduto.ViewHolder> {

    private Context context;
    private List<Produto> produtos = new ArrayList<Produto>();
    private ProdutoClick produtoClick;

    public AdapterRecyclerViewProduto(Context context, List<Produto> produtos, ProdutoClick produtoClick) {
        this.context = context;
        this.produtos = produtos;
        this.produtoClick = produtoClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_produto, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Produto produto = produtos.get(position);

        String valor = "R$ "+ produto.getValor();

        holder.textView_nome.setText(produto.getNome());
        holder.textView_descricao.setText(produto.getDescricao());
        holder.textView_valor.setText(valor);
        Glide.with(context).load(produto.getUrl_imagem()).into(holder.imageView);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                produtoClick.produtoOnClick(produto);
            }
        });

    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }

    public interface ProdutoClick{
        void produtoOnClick(Produto produto);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textView_nome;
        private TextView textView_descricao;
        private TextView textView_valor;
        private CardView cardView;
        private ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView_item_produto);
            imageView = itemView.findViewById(R.id.imageView_item_produtoImagem);
            textView_nome = itemView.findViewById(R.id.textView_item_produtoNome);
            textView_descricao = itemView.findViewById(R.id.textView_item_produtoDescricao);
            textView_valor = itemView.findViewById(R.id.textView_item_produtoValor);

        }
    }


}
