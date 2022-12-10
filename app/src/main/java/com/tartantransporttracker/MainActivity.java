package com.tartantransporttracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;

import com.tartantransporttracker.managers.RouteManager;
import com.tartantransporttracker.models.Route;
import com.tartantransporttracker.ui.route.AdminViewRoute;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.tartantransporttracker.databinding.ActivityMainBinding;
import com.tartantransporttracker.managers.UserManager;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class MainActivity extends BaseActivity<ActivityMainBinding> implements NavigationView.OnNavigationItemSelectedListener {
    private static final int RC_SIGN_IN = 123;
    private UserManager userManager = UserManager.getInstance();
    private RouteManager routeManager = RouteManager.getInstance();
    TextView userEmail;
    List<Route> routes;
    @Override
    ActivityMainBinding getViewBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        routes = routeManager.findAllRoutes();
         View hView = binding.navView.getHeaderView(0);

         userEmail = hView.findViewById(R.id.email);

         routes = routeManager.findAllRoutes();
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
                getUserData();
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
                Intent intent = new Intent(MainActivity.this, AdminViewRoute.class);
                startActivity(intent);
                break;

            case R.id.logout_button:
                userManager.signOut(this).addOnSuccessListener(aVoid ->{
                    finish();
                });
                break;
            case R.id.language_btn:
                showChangeLanguageDialog();
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

    public void showChangeLanguageDialog(){
        final String[] languages = {"French", "English"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setSingleChoiceItems(languages, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (i == 0){
                    setLocale("fr");
                    recreate();
                }else{
                    setLocale("en");
                    recreate();
                }
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void setLocale(String lang){
        Locale locale = new Locale(lang);
        locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }

    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        setLocale(language);
    }
}