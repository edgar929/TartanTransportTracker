package com.tartantransporttracker.ui.route;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.tartantransporttracker.R;
import com.tartantransporttracker.managers.BusStopManager;
import com.tartantransporttracker.managers.RouteManager;
import com.tartantransporttracker.models.BusStop;
import com.tartantransporttracker.models.Route;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CreateBusStopActivity extends AppCompatActivity {

    List<Route> items;
    AutoCompleteTextView spinner;

    Button btnCreateBusStop;
    EditText edt_address;
    EditText edt_position;

    BusStopManager busStopManager;
    RouteManager routeManager;
    
    Integer position_;
    Route route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_bus_stop);

        busStopManager = new BusStopManager();
        routeManager = new RouteManager();

        edt_address = findViewById(R.id.edtAddress);
        edt_position = findViewById(R.id.edtPosition);
        btnCreateBusStop = findViewById(R.id.btn_add_bus_stop);

        items = routeManager.findAllRoutes();

        spinner = findViewById(R.id.selectRouteSpinner);
        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, items);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinner.getAdapter().getItem(position) instanceof Route) {
                    route = (Route) spinner.getAdapter().getItem(position);
                    position_ = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.e("SPINNER", "Nothing selected");
            }
        });

        btnCreateBusStop.setOnClickListener(v -> {
            String address = edt_address.getText().toString();

            Integer position = Integer.parseInt(edt_position.getText().toString());

            BusStop busStop = new BusStop(address, route, position);
            busStopManager.createBusStop(busStop);

            Toast.makeText(CreateBusStopActivity.this, "Bus stop created", Toast.LENGTH_SHORT).show();
        });
    }
}