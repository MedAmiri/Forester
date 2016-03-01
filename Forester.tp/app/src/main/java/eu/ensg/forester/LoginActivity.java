package eu.ensg.forester;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import eu.ensg.forester.data.ForesterSpatialiteOpenHelper;
import eu.ensg.spatialite.SpatialiteDatabase;
import eu.ensg.spatialite.SpatialiteOpenHelper;
import jsqlite.*;
import jsqlite.Exception;

public class LoginActivity extends AppCompatActivity {

    private EditText editText;
    private Button login;
    private Button create;
    private SpatialiteDatabase db;
    private SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        create = (Button) findViewById(R.id.create);
        editText = (EditText) findViewById(R.id.serial);
        login = (Button) findViewById(R.id.login);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create_onClick(v);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_onClick(v);
            }
        });

        String serialExtra = getIntent().getStringExtra("serial");

        preferences = getPreferences(MODE_PRIVATE);

        if(serialExtra == null){
            preferences.getString("serial", "");
        }
        editText.setText(serialExtra);
        //initdb();
    }

    private void create_onClick(View view){
        Intent intent = new Intent(this, CreateUserActivity.class);
        startActivity(intent);
    }

    private void login_onClick(View v){

        String serial = editText.getText().toString();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("serial", serial);

        String serialUser = editText.getText().toString();


        try {
            db = DataBase.getInstance(this).getDatabase();
            Stmt stmt = db.prepare("SELECT ID FROM Forester WHERE Serial = '" + serialUser + "';");

            if(stmt.step()){
                int id =stmt.column_int(0);
                Intent intent = new Intent(this, MapsActivity.class);
                intent.putExtra("ForestID", -1);
                startActivity(intent);

                }else{
                Toast leToast = Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG);
                leToast.show();
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
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
//
//
//    }
}
