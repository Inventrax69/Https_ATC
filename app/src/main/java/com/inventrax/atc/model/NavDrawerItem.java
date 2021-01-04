package com.inventrax.atc.model;

/**
 * Created by nareshp on 05/01/2016.
 */
public class NavDrawerItem {
    private boolean showNotify;
    private String title;
    private int imageView;
    private String counter;


    public NavDrawerItem() {

    }

    public NavDrawerItem(boolean showNotify, String title) {
        this.showNotify = showNotify;
        this.title = title;
    }

    public NavDrawerItem(boolean showNotify, String title, String counter) {
        this.showNotify = showNotify;
        this.title = title;
        this.counter = counter;
    }

    public NavDrawerItem(String title, int imageView, String counter) {
        this.title = title;
        this.imageView = imageView;
        this.counter = counter;
    }

    public NavDrawerItem(String title, int imageView) {
        this.title = title;
        this.imageView = imageView;
    }

    public NavDrawerItem(boolean showNotify, String title, int imageView, String counter) {
        this.showNotify = showNotify;
        this.title = title;
        this.imageView = imageView;
        this.counter = counter;
    }

    public int getImageView() {
        return imageView;
    }

    public void setImageView(int imageView) {
        this.imageView = imageView;
    }

    public String getCounter() {
        return counter;
    }

    public void setCounter(String counter) {
        this.counter = counter;
    }

    public boolean isShowNotify() {
        return showNotify;
    }

    public void setShowNotify(boolean showNotify) {
        this.showNotify = showNotify;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }



}
