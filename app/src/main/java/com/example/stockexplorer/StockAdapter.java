package com.example.stockexplorer;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private List<StockRecord> stockList;

    public StockAdapter(List<StockRecord> stockList) {
        this.stockList = stockList != null ? stockList : new ArrayList<>();
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock_record, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        StockRecord record = stockList.get(position);

        holder.symbolText.setText(record.getSymbol());
        holder.dateText.setText(record.getDate());
        holder.openText.setText(String.format(Locale.US, "Open: %.2f", record.getOpen()));
        holder.closeText.setText(String.format(Locale.US, "Close: %.2f", record.getClose()));
        holder.highText.setText(String.format(Locale.US, "High: %.2f", record.getHigh()));
        holder.lowText.setText(String.format(Locale.US, "Low: %.2f", record.getLow()));
        holder.volumeText.setText(String.format(Locale.US, "Volume: %,d", record.getVolume()));

        boolean isUp = record.getClose() >= record.getOpen();
        int colorRes = isUp ? R.color.stock_up : R.color.stock_down;
        int bgColorRes = isUp ? R.color.stock_up_bg : R.color.stock_down_bg;
        holder.trendText.setText(isUp ? "▲ Up" : "▼ Down");
        holder.trendText.setTextColor(
                ContextCompat.getColor(holder.itemView.getContext(), colorRes));

        GradientDrawable badge = new GradientDrawable();
        badge.setColor(ContextCompat.getColor(holder.itemView.getContext(), bgColorRes));
        badge.setCornerRadius(20f);
        holder.trendText.setBackground(badge);
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public void updateData(List<StockRecord> newData) {
        stockList = newData != null ? newData : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class StockViewHolder extends RecyclerView.ViewHolder {
        final TextView symbolText;
        final TextView trendText;
        final TextView dateText;
        final TextView openText;
        final TextView closeText;
        final TextView highText;
        final TextView lowText;
        final TextView volumeText;

        StockViewHolder(@NonNull View itemView) {
            super(itemView);
            symbolText = itemView.findViewById(R.id.symbolText);
            trendText = itemView.findViewById(R.id.trendText);
            dateText = itemView.findViewById(R.id.dateText);
            openText = itemView.findViewById(R.id.openText);
            closeText = itemView.findViewById(R.id.closeText);
            highText = itemView.findViewById(R.id.highText);
            lowText = itemView.findViewById(R.id.lowText);
            volumeText = itemView.findViewById(R.id.volumeText);
        }
    }
}
