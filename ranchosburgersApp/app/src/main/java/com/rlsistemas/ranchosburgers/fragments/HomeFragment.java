package com.rlsistemas.ranchosburgers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rlsistemas.ranchosburgers.R;
import com.rlsistemas.ranchosburgers.activities.CarrinhoActivity;
import com.rlsistemas.ranchosburgers.activities.ProdutosActivity;
import com.rlsistemas.ranchosburgers.adapters.AdapterRecyclerViewCategoria;
import com.rlsistemas.ranchosburgers.adapters.SliderAdapterImage;
import com.rlsistemas.ranchosburgers.model.Categoria;
import com.rlsistemas.ranchosburgers.model.Produto;
import com.rlsistemas.ranchosburgers.singleton.Carrinho;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements View.OnClickListener, AdapterRecyclerViewCategoria.CategoriaClick {

    private SliderView sliderView;
    private AppCompatTextView textView_informativo;
    private RecyclerView recyclerView;
    private TextView textView_quantidadeCarrinho;
    private CardView cardView_carrinho;

   // Slider de imagens
    private SliderAdapterImage sliderAdapterImage;
    private List<String> urls = new ArrayList<String>();

    //Lista de categoria
    private AdapterRecyclerViewCategoria adapterRecyclerViewCategoria;
    private List<Categoria> categorias = new ArrayList<Categoria>();

    //Firebase
    private FirebaseFirestore firestore;

    private List<String> keys = new ArrayList<String>();

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sliderView = view.findViewById(R.id.imageSlider_home);
        textView_informativo = view.findViewById(R.id.textView_home_informativo);
        recyclerView = view.findViewById(R.id.recyclerView_home);
        cardView_carrinho = view.findViewById(R.id.cardView_carrinho);

        cardView_carrinho.setOnClickListener(this);

        firestore = FirebaseFirestore.getInstance();

        configRecyclerView();

        iniciarOuvinteHomeApp();
        iniciarOuvinteCategoria();

        return view;
    }

    //------------------------ AÇÕES DE CLICK ------------------------
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cardView_carrinho:
                startActivity(new Intent(getContext(), CarrinhoActivity.class));
                break;
        }
    }

    @Override
    public void categoriaOnClick(Categoria categoria) {
        Intent intent = new Intent(getContext(), ProdutosActivity.class);
        intent.putExtra("nomeCategoria", categoria.getNome());
        intent.putExtra("idCategoria", categoria.getId());
        startActivity(intent);
    }


    private void configSliderImage(String url1,String url2,String informativo){
        textView_informativo.setText(informativo);

        if (!urls.isEmpty()){
            urls.clear();
        }

        urls.add(url1);
        urls.add(url2);

        sliderAdapterImage = new SliderAdapterImage(getContext(),urls);
        sliderView.setSliderAdapter(sliderAdapterImage);

        sliderView.setScrollTimeInSec(3);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
    }

    private void configRecyclerView(){
        adapterRecyclerViewCategoria = new AdapterRecyclerViewCategoria(getContext(), categorias, this);
        GridLayoutManager gridLayout =  new GridLayoutManager(getContext(),2);
        recyclerView.setLayoutManager(gridLayout);
        recyclerView.setAdapter(adapterRecyclerViewCategoria);
    }

    private void iniciarOuvinteHomeApp(){
        DocumentReference reference = firestore.collection("app").document("homeapp");

        EventListener eventListener = new EventListener<DocumentSnapshot>(){

            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot.exists()){
                    String informativo = (String) documentSnapshot.getData().get("informativo");
                    String url1 = (String) documentSnapshot.getData().get("url_imagem1");
                    String url2 = (String) documentSnapshot.getData().get("url_imagem2");

                    configSliderImage(url1,url2,informativo);
                }
            }
        };

        reference.addSnapshotListener(eventListener);
    }


    private void iniciarOuvinteCategoria(){
        Query reference = firestore.collection("categorias").whereEqualTo("exibir_categoria", true);

        reference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                Categoria categoria;
                int index;
                for (DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                    switch (doc.getType()){
                        case ADDED:
                            categoria = doc.getDocument().toObject(Categoria.class);
                            categorias.add(categoria);
                            keys.add(categoria.getId());
                            adapterRecyclerViewCategoria.notifyDataSetChanged();
                            break;

                        case MODIFIED:
                            categoria = doc.getDocument().toObject(Categoria.class);
                            index = keys.indexOf(categoria.getId());
                            categorias.set(index, categoria);
                            adapterRecyclerViewCategoria.notifyDataSetChanged();
                            break;

                        case REMOVED:
                            categoria = doc.getDocument().toObject(Categoria.class);
                            index = keys.indexOf(categoria.getId());
                            categorias.remove(index);
                            keys.remove(index);
                            adapterRecyclerViewCategoria.notifyDataSetChanged();
                            break;
                    }
                }
            }
        });
    }

    private void atualizarCarrinho(){
        TextView textView = getView().findViewById(R.id.textView_home_quantidadeCarrinho);
        CardView cardView = getView().findViewById(R.id.cardView_carrinho);

        List<Produto> produtos = Carrinho.getInstance().getProdutosCarrinho();
        if (!produtos.isEmpty()){
            cardView.setVisibility(View.VISIBLE);
            int quantidade = produtos.size();
            textView.setText(quantidade+"");
        }else{
            cardView.setVisibility(View.GONE);
            textView.setText("0");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        atualizarCarrinho();

    }
}