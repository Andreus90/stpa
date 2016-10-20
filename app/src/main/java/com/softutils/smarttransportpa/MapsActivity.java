package com.softutils.smarttransportpa;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Permission;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button changeLoc, query;
    private LocationManager manager;
    private HttpURLConnection connection;
    private URL url;
    private BufferedReader reader;
    private InputStream is;
    private StringBuffer buffer;
    private JSONObject object;
    private JSONArray array;
    private OutputStream os;
    private BufferedWriter writer;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle("GPS Not Enable")
                    .setMessage("Your GPS is turn off.\nDo you want to turn on your GPS sensor?")
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Without services location you cannot use at 100% this application", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .create();
            dialog.show();
        }
        query = (Button) findViewById(R.id.query);
        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView queryText = (TextView) findViewById(R.id.queryText);
                try {
                    url = new URL("http://192.168.1.103/prova.php");
                    queryText.setText(new GetQuery().execute(url).get());
                } catch (Exception e){e.printStackTrace(); queryText.setText("Error");}
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        changeLoc = (Button) findViewById(R.id.newMap);
        changeLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng pos = new LatLng(0, 0);
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(pos).title("Equatore"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
            }
        });
        LatLng std = new LatLng(34,89);
        mMap.addMarker(new MarkerOptions().position(std).title("Standard"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(std));
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public class GetQuery extends AsyncTask<URL,Void,String> {
        @Override
        protected String doInBackground(URL... urls) {
            String query = "SELECT * FROM agency";
            try {
                connection = (HttpURLConnection) urls[0].openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                os = connection.getOutputStream();
                writer = new BufferedWriter(new OutputStreamWriter(os));
                writer.write("query="+query);
                writer.flush();

                is = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                buffer = new StringBuffer();
                String line;
                while ((line = reader.readLine())!=null) {
                    buffer.append(line);
                }

                String result = buffer.toString();
                object = new JSONObject(result);
                array = object.getJSONArray("risultato");
                String publish="";

                for(int i=0; i<array.length(); i++) {
                    object=array.getJSONObject(i);
                    publish += object.getString("agency_id")+"\t"+object.getString("agency_name")+"\n";
                }
                return publish;
            } catch (Exception e){ e.printStackTrace(); return "error";}
        }
    }
}