package com.thevarunshah.tonifai;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;

public class HomeScreen extends AppCompatActivity {

    ListView lv;
    ArrayAdapter<String> aa;
    ArrayList<String> contacts = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);

        lv = (ListView) findViewById(R.id.contactsInfo);
        aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, contacts);
        lv.setAdapter(aa);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView tv = (TextView) view;
                String details = tv.getText().toString();
                int nameIndex = details.indexOf(';');
                String name = details.substring(0, nameIndex);
                int numberIndex = details.indexOf(';', nameIndex + 1);
                String number = details.substring(nameIndex + 1, numberIndex);
                String bitmap = details.substring(numberIndex + 1);
                Uri bitmapUri;
                if(!bitmap.equals("")) {
                    bitmapUri = getImageUri(getApplicationContext(), stringToBitmap(bitmap));
                    doHttpPost(number, bitmapUri);
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
        StringBuffer sb = new StringBuffer();
        //sb.append("......Contact Details.....");
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        String phone = null;
        String image_uri = "";
        Bitmap bitmap = null;
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                image_uri = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    //System.out.println("name : " + name + ", ID : " + id);
                    sb.append(name+";");

                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        sb.append(phone+";");
                        //System.out.println("phone" + phone);
                        break;
                    }
                    pCur.close();

                    if (image_uri != null) {
                        //System.out.println(Uri.parse(image_uri));
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(image_uri));
                            sb.append(bitmapToString(bitmap));
                            //System.out.println(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    contacts.add(sb.toString());
                    sb = new StringBuffer();
                    //sb.append("\n........................................");
                }
            }
        }
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private String bitmapToString(Bitmap in){

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        in.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        return Base64.encodeToString(bytes.toByteArray(), Base64.DEFAULT);
    }

    private Bitmap stringToBitmap(String in){

        byte[] bytes = Base64.decode(in, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void doHttpPost(String number, Uri uri){

        //do this wherever you are wanting to POST
        URL url;
        HttpURLConnection conn;

        try{
            //if you are using https, make sure to import java.net.HttpsURLConnection
            url=new URL("http://2c7cfed2.ngrok.com/");

            //you need to encode ONLY the values of the parameters
            String param="phonenum=" + URLEncoder.encode(number,"UTF-8")+
            "&bitmap="+URLEncoder.encode(uri.toString(),"UTF-8");

            conn=(HttpURLConnection)url.openConnection();
            //set the output to true, indicating you are outputting(uploading) POST data
            conn.setDoOutput(true);
            //once you set the output to true, you don’t really need to set the request method to post, but I’m doing it anyway
            conn.setRequestMethod("POST");

            //Android documentation suggested that you set the length of the data you are sending to the server, BUT
            // do NOT specify this length in the header by using conn.setRequestProperty(“Content-Length”, length);
            //use this instead.
            conn.setFixedLengthStreamingMode(param.getBytes().length);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //send the POST out
            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.close();

            //build the string to store the response text from the server
            String response= "";

            //start listening to the stream
            Scanner inStream = new Scanner(conn.getInputStream());

            //process the stream and store it in StringBuilder
            while(inStream.hasNextLine())
                response+=(inStream.nextLine());

            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();

        }
        //catch some error
        catch(MalformedURLException ex){
            Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_SHORT).show();

        }
        // and some more
        catch(IOException ex){

            Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
