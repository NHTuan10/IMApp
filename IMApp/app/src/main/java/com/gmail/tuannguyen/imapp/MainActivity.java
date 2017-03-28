package com.gmail.tuannguyen.imapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.tuannguyen.imapp.connection.ConnectionService;
import com.gmail.tuannguyen.imapp.contact.ContactFragment;
import com.gmail.tuannguyen.imapp.gcm.RegistrationIntentService;
import com.gmail.tuannguyen.imapp.recent.RecentFragment;
import com.gmail.tuannguyen.imapp.security.MessageArchive;
import com.gmail.tuannguyen.imapp.setting.AccountFragment;
import com.gmail.tuannguyen.imapp.setting.ItemContent;
import com.gmail.tuannguyen.imapp.setting.SettingsActivity;
import com.gmail.tuannguyen.imapp.util.ActivityTracker;
import com.gmail.tuannguyen.imapp.util.Common;
import com.gmail.tuannguyen.imapp.util.CommonUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import static com.gmail.tuannguyen.imapp.util.Common.BROADCAST_CHAT_MESSAGE;
import static com.gmail.tuannguyen.imapp.util.Common.BROADCAST_FRIEND_REQUEST;

public class MainActivity extends AppCompatActivity implements AccountFragment.OnListFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private static final int PLAY_SERVICES_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (key.equals("cloud_storage")) {
                        boolean type = prefs.getBoolean("cloud_storage",false);
                        MessageArchive.sendMessageArchiveIQ(getApplicationContext(),type);
                    }
                }
            };
    private SharedPreferences preferences;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private ConnectionService boundConnectionService;


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boundConnectionService = ((ConnectionService.LocalBinder) service).getService();
            Log.d("Connection Service", "Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundConnectionService = null;
            Log.d("Connection Service", "Connected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        doBindService();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener);
        //
        // Accounts.getAccounts(getApplicationContext());
        /*if (!isServiceRunning(ConnectionService.class)) {
            startService(new Intent(this, ConnectionService.class));
            return;
        }
        */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
       if (checkGooglePlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStart() {
        super.onStart();
        ActivityTracker.activityStarted();

        if (!ActivityTracker.lockScreenRequired || !CommonUtil.isLockScreenEnabled(getApplicationContext()))
            return;
        KeyguardManager km = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
        Intent intent = km.createConfirmDeviceCredentialIntent("IMApp Authentication", "Unlock to continue");
        startActivityForResult(intent, Common.KEYGUARD_REQ_CODE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ActivityTracker.activityStopped();
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.KEYGUARD_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Successful done the lock screen authentication");
                ActivityTracker.lockScreenRequired = false;
            }
        }

    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Reload the listView when config change (change orientation, hidden keyboard , )
        System.out.println("Config changed");
        ContactFragment.getContactAdapter().notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void doBindService() {
        bindService(new Intent(this, ConnectionService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
        Log.d("Connection Service", "Bound");
    }

    @Override
    protected void onDestroy() {
        //boundConnectionService.logOut();
        doUnbindService();
        super.onDestroy();

    }

    void doUnbindService() {
        unbindService(serviceConnection);
        Log.d("Connection Service", "UnBound");
    }

    /**
     * Check whether service is running or not
     *
     * @param serviceClass
     * @return
     */
    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Implement onClick action in List of AccountFragment
     * @param item
     */
    @Override
    public void onListFragmentInteraction(ItemContent.Item item) {
        if (Common.ITEM_LOG_OUT.equals(item.content)) {
            boundConnectionService.logOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else if (Common.ITEM_SECURITY_PRIVACY.equals(item.content)) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.

            if (position == 0) {
                RecentFragment recentFragment = RecentFragment.newInstance();
                //Listen for chat message
                recentFragment.initBroadcastReceiver();
                BroadcastReceiver br1 = RecentFragment.getBroadcastReceiver();
                registerReceiver(RecentFragment.getBroadcastReceiver()
                        , new IntentFilter(BROADCAST_CHAT_MESSAGE));
                return recentFragment;

            } else if (position == 1) {
                ContactFragment contactFragment = ContactFragment.newInstance();
                contactFragment.initBroadcastReceiver();
                BroadcastReceiver br2 = ContactFragment.getBroadcastReceiver();
                registerReceiver(ContactFragment.getBroadcastReceiver()
                        , new IntentFilter(BROADCAST_FRIEND_REQUEST));
                return contactFragment;
                //return ChatFragment.newInstance(userName, "param2");
            } else {
                return AccountFragment.newInstance(1);
            }
                /* Return a PlaceholderFragment (defined as a static inner class below).
                return PlaceholderFragment.newInstance(position + 1);
            }*/

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.chat_section_title);
                case 1:
                    return getString(R.string.contact_section_title);
                case 2:
                    return "Settings";
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }
}
