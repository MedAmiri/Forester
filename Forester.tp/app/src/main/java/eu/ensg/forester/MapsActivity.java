package eu.ensg.forester;

import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Exception;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Locale;

import eu.ensg.forester.data.ForesterSpatialiteOpenHelper;
import eu.ensg.spatialite.GPSUtils;
import eu.ensg.spatialite.SpatialiteDatabase;
import eu.ensg.spatialite.SpatialiteOpenHelper;
import eu.ensg.spatialite.geom.BadGeometryException;
import eu.ensg.spatialite.geom.Point;
import eu.ensg.spatialite.geom.Polygon;
import eu.ensg.spatialite.geom.XY;
import jsqlite.Stmt;


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
    private SpatialiteDatabase db  = DataBase.getInstance(this).getDatabase();;

    public MapsActivity() {
    }

    //region MAP
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
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


        //initdb();

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
        loadPolygon();
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

        if (isRecording) {
            currentDistrict.addCoordinate(currentPosition.getCoordinate());
            drawDistrict(true, currentDistrict);
        }
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

    private void menuAddPointSelected(MenuItem item)  {
        new AsyncTask<Void, Void, String>(){

            @Override
            protected String doInBackground(Void... params) {
                return loadGeoCoding();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.w(this.getClass().getName(), "WEBSERVICE" + s);

                try {
                    JSONObject root = new JSONObject(s);
                    JSONArray array = root.getJSONArray("results");
                    JSONObject ele = (JSONObject) array.get(0);

                    String adress = ele.getString("formatted_adress");

                    db.exec("INSERT INTO PointOfInterest (ForesterID, Name, Description, position) VALUES\n" +
                            "('" + ForesterId + "','my position' ,'" + adress + "', '" + currentPosition.toSpatialiteQuery(ForesterSpatialiteOpenHelper.SRID)  + "')");
                    Toast leToast = Toast.makeText(MapsActivity.this, "Save Finished", Toast.LENGTH_LONG);
                    leToast.show();

                    mMap.addMarker(new MarkerOptions().position(currentPosition.toLatLng())
                            .title("ma position actuelle").snippet(adress));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                catch (jsqlite.Exception e) {
                    e.printStackTrace();
                }
                catch (BadGeometryException e){
                    e.printStackTrace();
                }
            }
        }.execute();

        actionLayout.setVisibility(View.VISIBLE);
        isRecording = true;


    }
    private String loadGeoCoding(){

        try {
            String uri = String.format(new Locale("en", "US"), "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s",
                    currentPosition.toLatLng().latitude,currentPosition.toLatLng().longitude,"AIzaSyC15I_i-AzURhX3MMH1jIUysR1X1aj_l1Q");
            Log.i(this.getClass().getName(),"requete"+uri);
            URL url = new URL(uri);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");}
            Log.w(this.getClass().getName(),"WEBSERVICE" + sb.toString());

            return sb.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private void menuAddDistrictSelected(MenuItem item) {
        currentDistrict = new Polygon();
        actionLayout.setVisibility(View.VISIBLE);
        isRecording = true;
    }

    private void drawDistrict(boolean animation, Polygon polygone) {

        if (currentMapPoly != null && animation) {
            currentMapPoly.remove();
        }
        PolygonOptions polygon = new PolygonOptions();
        for (XY xy : polygone.getCoordinates().getCoords()) {
            polygon.add(new Point(xy).toLatLng());
        }
        currentMapPoly = mMap.addPolygon(polygon);


    }

    //endregion

    //region button
    private void saveButtonOnClick(View v) {
        actionLayout.setVisibility(View.GONE);
        isRecording = false;

        try{
            db.exec("INSERT INTO District (ForesterID, Name, Description, area) VALUES\n" +
                    "('" + ForesterId + "','my district' ,'" + currentDistrict.toString() + "', " + currentDistrict.toSpatialiteQuery(ForesterSpatialiteOpenHelper.SRID)  + ")");
            Toast leToast = Toast.makeText(this, "Save Finished", Toast.LENGTH_LONG);
            leToast.show();
        }
        catch (jsqlite.Exception e) {
            e.printStackTrace();
        }
        catch (BadGeometryException e) {
            e.printStackTrace();
        }

    }

    private void abortButtonOnClick(View v) {
        actionLayout.setVisibility(View.GONE);
        isRecording = false;
    }

    private void loadPolygon(){
        try {
            Stmt stmt = db.prepare("SELECT Name, description, ST_asText(area) FROM District");

            while (stmt.step()){

                String name = stmt.column_string(0);
                String description =stmt.column_string(1);
                String area = stmt.column_string(2);
                Polygon polygone = Polygon.unMarshall(area);
                drawDistrict(true, polygone);
            }

        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        }
    }

//    private void loadPoint() {
//        try {
//            Stmt stmt = db.prepare("SELECT name, description, ST_asText(position) FROM PointOfInterest WHERE foresterID = " + foresterID);
//            while (stmt.step()) {
//                String name = stmt.column_string(0);
//                String description = stmt.column_string(1);
//                Point position = Point.unMarshall(stmt.column_string(2));
//
//                addPointOfInterest(name, description, position);
//            }
//        } catch (jsqlite.Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Sql Error !!!!", Toast.LENGTH_LONG).show();
//        }
//    }

    //endregion


//    private void initdb(){
//        try {
//            SpatialiteOpenHelper helper = new ForesterSpatialiteOpenHelper(this);
//            db = helper.getDatabase();
//
//        } catch (jsqlite.Exception | IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this,
//                    "Cannot initialize database !", Toast.LENGTH_LONG).show();
//            System.exit(0);
//        }
//    }
}
