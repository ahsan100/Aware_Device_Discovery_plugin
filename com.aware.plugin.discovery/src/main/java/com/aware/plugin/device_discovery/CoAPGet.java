package com.aware.plugin.device_discovery;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

public class CoAPGet extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_co_apget);
        Intent intent = getIntent();
        String URL = intent.getStringExtra("URL");
        //Toast.makeText(this, URL,Toast.LENGTH_SHORT).show();
        Log.d("MNBV", "onCreate: "+ URL);
        new CoapGetTask().execute(URL);
    }

    class CoapGetTask extends AsyncTask<String, String, CoapResponse> {

        protected void onPreExecute() {
            ((TextView)findViewById(R.id.textCodeName)).setText("Loading...");
            ((TextView)findViewById(R.id.textContent)).setText("");
        }

        protected CoapResponse doInBackground(String... args) {
            CoapClient client = new CoapClient(args[0]);
            client.setTimeout(5000);
            Log.d("WORK", "doInBackground: " + client.getTimeout());
            //InetSocketAddress bindToAddress = new InetSocketAddress("192.168.173.113", 5683);
            //client.setEndpoint(new CoapEndpoint(bindToAddress));
            return client.get();
        }

        protected void onPostExecute(CoapResponse response) {
            if (response!=null) {
                ((TextView)findViewById(R.id.textCodeName)).setText(response.getCode().name());
                ((TextView)findViewById(R.id.textContent)).setText(response.getResponseText());
            } else {
                ((TextView)findViewById(R.id.textCodeName)).setText("No response");
            }
        }
    }
}
