package com.rlsistemas.ranchosburgers.model;

public class Pedido {

    private String pedido_dados;
    private Long pedido_data;
    private String pedido_status;
    private String pedido_valor;
    private String pedido_id;


    public Pedido() {
    }

    public String getPedido_id() {
        return pedido_id;
    }

    public void setPedido_id(String pedido_id) {
        this.pedido_id = pedido_id;
    }

    public String getPedido_dados() {
        return pedido_dados;
    }

    public void setPedido_dados(String pedido_dados) {
        this.pedido_dados = pedido_dados;
    }

    public Long getPedido_data() {
        return pedido_data;
    }

    public void setPedido_data(Long pedido_data) {
        this.pedido_data = pedido_data;
    }

    public String getPedido_status() {
        return pedido_status;
    }

    public void setPedido_status(String pedido_status) {
        this.pedido_status = pedido_status;
    }

    public String getPedido_valor() {
        return pedido_valor;
    }

    public void setPedido_valor(String pedido_valor) {
        this.pedido_valor = pedido_valor;
    }
}
