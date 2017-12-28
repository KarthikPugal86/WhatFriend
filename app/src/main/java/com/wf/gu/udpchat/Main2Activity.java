package com.wf.gu.udpchat;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.List;

import umt.Callback;
import umt.SocketWrapper;

public class Main2Activity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    ListView listView;
    Intent mServiceIntent;
    private DrawerLayout mDrawerLayout;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ArrayList<String> itemArray = new ArrayList<>();
    private ArrayList<Integer> itemid = new ArrayList<>();
    private ArrayList<String> place = new ArrayList<>();
    private ArrayList<String> pictures = new ArrayList<>();
    private ArrayAdapter<String> itemAdapter;
    private boolean isReceiverRegistered;
    private UDPService mSensorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        // Adding Toolbar to Main screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Setting ViewPager for each Tabs
        listView = (ListView) findViewById(R.id.mobile_list);



        itemArray.clear();
        itemAdapter = new ArrayAdapter<String>(this, R.layout.activity_listview, itemArray);
        listView.setAdapter(itemAdapter);
        SharedPreferences sharedPref = this.getSharedPreferences("com.wf.gu.udpchat", Context.MODE_PRIVATE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(v.getContext(), Messages.class);
                intent.putExtra("USER_NAME", itemArray.get(position));
                intent.putExtra("USER_ID", itemid.get(position));
                intent.putExtra("USER_PLACE", place.get(position));
                intent.putExtra("USER_STATUS", "Hey!");
                intent.putExtra("USER_IMAGE", pictures.get(position));
                intent.putExtra("IS_CONTACT", false);
                v.getContext().startActivity(intent);
            }
        });

        if (Static.user_id == 0) {
            Static.user_id = sharedPref.getInt("whatfriend_user_id", 0);
            Static.user_name = sharedPref.getString("whatfriend_user_name", "").replaceAll(" ", "_");
            Static.user_image = sharedPref.getString("whatfriend_user_image", "").replaceAll(" ", "_");
            Static.user_place = sharedPref.getString("whatfriend_user_place", "").replaceAll(" ", "");
        } else {
            sharedPref.edit().putInt("whatfriend_user_id", Static.user_id).apply();
            sharedPref.edit().putString("whatfriend_user_name", Static.user_name.replaceAll(" ", "_")).apply();
            sharedPref.edit().putString("whatfriend_user_image", Static.user_image.replaceAll(" ", "_")).apply();
            sharedPref.edit().putString("whatfriend_user_place", Static.user_place.replaceAll(" ", "_")).apply();
            sharedPref.edit().putBoolean("whatfriend_if_reg", true).apply();
        }
        Static.is_visible = true;
        Static.curent_view = "CHAT";
        Log.d("curent_view", Static.curent_view);

        mSensorService = new UDPService();
        mServiceIntent = new Intent(getApplicationContext(), mSensorService.getClass());

        if (!isMyServiceRunning(mSensorService.getClass())) {
            startService(mServiceIntent);
        }


        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        // Set Tabs inside Toolbar
        final TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        Static.curent_view = "CHAT";
        // Create Navigation drawer and inlfate layout
        //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        // Adding menu icon to Toolbar

        // Set behavior of Navigation drawer


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d("curent_view", "" + position);
                if (position == 0) {
                    Static.curent_view = "CHAT";
                } else if (position == 1) {
                    Static.curent_view = "ONLINE";
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });




        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.d("Token", "SET");
                } else {
                    Log.d("Token", "NOT SET");
                }
            }
        };


    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new ChatContentFragment(), "CHAT");
        adapter.addFragment(new ListContentFragment(), "Online");
        viewPager.setAdapter(adapter);

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
        if (id == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        Static.is_visible = true;
        //Static.curent_view = "CHAT";
        super.onResume();
    }

    @Override
    protected void onPause() {
        Static.is_visible = false;
        // Static.curent_view = "SIR";
        super.onPause();
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }




}
