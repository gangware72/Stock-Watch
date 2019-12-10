package com.example.stockwatch;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class AsynchIEX extends AsyncTask<String, Void, String> {
    private static final String iex_base_url = "https://cloud.iexapis.com/stable/stock/";
    private static final String secrete_key = "/quote?token=sk_5a6fa680dee340979a166a54a5fe3acd";
    private MainActivity mainActivity;
    private static final String TAG = "AsynchIEX";
    private String ticker;
    private String BASE_URL;

    public AsynchIEX(MainActivity mainAct) { mainActivity = mainAct; }

    /*
    When this is done it will update a global variable with a hashmap on mainactivity. That is what the updateData function will do that I have not implemented yet.
     */
    @Override
    protected void onPostExecute(String s) {
        if (s == null)
            mainActivity.updateData(null);
        Stock wData = parseJSON(s);
        mainActivity.updateStocks(wData);

    }

    @Override
    protected String doInBackground(String... params) {

        BASE_URL = iex_base_url + params[0].trim() + secrete_key;
        Log.d(TAG, "doInBackground: BASE_URL: " + BASE_URL.toString());
        Uri dataUri = Uri.parse(BASE_URL);
        String urlToUse = dataUri.toString();

        StringBuilder sb = new StringBuilder();

        try {

            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            Log.d(TAG, "doInBackground: Response Code" + conn.getResponseCode());

            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);

            }

            Log.d(TAG, "doInBackground: The return string" + sb.toString());

        } catch (Exception e) {

            Log.d(TAG, "doInBackground: EXCEPTION" + e);
            return null;
        }


        return sb.toString();
    }

    /*
    JSON PARESES URL DATA INTO STOCK ARRAY
     Should return an empty array if no connection is made or there is no available data
     */
    protected Stock parseJSON(String s) {
        ArrayList<Stock> stock_names_list = new ArrayList<>();
        Stock stock_info = new Stock();

        try {

            JSONObject jsonStocks = new JSONObject(s);


            stock_info.setSymbol(jsonStocks.getString("symbol"));
            stock_info.setCompany_name(jsonStocks.getString("companyName"));
            stock_info.setPrice_delta(jsonStocks.getDouble("change"));
            stock_info.setPrice(jsonStocks.getDouble("latestPrice"));
            stock_info.setDelta_percentage(jsonStocks.getDouble("changePercent"));


        } catch (Exception e) {
            Log.d(TAG, "parseJSON: BAD JSON RETURN");
            return null;
        }

        return stock_info;

    }
}
