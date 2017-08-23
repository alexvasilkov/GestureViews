package com.alexvasilkov.gestures.sample.logic;

public class Painting {

    public final int imageId;
    public final String author;
    public final String title;
    public final String link;

    Painting(int imageId, String author, String title, String link) {
        this.author = author;
        this.imageId = imageId;
        this.title = title;
        this.link = link;
    }

}
