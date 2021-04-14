package com.rlsistemas.ranchosburgers.model;

public class Categoria {

    private String id;
    private String nome;
    private String url_imagem;

    public Categoria() {
    }

    public Categoria(String id, String nome, String url_imagem) {
        this.id = id;
        this.nome = nome;
        this.url_imagem = url_imagem;
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

    public String getUrl_imagem() {
        return url_imagem;
    }

    public void setUrl_imagem(String url_imagem) {
        this.url_imagem = url_imagem;
    }
}
