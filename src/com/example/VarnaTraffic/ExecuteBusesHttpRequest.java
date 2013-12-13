package com.example.VarnaTraffic;

import VarnaTraffic.Helpers.AutoCompleteListItem;
import VarnaTraffic.Helpers.BusesLiveData;
import VarnaTraffic.Helpers.LiveDataModel;
import VarnaTraffic.Helpers.ScheduleModel;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: miroslav
 * Date: 9/18/13
 * Time: 11:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExecuteBusesHttpRequest extends AsyncTask<Integer,Void,BusesLiveData> {


    private Context context;
    private View rootView;

    public ExecuteBusesHttpRequest(){}
    public ExecuteBusesHttpRequest(Context context, View rootView){
        this.context=context;
        this.rootView=rootView;
    }
    private static final String LOG_TAG = "VarnaTrafficApp";
    private static final String PLACES_API_BASE = "http://varnatraffic.com/Ajax/FindStationDevices";

    @Override
    protected BusesLiveData doInBackground(Integer... params) {
        BusesLiveData resultData = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
            sb.append("?stationId=" + params[0]);

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultData;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultData;
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "Error executing", e);
            return resultData;
        }finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
           // JSONArray jArray = new JSONArray(jsonResults.toString());
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray liveData = jsonObj.getJSONArray("liveData");
            JSONObject schedule = jsonObj.getJSONObject("schedule");

            ArrayList<LiveDataModel> liveDataModelList = new ArrayList<LiveDataModel>() ;
              for (int i = 0; i < liveData.length(); i++) {
                  liveDataModelList.add(new LiveDataModel(liveData.getJSONObject(i).getString("arriveIn"), liveData.getJSONObject(i).getString("arriveTime"),liveData.getJSONObject(i).getString("delay"), liveData.getJSONObject(i).getInt("device"),
                          liveData.getJSONObject(i).getInt("direction"), liveData.getJSONObject(i).getString("distanceLeft"),liveData.getJSONObject(i).getString("line"),null ,liveData.getJSONObject(i).getInt("state"))) ;
                }
            HashMap<String,ArrayList<ScheduleModel>> busesSchedule = new HashMap<String, ArrayList<ScheduleModel>>();
            Iterator<String> iter = schedule.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                try {
                    ArrayList<ScheduleModel> scheduleModuleList = new ArrayList<ScheduleModel>();
                    JSONArray scheduleForSingleBus = schedule.getJSONArray(key);
                    for (int i = 0; i < scheduleForSingleBus.length(); i++) {
                        scheduleModuleList.add(new ScheduleModel(scheduleForSingleBus.getJSONObject(i).getString("destStationTime"), scheduleForSingleBus.getJSONObject(i).getString("device"),scheduleForSingleBus.getJSONObject(i).getString("text"))) ;
                }
                    busesSchedule.put(key,scheduleModuleList) ;
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error executing", e);
                    return resultData;
                    // Something went wrong!
                }
            }

            // Extract the Place descriptions from the results
            resultData = new BusesLiveData();

            resultData.LiveData =    liveDataModelList;
            resultData.ScheduleData =   busesSchedule;


        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultData;

    }

    @Override
    protected void onPostExecute(BusesLiveData result) {

        TableLayout table = (TableLayout) rootView.findViewById(R.id.busesTable);
        //  table.setStretchAllColumns(true);
        //  table.setShrinkAllColumns(true);
        Integer tableChildCount = table.getChildCount();
        if(tableChildCount > 1)
        {
          table.removeViews(1000, tableChildCount -1);
        }

        int rowCounter = 0;
        int rowChildCounter = 0;
        for (LiveDataModel liveData : result.LiveData)
        {
            TableRow rowBusLine = new TableRow(context);
            rowBusLine.setId(1000 + rowCounter);
            TextView busLine = new TextView(context);
            busLine.setId(1001+rowChildCounter);
            busLine.setText(liveData.Line);
            busLine.setTypeface(Typeface.SERIF, Typeface.BOLD);
//            LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) busLine.getLayoutParams();
//            llp.setMargins(0, 0, 0, 1);
//            busLine.setLayoutParams(llp);
//            busLine.setPadding(10, 10, 40, 3);

            TextView arriveTimeTextView = new TextView(context);
            arriveTimeTextView.setText(liveData.ArriveTime);
            //  arriveTimeTextView.setGravity(Gravity.CENTER_HORIZONTAL);
//            llp = (LinearLayout.LayoutParams) arriveTimeTextView.getLayoutParams();
//            llp.setMargins(0, 0, 0, 1);
//            arriveTimeTextView.setLayoutParams(llp);
//            arriveTimeTextView.setPadding(10, 10, 40, 3);
            arriveTimeTextView.setId(1002+rowChildCounter);

            TextView arriveDelay = new TextView(context);
            arriveDelay.setText(liveData.Delay);
            //  arriveDelay.setGravity(Gravity.CENTER_HORIZONTAL);
//            llp = (LinearLayout.LayoutParams) arriveDelay.getLayoutParams();
//            llp.setMargins(0, 0, 0, 1);
//            arriveDelay.setLayoutParams(llp);
//            arriveDelay.setPadding(10, 10, 40, 3);
            arriveDelay.setId(1003+rowChildCounter);


            TextView arriveInTextView = new TextView(context);
            arriveInTextView.setText(liveData.ArriveIn);
            //   arriveInTextView.setGravity(Gravity.CENTER_HORIZONTAL);
//            llp = (LinearLayout.LayoutParams) arriveInTextView.getLayoutParams();
//            llp.setMargins(0, 0, 0, 1);
//            arriveInTextView.setLayoutParams(llp);
//            arriveInTextView.setPadding(10, 10, 40, 3);
            arriveInTextView.setId(1004+rowChildCounter);

            TextView distanceTextView = new TextView(context);
            distanceTextView.setText(liveData.DistanceLeft);
            //    distanceTextView.setGravity(Gravity.CENTER_HORIZONTAL);
//            llp = (LinearLayout.LayoutParams) distanceTextView.getLayoutParams();
//            llp.setMargins(0, 0, 0, 1);
//            distanceTextView.setLayoutParams(llp);
//            distanceTextView.setPadding(10, 10, 40, 3);
            distanceTextView.setId(1005+rowChildCounter);

            rowBusLine.addView(busLine);
            rowBusLine.addView(arriveTimeTextView);
            rowBusLine.addView(arriveDelay);
            rowBusLine.addView(arriveInTextView);
            rowBusLine.addView(distanceTextView);

            table.addView(rowBusLine);
            rowChildCounter++;
            rowCounter++;


        }
        }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

}
