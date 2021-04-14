package com.rlsistemas.ranchosburgers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rlsistemas.ranchosburgers.R;
import com.rlsistemas.ranchosburgers.adapters.AdapterRecyclerViewProduto;
import com.rlsistemas.ranchosburgers.model.Produto;

import java.util.ArrayList;
import java.util.List;

public class ProdutosActivity extends AppCompatActivity implements AdapterRecyclerViewProduto.ProdutoClick {

    private RecyclerView recyclerView;
    private AdapterRecyclerViewProduto adapterRecyclerViewProduto;
    private List<Produto> produtos = new ArrayList<Produto>();
    private List<String> keys = new ArrayList<String>();

    private FirebaseFirestore firestore;

    private String idCategoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produtos);

        firestore = FirebaseFirestore.getInstance();

        //Configurações da toolbar
        configToolbar();
        idCategoria = getIntent().getStringExtra("idCategoria");
        recyclerView = findViewById(R.id.recyclerView_produtos);
        configRecyclerView();
        iniciarOuvinteProduto();
    }

    // ---------------- CLICK VOLTAR DA TOOLBAR -----------------------

    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String nomeCategoria = getIntent().getStringExtra("nomeCategoria");
        TextView textView = findViewById(R.id.textView_toolbar);
        textView.setText(nomeCategoria);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return true;
    }


    private void configRecyclerView() {
        adapterRecyclerViewProduto = new AdapterRecyclerViewProduto(this, produtos, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapterRecyclerViewProduto);
    }

    //click item lista
    @Override
    public void produtoOnClick(Produto produto) {
        Intent intent = new Intent(this, AdicionaisActivity.class);

        intent.putExtra("produto",produto);

        startActivity(intent);
    }


    private void iniciarOuvinteProduto() {
        CollectionReference reference = firestore.collection("produtos");

        Query query = reference.whereEqualTo("categoria_id", idCategoria);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                Produto produto;
                int index;
                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    switch (doc.getType()) {
                        case ADDED:
                            produto = doc.getDocument().toObject(Produto.class);
                            produtos.add(produto);
                            keys.add(produto.getId());
                            adapterRecyclerViewProduto.notifyDataSetChanged();
                            break;

                        case MODIFIED:
                            produto = doc.getDocument().toObject(Produto.class);
                            index = keys.indexOf(produto.getId());
                            produtos.set(index, produto);
                            adapterRecyclerViewProduto.notifyDataSetChanged();
                            break;

                        case REMOVED:
                            produto = doc.getDocument().toObject(Produto.class);
                            index = keys.indexOf(produto.getId());
                            produtos.remove(index);
                            keys.remove(index);
                            adapterRecyclerViewProduto.notifyDataSetChanged();
                            break;
                    }
                }
            }
        });
    }
}