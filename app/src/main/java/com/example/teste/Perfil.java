package com.example.teste;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Perfil extends AppCompatActivity {
    // declarar variáveis
    private ListView listaConfig;
    private ImageView home, fotoPerfil, carrinho, perfilPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        // vincular variáveis
        listaConfig = findViewById(R.id.listaConfig);
        fotoPerfil = findViewById(R.id.fotoPerfil);
        home = findViewById(R.id.homePerfil);
        carrinho = findViewById(R.id.carrinhoPerfil);
        perfilPerfil = findViewById(R.id.perfilPerfil);

        // adicionar itens à lista
        ArrayList<String> lista = new ArrayList<>();
        lista.add("Editar perfil");
        lista.add("Formas de pagamento");
        lista.add("Histórico de compras");
        lista.add("Fidelidade");
        lista.add("Alterar senha");
        lista.add("Notificações");
        lista.add("Ajuda");

        // configurar adapter para a lista e exibir a lista na tela do usuário
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista);

        listaConfig.setAdapter(adapter);
        fotoPerfil.setImageResource(R.drawable.perfil);

        Intent intent = getIntent();

        // ação botão "home"
        home.setOnClickListener(v -> {
            Intent irParaTelaInicial = new Intent(Perfil.this, TelaInicial.class);
            startActivity(irParaTelaInicial);
        });

        // ação botão "carrinho"
        carrinho.setOnClickListener(v -> {
            Intent irParaCarrinho = new Intent(Perfil.this, Carrinho.class);
            startActivity(irParaCarrinho);
        });

        // ação botão "perfil"
        perfilPerfil.setOnClickListener(v -> {
            Toast.makeText(Perfil.this, "Você já está no seu perfil!", Toast.LENGTH_SHORT).show();
        });
    }
}