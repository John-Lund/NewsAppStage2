package com.example.android.newsappstage2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import java.util.List;

import static com.example.android.newsappstage2.R.string.us_news;

public class NewsAdapter extends ArrayAdapter<NewsObject> {
    private Context context;

    public NewsAdapter(@NonNull Context context, @NonNull List<NewsObject> objects) {
        super(context, 0, objects);
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        // creating convertView if it doesn't already exist and setting up viewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.news_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        NewsObject objectToDisplay = getItem(position);
        // adding details to list view item
        assert objectToDisplay != null;
        if (!objectToDisplay.getAuthor().equals(Constants.NOT_KNOWN)) {
            viewHolder.authorText.setText(objectToDisplay.getAuthor());
        }
        viewHolder.titleText.setText(objectToDisplay.getTitle());
        viewHolder.dateText.setText(objectToDisplay.getDate());
        viewHolder.sectionText.setText(objectToDisplay.getSection() + " /");
        // checking for available image - if none found, using placeholder image
        if (objectToDisplay.getImage() != null) {
            viewHolder.newsImage.setImageBitmap(objectToDisplay.getImage());
        } else {
            viewHolder.newsImage.setImageResource(R.drawable.placeholder_500x300);
        }
        // setting colours
        int colour = getCategoryColour(objectToDisplay.getSection(), viewHolder);
        viewHolder.backGround1.setBackgroundColor(colour);
        viewHolder.backGround2.setBackgroundColor(colour);
        viewHolder.sectionText.setTextColor(colour);
        viewHolder.authorText.setTextColor(colour);
        return convertView;
    }

    // setting up viewHolder class
    private class ViewHolder {
        final TextView titleText;
        final TextView authorText;
        final TextView sectionText;
        final TextView dateText;
        final ImageView newsImage;
        final ImageView backGround1;
        final ImageView backGround2;

        ViewHolder(View view) {
            this.titleText = view.findViewById(R.id.title_text);
            this.authorText = view.findViewById(R.id.author_text);
            this.dateText = view.findViewById(R.id.date_text);
            this.sectionText = view.findViewById(R.id.section_text);
            this.newsImage = view.findViewById(R.id.news_image);
            this.backGround1 = view.findViewById(R.id.background_image1);
            this.backGround2 = view.findViewById(R.id.background_image2);
        }
    }

    // method to get the right colour for each section category and to truncate unknown and long section names

    @SuppressLint("SetTextI18n")
    private int getCategoryColour(String category, ViewHolder viewHolder) {
        int categoryColourId;
        if (context.getString(R.string.news_colour_categories).contains(category)) {
            categoryColourId = R.color.news;
        } else if (context.getString(R.string.opinion_colour_categories).contains(category)) {
            categoryColourId = R.color.opinion;
        } else if (context.getString(R.string.sports_colour_categories).contains(category)) {
            categoryColourId = R.color.sports;
        } else if (context.getString(R.string.culture_colour_categories).contains(category)) {
            categoryColourId = R.color.culture;
        } else if (context.getString(R.string.lifestyle_colour_categories).contains(category)) {
            categoryColourId = R.color.lifestyle;
        } else {
            categoryColourId = R.color.unclassified;
            int index = category.indexOf(" ", category.indexOf(" ") + 1);

            if (index != -1) {
                category = category.substring(0, index);
                viewHolder.sectionText.setText(category + " /");
            }
        }

        return ContextCompat.getColor(getContext(), categoryColourId);
    }

}



















