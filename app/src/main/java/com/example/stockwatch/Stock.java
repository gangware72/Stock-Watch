package com.example.stockwatch;

import java.util.Comparator;

public class Stock implements Comparable<Stock>{

    private String symbol;
    private String company_name;
    private double price;
    private double price_delta;
    private double delta_percentage;

    public Stock() {
        super();

    }


    public String getSymbol() {

        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice_delta() {
        return price_delta;
    }

    public void setPrice_delta(double price_delta) {
        this.price_delta = price_delta;
    }

    public double getDelta_percentage() {
        return delta_percentage;
    }

    public void setDelta_percentage(double delta_percentage) {
        this.delta_percentage = delta_percentage;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "symbol='" + symbol + '\'' +
                ", company_name='" + company_name + '\'' +
                ", price=" + price +
                ", price_delta=" + price_delta +
                ", delta_percentage=" + delta_percentage +
                '}';
    }

    @Override
public int compareTo(Stock stock) {
    return this.symbol.compareTo(stock.getSymbol());
}

    public static Comparator<Stock> StockComparator =
            new Comparator<Stock>() {
                @Override
                public int compare(Stock stock1, Stock stock2) {
                    return stock1.compareTo(stock2);
                }
            };


}
