package com.example.stockwatch;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {

    private static final String TAG = "StockAdapter";
    private ArrayList<Stock> stock_list;
    private MainActivity mainAct;

    public StockAdapter(ArrayList<Stock> stocks, MainActivity mact){
        this.stock_list = stocks;
        mainAct = mact;

    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: MAKING NEW STOCKVIEWHOLDER");

        View stockView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_view, parent, false);

        stockView.setOnClickListener(mainAct);
        stockView.setOnLongClickListener(mainAct);

        return new StockViewHolder(stockView);

    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: FILLING VIEW HOLDER AT POSITION" + position);

        Stock stock = stock_list.get(position);
        Log.d(TAG, "onBindViewHolder: COMPANY NAME " + stock.getCompany_name());
        Log.d(TAG, "onBindViewHolder: COMAPNY PRICE" + stock.getPrice());
        holder.company_name.setText(stock.getCompany_name().replace('-', ' ').trim());
        holder.ticker.setText(stock.getSymbol());

        holder.price.setText(Double.toString(stock.getPrice()));
        Double percentage = stock.getDelta_percentage()* 100;

        //Double.toString(stock.getPrice_delta()) + "▲  (" + Double.toString(stock.getDelta_percentage() * 100) + "%)";
        if (stock.getPrice_delta() < 0) {
            String deltas = "▼" + Double.toString(stock.getPrice_delta()) + " (" + String.format("%.2f", percentage) + "%)";
            holder.company_name.setTextColor(Color.parseColor("#bd0f09"));
            holder.ticker.setTextColor(Color.parseColor("#bd0f09"));
            holder.price.setTextColor(Color.parseColor("#bd0f09"));
            holder.deltas.setTextColor(Color.parseColor("#bd0f09"));
            holder.deltas.setText(deltas);
        } else {

            String deltas = "▲" + Double.toString(stock.getPrice_delta()) + " (" +  String.format("%.2f", percentage) + "%)";
            holder.company_name.setTextColor(Color.parseColor("#2DC52D"));
            holder.ticker.setTextColor(Color.parseColor("#2DC52D"));
            holder.price.setTextColor(Color.parseColor("#2DC52D"));
            holder.deltas.setTextColor(Color.parseColor("#2DC52D"));
            holder.deltas.setText(deltas);
        }

    }

    @Override
    public int getItemCount() { return stock_list.size();}


}
