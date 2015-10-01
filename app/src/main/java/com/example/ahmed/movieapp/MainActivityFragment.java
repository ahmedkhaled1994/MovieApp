package com.example.ahmed.movieapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private String API_KEY;

    ImageAdapter imageAdapter;
    int count;      //number of movies
    int sortOrder;  //0 for most popular, 1 for highest rated
    movieClass [] result;
    static final String SORT_KEY = "sort key";
    static final String MOVIE_ARRAY_KEY = "movie array";
    boolean needFetch;
    DbHelper mDbHelper;
    GridView gridview;
    TextView textView;
    final String LOG_TAG = "temp log";

    public MainActivityFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        //-----------For UDACITY Reviewer-----------//
        //Change the following value of the API key//
        API_KEY = getString(R.string.my_api_key);
        //////////////////////////////////////////////

        Log.v(LOG_TAG, "mainActivityFragment onCreate");
        super.onCreate(savedInstanceState);
        mDbHelper = new DbHelper(getActivity(),null,null,1);
        needFetch = true;
        if(savedInstanceState != null) {
            sortOrder = savedInstanceState.getInt(SORT_KEY);
            result = (movieClass[])savedInstanceState.getParcelableArray(MOVIE_ARRAY_KEY);
            needFetch = false;
        }
        imageAdapter = new ImageAdapter(getActivity().getApplicationContext());
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        Log.v(LOG_TAG, "mainActivityFragment onSavedInstanceState");
        outState.putInt(SORT_KEY, sortOrder);
        outState.putParcelableArray(MOVIE_ARRAY_KEY, result);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Log.v(LOG_TAG, "mainActivityFragment onCreateView");
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        gridview = (GridView) rootView.findViewById(R.id.gridview);
        textView = (TextView) rootView.findViewById(R.id.main_text_view);
        gridview.setAdapter(imageAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                //Toast.makeText(getActivity(), "" + position,Toast.LENGTH_SHORT).show();
                //Intent detailIntent = new Intent(getActivity(), DetailActivity.class).putExtra(getString(R.string.intent_extra_object), result[position]);
                //startActivity(detailIntent);
                ((Callback) getActivity()).onItemSelected(result[position]);
            }
        });
        if (getArguments()!=null && getArguments().getInt(getString(R.string.remove_movie))==1)
            sortOrder=2;
        if (!isOnline()) {
            sortOrder = 2;
            Toast.makeText(getActivity(), "No Internet connection, only favourite movies are available", Toast.LENGTH_LONG).show();
        }
        return rootView;
    }


    public interface Callback{
        public void onItemSelected(movieClass movie);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment, menu);
        if(!isOnline()) {
            menu.getItem(0).getSubMenu().getItem(0).setEnabled(false);
            menu.getItem(0).getSubMenu().getItem(1).setEnabled(false);
        }else{
            menu.getItem(0).getSubMenu().getItem(0).setEnabled(true);
            menu.getItem(0).getSubMenu().getItem(1).setEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.sort_most_popular)        sortOrder = 0;
        else if (id == R.id.sort_highest_rated) sortOrder = 1;
        else if (id == R.id.sort_favourites)    sortOrder = 2;
        else                                    return true;
        updateList();
        return true;
    }

    @Override
    public void onStart(){
        super.onStart();
        updateList();
    }


    public void updateList(){
        count=0;
        FetchMoviesTask task = new FetchMoviesTask();
        switch (sortOrder) {
            case 0:
                task.execute("popularity.desc");
                break;
            case 1:
                task.execute("vote_average.desc");
                break;
            case 2:
                task.execute("null");
                break;
            default:
                //task.execute("popularity.desc");
                break;
        }
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, movieClass[]> {


        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
//            if(needFetch)
                mProgressDialog = ProgressDialog.show(getActivity(), "Loading", "Wait while loading...");
        }

        @Override
        protected movieClass[] doInBackground(String... params) {
            if(needFetch) {
                if (params[0] != "null") {
                    HttpURLConnection urlConnection = null;
                    BufferedReader reader = null;

                    String listJsonStr = null;
                    String sortBy = params[0];

                    try {
                        final String LIST_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                        final String LIST_BASE_URL_TOP_RATED = "http://api.themoviedb.org/3/movie/top_rated?";
                        final String SORT_PARAM = "sort_by";
                        final String API_KEY_PARAM = "api_key";

                        Uri builtUri;
                        URL url;
                        if (sortBy == "popularity.desc") {
                            builtUri = Uri.parse(LIST_BASE_URL).buildUpon()
                                    .appendQueryParameter(SORT_PARAM, sortBy)
                                    .appendQueryParameter(API_KEY_PARAM, API_KEY).build();
                            url = new URL(builtUri.toString());
                        } else {
                            builtUri = Uri.parse(LIST_BASE_URL_TOP_RATED).buildUpon()
                                    .appendQueryParameter(API_KEY_PARAM, API_KEY).build();
                            url = new URL(builtUri.toString());
                        }

//                        Log.v(LOG_TAG, "Built URI " + builtUri.toString());

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
//                        Log.v(LOG_TAG, "Forecast String: " + listJsonStr);

                    } catch (IOException e) {
//                        Log.e(LOG_TAG, "Error ", e);
                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (final IOException e) {
//                                Log.e(LOG_TAG, "Error closing stream", e);
                            }
                        }
                    }
                    try {
                        return getMovieDataFromJson(listJsonStr);
                    } catch (JSONException e) {
//                        Log.e(LOG_TAG, e.getMessage(), e);
                        e.printStackTrace();
                    }
                    return null;
                } else {
                    int count = mDbHelper.getCount();
                    String query = "SELECT * FROM " + mDbHelper.TABLE_NAME;
                    Cursor c = mDbHelper.getWritableDatabase().rawQuery(query, null);
                    c.moveToFirst();
                    result = new movieClass[count];
                    for (int i = 0; i < count; i++) {
                        String id = c.getString(c.getColumnIndex(mDbHelper.COLOUMN_ID));
                        String title = c.getString(c.getColumnIndex(mDbHelper.COLOUMN_TITLE));
                        String overview = c.getString(c.getColumnIndex(mDbHelper.COLOUMN_OVERVIEW));
                        String releaseDate = c.getString(c.getColumnIndex(mDbHelper.COLOUMN_RELEASE_DATE));
                        String poster = c.getString(c.getColumnIndex(mDbHelper.COLOUMN_POSTER));
                        String rating = c.getString(c.getColumnIndex(mDbHelper.COLOUMN_RATING));
                        movieClass movie = new movieClass(id, title, overview, releaseDate, poster
                                , rating, true);
                        result[i] = movie;
                        c.moveToNext();
                    }
                    return result;
                }
            }else {
                needFetch = true;
                return result;
            }
        }

        @Override
        protected void onPostExecute(movieClass[] movies) {
            if (movies != null) {
                imageAdapter.notifyDataSetChanged();
                imageAdapter.clear(movies);
                if(movies.length==0){
                    textView.setVisibility(View.VISIBLE);
                }else {
                    textView.setVisibility(View.GONE);
                    for (movieClass movie : movies) {
                        imageAdapter.add(movie);
                    }
                }
            }
//            if(needFetch)
                mProgressDialog.dismiss();
        }
        private movieClass[] getMovieDataFromJson(String jsonStr) throws JSONException{

            final String TMDB_LIST = "results";
            final String TMDB_ID = "id";
            final String TMDB_ORIGINAL_TITLE = "original_title";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_POSTER = "poster_path";
            final String TMDB_REL_DATE = "release_date";
            final String TMDB_RATING = "vote_average";

            JSONObject movieJson = new JSONObject(jsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMDB_LIST);

            result = new movieClass[20];
            for (int i=0; i<20; i++){result[i]=new movieClass();}
            for(int i = 0; i < movieArray.length(); i++) {

                JSONObject movieDetails = movieArray.getJSONObject(i);
//                Log.v(LOG_TAG, "movie: " + Integer.toString(i));

                String temp = movieDetails.getString(TMDB_ORIGINAL_TITLE);

                result[i].setId(movieDetails.getString(TMDB_ID));
//                Log.v(LOG_TAG, "\tId: " + movieDetails.getString(TMDB_ID));

                result[i].setTitle(temp);
//                Log.v(LOG_TAG, "\tTitle: " + movieDetails.getString(TMDB_ORIGINAL_TITLE));

                result[i].setOverview(movieDetails.getString(TMDB_OVERVIEW));
//                Log.v(LOG_TAG, "\toverview: " + movieDetails.getString(TMDB_OVERVIEW));

                result[i].setReleaseDate(movieDetails.getString(TMDB_REL_DATE));
//                Log.v(LOG_TAG, "\trelease date: " + movieDetails.getString(TMDB_REL_DATE));

                result[i].setPoster(movieDetails.getString(TMDB_POSTER));
//                Log.v(LOG_TAG, "\tposter path: " + movieDetails.getString(TMDB_POSTER));

                result[i].setRating(movieDetails.getString(TMDB_RATING));
//                Log.v(LOG_TAG, "\trating: " + movieDetails.getString(TMDB_RATING));
            }
            return result;
        }

    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                int orientation = getResources().getConfiguration().orientation;
                DisplayMetrics metrics = getActivity().getApplicationContext().getResources().getDisplayMetrics();
                //WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                boolean isTablet = isTablet(mContext);
                if (isTablet){
                    if (orientation == 1) {
                        gridview.setNumColumns(1);
                        imageView.setLayoutParams(new GridView.LayoutParams((int) (metrics.widthPixels*3/8),
                                getHeight(metrics.widthPixels*3/8)));
                    }else if (orientation == 2) {
                        gridview.setNumColumns(2);
                        imageView.setLayoutParams(new GridView.LayoutParams((int) (metrics.widthPixels * 3/16),
                                getHeight(metrics.widthPixels *3/16)));
                    }
                }else {
                    if (orientation == 1)
                        imageView.setLayoutParams(new GridView.LayoutParams((int) (metrics.widthPixels / 2),
                                getHeight(metrics.widthPixels / 2)));
                    else if (orientation == 2)
                        imageView.setLayoutParams(new GridView.LayoutParams(metrics.widthPixels / 3,
                                getHeight(metrics.widthPixels / 3)));
                }
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setPadding(0,0,0,0);
            } else {
                imageView = (ImageView) convertView;
            }

            //imageView.setImageResource(mThumbIds[position]);
            Picasso.with(getActivity()).load(mThumbIds[position])
                    .placeholder(R.drawable.temp).into(imageView);
            return imageView;
        }

        private String[] mThumbIds = new String[20];

        public void add(movieClass movie) {
            mThumbIds[count]= "http://image.tmdb.org/t/p/w185"+movie.getPoster();
            count++;
        }

        public void clear (movieClass[] movies){
            mThumbIds = new String[movies.length];
            count=0;
        }
    }

    private int getHeight (double width){
        return (int) (width * 275/185);
    }

    private boolean isTablet (Context context){
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public int getSortOrder(){
        return sortOrder;
    }
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

}
