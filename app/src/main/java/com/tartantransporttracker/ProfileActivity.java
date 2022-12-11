package com.tartantransporttracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tartantransporttracker.databinding.ActivityProfileBinding;
import com.tartantransporttracker.managers.RouteManager;
import com.tartantransporttracker.managers.UserManager;
import com.tartantransporttracker.models.Route;
import com.tartantransporttracker.models.User;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BaseActivity<ActivityProfileBinding> {

    private UserManager userManager = UserManager.getInstance();
    private RouteManager routeManager = RouteManager.getInstance();
    private Route selectedRoute = new Route();
    List<Route> routes;
    @Override
    ActivityProfileBinding getViewBinding() {
        return ActivityProfileBinding.inflate(getLayoutInflater());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        routes = routeManager.findAllRoutes();

//        Log.e("Routes size from profile", String.valueOf(routes.size()));
        setupListeners();
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                Route item =(Route) adapterView.getSelectedItem();
                if (item != null) {
                   selectedRoute = item;
                    Toast.makeText(ProfileActivity.this, selectedRoute.getName(),
                            Toast.LENGTH_SHORT).show();
                }
//                Toast.makeText(ProfileActivity.this, "Selected",
//                        Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO Auto-generated method stub

            }
        });
        updateUIWithUserData();
    }


    private void updateUIWithUserData(){
        if(userManager.isCurrentUserLogged()){
            FirebaseUser user = userManager.getCurrentUser();

            if(user.getPhotoUrl() != null){
                setProfilePicture(user.getPhotoUrl());
            }
            setTextUserData(user);
        }
    }

    private void setProfilePicture(Uri profilePictureUrl){
        Glide.with(this)
                .load(profilePictureUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.profileImageView);
    }

    private void setTextUserData(FirebaseUser user){

        //Get email & username from User
        String email = TextUtils.isEmpty(user.getEmail()) ? "email not found": user.getEmail();
        String username = TextUtils.isEmpty(user.getDisplayName()) ? "username not found" : user.getDisplayName();

        //Update views with data
        binding.usernameEditText.setText(username);
        binding.emailTextView.setText(email);
    }


    private void setupListeners(){


        List<Route> routeFromDB = new ArrayList<>();
        ArrayAdapter<Route> routes = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,routeFromDB);
        routes.setDropDownViewResource(android.R.layout.simple_spinner_item);

        binding.spinner.setAdapter(routes);
        routeManager.findAllRoutes().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Route subject = document.toObject(Route.class);
                        routeFromDB.add(subject);
                    }
                    routes.notifyDataSetChanged();
                    binding.spinner.setSelection(0);

                }
            }
        });


        // Mentor Checkbox
        binding.isMentorCheckBox.setOnCheckedChangeListener((compoundButton, checked) -> {
            userManager.updateRole("Driver");
        });

        // Sign out button
        binding.signOutButton.setOnClickListener(view -> {
            userManager.signOut(this).addOnSuccessListener(aVoid -> {
                finish();
            });
        });

        // Delete button
        binding.deleteButton.setOnClickListener(view -> {

            new AlertDialog.Builder(this)
                    .setMessage("Delete account")
                    .setPositiveButton("Yes", (dialogInterface, i) ->
                            userManager.deleteUser(ProfileActivity.this)
                                    .addOnSuccessListener(aVoid -> {
                                                finish();
                                            }
                                    )
                    )
                    .setNegativeButton("No", null)
                    .show();

        });

        binding.updateButton.setOnClickListener(view -> {
//            userManager.updatePreferedRoute(selectedRoute);
            userManager.getUserData().addOnSuccessListener(user -> {
//                List<User> students = new ArrayList<>();
//                if(selectedRoute.getStudents() !=null) {
//                    students = selectedRoute.getStudents();
//                }
//                students.add(user);
                Log.e("Selected Routed",selectedRoute.getStudents().toString());
                selectedRoute.register(user);
                selectedRoute.notifyObservers(" =======>added new student");
                routeManager.updateRoute(selectedRoute.getId(),selectedRoute);
            });
           showSnackBar("Preferred route updated successfully");
        });
    }

    private void showSnackBar(String message){
        Snackbar.make(binding.getRoot(), message,Snackbar.LENGTH_SHORT).show();
    }

    public List<String> getRoutes(){
        List<String> newRoutes = new ArrayList<>();
        routeManager.findAllRoutes().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(DocumentSnapshot doc:task.getResult()){
                            newRoutes.add(doc.get("name").toString());
                        }


                    }
                }
        );
        return newRoutes;
    }
}