package com.example.mystocks;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import android.graphics.Canvas;
import android.graphics.Paint;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.Collections;


import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.widget.TextView;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class HomeActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private SearchView searchView;

    private ProgressBar progressBar;

    private RecyclerView portfolioRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_main);

        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.ProgressBar);
        setSupportActionBar(toolbar);

        portfolioRecyclerView = findViewById(R.id.portfolioRecyclerView);
        portfolioRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchFavorites();
        fetchNetWorth();
        //        fetchPortfolio();
        //        fetchBalance();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh watchlist data here
        fetchFavorites();
        fetchNetWorth();
    }

    public class PortfolioStock {
        private String id;
        private String symbol;
        private String companyName;
        private int quantity;
        private double total;
        private double averageCost;
        private double currentPrice;

        public PortfolioStock(String id, String symbol, String companyName, int quantity, double total, double averageCost, double currentPrice) {
            this.id = id;
            this.symbol = symbol;
            this.companyName = companyName;
            this.quantity = quantity;
            this.total = total;
            this.averageCost = averageCost;
            this.currentPrice = currentPrice;
        }

        // Getter methods for the properties
        public String getId() {
            return id;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getCompanyName() {
            return companyName;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getTotal() {
            return total;
        }

        public double getAverageCost() {
            return averageCost;
        }

        public double getCurrentPrice() {
            return currentPrice;
        }
    }

    public class PortfolioAdapter extends RecyclerView.Adapter<HomeActivity.PortfolioAdapter.ViewHolder> implements com.example.mystocks.PortfolioAdapter {

        private Context context;
        private List<HomeActivity.PortfolioStock> portfolioStocks;

        public PortfolioAdapter(Context context, List<HomeActivity.PortfolioStock> portfolioStocks) {
            this.context = context;
            this.portfolioStocks = portfolioStocks;
        }

        @NonNull
        @Override
        public HomeActivity.PortfolioAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_portfolio_stock, parent, false);
            return new HomeActivity.PortfolioAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeActivity.PortfolioAdapter.ViewHolder holder, int position) {
            HomeActivity.PortfolioStock portfolioStock = portfolioStocks.get(position);
            holder.bind(portfolioStock);

            if (position == portfolioStocks.size() - 1) {
                // Hide the divider for the last item
                holder.dividerView.setVisibility(View.GONE);
            } else {
                // Show the divider for other items
                holder.dividerView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return portfolioStocks.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView symbolTextView;
            private TextView marketValueView;
            private TextView quantityTextView;
            private TextView changeView;
            private TextView changePercentView;
            View dividerView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                symbolTextView = itemView.findViewById(R.id.symbolTextView);
                marketValueView = itemView.findViewById(R.id.marketValueView);
                quantityTextView = itemView.findViewById(R.id.quantityTextView);
                changeView = itemView.findViewById(R.id.changeView);
                changePercentView = itemView.findViewById(R.id.changePercentView);
                dividerView = itemView.findViewById(R.id.dividerView);

                itemView.setOnClickListener(this);
            }

            // Trigger the search action with the symbol


            @Override
            public void onClick(View v) {
                // Get the symbol of the clicked portfolio item
                String symbol = symbolTextView.getText().toString();
                // Trigger the search action with the symbol
                searchView.setQuery(symbol, true);
            }

            public void bind(HomeActivity.PortfolioStock portfolioStock) {
                symbolTextView.setText(portfolioStock.getSymbol());
                marketValueView.setText(String.format("$%.2f", portfolioStock.getQuantity() * portfolioStock.getCurrentPrice()));
                changeView.setText(String.format(" $%.2f ", (portfolioStock.getCurrentPrice() - portfolioStock.getAverageCost()) * portfolioStock.getQuantity()));
                changePercentView.setText(String.format("(%.2f%% )", ((portfolioStock.getCurrentPrice() - portfolioStock.getAverageCost()) * portfolioStock.getQuantity()) /
                        (portfolioStock.getAverageCost() * portfolioStock.getQuantity()) * 100));
                quantityTextView.setText(String.format("%d shares", portfolioStock.getQuantity()));

                if ((portfolioStock.getCurrentPrice() - portfolioStock.getAverageCost()) * portfolioStock.getQuantity() > 0) {
                    changePercentView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    changeView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    getResources().getDrawable(R.drawable.trending_up).setTint(getResources().getColor(android.R.color.holo_green_dark));
                    changeView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.trending_up, 0, 0, 0);
                } else if ((portfolioStock.getCurrentPrice() - portfolioStock.getAverageCost()) * portfolioStock.getQuantity() < 0) {
                    changePercentView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    changeView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    getResources().getDrawable(R.drawable.trending_down).setTint(getResources().getColor(android.R.color.holo_red_dark));
                    changeView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.trending_down, 0, 0, 0);
                }
            }
        }
    }

    private List<HomeActivity.PortfolioStock> parsePortfolio(JSONArray jsonArray) {
        List<HomeActivity.PortfolioStock> portfolioStocks = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonStock = jsonArray.getJSONObject(i);
                String id = jsonStock.getString("_id");
                String symbol = jsonStock.getString("symbol");
                String companyName = jsonStock.optString("companyName", null);
                int quantity = jsonStock.getInt("quantity");
                double total = jsonStock.getDouble("total");
                double averageCost = jsonStock.getDouble("averageCost");
                double currentPrice = jsonStock.getDouble("currentPrice");

                HomeActivity.PortfolioStock portfolioStock = new HomeActivity.PortfolioStock(id, symbol, companyName, quantity, total, averageCost, currentPrice);
                portfolioStocks.add(portfolioStock);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("ParsedPortfolio", portfolioStocks.toString());
        return portfolioStocks;
    }

    private void fetchFavorites() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://webtech-hw3-419101.wl.r.appspot.com/watchlist";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Parse the JSON array and populate the RecyclerViewh favorites
                        Log.d("Response", "Received response: " + response.toString());
                        List<HomeActivity.FavoriteStock> favorites = parseFavorites(response);
                        RecyclerView recyclerView = findViewById(R.id.recyclerView);
                        recyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
                        HomeActivity.FavoritesAdapter adapter = new HomeActivity.FavoritesAdapter(HomeActivity.this, favorites);
                        recyclerView.setAdapter(adapter);

                        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
                            Drawable background;
                            boolean initiated = false;
                            Drawable deleteIcon;

                            private void init() {
                                background = new ColorDrawable(Color.RED); // Set the background color to red
                                deleteIcon = ContextCompat.getDrawable(HomeActivity.this, R.drawable.delete);
                                if (deleteIcon == null) {
                                    throw new NullPointerException("Delete icon drawable not found");
                                }
                                initiated = true;
                            }

                            @Override
                            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                                int swipeFlags = ItemTouchHelper.LEFT;;
                                return makeMovementFlags(dragFlags, swipeFlags);
                            }

                            @Override
                            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                                int fromPosition = viewHolder.getAdapterPosition();
                                int toPosition = target.getAdapterPosition();
                                Log.d("ItemMove", "Before move: " + favorites.toString());
                                Log.d("ItemMove", "Moving item from position " + fromPosition + " to position " + toPosition);
                                HomeActivity.FavoriteStock movedItem = favorites.remove(fromPosition);
                                // Add the item to its new position
                                favorites.add(toPosition, movedItem);
                                Log.d("ItemMove", "After move: " + favorites.toString());
                                adapter.notifyItemMoved(fromPosition, toPosition);
                                return true;
                            }

                            @Override
                            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                                // Not needed for drag and drop
                                int position = viewHolder.getBindingAdapterPosition();
                                if (position != RecyclerView.NO_POSITION && !favorites.isEmpty()) {
                                    HomeActivity.FavoriteStock deletedItem = favorites.get(position);
                                    deleteFavoriteFromBackend(deletedItem);
                                    adapter.notifyItemRemoved(position);
                                    favorites.remove(position);
                                    Log.d("SwipeAction", "Item swiped away at position: " + position);
                                } else {
                                    Log.e("SwipeAction", "Invalid position or empty list");
                                }
                            }
                            @Override
                            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                                if (!initiated) {
                                    init();
                                }
                                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                                    View itemView = viewHolder.itemView;
                                    int itemHeight = itemView.getHeight();

                                    background.setBounds(
                                            itemView.getRight() + (int) dX,
                                            itemView.getTop(),
                                            itemView.getRight(),
                                            itemView.getBottom()
                                    );
                                    background.draw(c);

                                    if (-dX >= deleteIcon.getIntrinsicWidth()) {
                                        int intrinsicWidth = deleteIcon.getIntrinsicWidth();
                                        int intrinsicHeight = deleteIcon.getIntrinsicHeight();
                                        int iconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                                        int iconMargin = (itemHeight - intrinsicHeight) / 2;
                                        int iconLeft = itemView.getRight() - iconMargin - intrinsicWidth;
                                        int iconRight = itemView.getRight() - iconMargin;
                                        int iconBottom = iconTop + intrinsicHeight;

                                        if (dX < 0) {
                                            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                                            deleteIcon.draw(c);
                                        }

                                    }
                                }
                                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                            }
                        };
                        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
                        itemTouchHelper.attachToRecyclerView(recyclerView);
//                        displayFavorites(favorites);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomeActivity.this, "Error fetching favorites", Toast.LENGTH_SHORT).show();
                        Log.e("FavoritesError", "Error fetching favorites", error);
                    }
                });

        queue.add(jsonArrayRequest);
    }

    private void deleteFavoriteFromBackend(HomeActivity.FavoriteStock deletedItem) {
        String url = "https://webtech-hw3-419101.wl.r.appspot.com/remove-from-watchlist";
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("symbol", deletedItem.getSymbol());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful deletion response from the backend
                        Log.d("DeleteResponse", "Item deleted from backend");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response from the backend
                        Log.e("DeleteError", "Error deleting item from backend", error);
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }

    public class FavoriteStock {
        private String symbol;
        private String companyName;
        private double lastPrice;
        private double change;
        private double percentChange;

        public FavoriteStock(String symbol, String companyName, double lastPrice, double change, double percentChange) {
            this.symbol = symbol;
            this.companyName = companyName;
            this.lastPrice = lastPrice;
            this.change = change;
            this.percentChange = percentChange;
        }

        // Getter methods for the properties
        public String getSymbol() {
            return symbol;
        }

        public String getCompanyName() {
            return companyName;
        }

        public double getLastPrice() {
            return lastPrice;
        }

        public double getChange() {
            return change;
        }

        public double getPercentChange() {
            return percentChange;
        }
    }

    public  class FavoritesAdapter extends RecyclerView.Adapter<HomeActivity.FavoritesAdapter.ViewHolder> {

        private Context context;
        private List<HomeActivity.FavoriteStock> favorites;

        public FavoritesAdapter(Context context, List<HomeActivity.FavoriteStock> favorites) {
            this.context = context;
            this.favorites = favorites;
        }

        @NonNull
        @Override
        public HomeActivity.FavoritesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_stock, parent, false);
            return new HomeActivity.FavoritesAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeActivity.FavoritesAdapter.ViewHolder holder, int position) {
            HomeActivity.FavoriteStock favoriteStock = favorites.get(position);
            holder.bind(favoriteStock);
            Log.d("ViewHolderBinding", "Bound item at position: " + position);
        }

        @Override
        public int getItemCount() {
            return favorites.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView symbolTextView;
            private TextView companyNameTextView;
            private TextView lastPriceTextView;
            private TextView changeTextView;
            private TextView percentChangeTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                symbolTextView = itemView.findViewById(R.id.symbolTextView);
                companyNameTextView = itemView.findViewById(R.id.companyNameTextView);
                lastPriceTextView = itemView.findViewById(R.id.lastPriceTextView);
                changeTextView = itemView.findViewById(R.id.changeTextView);
                percentChangeTextView = itemView.findViewById(R.id.percentChangeTextView);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                // Get the symbol of the clicked item
                String symbol = symbolTextView.getText().toString();

                // Trigger the search action with the symbol
                searchView.setQuery(symbol, true);
            }

            private void searchWithSymbol(String symbol) {
                // Implement the logic to perform search with the symbol
                // For example, you can start a new activity with the search query
                Intent intent = new Intent(context, SearchResultsActivity.class);
                intent.putExtra("search_results", symbol);
                context.startActivity(intent);
            }

            public void bind(HomeActivity.FavoriteStock favoriteStock) {
                symbolTextView.setText(favoriteStock.getSymbol());
                companyNameTextView.setText(favoriteStock.getCompanyName());
                lastPriceTextView.setText(String.format("$%.2f", favoriteStock.getLastPrice()));
                changeTextView.setText(String.format(" $%.2f", favoriteStock.getChange()));
                percentChangeTextView.setText(String.format("(%.2f%% )", favoriteStock.getPercentChange()));

                if (favoriteStock.getPercentChange() > 0) {
                    percentChangeTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    changeTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    getResources().getDrawable(R.drawable.trending_up).setTint(getResources().getColor(android.R.color.holo_green_dark));
                    changeTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.trending_up, 0, 0, 0);
                } else {
                    percentChangeTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    changeTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    getResources().getDrawable(R.drawable.trending_down).setTint(getResources().getColor(android.R.color.holo_red_dark));
                    changeTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.trending_down, 0, 0, 0);
                }
            }
        }
    }

    private List<HomeActivity.FavoriteStock> parseFavorites(JSONArray jsonArray) {
        List<HomeActivity.FavoriteStock> favorites = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonStock = jsonArray.getJSONObject(i);
                String symbol = jsonStock.getString("symbol");
                String companyName = jsonStock.getString("companyName");
                double lastPrice = jsonStock.getDouble("lastPrice");
                double change = jsonStock.getDouble("change");
                double percentChange = jsonStock.getDouble("percentChange");

                HomeActivity.FavoriteStock favoriteStock = new HomeActivity.FavoriteStock(symbol, companyName, lastPrice, change, percentChange);
                favorites.add(favoriteStock);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("ParsedFavorites", favorites.toString());
        return favorites;
    }

    public void openFinnhubHomePage(View view) {
        String url = "https://www.finnhub.io";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void performSearchRequest(String symbol) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://webtech-hw3-419101.wl.r.appspot.com/search";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("symbol", symbol);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject searchResponse) {
                        Log.d("SearchResponse", searchResponse.toString());

                        // Fetch quote details after successful search response
                        String quoteDetailsUrl = "https://webtech-hw3-419101.wl.r.appspot.com/quoteDetails?symbol=" + symbol;
//                        String hourlyDataUrl = "https://webtech-hw3-419101.wl.r.appspot.com/hourly?symbol=" + symbol;

                        JsonObjectRequest quoteDetailsRequest = new JsonObjectRequest(Request.Method.GET, quoteDetailsUrl, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject quoteDetails) {
                                        try {
                                            // Add quote details to search response
                                            searchResponse.put("quoteDetails", quoteDetails);
                                            Log.d("newSearchResponse", searchResponse.toString());
                                            // Start SearchResultsActivity with combined response
                                            Intent intent = new Intent(HomeActivity.this, SearchResultsActivity.class);
                                            intent.putExtra("search_results", searchResponse.toString());
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Log.e("SearchError", "Error adding quote details", e);
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("QuoteDetailsError", "Error fetching quote details", error);
                                // Handle error fetching quote details (optional: show toast or retry)
                            }
                        });

                        JsonArrayRequest portfolioRequest = new JsonArrayRequest(Request.Method.GET, "https://webtech-hw3-419101.wl.r.appspot.com/portfolio", null,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray portfolioData) {
                                        try {
                                            // Add portfolio data to search response
                                            searchResponse.put("portfolio", portfolioData);
                                            Log.d("portfolioSearchResponse", searchResponse.toString());
                                            // Start SearchResultsActivity with combined response
                                            Intent intent = new Intent(HomeActivity.this, SearchResultsActivity.class);
                                            intent.putExtra("search_results", searchResponse.toString());
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Log.e("PortfolioError", "Error adding portfolio data", e);
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("PortfolioError", "Error fetching portfolio data", error);
                                        // Handle error fetching portfolio data (optional: show toast or retry)
                                    }
                                });

                        JsonArrayRequest watchlistRequest = new JsonArrayRequest(Request.Method.GET, "https://webtech-hw3-419101.wl.r.appspot.com/watchlist", null,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray watchlistData) {
                                        try {
                                            // Add watchlist data to search response
                                            searchResponse.put("watchlist", watchlistData);
                                            Log.d("watchlistSearchResponse", searchResponse.toString());
                                            // Start SearchResultsActivity with combined response
                                            Intent intent = new Intent(HomeActivity.this, SearchResultsActivity.class);
                                            intent.putExtra("search_results", searchResponse.toString());
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Log.e("WatchlistError", "Error adding watchlist data", e);
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("WatchlistError", "Error fetching watchlist data", error);
                                        // Handle error fetching watchlist data (optional: show toast or retry)
                                    }
                                });

                        String peersUrl = "https://webtech-hw3-419101.wl.r.appspot.com/peers?q=" + symbol;

                        JsonArrayRequest peersRequest = new JsonArrayRequest(Request.Method.GET, peersUrl, null,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray peersResponse) {
                                        try {
                                            // Add peers data to search response
                                            searchResponse.put("peers", peersResponse);
                                            Log.d("peersSearchResponse", searchResponse.toString());
                                            // Start SearchResultsActivity with combined response
                                            Intent intent = new Intent(HomeActivity.this, SearchResultsActivity.class);
                                            intent.putExtra("search_results", searchResponse.toString());
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Log.e("PeersError", "Error adding peers data", e);
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("PeersError", "Error fetching peers data", error);
                                        // Handle error fetching peers data (optional: show toast or retry)
                                    }
                                });

                        String sentimentUrl = "https://webtech-hw3-419101.wl.r.appspot.com/insider-sentiment?q=" + symbol;

                        JsonArrayRequest sentimentRequest = new JsonArrayRequest(Request.Method.GET, sentimentUrl, null,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray sentimentResponse) {
                                        try {
                                            List<JSONObject> positiveSentiments = new ArrayList<>();
                                            List<JSONObject> negativeSentiments = new ArrayList<>();

                                            double totalMSPR = 0;
                                            double totalChange = 0;
                                            double positiveMSPR = 0;
                                            double negativeMSPR = 0;
                                            double positiveChange = 0;
                                            double negativeChange = 0;

                                            for (int i = 0; i < sentimentResponse.length(); i++) {
                                                JSONObject sentiment = sentimentResponse.getJSONObject(i);

                                                // Extract necessary fields
                                                double mspr = sentiment.getDouble("mspr");
                                                double change = sentiment.getDouble("change");

                                                // Update total values
                                                totalMSPR += mspr;
                                                totalChange += change;

                                                // Update positive and negative values
                                                if (mspr > 0) {
                                                    positiveSentiments.add(sentiment);
                                                    positiveMSPR += mspr;
                                                } else if (mspr < 0) {
                                                    negativeSentiments.add(sentiment);
                                                    negativeMSPR += mspr;
                                                }

                                                if (change > 0) {
                                                    positiveChange += change;
                                                } else if (change < 0) {
                                                    negativeChange += change;
                                                }
                                            }

                                            // Create JSON arrays for positive and negative sentiments
                                            JSONArray positiveSentimentsArray = new JSONArray(positiveSentiments);
                                            JSONArray negativeSentimentsArray = new JSONArray(negativeSentiments);

                                            // Add processed data to search response
//                                            searchResponse.put("insiderSentiment", sentimentResponse);
                                            searchResponse.put("totalMSPR", totalMSPR);
                                            searchResponse.put("totalChange", totalChange);
                                            searchResponse.put("positiveMSPR", positiveMSPR);
                                            searchResponse.put("negativeMSPR", negativeMSPR);
                                            searchResponse.put("positiveChange", positiveChange);
                                            searchResponse.put("negativeChange", negativeChange);
//                                            searchResponse.put("positiveSentiments", positiveSentimentsArray);
//                                            searchResponse.put("negativeSentiments", negativeSentimentsArray);

                                            Log.d("sentimentSearchResponse", searchResponse.toString());

                                            // Start SearchResultsActivity with combined response
                                            Intent intent = new Intent(HomeActivity.this, SearchResultsActivity.class);
                                            intent.putExtra("search_results", searchResponse.toString());
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Log.e("SentimentError", "Error adding sentiment data", e);
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("SentimentError", "Error fetching sentiment data", error);
                                        // Handle error fetching sentiment data (optional: show toast or retry)
                                    }
                                });
                        queue.add(peersRequest);
//                        queue.add(sentimentRequest);
                        queue.add(quoteDetailsRequest);
                        queue.add(portfolioRequest);
                        queue.add(watchlistRequest);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("SearchError", "Error occured while searching", error);
                Toast.makeText(HomeActivity.this, "Error occured while searching", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(jsonObjectRequest);
    }

    static final int REQUEST_CODE_SEARCH_RESULTS = 1001;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data!=null) {
            String peerSymbol = data.getStringExtra("search_query");
            // Call performSearch with the peerSymbol
            performSearchRequest(peerSymbol);
        }
    }


    private void fetchNetWorth() {
        // Fetch balance
        RequestQueue balanceQueue = Volley.newRequestQueue(this);
        String balanceUrl = "https://webtech-hw3-419101.wl.r.appspot.com/balance";

        JsonObjectRequest balanceRequest = new JsonObjectRequest(Request.Method.GET, balanceUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject balanceResponse) {
                        try {
                            double balance = balanceResponse.getDouble("balance");
                            Log.d("Balance", "Received response: " + balanceResponse.toString());
                            TextView walletBalanceTextView = findViewById(R.id.walletBalanceTextView);
                            walletBalanceTextView.setText(String.format("$%.2f", balance));

                            // Fetch portfolio
                            RequestQueue portfolioQueue = Volley.newRequestQueue(HomeActivity.this);
                            String portfolioUrl = "https://webtech-hw3-419101.wl.r.appspot.com/portfolio";

                            JsonArrayRequest portfolioRequest = new JsonArrayRequest(Request.Method.GET, portfolioUrl, null,
                                    new Response.Listener<JSONArray>() {
                                        @Override
                                        public void onResponse(JSONArray portfolioResponse) {
                                            // Parse the JSON array and populate the RecyclerView with portfolio data
                                            Log.d("PortfolioResponse", "Received response: " + portfolioResponse.toString());
                                            List<HomeActivity.PortfolioStock> portfolioStocks = parsePortfolio(portfolioResponse);
                                            RecyclerView recyclerView = findViewById(R.id.portfolioRecyclerView);
                                            recyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
                                            HomeActivity.PortfolioAdapter adapter = new HomeActivity.PortfolioAdapter(HomeActivity.this, portfolioStocks);
                                            recyclerView.setAdapter(adapter);

                                            ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
                                                @Override
                                                public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                                                    int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                                                    int swipeFlags = 0;
                                                    return makeMovementFlags(dragFlags, swipeFlags);
                                                }

                                                @Override
                                                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                                                    int fromPosition = viewHolder.getAdapterPosition();
                                                    int toPosition = target.getAdapterPosition();
                                                    Log.d("ItemMove", "Before move: " + portfolioStocks.toString());
                                                    Log.d("ItemMove", "Moving item from position " + fromPosition + " to position " + toPosition);
                                                    HomeActivity.PortfolioStock movedItem = portfolioStocks.remove(fromPosition);
                                                    // Add the item to its new position
                                                    portfolioStocks.add(toPosition, movedItem);
                                                    Log.d("ItemMove", "After move: " + portfolioStocks.toString());
                                                    adapter.notifyItemMoved(fromPosition, toPosition);
                                                    return true;
                                                }

                                                @Override
                                                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                                                    // Not needed for drag and drop
                                                }
                                            };
                                            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
                                            itemTouchHelper.attachToRecyclerView(portfolioRecyclerView);

                                            // Calculate net worth
                                            double totalStockValue = 0.0;
                                            for (HomeActivity.PortfolioStock stock : portfolioStocks) {
                                                totalStockValue += stock.getCurrentPrice() * stock.getQuantity();
                                            }
                                            double netWorth = balance + totalStockValue;
                                            // Set the net worth to the appropriate TextView
                                            TextView netWorthTextView = findViewById(R.id.netWorthTextView);
                                            netWorthTextView.setText(String.format("$%.2f", netWorth));
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(HomeActivity.this, "Error fetching portfolio data", Toast.LENGTH_SHORT).show();
                                            Log.e("PortfolioError", "Error fetching portfolio data", error);
                                        }
                                    });

                            portfolioQueue.add(portfolioRequest);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("BalanceResponseError", "Error parsing balance response", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomeActivity.this, "Error fetching balance data", Toast.LENGTH_SHORT).show();
                        Log.e("BalanceRequestError", "Error fetching balance data", error);
                    }
                });

        balanceQueue.add(balanceRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search...");

//        adapter = new SuggestionAdapter(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        AutoCompleteTextView autoCompleteTextView = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1);
//        searchView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search query submission
//                progressBar.setVisibility(View.VISIBLE);
                Toast.makeText(HomeActivity.this, "Search: " + query, Toast.LENGTH_SHORT).show();
                performSearchRequest(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle search query text change
                if (newText.isEmpty()) {
                    adapter.clear();
                    return true;
                }
                fetchSuggestions(newText, adapter);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void fetchSuggestions(String query, ArrayAdapter<String> adapter) {
        if (query.isEmpty()) {
            // Clear suggestions if query is empty
            adapter.clear();
            return;
        }
        String url = "https://webtech-hw3-419101.wl.r.appspot.com/autocomplete?q=" + query;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        adapter.clear();
                        try {
                            JSONArray resultArray = response.getJSONArray("result");
                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject suggestionObject = resultArray.getJSONObject(i);
                                String type = suggestionObject.getString("type");
                                String symbol = suggestionObject.getString("displaySymbol");
                                String description = suggestionObject.getString("description");
                                // Apply filter
                                if (type.equals("Common Stock")) {
                                    String formattedSuggestion = symbol + " | " + description;
                                    adapter.add(formattedSuggestion);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomeActivity.this, "Error fetching autocomplete suggestions", Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(jsonObjectRequest);

        AutoCompleteTextView autoCompleteTextView = searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        // Handle item click events on the autocomplete suggestions
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                // Extract the symbol from the selected item (assuming the format is "<symbol> | <description>")
                String[] parts = selectedItem.split("\\|");
                if (parts.length > 0) {
                    // Trim the symbol and set it as the search query
                    String selectedSymbol = parts[0].trim();
                    searchView.setQuery(selectedSymbol, true); // Submit the query
                }
            }
        });
    }

}
