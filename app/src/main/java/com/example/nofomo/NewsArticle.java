package com.example.nofomo;
public class NewsArticle {
    private String title;
    private String summary;
    private int imageResource;

    public NewsArticle(String title, String summary, int imageResource) {
        this.title = title;
        this.summary = summary;
        this.imageResource = imageResource;
    }

    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public int getImageResource() { return imageResource; }
}