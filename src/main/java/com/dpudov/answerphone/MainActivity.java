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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dpudov.answerphone.fragments.CheckFriendsFragment;
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
import static com.dpudov.answerphone.R.id.nav_checkFriend;
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
import static com.vk.sdk.VKSdk.isLoggedIn;
import static com.vk.sdk.VKSdk.login;
import static com.vk.sdk.VKSdk.wakeUpSession;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    SendFragment sendFragment;
    MainFragment mainFragment;
    SettingsFragment settingsFragment;
    CheckFriendsFragment checkFriendsFragment;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            if (!isLoggedIn())
                login(this, NOTIFICATIONS, MESSAGES, FRIENDS, WALL, ADS, GROUPS, STATUS);
            else
                wakeUpSession(this);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Что-то пошло не так. Проверьте соединение и попробуйте позже", Toast.LENGTH_LONG).show();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        button = (Button) findViewById(R.id.button);
        button.setVisibility(View.INVISIBLE);
        button.setClickable(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(container, checkFriendsFragment);
                ft.commit();
            }
        });
        NavigationView navigationView = (NavigationView) findViewById(nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Toast.makeText(getApplicationContext(), R.string.loginSuccess, Toast.LENGTH_SHORT).show();
                FragmentTransaction fTrans = getFragmentManager().beginTransaction();
                fTrans.replace(container, mainFragment);
                fTrans.commit();
// Пользователь успешно авторизовался
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), R.string.VK_Err, Toast.LENGTH_LONG).show();
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
            if (!settingsFragment.isVisible()) {
                button.setClickable(false);
                button.setVisibility(View.INVISIBLE);
            }
        } else if (id == nav_settings) {
            fTransaction.replace(container, settingsFragment);
            if (settingsFragment.isVisible()) {
                button.setClickable(true);
                button.setVisibility(View.VISIBLE);
            }
        } else if (id == nav_share) {
            shareWithVK();
            if (!settingsFragment.isVisible()) {
                button.setClickable(false);
                button.setVisibility(View.INVISIBLE);
            }
        } else if (id == nav_send) {
            fTransaction.replace(container, sendFragment);
            if (!settingsFragment.isVisible()) {
                button.setClickable(false);
                button.setVisibility(View.INVISIBLE);
            }
        } else if (id == nav_checkFriend) {
            fTransaction.replace(container, checkFriendsFragment);
            if (!settingsFragment.isVisible()) {
                button.setClickable(false);
                button.setVisibility(View.INVISIBLE);
            }
        }
        fTransaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public final void shareWithVK() {
        VKSdk.wakeUpSession(this);
        new VKShareDialog()
                .setText("ЭТО НЕВЕРОЯТНО!!! Alpha-version AnswerPhone рабочая!!!!! @ отправлено с помощью AnswerPhone for VK. All rights reserved by DPudov 2016")
                .setAttachmentLink("DPudov", "https://vk.com/answerphone_dev")
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

