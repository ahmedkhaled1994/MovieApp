package com.example.ahmed.movieapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    List<String> trailersNames = new ArrayList<String>();
    List<String> trailersKeys = new ArrayList<String>();
    List<String> reviewsAuthors = new ArrayList<>();
    List<String> reviewsContent = new ArrayList<>();
    LinearLayout trailersList;
    LinearLayout reviewsList;
    ImageView trailersLoading;
    ImageView reviewsLoading;

    DbHelper mDbHelper;

    private boolean mTwoPane;

    public DetailActivityFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments()!=null ||
                getActivity().getIntent().getParcelableExtra(getString(R.string.intent_extra_object))!=null) {
            final movieClass movie;
            Bundle args = getArguments();
            if (args != null) {
                movie = args.getParcelable(getString(R.string.intent_extra_object));
                mTwoPane = true;
            } else {
                Intent intent = getActivity().getIntent();
                movie = (movieClass) intent.getParcelableExtra(getString(R.string.intent_extra_object));
                mTwoPane = false;
            }
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            FetchTrailersTask fetchTrailersTask = new FetchTrailersTask();
            fetchTrailersTask.execute(movie.getId());

            trailersList = (LinearLayout) rootView.findViewById(R.id.detail_trailers_list);
            reviewsList = (LinearLayout) rootView.findViewById(R.id.detail_reviews_list);

            trailersLoading = (ImageView)rootView.findViewById(R.id.detail_trailers_loading);
            reviewsLoading = (ImageView)rootView.findViewById(R.id.detail_reviews_loading);

            ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView_detail);
            ImageView favouriteButton = (ImageView) rootView.findViewById(R.id.detail_favourite);

            Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w342" + movie.getPoster())
                    .placeholder(R.drawable.progress_animation).into(imageView);

            //((TextView) rootView.findViewById(R.id.detail_title)).setText(movie.getTitle());
            ((TextView) rootView.findViewById(R.id.detail_overview)).setText(movie.getOverview());
            ((TextView) rootView.findViewById(R.id.detail_rating)).setText("Rating: " + movie.getRating());
            ((TextView) rootView.findViewById(R.id.detail_date)).setText("Release Date: " + movie.getReleaseDate());

            getActivity().setTitle(movie.getTitle());

            mDbHelper = new DbHelper(getActivity(), null, null, 1);

            if (mDbHelper.isFavourite(movie))
                favouriteButton.setImageResource(R.drawable.unfavourite);
            else {
                favouriteButton.setImageResource(R.drawable.favourite);
            }


            favouriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDbHelper.isFavourite(movie)) {
                        ((ImageView) v).setImageResource(R.drawable.favourite);
                        movie.setIsFavourite(false);
                        mDbHelper.deleteFavouriteMovie(movie);
                    } else {
                        ((ImageView) v).setImageResource(R.drawable.unfavourite);
                        movie.setIsFavourite(true);
                        mDbHelper.addFavouriteMovie(movie);
                    }
                    if(mTwoPane)
                        ((Callback)getActivity()).onFavourite();
                }
            });

            return rootView;
        } else
            return null;
    }

    public interface Callback{
        public void onFavourite();
    }

    public class FetchTrailersTask extends AsyncTask<String, Void, Void> {

        private final String API_KEY = "8b48485377aade785fb0da272b2be789";
        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();
        private boolean hasReviews = false;

        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String listJsonStr = null;
            String id = params[0];

            try{
                final String LIST_BASE_URL ="http://api.themoviedb.org/3/movie/"+id+"/videos?";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(LIST_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM,API_KEY).build();

                URL url = new URL(builtUri.toString());

//                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                listJsonStr = buffer.toString();
//                Log.v(LOG_TAG, "Forecast String: " + listJsonStr);

            } catch (IOException e) {
//                Log.e(LOG_TAG, "Error ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
//                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                getMovieTrailersFromJson(listJsonStr);
            }catch(JSONException e){
//                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            //////////////////////////////////////////////////////////////////////////////////
            try{
                final String LIST_BASE_URL ="http://api.themoviedb.org/3/movie/"+id+"/reviews?";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(LIST_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM,API_KEY).build();

                URL url = new URL(builtUri.toString());

//                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                listJsonStr = buffer.toString();
//                Log.v(LOG_TAG, "Forecast String: " + listJsonStr);

            } catch (IOException e) {
//                Log.e(LOG_TAG, "Error ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
//                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try{
                hasReviews = getMovieReviewsFromJson(listJsonStr);
            }catch (JSONException e){
//                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void Void) {
            for(int i=0; i<trailersNames.size(); i++){
                final int temp = i;
                LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = inflater.inflate(R.layout.trailer_item, null);
                String trailerName = trailersNames.get(i);
                TextView nameTextView = (TextView)v.findViewById(R.id.detail_trailer_name);
                nameTextView.setText(trailerName);
                ImageView thumbnail = (ImageView)v.findViewById(R.id.detail_trailer_thumbnail);
                Picasso.with(getActivity()).load(Uri.parse("http://img.youtube.com/vi/" + trailersKeys.get(i)+"/0.jpg"))
                        .placeholder(R.drawable.progress_animation).into(thumbnail);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://www.youtube.com/watch?v=" + trailersKeys.get(temp))));
                    }
                });
                trailersList.addView(v);
                trailersLoading.setVisibility(View.GONE);
            }

            if(hasReviews){
                for(int i=0; i<reviewsAuthors.size();i++){
                    LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View v = inflater.inflate(R.layout.review_item,null);
                    TextView authorName= (TextView)v.findViewById(R.id.reviewer_name);
                    ExpandableTextView review = (ExpandableTextView) v.findViewById(R.id.review_text);
                    authorName.setText(reviewsAuthors.get(i));
                    review.setText(reviewsContent.get(i));
                    reviewsList.addView(v);
                    reviewsLoading.setVisibility(View.GONE);
                }
            }else{
                ((TextView)reviewsList.findViewById(R.id.detail_reviews_header)).setText("No Reviews Available");
                reviewsLoading.setVisibility(View.GONE);
            }
        }

        private void getMovieTrailersFromJson(String jsonStr) throws JSONException {
            final String TMDB_LIST = "results";
            final String TMDB_KEY = "key";
            final String TMDB_NAME = "name";

            JSONObject rootJson = new JSONObject(jsonStr);
            JSONArray movieArray = rootJson.getJSONArray(TMDB_LIST);
            //String temp = movieArray.getJSONObject(i).getString(TMDB_NAME);
            for (int i=0; i<movieArray.length(); i++){
                JSONObject videoDetails = movieArray.getJSONObject(i);
                trailersKeys.add(videoDetails.getString(TMDB_KEY));
                trailersNames.add(videoDetails.getString(TMDB_NAME));
                i++;
            }
        }

        private boolean getMovieReviewsFromJson(String jsonStr)throws JSONException{
            final String TMDB_LIST = "results";
            final String TMDB_AUTHOR = "author";
            final String TMDB_CONTENT = "content";
            final String TMDB_RESULT_COUNT = "total_results";
            JSONObject rootJson = new JSONObject(jsonStr);
            int count = rootJson.getInt(TMDB_RESULT_COUNT);
            JSONArray reviewsArray = rootJson.getJSONArray(TMDB_LIST);
            if(count==0)
                return false;
            else{
                for(int i=0; i<count; i++){
                    JSONObject review = reviewsArray.getJSONObject(i);
                    reviewsAuthors.add(review.getString(TMDB_AUTHOR));
                    reviewsContent.add(review.getString(TMDB_CONTENT));
                }
                return true;
            }

        }
    }
}
