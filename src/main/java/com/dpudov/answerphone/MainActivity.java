package com.dpudov.answerphone;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dpudov.answerphone.fragments.MainFragment;
import com.dpudov.answerphone.fragments.SendFragment;
import com.dpudov.answerphone.fragments.SettingsFragment;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.dialogs.VKShareDialog;

import static com.dpudov.answerphone.R.id.container;
import static com.dpudov.answerphone.R.id.drawer_layout;
import static com.dpudov.answerphone.R.id.nav_main;
import static com.dpudov.answerphone.R.id.nav_send;
import static com.dpudov.answerphone.R.id.nav_settings;
import static com.dpudov.answerphone.R.id.nav_share;
import static com.dpudov.answerphone.R.id.nav_view;
import static com.dpudov.answerphone.R.menu.main;
import static com.vk.sdk.VKScope.ADS;
import static com.vk.sdk.VKScope.FRIENDS;
import static com.vk.sdk.VKScope.GROUPS;
import static com.vk.sdk.VKScope.MESSAGES;
import static com.vk.sdk.VKScope.NOTIFICATIONS;
import static com.vk.sdk.VKScope.STATUS;
import static com.vk.sdk.VKScope.WALL;
import static com.vk.sdk.VKSdk.login;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    SendFragment sendFragment;
    MainFragment mainFragment;
    SettingsFragment settingsFragment;
    AppCompatActivity appCompatActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        login(this, NOTIFICATIONS, MESSAGES, FRIENDS, WALL, ADS, GROUPS, STATUS);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mainFragment = new MainFragment();

        settingsFragment = new SettingsFragment();


        sendFragment = new SendFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
// Пользователь успешно авторизовался
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), "Invalid", Toast.LENGTH_LONG).show();
// Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentTransaction fTransaction = getFragmentManager().beginTransaction();
        if (id == nav_main) {
            fTransaction.replace(container, mainFragment);
        } else if (id == nav_settings) {
            fTransaction.replace(container, settingsFragment);
        } else if (id == nav_share) {
            vkontaktePublish();
        } else if (id == nav_send) {
            fTransaction.replace(container, sendFragment);

        }
        fTransaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public final void vkontaktePublish() {
        VKSdk.wakeUpSession(this);
        new VKShareDialog()
                .setShareDialogListener(new VKShareDialog.VKShareDialogListener() {
                    @Override
                    public void onVkShareComplete(int postId) {

                    }

                    @Override
                    public void onVkShareCancel() {

                    }

                    @Override
                    public void onVkShareError(VKError error) {

                    }
                }).show(getSupportFragmentManager(), "VK_SHARE_DIALOG");
    }
}

