package com.example.stockwatch;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class StockViewHolder extends RecyclerView.ViewHolder{

    public TextView ticker;
    public TextView price;
    public TextView company_name;
    public TextView deltas;

    public StockViewHolder(View v) {
        super(v);
        ticker = itemView.findViewById(R.id.ticker);
        price = itemView.findViewById(R.id.price);
        company_name = itemView.findViewById(R.id.company_name);
        deltas = itemView.findViewById(R.id.deltas);

    }
}
