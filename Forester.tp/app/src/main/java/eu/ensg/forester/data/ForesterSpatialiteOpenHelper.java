package eu.ensg.forester.data;

import android.content.Context;

import java.io.IOException;

import eu.ensg.spatialite.SpatialiteDatabase;
import eu.ensg.spatialite.SpatialiteOpenHelper;
import jsqlite.Exception;

/**
 * Created by prof on 25/02/16.
 */
public class ForesterSpatialiteOpenHelper extends SpatialiteOpenHelper{

    public static final int version = 1;
    public static final String File_name = "Forester.sqlite";
    public static final int SRID = 4326;

    public ForesterSpatialiteOpenHelper(Context context) throws Exception, IOException {
        super(context, File_name, version);
    }

    @Override
    public void onCreate(SpatialiteDatabase db) throws jsqlite.Exception {

        db.exec("CREATE TABLE Forester ("
                            + "ID integer PRIMARY KEY AUTOINCREMENT,"
                            + "FirstName string NULL,"
                            +" LastName string NULL,"
                            + "Serial string NOT NULL"
                            +");");

        db.exec("CREATE TABLE PointOfInterest ( "
                            + "ID integer PRIMARY KEY AUTOINCREMENT, "
                            + "ForesterID integer NOT NULL, "
                            + "Name string NOT NULL, "
                            + "Description string, "
                            + "CONSTRAINT FK_poi_forester "
                            + "FOREIGN KEY (foresterID) "
                            + "REFERENCES forester (id) "
                            + " ); ");
        db.exec("CREATE INDEX idx_poi_interest_id ON PointOfInterest (foresterID);");

        db.exec("SELECT\n" +
                "AddGeometryColumn('PointOfInterest'\n" +
                ", 'position', "+SRID+", 'POINT', 'XY',\n" +
                "0);");

        db.exec("CREATE TABLE District ("
                            + "ID integer PRIMARY KEY AUTOINCREMENT,"
                            + "ForesterID integer NOT NULL,"
                            + "Name string NOT NULL,"
                            + "Description string,"
                            + "CONSTRAINT FK_destrict_forester "
                            + "FOREIGN KEY (foresterID) "
                            + "REFERENCES forester (id) "
                            + " );");

        db.exec("CREATE INDEX idx_destrict_id ON District (foresterID);");
        db.exec("SELECT AddGeometryColumn('District','Area', "+SRID+", 'POLYGON', 'XY', 0);");

    }

    @Override
    public void onUpgrade(SpatialiteDatabase db, int oldVersion, int newVersion) throws Exception {

    }
}
