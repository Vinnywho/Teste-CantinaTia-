
package com.example.teste;

import java.io.Serializable;

public class ItemCarrinho implements Serializable {
    private final Produto produto;
    private int quantidade;
    private final double precoUnitario;

    public ItemCarrinho(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = produto.getPrecoAtual();
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public double getPrecoTotal() {
        return precoUnitario * quantidade;
    }
}
