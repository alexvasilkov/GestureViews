package com.alexvasilkov.gestures.sample.logic;

import android.content.res.Resources;
import android.content.res.TypedArray;

import com.alexvasilkov.gestures.sample.R;

public class Paintings {

    private Paintings() {}

    public static Painting[] list(Resources res) {
        final String[] authors = res.getStringArray(R.array.paintings_authors);
        final String[] titles = res.getStringArray(R.array.paintings_titles);
        final String[] links = res.getStringArray(R.array.paintings_links);

        final TypedArray images = res.obtainTypedArray(R.array.paintings_images);

        final int size = titles.length;
        final Painting[] paintings = new Painting[size];

        for (int i = 0; i < size; i++) {
            final int imageId = images.getResourceId(i, -1);
            paintings[i] = new Painting(imageId, authors[i], titles[i], links[i]);
        }

        images.recycle();

        return paintings;
    }

}
