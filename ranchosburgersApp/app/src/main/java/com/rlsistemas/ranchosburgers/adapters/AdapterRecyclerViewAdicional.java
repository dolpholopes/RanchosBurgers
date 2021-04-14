package com.rlsistemas.ranchosburgers.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.rlsistemas.ranchosburgers.R;
import com.rlsistemas.ranchosburgers.model.Adicional;

import java.util.ArrayList;
import java.util.List;

public class AdapterRecyclerViewAdicional extends RecyclerView.Adapter<AdapterRecyclerViewAdicional.ViewHolder> {

    private Context context;
    private List<Adicional> adicionais = new ArrayList<Adicional>();
    private AdicionalClick adicionalClick;

    public AdapterRecyclerViewAdicional(Context context, List<Adicional> adicionais, AdicionalClick adicionalClick) {
        this.context = context;
        this.adicionais = adicionais;
        this.adicionalClick = adicionalClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclerview_adicional, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Adicional adicional = adicionais.get(position);

        holder.textView_nome.setText(adicional.getNome());
        holder.textView_valor.setText("R$ "+adicional.getValor());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.checkBox.isChecked()){

                    adicionalClick.adicionalOnClick(adicional, false, holder.checkBox);
                }else{
                    adicionalClick.adicionalOnClick(adicional, true, holder.checkBox);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return adicionais.size();
    }

    public interface AdicionalClick {
        void adicionalOnClick(Adicional adicional, boolean b, CheckBox checkBox);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CheckBox checkBox;
        private TextView textView_nome;
        private TextView textView_valor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            checkBox = itemView.findViewById(R.id.checkbox_item_adicionalCheckbox);
            textView_nome = itemView.findViewById(R.id.textView_item_adicionalNome);
            textView_valor = itemView.findViewById(R.id.textView_item_adicionalValor);
        }
    }


}
