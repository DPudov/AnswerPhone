package com.dpudov.answerphone.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dpudov.answerphone.R;
import com.dpudov.answerphone.fragments.CheckFriends2Fragment;
import com.dpudov.answerphone.fragments.CheckFriendsFragment;
import com.dpudov.answerphone.fragments.MainFragment;
import com.dpudov.answerphone.fragments.SendFragment;
import com.dpudov.answerphone.fragments.SendToFriendsFragment;
import com.dpudov.answerphone.fragments.SettingsFragment;
import com.dpudov.answerphone.lists.ImageLoader;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.dialogs.VKShareDialog;
import com.vk.sdk.dialogs.VKShareDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import static com.dpudov.answerphone.R.id.container;
import static com.dpudov.answerphone.R.id.nav_help;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private int[] usersToSendNow;
    private int[] usersToSendAuto;
    private SendFragment sendFragment;
    private MainFragment mainFragment;
    private CheckFriends2Fragment checkFriends2Fragment;
    private CheckFriendsFragment checkFriendsFragment;
    private SendToFriendsFragment sendToFriendsFragment;
    private SettingsFragment settingsFragment;
    private String msg;
    private ImageView imageView;
    private TextView name;
    private static final int DIALOG_MATERIAL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
                if (prefs.getBoolean("firstrun", true)) {
                    // Do first run stuff here then set 'firstrun' as false
                    // using the following line to edit/commit prefs
                    Intent i = new Intent(MainActivity.this, MyIntro.class);
                    startActivity(i);
                    prefs.edit().putBoolean("firstrun", false).apply();
                }
            }
        });

        // Start the thread
        t.start();
        if (VKSdk.isLoggedIn())
            VKSdk.wakeUpSession(this);
        else
            VKSdk.login(this, VKScope.OFFLINE, VKScope.FRIENDS, VKScope.MESSAGES, VKScope.NOTIFICATIONS, VKScope.WALL, VKScope.PHOTOS);

        //create fragments
        mainFragment = new MainFragment();
        settingsFragment = new SettingsFragment();
        sendFragment = new SendFragment();
        sendToFriendsFragment = new SendToFriendsFragment();
        //checkFriends2Fragment = new CheckFriends2Fragment();
        //checkFriendsFragment = new CheckFriendsFragment();

        //TODO put fragments to fragment manager
        /**Bundle bundleMain = new Bundle();
         Bundle bundleSettings = new Bundle();
         Bundle bundleSend = new Bundle();
         Bundle bundleSentTo = new Bundle();
         Bundle bundleCheck = new Bundle();
         Bundle bundleCheck2 = new Bundle();
         getFragmentManager().putFragment(bundleMain, "main", mainFragment);
         getFragmentManager().putFragment(bundleSettings, "settings", settingsFragment);
         getFragmentManager().putFragment(bundleSend, "send", sendFragment);
         getFragmentManager().putFragment(bundleSentTo, "sendToFriends", sendToFriendsFragment);
         getFragmentManager().putFragment(bundleCheck, "");**/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Go to main fragment or if from notification go to settings
        android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(container, settingsFragment);
        setTitle(R.string.settFrag);
        fragmentTransaction.commit();

        // Get the drawer
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Then set user's avatar
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        imageView = (ImageView) headerView.findViewById(R.id.imageMyAva);
        name = (TextView) headerView.findViewById(R.id.name);
        VKRequest vkRequest = VKApi.users().get(VKParameters.from("fields", "photo_50"));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    JSONObject userJSON = response.json.getJSONArray("response").getJSONObject(0);
                    String first_name = userJSON.get("first_name").toString();
                    String last_name = userJSON.get("last_name").toString();
                    String photo_50 = userJSON.get("photo_50").toString();
                    ImageLoader imageLoader = new ImageLoader(getApplicationContext());
                    imageLoader.DisplayImage(photo_50, imageView, 50);
                    String mName = first_name + " " + last_name;
                    name.setText(mName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

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
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
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
        android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(container, settingsFragment);
        setTitle(R.string.settFrag);
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
        android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
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
            //fragmentTransaction.replace(container, helpFragment);
            //setTitle(R.string.help_frag_head);
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            shareWithVK(DIALOG_MATERIAL);
        } else if (id == R.id.nav_send) {
            fragmentTransaction.replace(container, sendFragment);
            setTitle(R.string.leave_recall);
        } else if (id == R.id.nav_messenger) {
            Intent intent = new Intent(this, DialogsActivity.class);
            startActivity(intent);
        }

        fragmentTransaction.commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void shareWithVK(int mode) {
        VKSdk.wakeUpSession(this);
        switch (mode) {
            case 0:
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
            case 1:
                VKShareDialogBuilder vkShareDialogBuilder = new VKShareDialogBuilder();
                vkShareDialogBuilder.setAttachmentLink("DPudov", getString(R.string.group_link));
                vkShareDialogBuilder.setText(getString(R.string.default_post));
                vkShareDialogBuilder.setShareDialogListener(new VKShareDialog.VKShareDialogListener() {
                    @Override
                    public void onVkShareComplete(int postId) {

                    }

                    @Override
                    public void onVkShareCancel() {

                    }

                    @Override
                    public void onVkShareError(VKError error) {

                    }
                });
                vkShareDialogBuilder.show(getSupportFragmentManager(), "VK_SHARE_DIALOG");
        }

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
