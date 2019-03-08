package com.example.android.newsappstage2;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import java.util.List;
import java.io.IOException;
import java.net.URL;

public class NewsLoader extends AsyncTaskLoader<List<NewsObject>> {
    private URL url;

    public NewsLoader(Context context, String url) {
        super(context);
        this.url = Utilities.createUrl(url);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public List<NewsObject> loadInBackground() {
        if (url == null) {
            return null;
        }
        String jsonResponse = "";
        try {
            jsonResponse = Utilities.loadJSON(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Utilities.extractNewsObjects(jsonResponse);
    }


}
