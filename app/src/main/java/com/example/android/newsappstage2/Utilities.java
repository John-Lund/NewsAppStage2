package com.example.android.newsappstage2;

import android.annotation.SuppressLint;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public final class Utilities {
    private Utilities() {
    }

    // creating NewsObjects List from JSON response
    public static List<NewsObject> extractNewsObjects(String jsonResponse) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<NewsObject> newsItems = new ArrayList<>();
        try {
            JSONObject jsonRootObject = new JSONObject(jsonResponse);

            JSONArray jsonArray = jsonRootObject.optJSONObject(Constants.RESPONSE).getJSONArray(Constants.RESULTS);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject JSONObject = jsonArray.getJSONObject(i);
                // extracting title and truncating it to remove the author
                String title = JSONObject.getString(Constants.WEBTITLE);
                int index = title.indexOf(Constants.STROKE);
                if (index != -1) {
                    title = title.substring(0, index - 1);
                }
                // extracting section name

                String section = JSONObject.getString(Constants.SECTION_NAME);
                // sorting out the date and time
                String date = JSONObject.getString(Constants.WEB_PUBLICATION_DATE);
                index = date.indexOf(Constants.Z);
                date = date.substring(0, index);
                date = timeConversion(date);

                // getting image thumbnail

                Bitmap image = null;
                String imageUrl = null;
                if (JSONObject.optJSONObject(Constants.FIELDS) != null) {
                    imageUrl = JSONObject.optJSONObject(Constants.FIELDS).optString(Constants.THUMBNAIL, Constants.EMPTY);
                    if (!imageUrl.equals(Constants.EMPTY)) {
                        image = getImage(imageUrl);
                    } else {
                        image = null;
                    }
                }
                // trying to get contributor image if news item has no thumbnail

                if (image == null) {
                    if (JSONObject.optJSONArray(Constants.TAGS) != null && JSONObject.optJSONArray(Constants.TAGS).length() == 1
                            && !JSONObject.optJSONArray(Constants.TAGS).getJSONObject(0).optString(Constants.BYLINE_IMAGE_URL, Constants.NONE).equals(Constants.NONE)) {
                        image = getImage(JSONObject.optJSONArray(Constants.TAGS).getJSONObject(0).optString(Constants.BYLINE_IMAGE_URL, Constants.NONE));
                    }
                }

                // getting link to article
                String link = JSONObject.getString(Constants.WEB_URL);

                // extracting the name of the author if there is one
                String author;
                try {
                    author = JSONObject.getJSONArray(Constants.TAGS).getJSONObject(0).getString(Constants.WEBTITLE);
                } catch (JSONException e) {
                    author = Constants.NOT_KNOWN;
                }
                // creating new NewsObject object
                NewsObject item = new NewsObject(title, author, section, date, image, link);
                newsItems.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newsItems;
    }

    // method to convert url string to URL
    public static URL createUrl(String urlString) {
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        return url;
    }

    // method to load JSON string
    public static String loadJSON(URL url) throws IOException {
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        if (url == null) {
            return null;
        }
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = convertStream(inputStream);
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    // method to convert input stream to string
    private static String convertStream(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(streamReader);
            String output = reader.readLine();
            while (output != null) {
                builder.append(output);
                output = reader.readLine();
            }
        }
        return builder.toString();
    }

    // method to load thumbnail images
    private static Bitmap getImage(String url) {
        if (url == null) {
            return null;
        }
        URL link = createUrl(url);
        try {
            assert link != null;
            return BitmapFactory.decodeStream(link.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // method to generate date string to add to url string to get today's news from Guardian
    public static String getDate() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date today = Calendar.getInstance().getTime();
        return dateFormatter.format(today);
    }

    // method to convert Guardian's time stamp to format suitable for UI
    private static String timeConversion(String jsonTime) {
        long milliSeconds;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat guardianTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date date = guardianTime.parse(jsonTime);
            milliSeconds = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateConverter = new SimpleDateFormat("MMM d yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeConverter = new SimpleDateFormat("h:mm a");
        String articleDate = dateConverter.format(milliSeconds);
        String articleTime = timeConverter.format(milliSeconds);
        return articleDate + "\n" + articleTime;
    }


}
