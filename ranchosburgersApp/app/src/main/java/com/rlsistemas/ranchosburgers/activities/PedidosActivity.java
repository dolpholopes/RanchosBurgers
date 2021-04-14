package com.rlsistemas.ranchosburgers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rlsistemas.ranchosburgers.R;
import com.rlsistemas.ranchosburgers.adapters.AdapterRecyclerViewPedido;
import com.rlsistemas.ranchosburgers.model.Pedido;

import java.util.ArrayList;
import java.util.List;

public class PedidosActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private AdapterRecyclerViewPedido adapterRecyclerViewPedido;
    private List<Pedido> pedidos = new ArrayList<Pedido>();
    private List<String> keys = new ArrayList<String>();
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);

        configToolbar();

        recyclerView = findViewById(R.id.recyclerView_pedidos);
        firestore = FirebaseFirestore.getInstance();

        configRecyclerView();

        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            iniciarOuvinte();
        }else{
            finish();
            startActivity(new Intent(getBaseContext(), LoginActivity.class));
        }

    }

    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView textView = findViewById(R.id.textView_toolbar);
        textView.setText("Meus ultimos pedidos");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return true;
    }


    private void configRecyclerView() {
        adapterRecyclerViewPedido = new AdapterRecyclerViewPedido(getBaseContext(), pedidos);
        GridLayoutManager gridLayout = new GridLayoutManager(getBaseContext(), 1);
        recyclerView.setLayoutManager(gridLayout);
        recyclerView.setAdapter(adapterRecyclerViewPedido);
    }

    private void iniciarOuvinte(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        CollectionReference reference = firestore.collection("pedidos");

        Query query = reference.whereEqualTo("cliente_uid", uid).orderBy("pedido_data", Query.Direction.DESCENDING);

        listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
               if (error!=null){
                   Log.d("TAG","Error:"+ error.getMessage());
               }else {
                   Pedido pedido;
                   int index;

                   for (DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                       switch (doc.getType()){
                           case ADDED:
                               pedido = doc.getDocument().toObject(Pedido.class);
                               pedidos.add(pedido);
                               keys.add(pedido.getPedido_id());
                               adapterRecyclerViewPedido.notifyDataSetChanged();
                               break;

                           case MODIFIED:
                               pedido = doc.getDocument().toObject(Pedido.class);
                               index = keys.indexOf(pedido.getPedido_id());
                               pedidos.set(index, pedido);
                               adapterRecyclerViewPedido.notifyDataSetChanged();
                               break;

                           case REMOVED:
                               pedido = doc.getDocument().toObject(Pedido.class);
                               index = keys.indexOf(pedido.getPedido_id());
                               pedidos.remove(index);
                               keys.remove(index);
                               adapterRecyclerViewPedido.notifyItemRemoved(index);
                               adapterRecyclerViewPedido.notifyItemChanged(index,pedidos.size());
                               break;
                       }
                   }
               }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null){
            listenerRegistration.remove();
        }
    }
}