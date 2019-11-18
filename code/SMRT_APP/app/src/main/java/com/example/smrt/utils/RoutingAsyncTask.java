package com.example.smrt.utils;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//Routing Action
public class RoutingAsyncTask extends AsyncTask<Double, String, String> {

    Routing router;
    HttpURLConnection urlConnection;

    public RoutingAsyncTask(Context ctx) {
        this.router = (Routing) ctx;
    }

    @Override
    protected String doInBackground(Double... coors) {
        StringBuilder result = new StringBuilder();

        try {
            URL url = new URL("http://10.0.3.2:5000/route/v1/driving/"+coors[1]+","+coors[0]+";"+coors[3]+","+coors[2]+"?alternatives=true&overview=full&geometries=geojson");
            urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            JSONObject jObject = new JSONObject(result.toString());
            JSONArray routes = jObject.getJSONArray("routes");
            for(int i=routes.length()-1;i>=0;i--){
                JSONArray route = routes.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
                this.router.addRoute(route,(i==0) ? false:true);
            }
        }catch( Exception e) {
            e.printStackTrace();
        }
        finally {
            urlConnection.disconnect();
        }
        return result.toString();
    }
}
