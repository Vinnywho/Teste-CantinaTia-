package com.example.teste;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TelaInicial extends AppCompatActivity implements AlimentoAdapter.OnItemClickListener {

    private TextView txtSaudacao1;
    private ImageView carrinho;
    private RecyclerView recyclerView;
    private HashMap<String, Integer> carrinhoItens = new HashMap<>();

    public static final String CHAVE_QUANTIDADE_FRANGO = "quantidadeFrango";
    public static final String CHAVE_QUANTIDADE_BIFE = "quantidadeBife";

    // ATENÇÃO: Adicionei 'stock' ao SELECT
    private static final String SUPABASE_URL = "https://tganxelcsfitizoffvyn.supabase.co/rest/v1/products?select=name,price,image,quantity";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRnYW54ZWxjc2ZpdGl6b2ZmdnluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE4NTgzMTMsImV4cCI6MjA3NzQzNDMxM30.ObZQ__nbVlej-lPE7L0a6mtGj323gI1bRq4DD4SkTeM";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telainicial);

        carrinho = findViewById(R.id.carrinho);
        txtSaudacao1 = findViewById(R.id.txtSaudacao1);
        recyclerView = findViewById(R.id.rv_favoritos);

        String nomeRecebido = getIntent().getStringExtra("nomeUsuario");
        if (nomeRecebido != null && !nomeRecebido.isEmpty()) {
            txtSaudacao1.setText("Bem vindo(a), " + nomeRecebido + "!");
        } else {
            txtSaudacao1.setText("Bem vindo(a)!");
        }

        buscarProdutosSupabase();

        // Lógica do botão carrinho permanece inalterada
        carrinho.setOnClickListener(v -> {
            Intent irParaCarrinho = new Intent(TelaInicial.this, Carrinho.class);
            irParaCarrinho.putExtra(CHAVE_QUANTIDADE_FRANGO, carrinhoItens.getOrDefault("Filé de Frango", 0));
            irParaCarrinho.putExtra(CHAVE_QUANTIDADE_BIFE, carrinhoItens.getOrDefault("Bife", 0));
            startActivity(irParaCarrinho);
        });
    }

    // ----------------------------------------------------
    // LÓGICA DE BUSCA COM VOLLEY E SUPABASE
    // ----------------------------------------------------
    private void buscarProdutosSupabase() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                SUPABASE_URL,
                null,
                new com.android.volley.Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<Produto> listaDeAlimentos = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonProduto = response.getJSONObject(i);

                                String nome = jsonProduto.getString("name");
                                double precoAtual = jsonProduto.getDouble("price");
                                String emoji = jsonProduto.getString("image");
                                int estoque = jsonProduto.getInt("quantity"); // NOVO: Captura o estoque

                                // NOVO CONSTRUTOR: Passando o estoque
                                Produto produto = new Produto(nome, precoAtual, emoji, estoque);
                                listaDeAlimentos.add(produto);
                            }

                            configurarRecyclerView(listaDeAlimentos);

                        } catch (JSONException e) {
                            Toast.makeText(TelaInicial.this, "Erro: JSON mal formatado. O campo 'stock' foi encontrado?", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(com.android.volley.VolleyError error) {
                        Toast.makeText(TelaInicial.this, "Erro de rede ou Supabase.", Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_ANON_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(jsonArrayRequest);
    }

    // ----------------------------------------------------
    // LÓGICA DE CONFIGURAÇÃO DO RECYCLERVIEW
    // ----------------------------------------------------
    private void configurarRecyclerView(List<Produto> listaDeAlimentos) {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        // O AlimentoAdapter usará o novo estoque para habilitar/desabilitar o botão
        AlimentoAdapter adapter = new AlimentoAdapter(this, listaDeAlimentos, this);
        recyclerView.setAdapter(adapter);
    }

    // ----------------------------------------------------
    // IMPLEMENTAÇÃO DA INTERFACE DE CLIQUE (OnItemClickListener)
    // ----------------------------------------------------
    @Override
    public void onAdicionarClick(Produto produto) {
        String nomeProduto = produto.getNome();
        int quantidadeEmCarrinho = carrinhoItens.getOrDefault(nomeProduto, 0);

        // Verifica se ainda há estoque antes de adicionar
        if (produto.getEstoque() > quantidadeEmCarrinho) {
            carrinhoItens.put(nomeProduto, quantidadeEmCarrinho + 1);
            Toast.makeText(this, nomeProduto + " adicionado! Total em carrinho: " + (quantidadeEmCarrinho + 1), Toast.LENGTH_SHORT).show();

            // NOTE: Para refletir a mudança, você precisaria notificar o Adapter,
            // mas isso requer uma lógica de estado mais complexa (como LiveData ou ViewModel)

        } else {
            Toast.makeText(this, "Estoque esgotado para " + nomeProduto + "!", Toast.LENGTH_SHORT).show();
        }
    }
}