package com.thevarunshah.tonifai;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<Contact> {

    private final Activity context;
    private final ArrayList<Contact> contacts;

    public CustomListAdapter(Activity context, ArrayList<Contact> contacts) {

        super(context, R.layout.custom_listview, contacts);

        this.context = context;
        this.contacts = contacts;
    }

    public View getView(int position,View view,ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View contactDetails = inflater.inflate(R.layout.custom_listview, null, true);

        TextView name = (TextView) contactDetails.findViewById(R.id.name);
        ImageView image = (ImageView) contactDetails.findViewById(R.id.image);
        TextView number = (TextView) contactDetails.findViewById(R.id.number);

        name.setText(contacts.get(position).getName());
        if(contacts.get(position).getBitmap() != null)
            image.setImageBitmap(contacts.get(position).getBitmap());
        number.setText(contacts.get(position).getNumber());

        return contactDetails;
    };
}
