package panos.locationtrack;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int MY_PERMISSION_REQUEST_CODE = 7171;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 7172;
    public EditText phoneText;
    private TextView txtCoordinates;

    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    public String strPhone;
    public String strInfo;
    public String strContent;

    private static final int REQUEST_CALL = 1;
    private static final int PERMISSION_SEND_SMS = 2;




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
                break;
        }

        //here we get a callback when permission was requested and we can react to it

        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Button btnGetCoordinates;

        super.onCreate(savedInstanceState);
        setContentView(panos.locationtrack.R.layout.activity_main);

        phoneText = (EditText) findViewById(panos.locationtrack.R.id.phoneText) ;
        txtCoordinates = (TextView) findViewById(panos.locationtrack.R.id.txtCoordinates);
        btnGetCoordinates = (Button) findViewById(panos.locationtrack.R.id.btnGetCoordinates);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Run-time request permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        //Here we move the variables from FormActivity to the MainActivity
        Intent intent = getIntent();
        final String Content = intent.getStringExtra(FormActivity.EXTRA_TEXT);

        Button Move = (Button) findViewById(R.id.Move);
        Move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewWindow();
            }
        });

        btnGetCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRequestingLocationUpdates = true;
                startLocationUpdates();
                displayLocation();

                strPhone = phoneText.getText().toString();

                if (! strPhone.isEmpty()) {

                    if ( Content != null) {
                        strContent = strInfo + Content;
                        Log.d("first", strContent);
                        Log.d("second", Content);
                        sendSMS(strPhone, strContent);
                        makePhoneCall();
                        Toast.makeText(MainActivity.this, "Help is on the way, additional data has been send", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("third", strInfo);
                        sendSMS(strPhone, strInfo);
                        makePhoneCall();
                        Toast.makeText(MainActivity.this, "Help is on the way", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }

    private void makePhoneCall() {

        //checks if phone number has spaces and if it is not empty in order to proceed
        if (strPhone.trim().length() > 0) {

            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                String call = "tel:" + strPhone;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(call)));
            }

        } else {
            Toast.makeText(MainActivity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
        }
    }


    private void sendSMS(String strPhone, String strInfo) {

        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.SEND_SMS}, PERMISSION_SEND_SMS );
        } else {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(strPhone, null, strInfo, null, null);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        if(mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        super.onStop();
    }


    public void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            txtCoordinates.setText(latitude + " / " + longitude);
            strInfo = txtCoordinates.getText().toString();
        } else
            txtCoordinates.setText("0xFFFFFFFF");

    }

    private void createLocationRequest() {

        int UPDATE_INTERVAL = 5000; // SEC 5000
        int FASTEST_INTERVAL = 3000; // SEC
        int DISPLACEMENT = 10; // METERS



        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        //Fix first time run app if permission doesn't grant yet so can't get anything
        mGoogleApiClient.connect();


    }



    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(), "This device is not supported", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }


    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        if(mRequestingLocationUpdates)
            startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
            mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
            mLastLocation = location;
        displayLocation();
    }

        public void NewWindow() {
            Intent intent = new Intent(this, FormActivity.class);
            startActivity(intent);
        }
}
