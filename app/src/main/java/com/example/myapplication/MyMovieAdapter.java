package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.util.Log;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MyMovieAdapter extends RecyclerView.Adapter<MyMovieAdapter.ViewHolder> {

    List<MyMovieData> myMovieData = new ArrayList<>();
    Context context;
    public MyMovieAdapter(List<MyMovieData> myMovieData, MainActivity activity) {
        this.myMovieData = myMovieData;
        this.context = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.movie_item_list,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final MyMovieData myMovieDataList = myMovieData.get(position);
        holder.textViewName.setText(myMovieDataList.getMovieName());
        Log.d(myMovieDataList.getMovieImage(),"getMovieImage");
//        Picasso.get().load(myMovieDataList.getMovieImage()).fit().centerCrop().into(holder.movieImage);
        Picasso.get().load(myMovieDataList.getMovieImage()).fit().into(holder.movieSmallImage);
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                CardView cardView = view.findViewById(R.id.card_view);
//                TextView descriptioMovie = view.findViewById(R.id.textDescription);
                TextView textViewName = view.findViewById(R.id.textName);
                Drawable cardBackground = ContextCompat.getDrawable(view.getContext(), R.drawable.card_movie_design);
                if (hasFocus) {
                    // Change the color to the desired color when the view receives focus
                    cardView.setBackgroundColor(Color.parseColor("#D3D3D3"));
                } else {
                    // Change the color back to the original color when the view loses focus
                    cardView.setBackground(cardBackground);
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MoviePageActivity.class);
                intent.putExtra("movieUrl",myMovieDataList.getMovieUrl());
                intent.putExtra("movieImage",myMovieDataList.getMovieImage());
                intent.putExtra("movieName",myMovieDataList.getMovieName());
                intent.putExtra("movieDescription",myMovieDataList.getMovieDescription());
                v.getContext().startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return myMovieData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView movieImage;
        ImageView movieSmallImage;
        TextView textViewName;
        TextView descriptioMovie;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
//            movieImage = itemView.findViewById(R.id.imageview);
            movieSmallImage = itemView.findViewById(R.id.imageview_small);
            textViewName = itemView.findViewById(R.id.textName);
//           descriptioMovie = itemView.findViewById(R.id.textDescription);


        }
    }

}
