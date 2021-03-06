package com.mohamedibrahim.popularmovies.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.mohamedibrahim.popularmovies.R;
import com.mohamedibrahim.popularmovies.SettingsActivity;
import com.mohamedibrahim.popularmovies.adapters.MoviesAdapter;
import com.mohamedibrahim.popularmovies.interfaces.ClickListener;
import com.mohamedibrahim.popularmovies.models.Movie;
import com.mohamedibrahim.popularmovies.utils.DBUtils;
import com.mohamedibrahim.popularmovies.utils.JsonUtils;
import com.mohamedibrahim.popularmovies.utils.NetworkUtils;
import com.mohamedibrahim.popularmovies.utils.Utility;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoviesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<String>, View.OnClickListener {

    private static final String SELECTED_KEY = "selected_position";
    private static final String URL_EXTRA = "URL";
    private static final int LOADER_ID = 1;
    private int mPosition = RecyclerView.NO_POSITION;
    private boolean isTwoPane;
    private Snackbar snackbar;

    @BindView(R.id.pb_loading)
    ProgressBar progressBar;
    @BindView(R.id.movies_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.iv_placeholder)
    ImageView imgPlaceHolder;
    @BindView(R.id.refresh)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinator;
    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.app_bar)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_movies, container, false);
        ButterKnife.bind(this, mView);

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        initRecyclerView();
        toolbar.inflateMenu(R.menu.main);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public void onResume() {
        super.onResume();
        setFragmentTitle();
    }

    private void setFragmentTitle() {
        String sortedBy = Utility.getPreferredMovies(getActivity());
        if (sortedBy.equals(getString(R.string.pref_sort_popular))) {
            collapsingToolbarLayout.setTitle(getString(R.string.pref_sort_popular_label));
        } else if (sortedBy.equals(getString(R.string.pref_sort_top))) {
            collapsingToolbarLayout.setTitle(getString(R.string.pref_sort_top_label));
        } else if (sortedBy.equals(getString(R.string.pref_sort_favorite))) {
            collapsingToolbarLayout.setTitle(getString(R.string.pref_sort_favorite_label));
        }
    }


    private void onFinishMovies(ArrayList<Movie> movies) {
        progressBar.setVisibility(View.GONE);
        if (movies != null && !movies.isEmpty()) {
            MoviesAdapter mAdapter = new MoviesAdapter(getContext(), movies);
            mRecyclerView.setAdapter(mAdapter);
            imgPlaceHolder.setVisibility(View.GONE);
            if (mPosition != RecyclerView.NO_POSITION) {
                mRecyclerView.scrollToPosition(mPosition);
            } else {
                //first start && two pane
                if (isTwoPane) {
                    openFirstMovie(movies);
                }
            }
            DBUtils.updateCachedMovies(movies, getContext());
        } else {
            showError(R.string.no_movies);
        }
        stopRefreshing();
    }

    private void initRecyclerView() {
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.swipe_refresh_color));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),
                Utility.calculateNoOfColumns(getContext())));
    }

    private void updateMovies() {
        progressBar.setVisibility(View.VISIBLE);
        String sortedBy = Utility.getPreferredMovies(getContext());
        if (sortedBy.equalsIgnoreCase(getString(R.string.pref_sort_favorite))) {
            onFinishMovies(DBUtils.getFavoriteMovies(getContext()));
        } else {
            if (Utility.isOnline(getContext())) {
                fetchMoviesFromAPI(sortedBy);
            } else {
                onFinishMovies(DBUtils.getCachedMovies(getContext()));
            }
        }
    }

    private void fetchMoviesFromAPI(String sortedBy) {
        Bundle queryBundle = new Bundle();
        queryBundle.putString(URL_EXTRA, String.valueOf(NetworkUtils.buildUrl(sortedBy)));
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        Loader<String> moviesLoader = loaderManager.getLoader(LOADER_ID);
        if (moviesLoader == null) {
            loaderManager.initLoader(LOADER_ID, queryBundle, this);
        } else {
            loaderManager.restartLoader(LOADER_ID, queryBundle, this);
        }
    }

    private void openFirstMovie(ArrayList<Movie> movies) {
        final int FIRST_ARRAY_LIST_POSITION = 0;
        ((ClickListener) getActivity())
                .onItemSelected(movies.get(FIRST_ARRAY_LIST_POSITION),
                        FIRST_ARRAY_LIST_POSITION);
    }

    public void setItemPosition(int position) {
        this.mPosition = position;
    }

    public void setIsTwoPane(boolean isTwoPane) {
        this.isTwoPane = isTwoPane;
    }

    public void onSortedByChanged() {
        mPosition = RecyclerView.NO_POSITION;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                Intent settingIntent = new Intent(getContext(), SettingsActivity.class);
                startActivity(settingIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        updateMovies();
    }

    private void showError(int errorResID) {
        mRecyclerView.setAdapter(null);
        imgPlaceHolder.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        if (errorResID == R.string.no_connection) {
            imgPlaceHolder.setImageResource(R.drawable.ic_error);
        } else if (errorResID == R.string.no_movies) {
            imgPlaceHolder.setImageResource(R.drawable.ic_empty);
        }
        stopRefreshing();
        showSnackbar(errorResID, R.string.retry, this);
    }

    private void stopRefreshing() {
        if (refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false);
        }
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(getActivity()) {
            String mJsonResult;

            @Override
            protected void onStartLoading() {
                if (args == null) {
                    return;
                }
                if (mJsonResult != null) {
                    deliverResult(mJsonResult);
                } else {
                    forceLoad();
                }
            }

            @Override
            public String loadInBackground() {
                try {
                    String urlString = args.getString(URL_EXTRA);
                    if (urlString == null || TextUtils.isEmpty(urlString)) {
                        return null;
                    } else {
                        URL url = new URL(urlString);
                        return NetworkUtils.getResponseFromHttpUrl(url);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(String jsonResult) {
                mJsonResult = jsonResult;
                super.deliverResult(mJsonResult);
            }

        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if (null == data) {
            onFinishMovies(DBUtils.getCachedMovies(getContext()));
        } else {
            onFinishMovies(JsonUtils.getMoviesDataFromJson(data));
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        /*
         * We aren't using this method in application, but i required to Override
         * it to implement the LoaderCallbacks<String> interface
         */
    }

    private void showSnackbar(int messageRes) {
        showSnackbar(getString(messageRes));
    }

    private void showSnackbar(String message) {
        hideSnackbar();
        snackbar = Snackbar.make(coordinator, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void showSnackbar(int messageRes, int actionMessageRes, View.OnClickListener onClickListener) {
        showSnackbar(getString(messageRes), getString(actionMessageRes), onClickListener);
    }

    private void showSnackbar(String message, String actionMessage, View.OnClickListener onClickListener) {
        hideSnackbar();
        snackbar = Snackbar.make(coordinator, message, Snackbar.LENGTH_INDEFINITE)
                .setAction(actionMessage, onClickListener);
        snackbar.show();
    }

    private void hideSnackbar() {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    @Override
    public void onClick(View view) {
        updateMovies();
    }
}
