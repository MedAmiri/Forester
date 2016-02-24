package eu.ensg.forester;

import android.Manifest;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import eu.ensg.spatialite.GPSUtils;
import eu.ensg.spatialite.geom.Point;
import eu.ensg.spatialite.geom.Polygon;
import eu.ensg.spatialite.geom.XY;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private Point currentPosition;
    private Polygon currentDistrict;
    private ViewGroup actionLayout;
    private TextView xy;
    private boolean isRecording = false;
    private Button save;
    private Button abort;


    com.google.android.gms.maps.model.Polygon currentMapPoly;
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
        actionLayout = (ViewGroup) findViewById(R.id.layout);
        xy = (TextView) findViewById(R.id.xy);
        save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButtonOnClick(v);
            }
        });
        abort = (Button) findViewById(R.id.abort);
        abort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abortButtonOnClick(v);
            }
        });
        if (isRecording){
            if(currentDistrict == null){
                currentDistrict = new Polygon();
                currentDistrict.addCoordinate(currentPosition.getCoordinate());
                drawDistrict();
            }
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        //LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        GPSUtils.requestLocationUpdates(this, this);


    }

    @Override
    public void onLocationChanged(Location location) {

        currentPosition = new Point(location.getLongitude(), location.getLatitude());
        Log.i(this.getClass().getName(), "currentPosition : " + currentPosition.toString());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition.toLatLng()));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
        xy.setText(currentPosition.toLatLng().toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case (R.id.poi):
                menuAddPointSelected(item);
            case (R.id.district):
                menuAddDistrictSelected(item);
        }
        return true;
    }

    private void menuAddPointSelected(MenuItem item) {
        mMap.addMarker(new MarkerOptions().position(currentPosition.toLatLng()).title("ma position actuelle").snippet(currentPosition.toString()));
    }

    private void menuAddDistrictSelected(MenuItem item) {
        actionLayout.setVisibility(View.VISIBLE);
        isRecording = true;
    }

    private void drawDistrict(){

        if(currentMapPoly != null ){
            currentMapPoly.remove();
        }
        PolygonOptions polygon = new PolygonOptions();
        for(XY xy : currentDistrict.getCoordinates().getCoords()){
            polygon.add(new Point(xy).toLatLng());
        }
        mMap.addPolygon(polygon);
    }
    private void saveButtonOnClick(View v){
        actionLayout.setVisibility(View.GONE);
        isRecording = false;
    }
    private void abortButtonOnClick(View v){
        actionLayout.setVisibility(View.GONE);
        isRecording = false;
    }
}
