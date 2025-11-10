package com.example.teste;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class PedidoConfirmado extends AppCompatActivity {
    Button btnConfirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido_confirmado);

        btnConfirmar = findViewById(R.id.pedidoConfirmado);
        // ação botão "confirmar"
        btnConfirmar.setOnClickListener(v -> {
            finish();
        });
    }
}
