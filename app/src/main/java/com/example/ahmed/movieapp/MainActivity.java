package com.example.ahmed.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback, DetailActivityFragment.Callback {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static final String MAINFRAGMENT_TAG = "MFTAG";
    private boolean mTwoPane;

    MainActivityFragment mainActivityFragmentContainer;
    final String LOG_TAG = "temp log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Log.v(LOG_TAG, "mainActivity onCreate");
        super.onCreate(savedInstanceState);
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main);
        mainActivityFragmentContainer = (MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        if (findViewById(R.id.movie_detail_container)!= null){
            mTwoPane = true;
            if (savedInstanceState == null) {
                DetailActivityFragment detailActivityFragment = new DetailActivityFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, detailActivityFragment, DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public void onItemSelected(movieClass movie){
        if(mTwoPane){
            Bundle args = new Bundle();
            args.putParcelable(getString(R.string.intent_extra_object), movie);
            DetailActivityFragment fragment= new DetailActivityFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG).commit();

        } else {
            Intent detailIntent = new Intent(this, DetailActivity.class).putExtra(getString(R.string.intent_extra_object), movie);
            startActivity(detailIntent);
        }
    }

    @Override
    public void onFavourite() {
        if(mTwoPane && mainActivityFragmentContainer.getSortOrder()==2){
            mainActivityFragmentContainer.updateList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
