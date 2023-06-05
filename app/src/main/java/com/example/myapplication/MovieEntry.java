package com.example.myapplication;

import java.util.Date;

public class MovieEntry {
    private String url;
    private Date watchedDate;

    public MovieEntry(String url, Date watchedDate) {
        this.url = url;
        this.watchedDate = watchedDate;
    }

    public String getUrl() {
        return url;
    }

    public Date getWatchedDate() {
        return watchedDate;
    }
}
