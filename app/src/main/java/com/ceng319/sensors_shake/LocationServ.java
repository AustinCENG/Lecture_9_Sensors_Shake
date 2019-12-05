package com.ceng319.sensors_shake;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class LocationServ extends AppCompatActivity implements OnMapReadyCallback {
    private LocationManager mLocManager;
    private boolean isGpsEnabled = false;
    private boolean isNetworkEnabled = false;
    private TextView longitude;
    private TextView latitude;
    private TextView city;
    private GoogleMap mMap;
    private double mlongitude = -79.38;  // rough location, city of toronto
    private double mlatitude = 43.6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: Find all the views on layout.
        setContentView(R.layout.activity_location_serv);
        longitude = findViewById(R.id.longitude);
        latitude = findViewById(R.id.latitude);
        city = findViewById(R.id.city);
        //TODO: start the location search.
        startLocationUpdates();
        //TODO: Handle google maps service.
        handleGoogleMaps();
        // TODO: prevent the screen from orientation change.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


    }

    private void handleGoogleMaps() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void startLocationUpdates() {
        // Acquire a reference to the system Location Manager
        mLocManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListenerObj = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                try {
                    makeUseOfNewLocation(location);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

// Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 2000);
            return;
        }
        isGpsEnabled = mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = mLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        // TODO: If in door case, then just use network provider.
        mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, locationListenerObj);
        // TODO: Try the following if prefer to use GPS over network

        if (isGpsEnabled) {
            mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListenerObj);
        } else if (isNetworkEnabled) {
            mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, locationListenerObj);
        }
    }

    private void makeUseOfNewLocation(Location location) throws IOException {
        mlongitude = location.getLongitude();
        mlatitude = location.getLatitude();
        //after get the current location, handle the googlemaps operation.

       //  f = (float) (Math.round(n*100.0f)/100.0f);
        double longi = location.getLongitude();
        double lati = location.getLatitude();
        DecimalFormat df2 = new DecimalFormat( "#,###,###,##0.00" );
        double longitudevalue = new Double(df2.format(longi)).doubleValue();
        double latitudevalue = new Double(df2.format(lati)).doubleValue();


        longitude.setText("Longitude: " + String.valueOf(longitudevalue));
        latitude.setText("Latitude: " + String.valueOf(latitudevalue));
        updateMarker();
    }

    private void updateMarker() throws IOException {

        if (mMap != null)
        {
            LatLng position = new LatLng(mlatitude, mlongitude);
            mMap.addMarker(new MarkerOptions().position(position).title("Current Position"));

            Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
            if (!gcd.isPresent())
            {
                return;  // if GCD is not present, return
            }

            try {
                List<Address> addresses = gcd.getFromLocation(mlatitude, mlongitude, 1);
                Log.d("MapleLeaf", addresses.toString());
                if (addresses.size() > 0) {
                    city.setText("City: "+ addresses.get(0).getLocality() + ", "
                            + addresses.get(0).getPostalCode()+ ", "
                            + addresses.get(0).getCountryName());
                }
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                Log.e("MapleLeaf", "Error Message", ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                Log.e("MapleLeaf", "Illegal latitude or longitude values", illegalArgumentException);
            }

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // TODO: move the camera to Toronto.
        mMap = googleMap;
        LatLng position = new LatLng(mlatitude, mlongitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 9.0f));

        mMap.setTrafficEnabled(true);
    }
}
