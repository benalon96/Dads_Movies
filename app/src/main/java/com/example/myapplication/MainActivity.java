package com.example.myapplication;

import static android.os.Build.VERSION_CODES.M;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.SearchView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private StorageReference storageRef;
    private Uri videoUri;
    ArrayList<MyMovieData> watchedMoviesDataList = new ArrayList<>();
    private RecyclerView watchedRecyclerView;
    TextView moviesCounter;
    SearchView searchView;
    Set<String> watchedUrls;
    LinearLayout watchedLayout;
    Map<String, String> urls = new HashMap<>();
    Map<String, String> comedyMovies = new HashMap<>();
    RecyclerView recyclerView;
    Map<String, String> picturesUrls = new HashMap<>();
    Map<String, String> outputMap = new HashMap<>();
    Map<String, String> descriptionsOfMovies = new HashMap<>();
    ArrayList<MyMovieData> moviesDataList = new ArrayList<>();
    ArrayList<MyMovieData> moviesDataListWatched = new ArrayList<>();
    private String Categories = "All";
    TextView moviesCounterWatched;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        searchView = findViewById(R.id.simpleSearchView);
        searchView.setQuery("חפש סרט..", false);
        Drawable searchBackground = ContextCompat.getDrawable(this, R.drawable.bg_white_rounded);

        //For searching a movie by movie name and update the list of the movies.
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    searchView.setQuery("", false);
                    searchView.setBackgroundColor(Color.parseColor("#D3D3D3"));
                }
                else {
                    // Change the color back to the original color when the view loses focus
                    searchView.setBackground(searchBackground);

                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search query submission
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle search query text change
                filterMovies(newText);
                // Set the visibility of watchedLayout based on the presence of newText
                if (newText != null && !newText.isEmpty()) {
                    watchedLayout.setVisibility(View.GONE);
                } else {
                    watchedLayout.setVisibility(View.VISIBLE);
                    moviesCounter.setText("כל הסרטים (" + moviesDataList.size() + ")");
                }

                return true;
            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        watchedUrls = sharedPreferences.getStringSet("watched_urls", new HashSet<>());
        outputMap = loadMap();
        //findViewById elements
        recyclerView = findViewById(R.id.recyclerView);
        watchedRecyclerView = findViewById(R.id.watchedRecyclerView);
        moviesCounter = findViewById(R.id.counter_movies);
        moviesCounterWatched = findViewById(R.id.counter_movies_watched);
        ScrollView scrollView = findViewById(R.id.scrollView);  // Assuming you have assigned an id to the ScrollView
        RelativeLayout relativeLayout = scrollView.findViewById(R.id.relativeLayout);  // Assuming you have assigned an id to the RelativeLayout
        watchedLayout = relativeLayout.findViewById(R.id.watchedLayout);  // Accessing the watchedLayout inside the relativeLayout
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        LinearLayoutManager watchedLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        watchedRecyclerView.setLayoutManager(watchedLayoutManager);
        // Calculate the width of each column based on the screen density
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int columnWidthDp = 100; // Set your column width here
        int numColumns = (int) (screenWidthDp / columnWidthDp);
        // Set the GridLayoutManager to use the calculated number of columns
        GridLayoutManager layoutManager = new GridLayoutManager(this, numColumns - 3 <= 0 ? 3 : numColumns - 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        //get from the realtime firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("movies");
        DatabaseReference databaseReferencePictures = FirebaseDatabase.getInstance().getReference().child("pictures");
        DatabaseReference databaseReferenceDescriptions = FirebaseDatabase.getInstance().getReference().child("DescriptionMovies");
        // Read movie descriptions from the database
        databaseReferenceDescriptions.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    descriptionsOfMovies.put(movieSnapshot.getKey(), movieSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // Read movie picture URLs from the database
        databaseReferencePictures.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    picturesUrls.put(movieSnapshot.getKey(), movieSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // Read movie URLs from the database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the existing data
                moviesDataList.clear();
                moviesDataListWatched.clear();
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    urls.put(movieSnapshot.getKey(), movieSnapshot.getValue().toString());
                }

                for (Map.Entry<String, String> entry : urls.entrySet()) {
                    String movieName = entry.getKey();
                    String movieUrl = entry.getValue();
                    String movieDescription = descriptionsOfMovies.get(movieName);
                    String moviePictureUrl = picturesUrls.get(movieName);
                    // Check if the movie is a comedy movie based on the movie name
//                    if (index == 1 && !comedyMovies.containsKey(movieName)) {
//                        continue; // Skip this movie if it's not a comedy movie
//                    }

                    if (movieName != null && movieUrl != null && movieDescription != null && moviePictureUrl != null) {
                        MyMovieData movie = new MyMovieData(movieName, movieDescription, moviePictureUrl, movieUrl);
                        moviesDataList.add(movie);
                    }
                }
                moviesCounter.setText("כל הסרטים (" + moviesDataList.size() + ")");
                LinkedHashMap<String, String> orderedMap = new LinkedHashMap<>();

                List<Map.Entry<String, String>> entries = new ArrayList<>(outputMap.entrySet());

                Collections.sort(entries, (entry1, entry2) -> {
                    String dateString1 = entry1.getValue();
                    String dateString2 = entry2.getValue();

                    // Convert date strings to Date objects for comparison
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    Date date1, date2;
                    try {
                        date1 = format.parse(dateString1);
                        date2 = format.parse(dateString2);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return 0; // handle the parse exception based on your requirements
                    }

                    // Compare the Date objects
                    return date2.compareTo(date1);
                });

                for (Map.Entry<String, String> entry : entries) {
                    orderedMap.put(entry.getKey(), entry.getValue());
                }

                List<Map.Entry<String, String>> entries2 = new ArrayList<>(orderedMap.entrySet());
                for (int i = entries2.size() - 1; i >= 0; i--) {
                    Map.Entry<String, String> entry = entries2.get(i);
                    String movieUrl = entry.getKey();
                    for (MyMovieData movie : moviesDataList) {
                        if (movie.getMovieUrl().equals(movieUrl)) {
                            moviesDataListWatched.add(movie);
                            break;
                        }
                    }
                }

                // Sort the list by movie name in alphabetical order
                Collections.sort(moviesDataList, (o1, o2) -> o1.getMovieName().compareTo(o2.getMovieName()));
                MyMovieAdapter myMovieAdapter = new MyMovieAdapter(moviesDataList, MainActivity.this);
                recyclerView.setAdapter(myMovieAdapter);
                myMovieAdapter.notifyDataSetChanged();
                MyMovieAdapter myMovieAdapterWatched = new MyMovieAdapter(moviesDataListWatched, MainActivity.this);
                watchedRecyclerView.setAdapter(myMovieAdapterWatched);
                myMovieAdapterWatched.notifyDataSetChanged();
                moviesCounterWatched.setText("המשך צפייה (" + moviesDataListWatched.size() + ")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

    }

    private void filterMovies(String query) {
        // Create a new list to hold the filtered movies
        ArrayList<MyMovieData> filteredMovies = new ArrayList<>();
        // Iterate over the moviesDataList and check if the movie name contains the search query
        for (MyMovieData movie : moviesDataList) {
            if (movie.getMovieName().toLowerCase().contains(query.toLowerCase())) {
                filteredMovies.add(movie);
            }
        }

        // Update the RecyclerView adapter with the filtered movies
        MyMovieAdapter myMovieAdapter = new MyMovieAdapter(filteredMovies, MainActivity.this);
        recyclerView.setAdapter(myMovieAdapter);
        myMovieAdapter.notifyDataSetChanged();

        // Update the movies counter
        moviesCounter.setText("תוצאות חיפוש (" + filteredMovies.size() + ")");
    }

    private Map<String, String> loadMap() {
        Map<String, String> outputMap = new HashMap<>();
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return outputMap;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update the data when returning from the video activity
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        watchedUrls = sharedPreferences.getStringSet("watched_urls", new HashSet<>());
        outputMap = loadMap();
        moviesDataListWatched.clear();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("movies");
        DatabaseReference databaseReferencePictures = FirebaseDatabase.getInstance().getReference().child("pictures");
        DatabaseReference databaseReferenceDescriptions = FirebaseDatabase.getInstance().getReference().child("DescriptionMovies");
        // Read movie descriptions from the database
        databaseReferenceDescriptions.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    descriptionsOfMovies.put(movieSnapshot.getKey(), movieSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // Read movie picture URLs from the database
        databaseReferencePictures.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    picturesUrls.put(movieSnapshot.getKey(), movieSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // Read movie URLs from the database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the existing data
                moviesDataList.clear();
                moviesDataListWatched.clear();
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    urls.put(movieSnapshot.getKey(), movieSnapshot.getValue().toString());
                }

                for (Map.Entry<String, String> entry : urls.entrySet()) {
                    String movieName = entry.getKey();
                    String movieUrl = entry.getValue();
                    String movieDescription = descriptionsOfMovies.get(movieName);
                    String moviePictureUrl = picturesUrls.get(movieName);
                    Log.d("Ben", Categories);
                    // Check if the movie is a comedy movie based on the movie name
//                    if (index == 1 && !comedyMovies.containsKey(movieName)) {
//                        continue; // Skip this movie if it's not a comedy movie
//                    }

                    if (movieName != null && movieUrl != null && movieDescription != null && moviePictureUrl != null) {
                        MyMovieData movie = new MyMovieData(movieName, movieDescription, moviePictureUrl, movieUrl);
                        moviesDataList.add(movie);
                    }
                }
                moviesCounter.setText("כל הסרטים (" + moviesDataList.size() + ")");
                LinkedHashMap<String, String> orderedMap = new LinkedHashMap<>();

                List<Map.Entry<String, String>> entries = new ArrayList<>(outputMap.entrySet());

                Collections.sort(entries, new Comparator<Map.Entry<String, String>>() {
                    @Override
                    public int compare(Map.Entry<String, String> entry1, Map.Entry<String, String> entry2) {
                        String dateString1 = entry1.getValue();
                        String dateString2 = entry2.getValue();

                        // Convert date strings to Date objects for comparison
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                        Date date1, date2;
                        try {
                            date1 = format.parse(dateString1);
                            date2 = format.parse(dateString2);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0; // handle the parse exception based on your requirements
                        }

                        // Compare the Date objects
                        return date2.compareTo(date1);
                    }
                });

                for (Map.Entry<String, String> entry : entries) {
                    orderedMap.put(entry.getKey(), entry.getValue());
                }

                // the movies are ordered based on the date
                List<Map.Entry<String, String>> entries2 = new ArrayList<>(orderedMap.entrySet());
                for (int i = entries2.size() - 1; i >= 0; i--) {
                    Map.Entry<String, String> entry = entries2.get(i);
                    String movieUrl = entry.getKey();
                    for (MyMovieData movie : moviesDataList) {
                        if (movie.getMovieUrl().equals(movieUrl)) {
                            moviesDataListWatched.add(movie);
                            break;
                        }
                    }
                }


                // Sort the list by movie name in alphabetical order
                Collections.sort(moviesDataList, (o1, o2) -> o1.getMovieName().compareTo(o2.getMovieName()));
                MyMovieAdapter myMovieAdapter = new MyMovieAdapter(moviesDataList, MainActivity.this);
                recyclerView.setAdapter(myMovieAdapter);
                myMovieAdapter.notifyDataSetChanged();
                MyMovieAdapter myMovieAdapterWatched = new MyMovieAdapter(moviesDataListWatched, MainActivity.this);
                watchedRecyclerView.setAdapter(myMovieAdapterWatched);
                myMovieAdapterWatched.notifyDataSetChanged();
                moviesCounterWatched.setText("המשך צפייה (" + moviesDataListWatched.size() + ")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });


      /*  for (int i = 0; i < watchedUrls.size(); i++) {
            for (int j = 0; j < moviesDataList.size(); j++) {
                if (watchedUrls.get(i).equals(moviesDataList.get(j).getMovieUrl())) {
                    moviesDataListWatched.add(moviesDataList.get(j));
                }
            }
        }

        MyMovieAdapter myMovieAdapterWatched = new MyMovieAdapter(moviesDataListWatched, MainActivity.this);
        watchedRecyclerView.setAdapter(myMovieAdapterWatched);
        myMovieAdapterWatched.notifyDataSetChanged();
        moviesCounterWatched.setText("המשיכו לצפות (" + moviesDataListWatched.size() + ")");*/
    }
}
