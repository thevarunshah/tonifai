package com.thevarunshah.tonifai;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;

public class HomeScreen extends AppCompatActivity {

    ListView lv;
    ArrayAdapter<Contact> aa;
    ArrayList<Contact> contacts = new ArrayList<Contact>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);

        lv = (ListView) findViewById(R.id.contactsInfo);
        aa = new ArrayAdapter<Contact>(this, android.R.layout.simple_list_item_1, android.R.id.text1, contacts);
        lv.setAdapter(aa);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Contact c = contacts.get(position);
                Toast.makeText(getApplicationContext(), c.getName(), Toast.LENGTH_SHORT).show();

                Bitmap bitmap = c.getBitmap();
                Uri bitmapUri;
                if (c.getBitmap() != null) {
                    bitmapUri = getImageUri(getApplicationContext(), bitmap);
                    doHttpPost(c.getNumber(), bitmapUri);
                } else {
                    doHttpPost(c.getNumber(), null);
                }
            }
        });

        Button fetch = (Button) findViewById(R.id.fetchContacts);
        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                readContacts();
                aa.notifyDataSetChanged();
            }
        });
    }

    public void readContacts() {

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        String phone = "";
        String image_uri = "";
        Bitmap bitmap = null;

        if (cur.getCount() > 0) {

            while (cur.moveToNext()) {

                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                image_uri = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        break;
                    }
                    pCur.close();

                    if (image_uri != null) {
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(image_uri));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    Contact c = new Contact(name, phone, bitmap);
                    contacts.add(c);
                }
            }
        }
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void doHttpPost(String number, Uri uri){

        URL url;
        HttpURLConnection conn;

        try{

            url = new URL("http://2c7cfed2.ngrok.com/initiate");
            String param;
            if(uri == null){
                param = "number=" + URLEncoder.encode(number,"UTF-8") + "&image=" + URLEncoder.encode("","UTF-8");
            }
            else {
                InputStream iStream = getContentResolver().openInputStream(uri);
                byte[] inputData = getBytes(iStream);
                param = "number=" + URLEncoder.encode(number, "UTF-8") + "&image=" + Base64.encodeToString(inputData, Base64.DEFAULT);
            }

            conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setFixedLengthStreamingMode(param.getBytes().length);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.close();

            String response = "";
            Scanner inStream = new Scanner(conn.getInputStream());
            while(inStream.hasNextLine()) {
                response += (inStream.nextLine());
            }

            //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
        } catch(IOException e){

            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
