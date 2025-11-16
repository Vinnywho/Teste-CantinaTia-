package com.example.teste;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class Perfil extends AppCompatActivity {
    // constante para a seleção de imagem
    private static final int PICK_IMAGE_REQUEST = 1;
    // variável para armazenar a URI da imagem selecionada
    private Uri selectedImageUri;
    private ImageView home, fotoPerfil, carrinho, perfilPerfil;
    private TextView nomeUsuario;

    private Button historico;

    private static final String SUPABASE_URL = "https://tganxelcsfitizoffvyn.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRnYW54ZWxjc2ZpdGl6b2ZmdnluIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjE4NTgzMTMsImV4cCI6MjA3NzQzNDMxM30.ObZQ__nbVlej-lPE7L0a6mtGj323gI1bRq4DD4SkTeM";

    private static final String BUCKET_NAME = "imagens-perfil-app";
    private static final String STORAGE_UPLOAD_URL = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        fotoPerfil = findViewById(R.id.fotoPerfil);
        home = findViewById(R.id.homePerfil);
        carrinho = findViewById(R.id.carrinhoPerfil);
        perfilPerfil = findViewById(R.id.perfilPerfil);
        historico = findViewById(R.id.historico);

        nomeUsuario = findViewById(R.id.nomeUsuario);

        // tenta carregar a imagem e o nome salvo no banco
        loadProfile();

        historico.setOnClickListener(v -> {
            Intent irParaHistorico = new Intent(Perfil.this, HistoricoActivity.class);
            startActivity(irParaHistorico);
        });

        fotoPerfil.setOnClickListener(v -> {
            openImageChooser();
        });

        // ações da NavBar
        home.setOnClickListener(v -> {
            Intent irParaTelaInicial = new Intent(Perfil.this, TelaInicial.class);
            startActivity(irParaTelaInicial);
        });

        carrinho.setOnClickListener(v -> {
            Intent irParaCarrinho = new Intent(Perfil.this, Carrinho.class);
            startActivity(irParaCarrinho);
        });

        perfilPerfil.setOnClickListener(v -> {
            Toast.makeText(Perfil.this, "Você já está no seu perfil!", Toast.LENGTH_SHORT).show();
        });
    }

    // MÉTODOS DE AUTENTICAÇÃO

    private String getUserId() {
        SharedPreferences prefs = getSharedPreferences("PreferenciasUsuario", MODE_PRIVATE);
        return prefs.getString("userId", null);
    }

    private String getAuthToken() {
        SharedPreferences prefs = getSharedPreferences("PreferenciasUsuario", MODE_PRIVATE);
        return prefs.getString("auth_token", null);
    }

    // lógica para carregar o perfil, URL da imagem e nome

    private void loadProfile() {
        final String userId = getUserId();
        final String authToken = getAuthToken();

        if (userId == null || userId.isEmpty() || authToken == null || authToken.isEmpty()) {
            fotoPerfil.setImageResource(R.drawable.perfil);
            nomeUsuario.setText("Visitante"); // Define um nome padrão
            return;
        }

        // endpoint para buscar profile_url e nome do usuário logado
        String url = SUPABASE_URL + "/rest/v1/users_app?select=profile_url,name&id=eq." + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            if (response.length() > 0) {
                                JSONObject user = response.getJSONObject(0);

                                String nome = user.getString("name");
                                nomeUsuario.setText(nome);

                                // CARREGA A FOTO
                                String profileUrl = user.optString("profile_url", null);

                                if (profileUrl != null && !profileUrl.isEmpty()) {
                                    Glide.with(Perfil.this)
                                            .load(profileUrl)
                                            .placeholder(R.drawable.perfil)
                                            .error(R.drawable.perfil)
                                            .into(fotoPerfil);
                                    Log.d("LOAD_PROFILE", "Imagem carregada com sucesso do URL: " + profileUrl);
                                } else {
                                    fotoPerfil.setImageResource(R.drawable.perfil);
                                }
                            } else {
                                fotoPerfil.setImageResource(R.drawable.perfil);
                                nomeUsuario.setText("Usuário Não Encontrado");
                            }
                        } catch (JSONException e) {
                            Log.e("LoadProfile", "Erro no parsing da resposta: " + e.getMessage());
                            fotoPerfil.setImageResource(R.drawable.perfil);
                            nomeUsuario.setText("Erro de Carga");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("LoadProfile", "Erro ao buscar perfil: " + error.toString());
                        fotoPerfil.setImageResource(R.drawable.perfil);
                        nomeUsuario.setText("Erro de Rede");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                headers.put("apikey", SUPABASE_ANON_KEY);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    // lógica de seleção, leitura e Upload

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // lógica de upload de imagem selecionada e atualização do perfil
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            fotoPerfil.setImageURI(selectedImageUri);

            uploadProfilePicture(selectedImageUri);
        }
    }

    // lógica para converter a imagem selecionada em bytes
    private byte[] getBytesFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) return null;

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        inputStream.close();
        return byteBuffer.toByteArray();
    }


    // lógica para upload da imagem selecionada
    private void uploadProfilePicture(Uri imageUri) {
        final String userId = getUserId();
        final String authToken = getAuthToken();

        // Verifica se os dados de autenticação estão presentes
        if (userId == null || userId.isEmpty() || authToken == null || authToken.isEmpty()) {
            Toast.makeText(this, "Erro: Usuário não logado ou token de acesso inválido. Verifique o salvamento.", Toast.LENGTH_LONG).show();
            return;
        }

        // cria uma nova thread para o upload em segundo plano
        new Thread(() -> {
            try {
                final byte[] imageBytes = getBytesFromUri(imageUri);
                if (imageBytes == null) {
                    runOnUiThread(() -> Toast.makeText(Perfil.this, "Falha ao ler o arquivo de imagem.", Toast.LENGTH_LONG).show());
                    return;
                }

                final String filePath = userId + "/profile_picture.jpg";
                final String uploadUrl = STORAGE_UPLOAD_URL + filePath;
                final String mimeType = getContentResolver().getType(imageUri);

                BinaryUploadRequest request = new BinaryUploadRequest(
                        Request.Method.PUT,
                        uploadUrl,
                        imageBytes,
                        mimeType,
                        new Response.Listener<byte[]>() {
                            @Override
                            public void onResponse(byte[] response) {
                                runOnUiThread(() -> {
                                    Toast.makeText(Perfil.this, "Upload da imagem concluído. Atualizando perfil...", Toast.LENGTH_SHORT).show();
                                    updateProfileUrl(filePath);
                                });
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Upload", "Erro no upload para Supabase: " + error.toString());
                                runOnUiThread(() -> Toast.makeText(Perfil.this, "Falha no upload! Verifique as políticas de Storage. Código: " + (error.networkResponse != null ? error.networkResponse.statusCode : "N/A"), Toast.LENGTH_LONG).show());
                            }
                        }) {

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Bearer " + authToken);
                        headers.put("apikey", SUPABASE_ANON_KEY);
                        headers.put("Content-Type", mimeType != null ? mimeType : "image/jpeg");
                        headers.put("x-upsert", "true");
                        return headers;
                    }
                };

                Volley.newRequestQueue(Perfil.this).add(request);

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(Perfil.this, "Erro ao preparar o upload: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }


    // lógica para atualizar a URL do perfil no banco de dados
    private void updateProfileUrl(String filePath) {
        String userId = getUserId();
        String authToken = getAuthToken();

        if (userId == null || userId.isEmpty() || authToken == null || authToken.isEmpty()) {
            Log.e("UpdateProfile", "Dados de autenticação ausentes na atualização do PostgREST.");
            return;
        }

        String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + filePath;
        String profileUpdateUrl = SUPABASE_URL + "/rest/v1/users_app?id=eq." + userId;

        final JSONObject body = new JSONObject();
        try {
            body.put("profile_url", publicUrl);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.PATCH, profileUpdateUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(Perfil.this, "Foto de perfil atualizada com sucesso no banco de dados!", Toast.LENGTH_LONG).show();
                        // Recarrega o perfil para buscar o novo URL e NOME
                        loadProfile();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("UpdateProfile", "Erro REAL ao atualizar perfil: " + error.toString());
                        Toast.makeText(Perfil.this, "Erro do servidor ao atualizar URL. Status: " + (error.networkResponse != null ? error.networkResponse.statusCode : "N/A"), Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                headers.put("apikey", SUPABASE_ANON_KEY);
                headers.put("Content-Type", "application/json");
                headers.put("Prefer", "return=minimal");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}