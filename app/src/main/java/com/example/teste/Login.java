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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    private EditText emailRaEditText;
    private EditText senhaEditText;
    private Button butaoLogin;
    private Button btnCadastro;

    private static final String SUPABASE_URL = "https://tganxelcsfitizoffvyn.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRnYW54ZWxjc2ZpdGl6b2ZmdnluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE4NTgzMTMsImV4cCI6MjA3NzQzNDMxM30.ObZQ__nbVlej-lPE7L0a6mtGj323gI1bRq4DD4SkTeM";

    private static final String AUTH_SIGNIN_URL = SUPABASE_URL + "/auth/v1/token?grant_type=password";

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
            Toast.makeText(Login.this, "Preencha Email e Senha.", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject loginJson = new JSONObject();
        try {
            loginJson.put("email", email);
            loginJson.put("password", senha);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(Login.this, "Erro ao preparar os dados de login.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, AUTH_SIGNIN_URL, loginJson,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String accessToken = response.getString("access_token");
                            JSONObject user = response.getJSONObject("user");
                            String emailUsuario = user.getString("email");
                            String userId = user.getString("id");

                            SharedPreferences preferenciasCompartilhadas = getSharedPreferences("PreferenciasUsuario", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferenciasCompartilhadas.edit();
                            editor.putString("auth_token", accessToken);
                            editor.putString("emailDoUsuario", emailUsuario);
                            editor.putString("userId", userId);
                            editor.apply();

                            Toast.makeText(Login.this, "Login efetuado! Bem-vindo(a), " + emailUsuario, Toast.LENGTH_LONG).show();

                            Intent irParaTelaInicial = new Intent(Login.this, TelaInicial.class);
                            irParaTelaInicial.putExtra("userId", userId);
                            startActivity(irParaTelaInicial);
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Login.this, "Erro ao processar a resposta do servidor.", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("LoginAuthVolley", "Erro no login: " + error.toString());
                        String errorMessage = "Login falhou. Verifique seu Email e Senha.";

                        if (error.networkResponse != null) {
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject errorJson = new JSONObject(responseBody);

                                if (errorJson.has("error_description")) {
                                    errorMessage = errorJson.getString("error_description");
                                } else if (errorJson.has("msg")) {
                                    errorMessage = errorJson.getString("msg");
                                } else {
                                    errorMessage = "Erro HTTP: " + error.networkResponse.statusCode;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        Toast.makeText(Login.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_ANON_KEY);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }
}