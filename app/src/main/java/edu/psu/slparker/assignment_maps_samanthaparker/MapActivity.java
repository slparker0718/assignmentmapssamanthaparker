package edu.psu.slparker.assignment_maps_samanthaparker;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private final String LOG_MAP = "GOOGLE_MAPS";
    private MapFragment googleMapFragment;
    ArrayList<MapLocation> markers = new ArrayList<MapLocation>();
    private MapBroadcastReceiver mapBroadcastReceiver;
    private IntentFilter intentFilter;

    private LatLng currentLatLng;
    private Marker currentMapMarker;
    private Integer id = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        googleMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.google_map);
        googleMapFragment.getMapAsync(this);

        intentFilter = new IntentFilter("edu.psu.slparker.assignment_maps_samanthaparker.action.NEW_MAP_BROADCAST");
        mapBroadcastReceiver = new MapBroadcastReceiver();
    }

    public void firebaseLoadData(final GoogleMap googleMap)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference();

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for(DataSnapshot dSnapshot : dataSnapshot.getChildren())
                {
                    MapLocation maps = dSnapshot.getValue(MapLocation.class);
                        markers.add(maps);
                        triggerBroadcastMessageFromFirebase(maps, id);
                        id++;
                        Log.d(TAG, "Firebase data item: " + maps.toString());
                }

                createMarkersFromFirebase(googleMap);
                mapCameraConfiguration(googleMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void triggerBroadcastMessageFromFirebase(MapLocation maps, Integer id)
    {
        Intent firebaseDataIntent = new Intent(this, MapBroadcastReceiver.class);

        firebaseDataIntent.putExtra("LATITUDE", maps.getLatitude());
        firebaseDataIntent.putExtra("LONGITUDE", maps.getLongitude());
        firebaseDataIntent.putExtra("LOCATION", maps.getLocation());
        firebaseDataIntent.putExtra("ID", id);

        sendBroadcast(firebaseDataIntent);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        registerReceiver(mapBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mapBroadcastReceiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //load prepopulate data from database and configure camera around markers.
        firebaseLoadData(googleMap);

        //get user input data
        Intent intent = getIntent();
        Double latitude = intent.getDoubleExtra("LATITUDE", 0);
        Double longitude = intent.getDoubleExtra("LONGITUDE", 0);
        String location = intent.getStringExtra("LOCATION");
        String description = intent.getStringExtra("DESCRIPTION");

        createCustomMapMarkers(googleMap, new LatLng(latitude, longitude), location, description);


        //set map listeners to handle events.
        useMapClickListener(googleMap);
        useMarkerClickListener(googleMap);
        useMarkerDragListener(googleMap);
        useMapOnLongClickListener(googleMap);
    }

    private void useMapOnLongClickListener(GoogleMap googleMap)
    {
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // Get locality from latlon
                String newLocation = null;
                Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = gcd.getFromLocation(latLng.latitude, latLng.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addresses.size() > 0) {
                    newLocation = addresses.get(0).getLocality();
                }

                // Set new marker attributes
                MapLocation newMarker = new MapLocation();
                newMarker.setLocation(newLocation);
                newMarker.setLatitude(latLng.latitude);
                newMarker.setLongitude(latLng.longitude);

                // Add to firebase
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference dbRef = database.getReference();
                DatabaseReference newRef = dbRef.push();
                newRef.setValue(newMarker);
            }
        });
    }

    private void useMarkerDragListener(final GoogleMap googleMap)
    {
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = gcd.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addresses.size() > 0) {
                    marker.setTitle(addresses.get(0).getLocality());
                }
            }
        });
    }

    private void useMarkerClickListener(GoogleMap googleMap){
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            // If FALSE, when the map should have the standard behavior (based on the android framework)
            // When the marker is clicked, it wil focus / centralize on the specific point on the map
            // and show the InfoWindow. IF TRUE, a new behavior needs to be specified in the source code.
            // However, you are not required to change the behavior for this method.
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i(LOG_MAP, "setOnMarkerClickListener");

                return false;
            }
        });
    }

    private void useMapClickListener(final GoogleMap googleMap){

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latltn) {
                Log.i(LOG_MAP, "setOnMapClickListener");

                if(currentMapMarker != null){
                    // Remove current marker from the map.
                    currentMapMarker.remove();
                }
                // The current marker is updated with the new position based on the click.
                createCustomMapMarkers(
                        googleMap,
                        new LatLng(latltn.latitude, latltn.longitude),
                        "New Marker",
                        "Listener onMapClick - new position"
                                +"lat: "+latltn.latitude
                                +" lng: "+ latltn.longitude);
            }
        });
    }

    private void createCustomMapMarkers(GoogleMap googleMap, LatLng latlng, String title, String snippet){

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng)
                .title(title)
                .snippet(snippet)
                .draggable(true);

        googleMap.addMarker(markerOptions);
    }

    private void createMarkersFromFirebase(GoogleMap googleMap)
    {
        for(MapLocation marker: markers)
        {
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(marker.getLatitude(), marker.getLongitude()))
                    .title(marker.getLocation())
                    .draggable(true));
        }
    }

    private void mapCameraConfiguration(GoogleMap googleMap)
    {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(MapLocation marker: markers)
        {
            builder.include(new LatLng(marker.getLatitude(), marker.getLongitude()));
        }

        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 70);
        googleMap.moveCamera(cameraUpdate);
    }


}
