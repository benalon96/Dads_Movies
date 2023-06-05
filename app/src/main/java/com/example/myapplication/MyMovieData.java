package com.example.myapplication;

public class MyMovieData {

    private String descriptioMovie;
    private String movieName;

    private String movieImage;
    private String movieUrl;

    public String getMovieUrl() {
        return movieUrl;
    }

    public void setMovieUrl(String movieUrl) {
        this.movieUrl = movieUrl;
    }

    public MyMovieData(String movieName, String descriptioMovie, String movieImage, String movieUrl) {
        this.movieName = movieName;
        this.descriptioMovie = descriptioMovie;
        this.movieImage = movieImage;
        this.movieUrl = movieUrl;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getMovieDescription() {
        return descriptioMovie;
    }

    public void setMovieDate(String descriptionMovie) {
        this.descriptioMovie = descriptioMovie;
    }

    public String getMovieImage() {
        return movieImage;
    }

    public void setMovieImage(String movieImage) {
        this.movieImage = movieImage;
    }
}
