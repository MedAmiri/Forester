package eu.ensg.forester;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CreateUserActivity extends AppCompatActivity {


    private Button createOk;
    private EditText editSerial;

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

    }

    private void createOk_onClick(View v){
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("serial",editSerial.getText().toString());
        startActivity(intent);
    }

}
