package com.example.ahmed.movieapp;

import android.os.Parcel;
import android.os.Parcelable;

public class movieClass implements Parcelable {
    String id;
    String title;
    String overview;
    String releaseDate;
    String poster;
    String rating;
    boolean isFavourite;

    public movieClass(String id, String title, String overview, String releaseDate, String poster,
                      String rating, boolean isFavourite) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.poster = poster;
        this.rating = rating;
        this.isFavourite = isFavourite;
    }

    public movieClass() {
        this.title = "";
        this.overview = "";
        this.releaseDate = "";
        this.poster = "";
        this.rating = "";
        this.id="";
        this.isFavourite=false;
    }

    protected movieClass(Parcel in) {       //another constructor
        overview = in.readString();
        poster = in.readString();
        rating = in.readString();
        releaseDate = in.readString();
        title = in.readString();
        id = in.readString();
        isFavourite = in.readByte()!=0;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setIsFavourite(boolean isFavourite) {
        this.isFavourite = isFavourite;
    }

    //    public String getTrailer() {
//        return trailer;
//    }
//
//    public void setTrailer(String trailer) {
//        this.trailer = trailer;
//    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getOverview());
        dest.writeString(getPoster());
        dest.writeString(getRating());
        dest.writeString(getReleaseDate());
        dest.writeString(getTitle());
        dest.writeString(getId());
        dest.writeByte((byte)(isFavourite()? 1:0));

    }

    public static final Creator<movieClass> CREATOR = new Creator<movieClass>() {
        @Override
        public movieClass createFromParcel(Parcel in) {
            return new movieClass(in);
        }

        @Override
        public movieClass[] newArray(int size) {
            return new movieClass[size];
        }
    };
}