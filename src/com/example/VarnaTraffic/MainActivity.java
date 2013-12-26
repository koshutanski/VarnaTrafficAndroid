package com.example.VarnaTraffic;

import VarnaTraffic.Helpers.AutoCompleteListItem;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
    /**
     * Called when the activity is first created.
     */
    ListView lvMostRecent;
    AutoCompleteListItem listItem;
    ArrayAdapter<AutoCompleteListItem> adapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

       final Context ctx = this;
        lvMostRecent = (ListView)findViewById(R.id.lvMostRecent);
        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autocomplete);
        Button selectBusStopButton = (Button) findViewById(R.id.selectBusStopButton);
        autoCompView.setAdapter(new BusStopsAutoCompleteAdapter(this, R.layout.list_item));
        autoCompView.setOnItemClickListener(this);
        selectBusStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(listItem != null)
                {
                    try{
                Intent intent = new Intent(MainActivity.this, BusesTableActivity.class);
                intent.putExtra("listItem", (Serializable)listItem);
                startActivity(intent);
                    }
                    catch (Exception e) {
                        Log.e("Exception", "Error connecting to other page", e);
                    }
                }
            }
        });

        List<AutoCompleteListItem> testList = new ArrayList<AutoCompleteListItem>();
        testList.add(new AutoCompleteListItem(3,"Item1"));
        testList.add(new AutoCompleteListItem(4,"Item2"));
        testList.add(new AutoCompleteListItem(88,"Item3"));

       // adapter = new ArrayAdapter<AutoCompleteListItem>(ctx,R.id.lvMostRecent,testList);
       // lvMostRecent.setAdapter(adapter);

        lvMostRecent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                AutoCompleteListItem listItem = (AutoCompleteListItem)lvMostRecent.getItemAtPosition(position);

                Toast.makeText(ctx, listItem.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        AutoCompleteListItem str = (AutoCompleteListItem) adapterView.getItemAtPosition(position);
        listItem = str;
        Toast.makeText(this, str.getText(), Toast.LENGTH_SHORT).show();
    }

}
