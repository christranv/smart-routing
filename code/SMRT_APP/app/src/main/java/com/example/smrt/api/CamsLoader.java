package com.example.smrt.api;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.example.smrt.R;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CamsLoader extends AsyncTask<Void,String,String> {

    public static final String address = "http://10.0.3.2:9999";
    public static final String camsSource = address+"/api/cameras";

    private MapView mapView;
    private Context context;

    public CamsLoader(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
//        this.picasso = new Picasso(context);
    }

    @Override
    protected String doInBackground(Void... voids) {
        String result = null;
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(camsSource);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));
            result = reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(connection!=null) connection.disconnect();
            if(reader!=null) {
                try {
                    reader.close();
                } catch (IOException e) {e.printStackTrace();}
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        InputStream iStream=null;
        try {
            JSONArray jObj = new JSONArray(s);

            for(int i=0;i<jObj.length();i++){
                JSONObject j = jObj.getJSONObject(i);

                Marker marker = new Marker(mapView);
                marker.setPosition(new GeoPoint(j.getDouble("lon"),j.getDouble("lat")));
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setIcon(context.getResources().getDrawable(R.drawable.camera,null));
                marker.setTitle("Camera "+j.getString("id"));
                marker.setSnippet(j.getString("streetname"));
                marker.setSubDescription("From: "+j.getString("last_update"));
                iStream = (InputStream) new URL(address+j.getString("last_image")).getContent();
                Drawable img = Drawable.createFromStream(iStream, "src");
                marker.setImage(img);
                iStream.close();
                mapView.getOverlays().add(marker);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                iStream.close();
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }

    }
}
