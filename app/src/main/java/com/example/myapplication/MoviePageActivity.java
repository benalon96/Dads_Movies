package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Movie;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;

public class MoviePageActivity extends AppCompatActivity {

    private ImageView movieImagePage,movieImageSmallPage;
    private TextView movieTitlePage;
    private ImageButton playButton;
    private TextView movieDescriptionPage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_page);
        getSupportActionBar().hide();
        // Get the selected movie from the intent extras
        Intent intent = getIntent();
        String movieUrl="",movieName="",movieImage="",movieDescription="";
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            movieUrl = extras.getString("movieUrl");
            movieName = extras.getString("movieName");
            movieImage = extras.getString("movieImage");
            movieDescription = extras.getString("movieDescription");
        }



        // Find the UI elements
        playButton=findViewById(R.id.play_button_movie_page);
        movieImagePage = findViewById(R.id.movie_page_image);
        movieImageSmallPage = findViewById(R.id.movie_page_image_small);
        movieTitlePage = findViewById(R.id.movie_page_movie_name);
        movieDescriptionPage = findViewById(R.id.movie_page_description);

        // Set the movie details in the UI
        Picasso.get().load(movieImage).fit().centerCrop().into(movieImagePage);
        movieDescriptionPage.setText(movieDescription);
        movieTitlePage.setText(movieName);
        Picasso.get().load(movieImage).fit().into(movieImageSmallPage);

        // Set up the play button click listener
        String finalMovieUrl = movieUrl;
        playButton.setOnClickListener(view -> {
            // Start the movie player activity with the selected movie URL
            Intent intentVideo = new Intent(view.getContext(), VideoActivity.class);
            intentVideo.putExtra("movieUrl", finalMovieUrl);

            view.getContext().startActivity(intentVideo);
        });

        playButton.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                // Do something when the button gains focus
                playButton.setBackgroundColor(Color.parseColor("#D3D3D3"));
            } else {
                // Do something else when the button loses focus
                playButton.setBackgroundColor(Color.parseColor("#FF000000"));
            }
        });
        // Scroll down to the play button
        playButton.requestFocus();
        playButton.post(() -> playButton.requestFocusFromTouch());
    }
}
