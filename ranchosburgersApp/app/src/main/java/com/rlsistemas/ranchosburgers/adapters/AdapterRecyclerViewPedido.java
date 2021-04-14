package com.rlsistemas.ranchosburgers.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.rlsistemas.ranchosburgers.R;
import com.rlsistemas.ranchosburgers.model.Pedido;
import com.rlsistemas.ranchosburgers.util.Util;

import java.util.ArrayList;
import java.util.List;

public class AdapterRecyclerViewPedido extends RecyclerView.Adapter<AdapterRecyclerViewPedido.ViewHolder> {

    private Context context;
    private List<Pedido> pedidos = new ArrayList<Pedido>();

    public AdapterRecyclerViewPedido(Context context, List<Pedido> pedidos) {
        this.context = context;
        this.pedidos = pedidos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_pedido, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Pedido pedido = pedidos.get(position);

        String data = Util.dataPedido(pedido.getPedido_data());

        String dados = "Pedido: \n " + pedido.getPedido_dados().replaceAll("<br>", " \n ");
        String status = "Status Pedido: " + pedido.getPedido_status();
        String dataPedido = "Data Pedido: "+ data;
        String valor = "Valor Pedido: " + " R$ " + pedido.getPedido_valor();


        holder.textView_dados.setText(dados);
        holder.textView_data.setText(dataPedido);
        holder.textView_status.setText(status);
        holder.textView_valor.setText(valor);


    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textView_dados;
        private TextView textView_status;
        private TextView textView_data;
        private TextView textView_valor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView_dados = itemView.findViewById(R.id.textView_item_pedidoDados);
            textView_status = itemView.findViewById(R.id.textView_item_pedidoStatus);
            textView_data = itemView.findViewById(R.id.textView_item_pedidoData);
            textView_valor = itemView.findViewById(R.id.textView_item_pedidoValor);
        }
    }


}
