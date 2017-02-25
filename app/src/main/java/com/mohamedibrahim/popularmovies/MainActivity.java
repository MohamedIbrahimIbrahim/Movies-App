package com.mohamedibrahim.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mohamedibrahim.popularmovies.fragments.DetailsMovieFragment;
import com.mohamedibrahim.popularmovies.fragments.MoviesFragment;
import com.mohamedibrahim.popularmovies.interfaces.ClickListener;
import com.mohamedibrahim.popularmovies.models.Movie;
import com.mohamedibrahim.popularmovies.utils.Utility;

public class MainActivity extends AppCompatActivity implements ClickListener {


    private boolean mTwoPane;
    private MoviesFragment moviesFragment;
    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    private static final String MOVIE_DATA = "MOVIE_DATA";
    private String mSortedBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSortedBy = Utility.getPreferredMovies(this);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.movies_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movies_detail_container, new DetailsMovieFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setElevation(0f);
            }
        }
        moviesFragment = ((MoviesFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_movies));
        moviesFragment.setIsTwoPane(mTwoPane);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sortedBy = Utility.getPreferredMovies(this);
        if (getSupportActionBar() != null) {
            if (sortedBy.equals(getString(R.string.pref_sort_popular))) {
                getSupportActionBar().setTitle(R.string.pref_sort_popular_label);
            } else if (sortedBy.equals(getString(R.string.pref_sort_top))) {
                getSupportActionBar().setTitle(R.string.pref_sort_top_label);
            } else if (sortedBy.equals(getString(R.string.pref_sort_favorite))) {
                getSupportActionBar().setTitle(R.string.pref_sort_favorite_label);
            }
        }
        if (!sortedBy.equals(mSortedBy)) {
            MoviesFragment mf = (MoviesFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_movies);
            if (null != mf) {
                mf.onSortedByChanged();
            }
            mSortedBy = sortedBy;
        }
    }

    @Override
    public void onItemSelected(Movie movie, int position) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(MOVIE_DATA, movie);
            DetailsMovieFragment fragment = new DetailsMovieFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movies_detail_container, fragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent detailedIntent = new Intent(this, DetailsActivity.class);
            detailedIntent.putExtra(MOVIE_DATA, movie);
            startActivity(detailedIntent);
        }
        moviesFragment.setItemPosition(position);
    }
}
