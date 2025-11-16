package com.example.teste;

import android.content.Intent;
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
import com.android.volley.toolbox.JsonObjectRequest; // Importação correta para requisição JSON
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Cadastro extends AppCompatActivity {

    // declarar variáveis
    private EditText nomeEditText;
    private EditText raEditText; // Campo adicionado para o RA
    private EditText emailEditText;
    private EditText senhaEditText;
    private Button butaoConfirmar;
    private Button tenhoContaButton;

    // chaves supabase
    private static final String SUPABASE_URL = "https://tganxelcsfitizoffvyn.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRnYW54ZWxjc2ZpdGl6b2ZmdnluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE4NTgzMTMsImV4cCI6MjA3NzQzNDMxM30.ObZQ__nbVlej-lPE7L0a6mtGj323gI1bRq4DD4SkTeM";

    // URL CORRETO para o cadastro de usuário no Supabase Auth (GoTrue)
    private static final String AUTH_SIGNUP_URL = SUPABASE_URL + "/auth/v1/signup";

    // fila de requisição
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        requestQueue = Volley.newRequestQueue(this);

        // vincular variáveis
        nomeEditText = findViewById(R.id.nome);
        raEditText = findViewById(R.id.editNome3); // Assumindo que este é o input do RA
        emailEditText = findViewById(R.id.editNome2); // Assumindo que este é o input do Email
        senhaEditText = findViewById(R.id.editTextNumberPassword);
        butaoConfirmar = findViewById(R.id.button);
        tenhoContaButton = findViewById(R.id.tenhoConta);


        // ação botão "tenho conta"
        tenhoContaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent irParaLogin = new Intent(Cadastro.this, Login.class);
                startActivity(irParaLogin);
            }
        });

        // ação botão "confirmar"
        butaoConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cadastrarUsuario();
            }
        });
    }

    // função cadastrar usuário corrigida para usar o Supabase Auth
    private void cadastrarUsuario() {

        // pegar dados dos campos de texto e converter para string
        final String nome = nomeEditText.getText().toString().trim();
        final String ra = raEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String senha = senhaEditText.getText().toString().trim();

        // verificar se os campos estão vazios
        if (nome.isEmpty() || ra.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(Cadastro.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Criar o objeto JSON para os dados do perfil (options.data)
        // Estes dados serão salvos em 'raw_user_meta_data' e lidos pelo Gatilho SQL.
        JSONObject userData = new JSONObject();
        try {
            userData.put("name", nome);
            userData.put("ra", ra);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 2. Criar o objeto JSON principal para o signup
        JSONObject signupJson = new JSONObject();
        try {
            signupJson.put("email", email);
            signupJson.put("password", senha);
            signupJson.put("data", userData); // Incluir dados do perfil
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(Cadastro.this, "Erro ao preparar os dados de cadastro.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Fazer requisição POST para a API de AUTENTICAÇÃO
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, AUTH_SIGNUP_URL, signupJson,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Resposta de sucesso do Auth
                        Toast.makeText(Cadastro.this, "Usuário cadastrado com sucesso! Verifique seu email para confirmar.", Toast.LENGTH_LONG).show();
                        Intent irParaLogin = new Intent(Cadastro.this, Login.class);
                        startActivity(irParaLogin);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("CadastroAuthVolley", "Erro ao cadastrar: " + error.toString());
                        String errorMessage = "Erro no Cadastro. Tente outro Email ou verifique o RA.";

                        if (error.networkResponse != null) {
                            try {
                                // Tenta obter a mensagem de erro do corpo da resposta HTTP
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject errorJson = new JSONObject(responseBody);
                                if (errorJson.has("msg")) {
                                    errorMessage = errorJson.getString("msg"); // Ex: "Email already registered"
                                } else {
                                    errorMessage = "Erro HTTP: " + error.networkResponse.statusCode;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        Toast.makeText(Cadastro.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Envia a chave anon para autenticação no serviço Auth
                headers.put("apikey", SUPABASE_ANON_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // adicionar requisição à fila
        requestQueue.add(request);
    }
}