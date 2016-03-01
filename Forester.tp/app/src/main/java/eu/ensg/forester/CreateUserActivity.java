package eu.ensg.forester;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.lang.Exception;

import eu.ensg.forester.data.ForesterSpatialiteOpenHelper;
import eu.ensg.spatialite.SpatialiteDatabase;
import eu.ensg.spatialite.SpatialiteOpenHelper;
import jsqlite.*;

public class CreateUserActivity extends AppCompatActivity {


    private Button createOk;
    private EditText editSerial;
    private EditText editFirstName;
    private EditText editLastame;
    private  SpatialiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        createOk = (Button) findViewById(R.id.idok);
        createOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createOk_onClick(v);
            }
        });

        editSerial = (EditText) findViewById(R.id.serial3);
        editFirstName = (EditText) findViewById(R.id.serial1);
        editLastame = (EditText) findViewById(R.id.serial2);
        //initdb();
    }

    private void createOk_onClick(View v){
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("serial", editSerial.getText().toString());
        startActivity(intent);

        try {
            saveUserDB(db);
        } catch (jsqlite.Exception e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Save Failed !", Toast.LENGTH_LONG).show();
        }
    }

    private void saveUserDB(SpatialiteDatabase db) throws jsqlite.Exception {

        String FirstName = editFirstName.getText().toString();
        String LastName = editLastame.getText().toString();
        String serial = editSerial.getText().toString();

        //db = DataBase.getInstance(this).getDatabase();
        db.exec("INSERT INTO Forester (FirstName, LastName, Serial) VALUES\n" +
               "('"+FirstName+"', '"+LastName+"', '"+serial+"')");
    }

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
