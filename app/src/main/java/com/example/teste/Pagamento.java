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
import com.android.volley.ParseError; // ⬅️ IMPORT NECESSÁRIO
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpHeaderParser; // ⬅️ IMPORT NECESSÁRIO
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class Pagamento extends AppCompatActivity {
    // --- CHAVES DE CONFIGURAÇÃO ---
    private static final String SUPABASE_URL = "https://tganxelcsfitizoffvyn.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRnYW54ZWxjc2ZpdGl6b2ZmdnluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE4NTgzMTMsImV4cCI6MjA3NzQzNDMxM30.ObZQ__nbVlej-lPE7L0a6mtGj323gI1bRq4DD4SkTeM";
    private static final String USER_API_URL = SUPABASE_URL + "/rest/v1/users_app";
    private static final String PEDIDOS_API_URL = SUPABASE_URL + "/rest/v1/pedidos";
    private static final String MP_BACKEND_API_URL = "https://tganxelcsfitizoffvyn.supabase.co/functions/v1/quick-handler";

    // --- VARIÁVEIS DE CONTROLE ---
    private TextView valorTotal;
    private RadioGroup rgPagamento;
    private Button btnConfirmar, cancelar;

    private String metodoPagamentoSelecionado = "Não selecionado";
    private String valorDaCompraExibido = "0.00";
    private String emailDoUsuario = null;
    private String userId = null;
    private String accessToken = null;
    private List<ItemCarrinho> itensDoCarrinho = new ArrayList<>();

    private final String idempotencyId = UUID.randomUUID().toString();

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagamento);

        requestQueue = Volley.newRequestQueue(this);

        // --- VINCULAÇÃO E DADOS INICIAIS ---
        valorTotal = findViewById(R.id.valorTotal);
        rgPagamento = findViewById(R.id.rgPagamento);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        cancelar = findViewById(R.id.cancelar);

        SharedPreferences preferencias = getSharedPreferences("PreferenciasUsuario", MODE_PRIVATE);
        emailDoUsuario = preferencias.getString("emailDoUsuario", null);
        accessToken = preferencias.getString("auth_token", null);

        if (getIntent().hasExtra("itensCarrinho")) {
            itensDoCarrinho = (List<ItemCarrinho>) getIntent().getSerializableExtra("itensCarrinho");

            double totalParaExibicao = 0.0;
            for (ItemCarrinho item : itensDoCarrinho) {
                totalParaExibicao += item.getPrecoTotal();
            }
            valorDaCompraExibido = String.valueOf(totalParaExibicao);
        }

        try {
            double valorDouble = Double.parseDouble(valorDaCompraExibido);
            valorTotal.setText("Total " + String.format(Locale.getDefault(), "R$ %.2f", valorDouble));
        } catch (NumberFormatException e) {
            valorTotal.setText("Total R$0.00");
        }

        if (emailDoUsuario != null) {
            fetchUserIdByEmail(emailDoUsuario);
        } else {
            Toast.makeText(this, "Atenção: Email do usuário não encontrado! Não é possível prosseguir.", Toast.LENGTH_LONG).show();
            btnConfirmar.setEnabled(false);
        }

        if (accessToken == null) {
            Toast.makeText(this, "Erro: Token de acesso indisponível.", Toast.LENGTH_LONG).show();
        }

        // --- LISTENERS ---
        cancelar.setOnClickListener(v -> finish());

        rgPagamento.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = findViewById(checkedId);
            if (selectedRadioButton != null) {
                metodoPagamentoSelecionado = selectedRadioButton.getText().toString();
            } else {
                metodoPagamentoSelecionado = "Não selecionado";
            }
        });

        btnConfirmar.setOnClickListener(v -> {
            int selectedId = rgPagamento.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(Pagamento.this, "Por favor, selecione uma forma de pagamento.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userId == null) {
                Toast.makeText(Pagamento.this, "Aguardando confirmação do usuário (ID). Tente novamente.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🎯 INÍCIO DO FLUXO CONDICIONAL
            btnConfirmar.setEnabled(false); // Desabilitar o botão para prevenir o duplo clique

            if (metodoPagamentoSelecionado.toLowerCase().contains("pix")) {
                iniciarConfirmacaoPedidoPix();
            } else if (metodoPagamentoSelecionado.toLowerCase().contains("dinheiro")) {
                iniciarConfirmacaoPedidoDinheiro();
            } else {
                Toast.makeText(Pagamento.this, "Apenas PIX e Dinheiro estão implementados.", Toast.LENGTH_SHORT).show();
                btnConfirmar.setEnabled(true);
            }
        });
    }

    // --- MÉTODOS DE REDE ---

    private void fetchUserIdByEmail(String email) {
        String url = USER_API_URL + "?select=id&email=eq." + email;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            userId = response.getJSONObject(0).getString("id");
                        } else {
                            Toast.makeText(Pagamento.this, "Usuário não cadastrado!", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("SUPABASE", "Erro ao processar ID do usuário.", e);
                    }
                },
                error -> {
                    Toast.makeText(Pagamento.this, "Erro ao buscar ID do usuário.", Toast.LENGTH_LONG).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_ANON_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_ANON_KEY);
                return headers;
            }
        };
        requestQueue.add(jsonArrayRequest);
    }

    // 🎯 NOVO: Fluxo de Pedido com Pagamento em Dinheiro (Inserção Direta)
    private void iniciarConfirmacaoPedidoDinheiro() {
        if (userId == null || itensDoCarrinho.isEmpty() || accessToken == null) {
            Toast.makeText(this, "Erro interno: Dados de usuário, carrinho ou token inválidos.", Toast.LENGTH_LONG).show();
            btnConfirmar.setEnabled(true);
            return;
        }

        try {
            JSONObject pedidoJson = new JSONObject();
            pedidoJson.put("user_app_id", userId);
            pedidoJson.put("metodo_pagamento", "dinheiro");
            pedidoJson.put("valor_total", Double.parseDouble(valorDaCompraExibido));
            pedidoJson.put("data_pedido", getCurrentUtcIsoTime());
            pedidoJson.put("status_pedido", "EM ESPERA");

            // Não incluímos os itens aqui para simplificar a inserção direta
            // Para inserção completa, use uma Stored Procedure/Edge Function.

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, PEDIDOS_API_URL, pedidoJson,
                    response -> {
                        // Resposta de sucesso será tratada pelo parseNetworkResponse
                        Toast.makeText(Pagamento.this, "Pedido em Dinheiro registrado com sucesso!", Toast.LENGTH_LONG).show();
                        lancarTelaPedidoSimples();
                    },
                    error -> {
                        Log.e("DINHEIRO_FAIL", "Erro ao inserir pedido: " + error.toString());
                        Toast.makeText(Pagamento.this, "Falha ao registrar pedido em dinheiro.", Toast.LENGTH_LONG).show();
                        btnConfirmar.setEnabled(true);
                    }) {

                // ⬅️ CORREÇÃO CRÍTICA: Tratar resposta vazia (201/204) do PostgREST
                @Override
                protected Response<JSONObject> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                    if (response.statusCode == 201 || response.statusCode == 204) {
                        // Retorna um objeto JSON vazio, tratando a resposta como sucesso
                        return Response.success(new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));
                    }
                    // Se não for sucesso, chama o parser padrão para tratar erros (4xx/5xx)
                    return super.parseNetworkResponse(response);
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + accessToken); // Usa o token de usuário logado
                    headers.put("apikey", SUPABASE_ANON_KEY);
                    return headers;
                }
            };
            requestQueue.add(jsonObjectRequest);

        } catch (JSONException | NumberFormatException e) {
            Toast.makeText(this, "Erro de processamento dos dados do pedido.", Toast.LENGTH_LONG).show();
            btnConfirmar.setEnabled(true);
        }
    }

    // 🎯 NOVO NOME: Fluxo de Pedido com Pagamento PIX
    private void iniciarConfirmacaoPedidoPix() {
        if (userId == null || itensDoCarrinho.isEmpty()) {
            Toast.makeText(this, "Erro interno: Dados de usuário ou carrinho inválidos.", Toast.LENGTH_LONG).show();
            btnConfirmar.setEnabled(true);
            return;
        }

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("user_app_id", userId);
            requestBody.put("metodo_pagamento", "pix");
            requestBody.put("referencia_unica", idempotencyId);

            JSONArray itensJsonArray = new JSONArray();

            for (ItemCarrinho item : itensDoCarrinho) {
                JSONObject itemJson = new JSONObject();
                itemJson.put("produto_id", item.getProduto().getId());
                itemJson.put("quantidade", item.getQuantidade());
                itemJson.put("preco_unitario", item.getPrecoUnitario());
                itensJsonArray.put(itemJson);
            }
            requestBody.put("itens", itensJsonArray);

            Log.d("PAGAMENTO_REQUEST", "JSON para backend: " + requestBody.toString());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, MP_BACKEND_API_URL, requestBody,
                    response -> {
                        try {
                            String novoPedidoIdStr = response.getString("id_pedido_supabase");
                            String qrCodeBase64 = response.getString("qrCodeBase64");
                            String ticketUrl = response.getString("ticketUrl");

                            lancarTelaPix(novoPedidoIdStr, qrCodeBase64, ticketUrl);

                        } catch (JSONException e) {
                            Log.e("MP_INTEGRATION", "Erro ao processar resposta do Backend. JSON inválido.", e);
                            Toast.makeText(Pagamento.this, "Erro ao processar dados de pagamento.", Toast.LENGTH_LONG).show();
                            btnConfirmar.setEnabled(true);
                        }
                    },
                    error -> {
                        String errorMsg = "Falha de comunicação com o Backend MP.";
                        if (error.networkResponse != null) {
                            errorMsg += " Status: " + error.networkResponse.statusCode;
                            try {
                                String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                Log.e("MP_INTEGRATION_FAIL", "Detalhe: " + responseBody);
                                JSONObject errorJson = new JSONObject(responseBody);
                                if (error.networkResponse.statusCode == 409) {
                                    errorMsg = "Pedido duplicado bloqueado. Prosseguindo...";
                                }
                                errorMsg += " Detalhe: " + errorJson.optString("mensagem", "Erro desconhecido.");
                            } catch (Exception e) {}
                        }
                        Log.e("MP_INTEGRATION", errorMsg, error);
                        Toast.makeText(Pagamento.this, errorMsg, Toast.LENGTH_LONG).show();
                        btnConfirmar.setEnabled(true);
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + SUPABASE_ANON_KEY);
                    headers.put("apikey", SUPABASE_ANON_KEY);
                    return headers;
                }
            };
            requestQueue.add(jsonObjectRequest);

        } catch (JSONException e) {
            Toast.makeText(this, "Erro de processamento dos dados do pedido.", Toast.LENGTH_LONG).show();
            btnConfirmar.setEnabled(true);
        }
    }

    private void lancarTelaPix(String pedidoIdStr, String qrCodeBase64, String ticketUrl) {
        String mensagem = "Pedido #" + pedidoIdStr + " criado! Agora pague via PIX.";
        Toast.makeText(Pagamento.this, mensagem, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(Pagamento.this, PedidoConfirmado.class);
        intent.putExtra("pedidoId", pedidoIdStr);
        intent.putExtra("qrCodeBase64", qrCodeBase64);
        intent.putExtra("ticketUrl", ticketUrl);

        startActivity(intent);
        finish();
    }

    private void lancarTelaPedidoSimples() {
        Intent intent = new Intent(Pagamento.this, PedidoConfirmado.class);
        intent.putExtra("mensagemStatus", "Seu pedido foi registrado! Pague no balcão.");
        startActivity(intent);
        finish();
    }

    private String getCurrentUtcIsoTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }
}