package com.example.teste;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    Context context;
    ArrayList<Integer> arrayList;
    // construtor da classe
    public ImageAdapter(Context context, ArrayList<Integer> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }
    // métodos da classe RecyclerView.Adapter para lidar com a exibição das imagens
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_list_item, parent, false);
        return new ViewHolder(view);
    }
    // métodos para vincular os dados da imagem à view holder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(arrayList.get(position)).into(holder.imageView);
    }
    // métodos para obter o número de itens na lista
    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    // classe para representar a view holder do item da lista de imagens
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        // construtor da classe com a view
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.list_item_image);
        }
    }

}