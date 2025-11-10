package com.example.teste;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Pagamento extends AppCompatActivity {
    // chaves do supabase
    private static final String SUPABASE_URL = "https://tganxelcsfitizoffvyn.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRnYW54ZWxjc2ZpdGl6b2ZmdnluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE4NTgzMTMsImV4cCI6MjA3NzQzNDMxM30.ObZQ__nbVlej-lPE7L0a6mtGj323gI1bRq4DD4SkTeM";
    private static final String USER_API_URL = SUPABASE_URL + "/rest/v1/users_app";
    private static final String PEDIDOS_API_URL = SUPABASE_URL + "/rest/v1/pedidos";
    private static final String PEDIDOS_PRODUTOS_API_URL = SUPABASE_URL + "/rest/v1/pedidos_produtos";
    private static final String PRODUCTS_API_URL = SUPABASE_URL + "/rest/v1/products";

    // declarar variáveis
    private TextView valorTotal;
    private RadioGroup rgPagamento;
    private Button btnConfirmar, cancelar;
    // declarar variáveis de controle de fluxo
    private String metodoPagamentoSelecionado = "Não selecionado"; // inicialmente, não há método de pagamento selecionado
    private String valorDaCompra = null; // inicialmente, não há valor da compra
    private String emailDoUsuario = null; // inicialmente, não há email do usuário
    private int userId = -1; // inicialmente, o ID do usuário é desconhecido
    private List<ItemCarrinho> itensDoCarrinho = new ArrayList<>(); // inicialmente, o carrinho está vazio

    private RequestQueue requestQueue;
    private int itensPendentesParaAtualizar = 0;
    private int falhasDeEstoque = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagamento);
        // vincular variáveis
        requestQueue = Volley.newRequestQueue(this);

        valorTotal = findViewById(R.id.valorTotal);
        rgPagamento = findViewById(R.id.rgPagamento);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        cancelar = findViewById(R.id.cancelar);
        // pegar dados da tela anterior e passar para esta
        valorDaCompra = getIntent().getStringExtra("total");
        SharedPreferences preferencias = getSharedPreferences("PreferenciasUsuario", MODE_PRIVATE); // 'sharedPreferences' para 'preferenciasCompartilhadas'
        emailDoUsuario = preferencias.getString("emailDoUsuario", null);

        // pegar itens do carrinho
        if (getIntent().hasExtra("itensCarrinho")) {
            itensDoCarrinho = (List<ItemCarrinho>) getIntent().getSerializableExtra("itensCarrinho");
        }

        // exibir valor total da compra no TextView
        if (valorDaCompra != null) {
            try {
                double valorDouble = Double.parseDouble(valorDaCompra);
                valorTotal.setText("Total " + String.format(Locale.getDefault(), "R$ %.2f", valorDouble));
            } catch (NumberFormatException e) {
                valorTotal.setText("Total R$0.00");
                valorDaCompra = "0.00";
            }
        } else {
            valorDaCompra = "0.00";
            valorTotal.setText("Total R$0.00");
        }

        // pegar ID do usuário
        if (emailDoUsuario != null) {
            fetchUserIdByEmail(emailDoUsuario);
        } else {
            Toast.makeText(this, "Atenção: Email do usuário não encontrado!", Toast.LENGTH_LONG).show();
        }

        // ação botão "cancelar"
        cancelar.setOnClickListener(v -> finish());

        // ação botão "confirmar"
        rgPagamento.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = findViewById(checkedId);
            if (selectedRadioButton != null) {
                metodoPagamentoSelecionado = selectedRadioButton.getText().toString();
            } else {
                metodoPagamentoSelecionado = "Não selecionado";
            }
        });

        // ação botão "confirmar"
        btnConfirmar.setOnClickListener(v -> {
            int selectedId = rgPagamento.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(Pagamento.this, "Por favor, selecione uma forma de pagamento.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userId == -1) {
                Toast.makeText(Pagamento.this, "Aguardando confirmação do usuário. Tente novamente.", Toast.LENGTH_SHORT).show();
                return;
            }

            iniciarConfirmacaoPedido();
        });
    }

    // pegar ID do usuário a partir do email
    private void fetchUserIdByEmail(String email) {
        String url = USER_API_URL + "?select=id&email=eq." + email;

        // fazer requisição GET para a API do Supabase com o email do usuário
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            userId = response.getJSONObject(0).getInt("id"); // pegar o ID do usuário retornado pela API
                        } else {
                            Toast.makeText(Pagamento.this, "Usuário não cadastrado!", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                    }
                },
                error -> {
                    Toast.makeText(Pagamento.this, "Erro ao buscar ID do usuário.", Toast.LENGTH_LONG).show();
                }) {
            // configurar cabeçalho da requisição com a chave de autenticação
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_ANON_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_ANON_KEY);
                return headers;
            }
        };

        // adicionar a requisição à fila
        requestQueue.add(jsonArrayRequest);
    }

    // iniciar confirmação do pedido
    private void iniciarConfirmacaoPedido() {
        if (userId == -1 || valorDaCompra == null) {
            Toast.makeText(this, "Erro interno: Dados de usuário ou valor da compra inválidos.", Toast.LENGTH_LONG).show();
            return;
        }

        // fazer requisição POST para a API do Supabase para criar o pedido
        try {
            JSONObject pedidoJson = new JSONObject();
            pedidoJson.put("user_app_id", userId);
            pedidoJson.put("valor_total", Double.parseDouble(valorDaCompra));

            String url = PEDIDOS_API_URL + "?select=*";

            // fazer requisição POST para a API do Supabase para criar o pedido
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, url, new JSONArray().put(pedidoJson),
                    response -> {
                        try {
                            if (response.length() > 0) {
                                int novoPedidoId = response.getJSONObject(0).getInt("id");
                                salvarItensPedido(novoPedidoId);
                            } else {
                                Toast.makeText(Pagamento.this, "Erro: Pedido não retornado pelo servidor.", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(Pagamento.this, "Erro ao processar a resposta do pedido.", Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> {
                        String errorMsg = "Falha de rede ao criar pedido.";
                        if (error.networkResponse != null) {
                            errorMsg += " Status: " + error.networkResponse.statusCode;
                        }
                        Toast.makeText(Pagamento.this, errorMsg, Toast.LENGTH_LONG).show();
                    }) {
                // configurar cabeçalho da requisição com a chave de autenticação
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("apikey", SUPABASE_ANON_KEY);
                    headers.put("Authorization", "Bearer " + SUPABASE_ANON_KEY);
                    headers.put("Content-Type", "application/json");
                    headers.put("Prefer", "return=representation");
                    return headers;
                }
            };
            // adicionar a requisição à fila
            requestQueue.add(jsonArrayRequest);

        } catch (JSONException | NumberFormatException e) {
            Toast.makeText(this, "Erro de processamento dos dados do pedido.", Toast.LENGTH_LONG).show();
        }
    }
    // finalizar pedido com sucesso e exibir mensagem
    private void finalizarPedidoComSucesso(int pedidoId) {
        String mensagem;
        // verificar se houve falhas de estoque
        if (falhasDeEstoque > 0) {
            mensagem = "ALERTA: Pedido #" + pedidoId + " confirmado, mas " + falhasDeEstoque + " item(s) falhou(ram) na atualização de estoque!";
            Toast.makeText(Pagamento.this, mensagem, Toast.LENGTH_LONG).show();
        } else {
            mensagem = "Pedido #" + pedidoId + " (" + metodoPagamentoSelecionado + ") confirmado e estoque atualizado!";
            Toast.makeText(Pagamento.this, mensagem, Toast.LENGTH_LONG).show();
        }

        Intent intent = new Intent(Pagamento.this, PedidoConfirmado.class);

        startActivity(intent);
        finish();
    }

    // salvar itens do pedido
    private void salvarItensPedido(int pedidoId) {
        if (itensDoCarrinho.isEmpty()) {
            Toast.makeText(this, "Atenção: Carrinho estava vazio ao finalizar.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // fazer requisição POST para a API do Supabase para criar os itens do pedido
        try {
            JSONArray itensJsonArray = new JSONArray();

            for (ItemCarrinho item : itensDoCarrinho) {
                JSONObject itemJson = new JSONObject();
                itemJson.put("pedido_id", pedidoId);
                itemJson.put("produto_id", item.getProduto().getId());
                itemJson.put("quantidade", item.getQuantidade());
                itemJson.put("preco_unitario", item.getProduto().getPrecoAtual());
                itensJsonArray.put(itemJson);
            }
            // fazer requisição POST para a API do Supabase para criar os itens do pedido
            String urlItens = PEDIDOS_PRODUTOS_API_URL + "?select=*";

            // fazer requisição POST para a API do Supabase para criar os itens do pedido
            JsonArrayRequest itensRequest = new JsonArrayRequest(Request.Method.POST, urlItens, itensJsonArray,
                    response -> {
                        itensPendentesParaAtualizar = itensDoCarrinho.size();
                        falhasDeEstoque = 0;
                        for (ItemCarrinho item : itensDoCarrinho) {
                            atualizarEstoque(pedidoId, item);
                        }
                    },
                    error -> {
                        String errorMsg = "Falha ao salvar itens (pedidos_produtos). O pedido (cabeçalho) foi criado. ";
                        if (error.networkResponse != null) {
                            errorMsg += "Status HTTP: " + error.networkResponse.statusCode;
                            if (error.networkResponse.data != null) {
                                try {
                                    String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    errorMsg += ". Detalhe: " + responseBody;
                                } catch (Exception e) {
                                    errorMsg += ". Resposta do servidor não legível.";
                                }
                            }
                        } else {
                            errorMsg += "Sem resposta do servidor (Erro de rede/timeout).";
                        }

                        Log.e("SUPABASE_ITENS", errorMsg, error);
                        Toast.makeText(Pagamento.this, errorMsg, Toast.LENGTH_LONG).show();
                    }) {
                // configurar cabeçalho da requisição com a chave de autenticação
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("apikey", SUPABASE_ANON_KEY);
                    headers.put("Authorization", "Bearer " + SUPABASE_ANON_KEY);
                    headers.put("Content-Type", "application/json");
                    headers.put("Prefer", "return=representation");
                    return headers;
                }
            };

            requestQueue.add(itensRequest);

        } catch (JSONException e) {
            Toast.makeText(this, "Erro de processamento dos itens do pedido.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // atualizar estoque dos itens do pedido e verificar se houve falhas
    private void atualizarEstoque(int pedidoId, ItemCarrinho item) {
        String url = PRODUCTS_API_URL + "?id=eq." + item.getProduto().getId();

        // fazer requisição PATCH para a API do Supabase para atualizar o estoque
        try {
            int novaQuantidade = item.getProduto().getEstoque() - item.getQuantidade();

            // montar JSON de atualização
            JSONObject jsonUpdate = new JSONObject();
            jsonUpdate.put("quantity", Math.max(0, novaQuantidade));

            // fazer requisição PATCH para a API do Supabase para atualizar o estoque
            JsonObjectRequest updateRequest = new JsonObjectRequest(Request.Method.PATCH, url, jsonUpdate,
                    response -> {
                        itensPendentesParaAtualizar--;

                        if (itensPendentesParaAtualizar == 0) {
                            finalizarPedidoComSucesso(pedidoId);
                        }
                    },
                    error -> {
                        itensPendentesParaAtualizar--;
                        falhasDeEstoque++;

                        // log de erro
                        String errorMsg = "FALHA ao atualizar estoque para Produto ID: " + item.getProduto().getId();
                        if (error.networkResponse != null) {
                            errorMsg += " Status HTTP: " + error.networkResponse.statusCode;
                            if (error.networkResponse.data != null) {
                                try {
                                    errorMsg += " Detalhe: " + new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                } catch (Exception e) {}
                            }
                        }
                        Log.e("ESTOQUE", errorMsg);

                        // verificar se houve falhas de estoque
                        if (itensPendentesParaAtualizar == 0) {
                            finalizarPedidoComSucesso(pedidoId);
                        }
                    }) {
                // configurar cabeçalho da requisição com a chave de autenticação
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("apikey", SUPABASE_ANON_KEY);
                    headers.put("Authorization", "Bearer " + SUPABASE_ANON_KEY);
                    headers.put("Content-Type", "application/json");
                    headers.put("Content-Profile", "public");
                    headers.put("Prefer", "return=representation");
                    headers.put("Accept", "application/vnd.pgrst.object+json, application/json");
                    return headers;
                }
            };

            requestQueue.add(updateRequest);

        } catch (JSONException e) {
            Log.e("ESTOQUE", "Erro ao montar JSON de atualização de estoque.");
        }
    }
}