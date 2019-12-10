package com.example.stockwatch;

import android.net.Uri;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class AsyncSymCoName extends AsyncTask<String, Void, String> {

    private MainActivity mainActivity;
    private static final String DATA_URL = "https://api.iextrading.com/1.0/ref-data/symbols";
    private static final String TAG = "AsyncSymCoName";
    private HashMap<String, String> wData = new HashMap<>();

    AsyncSymCoName(MainActivity mainAct){mainActivity = mainAct;}

    /*
    When this is done it will update a global variable with a hashmap on mainactivity. That is what the updateData function will do that I have not implemented yet.
     */
    @Override
    protected void onPostExecute(String s) {
        if (s == null)
            mainActivity.updateData(null);
        HashMap<String, String> wData = parseJSON(s);
        mainActivity.updateData(wData);
        Toast.makeText(mainActivity, "Loaded Stocks", Toast.LENGTH_LONG);
    }

    @Override
    protected String doInBackground(String... params) {
        Uri dataUri = Uri.parse(DATA_URL);
        String urlToUse = dataUri.toString();
        Log.d(TAG, "doInBackground: " + urlToUse);

        StringBuilder sb = new StringBuilder();

        try {

            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            Log.d(TAG, "doInBackground: Response Code" + conn.getResponseCode());

            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while((line = reader.readLine()) != null) {
                sb.append(line).append('\n');

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
    protected HashMap<String, String> parseJSON(String s) {
        HashMap<String, String> stock_names_list = new HashMap<>();

        try {

            JSONArray jsonStocks = new JSONArray(s);

            for (int i=0; i < jsonStocks.length(); i++) {

                JSONObject jsonStock = (JSONObject) jsonStocks.get(i);

                String ticker = jsonStock.getString("symbol");
                String company_name = jsonStock.getString("name");

                stock_names_list.put(ticker, company_name);
            }
        } catch (Exception e) {
            Log.d(TAG, "parseJSON: BAD JSON RETURN");
            return null;
        }

        return stock_names_list;

    }




}
