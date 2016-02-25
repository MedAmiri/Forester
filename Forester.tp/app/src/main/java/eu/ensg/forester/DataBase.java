package eu.ensg.forester;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;

import eu.ensg.forester.data.ForesterSpatialiteOpenHelper;
import eu.ensg.spatialite.SpatialiteDatabase;
import eu.ensg.spatialite.SpatialiteOpenHelper;

/**
 * Created by prof on 25/02/16.
 */
public class DataBase {

    private static DataBase singleton;
    private SpatialiteDatabase database;

    private DataBase(Context context){
        try {
            SpatialiteOpenHelper helper = new ForesterSpatialiteOpenHelper(context);
            database = helper.getDatabase();

        } catch (jsqlite.Exception | IOException e) {
            e.printStackTrace();
        }
    }

    public SpatialiteDatabase getDatabase() {
        return database;
    }

    public static DataBase getInstance(Context context){
        if(singleton == null){
            singleton = new DataBase(context);
        }
        return singleton;
    }

}
