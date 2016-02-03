package uk.ac.uea.mapdemonew;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HelpPage extends Activity implements View.OnClickListener {
    ImageButton saveCar;
    ImageButton simpleView;
    ImageButton advancedView;
    ImageButton help;
    List<MarkerOptions> locations;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_page);


        saveCar = (ImageButton) findViewById(R.id.button);
        simpleView = (ImageButton) findViewById(R.id.button2);
        advancedView = (ImageButton) findViewById(R.id.button3);
        help = (ImageButton) findViewById(R.id.button4);

        saveCar.setOnClickListener(this);
        simpleView.setOnClickListener(this);
        advancedView.setOnClickListener(this);
        help.setOnClickListener(this);

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
                onClickBack();
                break;
            case R.id.button4:

                break;

        }
    }


    private void onClickBack() {
        Intent getMapsPage = new Intent(this,MapsActivity.class);

        startActivity(getMapsPage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_help_page, menu);
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



}
