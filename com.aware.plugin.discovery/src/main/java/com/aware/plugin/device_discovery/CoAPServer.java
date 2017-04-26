package com.aware.plugin.device_discovery;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.aware.Accelerometer;
import com.aware.Barometer;
import com.aware.Gravity;
import com.aware.Gyroscope;
import com.aware.Light;
import com.aware.Magnetometer;
import com.aware.providers.Accelerometer_Provider;
import com.aware.providers.Aware_Provider;
import com.aware.providers.Gyroscope_Provider;
import com.aware.providers.Light_Provider;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.net.URI;
import java.util.Calendar;

public class CoAPServer extends Service {

    public String action;
    public String extra;
    CoapServer server;
    String TAG = "COAP::";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.server = new CoapServer();

        server.add(new TimeResource());
        Cursor data = getContentResolver().query(Aware_Provider.Aware_Settings.CONTENT_URI,null,Aware_Provider.Aware_Settings.SETTING_VALUE + "=?", new String[]{"true"},null);
        String key;
        String sensorName;
        while (data.moveToNext()){
            int fieldNumber = 0;
            Boolean sensorCheck = true;
            key =data.getString(data.getColumnIndex(Aware_Provider.Aware_Settings.SETTING_KEY));
            if(key.contains("status")){
                sensorName = key.substring(7);
                switch (sensorName) {
                    case "accelerometer":
                        action = Accelerometer.ACTION_AWARE_ACCELEROMETER;
                        extra = Accelerometer.EXTRA_DATA;
                        fieldNumber = 1;
                        break;
                    case "light":
                        action = Light.ACTION_AWARE_LIGHT;
                        extra = Light.EXTRA_DATA;
                        fieldNumber = 2;
                        break;
                    case "gyroscope":
                        action = Gyroscope.ACTION_AWARE_GYROSCOPE;
                        extra = Gyroscope.EXTRA_DATA;
                        fieldNumber = 1;
                        break;
                    case "barometer":
                        action = Barometer.ACTION_AWARE_BAROMETER;
                        extra = Barometer.EXTRA_DATA;
                        fieldNumber = 3;
                        break;
                    case "magnetometer":
                        action = Magnetometer.ACTION_AWARE_MAGNETOMETER;
                        extra = Magnetometer.EXTRA_DATA;
                        fieldNumber = 1;
                        break;
                    case "gravity":
                        action = Gravity.ACTION_AWARE_GRAVITY;
                        extra = Gravity.EXTRA_DATA;
                        fieldNumber = 1;
                        break;
                    default:
                        sensorCheck = false;
                }
                    if(sensorCheck) {
                        server.add(new universalResource(sensorName, this, true, fieldNumber));
                    }
                }
        }
        server.start();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {

        server.destroy();
    }

    class universalResource extends CoapResource{
        private String linkName;
        private String value;
        private Context mContext;
        private int fieldNumber;

        public universalResource(String name, Context context, boolean visible, int number) {
            super(name, visible);
            this.linkName= name;
            this.mContext = context;
            this.fieldNumber = number;
            setObservable(true);
            setObserveType(CoAP.Type.CON);
            getAttributes().setObservable();
            getAttributes().setTitle(linkName);
            UniversalListener uListener = new UniversalListener();
            IntentFilter broadcastFilter = new IntentFilter();
            broadcastFilter.addAction(action);
            mContext.registerReceiver(uListener, broadcastFilter);
        }

        public class UniversalListener extends BroadcastReceiver {
            @Override
            public void onReceive(Context c, Intent intent) {
                Object sensorValue = intent.getExtras().get(extra);
                String [] details = sensorValue.toString().split(" ");
                value = details[fieldNumber];
                changed();

            }
        }

        @Override
        public void handleGET(CoapExchange exchange) {
            exchange.respond(value);
        }

        @Override
        public void handlePUT(CoapExchange exchange) {
            byte[] payload = exchange.getRequestPayload();

            try {
                value = new String(payload, "UTF-8");
                exchange.respond(CoAP.ResponseCode.CHANGED, value);
                changed();
            } catch (Exception e) {
                e.printStackTrace();
                exchange.respond("Invalid String");
            }
            super.handlePUT(exchange);
        }

    }

    public static class TimeResource extends CoapResource {
        public Handler handler = new Handler();
        String value =  "will be changed";

        public TimeResource() {
            super("time");
            setObservable(true);
            setObserveType(CoAP.Type.CON);
            getAttributes().setObservable();
            handler.postDelayed(runnable, 1100);
        }
        private Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Calendar c = Calendar.getInstance();
                int seconds = c.get(Calendar.SECOND);
                value = "TIME in Seconds : " + seconds;
                handler.postDelayed(this, 1100);
                changed();
            }
        };


        @Override
        public void handleGET(CoapExchange exchange) {
            exchange.respond(value);
        }
        @Override
        public void handlePUT(CoapExchange exchange) {
            byte[] payload = exchange.getRequestPayload();

            try {
                value = new String(payload, "UTF-8");
                exchange.respond(CoAP.ResponseCode.CHANGED, value);
                changed();
            } catch (Exception e) {
                e.printStackTrace();
                exchange.respond("Invalid String");
            }
            super.handlePUT(exchange);
        }
    }
}
