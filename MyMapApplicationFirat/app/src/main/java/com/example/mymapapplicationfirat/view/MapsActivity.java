package com.example.mymapapplicationfirat.view;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.example.mymapapplicationfirat.R;
import com.example.mymapapplicationfirat.model.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.mymapapplicationfirat.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener,GoogleMap.OnMarkerClickListener,GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    SQLiteDatabase database;


    LocationManager locationManager;
    LocationListener locationListener;
    double latitude,longitude;
    double end_latitude,end_longitude;


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent =getIntent();
        String info=intent.getStringExtra("info");
        if (info.matches("new")){
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener= new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    SharedPreferences sharedPreferences =MapsActivity.this.getSharedPreferences("com.example.mymapapplicationfirat",MODE_PRIVATE);
                    boolean trackBoolean=sharedPreferences.getBoolean("trackable",false);
                    if (trackBoolean==false){   /* !trackBoolean   da yazılabilir trackBoolean==false yerine */
                        LatLng userLocation= new LatLng(location.getLatitude(),location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
                        sharedPreferences.edit().putBoolean("trackable",true).apply();
                    }


                }
            };
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
            }else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation !=null){
                    LatLng lastUserLocation= new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                }
            }

        }else {
            //sqlite data

          Place place   = (Place) intent.getSerializableExtra("place");
          LatLng latLng = new LatLng(place.latitude,place.longitude);
          String placeName=place.name;
          mMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
          mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

        }
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull  int[] grantResults) {




        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length >0){
            if (requestCode==1){
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    double lat1=0,lat2=0,lon1=0,lon2=0,el1=0,el2=0;
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);


                    Intent intent =getIntent();

                    String info=intent.getStringExtra("info");


                    if (info.matches("new")){
                        Location lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastLocation !=null){
                            LatLng lastUserLocation= new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));


                        }

                    }else {

                        Location lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        Place place   = (Place) intent.getSerializableExtra("place");
                        LatLng latLng = new LatLng(place.latitude,place.longitude);



                        String placeName=place.name;
                        mMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

                    }
                }
            }
        }
    }

    @Override
    public void onMapLongClick( LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address="";

        try {
            List<Address> addressList=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(addressList != null && addressList.size()>0){
                if(addressList.get(0).getThoroughfare()!=null){
                    address +=addressList.get(0).getThoroughfare();

                    if (addressList.get(0).getSubThoroughfare()!=null){
                        address +="";
                        address +=addressList.get(0).getSubThoroughfare();
                    }
                }
            }else
            {
                address ="New Place";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        mMap.clear();
        mMap.addMarker(new MarkerOptions().title(address).position(latLng));
        Double latitude =latLng.latitude;
        Double longitude=latLng.longitude;

       final   Place place=new Place(address,latitude,longitude);

        //veri kaydetmek için yazılan kodlar
        AlertDialog.Builder aleBuilder   = new AlertDialog.Builder(MapsActivity.this);
        aleBuilder.setCancelable(false);   // kullanıcı seçeneklerımızden birini seçmek zorunda
        aleBuilder.setTitle("Are you sure");
        aleBuilder.setMessage(place.name);
        aleBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {

                    database=MapsActivity.this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
                    database.execSQL("CREATE TABLE IF NOT EXISTS places(id INTEGER PRIMARY KEY ,name VARCHAR,latitude VARCHAR,longitude VARCHAR)");

                    String toCompile ="INSERT INTO places(name,latitude,longitude) VALUES(?,?,?)";
                    SQLiteStatement sqLiteStatement=database.compileStatement(toCompile);
                    sqLiteStatement.bindString(1, place.name);
                    sqLiteStatement.bindString(2,String.valueOf(place.latitude));
                    sqLiteStatement.bindString(3,String.valueOf(place.longitude));

                    sqLiteStatement.execute();


                    Toast.makeText(getApplicationContext(),"Saved !",Toast.LENGTH_LONG).show();


                }catch (Exception e){
                    e.printStackTrace();

                }

            }
        });
        aleBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),"Canceled",Toast.LENGTH_LONG).show();

            }
        });

        aleBuilder.show();









    }
    public   double distance(double lat2,double lat1,double lon2,double lon1,double el1,double el2 ){
        /// odtü teknokent enlem ve boylamı

        lat2=39.89711212098982;
        lon2=32.77584649622441;







        final  int W =6371; // World Radius
        double latDistance=Math.toRadians(lat2-lat1);
        double lonDistance=Math.toRadians(lon2-lon1);
        double a=Math.sin(latDistance/2)*Math.sin(lonDistance/2)
                +Math.cos(Math.toRadians(lat1)*Math.cos(Math.toRadians(lat2)))
                *Math.sin(lonDistance/2)*Math.sin(lonDistance/2);
        double c = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        double distance= W*c*1000 ; // metreye cevırdık
        double height=el1-el2;
        distance=Math.pow(distance,2)+Math.pow(height,2);
        System.out.println(distance);
        return Math.sqrt(distance);



    }


    @Override
    public boolean onMarkerClick(@NonNull  Marker marker) {
        marker.setDraggable(true);
        return false;
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDrag(@NonNull  Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull  Marker marker) {
        end_latitude=marker.getPosition().latitude;
        end_longitude=marker.getPosition().longitude;


    }
    public void findDistance(){
  mMap.clear();
  MarkerOptions markerOptions=new MarkerOptions();
  markerOptions.position(new LatLng(end_latitude,end_longitude));
  markerOptions.title("Destination");
  float results[] = new float[10];
  Location.distanceBetween(latitude,longitude,end_latitude,end_longitude,results);
  markerOptions.snippet("Distance="+results[0]);
  mMap.addMarker(markerOptions);
  


    }
}