package com.example.teste;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class HistoricoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoricoAdapter adapter;
    private List<ItemHistorico> listaHistorico;
    private ImageView carrinho, perfil, home;

    private RequestQueue requestQueue;

    private static final String SUPABASE_URL = "https://tganxelcsfitizoffvyn.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRnYW54ZWxjc2ZpdGl6b2ZmdnluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE4NTgzMTMsImV4cCI6MjA3NzQzNDMxM30.ObZQ__nbVlej-lPE7L0a6mtGj323gI1bRq4DD4SkTeM";

    private static final String API_PEDIDOS_URL = SUPABASE_URL + "/rest/v1/pedidos";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        requestQueue = Volley.newRequestQueue(this);

        recyclerView = findViewById(R.id.rv_historico);
        perfil = findViewById(R.id.perfilHistorico);
        carrinho = findViewById(R.id.carrinhoHistorico);
        home = findViewById(R.id.homeHistorico);

        listaHistorico = new ArrayList<>();

        adapter = new HistoricoAdapter(listaHistorico);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchHistorico();

        carrinho.setOnClickListener(v -> {
            Intent irParaCarrinho = new Intent(HistoricoActivity.this, Carrinho.class);
            startActivity(irParaCarrinho);
        });

        perfil.setOnClickListener(v -> {
            Intent irParaPerfil = new Intent(HistoricoActivity.this, Perfil.class);
            startActivity(irParaPerfil);
        });

        home.setOnClickListener(v -> {
            Intent irParaHome = new Intent(HistoricoActivity.this, TelaInicial.class);
            startActivity(irParaHome);
        });
    }

    private void fetchHistorico() {
        SharedPreferences prefs = getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE);
        final String userId = prefs.getString("userId", null);
        final String accessToken = prefs.getString("auth_token", null);

        if (userId == null || accessToken == null) {
            Toast.makeText(this, "Erro: Usuário não logado ou sessão expirada.", Toast.LENGTH_LONG).show();
            return;
        }

        final String userIdString = String.valueOf(userId);

        String queryUrl = API_PEDIDOS_URL +
                "?user_app_id=eq." + userIdString.trim() +
                "&select=id,data_pedido,valor_total" + // ⬅️ ID adicionado aqui
                "&order=data_pedido.desc";


        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, queryUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        listaHistorico.clear();

                        SimpleDateFormat supabaseSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
                        supabaseSdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject pedido = response.getJSONObject(i);

                                int id = pedido.getInt("id"); // ⬅️ Extraindo o ID do pedido
                                String dataStr = pedido.getString("data_pedido");
                                double precoTotal = pedido.getDouble("valor_total");

                                String cleanedDataStr = dataStr.replace("Z", "");
                                int dotIndex = cleanedDataStr.indexOf('.');
                                if (dotIndex > 0 && cleanedDataStr.length() > dotIndex + 4) {
                                    cleanedDataStr = cleanedDataStr.substring(0, dotIndex + 4);
                                } else if (dotIndex < 0) {
                                    cleanedDataStr = cleanedDataStr + ".000";
                                }

                                Date date = supabaseSdf.parse(cleanedDataStr);

                                long timestampLong = date.getTime();

                                // O ItemHistorico precisa de um campo para o ID
                                // Assumindo que você corrigirá ItemHistorico para aceitar o ID (int)
                                listaHistorico.add(new ItemHistorico(id, timestampLong, precoTotal));

                            } catch (JSONException | ParseException e) {
                                Log.e("Historico", "Erro ao parsear JSON ou Data: " + e.getMessage());
                            }
                        }

                        adapter.notifyDataSetChanged();
                        if (listaHistorico.isEmpty()) {
                            Toast.makeText(HistoricoActivity.this, "Nenhum histórico de pedidos encontrado.", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("HistoricoVolley", "Erro na requisição: " + error.getMessage());
                        Toast.makeText(HistoricoActivity.this, "Falha ao carregar histórico: Verifique a conexão.", Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_ANON_KEY);
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }
}