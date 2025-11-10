package com.example.teste;

import java.io.Serializable;

public class Produto implements Serializable {
    private int id;
    private String nome;
    private double preco;
    private String emoji;
    private int estoque;

    // construtor
    public Produto(int id, String nome, double preco, String emoji, int estoque) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.emoji = emoji;
        this.estoque = estoque;
    }

    // construtor sem ID
    public Produto(String nome, double preco, String emoji, int estoque) {
        this.nome = nome;
        this.preco = preco;
        this.emoji = emoji;
        this.estoque = estoque;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public double getPrecoAtual() { return preco; }
    public String getEmoji() { return emoji; }
    public int getEstoque() { return estoque; }
}