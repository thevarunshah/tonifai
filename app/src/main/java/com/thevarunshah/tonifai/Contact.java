package com.thevarunshah.tonifai;

import android.graphics.Bitmap;

public class Contact implements Comparable<Contact>{

    private String name;
    private String number;
    private Bitmap bitmap;

    public Contact(String name, String number, Bitmap bitmap){

        this.name = name;
        this.number = number;
        this.bitmap = bitmap;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    public String toString(){

        return this.name + " - " + this.number;
    }

    @Override
    public int compareTo(Contact c){

        return this.getName().compareTo(c.getName());
    }
}
