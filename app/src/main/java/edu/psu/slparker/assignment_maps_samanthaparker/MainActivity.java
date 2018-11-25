package edu.psu.slparker.assignment_maps_samanthaparker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private EditText editText_latitude;
    private EditText editText_longitude;
    private EditText editText_location;
    private EditText editText_description;
    private Button button_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText_latitude = findViewById(R.id.editText_latitude);
        editText_longitude = findViewById(R.id.editText_longitude);
        editText_location = findViewById(R.id.editText_location);
        editText_description = findViewById(R.id.editText_description);
        button_submit = findViewById(R.id.button_submit);

        button_submit.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Intent broadcastIntent = new Intent(MainActivity.this, MapBroadcastReceiver.class);
                Double latitude = Double.valueOf(editText_latitude.getText().toString());
                Double longitude = Double.valueOf(editText_longitude.getText().toString());
                String location = editText_location.getText().toString();

                broadcastIntent.putExtra("LATITUDE", latitude);
                broadcastIntent.putExtra("LONGITUDE", longitude);
                broadcastIntent.putExtra("LOCATION", location);

                sendBroadcast(broadcastIntent);

                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                intent.putExtra("LATITUDE", latitude);
                intent.putExtra("LONGITUDE", longitude);
                intent.putExtra("LOCATION", editText_location.getText().toString());
                intent.putExtra("DESCRIPTION", editText_description.getText().toString());

                startActivity(intent);
            }
        });
    }


}
