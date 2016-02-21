package com.dpudov.answerphone;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dpudov.answerphone.fragments.HelpFragment;
import com.dpudov.answerphone.fragments.MainFragment;
import com.dpudov.answerphone.fragments.SendFragment;
import com.dpudov.answerphone.fragments.SendToFriendsFragment;
import com.dpudov.answerphone.fragments.SettingsFragment;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.dialogs.VKShareDialog;

import static com.dpudov.answerphone.R.id.container;
import static com.dpudov.answerphone.R.id.nav_help;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private int[] usersToSendNow;
    private int[] usersToSendAuto;
    private SendFragment sendFragment;
    private MainFragment mainFragment;
    private SendToFriendsFragment sendToFriendsFragment;
    private SettingsFragment settingsFragment;
    private HelpFragment helpFragment;
    private String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainFragment = new MainFragment();
        settingsFragment = new SettingsFragment();
        sendFragment = new SendFragment();
        sendToFriendsFragment = new SendToFriendsFragment();
        helpFragment = new HelpFragment();
        if (VKSdk.isLoggedIn())
            VKSdk.wakeUpSession(this);
        else {
            while (!VKSdk.isLoggedIn())
                VKSdk.login(this, VKScope.FRIENDS, VKScope.MESSAGES, VKScope.NOTIFICATIONS, VKScope.WALL);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(container, mainFragment);
        setTitle(R.string.mainFragment);
        fragmentTransaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Toast.makeText(getApplicationContext(), R.string.loginSuccess, Toast.LENGTH_SHORT).show();
// Пользователь успешно авторизовался
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), R.string.VK_Err, Toast.LENGTH_SHORT).show();
// Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(container, settingsFragment);
        fragmentTransaction.commit();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (id == R.id.nav_main) {
            fragmentTransaction.replace(container, mainFragment);
            setTitle(R.string.mainFragment);
        } else if (id == R.id.nav_sendToFriends) {
            fragmentTransaction.replace(container, sendToFriendsFragment);
            setTitle(R.string.sendToFriends);
        } else if (id == R.id.nav_manage) {
            fragmentTransaction.replace(container, settingsFragment);
            setTitle(R.string.settFrag);
        } else if (id == nav_help) {
            fragmentTransaction.replace(container, helpFragment);
            setTitle(R.string.help_frag_head);
        } else if (id == R.id.nav_share)

        {
            shareWithVK();
        } else if (id == R.id.nav_send)

        {
            fragmentTransaction.replace(container, sendFragment);
            setTitle(R.string.sendUsMsg);
        }

        fragmentTransaction.commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void shareWithVK() {
        VKSdk.wakeUpSession(this);
        //noinspection deprecation
        new VKShareDialog()
                .setText(getString(R.string.default_post))
                .setAttachmentLink("DPudov", getString(R.string.group_link))
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

    public int[] getUsersToSendAuto() {
        return usersToSendAuto;
    }

    public void setUsersToSendAuto(int[] usersToSendAuto) {
        this.usersToSendAuto = usersToSendAuto;
    }

    public int[] getUsersToSendNow() {
        return usersToSendNow;
    }

    public void setUsersToSendNow(int[] usersToSendNow) {
        this.usersToSendNow = usersToSendNow;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
