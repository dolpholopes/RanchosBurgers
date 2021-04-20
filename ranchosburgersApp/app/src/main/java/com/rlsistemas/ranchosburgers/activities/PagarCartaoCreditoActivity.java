package com.rlsistemas.ranchosburgers.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.model.Payment;
import com.rlsistemas.ranchosburgers.R;
import com.rlsistemas.ranchosburgers.interface_mp.ComunicacaoServidorMP;
import com.rlsistemas.ranchosburgers.model.Produto;
import com.rlsistemas.ranchosburgers.singleton.Carrinho;
import com.rlsistemas.ranchosburgers.util.DialogProgress;
import com.rlsistemas.ranchosburgers.util.PdfCreator;
import com.rlsistemas.ranchosburgers.util.Util;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class PagarCartaoCreditoActivity extends AppCompatActivity {

    private String nome;
    private String contato;
    private String endereco;

    private String taxaEntrega = "2";

    private FirebaseFirestore firestore;

    private String acessToken;
    private String publicKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagar_cartao_credito);

        firestore = FirebaseFirestore.getInstance();

        nome = getIntent().getStringExtra("nome");
        contato = getIntent().getStringExtra("contato");
        endereco = getIntent().getStringExtra("endereco");

        obterCredenciais();
    }


    private void obterCredenciais(){
        DialogProgress dialogProgress = new DialogProgress();
        dialogProgress.show(getSupportFragmentManager(),"0");

        firestore.collection("mp").document("credenciais").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                dialogProgress.dismiss();
                if (documentSnapshot.exists()){
                    acessToken = (String) documentSnapshot.getData().get("acessToken");
                    publicKey = (String) documentSnapshot.getData().get("publicKey");
                    criarJsonObject();
                }else{
                    Toast.makeText(getBaseContext(), "Erro de comunicação com o servidor", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialogProgress.dismiss();
                Toast.makeText(getBaseContext(), "Erro de comunicação com o servidor", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }


    private void criarJsonObject(){

        JsonObject dados = new JsonObject();

        //--------------------------------------------------------------------PRIMEIRO ITEM
        JsonArray itemsList = new JsonArray();
        JsonObject item = new JsonObject();

        List<Produto> produtos = Carrinho.getInstance().getProdutosCarrinho();

        for(Produto produto: produtos){

            item = new JsonObject();

            item.addProperty("title",produto.getNome());
            item.addProperty("description",produto.getDescricao());
            item.addProperty("quantity",1);
            item.addProperty("currency_id","BRL");
            item.addProperty("unit_price",Double.parseDouble(produto.getValor()));

            itemsList.add(item);
        }
        dados.add("items",itemsList);

        //--------------------------------------------------------------------SEGUNDO ITEM
        JsonObject email = new JsonObject();
        String emailUsuario = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        email.addProperty("email",emailUsuario);
        dados.add("payer",email);

        //--------------------------------------------------------------------TERCEIRO ITEM
        JsonObject excluded_payments_types = new JsonObject();
        JsonArray ids = new JsonArray();
        JsonObject removerBoleto = new JsonObject();
        JsonObject removerCartao = new JsonObject();
        JsonObject removerGift = new JsonObject();

        removerBoleto.addProperty("id","ticket");
        removerGift.addProperty("id","digital_currency");
        removerCartao.addProperty("id","debit_card");

        ids.add(removerBoleto);
        ids.add(removerCartao);
        ids.add(removerGift);

        excluded_payments_types.add("excluded_payment_types",ids);
        excluded_payments_types.addProperty("installments",1);

        dados.add("payment_methods",excluded_payments_types);

        //--------------------------------------------------------------------QUARTO ITEM
        JsonArray taxaEntrega = new JsonArray();
        JsonObject taxa = new JsonObject();

        taxa = new JsonObject();
        taxa.addProperty("cost", 2);

        taxaEntrega.add(taxa);

        dados.add("shipments", taxaEntrega);

        

        criarPreference(dados);
    }

    private void criarPreference(JsonObject dados){

        String site = "https://api.mercadopago.com";

        String url = "/checkout/preferences?access_token="+acessToken;

        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(site)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ComunicacaoServidorMP paymentConnection = retrofit.create(ComunicacaoServidorMP.class);

        Call<JsonObject> request = paymentConnection.sendPayment(url, dados);

        request.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                String preferenceId = response.body().get("id").getAsString();
                criarPagamento(preferenceId);
                // resposta mp caso sucesso
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // caso aconteca erro
            }
        });

    }

    private void criarPagamento(String preferenceId){
        final AdvancedConfiguration advancedConfiguration =
                new AdvancedConfiguration.Builder().setBankDealsEnabled(false).build();

        new MercadoPagoCheckout
                .Builder(publicKey, preferenceId)
                .setAdvancedConfiguration(advancedConfiguration).build()
                .startPayment(this, 1245);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1245) {  //RESPOSTA DO MERCADO PAGO
            if (resultCode == MercadoPagoCheckout.PAYMENT_RESULT_CODE) {
                final Payment payment = (Payment) data.getSerializableExtra(MercadoPagoCheckout.EXTRA_PAYMENT_RESULT);
                respostaMercadoPago(payment);
                //Done!
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }else if (requestCode == 1234){ // RESPOSTA DE QUANDO VOLTAMOS PARA O APP DEPOIS QUE VISUALIZAMOS O PDF
            voltarActivityPrincipal();
        }
    }

    private void respostaMercadoPago(Payment payment) {

        String status = payment.getPaymentStatus();
        String statusDetlahe = payment.getPaymentStatusDetail();

        if (status.equalsIgnoreCase("approved")) { // SE PAGAMENTO FOR APROVADO, FAZEMOS PROCESSO PARA SALVAR PEDIDO NO FIREBASE
            String forma_pagamento = "Pagamento efetuado e aprovado por Cartão";
            obterToken(nome,contato,endereco,forma_pagamento,payment);
        } else if (status.equalsIgnoreCase("rejected")) {
            if(statusDetlahe.equalsIgnoreCase("cc_rejected_bad_filled_card_number")){
                exibirDialog("Erro ","Revise o número do cartão.");
            }
            else if(statusDetlahe.equalsIgnoreCase("cc_rejected_bad_filled_date")) {
                exibirDialog("Erro ","Revise a data de validade.");
            }
            else if(statusDetlahe.equalsIgnoreCase("cc_rejected_bad_filled_other")) {
                exibirDialog("Erro ","Confira se os dados estão corretos.");
            }
            else if(statusDetlahe.equalsIgnoreCase("cc_rejected_bad_filled_security_code")) {
                exibirDialog("Erro ","Revise o código de segurança.");
            }
            else if(statusDetlahe.equalsIgnoreCase("cc_rejected_blacklist")) {
                exibirDialog("Erro ","Não foi possivel processar seu pagamento.");
            }
            else if(statusDetlahe.equalsIgnoreCase("cc_rejected_call_for_authorize")) {
                exibirDialog("Erro ","Você deve autorizar o pagamento do valor.");
            }
            else if(statusDetlahe.equalsIgnoreCase("\tcc_rejected_card_disabled")) {
                exibirDialog("Erro ","Você precisa autorizar seu cartão.");
            }
            else if(statusDetlahe.equalsIgnoreCase("cc_rejected_card_error")) {
                exibirDialog("Erro ","Não conseguimos processar seu pagamento.");
            }
            else if(statusDetlahe.equalsIgnoreCase("cc_rejected_insufficient_amount")) {
                exibirDialog("Erro ","Saldo insuficiente.");
            }
            else if(statusDetlahe.equalsIgnoreCase("cc_rejected_max_attempts")) {
                exibirDialog("Erro ","Escolha outro cartão ou outra forma de pagamento.");
            }
            else{
                exibirDialog("Erro ","Erro ao fazer pagamento");
            }
        }
    }

    private void exibirDialog(String titulo, String msg){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setCancelable(false)
                .setMessage(msg)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create();
        dialog.show();
    }


    private void obterToken(String nome, String contato, String endereco, String forma_pagamento, Payment payment){
        DialogProgress dialogProgress = new DialogProgress();
        dialogProgress.show(getSupportFragmentManager(),"2");

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(
                new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        dialogProgress.dismiss();
                        if (task.isSuccessful()){
                            String token = task.getResult().getToken();
                            salvarPedidoFirebase(nome, contato, endereco, forma_pagamento, token, payment);
                        }else{
                            String erro = "Sem token";
                            salvarPedidoFirebase(nome, contato, endereco, forma_pagamento, erro, payment);
                        }
                    }
                });
    }

    private void salvarPedidoFirebase(String nome,String  contato,String  endereco,String forma_pagamento,String  token, Payment payment){
        DialogProgress dialogProgress = new DialogProgress();
        dialogProgress.show(getSupportFragmentManager(),"3");

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String idPedido = firestore.collection("pedidos").document().getId();
        String totalValorProdutos = valorTotalProdutos();

        long data = Timestamp.now().toDate().getTime();

        String todosProdutos = "";

        for (Produto produto: Carrinho.getInstance().getProdutosCarrinho()){
            todosProdutos = todosProdutos + produto.getNome() + " <br> " +
                    produto.getAdicional() + " <br> " +
                    produto.getObservacao() + " <br><br> ";
        }

        HashMap<String, Object> dadosPedido = new HashMap<>();
        dadosPedido.put("cliente_contato",contato);
        dadosPedido.put("cliente_endereco",endereco);
        dadosPedido.put("cliente_nome",nome);
        dadosPedido.put("cliente_uid",uid);

        dadosPedido.put("pedido_dados",todosProdutos);
        dadosPedido.put("pedido_data", data);
        dadosPedido.put("pedido_forma_pagamento",forma_pagamento);
        dadosPedido.put("pedido_id",idPedido);
        dadosPedido.put("pedido_status","em andamento");
        dadosPedido.put("pedido_valor",totalValorProdutos);
        dadosPedido.put("token_msg",token);

        DocumentReference reference = firestore.collection("pedidos").document(idPedido);
        String finalTotalProdutos = todosProdutos;
        reference.set(dadosPedido).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialogProgress.dismiss();
                if (task.isSuccessful()){
                    dialogExibirPdf(idPedido,nome,data, finalTotalProdutos,totalValorProdutos, payment);
                }else{
                    Toast.makeText(getBaseContext(), "Erro ao salvar o pedido", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void dialogExibirPdf(String idPedido,String nome, long data, String produtos, String totalValorProdutos, Payment payment){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Pedido efetuado com Sucesso")
                .setCancelable(false)
                .setMessage("\nGostaria de Gerar o comprovante do Pedido ?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override

                    public void onClick(DialogInterface dialog, int which) {

                        try {
                            gerarPdf(idPedido,nome,data, produtos,totalValorProdutos, payment);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton("Não", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        voltarActivityPrincipal();
                    }
                }).create();
        dialog.show();
    }


    private void gerarPdf(String idPedido,String nome, long data, String produtos, String totalValorProdutos, Payment payment) throws IOException, DocumentException {

        ParcelFileDescriptor descriptor = null;
        OutputStream outputStream;
        File pdf = null;
        Uri uri = null;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){

            ContentValues contentValues = new ContentValues();

            contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"application/pdf");
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,"Pedido"+idPedido);
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS+"/Recibo/");

            ContentResolver resolver = getContentResolver();
            uri = resolver.insert(MediaStore.Downloads.getContentUri("external"),contentValues);
            descriptor = resolver.openFileDescriptor(uri,"rw");
            outputStream = new FileOutputStream(descriptor.getFileDescriptor());

        }else{

            File diretorioRaiz = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File diretorio = new File(diretorioRaiz.getPath()+"/Recibo/");

            if(  !diretorio.exists()){
                diretorio.mkdir();
            }

            String nomeArquivo = diretorio.getPath()+"/Pedido"+idPedido+".pdf";
            pdf = new File(nomeArquivo);
            outputStream = new FileOutputStream(pdf);
        }

        Rectangle rectangle = new Rectangle(250,800);
        Document document = new Document(rectangle,5, 5, 5, 5);

        PdfCreator pdfCreator = new PdfCreator();

        PdfWriter pdfWriter = PdfWriter.getInstance(document,outputStream);

        pdfWriter.setBoxSize("box",new Rectangle(0,0,0,0));
        pdfWriter.setPageEvent(pdfCreator);

        document.open();

        String idPagamento = "ID Pagamento: " + payment.getId();
        String dataPagamento = "Data Pagamento: " + payment.getDateApproved();
        String valorPagamento = "Valor Pagamento:" + payment.getTransactionAmount().toString();

        String id = "ID Pedido:" + idPedido;
        String nomeCliente = "\nNome: \n"+nome;
        String dataPedido = "\nData e Hora: \n"+  Util.dataPedido(data);
        String pedido = "\nPedido: \n"+ produtos.replaceAll("<br>","\n");
        //String taxaEntrega = "\nTaxa de Entrega: R$ 2,00";
        String valorPedido = "\nValor Total: "+totalValorProdutos;

        Font font = new Font(Font.FontFamily.TIMES_ROMAN,10, Font.NORMAL);
        Font fontTitulo = new Font(Font.FontFamily.TIMES_ROMAN,14, Font.BOLD);

        Image image = null;

        Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.logo_com_fundo);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
        image = Image.getInstance(bytes.toByteArray());
        image.scaleAbsolute(70f,50f);
        image.setAlignment(Element.ALIGN_RIGHT);

        document.add(image);

        Paragraph paragraph = new Paragraph("Rancho's Burgers",fontTitulo);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);


        paragraph = new Paragraph("__________________",fontTitulo);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);

        paragraph = new Paragraph("Dados Pagamento", font);
        document.add(paragraph);

        paragraph = new Paragraph(idPagamento,font);
        document.add(paragraph);

        paragraph = new Paragraph(dataPagamento,font);
        document.add(paragraph);

        paragraph = new Paragraph(valorPagamento,font);
        document.add(paragraph);


        paragraph = new Paragraph("__________________",fontTitulo);
        document.add(paragraph);

        paragraph = new Paragraph("\n\nDados Pedido",font);
        document.add(paragraph);

        paragraph = new Paragraph(id,font);
        document.add(paragraph);

        paragraph = new Paragraph(nomeCliente,font);
        document.add(paragraph);

        paragraph = new Paragraph(dataPedido,font);
        document.add(paragraph);

        paragraph = new Paragraph(pedido,font);
        document.add(paragraph);


        paragraph = new Paragraph("__________________",fontTitulo);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);

        //paragraph = new Paragraph(taxaEntrega,font);
        //document.add(paragraph);

        paragraph = new Paragraph(valorPedido,font);
        document.add(paragraph);

        document.close();
        outputStream.close();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            descriptor.close();
            visualizarPdf(pdf, uri);
        }else{
            visualizarPdf(pdf,uri);
        }
    }


    private void visualizarPdf(File pdf, Uri uri){

        PackageManager packageManager = getPackageManager();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("application/pdf");

        List<ResolveInfo> lista = packageManager.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);

        if(lista.size() > 0){

            Intent intent1 = new Intent(Intent.ACTION_VIEW);
            intent1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if(pdf == null){ // versao mais nova android
                intent1.setDataAndType(uri,"application/pdf");
            }else{ // versao mais antiga android
                Uri uri1 = FileProvider.getUriForFile(getBaseContext(),"com.rlsistemas.ranchosburgers",pdf);
                intent1.setDataAndType(uri1,"application/pdf");
            }

            startActivityForResult(intent1,1234);

        }else{
            erroAbrirPdf();
        }
    }

    private void erroAbrirPdf(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Erro ao Abrir PDF")
                .setCancelable(false)
                .setMessage("\nNão foi detectado um leitor PDF no seu Dispositivo. Caso queira visualizar a sua nota do Pedido Futuramente acesse a pasta de downlaods do seu Dispositivo.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        voltarActivityPrincipal();
                    }
                }).create();

        dialog.show();
    }

    private void voltarActivityPrincipal(){

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Acompanhe seu pedido")
                .setCancelable(false)
                .setMessage("\nClique no Menu do canto esquerdo da tela principal e acompanhe o Status do seu Pedido")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Carrinho.getInstance().getProdutosCarrinho().clear();
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }
                }).create();
        dialog.show();
    }

    private String valorTotalProdutos(){

        int taxaEntrega = 0;

        double valorTotal = taxaEntrega;
        for (Produto produto: Carrinho.getInstance().getProdutosCarrinho()){
            double valor = Double.valueOf(produto.getValor());
            valorTotal = valorTotal + valor;
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String valorTotalString = decimalFormat.format(valorTotal);
        return valorTotalString;
    }


}