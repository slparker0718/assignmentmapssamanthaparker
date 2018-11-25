package edu.psu.slparker.assignment_maps_samanthaparker;

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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private final String LOG_MAP = "GOOGLE_MAPS";
    private MapFragment googleMapFragment;
    ArrayList<Maps> markers = new ArrayList<Maps>();

    private LatLng currentLatLng;
    private Marker currentMapMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.google_map);
        googleMapFragment.getMapAsync(this);
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
                    Maps maps = dSnapshot.getValue(Maps.class);
                        markers.add(maps);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //load prepopulate data from database and configure camera around markers.
        firebaseLoadData(googleMap);

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
                Maps newMarker = new Maps();
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
        markerOptions.position(latlng) // coordinates
                .title(title) // location name
                .snippet(snippet); // location description

        // Update the global variable (currentMapMarker)
        currentMapMarker = googleMap.addMarker(markerOptions);
    }

    private void createMarkersFromFirebase(GoogleMap googleMap)
    {
        for(Maps marker: markers)
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

        for(Maps marker: markers)
        {
            builder.include(new LatLng(marker.getLatitude(), marker.getLongitude()));
        }

        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 70);
        googleMap.moveCamera(cameraUpdate);
    }
}
