package com.alexvasilkov.gestures.sample.logic;

public class Painting {

    private final int imageId;
    private final String author;
    private final String title;
    private final String link;

    Painting(int imageId, String author, String title, String link) {
        this.author = author;
        this.imageId = imageId;
        this.title = title;
        this.link = link;
    }

    public int getImageId() {
        return imageId;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

}
