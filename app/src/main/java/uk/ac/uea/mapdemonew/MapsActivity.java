package uk.ac.uea.mapdemonew;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Path;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import uk.ac.uea.framework.framework.sl.directions.pojos.DirectionsPojo;
import uk.ac.uea.framework.framework.sl.directions.pojos.Legs;
import uk.ac.uea.framework.framework.sl.directions.pojos.Routes;
import uk.ac.uea.framework.framework.sl.directions.pojos.Steps;
import uk.ac.uea.framework.framework.sl.utils.JsonGenerator;
import uk.ac.uea.framework.mapdemonew.DirectionsJSONParser;


public class MapsActivity extends FragmentActivity implements View.OnClickListener {


    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Location myLocation = null;
    Location destination = new Location("Destination");
    ArrayList<MarkerOptions> locations = new ArrayList<MarkerOptions>();
    String directionsURL = null;
    String directions = null;
    AutoCompleteTextView location_tf;


    ImageButton getToLoc;
    ImageButton simpleView;
    ImageButton advancedView;
    ImageButton help;

    ///////////////////////// UI METHODS /////////////////////////

    public void onClickHelp(View view) {
        Intent getHelpPage = new Intent(this, HelpPage.class);

        startActivity(getHelpPage);


    }

    /**
     * Method that returns a marker when given a string title.
     * @param name
     * @return
     */
    public MarkerOptions getMarkerWithName(String name) {
        MarkerOptions searchedLoc = null;
        for (int i = 0; i < locations.size(); i++) {

            if (name.equalsIgnoreCase(locations.get(i).getTitle())) {

                searchedLoc = locations.get(i);

            }

        }
        return searchedLoc;
    }

    public void onClickDescription(View view) {
        Intent getTextPage = new Intent(this, DirectionsPage.class);

        getTextPage.putExtra("Direction data", directions);
        startActivity(getTextPage);

    }

    public void onClickLocInfo(View view) {

        Intent getTextPage = new Intent(this, LocationInfo.class);

        MarkerOptions searchedLoc = getMarkerWithName(location_tf.getText().toString());

        getTextPage.putExtra("Building data", searchedLoc.getTitle());
        startActivity(getTextPage);

    }

    /////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // textView = (TextView) findViewById(R.id.textView);
        getToLoc = (ImageButton) findViewById(R.id.button);
        simpleView = (ImageButton) findViewById(R.id.button2);
        advancedView = (ImageButton) findViewById(R.id.button3);
        help = (ImageButton) findViewById(R.id.button4);
        location_tf = (AutoCompleteTextView) findViewById(R.id.TFaddress);

        getToLoc.setOnClickListener(this);
        simpleView.setOnClickListener(this);
        advancedView.setOnClickListener(this);
        help.setOnClickListener(this);
        // textView.setAlpha(0);

        String[] ueaLocationsList = getResources().getStringArray(R.array.ueaLocationsList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ueaLocationsList);
        location_tf.setAdapter(adapter);

        setUpMapIfNeeded();

        try {
            ReadMap();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                if (myLocation != null && destination != null)
                    onGetToLocation();

                break;
            case R.id.button2:
                if (myLocation != null && destination != null && directionsURL != null) {
                    LatLng origin = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    LatLng dest = new LatLng(destination.getLatitude(), destination.getLongitude());

                    String url = null;
                    try {
                        url = getDirectionsUrl(origin, dest);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        DirectionFinder();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    onClickDescription(v);
                    directions = null;
                }
                break;
            case R.id.button3:
                onClickLocInfo(v);

                break;
            case R.id.button4:

                onClickHelp(v);

                break;
        }
    }

    void ReadMap() throws IOException {

        String[] columns = new String[10];

        BufferedReader br = null;

        String line = "";
        String cvsSplitBy = ",";

        InputStream stream = getResources().openRawResource(R.raw.mapdata);
        br = new BufferedReader(new InputStreamReader(stream));

        while ((line = br.readLine()) != null) {

            columns = line.split(cvsSplitBy);


            float lat = Float.parseFloat(columns[2]);
            float longt = Float.parseFloat(columns[3]);
            String title = columns[1];
            MarkerOptions markerOption = new MarkerOptions();

            markerOption.position(new LatLng(lat, longt)).title(title);

            locations.add(markerOption);
            // mMap.addMarker(markerOption);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    public void onSearch(View view) {

        String loc = location_tf.getText().toString();

        MarkerOptions searchedLoc = getMarkerWithName(loc);

        destination.setLatitude(searchedLoc.getPosition().latitude);
        destination.setLongitude(searchedLoc.getPosition().longitude);

        if (searchedLoc != null) {
            mMap.clear();
            mMap.addMarker(searchedLoc);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(searchedLoc.getPosition()));

        }

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

    private void onGetToLocation() {
        String url = null;
        try {
            url = getDirectionsUrl(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), new LatLng(destination.getLatitude(), destination.getLongitude()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);

    }

    private void setUpMap() {

        EditText location_tf = (EditText) findViewById(R.id.TFaddress);
        String location = location_tf.getText().toString();
        // Enable MyLocation Layer of Google Map

        mMap.setMyLocationEnabled(true);

        updateMyLocation();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(20));
        //  mMap.addMarker(new MarkerOptions().position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())).title("You are here!"));

    }

    /**
     * UPDATES USER LOCATION
     */
    private void updateMyLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        myLocation = locationManager.getLastKnownLocation(provider);
    }


    private String getDirectionsUrl(LatLng origin, LatLng dest) throws IOException {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Waypoints


        String walk = "avoid=highways&mode=walking";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + walk;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {

        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        directionsURL = data;
        return data;
    }

    /**
     * Receives a JSONObject and returns a list of lists containing latitude and longitude
     *
     * @param jObject
     * @return
     */
    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {

            jRoutes = jObject.getJSONArray("routes");

            // Traversing all routes
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                // Traversing all legs
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    // Traversing all steps
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";

                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");

                        List<LatLng> list = decodePoly(polyline);

                        // Traversing all points
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(list.get(l).latitude));
                            hm.put("lng", Double.toString(list.get(l).longitude));
                            path.add(hm);

                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }


        return routes;
    }

    /**
     * @param encoded
     * @return
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }


        return poly;
    }

    public void DirectionFinder() throws IOException {
        List<String> d = new ArrayList<>();

        DirectionsPojo dp = (DirectionsPojo) JsonGenerator.generateTOfromJson(directionsURL, DirectionsPojo.class);
        for (Routes route : dp.getRoutes()) {

            d.add("----- Route Begins ------");
            //System.out.println("----- Route Begins ------");
            for (Legs leg : route.getLegs()) {
                d.add("Total Distance " + leg.getDistance().getText() + "\n");
                d.add(leg.getStart_address());
                //System.out.println("Total Distance "+leg.getDistance().getText());
                for (Steps step : leg.getSteps()) {

                    d.add(step.getDistance().getText());
                    d.add("Walk for: " + step.getDuration().getText());


                }
                d.add(leg.getEnd_address());
            }
            d.add("----- Route Ends ------");

        }
        d.add("\n");
        directions = "\n";
        for (int i = 0; i < d.size(); i++) {
            directions += d.get(i) + "\n";
        }

    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }


            return data;
        }

        // Executes in UI thread, after the execution of
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                //lineOptions.color(Color.MAGENTA);
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(7);
                lineOptions.color(Color.MAGENTA);
            }

            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }


}