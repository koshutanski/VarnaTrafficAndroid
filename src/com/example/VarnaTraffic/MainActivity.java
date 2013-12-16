package com.example.VarnaTraffic;

import VarnaTraffic.Helpers.AutoCompleteListItem;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
    /**
     * Called when the activity is first created.
     */
    AutoCompleteListItem listItem;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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
    }
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        AutoCompleteListItem str = (AutoCompleteListItem) adapterView.getItemAtPosition(position);
        listItem = str;
        Toast.makeText(this, str.getText(), Toast.LENGTH_SHORT).show();
    }

}
