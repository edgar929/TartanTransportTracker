package com.tartantransporttracker.ui.route;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tartantransporttracker.R;
import com.tartantransporttracker.managers.BusStopManager;
import com.tartantransporttracker.managers.RouteManager;
import com.tartantransporttracker.models.BusStop;
import com.tartantransporttracker.models.Route;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UpdateBusStopActivity extends AppCompatActivity {

    private ArrayList<Route> items;
    private Spinner spinner;
    private Button btnCreateBusStop;
    private EditText edt_address;
    private EditText edt_position;
    private BusStopManager busStopManager;
    private RouteManager routeManager;
    private Route selectedRoute = new Route();
    private String id;
    private BusStop busStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_bus_stop);

        busStopManager = new BusStopManager();
        routeManager = new RouteManager();

        edt_address = findViewById(R.id.edtAddress);
        edt_position = findViewById(R.id.edtPosition);
        btnCreateBusStop = findViewById(R.id.btn_add_bus_stop);
        spinner = findViewById(R.id.selectRouteSpinner);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        if (id != null)
        {
            busStop = findBusStop(id);
            if (busStop != null)
            {
                edt_address.setText(busStop.getBusStopName());
                edt_position.setText(busStop.getPosition());
            }
        }
        items = new ArrayList<>();

        setUpRoutesSpinner();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Route route = (Route) parent.getItemAtPosition(position);
                if (route != null)
                {
                    selectedRoute = route;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.e("SPINNER", "Nothing selected");
            }
        });

        btnCreateBusStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = edt_address.getText().toString();
                Integer position = Integer.parseInt(edt_position.getText().toString());

                if (address != null && position != null) {
                    if (busStop != null) {
                        busStop.setBusStopName(address);
                        busStop.setPosition(position);
                        busStop.setRoute(selectedRoute);
                        busStopManager.updateBusStop(id, busStop);
                        Toast.makeText(UpdateBusStopActivity.this, "Bus stop Updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UpdateBusStopActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UpdateBusStopActivity.this, "Invalid Inputs", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setUpRoutesSpinner()
    {
        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, items);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinner.setAdapter(adapter);

        routeManager.findAllRoutes().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Route route = document.toObject(Route.class);
                        items.add(route);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private BusStop findBusStop (String id)
    {
        BusStop busStop = null;
        List<BusStop> busStops = getAvailableBusStops();
        Iterator<BusStop> iterator = busStops.iterator();
        if (iterator.hasNext())
        {
            if (iterator.next().getId().equalsIgnoreCase(id))
            {
                busStop = iterator.next();
            }
        }
        return busStop;
    }

    private boolean busStopExists (String name)
    {
        List<BusStop> busStops = getAvailableBusStops();
        Iterator<BusStop> iterator = busStops.iterator();
        if (iterator.hasNext())
        {
            if (iterator.next().getBusStopName().equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }

    private List<BusStop> getAvailableBusStops ()
    {
        List<BusStop> allBusStops = new ArrayList<>();
        allBusStops = busStopManager.findAllBusStops();
        return allBusStops;
    }
}
