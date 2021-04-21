package com.rlsistemas.ranchosburgers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.rlsistemas.ranchosburgers.R;
import com.rlsistemas.ranchosburgers.adapters.AdapterRecyclerViewAdicional;
import com.rlsistemas.ranchosburgers.model.Adicional;
import com.rlsistemas.ranchosburgers.model.Produto;
import com.rlsistemas.ranchosburgers.singleton.Carrinho;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AdicionaisActivity extends AppCompatActivity implements View.OnClickListener, AdapterRecyclerViewAdicional.AdicionalClick {

    private RecyclerView recyclerView;

    private FirebaseFirestore firestore;

    private AdapterRecyclerViewAdicional adapterRecyclerViewAdicional;
    private List<Adicional> adicionais = new ArrayList<Adicional>();
    private List<String> keys = new ArrayList<String>();

    private ImageView imageView;
    private TextView textView_nome;
    private TextView textView_descricao;
    private EditText editText_observacao;
    private TextView textView_valorTotal;
    private TextView textView_AdicionarCarrinho;

    private TextView textView_infoQuantAdicionais;

    private TextView textView_quantidade;
    private ImageView imageView_adicionar;
    private ImageView imageView_remover;

    private Produto produto;

    private int quantidadeProduto = 1;
    private double valorTotalPedido = 0;

    private List<Adicional> adicionaisSelecionados = new ArrayList<Adicional>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionais);

        firestore = FirebaseFirestore.getInstance();

        configToolbar();
        atualizarView();

        if (produto.isPossui_adicional()) {
            configRecyclerView();
            iniciarOuvinteAdicional();
            textView_infoQuantAdicionais.setVisibility(View.VISIBLE);
        }

    }

    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView textView = findViewById(R.id.textView_toolbar);
        textView.setText("Faça seu pedido");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return true;
    }

    private void atualizarView() {
        recyclerView = findViewById(R.id.recyclerView_adicionais);

        imageView = findViewById(R.id.imageView_detalhes_produto);
        textView_nome = findViewById(R.id.textView_detalhes_produtoNome);
        textView_descricao = findViewById(R.id.textView_detalhes_produtoDescricao);
        textView_valorTotal = findViewById(R.id.textView_detalhes_produtoValorTotal);
        editText_observacao = findViewById(R.id.editText_detalhes_produtoObservacao);
        textView_AdicionarCarrinho = findViewById(R.id.textView_detalhes_produtoAdicionarCarrinho);
        textView_infoQuantAdicionais = findViewById(R.id.textView_detalhes_produtoInfoQuantAdicionais);

        textView_quantidade = findViewById(R.id.textView_detalhes_produtoQuantidade);
        imageView_adicionar = findViewById(R.id.imageView_detalhes_produtoAdicionar);
        imageView_remover = findViewById(R.id.imageView_detalhes_produtoRemover);

        imageView_adicionar.setOnClickListener(this);
        imageView_remover.setOnClickListener(this);
        textView_AdicionarCarrinho.setOnClickListener(this);

        produto = getIntent().getParcelableExtra("produto");

        Glide.with(getBaseContext()).load(produto.getUrl_imagem()).into(imageView);
        textView_nome.setText(produto.getNome());
        textView_descricao.setText(produto.getDescricao());
        textView_valorTotal.setText("R$ " + produto.getValor());

        valorTotalPedido = Double.valueOf(produto.getValor());
    }


    private void configRecyclerView() {
        adapterRecyclerViewAdicional = new AdapterRecyclerViewAdicional(this, adicionais, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapterRecyclerViewAdicional);
    }


    @Override
    public void adicionalOnClick(Adicional adicional, boolean statusCheckBox, CheckBox checkBox) {

        if (adicionaisSelecionados.size() < 15 || !statusCheckBox) {

            if (statusCheckBox) {
                adicionaisSelecionados.add(adicional);
            } else {
                adicionaisSelecionados.remove(adicional);
            }

            checkBox.setChecked(statusCheckBox);
            atualizarValorProduto();
        } else {
            Toast.makeText(getBaseContext(), "Limite de adicionais atingido", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView_detalhes_produtoAdicionar:
                quantidadeProduto++;
                atualizarValorProduto();
                break;

            case R.id.imageView_detalhes_produtoRemover:
                if (quantidadeProduto > 1) {
                    quantidadeProduto--;
                    atualizarValorProduto();
                }
                break;

            case R.id.textView_detalhes_produtoAdicionarCarrinho:
                buttonAdicionarCarrinho();
                break;
        }
    }


    private void buttonAdicionarCarrinho() {

        double valorProduto = Double.valueOf(produto.getValor());
        double valorAtual = quantidadeProduto * valorProduto;

        if (valorAtual == valorTotalPedido) {
            possuiAdicionalNao();
        } else {
            possuiAdicionalSim();
        }

        finish();
        startActivity(new Intent(this, CarrinhoActivity.class));
    }

    private void possuiAdicionalNao() {
        adicionarPedidoCarrinho(produto.getValor(), "Sem Adicional");
    }

    private void possuiAdicionalSim() {
        String adicionaisCarrinho = "Adicional: ";
        for (Adicional adicional : adicionaisSelecionados) {
            adicionaisCarrinho = adicionaisCarrinho + " - " + adicional.getNome();
        }

        double valorCadaProduto = valorTotalPedido / quantidadeProduto;
        String valorCadaprodutoString = String.valueOf(valorCadaProduto);

        adicionarPedidoCarrinho(valorCadaprodutoString, adicionaisCarrinho);
    }

    private void adicionarPedidoCarrinho(String valor, String adicionais) {

        List<Produto> produtosCarrinho = Carrinho.getInstance().getProdutosCarrinho();

        String observacao = editText_observacao.getText().toString();

        for (int i = 0; i < quantidadeProduto; i++) {
            Produto prod;
            if (observacao.trim().isEmpty()) {
                prod = new Produto(produto.getNome(), produto.getDescricao(), valor, adicionais, "Sem Observação");
            } else {
                prod = new Produto(produto.getNome(), produto.getDescricao(), valor, adicionais, "Observação: " + observacao);
            }
            produtosCarrinho.add(prod);
        }
    }

    private void atualizarValorProduto() {

        double valorProduto = Double.valueOf(produto.getValor());

        double adicionalValor = 0;

        for (Adicional ad : adicionaisSelecionados) {
            adicionalValor = adicionalValor + Double.valueOf(ad.getValor());
        }

        double valor = valorProduto * quantidadeProduto;
        double valorTotal = valor + (adicionalValor * quantidadeProduto);

        atualizarValorProdutoView(valorTotal);
    }

    private void atualizarValorProdutoView(double valorTotal) {

        valorTotalPedido = valorTotal;

        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String valorTotalString = decimalFormat.format(valorTotal);

        textView_valorTotal.setText("R$ " + valorTotalString);
        textView_quantidade.setText(quantidadeProduto + "");
    }


    private void iniciarOuvinteAdicional() {
        CollectionReference reference = firestore.collection("adicionais");

        reference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                Adicional adicional;
                int index;
                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    switch (doc.getType()) {
                        case ADDED:
                            adicional = doc.getDocument().toObject(Adicional.class);
                            adicionais.add(adicional);
                            keys.add(adicional.getId());
                            adapterRecyclerViewAdicional.notifyDataSetChanged();
                            break;

                        case MODIFIED:
                            adicional = doc.getDocument().toObject(Adicional.class);
                            index = keys.indexOf(adicional.getId());
                            adicionais.set(index, adicional);
                            adapterRecyclerViewAdicional.notifyDataSetChanged();
                            break;

                        case REMOVED:
                            adicional = doc.getDocument().toObject(Adicional.class);
                            index = keys.indexOf(adicional.getId());
                            adicionais.remove(index);
                            keys.remove(index);
                            adapterRecyclerViewAdicional.notifyDataSetChanged();
                            break;
                    }
                }
            }
        });
    }

}