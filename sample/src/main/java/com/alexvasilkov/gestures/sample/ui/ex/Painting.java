package com.alexvasilkov.gestures.sample.ui.ex;

import android.content.res.Resources;
import android.content.res.TypedArray;

import com.alexvasilkov.gestures.sample.R;

public class Painting {

    public final int imageId;
    public final int thumbId;
    public final String author;
    public final String title;
    public final String link;

    private Painting(int imageId, int thumbId, String author, String title, String link) {
        this.imageId = imageId;
        this.thumbId = thumbId;
        this.author = author;
        this.title = title;
        this.link = link;
    }


    public static Painting[] list(Resources res) {
        final String[] authors = res.getStringArray(R.array.paintings_authors);
        final String[] titles = res.getStringArray(R.array.paintings_titles);
        final String[] links = res.getStringArray(R.array.paintings_links);

        final TypedArray images = res.obtainTypedArray(R.array.paintings_images);
        final TypedArray thumbs = res.obtainTypedArray(R.array.paintings_thumbs);

        final int size = titles.length;
        final Painting[] paintings = new Painting[size];

        for (int i = 0; i < size; i++) {
            final int imageId = images.getResourceId(i, -1);
            final int thumbId = thumbs.getResourceId(i, -1);
            paintings[i] = new Painting(imageId, thumbId, authors[i], titles[i], links[i]);
        }

        images.recycle();
        thumbs.recycle();

        return paintings;
    }

}
