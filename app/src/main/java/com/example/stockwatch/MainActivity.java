package com.example.stockwatch;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.coordinatorlayout.widget.DirectedAcyclicGraph;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.renderscript.ScriptGroup;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import com.example.stockwatch.R;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "MainActivity";


    private ArrayList<Stock> stock_list = new ArrayList<Stock>();
    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private SwipeRefreshLayout swiper;
    private DatabaseHandler databaseHandler;
    private HashMap<String, String> search_stocks;
    private MainActivity mainAct = this;




    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stockAdapter = new StockAdapter(stock_list, this);
        Window window = mainAct.getWindow();
        window.setStatusBarColor(Color.BLACK);

        recyclerView = findViewById(R.id.recycler);
        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

        new AsyncSymCoName(this).execute();
        //new AsynchIEX(this;

        databaseHandler = new DatabaseHandler(this);



        if(!databaseHandler.loadStocks().isEmpty()) {
            for (String[] stored_stock : databaseHandler.loadStocks()) {
                Stock stock = new Stock();
                stock.setSymbol(stored_stock[0]);
                stock.setCompany_name(stored_stock[1]);
                stock_list.add(stock);
                new AsynchIEX(this).execute(stored_stock[0]);


            }
        }

        //loading stocks


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            wrapNotifyAdapter();//stockAdapter.notifyDataSetChanged();
        }

        //ADD NOTES HERE
    }


    //AT LEAST LOADING COUNTRIES FROM THE DATABASE ON TO RECYCLER VIEW, POSSIBLY CLEARING THE RECYCLER VIEW
    @Override
    protected void onResume() {
        databaseHandler.dumpDbToLog();

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        databaseHandler.shutDown();
        super.onDestroy();
    }

    //CREATES MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    /**
     * This method is activated by clicking on the plus icon on the action menu
     * Prompts a dialog box that allows the entry of only capital letters to search for a company to watch
     *
     */
    public void searchCompanyToWatch(MenuItem item) {
        Log.d(TAG, "searchCompanyToWatch: MENU METHOD CALLEd");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stock Selection");
        builder.setMessage("Please enter a Stock Symbol:");

        final EditText edittext = new EditText(this);
        edittext.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        edittext.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        builder.setView(edittext);


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                if (!hasNetworkConnection()) {
                    responseDialog(-1, new ArrayList<String[]>(), null);
                } else {
                    ArrayList<String[]> matches = new ArrayList<>();
                    StringBuilder search = new StringBuilder(edittext.getText().toString());
                    //if the search ticker is already in the stock array call response function and pass a 3

                    for (String symbol : search_stocks.keySet()) {
                        StringBuilder ticker = new StringBuilder(symbol);


                        if (search.length() <= ticker.length() && search.length() != 0 && search.toString().trim().equals(ticker.substring(0, search.length()))) {
                            matches.add(new String[]{ticker.toString(), search_stocks.get(ticker.toString())});
                        }
                    }

                    int results_length = matches.size();//Search and results

                    if (results_length > 1)
                        results_length = 2;

                    if (results_length == 1) {
                        Stock new_stock = new Stock();
                        String ticker = matches.get(0)[0].toString();
                        String company_name = matches.get(0)[0].toString();
                        new_stock.setCompany_name(company_name);
                        new_stock.setSymbol(ticker);
                        stock_list.add(new_stock);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            wrapNotifyAdapter();
                        }
                        //stockAdapter.notifyDataSetChanged();
                        databaseHandler.addStock(new_stock);
                        executeAsyncTask(new_stock.getSymbol());



                    } else {

                        responseDialog((int) results_length, matches, edittext.getText().toString());

                }                    }


        }});
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }

    public void responseDialog(int results_length, ArrayList<String[]> matches, String search){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        for (String[] match: matches)
        Log.d(TAG, "responseDialog: MATCHES " + match[0] + "-" + match[1]);

        switch(results_length) {

            case -1:
                builder.setTitle("No Network Connection");//if none tell the user or if no internet connection tell the user
                builder.setMessage("Stocks Cannot Be Added Without A Network Connection");
                break;
            case 0: //Also set for not found or network connectivity error
                builder.setTitle("Symbol Not Found: " + search);//if none tell the user or if no internet connection tell the user
                builder.setMessage("Data for Stock Symbol");
                break;

            case 2:

                builder.setTitle("Make a selection");
                final String[] list = new String[matches.size()];

                for (int i=0; i< matches.size(); i++)
                    list[i] = matches.get(i)[0] + " - " +  matches.get(i)[1]; //come back here,
                Arrays.sort(list);

                builder.setItems(list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Stock new_stock = new Stock();
                        String info = list[i];
                        String ticker = info.substring(0, info.indexOf(" - ")).trim();
                        String company_name = info.substring(info.indexOf(" - ") + 1, info.length()).trim();

                        Boolean already_added = false;
                        for (Stock stock: stock_list) { //check if stock already exists
                            String stock_ticker = stock.getSymbol().toString(); //are we not leaving out a search for longer fbs is facebook is already chosen?
                            if (ticker.toString().trim().equals(stock_ticker))
                                already_added = true;
                            break;

                        }

                        if (already_added) {

                            popDuplicateDialog(ticker);

                        } else {
                            Log.d(TAG, "onClick: TICKERNAME " + ticker + "CONAME " + company_name);
                            new_stock.setSymbol(ticker);
                            new_stock.setCompany_name(company_name);
                            stock_list.add(new_stock);
                            databaseHandler.addStock(new_stock);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                wrapNotifyAdapter();
                            }
                            //stockAdapter.notifyDataSetChanged();
                            executeAsyncTask(new_stock.getSymbol());
                        }


                        //Parse back to ticker and company name and create stock
                    }
                });

                builder.setNegativeButton("NEVERMIND", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //just return
                    }
                });


                //if one company place it in and update recycler view
                //add it to the db
                break;
            case 3:

                builder.setTitle("Duplicate Stock");
                Drawable unwrappedDrawable = AppCompatResources.getDrawable(this, android.R.drawable.stat_sys_warning);
                Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                DrawableCompat.setTint(wrappedDrawable, Color.BLACK);
                builder.setIcon(wrappedDrawable);
                builder.setMessage("Stock Symbol " + search + " is already displayed");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + results_length);
        }
        AlertDialog dialog = builder.create();
        dialog.show();


    }

    public void popDuplicateDialog(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Duplicate Stock");
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(this, android.R.drawable.stat_sys_warning);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Color.BLACK);
        builder.setIcon(wrappedDrawable);
        builder.setMessage("Stock Symbol " + s + " is already displayed");
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    //for the short click on the recycler view
    public void onClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        Stock stock = stock_list.get(pos);
        String marketWatch_url = "http://www.marketwatch.com/investing/stock/" + stock.getSymbol().trim();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(marketWatch_url));
        startActivity(i);

    }

    //For the long click of the recycler view
    public boolean onLongClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        final Stock stock = stock_list.get(pos);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setIcon(R.drawable.warning_background);
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stock_list.remove(stock);
                databaseHandler.deleteStock(stock.getSymbol());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    wrapNotifyAdapter();//stockAdapter.notifyDataSetChanged();
                }
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setTitle("Delete Stock");
        builder.setMessage("Delete Stock Symbol \'" + stock.getCompany_name() + "?\'");
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(this, android.R.drawable.ic_menu_delete);

        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Color.BLACK);
        builder.setIcon(wrappedDrawable);
        AlertDialog dialog = builder.create();
        dialog.show();

        return false;
    }

    //Handles the SRL response once activated
    public void doRefresh() {
        swiper.setRefreshing(true); //turn off wheel

        boolean isConnected = hasNetworkConnection();

        if (!isConnected) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");//if none tell the user or if no internet connection tell the user
            builder.setMessage("Stocks Cannot Be Added Without A Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {

            for (Stock stock : stock_list)
                new AsynchIEX(this).execute(stock.getSymbol());
        }

        swiper.setRefreshing(false);


    }
    
    public void updateData(HashMap<String, String> snames) {


        search_stocks = snames;


    }

    public void updateStocks(Stock stock_info) {
        if (stock_info == null)
            return;
        Boolean found = false;


//        for (Stock stock : stock_list) {
        for (int i=0; i < stock_list.size(); i++) {
            Stock stock = stock_list.get(i);
            if (stock != null && stock.getSymbol().equals(stock_info.getSymbol())) {
                stock.setDelta_percentage(stock_info.getDelta_percentage());
                stock.setPrice(stock_info.getPrice());
                stock.setPrice_delta(stock_info.getPrice_delta());
                stock.setCompany_name(stock_info.getCompany_name());
                stock.setSymbol(stock_info.getSymbol());
                found = true;

            }
        if (found) {
            break;
        }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            wrapNotifyAdapter();//stockAdapter.notifyDataSetChanged();
        }


    }

    public boolean hasNetworkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null) {
            Toast.makeText(this, "Cannot access ConnectivityManager", Toast.LENGTH_SHORT).show();
            return false;
        }

        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected())
            return true;
        return false;

    }

    public void executeAsyncTask(String s) {
        new AsynchIEX(this).execute(s);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void wrapNotifyAdapter() {
        Collections.sort(stock_list);
        stockAdapter.notifyDataSetChanged();

    }

}
