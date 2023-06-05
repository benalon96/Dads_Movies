package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.myapplication.R;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class VideoActivity extends AppCompatActivity {
    PlayerView playerView;
    String fullTime;
    SimpleExoPlayer player;
    Set<String> watchedUrls;
    Map<String,String> inputMap1;
    String videoUrl = "";
    long duration;
    long lastPlayedPosition;
    private Handler keepAliveHandler = new Handler();
    private Runnable keepAliveRunnable = new Runnable() {
        @Override
        public void run() {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            keepAliveHandler.postDelayed(this, 5000); // Keep alive every 5 seconds
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        watchedUrls= sharedPreferences.getStringSet("watched_urls", new HashSet() {
        });
//        watchedUrls = new HashSet<>(stringSet);
        Log.d(watchedUrls.toString(),"stringSet");
        keepAliveHandler.postDelayed(keepAliveRunnable, 5000);
        player.play();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }
    private Map<String,String> loadMap() {
        Map<String,String> outputMap = new HashMap<>();
        SharedPreferences pSharedPref = getApplicationContext().getSharedPreferences("MyVariables", Context.MODE_PRIVATE);
        try {
            if (pSharedPref != null) {
                String jsonString = pSharedPref.getString("My_map", (new JSONObject()).toString());
                if (jsonString != null) {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    Iterator<String> keysItr = jsonObject.keys();
                    while (keysItr.hasNext()) {
                        String key = keysItr.next();
                        String value = jsonObject.getString(key);
                        outputMap.put(key, value);
                    }
                }
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return outputMap;
    }
    private void saveMap(Map<String,String> inputMap) {
        SharedPreferences pSharedPref = getApplicationContext().getSharedPreferences("MyVariables", Context.MODE_PRIVATE);
        if (pSharedPref != null){
            JSONObject jsonObject = new JSONObject(inputMap);
            String jsonString = jsonObject.toString();
            pSharedPref.edit()
                    .remove("My_map")
                    .putString("My_map", jsonString)
                    .apply();
        }
    }
    private void addOrUpdateMovie(String videoUrl) {
        SharedPreferences pSharedPref = getApplicationContext().getSharedPreferences("MyVariables", Context.MODE_PRIVATE);
        Map<String, String> inputMap = loadMap();

        // Check if the movie URL already exists in the map
        if (inputMap.containsKey(videoUrl)) {
            // Movie URL already exists, remove the existing entry
            inputMap.remove(videoUrl);
        }

        // Add the movie URL with the current date
        String currentDate = new Date().toString();
        inputMap.put(videoUrl, currentDate);

        // Save the updated map
        saveMap(inputMap);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
            // Add the videoUrl to the end of the watchedUrls set if it's not already present
            if (!watchedUrls.contains(videoUrl)) {
                watchedUrls.add(videoUrl);
            }

            // Convert the watchedUrls set to a Set<String>
          /*  Set<String> stringSet = new HashSet<>(watchedUrls);*/

            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet("watched_urls", watchedUrls);
            editor.putString("video_url", videoUrl);
            editor.putLong("last_played_position", player.getCurrentPosition());
            editor.apply();
            inputMap1=new HashMap<>();
            String date=new Date().toString();
            inputMap1.put(videoUrl,date);
            addOrUpdateMovie(videoUrl);
        }
        keepAliveHandler.removeCallbacks(keepAliveRunnable);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        playerView = findViewById(R.id.exoPlayerView);
        ProgressBar loadingBar = findViewById(R.id.loading_bar);
        loadingBar.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        String savedVideoUrl = sharedPreferences.getString("video_url", "");
        lastPlayedPosition = sharedPreferences.getLong("last_played_position", 0);
        SharedPreferences sharedPreferences1 = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
         watchedUrls = sharedPreferences1.getStringSet("watched_urls", new HashSet<>());
        Bundle extras = getIntent().getExtras();
        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if (extras != null) {
            videoUrl = extras.getString("movieUrl");
        }

        fullTime = getFullTime(duration);
        Uri uri = Uri.parse(videoUrl);
        player = new SimpleExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(this).setLiveTargetOffsetMs(5000))
                .build();
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(videoUrl)
                .setLiveConfiguration(new MediaItem.LiveConfiguration.Builder()
                        .setMaxPlaybackSpeed(1.02f)
                        .build())
                .build();

        player.setMediaItem(mediaItem);

        if (savedVideoUrl.equals(videoUrl)) {
            long currentPositionInSeconds = lastPlayedPosition / 1000; // convert to seconds
            long startThreshold = 5 * 60; // 5 minutes in seconds
            long endThreshold = duration / 1000 - 10 * 60; // 10 minutes before the end in seconds
            if (currentPositionInSeconds >= startThreshold) {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("שים לב!")
                        .setIcon(getResources().getDrawable(R.drawable.movieappicon))
                        .setMessage(Html.fromHtml(" </b>ראית את הסרט בעבר, האם תרצה להמשיך מאותה הנקודה שבה הפסקת ? <b>" + "(" + getTimeMovieInTheLastTime() + ")"))
                        .setPositiveButton("כן", (dialog, which) -> {
                            playVideoFromLastPosition();

                        })
                        .setNegativeButton("לא", (dialog, which) -> {
                            playVideoFromBeginning();
                        })
                        .show();
                alertDialog.setOnShowListener(dialog -> {
                    Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    positiveButton.setBackgroundColor(Color.parseColor("#D3D3D3"));
                    positiveButton.requestFocus();
                });
            } else {
                playVideoFromBeginning();
            }
        } else {
            playVideoFromBeginning();
        }

        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    loadingBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void playVideoFromBeginning() {
        player.setPlayWhenReady(true);
        player.prepare();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        playerView.setPlayer(player);
    }

    private void playVideoFromLastPosition() {
        player.setPlayWhenReady(true);
        player.prepare();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        playerView.setPlayer(player);
        player.seekTo(lastPlayedPosition);
    }

    @SuppressLint("DefaultLocale")
    public String getTimeMovieInTheLastTime() {
        long currentPositionInSeconds = lastPlayedPosition / 1000; // convert to seconds
        long hours = currentPositionInSeconds / 3600; // calculate the number of hours
        long minutes = (currentPositionInSeconds % 3600) / 60; // calculate the number of remaining minutes
        long seconds = currentPositionInSeconds % 60; // calculate the number of remaining seconds
        return String.format("%d:%02d:%02d", hours, minutes, seconds); // format the position as "hours:minutes:seconds"
    }

    @SuppressLint("DefaultLocale")
    private String getFullTime(long duration) {
        long durationSeconds = duration / 1000;
        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        long seconds = durationSeconds % 60;
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }
}
