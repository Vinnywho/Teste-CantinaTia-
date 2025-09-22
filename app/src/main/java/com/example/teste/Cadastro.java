package com.example.teste;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class Cadastro extends AppCompatActivity {

    private EditText editNome;
    private Button tenhoConta;

    private Button butao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro); // Layout da primeira tela

        editNome = findViewById(R.id.nome);
        tenhoConta = findViewById(R.id.tenhoConta);
        butao = findViewById(R.id.button);

        tenhoConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent irParaPrimeiraTela = new Intent(Cadastro.this, Login.class);
                startActivity(irParaPrimeiraTela);
            }
        });
        butao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomeDigitado = editNome.getText().toString();

                Intent irParaSegundaTela = new Intent(Cadastro.this, UltimaTela.class);
                irParaSegundaTela.putExtra("nomeUsuario", nomeDigitado);
                startActivity(irParaSegundaTela);
            }
        });
    }
}