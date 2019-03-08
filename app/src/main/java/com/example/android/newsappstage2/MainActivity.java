package com.example.android.newsappstage2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.List;




public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewsObject>> {
    // setting up necessary fields
    private static final int NEWS_LOADER_ID = 1;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar bar;
    private TextView emptyState;
    private NewsAdapter adapter;
    private SharedPreferences sharedPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("ONCREATE: ","RECREATING ACTIVITY" );


        // referencing toolbar to replace action bar
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // creating sharedPreferences
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // referencing progress bar
        bar = findViewById(R.id.progressBar);

        // setting empty view
        ListView list = findViewById(R.id.list);
        emptyState = findViewById(R.id.noDataLoaded);
        list.setEmptyView(emptyState);

        // starting loader
        if (checkInternet()) {
            getSupportLoaderManager().initLoader(NEWS_LOADER_ID, null, this);
        } else {
            noInternet();
        }

        // setting up swipeRefresh listener
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getSupportLoaderManager().destroyLoader(NEWS_LOADER_ID);
                        if (checkInternet()) {

                            getSupportLoaderManager().initLoader(NEWS_LOADER_ID, null, MainActivity.this);
                        } else {
                            noInternet();
                            swipeRefresh.setRefreshing(false);
                        }
                    }
                }
        );
    }
    // setting up settings icon
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_icon, menu);
        return true;
    }

    // overriding onOptionsItemSelected to take user to settings activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, NewsSettings.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // overriding onCreateLoader
    @NonNull
    @Override
    public Loader<List<NewsObject>> onCreateLoader(int id, @Nullable Bundle args) {
        Log.e("ONCREATELOADER: ","CREATING NEW LOADER" );
        String newUrl = getURL(sharedPrefs);
        return new NewsLoader(this, newUrl);
    }

    // overriding onLoadFinished to update UI
    @Override
    public void onLoadFinished(@NonNull Loader<List<NewsObject>> loader, List<NewsObject> data) {
        swipeRefresh.setRefreshing(false);
        if (data != null) {
            updateUi(data);
            bar.setVisibility(View.GONE);
        } else {
            bar.setVisibility(View.GONE);
            emptyState.setText(R.string.no_server);
        }
    }
    // overriding onLoaderReset to clear adapter
    @Override
    public void onLoaderReset(@NonNull Loader<List<NewsObject>> loader) {
        adapter.clear();
    }

    // UI updating method
    public void updateUi(List<NewsObject> news) {

        ListView list = findViewById(R.id.list);
        adapter = new NewsAdapter(this, news);
        list.setAdapter(adapter);

        AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                NewsObject currentItem = adapter.getItem(position);
                assert currentItem != null;
                String link = currentItem.getLink();

                Uri webPage = Uri.parse(link);
                Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        };
        list.setOnItemClickListener(itemListener);
        if(news.size() == 0){
            searchFailed();

        }
    }

    // method to check internet connection
    private boolean checkInternet() {
        ConnectivityManager manager =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager != null ? manager.getActiveNetworkInfo() : null;
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    // method to inform the user if there is no internet connection
    private void noInternet() {
        bar.setVisibility(View.GONE);
        emptyState.setText(R.string.no_internet);
    }
    // method to inform the user that there were no items found using their search criteria
    @SuppressLint("SetTextI18n")
    private void searchFailed() {
        bar.setVisibility(View.GONE);
        emptyState.setText(getString(R.string.your_search_for) + "\n'" + sharedPrefs.getString(getString(R.string.search_key), "") + "'\n" + getString(R.string.returned_no_results));
    }

    // a method to construct a URL string using the search criteria from the user
    public String getURL(SharedPreferences sharedPrefs) {

        String section = sharedPrefs.getString(getString(R.string.sections_key), getString(R.string.all));
        String keyWords = "";

        if (!sharedPrefs.getString(getString(R.string.search_key), getString(R.string.none)).equals(getString(R.string.none))) {
            keyWords = sharedPrefs.getString(getString(R.string.search_key), getString(R.string.none));
        }
        if (!keyWords.equals("")) {
            keyWords = searchStringFormatter(keyWords);
        }
        /// Uri builder
        Uri baseUri = Uri.parse(Constants.BASE_URL);

        Uri.Builder builder = baseUri.buildUpon();

        if (!section.equals(getString(R.string.all))) {
            builder.appendQueryParameter(getString(R.string.section), section);

        }
        builder.appendQueryParameter(getString(R.string.production_office), getString(R.string.uk));

        if (section.equals(getString(R.string.news)) && keyWords.equals("")) {
            builder.appendQueryParameter(getString(R.string.order_by), getString(R.string.relevance));
        }

        builder.appendQueryParameter(getString(R.string.show_fields), getString(R.string.thumbnail));
        builder.appendQueryParameter(getString(R.string.page_size), sharedPrefs.getString(getString(R.string.requests_key), getString(R.string.default_request_number)));

        builder.appendQueryParameter(getString(R.string.show_tags), getString(R.string.contributor));

        if (!keyWords.equals("")) {
            builder.appendQueryParameter(getString(R.string.q), keyWords);
        }
        builder.appendQueryParameter(getString(R.string.api_key), getString(R.string.key_api));

        return builder.toString();

    }

    // a method to remove unwanted spaces from the user's search string and insert "AND" between words to allow for better searching
    private String searchStringFormatter(String keyWords) {
        while (keyWords.contains("  ")) {
            keyWords = keyWords.replace("  ", " ");
        }
        while (keyWords.startsWith(" ")) {
            keyWords = keyWords.substring(1);
        }
        while (keyWords.endsWith(" ")) {
            keyWords = keyWords.substring(0, keyWords.length() - 1);

        }
        if (keyWords.equals(" ")) {
            keyWords = "";
        }

        if (keyWords.contains(" ")) {
            keyWords = keyWords.replace(" ", " " + getString(R.string.AND) + " ");
        }

        return keyWords;
    }
}






































