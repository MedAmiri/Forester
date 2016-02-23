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

public class LoginActivity extends AppCompatActivity {

    private EditText editText;
    private Button login;
    private Button create;
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
    }

    private void create_onClick(View view){
        Intent intent = new Intent(this, CreateUserActivity.class);
        startActivity(intent);
    }

    private void login_onClick(View v){

        String serial = editText.getText().toString();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("serial", serial);

        //Toast
        Toast leToast = Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG);
        leToast.show();


        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}
