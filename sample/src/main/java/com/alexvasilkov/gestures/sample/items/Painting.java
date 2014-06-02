package com.alexvasilkov.gestures.sample.items;

import android.content.res.Resources;
import android.content.res.TypedArray;
import com.alexvasilkov.gestures.sample.R;

public class Painting {

    private final int imageId;
    private final String title;

    private Painting(int imageId, String title) {
        this.imageId = imageId;
        this.title = title;
    }

    public int getImageId() {
        return imageId;
    }

    public String getTitle() {
        return title;
    }

    public static Painting[] getAllPaintings(Resources res) {
        String[] titles = res.getStringArray(R.array.paintings_titles);
        TypedArray images = res.obtainTypedArray(R.array.paintings_images);

        int size = titles.length;
        Painting[] paintings = new Painting[size];

        for (int i = 0; i < size; i++) {
            paintings[i] = new Painting(images.getResourceId(i, -1), titles[i]);
        }

        return paintings;
    }

}
