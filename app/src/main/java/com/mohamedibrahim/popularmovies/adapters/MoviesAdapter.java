package com.mohamedibrahim.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.mohamedibrahim.popularmovies.R;
import com.mohamedibrahim.popularmovies.interfaces.ClickListener;
import com.mohamedibrahim.popularmovies.models.Movie;
import com.mohamedibrahim.popularmovies.utils.DBUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Mohamed Ibrahim on 7/31/2016.
 **/

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Movie> movies;
    private int lastPosition = -1;

    /**
     * @param context context
     * @param movies  moviesArrayList
     */
    public MoviesAdapter(Context context, ArrayList<Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    @Override
    public MoviesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_recyclerview_item, parent, false);
        return new ViewHolder(mView);
    }


    /**
     * @param holder   viewHolder for movies
     * @param position position
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final String BASE_POSTER_PATH = "http://image.tmdb.org/t/p/";
        final String SIZE = "w185";
        final Movie movie = movies.get(position);
        String POSTER_PATH = movie.getPosterPath();

        String FullPosterPath = BASE_POSTER_PATH + SIZE + POSTER_PATH;
        Picasso.with(context)
                .load(FullPosterPath)
                .placeholder(R.drawable.progress)
                .error(R.drawable.ic_error)
                .into(holder.posterMovie);

        if (DBUtils.isMovieFavorite(movie.getId(), context)) {
            holder.favoriteBtn.setBackgroundResource(R.drawable.ic_action_favorite);
        } else {
            holder.favoriteBtn.setBackgroundResource(R.drawable.ic_action_favorite_outline);
        }

        holder.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DBUtils.isMovieFavorite(movie.getId(), context)) {
                    DBUtils.deleteFavoriteMovie(movie.getId(), context);
                    holder.favoriteBtn.setBackgroundResource(R.drawable.ic_action_favorite_outline);
                } else {
                    DBUtils.addFavoriteMovie(movie, context);
                    holder.favoriteBtn.setBackgroundResource(R.drawable.ic_action_favorite);
                }
            }
        });
        setAnimation(holder.itemView, position);
    }


    @Override
    public int getItemCount() {
        return movies.size();
    }

    /**
     * viewHolder for movies
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.movie_poster_img)
        ImageView posterMovie;
        @BindView(R.id.favorite_btn)
        ToggleButton favoriteBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            ((ClickListener) context)
                    .onItemSelected(movies.get(clickedPosition), clickedPosition);
        }

        public void clearAnimation() {
            itemView.clearAnimation();
        }
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        holder.clearAnimation();
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation;
            if (position % 2 == 1) {
                animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
            } else {
                animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            }
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
}
