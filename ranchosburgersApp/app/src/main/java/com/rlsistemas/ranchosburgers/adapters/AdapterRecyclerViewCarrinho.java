package com.rlsistemas.ranchosburgers.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.rlsistemas.ranchosburgers.R;
import com.rlsistemas.ranchosburgers.model.Produto;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AdapterRecyclerViewCarrinho extends RecyclerView.Adapter<AdapterRecyclerViewCarrinho.ViewHolder> {

    private Context context;
    private List<Produto> produtos = new ArrayList<Produto>();
    private CarrinhoClick carrinhoClick;


    public AdapterRecyclerViewCarrinho(Context context, List<Produto> produtos, CarrinhoClick carrinhoClick) {
        this.context = context;
        this.produtos = produtos;
        this.carrinhoClick = carrinhoClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_carrinho, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Produto produto = produtos.get(position);

        double valorDouble = Double.parseDouble(produto.getValor());
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String valor = decimalFormat.format(valorDouble);

        holder.textView_nome.setText(produto.getNome());
        holder.textView_descricao.setText(produto.getDescricao());
        holder.textView_valor.setText(valor);
        holder.textView_adicional.setText(produto.getAdicional());


        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carrinhoClick.carrinhoOnClick(produto);
            }
        });

    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }

    public interface CarrinhoClick {
        void carrinhoOnClick(Produto produto);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textView_nome;
        private TextView textView_descricao;
        private TextView textView_valor;
        private TextView textView_adicional;
        private ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_item_carrinhoProdutoRemover);
            textView_nome = itemView.findViewById(R.id.textView_item_carrinhoProdutoNome);
            textView_descricao = itemView.findViewById(R.id.textView_item_carrinhoProdutoDescricao);
            textView_valor = itemView.findViewById(R.id.textView_item_carrinhoProdutoValor);
            textView_adicional = itemView.findViewById(R.id.textView_item_carrinhoProdutoAdicional);



        }
    }


}
