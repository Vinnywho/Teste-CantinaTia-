package com.example.teste;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UltimaTela extends AppCompatActivity {

    private TextView txtSaudacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ultima); // Layout da segunda tela

        txtSaudacao = findViewById(R.id.txtSaudacao);

        // Pegando o nome enviado da MainActivity
        String nomeRecebido = getIntent().getStringExtra("nomeUsuario");

        txtSaudacao.setText("Olá, " + nomeRecebido + "! Vai a merda!");
    }
}