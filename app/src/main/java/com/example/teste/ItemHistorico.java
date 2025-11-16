package com.example.teste;

import java.io.Serializable;

public class ItemHistorico implements Serializable {
    private int id; // ⬅️ NOVO: ID do pedido
    private long data;
    private double precoTotal;

    public ItemHistorico(int id, long data, double precoTotal) {
        this.id = id;
        this.data = data;
        this.precoTotal = precoTotal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
    }

    public double getPrecoTotal() {
        return precoTotal;
    }

    public void setPrecoTotal(double precoTotal) {
        this.precoTotal = precoTotal;
    }

}