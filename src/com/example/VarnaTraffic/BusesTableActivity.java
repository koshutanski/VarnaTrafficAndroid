package com.example.VarnaTraffic;

import VarnaTraffic.Helpers.AutoCompleteListItem;
import VarnaTraffic.Helpers.BusesLiveData;
import VarnaTraffic.Helpers.ScheduleModel;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: miroslav
 * Date: 9/13/13
 * Time: 5:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class BusesTableActivity extends Activity implements View.OnClickListener {
    AutoCompleteListItem listItem;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_schedule_table);

        Button refreshButton = (Button) this.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            listItem = (AutoCompleteListItem)extras.get("listItem");
        }


        Toast.makeText(this, listItem.getText(),Toast.LENGTH_SHORT).show();
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        callAsynchronousTask(this,rootView,listItem.getId());
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this, listItem.getText(),Toast.LENGTH_SHORT).show();
       View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
       new ExecuteBusesHttpRequest(this,rootView).execute(listItem.getId());

    }

    public void callAsynchronousTask(Context cntx,View rtView, Integer prm) {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        final Context context = cntx;
        final View rootView = rtView;
        final Integer param = prm;
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            ExecuteBusesHttpRequest performBackgroundTask = new ExecuteBusesHttpRequest(context,rootView);
                            // PerformBackgroundTask this class is the class that extends AsynchTask
                            performBackgroundTask.execute(param);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 5000); //execute in every 5000 ms
    }



}
