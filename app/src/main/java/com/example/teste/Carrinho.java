package com.example.teste;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import java.util.Locale;
import java.util.Map;

// A classe implementa a interface do Adapter para lidar com cliques de remoção
public class Carrinho extends AppCompatActivity implements CarrinhoAdapter.OnItemRemoveListener {

    private TextView valorFinal, txtCarrinhoVazio;
    private ImageView home, perfil, carrinho;
    private RecyclerView rvCarrinho;
    private CarrinhoAdapter adapter;

    private List<ItemCarrinho> listaItensCarrinho = new ArrayList<>();
    // Este HashMap será modificado (itens removidos) e devolvido para a TelaInicial
    private HashMap<String, Integer> carrinhoItensMap;

    private static final String SUPABASE_URL = "https://tganxelcsfitizoffvyn.supabase.co/rest/v1/products?select=name,price,image,quantity";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRnYW54ZWxjc2ZpdGl6b2ZmdnluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE4NTgzMTMsImV4cCI6MjA3NzQzNDMxM30.ObZQ__nbVlej-lPE7L0a6mtGj323gI1bRq4DD4SkTeM";



    @Override
    public void onBackPressed() {
        Intent intentDeRetorno = new Intent();

        intentDeRetorno.putExtra("carrinhoAtualizado", carrinhoItensMap);

        setResult(RESULT_OK, intentDeRetorno);

        super.onBackPressed();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrinho);

        valorFinal = findViewById(R.id.valorFinal);
        home = findViewById(R.id.homeCarrinho);
        perfil = findViewById(R.id.perfilCarrinho);
        carrinho = findViewById(R.id.carrinhoCarrinho);
        rvCarrinho = findViewById(R.id.rv_carrinho);
        txtCarrinhoVazio = findViewById(R.id.txtCarrinhoVazio);

        configurarRecyclerView();

        Intent intent = getIntent();
        carrinhoItensMap = (HashMap<String, Integer>) intent.getSerializableExtra("carrinhoItens");

        if (carrinhoItensMap != null && !carrinhoItensMap.isEmpty()) {
            txtCarrinhoVazio.setVisibility(View.GONE);
            rvCarrinho.setVisibility(View.VISIBLE);
            buscarDetalhesDosProdutos(carrinhoItensMap);
        } else {
            exibirCarrinhoVazio();
        }

        home.setOnClickListener(v -> {
            onBackPressed();
        });

        perfil.setOnClickListener(v -> {
            Intent irParaPerfil = new Intent(Carrinho.this, Perfil.class);
            startActivity(irParaPerfil);
        });

        carrinho.setOnClickListener(v -> {
            Toast.makeText(Carrinho.this, "Você já está no carrinho!", Toast.LENGTH_SHORT).show();
        });
    }

    private void configurarRecyclerView() {
        rvCarrinho.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CarrinhoAdapter(listaItensCarrinho, this);
        rvCarrinho.setAdapter(adapter);
    }


    @Override
    public void onItemRemove(int position) {
        ItemCarrinho itemRemovido = listaItensCarrinho.remove(position);

        if (carrinhoItensMap != null) {
            carrinhoItensMap.remove(itemRemovido.getProduto().getNome());
        }

        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, listaItensCarrinho.size());

        Toast.makeText(this, itemRemovido.getProduto().getNome() + " removido.", Toast.LENGTH_SHORT).show();

        recalcularTotal();

        if (listaItensCarrinho.isEmpty()) {
            exibirCarrinhoVazio();
        }
    }

    private void recalcularTotal() {
        double totalCompra = 0.0;
        for (ItemCarrinho item : listaItensCarrinho) {
            totalCompra += item.getPrecoTotal();
        }
        valorFinal.setText(String.format(Locale.getDefault(), "R$ %.2f", totalCompra));
    }

    private void exibirCarrinhoVazio() {
        txtCarrinhoVazio.setVisibility(View.VISIBLE);
        rvCarrinho.setVisibility(View.GONE);
        valorFinal.setText(String.format(Locale.getDefault(), "R$ %.2f", 0.0));
    }

    private void buscarDetalhesDosProdutos(HashMap<String, Integer> carrinhoItensMap) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                SUPABASE_URL,
                null,
                response -> {
                    try {
                        listaItensCarrinho.clear();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonProduto = response.getJSONObject(i);
                            String nome = jsonProduto.getString("name");

                            if (carrinhoItensMap.containsKey(nome)) {
                                double preco = jsonProduto.getDouble("price");
                                String emoji = jsonProduto.getString("image");
                                int estoque = jsonProduto.getInt("quantity");
                                int quantidadeNoCarrinho = carrinhoItensMap.get(nome);

                                Produto produtoCompleto = new Produto(nome, preco, emoji, estoque);
                                listaItensCarrinho.add(new ItemCarrinho(produtoCompleto, quantidadeNoCarrinho));
                            }
                        }

                        adapter.notifyDataSetChanged();
                        // Calcula o total inicial
                        recalcularTotal();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(Carrinho.this, "Erro ao processar os dados do carrinho.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(Carrinho.this, "Erro de rede ao buscar detalhes dos produtos.", Toast.LENGTH_SHORT).show();
                }) {
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
}
