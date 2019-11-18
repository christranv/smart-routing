package com.example.smrt.api;

import android.content.Context;
import android.os.AsyncTask;

import com.example.smrt.utils.Routing;

import org.json.JSONArray;
import org.osmdroid.views.MapView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Router extends AsyncTask<Double,String,String> {

    public static final String routeSource = CamsLoader.address+"/api/routing/";

    private MapView mapView;
    private Context context;

    public Router(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
    }

    @Override
    protected String doInBackground(Double... poses) {
        String result = null;
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(routeSource+poses[0]+","+poses[1]+"/"+poses[2]+","+poses[3]);
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
        try {
            JSONArray jObj = new JSONArray(s);
//            for(int i=0;i<jObj.length();i++){
//                JSONArray j = jObj.getJSONArray(i);
//
//            }
            ((Routing)context).drawRoute(jObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
