package com.example.ravi.bag_location;

import android.content.Intent;
import android.graphics.Color;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.StringReader;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseDatabase database;
    List<LatLng> MapLocation = new ArrayList<>();
    List<String> timedateList = new ArrayList<>();
    FirebaseUser firebase_user;
    private DatabaseReference myRef;
    private String uuid, name, email;
    private String bag_ref;
    private int index = 1;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public Marker previousMarker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getCurrentuser();
        bag_ref = uuid+"_1";
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(bag_ref);
        getData();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

 /*       FloatingActionButton fabLocation = (FloatingActionButton) findViewById(R.id.livelocation);
        fabLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace LiveLocation", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
        FloatingActionButton fabAdd = (FloatingActionButton) findViewById(R.id.add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                Menu menu =  navigationView.getMenu();
                String item = "Bag "+(index);
                menu.add(item);
                menu.getItem(index).setIcon(R.drawable.ic_menu_send);
                Toast.makeText(getApplicationContext(), "Added Bag "+(index++)+" Successfully", Toast.LENGTH_SHORT).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        View hView =  navigationView.getHeaderView(0);
        TextView textview = (TextView) hView.findViewById(R.id.textName);
        textview.setText(name);
        textview = (TextView) hView.findViewById(R.id.textEmail);
        textview.setText(email);

        Menu menu =  navigationView.getMenu();
        String item = "Bag 1";
        menu.add(item);
        menu.getItem(index++).setIcon(R.drawable.ic_menu_send);
    }
    void getData(){
        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot topSnapshot) {
                for (DataSnapshot snapshot: topSnapshot.getChildren()) {
                    data data = snapshot.getValue(data.class);
                    MapLocation.add(new LatLng(data.latitude,data.longitude));
                    timedateList.add(data.timedate);
                }
                try{
                mMap.clear();
                }
                catch(Exception e){}
                onMapReady(mMap);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("TAG", "Failed to read user", error.toException());
            }
        });
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                try {
                    previousMarker.hideInfoWindow();
                }
                catch(Exception e){}
                if (marker != null) {
                    marker.showInfoWindow();
                }
                previousMarker = marker;
                return true;
            }
        });
        int i=1,j=0;
        for(LatLng current : MapLocation) {
            mMap.addMarker(new MarkerOptions().position(current).title(String.valueOf(i++)).snippet(timedateList.get(j++)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current,14.0f));
        }
        Polyline line = mMap.addPolyline(new PolylineOptions()
                .addAll(MapLocation)
                .width(3)
                .color(Color.RED));
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if(item.getItemId() == R.id.signout){
            signout();
        }else {
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            Menu menu = navigationView.getMenu();
            int in = menu.getItem(item.getItemId()).getItemId();
            bag_ref = uuid + "_" +String.valueOf(in+2) ;

            myRef = database.getReference(bag_ref);
            MapLocation = new ArrayList<>();
            timedateList = new ArrayList<>();
            getData();
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            onMapReady(mMap);
        }

        return true;
    }
    void signout(){
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        Intent i=new Intent(MainActivity.this,login.class);
        startActivity(i);

    }

    private void getCurrentuser()
    {
        firebase_user = FirebaseAuth.getInstance().getCurrentUser();
        if (firebase_user != null) {
            uuid = firebase_user.getUid();
            name = firebase_user.getDisplayName();
            email = firebase_user.getEmail();
        }
        else{
            Intent i=new Intent(MainActivity.this,login.class);
            startActivity(i);
        }
    }
}
