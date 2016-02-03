package uk.ac.uea.mapdemonew;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LocationInfo extends AppCompatActivity implements View.OnClickListener {

    ImageButton saveCar;
    ImageButton simpleView;
    ImageButton advancedView;
    ImageButton help;
    List<MyLocation> locationInfo = new ArrayList<MyLocation>();


    public static class MyLocation
    {
        String title;
        String desc;
        LatLng loc;

        public MyLocation(String title, String desc, LatLng loc)
        {
            this.desc = desc;
            this.title = title;
            this.loc = loc;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public LatLng getLoc() {
            return loc;
        }

        public void setLoc(LatLng loc) {
            this.loc = loc;
        }

        public static MyLocation getLocationWithName(String name, List<MyLocation> myLocationList)
        {
            MyLocation locToReturn = null;
            for (int i = 0; i < myLocationList.size(); i++) {

                if (name.equalsIgnoreCase(myLocationList.get(i).getTitle())) {

                    locToReturn = myLocationList.get(i);

                }

            }
            return locToReturn;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions_page);

        try {
            ReadMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView textView = (TextView) findViewById(R.id.textView);

        saveCar = (ImageButton) findViewById(R.id.button);
        simpleView = (ImageButton) findViewById(R.id.button2);
        advancedView = (ImageButton) findViewById(R.id.button3);
        help = (ImageButton) findViewById(R.id.button4);

        saveCar.setOnClickListener(this);
        simpleView.setOnClickListener(this);
        advancedView.setOnClickListener(this);
        help.setOnClickListener(this);


        Intent calledActivity = getIntent();

        String buildingData = calledActivity.getExtras().getString("Building data");

        MyLocation info = MyLocation.getLocationWithName(buildingData, locationInfo);

        textView.setText(info.getTitle() + "\n" + info.getDesc() + "\n" + info.getLoc());

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

            MyLocation myLocation = new MyLocation(title, columns[6],new LatLng(lat,longt));
            locationInfo.add(myLocation);


        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                onClickBack();
                break;
            case R.id.button2:
                onClickBack();
                break;
            case R.id.button3:
                break;
            case R.id.button4:
                onClickHelp(v);
                break;

        }
    }

    private void onClickBack() {
        Intent getBack = new Intent(this,MapsActivity.class);

        startActivity(getBack);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_directions_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickHelp(View view) {
        Intent getHelpPage = new Intent(this,HelpPage.class);

        startActivity(getHelpPage);


    }


}
