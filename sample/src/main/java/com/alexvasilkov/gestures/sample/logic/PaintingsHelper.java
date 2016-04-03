package com.alexvasilkov.gestures.sample.logic;

import android.content.res.Resources;
import android.content.res.TypedArray;

import com.alexvasilkov.gestures.sample.R;

public class PaintingsHelper {

    private static Painting[] paintings;

    private PaintingsHelper() {}

    public static Painting[] list(Resources res) {
        if (paintings == null) {
            String[] authors = res.getStringArray(R.array.paintings_authors);
            String[] titles = res.getStringArray(R.array.paintings_titles);
            String[] links = res.getStringArray(R.array.paintings_links);
            TypedArray images = res.obtainTypedArray(R.array.paintings_images);

            int size = titles.length;
            paintings = new Painting[size];

            for (int i = 0; i < size; i++) {
                int imageId = images.getResourceId(i, -1);
                paintings[i] = new Painting(imageId, authors[i], titles[i], links[i]);
            }

            images.recycle();
        }

        return paintings;
    }

}
