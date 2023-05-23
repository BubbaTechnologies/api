package com.bubbaTech.api.like;

public class Ratings {
    static public double DISLIKE_RATING = 0.0;
    static public double REMOVE_LIKE_RATING = -4;
    static public double PAGE_CLICK_RATING = 2;
    static public double LIKE_RATING = 5;
    static public double BUY_RATING = 10;
    static public double TOTAL_IMAGE_TAP_RATING = 2;

    public enum Actions {
        DISLIKE, REMOVE_LIKE, PAGE_CLICK, LIKE_RATING, BUY_RATING, IMAGE_TAP
    }
}
