package com.aware.plugin.device_discovery;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

public class CoapObserve extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coap_observe);
        Intent intent = getIntent();
        String URL = intent.getStringExtra("URL");
        ((TextView)findViewById(R.id.textCodeName)).setText("Loading...");
        ((TextView)findViewById(R.id.textContent)).setText("");
        CoapClient client = new CoapClient(URL);
        client.observe(new CoapHandler() {
            @Override
            public void onLoad(final CoapResponse response) {
                Log.d("HELLO", "onLoad: ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView)findViewById(R.id.textCodeName)).setText(response.getCode().name());
                        ((TextView)findViewById(R.id.textContent)).setText(response.getResponseText());

                    }
                });
            }

            @Override
            public void onError() {
                Log.d("HELLO====", "onLoad: ");

            }
        });

    }

}
