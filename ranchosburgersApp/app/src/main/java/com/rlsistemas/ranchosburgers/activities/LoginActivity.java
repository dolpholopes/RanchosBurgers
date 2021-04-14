package com.rlsistemas.ranchosburgers.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.rlsistemas.ranchosburgers.R;
import com.rlsistemas.ranchosburgers.util.DialogProgress;
import com.rlsistemas.ranchosburgers.util.Util;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editText_email;
    private EditText editText_senha;
    private Button button_login;
    private TextView textView_cadastrar;
    private TextView textView_recuperarSenha;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_login);

        recuperarView();

        auth = FirebaseAuth.getInstance();

    }


    private void recuperarView(){
        editText_email = findViewById(R.id.editText_email);
        editText_senha = findViewById(R.id.editText_senha);
        button_login = findViewById(R.id.button_login);
        textView_cadastrar = findViewById(R.id.textView_login_cadastrar);
        textView_recuperarSenha = findViewById(R.id.textView_login_recuperarSenha);

        button_login.setOnClickListener(this);
        textView_cadastrar.setOnClickListener(this);
        textView_recuperarSenha.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_login:

                buttonLogin();

                break;

            case R.id.textView_login_cadastrar:
                finish();
               startActivity(new Intent(getBaseContext(), CadastrarActivity.class));

                break;

            case R.id.textView_login_recuperarSenha:

                buttonRecuperarSenha();

                break;
        }
    }

    private void buttonLogin(){
        String email = editText_email.getText().toString().trim();
        String senha =  editText_senha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()){
            Toast.makeText(getBaseContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
        }else{
            if (Util.statusInternet_MoWi(getBaseContext())){

                loginFirebase(email, senha);

            }else{
                Toast.makeText(getBaseContext(), (R.string.sem_conexao), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loginFirebase(String email, String senha){

        DialogProgress dialogProgress = new DialogProgress();
        dialogProgress.show(getSupportFragmentManager(),"");

        auth.signInWithEmailAndPassword(email,senha).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
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
                .setMessage("Login efetuado com sucesso\n\nEscolha uma opção")
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

                    }
                }).setNeutralButton("Voltar para o carrinho", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create();

        dialog.show();
    }

    private void buttonRecuperarSenha(){

        String email = editText_email.getText().toString().trim();

        if (email.isEmpty()){
            Toast.makeText(getBaseContext(), "Insira seu email para poder recuperar sua senha", Toast.LENGTH_SHORT).show();
        }else{
            if (Util.statusInternet_MoWi(getBaseContext())){
                recuperarSenhaFirebase(email);
            }else{

            }
        }
    }

    private void recuperarSenhaFirebase(String email){
        DialogProgress dialogProgress = new DialogProgress();
        dialogProgress.show(getSupportFragmentManager(),"");

        auth.sendPasswordResetEmail(email).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialogProgress.dismiss();
                if (task.isSuccessful()){
                    Toast.makeText(getBaseContext(), "Enviamos um link de redefinição no sei email", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getBaseContext(), "Erro ao enviar email ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}