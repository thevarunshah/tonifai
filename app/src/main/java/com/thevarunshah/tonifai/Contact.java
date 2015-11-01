package com.thevarunshah.tonifai;

import android.graphics.Bitmap;

public class Contact {

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
}
