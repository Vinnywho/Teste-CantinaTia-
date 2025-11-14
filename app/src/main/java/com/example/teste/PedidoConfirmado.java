package com.example.teste;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import java.util.HashMap;
import java.util.Map;

public class PedidoConfirmado extends AppCompatActivity {

    // --- VARIÁVEIS DE CONTROLE ---
    private ImageView imgQrCodePix;
    private TextView txtChavePix;
    private TextView txtNumeroPedido; // 🎯 VARIÁVEL DECLARADA
    private Button btnConfirmar;

    // --- VARIÁVEIS DE PESQUISA DE STATUS (POLLING) ---
    private Handler handler;
    private Runnable runnable;
    private RequestQueue requestQueue;
    private String pedidoIdStr; // ID do pedido do Supabase

    // ⚠️ ATENÇÃO: USE A CHAVE E URL CORRETAS DO SUPABASE
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRnYW54ZWxjc2ZpdGl6b2ZmdnluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE4NTgzMTMsImV4cCI6MjA3NzQzNDMxM30.ObZQ__nbVlej-lPE7L0a6mtGj323gI1bRq4DD4SkTeM";
    private static final String SUPABASE_PEDIDO_URL = "https://tganxelcsfitizoffvyn.supabase.co/rest/v1/pedidos";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido_confirmado);

        requestQueue = Volley.newRequestQueue(this);

        // --- VINCULAÇÃO DE COMPONENTES ---
        imgQrCodePix = findViewById(R.id.imgQrCodePix);
        txtChavePix = findViewById(R.id.txtChavePix);
        txtNumeroPedido = findViewById(R.id.txtNumeroPedido); // 🎯 INICIALIZAÇÃO CORRIGIDA
        btnConfirmar = findViewById(R.id.pedidoConfirmado);

        // --- RECEBIMENTO DE DADOS E INICIALIZAÇÃO DO POLLING ---
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            pedidoIdStr = extras.getString("pedidoId");
            String qrCodeBase64 = extras.getString("qrCodeBase64");
            String ticketUrl = extras.getString("ticketUrl");

            // 1. Exibir Número do Pedido
            if (pedidoIdStr != null) {
                txtNumeroPedido.setText("Nº: #" + pedidoIdStr);
                // Inicia o Polling
                iniciarPollingStatus();
            } else {
                txtNumeroPedido.setText("Nº: Indisponível");
            }

            // 2. EXIBIR QR CODE (Decodificar Base64)
            if (qrCodeBase64 != null && !qrCodeBase64.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(qrCodeBase64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imgQrCodePix.setImageBitmap(decodedByte);
                } catch (IllegalArgumentException e) {
                    Log.e("PIX_DECODE", "Erro ao decodificar QR Code.", e);
                    Toast.makeText(this, "Erro ao decodificar QR Code.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "QR Code não recebido.", Toast.LENGTH_SHORT).show();
            }

            // 3. EXIBIR A CHAVE PIX (Linha Digitável)
            if (ticketUrl != null && !ticketUrl.isEmpty()) {
                txtChavePix.setText(ticketUrl);
            } else {
                txtChavePix.setText("Chave PIX não disponível. Use o QR Code.");
            }
        }

        // --- LISTENERS ---
        txtChavePix.setOnClickListener(v -> {
            String chave = txtChavePix.getText().toString();
            if (!chave.contains("não disponível") && !chave.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Chave PIX", chave);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Chave PIX copiada!", Toast.LENGTH_SHORT).show();
            }
        });

        btnConfirmar.setOnClickListener(v -> {
            finish();
        });
    }

    // 🎯 MÉTODO: Inicializa o Polling
    private void iniciarPollingStatus() {
        handler = new Handler();
        final int intervalo = 5000; // 5 segundos

        runnable = new Runnable() {
            @Override
            public void run() {
                verificarStatusPedido();
                handler.postDelayed(this, intervalo);
            }
        };
        handler.post(runnable);
    }

    // 🎯 MÉTODO: Verifica o status no Supabase via Volley
    private void verificarStatusPedido() {
        if (pedidoIdStr == null) return;

        // 🛑 CORREÇÃO FINAL: Usamos 'payment_id_mp' para pesquisa, pois o ID que veio da Pagamento.java é o ID do MP
        String url = SUPABASE_PEDIDO_URL + "?select=status&payment_id_mp=eq." + pedidoIdStr;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            String status = response.getJSONObject(0).getString("status");

                            if ("approved".equalsIgnoreCase(status) || "paid".equalsIgnoreCase(status)) {
                                // 🟢 PAGAMENTO CONFIRMADO!
                                handler.removeCallbacks(runnable); // Para o polling
                                navegarParaConfirmacaoFinal(); // Navega para a nova tela
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("POLLING", "Erro ao processar JSON de status.", e);
                    }
                },
                error -> {
                    Log.e("POLLING", "Erro de rede ao buscar status: " + error.toString());
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

    // 🎯 MÉTODO: Navegação para a tela final
    private void navegarParaConfirmacaoFinal() {
        Intent intent = new Intent(PedidoConfirmado.this, PagamentoConfirmado.class);
        intent.putExtra("pedidoId", pedidoIdStr);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        super.onDestroy();
    }
}