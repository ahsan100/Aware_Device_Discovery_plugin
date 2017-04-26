package com.aware.plugin.device_discovery;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.aware.Aware;
import com.aware.Aware_Preferences;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;




public class NSD extends Service {

    private static final String TAG = "NSD::";

    public NsdManager NSDMANAGER;
    public NsdManager.RegistrationListener REGISTRATIONLISTENER;
    public NsdManager.DiscoveryListener DISCOVERYLISTENER;

    public String SERVICENAME;
    public int LOCALPORT;
    public String ID;

    public ServerSocket SERVERSOCKET;

    public static ArrayList<NsdServiceInfo> NSD_DEVICES;

    public static int a = 1;





    public NSD() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        NSD_DEVICES  = new ArrayList<NsdServiceInfo>();


        ID = android.provider.Settings.Secure.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        NSDMANAGER = (NsdManager) getApplicationContext().getSystemService(Context.NSD_SERVICE);

        initializeDiscoveryListener();
        initializeRegistrationListener();
        initializeServerSocket();
        this.discoverServices();

        this.registerService(LOCALPORT);
        Toast.makeText(this, "NSD Started",Toast.LENGTH_SHORT).show();

        return super.onStartCommand(intent, flags, startId);
    }


    public void discoverServices() {
        NSDMANAGER.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, DISCOVERYLISTENER);
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setServiceName("AWARE_" + ID);
        serviceInfo.setServiceType("_http._tcp.");
        serviceInfo.setPort(port);

        NSDMANAGER.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, REGISTRATIONLISTENER);
    }

    public void initializeServerSocket() {
        try {
            SERVERSOCKET = new ServerSocket(0);
            LOCALPORT = SERVERSOCKET.getLocalPort();
        }
        catch (IOException ie){
            Log.d(TAG, "initializeServerSocket: Socket Error");
        }

    }

    public void initializeRegistrationListener() {
        REGISTRATIONLISTENER = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                SERVICENAME = NsdServiceInfo.getServiceName();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "Registration Error");
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "UnRegistration Error");
            }
        };
    }

    public void initializeDiscoveryListener() {
        DISCOVERYLISTENER = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals("_http._tcp.")) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(SERVICENAME)) {
                    Log.d(TAG, "Same machine: " + SERVICENAME);
                } else if (service.getServiceName().contains("AWARE_")){
                    NSDMANAGER.resolveService(service, new NsdManager.ResolveListener(){
                        @Override
                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                            Log.d(TAG, "Resolve failed" + errorCode);
                        }

                        @Override
                        public void onServiceResolved(NsdServiceInfo serviceInfo) {
                            Log.d(TAG, "Resolve Succeeded. " + serviceInfo);
                            if (serviceInfo.getServiceName().equals(SERVICENAME)) {
                                Log.d(TAG, "Same IP.");
                                return;
                            }
                            if (!NSD_DEVICES.contains(serviceInfo)){
                                NSD_DEVICES.add(serviceInfo);
                                ContentValues data = new ContentValues();
                                data.put(Provider.DeviceDiscovery_Data.TIMESTAMP, System.currentTimeMillis());
                                data.put(Provider.DeviceDiscovery_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                                data.put(Provider.DeviceDiscovery_Data.NAME, serviceInfo.getServiceName());
                                data.put(Provider.DeviceDiscovery_Data.HOST, serviceInfo.getHost().toString());
                                data.put(Provider.DeviceDiscovery_Data.TYPE, serviceInfo.getServiceType());
                                data.put(Provider.DeviceDiscovery_Data.PORT, serviceInfo.getPort());
                                getContentResolver().insert(Provider.DeviceDiscovery_Data.CONTENT_URI, data);


                            }

                        }
                    });
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.d(TAG, "Service lost" + service);
                String[] data = new String[1];
                data[0] = service.getServiceName();
                getContentResolver().delete(Provider.DeviceDiscovery_Data.CONTENT_URI, Provider.DeviceDiscovery_Data.NAME + " = ?" , data);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.d(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.d(TAG, "Discovery failed: Error code:" + errorCode);
                NSDMANAGER.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.d(TAG, "Discovery failed: Error code:" + errorCode);
                NSDMANAGER.stopServiceDiscovery(this);
            }
        };
    }

    @Override
    public void onDestroy() {
        NSDMANAGER.stopServiceDiscovery(DISCOVERYLISTENER);
        NSDMANAGER.unregisterService(REGISTRATIONLISTENER);
        super.onDestroy();
    }
}
