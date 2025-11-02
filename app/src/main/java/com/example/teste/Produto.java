package com.example.teste;

//objeto do produto para construir a lista de alimentos
public class Produto {
    private String nome;
    private double precoAtual;
    private String emoji;
    private int estoque;

    public Produto(String nome, double precoAtual, String emoji, int estoque) {
        this.nome = nome;
        this.precoAtual = precoAtual;
        this.emoji = emoji;
        this.estoque = estoque;
    }

    public String getNome() { return nome; }
    public double getPrecoAtual() { return precoAtual; }
    public String getEmoji() { return emoji; }
    public int getEstoque() { return estoque; }
}