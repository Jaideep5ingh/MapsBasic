package com.component.independent.mapsbasic;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
        , LocationListener {

    GoogleMap mGoogleMap;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    String lat_home,long_home;
    Marker marker_home,marker=null;
    MarkerOptions marker_home_options,options;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String first = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=";
    String second ="&destinations=28.7156138,77.1276354&key=AIzaSyBb3Cv2OOTZdIUxlNV0OBC-HfdGcvrPDsg";
    String url_on ="https://api.thingspeak.com/update?api_key=6S9LM0BJ63OYT5CP&field1=1";
    String json_url,duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        editor = sharedPreferences.edit();

        lat_home=sharedPreferences.getString("Latitude",null);
        long_home = sharedPreferences.getString("Longitude",null);


        if (servicesAvailable()) {
            Toast.makeText(this, "Perfect", Toast.LENGTH_SHORT).show();
            }
            InitMap();

    }



    public boolean servicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int isAvailable = googleApiAvailability.isGooglePlayServicesAvailable(MainActivity.this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApiAvailability.isUserResolvableError(isAvailable)) {
            Dialog dialog = googleApiAvailability.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void InitMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment3);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        if(lat_home!=null && long_home!=null){
            marker_home_options = new MarkerOptions().title("Home")
                    .position(new LatLng(Double.parseDouble(lat_home),
                            Double.parseDouble(long_home)))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

            marker=mGoogleMap.addMarker(marker_home_options);

        }
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();


    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(45*1000);

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission
                        (this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
    if(location == null){
        Toast.makeText(this, "Something Went wrong", Toast.LENGTH_SHORT).show();
    }else{
        LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
        CameraUpdate updates = CameraUpdateFactory.newLatLngZoom(ll,15);
        mGoogleMap.animateCamera(updates);
        updateETA(location);
        setHome(location);



    }
    }

    private void updateETA(Location location) {
        String lat=String.valueOf(location.getLatitude());
        String lng = String.valueOf(location.getLongitude());
        json_url=first+lat+","+lng+second;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, json_url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject jsonObject=null;
                        try {
                            jsonObject = response.getJSONArray("rows").getJSONObject(0)
                                    .getJSONArray("elements").getJSONObject(0).getJSONObject("duration");
                            duration = jsonObject.getString("value");
                            int mins_duration = (Integer.valueOf(duration))/60;
                            Toast.makeText(MainActivity.this, "ETA : " + mins_duration + "minutes", Toast.LENGTH_SHORT).show();
                            if(mins_duration<1){
                                updateServer();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            }
        });

        MySingleton.getInstance(MainActivity.this).addToQueue(jsonObjectRequest);

    }

    private void updateServer() {
        RequestQueue req = Volley.newRequestQueue(MainActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url_on, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Something Went Wrong",Toast.LENGTH_LONG).show();
            }
        });
        req.add(stringRequest);
    }


    public void setHome(final Location location) {
        if(lat_home==null && long_home==null){
            AlertDialog adb = new AlertDialog.Builder(this)
                    .setTitle("Set Home")
                    .setMessage("Set current location as home?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setValues(location);
                            Toast.makeText(MainActivity.this, "Home set at " +
                                    location.getLatitude() + "," + location.getLongitude(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "Home not set", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .create();
            adb.show();
        }

        }


    private void setValues(Location location) {
        String lat,lng;
        lat =  String.valueOf(location.getLatitude());
        lng = String.valueOf(location.getLongitude());
        editor.putString("Latitude",lat);
        editor.putString("Longitude",lng);
        editor.commit();

    }


    private void goToLocationZoom(double lat,double lng,int zoom){
        LatLng ll = new LatLng(lat,lng);
        CameraUpdate updates = CameraUpdateFactory.newLatLngZoom(ll,zoom);
        mGoogleMap.moveCamera(updates);
    }
}
