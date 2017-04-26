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

public class CoAPDiscovery extends AppCompatActivity {

    public Set DISCOVERY;
    public ArrayAdapter arrayAdapter;
    public ListView listView;
    public ArrayList<String> URL;
    public Object[] numberUrl;
    public Intent intentSend;
    public String ADDRESS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_co_apdiscovery);
        listView = (ListView) findViewById(R.id.listViewDiscovery);
        Log.d("QWERTY", "onCreate: is working");
        URL= new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, URL);
        listView.setAdapter(arrayAdapter);
        intentSend = new Intent(this, CoAPGet.class);
        Intent intent = getIntent();
        ADDRESS = intent.getStringExtra("address");
        new CoapDiscoverTask().execute(ADDRESS);
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
            DISCOVERY = client.discover();
            numberUrl = DISCOVERY.toArray();
            //InetSocketAddress bindToAddress = new InetSocketAddress("192.168.173.113", 5683);
            //client.setEndpoint(new CoapEndpoint(bindToAddress));
            return client.get();
        }

        protected void onPostExecute(CoapResponse response) {
            for(int i = 0  ; i < numberUrl.length; i++ ){
                URL.add(numberUrl[i].toString().substring(1,numberUrl[i].toString().indexOf(">")));
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
