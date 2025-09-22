package com.example.teste;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Carrinho extends AppCompatActivity {

    private TextView prod1, prod2, qtd1, qtd2, valorFinal;
    private ImageView home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrinho);

        prod1 = findViewById(R.id.subtotal1);
        prod2 = findViewById(R.id.subtotal2);
        qtd1 = findViewById(R.id.qtd1);
        qtd2 = findViewById(R.id.qtd2);
        valorFinal = findViewById(R.id.valorFinal);
        home = findViewById(R.id.homeCarrinho);

        Intent intent = getIntent();

        int qtdFrango = intent.getIntExtra(TelaInicial.CHAVE_QUANTIDADE_FRANGO, 0);
        int qtdBife = intent.getIntExtra(TelaInicial.CHAVE_QUANTIDADE_BIFE, 0);

        int precoFrango = qtdFrango * 25;
        int precoBife = qtdBife * 20;

        int total = precoFrango + precoBife;

        if(qtdFrango > 0){
            prod1.setText("Frango - R$ 25,00");
            qtd1.setText(String.valueOf(qtdFrango));
        }
        if(qtdBife > 0){
            prod2.setText("Bife - R$ 20,00");
            qtd2.setText(String.valueOf(qtdBife));
        }

        valorFinal.setText("R$ " + total + ",00");

        home.setOnClickListener(v -> {
            Intent irParaTelaInicial = new Intent(Carrinho.this, TelaInicial.class);
            startActivity(irParaTelaInicial);
        });

    }
}