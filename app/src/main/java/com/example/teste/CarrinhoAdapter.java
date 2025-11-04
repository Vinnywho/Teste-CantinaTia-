// CarrinhoAdapter.java
package com.example.teste;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class CarrinhoAdapter extends RecyclerView.Adapter<CarrinhoAdapter.CarrinhoViewHolder> {

    private final List<ItemCarrinho> itens;
    private final OnItemRemoveListener removeListener;


    public interface OnItemRemoveListener {
        void onItemRemove(int position);
    }

    public CarrinhoAdapter(List<ItemCarrinho> itens, OnItemRemoveListener listener) {
        this.itens = itens;
        this.removeListener = listener;
    }

    @NonNull
    @Override
    public CarrinhoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carrinho, parent, false);
        return new CarrinhoViewHolder(view, removeListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CarrinhoViewHolder holder, int position) {
        ItemCarrinho item = itens.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class CarrinhoViewHolder extends RecyclerView.ViewHolder {
        private final TextView nomeProduto;
        private final TextView qtdProduto;
        private final TextView precoProduto;
        private final ImageView btnRemover;

        public CarrinhoViewHolder(@NonNull View itemView, OnItemRemoveListener removeListener) {
            super(itemView);
            nomeProduto = itemView.findViewById(R.id.txtNomeProdutoCarrinho);
            qtdProduto = itemView.findViewById(R.id.txtQtdProdutoCarrinho);
            precoProduto = itemView.findViewById(R.id.txtPrecoProdutoCarrinho);
            btnRemover = itemView.findViewById(R.id.btnRemoverItem);

            btnRemover.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && removeListener != null) {
                    removeListener.onItemRemove(position);
                }
            });
        }

        public void bind(ItemCarrinho item) {
            nomeProduto.setText(item.getProduto().getNome());
            qtdProduto.setText("x" + item.getQuantidade());
            precoProduto.setText(String.format(Locale.getDefault(), "R$ %.2f", item.getPrecoTotal()));
        }
    }
}
