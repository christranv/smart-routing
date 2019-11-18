package com.example.smrt;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.smrt.api.CamsLoader;
import com.example.smrt.api.Router;
import com.example.smrt.model.MapPoint;
import com.example.smrt.utils.Routing;

import org.json.JSONArray;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Routing, IMyLocationConsumer {

  public static GeoPoint CENTER = new GeoPoint(16.054456, 108.222966);

  public static HashMap<String,double[]> streetAndPos = new HashMap<>();
  public static ArrayAdapter adapStreetName;

  private MyLocationNewOverlay myLocationoverlay;
  private MapView mapView;
  private TextView startTextView;
  private TextView endTextView;
  private ConstraintLayout routingInfoLayout;
  private ConstraintLayout choosePointLayout;
  private String activeInput;
  private Polyline rLine;
  private Marker startPoint;
  private Marker endPoint;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //Ingore Internet Security
    if (android.os.Build.VERSION.SDK_INT > 9) {
      StrictMode.ThreadPolicy policy = new
              StrictMode.ThreadPolicy.Builder().permitAll().build();
      StrictMode.setThreadPolicy(policy);
    }
    //Load street data from file
    loadStreetAndPos();
    adapStreetName = new ArrayAdapter(this,android.R.layout.simple_list_item_1,MainActivity.streetAndPos.keySet().toArray());

    Context ctx = getApplicationContext();
    Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

    setContentView(R.layout.activity_main);

    //Layout for routing info
    routingInfoLayout = findViewById(R.id.routingInfoLayout);
    //Layout for choose point
    choosePointLayout = findViewById(R.id.choosePointLayout);

    // set up map
    mapView = findViewById(R.id.mapview);
    mapView.setClickable(true);
    //ability to zoom with 2 fingers
    mapView.setBuiltInZoomControls(false);
    mapView.setMultiTouchControls(true);

    mapView.setUseDataConnection(true);
//    mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);

    mapView.setTilesScaledToDpi(true);

    // zoom to CENTER
    this.mapView.getController().setZoom(16);
    this.mapView.getController().setCenter(CENTER);

    MapEventsReceiver mReceive = new MapEventsReceiver() {
      @Override
      public boolean singleTapConfirmedHelper(GeoPoint p) {
        InfoWindow.closeAllInfoWindowsOn(mapView);
        return false;
      }
      @Override
      public boolean longPressHelper(GeoPoint p) {
        return false;
      }
    };

    MapEventsOverlay OverlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);
    mapView.getOverlays().add(OverlayEvents);

    //User Location
    IMyLocationProvider lp = new GpsMyLocationProvider(this);
    lp.startLocationProvider(this);
    myLocationoverlay = new MyLocationNewOverlay(mapView);
    Bitmap arrowIcon = BitmapFactory.decodeResource(getResources(), R.drawable.user_point);
    myLocationoverlay.setDirectionArrow(null,arrowIcon);
    mapView.getOverlays().add(myLocationoverlay);

//    Load cameras to map
    new CamsLoader(this,mapView).execute();

    startTextView = findViewById(R.id.startTextView1);
    startTextView.setOnClickListener(view -> openRoutInpAct("start"));
    endTextView = findViewById(R.id.endTextView2);
    endTextView.setOnClickListener(view -> openRoutInpAct("end"));

    Button okButton = findViewById(R.id.okButton);
    okButton.setOnClickListener(view -> {
      GeoPoint selPoint = (GeoPoint) mapView.getMapCenter();
      MapPoint mp = new MapPoint(selPoint.getLatitude()+", "+selPoint.getLongitude(),selPoint.getLatitude(),selPoint.getLongitude());
      if(activeInput.equals("start")) startTextView.setText(mp.getLabel());
      else endTextView.setText(mp.getLabel());
      openRoutInpAct(activeInput);
    });
  }

  //Load street's name and it's coordinate
  private void loadStreetAndPos() {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("street_name_and_pos.csv"), "UTF-8"))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] vals = line.split(",");
        this.streetAndPos.put(vals[0],new double[]{Double.parseDouble(vals[1]),Double.parseDouble(vals[2])});
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void onResume(){
    super.onResume();
    mapView.onResume();
  }

  public void onPause(){
    super.onPause();
    mapView.onPause();
  }

  public void openRoutInpAct(String activeInput){
    CENTER = (GeoPoint) mapView.getMapCenter();
    Intent intent = new Intent(this, RoutingInputActivity.class);
    intent.putExtra("start", startTextView.getText().toString());
    intent.putExtra("end", endTextView.getText().toString());
    intent.putExtra("activeInput", activeInput);
    startActivityForResult(intent, 1234);
    mapView.getOverlayManager().remove(rLine);
    mapView.getOverlayManager().remove(startPoint);
    mapView.getOverlayManager().remove(endPoint);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode){
      case 1234:
        if(resultCode == RESULT_OK && data != null){
          if(data.getExtras().getInt("mode")==1){
            routingInfoLayout.setVisibility(View.VISIBLE);
            choosePointLayout.setVisibility(View.INVISIBLE);
//            Arrays.stream((startTextView.getText().toString()+","+endTextView.getText().toString()).split(",")).mapToDouble(Double::parseDouble).toArray();
            String start = data.getExtras().getString("start");
            String end = data.getExtras().getString("end");

            startTextView.setText(start);
            endTextView.setText(end);
            double[] s = streetAndPos.get(start);
            double[] e = streetAndPos.get(end);
            new Router(this,mapView).execute(s[0],s[1],e[0],e[1]);
          }else{
            activeInput = data.getExtras().getString("activeInput");
            routingInfoLayout.setVisibility(View.INVISIBLE);
            choosePointLayout.setVisibility(View.VISIBLE);
          }
        }
    }
  }

  @Override
  public void drawRoute(JSONArray pArr) throws Exception{
    addRoute(pArr, false);
    JSONArray startPos = pArr.getJSONArray(0);
    startPoint = addMarker(startPos.getDouble(0),startPos.getDouble(1));
    JSONArray endPos = pArr.getJSONArray(pArr.length()-1);
    endPoint = addMarker(endPos.getDouble(0),endPos.getDouble(1));

    double[] mp = midPoint(startPoint.getPosition(), endPoint.getPosition());
    mapView.getController().animateTo(new GeoPoint(mp[0],mp[1]));
  }

  private double[] midPoint(GeoPoint A, GeoPoint B) {
    double phi1 = Math.toRadians(A.getLatitude());
    double gamma1 = Math.toRadians(A.getLongitude());

    double phi2 = Math.toRadians(B.getLatitude());
    double deltaGamma = Math.toRadians(B.getLongitude() - A.getLongitude());

    double aux1 = Math.cos(phi2) * Math.cos(deltaGamma);
    double aux2 = Math.cos(phi2) * Math.sin(deltaGamma);

    double x = Math.sqrt((Math.cos(phi1) + aux1) * (Math.cos(phi1) + aux1) + aux2 * aux2);
    double y = Math.sin(phi1) + Math.sin(phi2);
    double phi3 = Math.atan2(y, x);

    double gamma3 = gamma1 + Math.atan2(aux2, Math.cos(phi1) + aux1);

    return new double[]{Math.toDegrees(phi3), (Math.toDegrees(gamma3) + 540) % 360 - 180};
  }

  @Override
  public void addRoute(JSONArray pArr, boolean isSub) throws Exception{
    if (rLine!=null){
        mapView.getOverlayManager().remove(rLine);
    }
    rLine = new Polyline(mapView);
    rLine.setTitle("Central Park, NYC");
    rLine.setSubDescription(Polyline.class.getCanonicalName());
    rLine.setWidth(11f);
    rLine.setColor(getResources().getColor((isSub) ? R.color.colorSubRouting:R.color.colorRouting));
    List<GeoPoint> pts = new ArrayList<>();
    JSONArray j=null;
    Log.d("XXXX",String.valueOf(pArr.length()));
    for(int i=0;i<pArr.length();i++) {
      j = pArr.getJSONArray(i);
      pts.add(new GeoPoint(j.getDouble(0),j.getDouble(1)));
    }
    rLine.setPoints(pts);
    rLine.setGeodesic(true);
    rLine.getPaint().setStrokeCap(Paint.Cap.ROUND);
    rLine.setInfoWindow(new BasicInfoWindow(R.layout.bonuspack_bubble, mapView));
    mapView.getOverlayManager().add(rLine);
    addMarker(j.getDouble(1),j.getDouble(0));
  }

  public Marker addMarker(double lat, double lon){
    Marker marker = new Marker(mapView);
    marker.setPosition(new GeoPoint(lat,lon));
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
    marker.setIcon(getResources().getDrawable(R.drawable.marker,null));
    mapView.getOverlays().add(marker);
    return marker;
  }

  @Override
  public void onLocationChanged(Location location, IMyLocationProvider source) {}
}
