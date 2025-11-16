package com.example.teste;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.HistoricoViewHolder> {
    private final List<ItemHistorico> itens;

    public HistoricoAdapter(List<ItemHistorico> itens) {
        this.itens = itens;
    }

    @NonNull
    @Override
    public HistoricoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historico, parent, false);
        return new HistoricoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoricoViewHolder holder, int position) {
        ItemHistorico item = itens.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class HistoricoViewHolder extends RecyclerView.ViewHolder {
        private final TextView pedidoIdTextView; // ⬅️ NOVO: TextView para o ID
        private final TextView dataTextView;
        private final TextView valorTextView;

        public HistoricoViewHolder(@NonNull View itemView) {
            super(itemView);
            pedidoIdTextView = itemView.findViewById(R.id.pedido_id); // ⬅️ Vincula o novo ID
            dataTextView = itemView.findViewById(R.id.data);
            valorTextView = itemView.findViewById(R.id.valor);
        }

        public void bind(ItemHistorico item) {

            // Exibe o ID do pedido
            pedidoIdTextView.setText(String.format(Locale.getDefault(), "Pedido #%d", item.getId()));

            Date date = new Date(item.getData());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            sdf.setTimeZone(TimeZone.getTimeZone("GMT-3"));

            dataTextView.setText(sdf.format(date));

            valorTextView.setText(String.format(Locale.getDefault(), "Total: R$ %.2f", item.getPrecoTotal()));
        }
    }
}