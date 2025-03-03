package com.example.mystocks;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import java.util.Date;

import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class SearchResultsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager2 viewPager;
    private TabLayout tabs;

    private View dialogView;

    private AlertDialog dialog;

    WebView webView;
    WebView earningswebView;

    WebView hourlywebView;
    private RecyclerView recyclerView;
    private Handler handler;
    double latestPrice;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Declare newsArticles as ArrayList<NewsArticle>

        startPeriodicUpdates();

        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);

        for (int i = 0; i < constraintLayout.getChildCount(); i++) {
            View child = constraintLayout.getChildAt(i);
            // Exclude the toolbar from hiding
            if (!(child instanceof Toolbar)&& !(child.getId() == R.id.toolbar)) {
                child.setVisibility(View.GONE);
            }
        }

        ProgressBar progressBar = findViewById(R.id.ProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.view_pager);
        tabs = findViewById(R.id.tabs);

        tabs.setSelectedTabIndicatorHeight(0);

        tabs.setTabMode(TabLayout.MODE_FIXED);

        MyFragmentAdapter adapter = new MyFragmentAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabs, viewPager,
                (tab, position) -> { /* No title needed, using icons */ });
        tabLayoutMediator.attach();

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tabs.getTabAt(0).setIcon(ContextCompat.getDrawable(this, R.drawable.chart_hour));
        tabs.getTabAt(1).setIcon(ContextCompat.getDrawable(this, R.drawable.chart_historical));

        // Get the Intent that started this activity
        Intent intent = getIntent();
        if (intent.hasExtra("search_query")) {
            String peerTicker = intent.getStringExtra("search_query");
            String searchResultsJson = intent.getStringExtra("search_results");
            JSONObject searchResultsObject = null;
            try {
                searchResultsObject = new JSONObject(searchResultsJson);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            try {
                String ticker = searchResultsObject.getString("ticker");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            // Fetch and display details for the clicked peer with the ticker peerTicker
        }
        String searchResultsJson = intent.getStringExtra("search_results");
        if (searchResultsJson != null) {
            try {
                JSONObject searchResultsObject = new JSONObject(searchResultsJson);

                String ticker = searchResultsObject.getString("ticker");
//                fetchNewsData(ticker);
                toolbar.setTitle(ticker);

                JSONArray watchlistArray = searchResultsObject.getJSONArray("watchlist");
                boolean isInWatchlist = false;
                for (int i = 0; i < watchlistArray.length(); i++) {
                    JSONObject watchlistItem = watchlistArray.getJSONObject(i);
                    if (watchlistItem.getString("symbol").equals(ticker)) {
                        isInWatchlist = true;
                        break;
                    }
                }

                ImageView starImageView = findViewById(R.id.imageView);
                if (isInWatchlist) {
                    starImageView.setImageResource(R.drawable.full_star);
                    starImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Send POST request to remove company from watchlist
                            removeFromWatchlist(ticker);
                            starImageView.setImageResource(R.drawable.star_border);
                        }
                    });
                } else {
                    starImageView.setImageResource(R.drawable.star_border);
                    starImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Send POST request to add company to watchlist
                            addToWatchlist(ticker, searchResultsObject);
                            starImageView.setImageResource(R.drawable.full_star);
                        }
                    });
                }

                String name = searchResultsObject.getString("name");
                TextView tickerText = findViewById(R.id.ticker_text);
                tickerText.setText(ticker);

                webView = findViewById(R.id.webView);
                webView.getSettings().setJavaScriptEnabled(true);
                String url = "https://webtech-hw3-419101.wl.r.appspot.com/recommendation?q=" + ticker;



                RequestQueue queue = Volley.newRequestQueue(this);
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                // Parse recommendation data from JSON response
                                // Handle the recommendation data as needed
                                Log.d("Recommendation", "Recommendation response" + response.toString());
//                                renderChart(response);
                                loadHtmlContent(response);
//                                for (int i = 0; i < response.length(); i++) {
//                                    try {
//                                        JSONObject recommendationData = response.getJSONObject(i);
//                                        Log.d("RecommendationObjectString", recommendationData.toString());
//                                        loadHtmlContent(recommendationData);
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        Toast.makeText(SearchResultsActivity.this, "Error fetching recommendation data", Toast.LENGTH_SHORT).show();
                    }
                });

                // Add the request to the RequestQueue
                queue.add(jsonObjectRequest);

                JsonArrayRequest hourlyRequest = new JsonArrayRequest(Request.Method.GET,"https://webtech-hw3-419101.wl.r.appspot.com/hourly?symbol=" + ticker , null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                // Parse recommendation data from JSON response
                                // Handle the recommendation data as needed
                                Log.d("Hourly", "Hourly response" + response.toString());
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        Toast.makeText(SearchResultsActivity.this, "Error fetching hourly data", Toast.LENGTH_SHORT).show();
                    }
                });

                // Add the request to the RequestQueue
                queue.add(hourlyRequest);


//                webView.loadUrl("file:///android_asset/highcharts.html");
//                fetchRecommendationData(ticker);

                earningswebView = findViewById(R.id.epsWebView);
                earningswebView.getSettings().setJavaScriptEnabled(true);
                String earn_url = "https://webtech-hw3-419101.wl.r.appspot.com/earnings?q=" + ticker;

                JsonArrayRequest EarningsObjectRequest = new JsonArrayRequest(Request.Method.GET, earn_url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                // Parse recommendation data from JSON response
                                // Handle the recommendation data as needed
                                Log.d("Earnings", "Earnings response" + response.toString());
//                                renderChart(response);
                                loadHtmlContentforEarnings(response);
//                                for (int i = 0; i < response.length(); i++) {
//                                    try {
//                                        JSONObject recommendationData = response.getJSONObject(i);
//                                        Log.d("RecommendationObjectString", recommendationData.toString());
//                                        loadHtmlContent(recommendationData);
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        Toast.makeText(SearchResultsActivity.this, "Error fetching earnings data", Toast.LENGTH_SHORT).show();
                    }
                });
                queue.add(EarningsObjectRequest);

                String portfolioUrl = "https://webtech-hw3-419101.wl.r.appspot.com/portfolio";
                JsonArrayRequest portfolioRequest = new JsonArrayRequest(Request.Method.GET, portfolioUrl, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray portfolioData) {
                                try {
                                    // Iterate through the portfolio data to find the item for the specified ticker
                                    JSONObject portfolioItem = null;
                                    for (int i = 0; i < portfolioData.length(); i++) {
                                        JSONObject item = portfolioData.getJSONObject(i);
                                        if (item.getString("symbol").equals(ticker)) {
                                            portfolioItem = item;
                                            break;
                                        }
                                    }

                                    // Update the UI with portfolio data
                                    if (portfolioItem == null) {
                                        // Set default values for text views if portfolio item not found
                                        TextView sharesValue = findViewById(R.id.sharesValue);
                                        sharesValue.setText("0");

                                        TextView avgCostValue = findViewById(R.id.avgCostValue);
                                        avgCostValue.setText("$0.00");

                                        TextView totalCostValue = findViewById(R.id.totalCostValue);
                                        totalCostValue.setText("$0.00");

                                        TextView changeValue = findViewById(R.id.changeValue);
                                        changeValue.setText("$0.00");

                                        TextView marketValueTextView = findViewById(R.id.marketValue);
                                        marketValueTextView.setText("$0.00");
                                    } else {
                                        // Extract data from portfolio item
                                        int sharesOwned = portfolioItem.getInt("quantity");
                                        double avgCostPerShare = portfolioItem.getDouble("averageCost");
                                        double totalCost = avgCostPerShare * sharesOwned;
                                        double currentPrice = portfolioItem.getDouble("currentPrice");
                                        double change = totalCost - (currentPrice * sharesOwned);
                                        double marketValue = currentPrice * sharesOwned;

                                        // Update text views with portfolio data
                                        TextView sharesValue = findViewById(R.id.sharesValue);
                                        sharesValue.setText(String.valueOf(sharesOwned));

                                        TextView avgCostValue = findViewById(R.id.avgCostValue);
                                        avgCostValue.setText(String.format("$%.2f", avgCostPerShare));

                                        TextView totalCostValue = findViewById(R.id.totalCostValue);
                                        totalCostValue.setText(String.format("$%.2f", totalCost));

                                        TextView changeValue = findViewById(R.id.changeValue);
                                        changeValue.setText(String.format("$%.2f", change));

                                        TextView marketValueTextView = findViewById(R.id.marketValue);
                                        marketValueTextView.setText(String.format("$%.2f", marketValue));

                                        if (change > 0) {
                                            changeValue.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                                            marketValueTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
//                        getResources().getDrawable(R.drawable.trending_up).setTint(getResources().getColor(android.R.color.holo_green_dark));
//                        changeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.trending_up, 0, 0, 0);
                                        } else if (change < 0) {
                                            changeValue.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                            marketValueTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        error.printStackTrace();
                    }
                });
                queue.add(portfolioRequest);

                TextView nameText = findViewById(R.id.name_text);
                nameText.setText(name);

                String ipoStartDateString = searchResultsObject.getString("ipo");
                DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                DateFormat outputFormat = new SimpleDateFormat("MM-dd-yyyy");
                Date ipoStartDate = inputFormat.parse(ipoStartDateString);
                String formattedIPOStartDate = outputFormat.format(ipoStartDate);

                String industry = searchResultsObject.getString("finnhubIndustry");
                String webpage = searchResultsObject.getString("weburl");

                // Set the text for IPO Start Date
                TextView ipoStartDateTextView = findViewById(R.id.ipoStartDateValue);
                ipoStartDateTextView.setText(formattedIPOStartDate);

                // Set the text for Industry
                TextView industryTextView = findViewById(R.id.industryValue);
                industryTextView.setText(industry);

                // Set the text for Webpage
                TextView webpageTextView = findViewById(R.id.webpageValue);
                webpageTextView.setText(webpage);
                webpageTextView.setMovementMethod(LinkMovementMethod.getInstance());

                webpageTextView.setTextColor(Color.BLUE);
                webpageTextView.setPaintFlags(webpageTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                webpageTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse(webpage);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.setPackage("com.android.chrome");
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            // Chrome is not installed, open in default browser
                            intent.setPackage(null);
                            startActivity(intent);
                        }
                    }
                });

                fetchNewsData(ticker);

                JSONArray peersArray = searchResultsObject.getJSONArray("peers");
                SpannableStringBuilder peersStringBuilder = new SpannableStringBuilder();
                for (int i = 0; i < peersArray.length(); i++) {
                    final String peerSymbol = peersArray.getString(i);
                    peersStringBuilder.append(peerSymbol);
                    if (i < peersArray.length() - 1) {
                        peersStringBuilder.append(", ");
                    }
                    peersStringBuilder.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View view) {
                            // Handle click on peer symbol here
                            String clickedPeerSymbol = ((TextView) view).getText().toString();
                            Intent intent = new Intent();
                            intent.putExtra("clicked_peer_symbol", clickedPeerSymbol);
                            setResult(RESULT_OK, intent);
                             // Finish the activity and return to the previous activity

                            // You can perform other actions here if needed
                            // For example, start a new activity with the peer symbol
                            // Intent intent = new Intent(SearchResultsActivity.this, AnotherActivity.class);
                            // intent.putExtra("search_query", clickedPeerSymbol);
                            // startActivity(intent);
                        }

                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setUnderlineText(true);
                            ds.setColor(Color.BLUE);
                        }
                    }, peersStringBuilder.length() - peerSymbol.length(), peersStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                TextView peersTextView = findViewById(R.id.companyPeersValue);
                peersTextView.setText(peersStringBuilder);
                peersTextView.setMovementMethod(LinkMovementMethod.getInstance());


//                JSONArray peersArray = searchResultsObject.getJSONArray("peers");
//                StringBuilder peersStringBuilder = new StringBuilder();
//                for (int i = 0; i < peersArray.length(); i++) {
//                    String peerSymbol = peersArray.getString(i);
//                    peersStringBuilder.append(peerSymbol);
//                    if (i < peersArray.length() - 1) {
//                        peersStringBuilder.append(", ");
//                    }
//                }
//                String peersText = peersStringBuilder.toString();
//                TextView peersTextView = findViewById(R.id.companyPeersValue);
//                peersTextView.setText(peersText);
//                peersTextView.setTextColor(Color.BLUE);
//                peersTextView.setPaintFlags(webpageTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


                TextView currentPriceText = findViewById(R.id.current_price_text);
                currentPriceText.setText(String.format("$%.2f", searchResultsObject.getJSONObject("quoteDetails").getDouble("c")));

                JSONObject quoteDetails = searchResultsObject.getJSONObject("quoteDetails");

                // Set text for Open Price
                TextView openPriceValue = findViewById(R.id.openPriceValue);
                if (quoteDetails.has("o")) {
                    double openPrice = quoteDetails.getDouble("o");
                    openPriceValue.setText("$" + String.format("%.2f", openPrice));
                } else {
                    openPriceValue.setText("$0.00");
                }

                // Set text for Low Price
                TextView lowPriceValue = findViewById(R.id.lowPriceValue);
                if (quoteDetails.has("l")) {
                    double lowPrice = quoteDetails.getDouble("l");
                    lowPriceValue.setText("$" + String.format("%.2f", lowPrice));
                } else {
                    lowPriceValue.setText("$0.00");
                }

                // Set text for High Price
                TextView highPriceValue = findViewById(R.id.highPriceValue);
                if (quoteDetails.has("h")) {
                    double highPrice = quoteDetails.getDouble("h");
                    highPriceValue.setText("$" + String.format("%.2f", highPrice));
                } else {
                    highPriceValue.setText("$0.00");
                }

                // Set text for Previous Close
                TextView prevCloseValue = findViewById(R.id.prevCloseValue);
                if (quoteDetails.has("pc")) {
                    double prevClose = quoteDetails.getDouble("pc");
                    prevCloseValue.setText("$" + String.format("%.2f", prevClose));
                } else {
                    prevCloseValue.setText("$0.00");
                }

                TextView companyName = findViewById(R.id.companyName);
                companyName.setText(searchResultsObject.getString("name"));

                String sentimentUrl = "https://webtech-hw3-419101.wl.r.appspot.com/insider-sentiment?q=" + ticker;

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

                                    TextView totalMSRPText = findViewById(R.id.totalMSRPText);
                                    totalMSRPText.setText(String.format("%.2f",totalMSPR));

                                    TextView totalChangeText = findViewById(R.id.totalChangeText);
                                    totalChangeText.setText(String.format("%.2f", totalChange));

                                    TextView positiveMSRPText = findViewById(R.id.positiveMSRPText);
                                    positiveMSRPText.setText(String.format("%.2f", positiveMSPR));

                                    TextView positiveChangeText = findViewById(R.id.positiveChangeText);
                                    positiveChangeText.setText(String.format("%.2f", positiveChange));

                                    TextView negativeMSRPText = findViewById(R.id.negativeMSRPText);
                                    negativeMSRPText.setText(String.format("%.2f", negativeMSPR));

                                    TextView negativeChangeText = findViewById(R.id.negativeChangeText);
                                    negativeChangeText.setText(String.format("%.2f", negativeChange));

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

                queue.add(sentimentRequest);

                TextView changeText = findViewById(R.id.change_text);
                double change = searchResultsObject.getJSONObject("quoteDetails").getDouble("d");
                double changePercent = searchResultsObject.getJSONObject("quoteDetails").getDouble("dp");
                changeText.setText(String.format(" %.2f (%+.2f%%)", change, changePercent));

                if (changePercent > 0) {
                    changeText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    getResources().getDrawable(R.drawable.trending_up).setTint(getResources().getColor(android.R.color.holo_green_dark));
                    changeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.trending_up, 0, 0, 0);
                } else {
                    changeText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    getResources().getDrawable(R.drawable.trending_down).setTint(getResources().getColor(android.R.color.holo_red_dark));
                    changeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.trending_down, 0, 0, 0);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                // Handle parsing error (e.g., show a toast message)
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
            // Handle case where no search results data is available (e.g., show a message)
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Hide the progress bar
                for (int i = 0; i < constraintLayout.getChildCount(); i++) {
                    View child = constraintLayout.getChildAt(i);
                    child.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.GONE);
            }
        }, 3000);
    }

    private void startPeriodicUpdates() {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Fetch latest quote details
                try {
                    fetchLatestQuoteDetails();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                // Schedule next update after 15 seconds
                handler.postDelayed(this, 15000); // 15 seconds in milliseconds
            }
        }, 0); // Start immediately
    }
    private void fetchLatestQuoteDetails() throws JSONException {
        // Fetch latest quote details for the ticker
        String ticker = null;
        Intent intent = getIntent();
        String searchResultsJson = intent.getStringExtra("search_results");
        if (searchResultsJson != null && !searchResultsJson.isEmpty()) {
            try {
                JSONObject searchResultsObject = new JSONObject(searchResultsJson);
                ticker = searchResultsObject.getString("ticker");
                // Use ticker variable as needed
            } catch (JSONException e) {
                e.printStackTrace();
                // Handle JSON parsing exception
            }
        } else {
            // Handle case where searchResultsJson is null or empty
        }
        String url = "https://webtech-hw3-419101.wl.r.appspot.com/quoteDetails?symbol=" + ticker;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Parse quote details from the response
                            Log.d("QuoteResponse", response.toString());
                            latestPrice = response.getDouble("c");
                            double change = response.getDouble("d");
                            double changePercent = response.getDouble("dp");

                            // Update UI with latest quote details
                            TextView currentPriceText = findViewById(R.id.current_price_text);
                            currentPriceText.setText(String.format("$%.2f", latestPrice));

                            TextView changeText = findViewById(R.id.change_text);
                            changeText.setText(String.format(" %.2f (%+.2f%%)", change, changePercent));

                            if (changePercent > 0) {
                                changeText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                                getResources().getDrawable(R.drawable.trending_up).setTint(getResources().getColor(android.R.color.holo_green_dark));
                                changeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.trending_up, 0, 0, 0);
                            } else {
                                changeText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                getResources().getDrawable(R.drawable.trending_down).setTint(getResources().getColor(android.R.color.holo_red_dark));
                                changeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.trending_down, 0, 0, 0);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Handle parsing error
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error", "Error fetching quote details: " + error.getMessage());
                Toast.makeText(SearchResultsActivity.this, "Error fetching quote details", Toast.LENGTH_SHORT).show();
            }
        });

        // Add request to queue
        queue.add(jsonObjectRequest);
    }

    public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_FIRST_ARTICLE = 1;
        private static final int VIEW_TYPE_NORMAL_ARTICLE = 2;

        private List<NewsArticle> newsArticles;
        private Context context;

        public NewsAdapter(Context context, List<NewsArticle> newsArticles) {
            this.context = context;
            this.newsArticles = newsArticles;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case VIEW_TYPE_FIRST_ARTICLE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_first_news_article, parent, false);
                    return new FirstArticleViewHolder(view);
                case VIEW_TYPE_NORMAL_ARTICLE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_article, parent, false);
                    return new NormalArticleViewHolder(view);
                default:
                    throw new IllegalArgumentException("Invalid view type");
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof FirstArticleViewHolder) {
                NewsArticle newsArticle = newsArticles.get(position);
                FirstArticleViewHolder firstViewHolder = (FirstArticleViewHolder) holder;
                long timestamp = newsArticle.getPublishedDate();

                long currentTimeMillis = System.currentTimeMillis() / 1000;
                long elapsedTimeSeconds = currentTimeMillis - timestamp;

                String timeElapsedString = getTimeElapsedString(elapsedTimeSeconds);
                firstViewHolder.textHoursElapsed.setText(timeElapsedString);
                Picasso.get().load(newsArticle.getImageUrl()).into(firstViewHolder.imageNews);

                firstViewHolder.textSource.setText(newsArticle.getSource());
                firstViewHolder.textHeadline.setText(newsArticle.getHeadline());
                firstViewHolder.bind(newsArticle);

            } else if (holder instanceof NormalArticleViewHolder) {
                NewsArticle newsArticle = newsArticles.get(position);
                NormalArticleViewHolder normalViewHolder = (NormalArticleViewHolder) holder;
                long timestamp = newsArticle.getPublishedDate();

                long currentTimeMillis = System.currentTimeMillis() / 1000;
                long elapsedTimeSeconds = currentTimeMillis - timestamp;

                String timeElapsedString = getTimeElapsedString(elapsedTimeSeconds);
                normalViewHolder.textHoursElapsed.setText(timeElapsedString);
                Picasso.get().load(newsArticle.getImageUrl()).into(normalViewHolder.imageNews);

                normalViewHolder.textSource.setText(newsArticle.getSource());
                normalViewHolder.textHeadline.setText(newsArticle.getHeadline());
                normalViewHolder.bind(newsArticle);
            }
        }

        private String getTimeElapsedString(long elapsedTimeSeconds) {
            String timeElapsedString;
            if (elapsedTimeSeconds < 60) {
                timeElapsedString = elapsedTimeSeconds == 1 ? "1 second ago" : elapsedTimeSeconds + " seconds ago";
            } else if (elapsedTimeSeconds < 3600) {
                long elapsedMinutes = TimeUnit.SECONDS.toMinutes(elapsedTimeSeconds);
                timeElapsedString = elapsedMinutes == 1 ? "1 minute ago" : elapsedMinutes + " minutes ago";
            } else {
                long elapsedHours = TimeUnit.SECONDS.toHours(elapsedTimeSeconds);
                timeElapsedString = elapsedHours == 1 ? "1 hour ago" : elapsedHours + " hours ago";
            }
            return timeElapsedString;
        }

        @Override
        public int getItemCount() {
            return Math.min(newsArticles.size(), 20);
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? VIEW_TYPE_FIRST_ARTICLE : VIEW_TYPE_NORMAL_ARTICLE;
        }

        class FirstArticleViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView textSource, textHoursElapsed, textHeadline;
            ImageView imageNews;

            FirstArticleViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.card_view);
                cardView.setOnClickListener(v -> {
//                    NewsArticle newsArticle = newsArticles.get(getAdapterPosition());
//                    showNewsArticleDialog(context, newsArticle);
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        NewsArticle newsArticle = newsArticles.get(position);
                        showNewsArticleDialog(context, newsArticle);
                    }
                });
                textSource = itemView.findViewById(R.id.text_source);
                textHoursElapsed = itemView.findViewById(R.id.text_hours_elapsed);
                textHeadline = itemView.findViewById(R.id.text_headline);
                imageNews = itemView.findViewById(R.id.image_news);
            }

            void bind(NewsArticle newsArticle) {
                // Implement binding logic here if needed
            }
        }

        class NormalArticleViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView textSource, textHoursElapsed, textHeadline;
            ImageView imageNews;

            NormalArticleViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.card_view);
                cardView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        NewsArticle newsArticle = newsArticles.get(position);
                        showNewsArticleDialog(context, newsArticle);
                    }
                });
                textSource = itemView.findViewById(R.id.text_source);
                textHoursElapsed = itemView.findViewById(R.id.text_hours_elapsed);
                textHeadline = itemView.findViewById(R.id.text_headline);
                imageNews = itemView.findViewById(R.id.image_news);
            }

            void bind(NewsArticle newsArticle) {
                // Implement binding logic here if needed
            }
        }
    }


//    public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//        private static final int VIEW_TYPE_FIRST_ARTICLE = 1;
//        private static final int VIEW_TYPE_NORMAL_ARTICLE = 2;
//
//        private List<NewsArticle> newsArticles;
//        private Context context;
//
//        public NewsAdapter(Context context, List<NewsArticle> newsArticles) {
//            this.context = context;
//            this.newsArticles = newsArticles;
//        }
//
//        @NonNull
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view;
//            switch (viewType) {
//                case VIEW_TYPE_FIRST_ARTICLE:
//                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_first_news_article, parent, false);
//                    return new FirstArticleViewHolder(view);
//                case VIEW_TYPE_NORMAL_ARTICLE:
//                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_article, parent, false);
//                    return new NormalArticleViewHolder(view);
//                default:
//                    throw new IllegalArgumentException("Invalid view type");
//            }
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//            if (holder instanceof FirstArticleViewHolder) {
//                NewsArticle newsArticle = newsArticles.get(position);
//                FirstArticleViewHolder firstViewHolder = (FirstArticleViewHolder) holder;
//                long timestamp = newsArticle.getPublishedDate();
//
//                long currentTimeMillis = System.currentTimeMillis() / 1000;
//                long elapsedTimeSeconds = currentTimeMillis - timestamp;
//
//                String timeElapsedString;
//                if (elapsedTimeSeconds < 60) {
//                    timeElapsedString = elapsedTimeSeconds == 1 ? "1 second ago" : elapsedTimeSeconds + " seconds ago";
//                } else if (elapsedTimeSeconds < 3600) {
//                    long elapsedMinutes = TimeUnit.SECONDS.toMinutes(elapsedTimeSeconds);
//                    timeElapsedString = elapsedMinutes == 1 ? "1 minute ago" : elapsedMinutes + " minutes ago";
//                } else {
//                    long elapsedHours = TimeUnit.SECONDS.toHours(elapsedTimeSeconds);
//                    timeElapsedString = elapsedHours == 1 ? "1 hour ago" : elapsedHours + " hours ago";
//                }
//                holder.textHoursElapsed.setText(timeElapsedString);
//                Picasso.get().load(newsArticle.getImageUrl()).into(holder.imageNews);
//
//                holder.textSource.setText(newsArticle.getSource());
//                holder.textHeadline.setText(newsArticle.getHeadline());
//                firstViewHolder.bind(newsArticle);
//
//            } else if (holder instanceof NormalArticleViewHolder) {
//                NewsArticle newsArticle = newsArticles.get(position - 1);
//                NormalArticleViewHolder normalViewHolder = (NormalArticleViewHolder) holder;
//                long timestamp = newsArticle.getPublishedDate();
//
//                long currentTimeMillis = System.currentTimeMillis() / 1000;
//                long elapsedTimeSeconds = currentTimeMillis - timestamp;
//
//                String timeElapsedString;
//                if (elapsedTimeSeconds < 60) {
//                    timeElapsedString = elapsedTimeSeconds == 1 ? "1 second ago" : elapsedTimeSeconds + " seconds ago";
//                } else if (elapsedTimeSeconds < 3600) {
//                    long elapsedMinutes = TimeUnit.SECONDS.toMinutes(elapsedTimeSeconds);
//                    timeElapsedString = elapsedMinutes == 1 ? "1 minute ago" : elapsedMinutes + " minutes ago";
//                } else {
//                    long elapsedHours = TimeUnit.SECONDS.toHours(elapsedTimeSeconds);
//                    timeElapsedString = elapsedHours == 1 ? "1 hour ago" : elapsedHours + " hours ago";
//                }
//                holder.textHoursElapsed.setText(timeElapsedString);
//                Picasso.get().load(newsArticle.getImageUrl()).into(holder.imageNews);
//
//                holder.textSource.setText(newsArticle.getSource());
//                holder.textHeadline.setText(newsArticle.getHeadline());
//                normalViewHolder.bind(newsArticle);
//            }
//        }
//
//        @Override
//        public int getItemCount() {
//            return Math.min(newsArticles.size() + 1, 20);
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            return position == 0 ? VIEW_TYPE_FIRST_ARTICLE : VIEW_TYPE_NORMAL_ARTICLE;
//        }
//
//        class FirstArticleViewHolder extends RecyclerView.ViewHolder {
//            CardView cardView;
//            TextView textSource, textHoursElapsed, textHeadline;
//            ImageView imageNews;
//
//            FirstArticleViewHolder(@NonNull View itemView) {
//                super(itemView);
//                cardView = itemView.findViewById(R.id.card_view);
//                cardView.setOnClickListener(v -> {
//                    // Get the clicked news article
//                    NewsArticle newsArticle = newsArticles.get(getAdapterPosition());
//                    // Show the dialog for the clicked news article
//                    showNewsArticleDialog(context, newsArticle);
//                });
//                textSource = itemView.findViewById(R.id.text_source);
//                textHoursElapsed = itemView.findViewById(R.id.text_hours_elapsed);
//                textHeadline = itemView.findViewById(R.id.text_headline);
//                imageNews = itemView.findViewById(R.id.image_news);
//            }
//
//            void bind(NewsArticle newsArticle) {
//            }
//        }
//
//        class NormalArticleViewHolder extends RecyclerView.ViewHolder {
//            CardView cardView;
//            TextView textSource, textHoursElapsed, textHeadline;
//            ImageView imageNews;
//
//            NormalArticleViewHolder(@NonNull View itemView) {
//                super(itemView);
//                cardView = itemView.findViewById(R.id.card_view);
//                cardView.setOnClickListener(v -> {
//                    // Get the clicked news article
//                    NewsArticle newsArticle = newsArticles.get(getAdapterPosition());
//                    // Show the dialog for the clicked news article
//                    showNewsArticleDialog(context, newsArticle);
//                });
//                textSource = itemView.findViewById(R.id.text_source);
//                textHoursElapsed = itemView.findViewById(R.id.text_hours_elapsed);
//                textHeadline = itemView.findViewById(R.id.text_headline);
//                imageNews = itemView.findViewById(R.id.image_news);
//            }
//
//            void bind(NewsArticle newsArticle) {
//            }
//        }
//    }


//    public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
//
//        private List<NewsArticle> newsArticles;
//        private Context context;
//
//        public NewsAdapter(Context context, List<NewsArticle> newsArticles) {
//            this.context = context;
//            this.newsArticles = newsArticles;
//        }
//
//        @NonNull
//        @Override
//        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_article, parent, false);
//            return new ViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//            NewsArticle newsArticle = newsArticles.get(position);
//
//            // Calculate time elapsed since publication
//            long timestamp = newsArticle.getPublishedDate();
//
//            long currentTimeMillis = System.currentTimeMillis() / 1000;
//            long elapsedTimeSeconds = currentTimeMillis - timestamp;
//
//            String timeElapsedString;
//            if (elapsedTimeSeconds < 60) {
//                timeElapsedString = elapsedTimeSeconds == 1 ? "1 second ago" : elapsedTimeSeconds + " seconds ago";
//            } else if (elapsedTimeSeconds < 3600) {
//                long elapsedMinutes = TimeUnit.SECONDS.toMinutes(elapsedTimeSeconds);
//                timeElapsedString = elapsedMinutes == 1 ? "1 minute ago" : elapsedMinutes + " minutes ago";
//            } else {
//                long elapsedHours = TimeUnit.SECONDS.toHours(elapsedTimeSeconds);
//                timeElapsedString = elapsedHours == 1 ? "1 hour ago" : elapsedHours + " hours ago";
//            }
//
//
//            holder.textHoursElapsed.setText(timeElapsedString);
//
//            Picasso.get().load(newsArticle.getImageUrl()).into(holder.imageNews);
//
//            holder.textSource.setText(newsArticle.getSource());
//            holder.textHeadline.setText(newsArticle.getHeadline());
//
//            holder.bind(newsArticle);
//        }
//
//        @Override
//        public int getItemCount() {
//            return Math.min(newsArticles.size(), 20); // Return at most 20 articles
//        }
//
//        class ViewHolder extends RecyclerView.ViewHolder {
//            CardView cardView;
//            TextView textSource, textHoursElapsed, textHeadline;
//            ImageView imageNews;
//
//            ViewHolder(@NonNull View itemView) {
//                super(itemView);
//                cardView = itemView.findViewById(R.id.card_view);
//                cardView.setOnClickListener(v -> {
//                    // Get the clicked news article
//                    NewsArticle newsArticle = newsArticles.get(getAdapterPosition());
//                    // Show the dialog for the clicked news article
//                    showNewsArticleDialog(context, newsArticle);
//                });
//                textSource = itemView.findViewById(R.id.text_source);
//                textHoursElapsed = itemView.findViewById(R.id.text_hours_elapsed);
//                textHeadline = itemView.findViewById(R.id.text_headline);
//                imageNews = itemView.findViewById(R.id.image_news);
//            }
//
//            public void bind(NewsArticle newsArticle) {
//            }
//        }
//    }
    public class NewsArticle {
        private String category;
        private String headline;
        private String imageUrl;
        private String related;
        private String source;
        private Integer publishedDate;
        private String summary;
        private String url;

        public NewsArticle(String category, String headline, String imageUrl, String related, String source, Integer publishedDate, String summary, String url) {
            this.category = category;
            this.headline = headline;
            this.imageUrl = imageUrl;
            this.related = related;
            this.source = source;
            this.publishedDate = publishedDate;
            this.summary = summary;
            this.url = url;
        }

        public String getCategory() {
            return category;
        }

        public String getHeadline() {
            return headline;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getRelated() {
            return related;
        }

        public String getSource() {
            return source;
        }

        public Integer getPublishedDate() {
            return publishedDate;
        }

        public String getSummary() {
            return summary;
        }

        public String getUrl() {
            return url;
        }
    }
    private void fetchNewsData(String symbol) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://webtech-hw3-419101.wl.r.appspot.com/news?q=" + symbol;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONArray articlesArray = response;
                            List<NewsArticle> newsArticles = new ArrayList<>();
                            for (int i = 0; i < articlesArray.length(); i++) {
                                JSONObject articleObject = articlesArray.getJSONObject(i);
                                String category = articleObject.getString("category");
                                String headline = articleObject.getString("headline");
                                String imageUrl = articleObject.getString("image");
                                String related = articleObject.getString("related");
                                String source = articleObject.getString("source");
                                Integer publishedDate = articleObject.getInt("datetime");
                                String summary = articleObject.getString("summary");
                                String url = articleObject.getString("url");
                                // Parse other properties like published date, description, etc.

                                // Create NewsArticle object and add to list
                                NewsArticle article = new NewsArticle(category, headline, imageUrl, related, source, publishedDate, summary, url);
                                newsArticles.add(article);
                            }
                            // Update RecyclerView with news articles
                            updateRecyclerView(newsArticles);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error", "Error fetching news data: " + error.getMessage());
                Toast.makeText(SearchResultsActivity.this, "Error fetching news data", Toast.LENGTH_SHORT).show();
            }
        });

        // Add request to queue
        queue.add(jsonArrayRequest);
    }
    private void updateRecyclerView(List<NewsArticle> newsArticles) {
        List<NewsArticle> filteredArticles = new ArrayList<>();
        for (NewsArticle article : newsArticles) {
            if (!TextUtils.isEmpty(article.getImageUrl())) {
                filteredArticles.add(article);
            }
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        NewsAdapter newsAdapter = new NewsAdapter(this, filteredArticles);
        recyclerView.setAdapter(newsAdapter);
    }
    private void showNewsArticleDialog(Context context, NewsArticle newsArticle) {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_news_article, null);

        // Initialize dialog views
        TextView textSource = dialogView.findViewById(R.id.text_source);
        TextView textPublishedDate = dialogView.findViewById(R.id.text_published_date);
        TextView textTitle = dialogView.findViewById(R.id.text_title);
        TextView textDescription = dialogView.findViewById(R.id.text_description);
        Button buttonChrome = dialogView.findViewById(R.id.button_chrome);
        Button buttonTwitter = dialogView.findViewById(R.id.button_twitter);
        Button buttonFacebook = dialogView.findViewById(R.id.button_facebook);

        // Set news article details
        textSource.setText(newsArticle.getSource());
        // Convert published date to human-readable format and set it
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(newsArticle.getPublishedDate() * 1000L));
        textPublishedDate.setText(formattedDate);
        textTitle.setText(newsArticle.getHeadline());
        textDescription.setText(newsArticle.getSummary());

        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set click listeners for buttons
        buttonChrome.setOnClickListener(v -> openUrlInChrome(newsArticle.getUrl()));
        buttonTwitter.setOnClickListener(v -> openUrlInChrome("https://twitter.com/share?url=" + newsArticle.getUrl()));
        buttonFacebook.setOnClickListener(v -> openUrlInChrome("https://www.facebook.com/sharer/sharer.php?u=" + newsArticle.getUrl()));
    }
    private void openUrlInChrome(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.chrome");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Chrome not installed, open URL in default browser
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(webIntent);
        }
    }
    private void fetchAndSetPortfolioData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String portfolioUrl = "https://webtech-hw3-419101.wl.r.appspot.com/portfolio";
        JsonArrayRequest portfolioRequest = new JsonArrayRequest(Request.Method.GET, portfolioUrl, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray portfolioData) {
                        try {
                            // Iterate through the portfolio data to find the item for the specified ticker
                            JSONObject portfolioItem = null;
                            for (int i = 0; i < portfolioData.length(); i++) {
                                JSONObject item = portfolioData.getJSONObject(i);
                                Intent intent = getIntent();
                                String searchResultsJson = intent.getStringExtra("search_results");
                                JSONObject searchResultsObject = new JSONObject(searchResultsJson);

                                String ticker = searchResultsObject.getString("ticker");
                                if (item.getString("symbol").equals(ticker)) {
                                    portfolioItem = item;
                                    break;
                                }
                            }

                            // Update the UI with portfolio data
                            if (portfolioItem == null) {
                                // Set default values for text views if portfolio item not found
                                TextView sharesValue = findViewById(R.id.sharesValue);
                                sharesValue.setText("0");
                                sharesValue.setTextColor(Color.BLACK);

                                TextView avgCostValue = findViewById(R.id.avgCostValue);
                                avgCostValue.setText("$0.00");
                                avgCostValue.setTextColor(Color.BLACK);

                                TextView totalCostValue = findViewById(R.id.totalCostValue);
                                totalCostValue.setText("$0.00");
                                totalCostValue.setTextColor(Color.BLACK);

                                TextView changeValue = findViewById(R.id.changeValue);
                                changeValue.setText("$0.00");
                                changeValue.setTextColor(Color.BLACK);

                                TextView marketValueTextView = findViewById(R.id.marketValue);
                                marketValueTextView.setText("$0.00");
                                marketValueTextView.setTextColor(Color.BLACK);
                            } else {
                                // Extract data from portfolio item
                                int sharesOwned = portfolioItem.getInt("quantity");
                                double avgCostPerShare = portfolioItem.getDouble("averageCost");
                                double totalCost = avgCostPerShare * sharesOwned;
                                double currentPrice = latestPrice;
                                double change = (currentPrice - avgCostPerShare) * sharesOwned;
                                double marketValue = currentPrice * sharesOwned;

                                // Update text views with portfolio data
                                TextView sharesValue = findViewById(R.id.sharesValue);
                                sharesValue.setText(String.valueOf(sharesOwned));

                                TextView avgCostValue = findViewById(R.id.avgCostValue);
                                avgCostValue.setText(String.format("$%.2f", avgCostPerShare));

                                TextView totalCostValue = findViewById(R.id.totalCostValue);
                                totalCostValue.setText(String.format("$%.2f", totalCost));

                                TextView changeValue = findViewById(R.id.changeValue);
                                changeValue.setText(String.format("$%.2f", change));

                                TextView marketValueTextView = findViewById(R.id.marketValue);
                                marketValueTextView.setText(String.format("$%.2f", marketValue));

                                if (change > 0) {
                                    changeValue.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                                    marketValueTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
//                        getResources().getDrawable(R.drawable.trending_up).setTint(getResources().getColor(android.R.color.holo_green_dark));
//                        changeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.trending_up, 0, 0, 0);
                                } else if (change < 0) {
                                    changeValue.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                    marketValueTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error response
                error.printStackTrace();
            }
        });
        queue.add(portfolioRequest);

    }
    public void onBuyButtonClick(View view) throws JSONException {
        // Handle buy action
        // Update available amount after buy action
        // You can access the dialog views and perform actions accordingly
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://webtech-hw3-419101.wl.r.appspot.com/balance";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Parse the JSON response to get the current balance
                            double balance = response.getDouble("balance");

                            String searchResultsJson = getIntent().getStringExtra("search_results");

                            // Convert the JSON string back to a JSONObject
                            JSONObject searchResults = null;
                            try {
                                searchResults = new JSONObject(searchResultsJson);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            double currentPrice = searchResults.getJSONObject("quoteDetails").getDouble("c");
                            EditText numericInputField = dialogView.findViewById(R.id.numeric_input_field);
                            String inputText = numericInputField.getText().toString();
                            if (!inputText.isEmpty()) {
                                // Parse the numeric input value
                                double inputValue = Double.parseDouble(inputText);
                                double totalCost = currentPrice * inputValue;
                                if (totalCost > balance) {
                                    // Display toast message for not enough money
                                    Toast.makeText(SearchResultsActivity.this, "Not enough money to buy", Toast.LENGTH_SHORT).show();
                                    return; // Exit the method without processing the buy action
                                }
                                if (inputValue < 0) {
                                    // Display toast message for non-positive shares
                                    Toast.makeText(SearchResultsActivity.this, "Cannot sell non-positive shares", Toast.LENGTH_SHORT).show();
                                    return; // Exit the method without processing the sell action
                                }
                                double newBalance = balance - (currentPrice * inputValue);
                                updateWalletBalance(newBalance);
                                addToPortfolio(searchResults, inputValue, currentPrice);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        fetchAndSetPortfolioData();
                                    }
                                }, 500);
                                showTransactionSuccessDialog("bought", searchResults.getString("ticker"), inputValue);
                                dialog.dismiss();
                            } else {
                                // Handle the case where the input text is empty
                                Toast.makeText(SearchResultsActivity.this, "Please enter a valid numeric value", Toast.LENGTH_SHORT).show();
                            }
                            // Now you can use the balance variable as needed
                            // For example:
                            // Log.d("Balance", "Current balance: " + balance);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error response
                error.printStackTrace();
            }
        });
        queue.add(jsonObjectRequest);


        // Add the request to the RequestQueue
    }
    public void onSellButtonClick(View view) throws JSONException {
        // Handle sell action
        // Update available amount after sell action
        // You can access the dialog views and perform actions accordingly
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://webtech-hw3-419101.wl.r.appspot.com/balance";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Parse the JSON response to get the current balance
                            double balance = response.getDouble("balance");

                            // Fetch portfolio data from backend
                            String portfolioUrl = "https://webtech-hw3-419101.wl.r.appspot.com/portfolio";
                            JsonArrayRequest portfolioRequest = new JsonArrayRequest(Request.Method.GET, portfolioUrl, null,
                                    new Response.Listener<JSONArray>() {
                                        @Override
                                        public void onResponse(JSONArray portfolioData) {
                                            try {
                                                // Iterate through the portfolio data to find the item for the specified ticker
                                                int sharesOwned = 0;
                                                for (int i = 0; i < portfolioData.length(); i++) {
                                                    JSONObject item = portfolioData.getJSONObject(i);
                                                    // Replace "symbol" with the appropriate key in your JSON data
                                                    Intent intent = getIntent();
                                                    String searchResultsJson = intent.getStringExtra("search_results");
                                                    JSONObject searchResultsObject = new JSONObject(searchResultsJson);

                                                    String ticker = searchResultsObject.getString("ticker");
                                                    if (item.getString("symbol").equals(ticker)) {
                                                        sharesOwned = item.getInt("quantity");
                                                        break;
                                                    }
                                                }

                                                String searchResultsJson = getIntent().getStringExtra("search_results");
                                                JSONObject searchResults = new JSONObject(searchResultsJson);
                                                double currentPrice = searchResults.getJSONObject("quoteDetails").getDouble("c");
                                                EditText numericInputField = dialogView.findViewById(R.id.numeric_input_field);
                                                String inputText = numericInputField.getText().toString();
                                                if (!inputText.matches("^\\d*\\.?\\d+$")) {
                                                    // Display toast message for invalid input
                                                    Toast.makeText(SearchResultsActivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                                                    return; // Exit the method without processing the action
                                                }
                                                double inputValue;
                                                if (!inputText.isEmpty()) {
                                                    // Parse the numeric input value
                                                        // Parse the numeric input value
                                                    try {
                                                        inputValue = Double.parseDouble(inputText);
                                                    } catch (NumberFormatException ex) {
                                                        return;
                                                    }
                                                        // Handle the positive input value as needed
                                                    if (inputValue > sharesOwned) {
                                                        // Display toast message for not enough shares
                                                        Toast.makeText(SearchResultsActivity.this, "Not enough shares to sell", Toast.LENGTH_SHORT).show();
                                                        return; // Exit the method without processing the sell action
                                                    }
                                                    if (inputValue < 0) {
                                                        // Display toast message for non-positive shares
                                                        Toast.makeText(SearchResultsActivity.this, "Cannot sell non-positive shares", Toast.LENGTH_SHORT).show();
                                                        return; // Exit the method without processing the sell action
                                                    }
                                                    double newBalance = balance + (currentPrice * inputValue);
                                                    updateWalletBalance(newBalance);
                                                    sellFromPortfolio(searchResults, inputValue, currentPrice);
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            fetchAndSetPortfolioData();
                                                        }
                                                    }, 1000);
                                                    showTransactionSuccessDialog("sold", searchResults.getString("ticker"), inputValue);
                                                    dialog.dismiss();
                                                } else {
                                                    // Handle the case where the input text is empty
                                                    Toast.makeText(SearchResultsActivity.this, "Please enter a valid numeric value", Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // Handle error response
                                    error.printStackTrace();
                                }
                            });
                            queue.add(portfolioRequest);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error response
                error.printStackTrace();
            }
        });
        queue.add(jsonObjectRequest);
    }
    private void loadHtmlContentforEarnings(JSONArray earningsData) {
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Recommendation Trends</title>\n" +
                "    <script src=\"https://code.highcharts.com/highcharts.js\"></script>\n" +
                "    <script src=\"https://code.highcharts.com/modules/exporting.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"container\" style=\"width:100%; height:400px;\"></div>\n" +
                "    <script>\n" +
                "        var earnData = {\n" +
                "            actual: [],\n" +
                "            estimate: [],\n" +
                "            period: [],\n" +
                "            symbol: [],\n" +
                "            surprise: [],\n" +
                "            surprisePercent: [],\n" +
                "        };\n" +
                "        var earningsData = " + earningsData.toString() + ";\n" +
                "        var xAxisLabels = [];\n" +
                "        for (var i = 0; i < earningsData.length; i++) {\n" +
                "            var earning = earningsData[i];\n" +
                "            earnData.actual.push(earning.actual);\n" +
                "            earnData.estimate.push(earning.estimate);\n" +
                "            earnData.period.push(earning.period);\n" +
                "            earnData.symbol.push(earning.symbol);\n" +
                "            earnData.surprise.push(earning.surprise);\n" +
                "            earnData.surprisePercent.push(earning.surprisePercent);\n" +
                "            var surpriseValue = earning.surprise;\n" +
                "            xAxisLabels.push(earning.period + '<br>Surprise: ' + surpriseValue);\n" +
                "        }\n" +
                "        Highcharts.chart('container', {\n" +
                "            chart: {\n" +
                "                type: 'spline',\n" +
                "                events: {\n" +
                "                    load: function () {\n" +
                "                        window.addEventListener('resize', () => {\n" +
                "                            this.reflow();\n" +
                "                        });\n" +
                "                    }\n" +
                "                }\n" +
                "            },\n" +
                "            title: {\n" +
                "                text: 'Historical EPS Surprises',\n" +
                "                align: 'center',\n" +
                "            },\n" +
                "            xAxis: {\n" +
                "                title: {\n" +
                "                    text: 'Quarterly EPS',\n" +
                "                },\n" +
                "                labels: {\n" +
                "                    format: '{value}',\n" +
                "                    formatter: function () {\n" +
                "                        return xAxisLabels[this.pos];\n" +
                "                    },\n" +
                "                    style: {\n" +
                "                        fontSize: '12px'\n" +
                "                    },\n" +
                "                    rotation: -45\n" +
                "                },\n" +
                "            },\n" +
                "            yAxis: {\n" +
                "                min: 0,\n" +
                "                title: {\n" +
                "                    text: 'Quarterly EPS',\n" +
                "                },\n" +
                "                stackLabels: {\n" +
                "                    enabled: true\n" +
                "                }\n" +
                "            },\n" +
                "            legend: {\n" +
                "                verticalAlign: 'bottom',\n" +
                "                y: 10,\n" +
                "                shadow: false\n" +
                "            },\n" +
                "            tooltip: {\n" +
                "                shared: true \n" +
                "            },\n" +
                "            plotOptions: {\n" +
                "                column: {\n" +
                "                    stacking: 'normal',\n" +
                "                    dataLabels: {\n" +
                "                        enabled: true\n" +
                "                    }\n" +
                "                }\n" +
                "            },\n" +
                "            series: [\n" +
                "                {\n" +
                "                    name: 'Actual',\n" +
                "                    data: earnData.actual,\n" +
                "                },\n" +
                "                {\n" +
                "                    name: 'Estimate',\n" +
                "                    data: earnData.estimate\n" +
                "                }\n" +
                "            ],\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        earningswebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }
    private void loadHtmlContent(JSONArray recommendationData) {
        // Construct HTML content with recommendation data
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Recommendation Trends</title>\n" +
                "    <script src=\"https://code.highcharts.com/highcharts.js\"></script>\n" +
               " <script src=\"https://code.highcharts.com/modules/exporting.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"container\" style=\"width:100%; height:400px;\"></div>\n" +
                "    <script>\n" +
                "        var recoData = {\n" +
                "            buy: [],\n" +
                "            hold: [],\n" +
                "            period: [],\n" +
                "            sell: [],\n" +
                "            strongBuy: [],\n" +
                "            strongSell: [],\n" +
                "            symbol: []\n" +
                "        };\n" +
                "        var recommendationData = " + recommendationData.toString() + ";\n" +
        "for (var i = 0; i < recommendationData.length; i++) {\n" +
            "            var recommendation = recommendationData[i];\n" +
                    "            recoData.buy.push(recommendation.buy);\n" +
                    "            recoData.hold.push(recommendation.hold);\n" +
                    "            recoData.period.push(recommendation.period);\n" +
                    "            recoData.sell.push(recommendation.sell);\n" +
                    "            recoData.strongBuy.push(recommendation.strongBuy);\n" +
                    "            recoData.strongSell.push(recommendation.strongSell);\n" +
                    "            recoData.symbol.push(recommendation.symbol);\n" +
                    "        }\n" +
                "        Highcharts.chart('container', {\n" +
                "            chart: {\n" +
                "                type: 'column',\n" +
                "                events: {\n" +
                "                    load: function () {\n" +
                "                        window.addEventListener('resize', () => {\n" +
                "                            this.reflow();\n" +
                "                        });\n" +
                "                    }\n" +
                "                }\n" +
                "            },\n" +
                "            title: {\n" +
                "                text: 'Recommendation Trends',\n" +
                "                align: 'center'\n" +
                "            },\n" +
                "            xAxis: {\n" +
                "                categories: recoData.period\n" +
                "            },\n" +
                "            yAxis: {\n" +
                "                min: 0,\n" +
                "                title: {\n" +
                "                    text: '#Analysis'\n" +
                "                }\n" +
                "            },\n" +
                "            legend: {\n" +
                "                verticalAlign: 'bottom',\n" +
                "                y: 10,\n" +
                "                backgroundColor: Highcharts.defaultOptions.legend ? Highcharts.defaultOptions.legend.backgroundColor || 'white' : 'white',\n" +
                "                shadow: false\n" +
                "            },\n" +
                "            tooltip: {\n" +
                "                headerFormat: '<b>{point.x}</b><br/>',\n" +
                "                pointFormat: '{series.name}: {point.y}<br/>Total: {point.total}'\n" +
                "            },\n" +
                "            plotOptions: {\n" +
                "                column: {\n" +
                "                    stacking: 'normal',\n" +
                "                    dataLabels: {\n" +
                "                        enabled: true\n" +
                "                    }\n" +
                "                }\n" +
                "            },\n" +
                "            series: [{\n" +
                "                name: 'strongBuy',\n" +
                "                data: recoData.strongBuy,\n" +
                "                color: '#176333'\n" +
                "            }, {\n" +
                "                name: 'Buy',\n" +
                "                data: recoData.buy,\n" +
                "                color: '#25af51'\n" +
                "            }, {\n" +
                "                name: 'Hold',\n" +
                "                data: recoData.hold,\n" +
                "                color: '#ae7e27'\n" +
                "            }, {\n" +
                "                name: 'Sell',\n" +
                "                data: recoData.sell,\n" +
                "                color: '#f15053'\n" +
                "            }, {\n" +
                "                name: 'Strong Sell',\n" +
                "                data: recoData.strongSell,\n" +
                "                color: '#752b2c'\n" +
                "            }]\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        // Load the HTML content into the WebView
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }
    private void showTransactionSuccessDialog(String transactionType, String ticker, double inputValue) {
        // Create the dialog
        int quantity = (int) inputValue;
        final Dialog dialog = new Dialog(SearchResultsActivity.this);
        dialog.setContentView(R.layout.dialog_success);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Transparent background

        // Initialize views
        TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
        TextView dialogMessage = dialog.findViewById(R.id.dialogMessage);

        // Set title and message based on transaction type (buy/sell) and quantity
        dialogTitle.setText("Congratulations!");
        String message = "You have successfully " + transactionType + " " + quantity + " shares of " + ticker;
        dialogMessage.setText(message);

        Button doneButton = dialog.findViewById(R.id.doneButton);

// Set OnClickListener for the "Done" button
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the dialog when the button is clicked
                dialog.dismiss();
            }
        });

        // Show the dialog
        dialog.show();
    }
    private void sellFromPortfolio(JSONObject searchResults, double quantity, double currentPrice) {
        try {
            String symbol = searchResults.getString("ticker");
            String companyName = searchResults.getString("name");

            JSONObject portfolioData = new JSONObject();
            portfolioData.put("symbol", symbol);
            portfolioData.put("companyName", companyName);
            portfolioData.put("quantity", quantity);
            portfolioData.put("currentPrice", currentPrice);

            // Create a request to sell from the portfolio
            JsonObjectRequest sellFromPortfolioRequest = new JsonObjectRequest(Request.Method.POST, "https://webtech-hw3-419101.wl.r.appspot.com/sell-from-portfolio", portfolioData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Handle response
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Handle error
                }
            });

            // Add the request to the queue
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(sellFromPortfolioRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void addToPortfolio(JSONObject searchResults, double quantity, double currentPrice) {
        try {
            String symbol = searchResults.getString("ticker");
            String companyName = searchResults.getString("name");
            double total = currentPrice * quantity;

            JSONObject portfolioData = new JSONObject();
            portfolioData.put("symbol", symbol);
            portfolioData.put("companyName", companyName);
            portfolioData.put("quantity", quantity);
            portfolioData.put("total", total);
            portfolioData.put("currentPrice", currentPrice);

            // Create a request to add to the portfolio
            JsonObjectRequest addToPortfolioRequest = new JsonObjectRequest(Request.Method.POST, "https://webtech-hw3-419101.wl.r.appspot.com/add-to-portfolio", portfolioData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Handle response
                            Log.d("Portfolio", "Added to portfolio: " + response.toString());
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Handle error
                    Log.e("Portfolio", "Error adding to portfolio: " + error.toString());
                }
            });

            // Add the request to the queue
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(addToPortfolioRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void updateWalletBalance(double inputValue) {
        // Instantiate the RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // URL of the endpoint for updating wallet balance
        String url = "https://webtech-hw3-419101.wl.r.appspot.com/update-wallet";

        // Create the JSON object for the request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("newBalance", inputValue); // Assuming inputValue represents the new balance
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create the POST request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful response
                        Toast.makeText(SearchResultsActivity.this, "Wallet updated successfully", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        Toast.makeText(SearchResultsActivity.this, "Error updating wallet", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }
    // Inside your activity or fragment
    private void showTradeDialog() throws JSONException {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.trade_dialog_layout, null);
        builder.setView(dialogView);

        String searchResultsJson = getIntent().getStringExtra("search_results");

        // Convert the JSON string back to a JSONObject
        JSONObject searchResults = null;
        try {
            searchResults = new JSONObject(searchResultsJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TextView modalHeader = dialogView.findViewById(R.id.modalHeaderText);
        if (searchResults != null) {
            String headerText = "Trade " + searchResults.getString("name") + " shares";
            modalHeader.setText(headerText);
        }

        EditText numericInputField = dialogView.findViewById(R.id.numeric_input_field);
        TextView calculationTextView = dialogView.findViewById(R.id.calculation_text_view);
        TextView availableAmountTextView = dialogView.findViewById(R.id.available_amount_text_view);


        double currentPrice = latestPrice;
        double input;
        try {
            input = Double.parseDouble(numericInputField.getText().toString());
        } catch (NumberFormatException e) {
            input = 0;
        }
        calculationTextView.setText(String.format(Locale.getDefault(),"%.2f*$%.2f/share = %.2f", input, currentPrice, input*currentPrice));

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://webtech-hw3-419101.wl.r.appspot.com/balance";

        // Request a string response from the provided URL
        JSONObject finalSearchResults1 = searchResults;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parse the JSON response
                            JSONObject jsonResponse = new JSONObject(response);

                            // Extract the balance from the response
                            double balance = jsonResponse.getDouble("balance");

                            // Set the text for availableAmountTextView
                            String ticker = finalSearchResults1.getString("ticker");
                            String availableAmountText = String.format(Locale.getDefault(), "$%.2f to buy %s", balance, ticker);
                            availableAmountTextView.setText(availableAmountText);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
                error.printStackTrace();
            }
        });

        // Add the request to the RequestQueue
        queue.add(stringRequest);



        // Logic to update calculationTextView based on numeric input
        JSONObject finalSearchResults = searchResults;
        numericInputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                try {
                    String inputString = s.toString();
                    double inputValue;

                    if (!inputString.isEmpty()) {
                        inputValue = Double.parseDouble(inputString);
                    } else {
                        // Handle the case where the input string is empty
                        inputValue = 0.0; // Or any default value you prefer
                    }
                    double currentPricePerShare = finalSearchResults.getJSONObject("quoteDetails").getDouble("c");
                    double result = inputValue * currentPricePerShare;
                    calculationTextView.setText(String.format(Locale.getDefault(),"%.2f*$%.2f/share = %.2f", inputValue, currentPricePerShare, result));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Update calculationTextView based on numeric input
                // Example: calculationTextView.setText("Calculated amount: " + calculateAmount(s));
                try {
                    String inputString = s.toString();
                    double inputValue;

                    if (!inputString.isEmpty()) {
                        inputValue = Double.parseDouble(inputString);
                    } else {
                        // Handle the case where the input string is empty
                        inputValue = 0.0; // Or any default value you prefer
                    }
                    double currentPricePerShare = finalSearchResults.getJSONObject("quoteDetails").getDouble("c");
                    double result = inputValue * currentPricePerShare;
                    calculationTextView.setText(String.format("%.2f*$%.2f/share = %.2f", inputValue, currentPricePerShare, result));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });



        // Logic to update availableAmountTextView with current available amount

        // Set up dialog buttons

        // Create and show the dialog
        dialog = builder.create();
        dialog.show();
    }
    public void onTradeButtonClick(View view) throws JSONException {
        showTradeDialog();
    }

    private void addToWatchlist(String symbol, JSONObject searchResultsObject) {
        // Create request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("symbol", symbol);
            requestBody.put("companyName", searchResultsObject.getString("name"));
            requestBody.put("lastPrice", searchResultsObject.getJSONObject("quoteDetails").getDouble("c"));
            requestBody.put("change", searchResultsObject.getJSONObject("quoteDetails").getDouble("d"));
            requestBody.put("percentChange", searchResultsObject.getJSONObject("quoteDetails").getDouble("dp"));
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle JSON creation error
        }

        // Create POST request
        JsonObjectRequest addToWatchlistRequest = new JsonObjectRequest(Request.Method.POST, "https://webtech-hw3-419101.wl.r.appspot.com/add-to-watchlist", requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful response (optional)
                        // For example, show a toast message indicating success
                        Toast.makeText(SearchResultsActivity.this, "Company added to watchlist", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response (optional)
                        // For example, show a toast message indicating error
                        Toast.makeText(SearchResultsActivity.this, "Failed to add company to watchlist", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add request to Volley request queue
        Volley.newRequestQueue(this).add(addToWatchlistRequest);
    }
    private void removeFromWatchlist(String symbol) {
        // Create request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("symbol", symbol);
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle JSON creation error
        }

        // Create POST request
        JsonObjectRequest removeFromWatchlistRequest = new JsonObjectRequest(Request.Method.POST, "https://webtech-hw3-419101.wl.r.appspot.com/remove-from-watchlist", requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful response (optional)
                        // For example, show a toast message indicating success
                        Toast.makeText(SearchResultsActivity.this, "Company removed from watchlist", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response (optional)
                        // For example, show a toast message indicating error
                        Toast.makeText(SearchResultsActivity.this, "Failed to remove company from watchlist", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add request to Volley request queue
        Volley.newRequestQueue(this).add(removeFromWatchlistRequest);
    }

    private static String loadHourlyHtmlContent( JSONArray hourlyData) {
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Hourly Price Variation</title>\n" +
                "<script src=\"https://code.highcharts.com/stock/highstock.js\"></script>\n" +
    "<script src=\"https://code.highcharts.com/stock/modules/drag-panes.js\"></script>\n" +
    "<script src=\"https://code.highcharts.com/stock/indicators/indicators.js\"></script>\n" +
//                "    <script src=\"https://code.highcharts.com/highcharts.js\"></script>\n" +
//                "    <script src=\"https://code.highcharts.com/modules/exporting.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"container\" style=\"width:100%; height:400px;\"></div>\n" +
                "    <script>\n" +
                "        var hourlyData = " + hourlyData.toString() + ";\n" +
                "        var timestamps = hourlyData.map(function(result) { return new Date(result.t).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false }); });\n" +
                "        var stockPrices = hourlyData.map(function(result) { return result.c; });\n" +
                "        var data = timestamps.map(function(timestamp, index) { return [timestamp, stockPrices[index]]; });\n" +
                "        var totalEntries = timestamps.length;\n" +
                "        var stepSize = Math.ceil(totalEntries / 10);\n" +
                "        Highcharts.chart('container', {\n" +
                "            chart: {\n" +
                "                type: 'line',\n" +
                "                backgroundColor: '#f0f0f0'\n" +
                "            },\n" +
                "            scrollbar: {\n" +
                "                enabled: true\n" +
                "            },\n" +
                "            legend: {\n" +
                "                enabled: false\n" +
                "            },\n" +
                "            title: {\n" +
                "                text: 'Hourly Price Variation'\n" +
                "            },\n" +
                "            xAxis: {\n" +
                "                categories: timestamps,\n" +
                "                tickInterval: stepSize,\n" +
                "                labels: {\n" +
                "                    formatter: function() {\n" +
                "                        return this.value.toString();\n" +
                "                    }\n" +
                "                },\n" +
                "                tickWidth: 2,\n" +
                "                tickLength: 10\n" +
                "            },\n" +
                "            yAxis: {\n" +
                "                title: {\n" +
                "                    text: null\n" +
                "                },\n" +
                "                opposite: true\n" +
                "            },\n" +
                "            navigator: {\n" +
                "                enabled: true\n" +
                "            },\n" +
                "            series: [{\n" +
                "                name: '',\n" +
                "                type: 'line',\n" +
                "                color: 'blue', // Change color as needed\n" +
                "                data: data,\n" +
                "                marker: {\n" +
                "                    enabled: false\n" +
                "                }\n" +
                "            }]\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        return htmlContent;

    }
    public class MyFragmentAdapter extends FragmentStateAdapter {

        public MyFragmentAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new HighchartsFragment1();  // Replace with your Fragment class for Tab 1
                case 1:
                    return new HighchartsFragment2();  // Replace with your Fragment class for Tab 2
                default:
                    return null;
            }
        }
        @Override
        public int getItemCount() {
            return 2;  // Number of tabs
        }
    }

    private static String loadHistoricalHtmlContent( JSONArray historicalData) {
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Historical Price Data</title>\n" +
                "    <script src=\"https://code.highcharts.com/highcharts.js\"></script>\n" +
                "    <script src=\"https://code.highcharts.com/modules/exporting.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"container\" style=\"width:100%; height:400px;\"></div>\n" +
                "    <script>\n" +
                "        const ohlc = [];\n" +
                "        const volume = [];\n" +
                "        const groupingUnits = [['day', [1]], ['week', [1]], ['month', [1, 3, 6]], ['year', null]];\n" +
                "        var hourlyData = " + historicalData.toString() + ";\n" +
                "        historicalData.forEach((result) => {\n" +
                "            const timestamp = result.t;\n" +
                "            const open = result.o;\n" +
                "            const high = result.h;\n" +
                "            const low = result.l;\n" +
                "            const close = result.c;\n" +
                "            const vol = result.v;\n" +
                "            ohlc.push([timestamp, open, high, low, close]);\n" +
                "            volume.push([timestamp, vol]);\n" +
                "        });\n" +
                "        Highcharts.chart('container', {\n" +
                "            chart: {\n" +
                "                backgroundColor: '#f0f0f0'\n" +
                "            },\n" +
                "            accessibility: {\n" +
                "                enabled: false\n" +
                "            },\n" +
                "            scrollbar: {\n" +
                "                enabled: true\n" +
                "            },\n" +
                "            legend: {\n" +
                "                enabled: false\n" +
                "            },\n" +
                "            rangeSelector: {\n" +
                "                allButtonsEnabled: true,\n" +
                "                enabled: true,\n" +
                "                inputEnabled: true,\n" +
                "                buttons: [{\n" +
                "                    type: 'month',\n" +
                "                    count: 1,\n" +
                "                    text: '1m'\n" +
                "                }, {\n" +
                "                    type: 'month',\n" +
                "                    count: 3,\n" +
                "                    text: '3m'\n" +
                "                }, {\n" +
                "                    type: 'month',\n" +
                "                    count: 6,\n" +
                "                    text: '6m'\n" +
                "                }, {\n" +
                "                    type: 'ytd',\n" +
                "                    text: 'YTD'\n" +
                "                }, {\n" +
                "                    type: 'year',\n" +
                "                    count: 1,\n" +
                "                    text: '1y'\n" +
                "                }, {\n" +
                "                    type: 'all',\n" +
                "                    text: 'All'\n" +
                "                }],\n" +
                "                selected: 2\n" +
                "            },\n" +
                "            title: {\n" +
                "                text: '" + " Historical'\n" +
                "            },\n" +
                "            subtitle: {\n" +
                "                text: 'With SMA and Volume by Price technical indicators'\n" +
                "            },\n" +
                "            xAxis: {\n" +
                "                type: 'datetime',\n" +
                "                labels: {\n" +
                "                    formatter: function() {\n" +
                "                        const date = new Date(this.value);\n" +
                "                        const day = date.getDate();\n" +
                "                        const month = date.toLocaleString('default', { month: 'short' });\n" +
                "                        return day + ' ' + month;\n" +
                "                    }\n" +
                "                }\n" +
                "            },\n" +
                "            yAxis: [{\n" +
                "                startOnTick: false,\n" +
                "                endOnTick: false,\n" +
                "                labels: {\n" +
                "                    align: 'right',\n" +
                "                    x: -3\n" +
                "                },\n" +
                "                title: {\n" +
                "                    text: 'OHLC'\n" +
                "                },\n" +
                "                height: '60%',\n" +
                "                lineWidth: 2,\n" +
                "                resize: {\n" +
                "                    enabled: true\n" +
                "                },\n" +
                "                opposite: true\n" +
                "            }, {\n" +
                "                labels: {\n" +
                "                    align: 'right',\n" +
                "                    x: -3\n" +
                "                },\n" +
                "                title: {\n" +
                "                    text: 'Volume'\n" +
                "                },\n" +
                "                top: '65%',\n" +
                "                height: '35%',\n" +
                "                offset: 0,\n" +
                "                lineWidth: 2,\n" +
                "                opposite: true\n" +
                "            }],\n" +
                "            tooltip: {\n" +
                "                split: true\n" +
                "            },\n" +
                "            plotOptions: {\n" +
                "                series: {\n" +
                "                    dataGrouping: {\n" +
                "                        units: groupingUnits\n" +
                "                    }\n" +
                "                }\n" +
                "            },\n" +
                "            series: [{\n" +
                "                type: 'candlestick',\n" +
                "                name: '" + "',\n" +
                "                id: '" + "',\n" +
                "                zIndex: 2,\n" +
                "                data: ohlc\n" +
                "            }, {\n" +
                "                type: 'column',\n" +
                "                name: 'Volume',\n" +
                "                id: 'volume',\n" +
                "                data: volume,\n" +
                "                yAxis: 1\n" +
                "            }, {\n" +
                "                type: 'vbp',\n" +
//                "                linkedTo: '" + "',\n" +
                "                params: {\n" +
                "                    volumeSeriesID: 'volume'\n" +
                "                },\n" +
                "                dataLabels: {\n" +
                "                    enabled: false\n" +
                "                },\n" +
                "                zoneLines: {\n" +
                "                    enabled: false\n" +
                "                }\n" +
                "            }, {\n" +
                "                type: 'sma',\n" +
//                "                linkedTo: '" + "',\n" +
                "                zIndex: 1,\n" +
                "                marker: {\n" +
                "                    enabled: false\n" +
                "                }\n" +
                "            }],\n" +
                "            navigator: {\n" +
                "                enabled: true\n" +
                "            }\n" +
                "        };\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
        return htmlContent;
    }

    public static class HighchartsFragment1 extends Fragment {
        private WebView webView;
        private double changeValue;
        private String ticker;
        private JSONArray hourlyData;
        private String htmlContent;

        public HighchartsFragment1 newInstance(JSONArray hourlyData) {
            HighchartsFragment1 fragment = new HighchartsFragment1();
            fragment.hourlyData = hourlyData;
            return fragment;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView =  inflater.inflate(R.layout.fragment_highcharts1, container, false);  // Replace with your fragment layout (optional)
            webView = rootView.findViewById(R.id.highcharts_webview);
            webView.getSettings().setJavaScriptEnabled(true);
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            Intent intent = getActivity().getIntent();
            String searchResultsJson = intent.getStringExtra("search_results");
            JSONObject searchResultsObject = null;
            try {
                searchResultsObject = new JSONObject(searchResultsJson);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            try {
                ticker = searchResultsObject.getString("ticker");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            JsonArrayRequest hourlyRequest = new JsonArrayRequest(Request.Method.GET,"https://webtech-hw3-419101.wl.r.appspot.com/hourly?symbol=" + ticker , null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            // Parse recommendation data from JSON response
                            // Handle the recommendation data as needed
                            Log.d("Hourly", "Hourly response" + response.toString());
                            htmlContent = loadHourlyHtmlContent(response);
                            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Handle error response
                    Toast.makeText(getActivity(), "Error fetching hourly data", Toast.LENGTH_SHORT).show();
                }
            });

            queue.add(hourlyRequest);

//            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null);
            return rootView;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            try { Intent intent = getActivity().getIntent();
                String searchResultsJson = intent.getStringExtra("search_results");
                Log.d("searchResults", "SearchResults: " + searchResultsJson.toString());
                JSONObject searchResultsObject = new JSONObject(searchResultsJson);
                String hourlyDataString = searchResultsObject.getString("hourlyData");
                try {
                    // Assuming hourlyDataString is the JSON string containing hourly data
                    hourlyData = new JSONArray(hourlyDataString);

                    // Now you can access individual elements in the JSONArray
                    for (int i = 0; i < hourlyData.length(); i++) {
                        JSONObject item = hourlyData.getJSONObject(i);
                        // Extract other values as needed
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("HourlyData", "Hourly Data: " + hourlyData.toString());
                changeValue = searchResultsObject.getJSONObject("quoteData").getDouble("d");
                ticker = searchResultsObject.getString("ticker");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            webView.addJavascriptInterface(new WebAppInterface(), "Android");
        }

        public class WebAppInterface {
            @JavascriptInterface
            public String getHourlyData() {
//                Log.d("WebAppInterface", "Hourly Data in getHourlyData: " + hourlyData.toString());
                return hourlyData != null ? hourlyData.toString() : null;
            }
            @JavascriptInterface
            public double getChangeValue() {
                return changeValue;
            }
            @JavascriptInterface
            public String getTicker() {
                return ticker;
            }
        }
    }

    public static class HighchartsFragment2 extends Fragment {
        private WebView webView;
        private String ticker;
        private JSONArray historicalData;

        public static HighchartsFragment2 newInstance() {
            return new HighchartsFragment2();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_highcharts2, container, false);
            webView = rootView.findViewById(R.id.highcharts_webview1);
            webView.getSettings().setJavaScriptEnabled(true);

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            Intent intent = getActivity().getIntent();
            String searchResultsJson = intent.getStringExtra("search_results");
            JSONObject searchResultsObject = null;
            try {
                searchResultsObject = new JSONObject(searchResultsJson);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            try {
                ticker = searchResultsObject.getString("ticker");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            JsonArrayRequest historicalRequest = new JsonArrayRequest(Request.Method.GET, "https://webtech-hw3-419101.wl.r.appspot.com/historical?q=" + ticker, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.d("Historical", "Historical response" + response.toString());
                            historicalData = response;
                            loadChart();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getActivity(), "Error fetching historical data", Toast.LENGTH_SHORT).show();
                }
            });
            queue.add(historicalRequest);

            return rootView;
        }

        private void loadChart() {
            // Pass historical data and ticker to JavaScript using WebView's addJavascriptInterface method
            webView.addJavascriptInterface(new HighchartsJavascriptInterface(historicalData, ticker), "Android");

            // Load the HTML content with WebView
            webView.loadUrl("file:///android_asset/index.html");
        }

        // Inner class to handle JavaScript interface
        private class HighchartsJavascriptInterface {
            private JSONArray historicalData;
            private String ticker;

            public HighchartsJavascriptInterface(JSONArray historicalData, String ticker) {
                this.historicalData = historicalData;
                this.ticker = ticker;
            }

            @JavascriptInterface
            public String getHistoricalData() {
                // Convert historicalData JSONArray to a string and return it
                return historicalData.toString();
            }

            @JavascriptInterface
            public String getTicker() {
                // Return the ticker symbol
                return ticker;
            }
        }
    }


//    public static class HighchartsFragment2 extends Fragment {
//        private WebView webView;
//        private String ticker;
//        private String htmlContent;
//        private JSONArray historicalData;
//
//        // Method to create a new instance of the fragment with historical data
//
//
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_highcharts2, container, false);
//            webView = rootView.findViewById(R.id.highcharts_webview1);
//            webView.getSettings().setJavaScriptEnabled(true);
//
//            // Pass historical data to JavaScript using WebView's addJavascriptInterface method
//            webView.addJavascriptInterface(new HighchartsJavascriptInterface(historicalData), "Android");
//
//            // Load the HTML content with WebView
//            webView.loadUrl("file:///android_asset/index.html");
//
//            return rootView;
//        }
//
//        // Inner class to handle JavaScript interface
//        private class HighchartsJavascriptInterface {
//            private JSONArray historicalData;
//
//            public HighchartsJavascriptInterface(JSONArray historicalData) {
//                this.historicalData = historicalData;
//            }
//
//            @JavascriptInterface
//            public String getHistoricalData() {
//                // Convert historicalData JSONArray to a string and return it
//                return historicalData.toString();
//            }
//        }
//    }


//    public static class HighchartsFragment2 extends Fragment {
//        private WebView webView;
//        private String ticker;
//        private String htmlContent;
//        private JSONArray historicalData;
//
//        public HighchartsFragment2 newInstance(JSONArray historicalData) {
//            HighchartsFragment2 fragment = new HighchartsFragment2();
//            fragment.historicalData = historicalData;
//            return fragment;
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//            View rootView =  inflater.inflate(R.layout.fragment_highcharts2, container, false);  // Replace with your fragment layout (optional)
//            webView = rootView.findViewById(R.id.highcharts_webview1);
//            webView.getSettings().setJavaScriptEnabled(true);
//            RequestQueue queue = Volley.newRequestQueue(getActivity());
//            Intent intent = getActivity().getIntent();
//            String searchResultsJson = intent.getStringExtra("search_results");
//            JSONObject searchResultsObject = null;
//            try {
//                searchResultsObject = new JSONObject(searchResultsJson);
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//            try {
//                ticker = searchResultsObject.getString("ticker");
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//            JsonArrayRequest historicalRequest = new JsonArrayRequest(Request.Method.GET, "http://10.0.2.2:8080/historical?q=" + ticker, null,
//                    new Response.Listener<JSONArray>() {
//                        @Override
//                        public void onResponse(JSONArray response) {
//                            Log.d("Historical", "Historical response" + response.toString());
//                            htmlContent = loadHistoricalHtmlContent(response);
//                            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
//                        }
//                    }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Toast.makeText(getActivity(), "Error fetching historical data", Toast.LENGTH_SHORT).show();
//                }
//            });
//            queue.add(historicalRequest);
//            return rootView;
//        }
//    }

}
