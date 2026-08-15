package com.example.nofomo;

public class NewsArticle {
    private String title;
    private String summary;
    private String imageBase64;

    public NewsArticle(String title, String summary, String imageBase64) {
        this.title = title;
        this.summary = summary;
        this.imageBase64 = imageBase64;
    }

    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getImageBase64() { return imageBase64; }
}