package com.example.teste;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    private Button btnEnviar;
    private Button buto;
    private EditText nome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnEnviar = findViewById(R.id.btnEnviar);
        buto = findViewById(R.id.buto);
        nome = findViewById(R.id.nome);

        btnEnviar.setOnClickListener(v -> {
            Intent irParaCadastro = new Intent(Login.this, Cadastro.class);
            startActivity(irParaCadastro);
        });

        buto.setOnClickListener(v -> {
            String nomeDigitado = nome.getText().toString().trim();

            if (nomeDigitado.isEmpty()) {
                nome.setError("Digite seu nome");
                nome.requestFocus();
            } else {
                Intent irParaTelaInicial = new Intent(Login.this, TelaInicial.class);
                irParaTelaInicial.putExtra("nomeUsuario", nomeDigitado);
                startActivity(irParaTelaInicial);
            }
        });
    }
}
