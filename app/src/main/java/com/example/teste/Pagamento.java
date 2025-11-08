package com.example.teste;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class Pagamento extends AppCompatActivity {
    private TextView valorTotal;
    private RadioGroup rgPagamento;
    private Button btnConfirmar, cancelar;

    private String metodoPagamentoSelecionado = "Não selecionado";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagamento);

        valorTotal = findViewById(R.id.valorTotal);
        rgPagamento = findViewById(R.id.rgPagamento);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        cancelar = findViewById(R.id.cancelar);

        cancelar.setOnClickListener(v -> {
            finish();
        });

        String valor = getIntent().getStringExtra("total");
        if (valor != null) {
            double valorDouble = Double.parseDouble(valor);
            valorTotal.setText("Total " + String.format(Locale.getDefault(), "R$ %.2f", valorDouble));
        } else {
            valorTotal.setText("Total R$0.00");
        }

        rgPagamento.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1) {
                    RadioButton selectedRadioButton = findViewById(checkedId);
                    metodoPagamentoSelecionado = selectedRadioButton.getText().toString();
                    Log.d("PAGAMENTO", "Método selecionado: " + metodoPagamentoSelecionado);
                } else {
                    metodoPagamentoSelecionado = "Não selecionado";
                }
            }
        });

        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rgPagamento.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(Pagamento.this, "Por favor, selecione uma forma de pagamento.", Toast.LENGTH_SHORT).show();
                } else {
                    String mensagem = "Pagamento de " + valorTotal.getText().toString() +
                            " confirmado via " + metodoPagamentoSelecionado + ".";
                    Toast.makeText(Pagamento.this, mensagem, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}