package com.varnatraffic.VarnaTraffic;

import VarnaTraffic.Helpers.AutoCompleteListItem;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Date: 9/13/13
 */
public class BusesTableActivity extends Activity {
    AutoCompleteListItem listItem;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_schedule_table);



        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            listItem = (AutoCompleteListItem)extras.get("listItem");
        }

        Toast.makeText(this, listItem.getText(),Toast.LENGTH_SHORT).show();
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        setTitle(listItem.getText());
        callAsynchronousTask(this,rootView,listItem.getId());

    }

//    @Override
//    public void onClick(View v) {
//        Toast.makeText(this, listItem.getText(),Toast.LENGTH_SHORT).show();
//       View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
//       new ExecuteBusesHttpRequest(this,rootView).execute(listItem.getId());
//        setTitle(listItem.getText());
//    }

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
                           //  PerformBackgroundTask this class is the class that extends AsynchTask
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
