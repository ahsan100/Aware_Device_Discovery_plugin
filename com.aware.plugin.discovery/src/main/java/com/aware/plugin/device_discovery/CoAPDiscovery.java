package com.aware.plugin.device_discovery;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

import java.util.ArrayList;
import java.util.Set;

public class CoapDiscovery extends AppCompatActivity {

    public Set DISCOVERY;
    public ArrayAdapter arrayAdapter;
    public ListView listView;
    public ArrayList<String> URL;
    public Object[] numberUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String ADDRESS;
        final Intent intentSend;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coap_discovery);
        listView = (ListView) findViewById(R.id.listViewDiscovery);

        URL= new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, URL);
        listView.setAdapter(arrayAdapter);
        intentSend = new Intent(this, CoapObserve.class);
        Intent intent = getIntent();
        ADDRESS = intent.getStringExtra("address");
        new CoapDiscoverTask().execute(ADDRESS.substring(1)+":5683"+"/.well-known/core");


        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Toast.makeText(getApplicationContext(),ADDRESS.substring(1) + String.valueOf(URL.get(i)), Toast.LENGTH_SHORT).show();
                        intentSend.putExtra("URL", ADDRESS.substring(1)+":5683"+String.valueOf(URL.get(i)));
                        startActivity(intentSend);
                    }
                });
    }

    class CoapDiscoverTask extends AsyncTask<String, String, CoapResponse> {

        protected void onPreExecute() {
        }

        protected CoapResponse doInBackground(String... args) {
            CoapClient client = new CoapClient(args[0]);
            Log.d("DISCOVERY", "doInBackground: "+ args[0]);
            return client.get();
        }

        protected void onPostExecute(CoapResponse response) {
            if (response!=null) {
                String[] resources = response.getResponseText().split(",");
                for(int i=0; i < resources.length; i++){
                    //Log.d("DISCOVERY", "onPostExecute: " + resources[i].substring(2,resources[i].indexOf(">")));
                    URL.add(resources[i].substring(1,resources[i].indexOf(">")));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        arrayAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }
}
