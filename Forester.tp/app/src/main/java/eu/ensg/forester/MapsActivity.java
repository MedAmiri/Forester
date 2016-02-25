package eu.ensg.forester;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.IOException;
import java.lang.Exception;

import eu.ensg.forester.data.ForesterSpatialiteOpenHelper;
import eu.ensg.spatialite.GPSUtils;
import eu.ensg.spatialite.SpatialiteDatabase;
import eu.ensg.spatialite.SpatialiteOpenHelper;
import eu.ensg.spatialite.geom.BadGeometryException;
import eu.ensg.spatialite.geom.Point;
import eu.ensg.spatialite.geom.Polygon;
import eu.ensg.spatialite.geom.XY;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private com.google.android.gms.maps.model.Polygon currentMapPoly;

    private Point currentPosition;
    private Polygon currentDistrict;

    private boolean isRecording = false;

    private ViewGroup actionLayout;
    private TextView xy;

    private Button save;
    private Button abort;
    private int ForesterId;
    private SpatialiteDatabase db;

    public MapsActivity() {
    }

    //region MAP
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
        ForesterId = getIntent().getIntExtra("ForestID",-1);
        if (isRecording) {
            if (currentDistrict == null) {
                currentDistrict = new Polygon();
                currentDistrict.addCoordinate(currentPosition.getCoordinate());
                drawDistrict();
            }
        }
        initdb();

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
    // endregion

    // region Listner
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

    //endregion

    // region menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case (R.id.poi):
                try {
                    menuAddPointSelected(item);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast leToast = Toast.makeText(this, "Position GPS unKnown", Toast.LENGTH_LONG);
                    leToast.show();
                }

            case (R.id.district):
                menuAddDistrictSelected(item);
        }
        return true;
    }

    private void menuAddPointSelected(MenuItem item) throws Exception {
        actionLayout.setVisibility(View.VISIBLE);
        isRecording = true;
        mMap.addMarker(new MarkerOptions().position(currentPosition.toLatLng())
                .title("ma position actuelle").snippet(currentPosition.toString()));

        try{
            db.exec("INSERT INTO PointOfInterest (ForesterID, Name, Description, position) VALUES\n" +
                    "('" + ForesterId + "','my position' ,'" + currentPosition.toString() + "', '" + currentPosition.toSpatialiteQuery(ForesterSpatialiteOpenHelper.SRID)  + "')");
        }
         catch (jsqlite.Exception e) {
            e.printStackTrace();
        }
        catch (BadGeometryException e){
            e.printStackTrace();
        }
    }

    private void menuAddDistrictSelected(MenuItem item) {
        actionLayout.setVisibility(View.VISIBLE);
        isRecording = true;

        try{
            db.exec("INSERT INTO District (ForesterID, Name, Description, area) VALUES\n" +
                    "('" + ForesterId + "','my district' ,'" + currentPosition.toString() + "', '" + currentPosition.toSpatialiteQuery(ForesterSpatialiteOpenHelper.SRID)  + "')");
        }
        catch (jsqlite.Exception e) {
            e.printStackTrace();
        }
        catch (BadGeometryException e) {
            e.printStackTrace();
        }
    }

    private void drawDistrict() {

        if (currentMapPoly != null) {
            currentMapPoly.remove();
        }
        PolygonOptions polygon = new PolygonOptions();
        for (XY xy : currentDistrict.getCoordinates().getCoords()) {
            polygon.add(new Point(xy).toLatLng());
        }
        currentMapPoly = mMap.addPolygon(polygon);


    }

    //endregion

    //region button
    private void saveButtonOnClick(View v) {
        actionLayout.setVisibility(View.GONE);
        isRecording = false;
        Toast leToast = Toast.makeText(this, "Save Finished", Toast.LENGTH_LONG);
        leToast.show();


    }

    private void abortButtonOnClick(View v) {
        actionLayout.setVisibility(View.GONE);
        isRecording = false;
    }

    //endregion


    private void initdb(){
        try {
            SpatialiteOpenHelper helper = new ForesterSpatialiteOpenHelper(this);
            db = helper.getDatabase();

        } catch (jsqlite.Exception | IOException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Cannot initialize database !", Toast.LENGTH_LONG).show();
            System.exit(0);
        }
    }
}
