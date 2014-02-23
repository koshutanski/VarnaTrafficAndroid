package com.example.VarnaTraffic;

import VarnaTraffic.Helpers.AutoCompleteListItem;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.*;
import java.util.*;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
    /**
     * Called when the activity is first created.
     */
    private static final String LOGDEBUGTAG = "MAIN ACTIVITY DEBUG";
    ListView lvMostRecent;
    AutoCompleteListItem listItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Context ctx = this;

//           SharedPreferences sharedPrefs = this.getSharedPreferences(getString(R.string.sharedPrreferencesBusStops), Context.MODE_PRIVATE);
//           SharedPreferences.Editor editor = sharedPrefs.edit();
//           editor.clear().commit();

        lvMostRecent = (ListView) findViewById(R.id.lvMostRecent);
        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autocomplete);
        Button selectBusStopButton = (Button) findViewById(R.id.selectBusStopButton);
        autoCompView.setAdapter(new BusStopsAutoCompleteAdapter(this, R.layout.list_item));
        autoCompView.setOnItemClickListener(this);
        selectBusStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (listItem != null) {
                    try {
                        Intent intent = new Intent(MainActivity.this, BusesTableActivity.class);
                        intent.putExtra("listItem", (Serializable) listItem);
                        SaveMostRecentBusStop(listItem);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e("Exception", "Error connecting to other page", e);
                    }
                }
            }
        });
        List<AutoCompleteListItem> mostRecentStopsList = new ArrayList<AutoCompleteListItem>();
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.sharedPrreferencesBusStops), Context.MODE_PRIVATE);
        Map<String, ?> busStops = sharedPref.getAll();
        Map<Integer,String> dictBusStops = new HashMap<Integer, String>();

        for (Map.Entry<String, ?> entry : busStops.entrySet()) {
            String objectValue = entry.getValue().toString();
            int key = Integer.parseInt(entry.getKey());
            dictBusStops.put(key, objectValue);
        }
        


        AutoCompleteListItem obj = new AutoCompleteListItem();
        try {
            obj.serializeJSON(objectValue);
            Log.d(LOGDEBUGTAG, "view: obj=" + obj);
            mostRecentStopsList.add(obj);
        } catch (Exception e) {
            Log.e("Exception", "Error trying deserialize the bus stop object", e);
        }

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        ArrayAdapter<AutoCompleteListItem> adapter = new ArrayAdapter<AutoCompleteListItem>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, mostRecentStopsList);


        // Assign adapter to ListView
        lvMostRecent.setAdapter(adapter);

        lvMostRecent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                AutoCompleteListItem listItem = (AutoCompleteListItem) lvMostRecent.getItemAtPosition(position);
                //  Intent intent = new Intent(MainActivity.this, BusesTableActivity.class);
                //  intent.putExtra("listItem", (Serializable)listItem);
                SaveMostRecentBusStop(listItem);
                // startActivity(intent);
                Toast.makeText(ctx, listItem.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        AutoCompleteListItem str = (AutoCompleteListItem) adapterView.getItemAtPosition(position);
        listItem = str;
        Toast.makeText(this, str.getText(), Toast.LENGTH_SHORT).show();
    }


    private void SaveMostRecentBusStop(AutoCompleteListItem listItem) {
        try {
            String serializedObject = listItem.toJSON();

            SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.sharedPrreferencesBusStops), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();


            Map<String, ?> busStops = sharedPref.getAll();

            int numberOfBusStops = busStops.size();
            String[] values = busStops.values().toArray(new String[busStops.values().size()]);
            List<String> listValues = new ArrayList<String>(Arrays.asList(values));
            boolean isValueContained = listValues.contains(listItem.toJSON());
           // Stack<String> stackOfValues = new Stack<String>();
            // for (int i = values.length - 1; i >= 0; i--) {
//            for (int i = 0; i <= values.length - 1; i++) {
//                Log.d(LOGDEBUGTAG, "first: " + values[i] + "i=" + i);
//                stackOfValues.add(values[i]);
//            }
            if (!isValueContained) {
                if (numberOfBusStops == 5) {
                   // String popObject = stackOfValues.pop();
                    listValues.remove(values.length-1);
                }
                listValues.add(0,serializedObject);
            } else {
                listValues.remove(listItem.toJSON());
                listValues.add(0,serializedObject);
            }


            for (Map.Entry<String, ?> entry : busStops.entrySet()) {
                String keyToRemove = entry.getKey();
                editor.remove(keyToRemove);
            }
            //  for (Integer i = 0; i <= stackOfValues.size() - 1; i++) {
            for (Integer i = 0; i <= listValues.size() - 1; i++) {
                String valueToSave = listValues.get(i);
                Log.d(LOGDEBUGTAG, "final: " + valueToSave + " i= " + i);
                editor.putString(i.toString(), valueToSave);
            }
            editor.commit();
        } catch (Exception e) {
            Log.e("Exception", "Error trying to save the chosen bus stop", e);
        }
    }


}




