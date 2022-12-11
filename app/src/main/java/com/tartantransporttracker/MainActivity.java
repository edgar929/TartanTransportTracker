package com.tartantransporttracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tartantransporttracker.managers.BusStopManager;
import com.tartantransporttracker.managers.RouteManager;
import com.tartantransporttracker.models.BusStop;
import com.tartantransporttracker.models.Route;
import com.tartantransporttracker.ui.route.AdminViewRoute;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.tartantransporttracker.databinding.ActivityMainBinding;
import com.tartantransporttracker.managers.UserManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends BaseActivity<ActivityMainBinding> implements NavigationView.OnNavigationItemSelectedListener {
    private static final int RC_SIGN_IN = 123;
    private UserManager userManager = UserManager.getInstance();
    private RouteManager routeManager = RouteManager.getInstance();
    private BusStopManager busStopManager = BusStopManager.getInstance();
    TextView userEmail;
    List<Route> routes;
    @Override
    ActivityMainBinding getViewBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
        firebaseMessaging.subscribeToTopic("Bus_departure");
//        Log.e("====================","before");
//        routes = getRoutes();
//        Log.e("==================================","After");
         View hView = binding.navView.getHeaderView(0);

         userEmail = hView.findViewById(R.id.email);

//         List<BusStop> busStops  = busStopManager.findAllBusStops();

//         Log.e("From size",String.valueOf(routes.size()));

        setupListeners();
    }
    private void setupListeners(){
        binding.navView.setNavigationItemSelectedListener(this);
        binding.goToLogin.setOnClickListener(view -> {
            if(userManager.isCurrentUserLogged()){
                getUserData();
                startMapActivity();
            } else{
                startSignInActivity();
            }
        });
    }


    public List<Route> getRoutes(){
        List<Route> newRoutes = new ArrayList<>();
        routeManager.findAllRoutes().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        newRoutes.addAll(task.getResult().toObjects(Route.class));
                    }
                }
        );
        return newRoutes;
    }

    private void startSignInActivity(){
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers =
                Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build()
                );

        // Launch the activity
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
//                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false, true)
//                        .setLogo(R.drawable.ic_launcher_foreground)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        this.handleResponseAfterSignIn(requestCode,resultCode,data);
    }

    @Override
    public void onBackPressed() {
        if(binding.drawLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }

    }

    private void showSnackBar(String message){
        Snackbar.make(binding.drawLayout, message,Snackbar.LENGTH_SHORT).show();
    }

    public void handleResponseAfterSignIn(int requestCode,int resultCode,Intent data){
        IdpResponse response = IdpResponse.fromResultIntent(data);
        if(requestCode == RC_SIGN_IN){
            //success
            if(resultCode == RESULT_OK){
                userManager.createUser();
//                getUserData();
                showSnackBar(getString(R.string.connection_succeed));
            } else {
                //ERRORS
                if(response == null){
                    showSnackBar(getString(R.string.error_authentication_canceled));
                } else if( response.getError() !=null){
                    if(response.getError().getErrorCode() == ErrorCodes.NO_NETWORK){
                        showSnackBar(getString(R.string.error_no_internet));
                    } else if( response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR){
                        showSnackBar(getString(R.string.error_unknown_error));
                    }
                }
            }
        }
    }

    @Override
    protected  void onResume() {
        super.onResume();
        updateLoginButton();
    }

    private void getUserData(){
        userManager.getUserData().addOnSuccessListener(user->{
//            String username = TextUtils.isEmpty(user.getUsername()) ?
//                    getString(R.string.info_no_username_found) : user.getUsername();
            Log.e("In Get user function",user.getUsername());
            userEmail.setText(user.getUsername());
        });
    }

    private void startMapActivity(){
        Intent intent = new Intent(this,MapActivity.class);
        startActivity(intent);
    }

    private void updateLoginButton(){
        binding.goToLogin.setText(userManager.isCurrentUserLogged() ? getString(R.string.button_login_text_logged) :
                getString(R.string.button_login_text_not_logged));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.view_routes:
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                break;

            case R.id.logout_button:
                userManager.signOut(this).addOnSuccessListener(aVoid ->{
                    finish();
                });
                break;

            //TODO: Dear Special Ops, Any other activities can be added below for easy navigation
            //TODO: Just remember to use the switch below. Tie the ID to a menu item.
//            case R.id.scans:
//                Intent intent = new Intent(MainActivity.this,PastRecords.class);
//                startActivity(intent);
//                break;
//
//            case R.id.diseases:
//                Intent intent1 = new Intent(MainActivity.this,MaizeDiseases.class);
//                startActivity(intent1);
//                break;
        }
        binding.drawLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}