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
    // declaração de variáveis
    private final List<ItemCarrinho> itens;
    private final OnItemRemoveListener removeListener;

    // declaração de interface para lidar com cliques no botão de remover
    public interface OnItemRemoveListener {
        void onItemRemove(int position);
    }
    // construtor da classe com a lista de itens e o listener para lidar com cliques no botão de remover
    public CarrinhoAdapter(List<ItemCarrinho> itens, OnItemRemoveListener listener) {
        this.itens = itens;
        this.removeListener = listener;
    }

    // métodos da classe RecyclerView.Adapter e ViewHolder para lidar com a exibição dos itens do carrinho
    @NonNull
    @Override
    public CarrinhoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carrinho, parent, false);
        return new CarrinhoViewHolder(view, removeListener);
    }
    // métodos para vincular os dados do item à view holder e lidar com cliques no botão de remover
    @Override
    public void onBindViewHolder(@NonNull CarrinhoViewHolder holder, int position) {
        ItemCarrinho item = itens.get(position);
        holder.bind(item);
    }
    // métodos para obter o número de itens na lista
    @Override
    public int getItemCount() {
        return itens.size();
    }
    // classe para representar a view holder do item da lista de itens do carrinho
    static class CarrinhoViewHolder extends RecyclerView.ViewHolder {
        private final TextView nomeProduto;
        private final TextView qtdProduto;
        private final TextView precoProduto;
        private final ImageView btnRemover;
        // construtor da classe com a view e o listener para lidar com cliques no botão de remover
        public CarrinhoViewHolder(@NonNull View itemView, OnItemRemoveListener removeListener) {
            super(itemView);
            nomeProduto = itemView.findViewById(R.id.txtNomeProdutoCarrinho);
            qtdProduto = itemView.findViewById(R.id.txtQtdProdutoCarrinho);
            precoProduto = itemView.findViewById(R.id.txtPrecoProdutoCarrinho);
            btnRemover = itemView.findViewById(R.id.btnRemoverItem);

            // lidar com cliques no botão de remover
            btnRemover.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && removeListener != null) {
                    removeListener.onItemRemove(position);
                }
            });
        }
        // método para vincular os dados do item à view
        public void bind(ItemCarrinho item) {
            nomeProduto.setText(item.getProduto().getNome());
            qtdProduto.setText("x" + item.getQuantidade());
            precoProduto.setText(String.format(Locale.getDefault(), "R$ %.2f", item.getPrecoTotal()));
        }
    }
}
