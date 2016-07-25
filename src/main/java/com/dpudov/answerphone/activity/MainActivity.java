package com.dpudov.answerphone.activity;

import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dpudov.answerphone.R;
import com.dpudov.answerphone.RegistrationIntentService;
import com.dpudov.answerphone.fragments.MainFragment;
import com.dpudov.answerphone.fragments.SendFragment;
import com.dpudov.answerphone.fragments.SendToFriendsFragment;
import com.dpudov.answerphone.fragments.SettingsFragment;
import com.dpudov.answerphone.lists.ImageLoader;
import com.dpudov.answerphone.model.CurrentUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
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

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import static com.dpudov.answerphone.R.id.container;
import static com.dpudov.answerphone.R.id.nav_help;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static MainActivity mainActivity;
    public static Boolean isVisible = false;
    private GoogleCloudMessaging gcm;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private int[] usersToSendNow;
    private int[] usersToSendAuto;
    public static final String TAG = "MainActivity";
    private SendFragment sendFragment;
    private SendToFriendsFragment sendToFriendsFragment;
    private SettingsFragment settingsFragment;
    private MainFragment mainFragment;
    private String msg;
    private ImageView imageView;
    private TextView name;
    private static final int DIALOG_MATERIAL = 1;
    public static int currentUserId;

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
        settingsFragment = new SettingsFragment();
        sendFragment = new SendFragment();
        sendToFriendsFragment = new SendToFriendsFragment();
        mainFragment = new MainFragment();
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
        if (drawer != null) {
            drawer.setDrawerListener(toggle);
        }
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
                    currentUserId = userJSON.getInt("id");
                    String first_name = userJSON.getString("first_name");
                    String last_name = userJSON.getString("last_name");
                    String photo_50 = userJSON.getString("photo_50");
                    ImageLoader imageLoader = new ImageLoader(getApplicationContext());
                    EventBus.getDefault().post(new CurrentUser(currentUserId, first_name, last_name, photo_50));
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
    protected void onStart() {
        super.onStart();
        isVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isVisible = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    public void ToastNotify(final String notificationMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, notificationMessage, Toast.LENGTH_LONG).show();

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
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if (id == R.id.nav_main) {
            fragmentTransaction.replace(container, mainFragment);
            setTitle(R.string.main_page_answer);
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
        } else if (id == R.id.nav_rate) {
            Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            }
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
            }
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
                vkShareDialogBuilder.show(getSupportFragmentManager(), "VK_SHARE_ч  DIALOG");
        }

    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported by Google Play Services.");
                ToastNotify("This device is not supported by Google Play Services.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void registerWithNotificationHubs() {
        Log.i(TAG, " Registering with Notification Hubs");

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
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

    public int getCurrentUserId() {
        return currentUserId;
    }
}
