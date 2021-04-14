package com.rlsistemas.ranchosburgers.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Produto implements Parcelable {

    private String id;
    private String nome;
    private String descricao;
    private String valor;
    private String url_imagem;
    private String categoria_id;
    private boolean possui_adicional;

    //adicionando ao carrinho
    private String adicional;
    private String observacao;


    public Produto() {
    }

    public Produto(String nome, String descricao, String valor, String adicional, String observacao) {
        this.nome = nome;
        this.descricao = descricao;
        this.valor = valor;
        this.adicional = adicional;
        this.observacao = observacao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getUrl_imagem() {
        return url_imagem;
    }

    public void setUrl_imagem(String url_imagem) {
        this.url_imagem = url_imagem;
    }

    public String getCategoria_id() {
        return categoria_id;
    }

    public void setCategoria_id(String categoria_id) {
        this.categoria_id = categoria_id;
    }

    public boolean isPossui_adicional() {
        return possui_adicional;
    }

    public void setPossui_adicional(boolean possui_adicional) {
        this.possui_adicional = possui_adicional;
    }

    public String getAdicional() {
        return adicional;
    }

    public void setAdicional(String adicional) {
        this.adicional = adicional;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }




    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.nome);
        dest.writeString(this.descricao);
        dest.writeString(this.valor);
        dest.writeString(this.url_imagem);
        dest.writeString(this.categoria_id);
        dest.writeByte(this.possui_adicional ? (byte) 1 : (byte) 0);
        dest.writeString(this.adicional);
        dest.writeString(this.observacao);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readString();
        this.nome = source.readString();
        this.descricao = source.readString();
        this.valor = source.readString();
        this.url_imagem = source.readString();
        this.categoria_id = source.readString();
        this.possui_adicional = source.readByte() != 0;
        this.adicional = source.readString();
        this.observacao = source.readString();
    }

    protected Produto(Parcel in) {
        this.id = in.readString();
        this.nome = in.readString();
        this.descricao = in.readString();
        this.valor = in.readString();
        this.url_imagem = in.readString();
        this.categoria_id = in.readString();
        this.possui_adicional = in.readByte() != 0;
        this.adicional = in.readString();
        this.observacao = in.readString();
    }

    public static final Creator<Produto> CREATOR = new Creator<Produto>() {
        @Override
        public Produto createFromParcel(Parcel source) {
            return new Produto(source);
        }

        @Override
        public Produto[] newArray(int size) {
            return new Produto[size];
        }
    };
}
