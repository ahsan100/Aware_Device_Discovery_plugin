package com.aware.plugin.device_discovery;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.nsd.NsdServiceInfo;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aware.utils.IContextCard;


public class ContextCard implements IContextCard {


    public ListView DEVICES_LISTVIEW;

    public Context mContext;

    private discoveryAdapter adapter;

    //Constructor used to instantiate this card
    public ContextCard() {}

    @Override
    public View getContextCard(Context context) {
        //Load card layout

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View card = inflater.from(context).inflate(R.layout.card, null);
        DEVICES_LISTVIEW = (ListView) card.findViewById(R.id.nsd_devices);

        ViewGroup.LayoutParams params = DEVICES_LISTVIEW.getLayoutParams();
        params.height = 500;
        DEVICES_LISTVIEW.setLayoutParams(params);
        mContext = context;

        DEVICES_LISTVIEW.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true); //allow scrolling of this view
                return false;
            }
        });

        adapter = new discoveryAdapter(context, context.getContentResolver().query(Provider.DeviceDiscovery_Data.CONTENT_URI, null, null, null, null, null),true);
        DEVICES_LISTVIEW.setAdapter(adapter);

        DEVICES_LISTVIEW.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Cursor cur = (Cursor) adapter.getItem(i);
                        Toast.makeText(mContext, cur.getString(cur.getColumnIndex(Provider.DeviceDiscovery_Data.HOST)), Toast.LENGTH_SHORT).show();
                        //Intent intent = new Intent(mContext, CoAPDiscovery.class);
                        //intent.putExtra("address", DEVICES.get(i).getHost());
                        //mContext.startActivity(intent);
                        //startActivity(intent);
                    }
                });

        //Initialize UI elements from the card

        //Set data on the UI

        //Return the card to AWARE/apps
        return card;
    }


    private class discoveryAdapter extends CursorAdapter {
        private Context mContext;

        discoveryAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            mContext = context;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.row_layout, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final TextView name = (TextView) view.findViewById(R.id.deviceName);
            final TextView host = (TextView) view.findViewById(R.id.host);
            name.setText(cursor.getString(cursor.getColumnIndex(Provider.DeviceDiscovery_Data.NAME)));
            host.setText(cursor.getString(cursor.getColumnIndex(Provider.DeviceDiscovery_Data.HOST)));
        }
    }





}
