package com.example.teste;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;


public class AlimentoAdapter extends RecyclerView.Adapter<AlimentoAdapter.AlimentoViewHolder> implements Filterable {

    public interface OnItemClickListener {
        void onAdicionarClick(Produto produto);
    }

    private List<Produto> listaProdutos;
    private final List<Produto> listaProdutosCompleta;
    private final Context context;
    private final OnItemClickListener listener;

    public AlimentoAdapter(Context context, List<Produto> listaProdutos, OnItemClickListener listener) {
        this.context = context;
        this.listaProdutos = listaProdutos;
        this.listaProdutosCompleta = new ArrayList<>(listaProdutos);
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlimentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card_alimento, parent, false);
        return new AlimentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlimentoViewHolder holder, int position) {
        Produto produto = listaProdutos.get(position);
        int estoque = produto.getEstoque();

        holder.nomeAlimento.setText(produto.getNome());
        holder.precoAtual.setText(String.format("R$ %.2f", produto.getPrecoAtual()));

        holder.emojiView.setText(produto.getEmoji());

        holder.txtEstoque.setText("Disponível: " + estoque);

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

    @Override
    public Filter getFilter() {
        return foodFilter;
    }

    private final Filter foodFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Produto> listaFiltrada = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                listaFiltrada.addAll(listaProdutosCompleta);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Produto item : listaProdutosCompleta) {
                    if (item.getNome().toLowerCase().contains(filterPattern)) {
                        listaFiltrada.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = listaFiltrada;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            listaProdutos.clear();
            listaProdutos.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

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