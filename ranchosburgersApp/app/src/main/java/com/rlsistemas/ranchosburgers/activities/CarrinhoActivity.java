package com.rlsistemas.ranchosburgers.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rlsistemas.ranchosburgers.R;
import com.rlsistemas.ranchosburgers.adapters.AdapterRecyclerViewCarrinho;
import com.rlsistemas.ranchosburgers.model.Produto;
import com.rlsistemas.ranchosburgers.singleton.Carrinho;
import com.rlsistemas.ranchosburgers.util.DialogProgress;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CarrinhoActivity extends AppCompatActivity implements View.OnClickListener, AdapterRecyclerViewCarrinho.CarrinhoClick {

    private RecyclerView recyclerView;
    private TextView textView_continuar;
    private TextView textView_valorTotal;

    private AdapterRecyclerViewCarrinho adapterRecyclerViewCarrinho;
    private List<Produto> produtos = new ArrayList<Produto>();

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrinho);

        configToolbar();


        recyclerView = findViewById(R.id.recyclerView_carrinho);
        textView_continuar = findViewById(R.id.textView_carrinho_continuar);
        textView_valorTotal = findViewById(R.id.textView_carrinho_valorTotal);

        textView_continuar.setOnClickListener(this);

        produtos = Carrinho.getInstance().getProdutosCarrinho();

        configRecyclerView();

        atualizarValorTotalProdutos();
    }

    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView textView = findViewById(R.id.textView_toolbar);
        textView.setText("Meus pedidos");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return true;
    }

    private void atualizarValorTotalProdutos(){
        double valorTotal = 0;
        for (Produto produto: produtos){
            double valor = Double.valueOf(produto.getValor());
            valorTotal = valorTotal + valor;
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String valorTotalString = decimalFormat.format(valorTotal);

        textView_valorTotal.setText("R$ " + valorTotalString);
    }

    private void configRecyclerView() {
        adapterRecyclerViewCarrinho = new AdapterRecyclerViewCarrinho(this, produtos, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapterRecyclerViewCarrinho);
    }

    @Override
    public void carrinhoOnClick(Produto produto) {
        produtos.remove(produto);
        adapterRecyclerViewCarrinho.notifyDataSetChanged();
        atualizarValorTotalProdutos();
        Toast.makeText(getBaseContext(), "O item " + produto.getNome() + " foi removido", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.textView_carrinho_continuar:
                consultarEstadoEmpresa();
                break;
        }
    }

    private void consultarEstadoEmpresa(){
        DialogProgress dialogProgress = new DialogProgress();
        dialogProgress.show(getSupportFragmentManager(),"33");

        firestore.collection("app").document("estadoempresa").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                dialogProgress.dismiss();
                if (documentSnapshot.exists()){
                    boolean estadoEmpresa = (Boolean) documentSnapshot.getData().get("empresaaberta");
                    if (estadoEmpresa){
                        buttonCarrinhoContinuar();
                    }else{
                        dialogoEstadoEmpresa();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialogProgress.dismiss();
                Toast.makeText(getBaseContext(), "Erro no servidor, tente novamente!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buttonCarrinhoContinuar(){
        if (produtos.isEmpty()){
            Toast.makeText(getBaseContext(), "Nenhum item foi adicionado ao carrinho", Toast.LENGTH_SHORT).show();
        }else{
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null){
                dialogoOpcaoPagamento();
            }else{
                startActivity(new Intent(this, LoginActivity.class));
            }
        }
    }

    private void dialogoOpcaoPagamento(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Escolha uma opção")
                .setPositiveButton("Retirar pedido no local", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getBaseContext(), RetirarLocalActivity.class));
                    }
                }).setNegativeButton("Receber pedido em casa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getBaseContext(), PedidoReceberEmCasaActivity.class));
                    }
                }).create();

        dialog.show();
    }

    private void dialogoEstadoEmpresa(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("O delivery esta fechado no momento\n\nHorário de funcionamento das 18:00 às 00:00 horas")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();

        dialog.show();
    }
}