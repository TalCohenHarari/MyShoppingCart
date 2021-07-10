package com.example.mymarketlist;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    NavController navController;
    DrawerLayout drawer;
    int selectedItem=R.id.menu_itemsListFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_homeFragment,
                R.id.nav_itemsListFragment,
                R.id.nav_allMyShoppingCartsFragment,
                R.id.nav_myMarketListFragment)
                .setDrawerLayout(drawer)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_homeFragment:
                if(selectedItem!=R.id.menu_homeFragment){
                    selectedItem=R.id.menu_homeFragment;
                    while (navController.popBackStack()) ;
                    navController.navigate(R.id.nav_homeFragment);
                }
                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.menu_itemsListFragment:
                if(selectedItem!=R.id.menu_itemsListFragment) {
                    selectedItem=R.id.menu_itemsListFragment;
                    navController.navigate(R.id.nav_itemsListFragment);
                }
                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.menu_myMarketListFragment:
                if(selectedItem!=R.id.menu_myMarketListFragment) {
                    selectedItem=R.id.menu_myMarketListFragment;
                    navController.navigate(R.id.nav_myMarketListFragment);
                }
                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.menu_allMyShoppingCartsFragment:
                if(selectedItem!=R.id.menu_allMyShoppingCartsFragment) {
                    selectedItem=R.id.menu_allMyShoppingCartsFragment;
                    navController.navigate(R.id.nav_allMyShoppingCartsFragment);
                }
                drawer.closeDrawer(GravityCompat.START);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}