package com.rlsistemas.ranchosburgers.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.rlsistemas.ranchosburgers.R;
import com.rlsistemas.ranchosburgers.util.DialogProgress;
import com.rlsistemas.ranchosburgers.util.Util;

public class CadastrarActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editText_email;
    private EditText editText_senha;
    private EditText editText_senhaConfirmar;
    private Button button_cadastrar;

    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_cadastrar);

        configToolbar();

        editText_email = findViewById(R.id.editText_cadastrar_email);
        editText_senha = findViewById(R.id.editText_cadastrar_senha);
        editText_senhaConfirmar = findViewById(R.id.editText_cadastrar_senhaConfirmar);
        button_cadastrar = findViewById(R.id.button_cadastrar);

        button_cadastrar.setOnClickListener(this);
        auth = FirebaseAuth.getInstance();

    }

    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView textView = findViewById(R.id.textView_toolbar);
        textView.setText("Cadastrar");
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
           // startActivity(new Intent(this, LoginActivity.class));
        }
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_cadastrar:
                buttonCadastrar();
                break;
        }
    }

    private void buttonCadastrar(){

        String email = editText_email.getText().toString().trim();
        String senha = editText_senha.getText().toString().trim();
        String senhaConfirmar = editText_senhaConfirmar.getText().toString().trim();
        
        if (!senha.equals(senhaConfirmar)){
            Toast.makeText(getBaseContext(), "As senhas são diferentes", Toast.LENGTH_SHORT).show();
        }
        else if (email.isEmpty() || senha.isEmpty() || senhaConfirmar.isEmpty()){
            Toast.makeText(getBaseContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
        }
        else{
           if (Util.statusInternet_MoWi(getBaseContext())){
               criarContaFirebase(email,senha);
           }else{
               Toast.makeText(getBaseContext(), (R.string.sem_conexao), Toast.LENGTH_SHORT).show();
           }
        }
    }

    private void criarContaFirebase(String email, String senha){
        DialogProgress dialogProgress = new DialogProgress();
        dialogProgress.show(getSupportFragmentManager(),"");

        auth.createUserWithEmailAndPassword(email,senha).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                dialogProgress.dismiss();
                if (task.isSuccessful()){
                    dialogoOpcao();
                }else{
                    String errorFirebase = task.getException().toString();
                    String error = Util.errorFirebase(errorFirebase);
                    Toast.makeText(getBaseContext(), error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void dialogoOpcao(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Cadastro efetuado com sucesso\n\nEscolha uma opção")
                .setCancelable(false)
                .setPositiveButton("Retirar pedido no local", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        startActivity(new Intent(getBaseContext(), RetirarLocalActivity.class));
                    }
                }).setNegativeButton("Receber pedido em casa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        startActivity(new Intent(getBaseContext(), PedidoReceberEmCasaActivity.class));

                    }
                }).setNeutralButton("Voltar para o carrinho", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create();
        dialog.show();
    }

}