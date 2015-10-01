package com.example.ahmed.movieapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    static final String DATABASE_NAME = "movies.db";
    static final String TABLE_NAME = "movie";
    static final String COLOUMN_ID = "_id";
    static final String COLOUMN_TITLE = "title";
    static final String COLOUMN_OVERVIEW = "overview";
    static final String COLOUMN_RELEASE_DATE = "release_date";
    static final String COLOUMN_POSTER = "poster";
    static final String COLOUMN_RATING = "rating";


    public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COLOUMN_ID + " INTEGER PRIMARY KEY," +
                COLOUMN_TITLE + " TEXT UNIQUE NOT NULL, " +
                COLOUMN_OVERVIEW + " TEXT NOT NULL, " +
                COLOUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                COLOUMN_POSTER + " TEXT NOT NULL, " +
                COLOUMN_RATING + " REAL NOT NULL " +
                " );";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addFavouriteMovie (movieClass movie){
        String temp = "DROP TABLE IF EXISTS movies";
        getWritableDatabase().execSQL(temp);
        ContentValues values = new ContentValues();
        values.put(COLOUMN_ID , movie.getId());
        values.put(COLOUMN_TITLE , movie.getTitle());
        values.put(COLOUMN_OVERVIEW , movie.getOverview());
        values.put(COLOUMN_RELEASE_DATE , movie.getReleaseDate());
        values.put(COLOUMN_POSTER , movie.getPoster());
        values.put(COLOUMN_RATING , movie.getRating());

        getWritableDatabase().insert(TABLE_NAME, null, values);
        getWritableDatabase().close();
    }

    public void deleteFavouriteMovie (movieClass movie){
        getWritableDatabase().execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLOUMN_ID + "=\"" +
                movie.getId() + "\";");
    }

    public boolean isFavourite (movieClass movie){
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLOUMN_ID + " =\""
                + movie.getId() + "\";";
        Cursor c = getWritableDatabase().rawQuery(query , null);
        return c.getCount()!=0? true: false;
    }
    public int getCount (){
        String query = "SELECT * FROM " + TABLE_NAME +";";
        Cursor c = getWritableDatabase().rawQuery(query,null);
        return c.getCount();
    }

//    public movieClass getMovieAtIndex (int index){
//        String query = "SELECT * FROM " + TABLE_NAME ;
//        Cursor c = getWritableDatabase().rawQuery(query,null);
//    }
}
