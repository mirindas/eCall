package panos.locationtrack;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;


public class FormActivity extends AppCompatActivity {

    public static final String EXTRA_TEXT = "locationtrack.EXTRA_TEXT";

    EditText Name;
    EditText VehicleID;
    EditText Age;
    Button Savebtn;
    Button Backbtn;
    String MsdContent;

    String UserName;
    String VIN;
    String UserAge;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(panos.locationtrack.R.layout.activity_form);



        Name = (EditText) findViewById(R.id.Name);
        VehicleID = (EditText) findViewById(R.id.VehicleID);
        Age = (EditText) findViewById(R.id.Age);
        Savebtn = (Button) findViewById(R.id.Save);
        Backbtn = (Button) findViewById(R.id.Back);



        Backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainWindow();
        }
    });

        Savebtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            UserName = Name.getText().toString();
            VIN = VehicleID.getText().toString();
            UserAge = Age.getText().toString();

            MsdContent  = UserName + " " + VIN + " " + UserAge;

            Log.d("bimbo",MsdContent);
            Log.d("Jimbo",UserName);
            MainWindow();

        }

    });

    }

    public void MainWindow() {

        String Content = MsdContent;
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_TEXT,Content);
        startActivity(intent);
    }

}