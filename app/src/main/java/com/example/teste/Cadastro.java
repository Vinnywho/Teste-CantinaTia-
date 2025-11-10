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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Cadastro extends AppCompatActivity {

    // declarar variáveis
    private EditText nomeEditText;
    private EditText raEditText;
    private EditText emailEditText;
    private EditText senhaEditText;
    private Button butaoConfirmar;
    private Button tenhoContaButton;

    // chaves supabase
    private static final String SUPABASE_URL = "https://tganxelcsfitizoffvyn.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRnYW54ZWxjc2ZpdGl6b2ZmdnluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE4NTgzMTMsImV4cCI6MjA3NzQzNDMxM30.ObZQ__nbVlej-lPE7L0a6mtGj323gI1bRq4DD4SkTeM";
    private static final String API_URL = SUPABASE_URL + "/rest/v1/users_app";

    // fila de requisição
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        requestQueue = Volley.newRequestQueue(this);

        // vincular variáveis
        nomeEditText = findViewById(R.id.nome);
        raEditText = findViewById(R.id.editNome3);
        emailEditText = findViewById(R.id.editNome2);
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

    // função cadastrar usuário
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

        // criar objeto JSON com os dados do usuário
        JSONObject userJson = new JSONObject();
        try {
            userJson.put("name", nome);
            userJson.put("ra", ra);
            userJson.put("email", email);
            userJson.put("password", senha);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(Cadastro.this, "Erro ao preparar os dados.", Toast.LENGTH_SHORT).show(); // notificar erro
            return;
        }

        // converter objeto JSON para string
        final String requestBody = userJson.toString();

        // fazer requisição POST para a API do Supabase
        StringRequest request = new StringRequest(Request.Method.POST, API_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(Cadastro.this, "Usuário adicionado com sucesso!", Toast.LENGTH_LONG).show();
                        Intent irParaLogin = new Intent(Cadastro.this, Login.class);
                        startActivity(irParaLogin);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("CadastroVolley", "Erro ao cadastrar: " + error.toString());
                        String errorMessage = "Erro no Cadastro. Verifique RA/Email.";

                        if (error.networkResponse != null) {
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                if (responseBody.contains("duplicate key value")) {
                                    errorMessage = "RA ou Email já cadastrados.";
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

            // configurar cabeçalho da requisição com o tipo de conteúdo
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            // configurar corpo da requisição com os dados do usuário
            @Override
            public byte[] getBody() {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    return null;
                }
            }

            // configurar cabeçalho da requisição com a chave de autenticação
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_ANON_KEY);
                headers.put("Prefer", "return=minimal");
                return headers;
            }
        };

        // adicionar requisição à fila
        requestQueue.add(request);
    }
}