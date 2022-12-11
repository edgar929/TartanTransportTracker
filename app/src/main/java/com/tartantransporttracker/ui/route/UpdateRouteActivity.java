package com.tartantransporttracker.ui.route;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tartantransporttracker.R;
import com.tartantransporttracker.managers.RouteManager;
import com.tartantransporttracker.models.Route;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UpdateRouteActivity extends AppCompatActivity {

    private Button updateBtn;
    private EditText edtName;
    private RouteManager routeManager;
    private String id;
    private List<Route> routes = new ArrayList<>();
    private Route route = new Route();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_route);

        updateBtn = (Button) findViewById(R.id.btnUpdateRoute);
        edtName = findViewById(R.id.route_name);
        routeManager = new RouteManager();
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        findAllRoutes();
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtName.getText().toString();
                if (route != null) {
                    Boolean routeExists = routeExists(name);
                    if (!routeExists) {
                        route.setName(name);
                        routeManager.updateRoute(id, route);

                        edtName.setText("");
                        edtName.clearFocus();

                        Toast.makeText(getApplicationContext(), "Route Updated", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), AdminViewRoute.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Route exists!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Route not found", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public void findAllRoutes()
    {
        routeManager.findAllRoutes().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for(DocumentSnapshot doc:list){
                        Route routeFromDb = doc.toObject(Route.class);
                        routes.add(routeFromDb);
                        if(routeFromDb.getId().equals(id))
                        {
                            edtName.setText(routeFromDb.getName());
                            route = routeFromDb;
                        }
                    }
                }else{
                    Log.w("Message:","No data found in the database");
                }
            }
        });
    }

    private List<Route> getAvailableRoutes()
    {
        List<Route> allRoutes = new ArrayList<>();
        routeManager.findAllRoutes().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Route route = document.toObject(Route.class);
                                allRoutes.add(route);
                            }
                        }
                    }
                }
        );
        return allRoutes;
    }

    private boolean routeExists (String name)
    {
        List<Route> routes = getAvailableRoutes();
        Iterator<Route> iterator = routes.iterator();
        if (iterator.hasNext())
        {
            if (iterator.next().getName().equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }
}