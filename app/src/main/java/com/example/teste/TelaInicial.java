package com.example.teste;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TelaInicial extends AppCompatActivity {

    private TextView txtSaudacao1;
    private ImageView frangoAdd, bifeAdd, carrinho;
    public static final String CHAVE_QUANTIDADE_FRANGO = "quantidadeFrango";
    public static final String CHAVE_QUANTIDADE_BIFE = "quantidadeBife";

    private int quantidadeFrango = 0;
    private int quantidadeBife = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telainicial);

        frangoAdd = findViewById(R.id.frangoAdd);
        bifeAdd = findViewById(R.id.bifeAdd);
        carrinho = findViewById(R.id.carrinho);
        txtSaudacao1 = findViewById(R.id.txtSaudacao1);

        frangoAdd.setOnClickListener(v -> adicionarCarrinho("frango"));
        bifeAdd.setOnClickListener(v -> adicionarCarrinho("bife"));

        String nomeRecebido = getIntent().getStringExtra("nomeUsuario");
        // We need to handle the case where the name isn't passed.
        if (nomeRecebido != null && !nomeRecebido.isEmpty()) {
            txtSaudacao1.setText("Bem vindo(a), " + nomeRecebido + "!");
        } else {
            txtSaudacao1.setText("Bem vindo(a)!");
        }

        carrinho.setOnClickListener(v -> {
            Intent irParaCarrinho = new Intent(TelaInicial.this, Carrinho.class);

            irParaCarrinho.putExtra(CHAVE_QUANTIDADE_FRANGO, quantidadeFrango);
            irParaCarrinho.putExtra(CHAVE_QUANTIDADE_BIFE, quantidadeBife);
            startActivity(irParaCarrinho);
        });
    }

    public void adicionarCarrinho(String comida) {

        switch (comida) {
            case "frango":
                quantidadeFrango++;

                break;
            case "bife":
                quantidadeBife++;
                break;
        }
    }
}