package com.example.teste;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
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
import com.google.android.material.carousel.CarouselLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.Activity;


public class TelaInicial extends AppCompatActivity implements AlimentoAdapter.OnItemClickListener {

    private TextView txtSaudacao1;
    private ImageView carrinho, perfil, home;
    private RecyclerView recyclerView;
    private EditText editTextText;
    private AlimentoAdapter alimentoAdapter;


    private HashMap<String, Integer> carrinhoItens = new HashMap<>();

    private static final String SUPABASE_URL = "https://tganxelcsfitizoffvyn.supabase.co/rest/v1/products?select=name,price,image,quantity";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRnYW54ZWxjc2ZpdGl6b2ZmdnluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE4NTgzMTMsImV4cCI6MjA3NzQzNDMxM30.ObZQ__nbVlej-lPE7L0a6mtGj323gI1bRq4DD4SkTeM";

    private final ActivityResultLauncher<Intent> carrinhoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.hasExtra("carrinhoAtualizado")) {
                        HashMap<String, Integer> carrinhoAtualizado = (HashMap<String, Integer>) data.getSerializableExtra("carrinhoAtualizado");

                        if (carrinhoAtualizado != null) {
                            this.carrinhoItens = carrinhoAtualizado;
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telainicial);

        perfil = findViewById(R.id.perfilInicio);
        carrinho = findViewById(R.id.carrinhoInicio);
        txtSaudacao1 = findViewById(R.id.txtSaudacao1);
        recyclerView = findViewById(R.id.rv_favoritos);
        home = findViewById(R.id.homeInicio);
        editTextText = findViewById(R.id.editTextText);

        String nomeRecebido = getIntent().getStringExtra("nomeUsuario");
        if (nomeRecebido != null && !nomeRecebido.isEmpty()) {
            txtSaudacao1.setText("Bem vindo(a), " + nomeRecebido + "!");
        } else {
            txtSaudacao1.setText("Bem vindo(a)!");
        }

        buscarProdutosSupabase();

        editTextText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (alimentoAdapter != null) {
                    alimentoAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        carrinho.setOnClickListener(v -> {
            Intent irParaCarrinho = new Intent(TelaInicial.this, Carrinho.class);
            irParaCarrinho.putExtra("carrinhoItens", carrinhoItens);
            carrinhoLauncher.launch(irParaCarrinho);
        });

        perfil.setOnClickListener(v -> {
            Intent irParaPerfil = new Intent(TelaInicial.this, Perfil.class);
            startActivity(irParaPerfil);
        });

        home.setOnClickListener(v -> {
            Toast.makeText(TelaInicial.this, "Você já está na home!", Toast.LENGTH_SHORT).show();
        });

        RecyclerView recyclerView1 = findViewById(R.id.recycler);
        ArrayList<Integer> arrayList = new ArrayList<>();

        CarouselLayoutManager carouselLayoutManager = new CarouselLayoutManager();
        recyclerView1.setLayoutManager(carouselLayoutManager);

        arrayList.add(R.drawable.group389);
        arrayList.add(R.drawable.group390);
        arrayList.add(R.drawable.sorveteloslos);

        ImageAdapter adapter = new ImageAdapter(TelaInicial.this, arrayList);
        recyclerView1.setAdapter(adapter);
    }

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
                                int estoque = jsonProduto.getInt("quantity");

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

    private void configurarRecyclerView(List<Produto> listaDeAlimentos) {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        alimentoAdapter = new AlimentoAdapter(this, listaDeAlimentos, this);
        recyclerView.setAdapter(alimentoAdapter);
    }

    @Override
    public void onAdicionarClick(Produto produto) {
        String nomeProduto = produto.getNome();
        int quantidadeEmCarrinho = carrinhoItens.getOrDefault(nomeProduto, 0);

        if (produto.getEstoque() > quantidadeEmCarrinho) {
            carrinhoItens.put(nomeProduto, quantidadeEmCarrinho + 1);
            Toast.makeText(this, nomeProduto + " adicionado! Total em carrinho: " + (quantidadeEmCarrinho + 1), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Estoque esgotado para " + nomeProduto + "!", Toast.LENGTH_SHORT).show();
        }
    }

}