package uk.ac.uea.mapdemonew;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Path;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MapsActivity extends FragmentActivity {



    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Address address;
    String[] columns = new String[10];
    MarkerOptions[] locations = new MarkerOptions[333];
    private LatLng uea = new LatLng(52.6219942,1.2368956);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setUpMapIfNeeded();

       try {
            ReadMap();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void ReadMap() throws IOException {

        BufferedReader br = null;

        String line = "";
        String cvsSplitBy = ",";

        InputStream stream = getResources().openRawResource(R.raw.mapdata);
        br = new BufferedReader(new InputStreamReader(stream));
int i =0;
        while ((line = br.readLine()) != null) {

            columns = line.split(cvsSplitBy);

            float lat = Float.parseFloat(columns[2]);
            float longt = Float.parseFloat(columns[3]);
            String title = columns[1];
            MarkerOptions markerOption = new MarkerOptions();

            markerOption.position(new LatLng(lat,longt)).title(title);
            locations[i] = new MarkerOptions().position(new LatLng(lat,longt)).title(title);
                    i++;
           // mMap.addMarker(markerOption);

        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    public void onSearch(View view) {
      //  Log.d("YOMOMO", address.getFeatureName());

        EditText location_tf = (EditText) findViewById(R.id.TFaddress);
        String location = location_tf.getText().toString();
        List<Address> addressList = null;
        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
/*
            for (int i = 0; i < locations.length; i++) {
                if (address.getFeatureName().equalsIgnoreCase(locations[i].getTitle())) {
                    address.setFeatureName(locations[i].getTitle());
                    address.setLatitude(locations[i].getPosition().latitude);
                    address.setLongitude(locations[i].getPosition().longitude);
                }

            }
             Log.d("YOMOMO", address.getFeatureName());
*/

                try {
                    addressList = geocoder.getFromLocationName(location, 1);


                } catch (IOException e) {
                    e.printStackTrace();
                }

                address = addressList.get(0);
            }
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title(address.getFeatureName().toUpperCase()));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));




    }


    public void changeType(View view) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {

        EditText location_tf = (EditText) findViewById(R.id.TFaddress);
        String location = location_tf.getText().toString();
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title(location.toString()));

        // Enable MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true);

        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Get Current Location

        Location myLocation;
        myLocation = locationManager.getLastKnownLocation(provider);


        // set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Get latitude of the current location
        double latitude = myLocation.getLatitude();

        // Get longitude of the current location
        double longitude = myLocation.getLongitude();

        // Create a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);

        // Show the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You are here!"));
    }

}