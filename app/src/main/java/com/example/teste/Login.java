package com.example.teste;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


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

import java.util.HashMap;
import java.util.Map;
import java.io.UnsupportedEncodingException;

public class Login extends AppCompatActivity {


    private EditText emailRaEditText;
    private EditText senhaEditText;
    private Button butaoLogin;
    private Button btnCadastro;

    private static final String SUPABASE_URL = "https://tganxelcsfitizoffvyn.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRnYW54ZWxjc2ZpdGl6b2ZmdnluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE4NTgzMTMsImV4cCI6MjA3NzQzNDMxM30.ObZQ__nbVlej-lPE7L0a6mtGj323gI1bRq4DD4SkTeM";
    private static final String API_URL = SUPABASE_URL + "/rest/v1/users_app";

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        requestQueue = Volley.newRequestQueue(this);

        emailRaEditText = findViewById(R.id.nome);
        senhaEditText = findViewById(R.id.senhaEditText);
        butaoLogin = findViewById(R.id.buto);
        btnCadastro = findViewById(R.id.btnEnviar);

        btnCadastro.setOnClickListener(v -> {
            Intent irParaCadastro = new Intent(Login.this, Cadastro.class);
            startActivity(irParaCadastro);
        });

        butaoLogin.setOnClickListener(v -> {
            fazerLogin();
        });
    }

    private void fazerLogin() {

        final String email = emailRaEditText.getText().toString().trim();
        final String senha = senhaEditText.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(Login.this, "Preencha Email/RA e Senha.", Toast.LENGTH_SHORT).show();
            return;
        }

        String loginUrl = API_URL + "?email=eq." + email + "&password=eq." + senha;


        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, loginUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if (response.length() > 0) {
                            try {
                                JSONObject user = response.getJSONObject(0);
                                String nomeUsuario = user.getString("name");
                                String emailUsuario = user.getString("email");

                                SharedPreferences preferenciasCompartilhadas = getSharedPreferences("PreferenciasUsuario", MODE_PRIVATE); // 'sharedPreferences' para 'preferenciasCompartilhadas'
                                SharedPreferences.Editor editor = preferenciasCompartilhadas.edit();
                                editor.putString("emailDoUsuario", emailUsuario);
                                editor.apply();

                                Toast.makeText(Login.this, "Login efetuado! Bem-vindo(a), " + nomeUsuario, Toast.LENGTH_LONG).show();

                                Intent irParaTelaInicial = new Intent(Login.this, TelaInicial.class);
                                irParaTelaInicial.putExtra("nomeUsuario", nomeUsuario);
                                startActivity(irParaTelaInicial);
                                finish();

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(Login.this, "Erro ao processar dados do usuário.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(Login.this, "Credenciais inválidas. Verifique o Email/RA e Senha.", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("LoginVolley", "Erro na requisição: " + error.getMessage());
                        Toast.makeText(Login.this, "Erro de comunicação com o servidor.", Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_ANON_KEY);
                return headers;
            }
        };

        requestQueue.add(request);
    }
}