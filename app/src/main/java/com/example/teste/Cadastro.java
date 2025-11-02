package com.example.teste;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Cadastro extends AppCompatActivity {

    private Button tenhoConta;

    private Button butao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

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

                Intent irParaSegundaTela = new Intent(Cadastro.this, Login.class);
                Toast.makeText(Cadastro.this, "Usuário adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                startActivity(irParaSegundaTela);
            }
        });
    }
}