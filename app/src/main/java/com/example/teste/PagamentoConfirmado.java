package com.example.teste;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PagamentoConfirmado extends AppCompatActivity {

    private TextView txtNumeroPedidoFinal;
    private Button btnVoltar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagamento_confirmado);

        // Certifique-se de que o ID do botão no XML (activity_pagamento_confirmado.xml) é 'btnVoltar'
        txtNumeroPedidoFinal = findViewById(R.id.txtNumeroPedidoFinal);
        btnVoltar = findViewById(R.id.btnVoltar);

        // Recebe o ID do pedido que foi confirmado
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String pedidoIdStr = extras.getString("pedidoId");
            if (pedidoIdStr != null) {
                txtNumeroPedidoFinal.setText("Pedido Nº: #" + pedidoIdStr);
            }
        }

        // Ação do botão para voltar (fecha a activity)
        btnVoltar.setOnClickListener(v -> {

            Intent homeIntent = new Intent(PagamentoConfirmado.this, TelaInicial.class);

            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        });
    }
}