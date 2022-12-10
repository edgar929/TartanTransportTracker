package com.tartantransporttracker.ui.route;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.tartantransporttracker.R;
import com.tartantransporttracker.managers.RouteManager;
import com.tartantransporttracker.models.Route;

public class CreateRouteActivity extends AppCompatActivity {

    Button btnAddRoute;
    Button btnViewStops;
    EditText routeName;
    RouteManager routeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_route);

        routeManager = new RouteManager();

        btnAddRoute = findViewById(R.id.createRouteBtn);
        btnViewStops = findViewById(R.id.viewStops);

        routeName = findViewById(R.id.route_name);

        btnAddRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = routeName.getText().toString();
                Route route = new Route(name);
                routeManager.createRoute(route);

                routeName.setText("");
                routeName.clearFocus();

                Toast.makeText(getApplicationContext(), "Route created", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), AdminViewRoute.class);
                startActivity(intent);
            }
        });

        btnViewStops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateBusStopActivity.class);
                startActivity(intent);
            }
        });
    }
}