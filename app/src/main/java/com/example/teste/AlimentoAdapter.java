package com.example.teste;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AlimentoAdapter extends RecyclerView.Adapter<AlimentoAdapter.AlimentoViewHolder> {

    // 1. Interface de Click para comunicar com a Activity
    public interface OnItemClickListener {
        void onAdicionarClick(Produto produto);
    }

    private final List<Produto> listaProdutos;
    private final Context context;
    private final OnItemClickListener listener; // Referência da Activity

    // Construtor modificado para receber o Listener
    public AlimentoAdapter(Context context, List<Produto> listaProdutos, OnItemClickListener listener) {
        this.context = context;
        this.listaProdutos = listaProdutos;
        this.listener = listener;
    }

    // Cria a View Holder (Infla o XML do Card)
    @NonNull
    @Override
    public AlimentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card_alimento, parent, false);
        return new AlimentoViewHolder(view);
    }

    // Vincula os Dados à View (Popula o Card)
    @Override
    public void onBindViewHolder(@NonNull AlimentoViewHolder holder, int position) {
        Produto produto = listaProdutos.get(position);
        int estoque = produto.getEstoque();

        holder.nomeAlimento.setText(produto.getNome());
        holder.precoAtual.setText(String.format("R$ %.2f", produto.getPrecoAtual()));

        // Define o EMOJI
        holder.emojiView.setText(produto.getEmoji());

        holder.txtEstoque.setText("Disponível: " + estoque);

        // Lógica de clique que notifica a Activity
        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAdicionarClick(produto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaProdutos.size();
    }

    // Classe ViewHolder que referencia os componentes do item
    public static class AlimentoViewHolder extends RecyclerView.ViewHolder {
        TextView emojiView;
        TextView nomeAlimento, precoAtual, txtEstoque;
        Button btnAdd;

        public AlimentoViewHolder(@NonNull View itemView) {
            super(itemView);
            emojiView = itemView.findViewById(R.id.img_alimento_emoji);
            nomeAlimento = itemView.findViewById(R.id.txt_nome_alimento);
            precoAtual = itemView.findViewById(R.id.txt_preco_atual);
            txtEstoque = itemView.findViewById(R.id.txt_estoque);
            btnAdd = itemView.findViewById(R.id.btn_adicionar);
        }
    }
}