package com.aware.plugin.device_discovery;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;

public class Plugin extends Aware_Plugin {

    private Intent aware;

    private Intent NSD_SERVICE;
    private Intent COAP_SERVICE;

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::"+getResources().getString(R.string.app_name);

        /**
         * Plugins share their current status, i.e., context using this method.
         * This method is called automatically when triggering
         * {@link Aware#ACTION_AWARE_CURRENT_CONTEXT}
         **/
        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                //Broadcast your context here
            }
        };

        //Add permissions you need (Android M+).
        //By default, AWARE asks access to the #Manifest.permission.WRITE_EXTERNAL_STORAGE

        //REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        REQUIRED_PERMISSIONS.add(Manifest.permission.INTERNET);
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_NETWORK_STATE);
        REQUIRED_PERMISSIONS.add(Manifest.permission.CAMERA);


        //To sync data to the server, you'll need to set this variables from your ContentProvider
        DATABASE_TABLES = Provider.DATABASE_TABLES;
        TABLES_FIELDS = Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ Provider.DeviceDiscovery_Data.CONTENT_URI }; //this syncs dummy TableOne_Data to server

        //Initialise AWARE's service
        aware = new Intent(this, Aware.class);
        startService(aware);

        //start NSD Service
        NSD_SERVICE = new Intent(this, NSD.class);
        startService(NSD_SERVICE);

        //start COAP SERVER Service
        COAP_SERVICE = new Intent(this, COAPServer.class);
        startService(COAP_SERVICE);
    }

    //This function gets called every 5 minutes by AWARE to make sure this plugin is still running.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (PERMISSIONS_OK) {

            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

            //Initialize our plugin's settings

            //TEMPORARY BELOW
            /*Aware.setSetting(this, Aware_Preferences.STATUS_LIGHT, true);
            Aware.setSetting(this, Aware_Preferences.FREQUENCY_LIGHT, 100);
            Aware.startLight(this);*/
            //DONE

            Aware.setSetting(this, Settings.STATUS_PLUGIN_TEMPLATE, true);

        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Aware.setSetting(this, Settings.STATUS_PLUGIN_TEMPLATE, false);
        stopService(NSD_SERVICE);
        stopService(COAP_SERVICE);
        //Aware.stopLight(this);

        //Stop AWARE's service
        stopService(aware);
    }
}
