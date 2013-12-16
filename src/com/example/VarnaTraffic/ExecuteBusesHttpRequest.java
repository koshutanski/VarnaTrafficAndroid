package com.example.VarnaTraffic;

import VarnaTraffic.Helpers.AutoCompleteListItem;
import VarnaTraffic.Helpers.BusesLiveData;
import VarnaTraffic.Helpers.LiveDataModel;
import VarnaTraffic.Helpers.ScheduleModel;
import android.content.Context;
import android.graphics.Color;
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
public class ExecuteBusesHttpRequest extends AsyncTask<Integer, Void, BusesLiveData> {


    private Context context;
    private View rootView;

    public ExecuteBusesHttpRequest() {
    }

    public ExecuteBusesHttpRequest(Context context, View rootView) {
        this.context = context;
        this.rootView = rootView;
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
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error executing", e);
            return resultData;
        } finally {
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

            ArrayList<LiveDataModel> liveDataModelList = new ArrayList<LiveDataModel>();
            for (int i = 0; i < liveData.length(); i++) {
              LiveDataModel liveModel =  new LiveDataModel();

                liveModel.ArriveIn =  liveData.getJSONObject(i).has("arriveIn") ? liveData.getJSONObject(i).getString("arriveIn"): null;
                liveModel.ArriveTime = liveData.getJSONObject(i).has("arriveTime") ? liveData.getJSONObject(i).getString("arriveTime"): null;
                liveModel.Delay = liveData.getJSONObject(i).has("delay") ? liveData.getJSONObject(i).getString("delay"): null;
                liveModel.Device = liveData.getJSONObject(i).has("device") ? liveData.getJSONObject(i).getInt("device"): null;
                liveModel.Direction = liveData.getJSONObject(i).has("direction") ? liveData.getJSONObject(i).getInt("direction"): null;
                liveModel.DistanceLeft =liveData.getJSONObject(i).has("distanceLeft") ? liveData.getJSONObject(i).getString("distanceLeft"): null;
                liveModel.Line = liveData.getJSONObject(i).has("line") ? liveData.getJSONObject(i).getString("line"): null;
                liveModel.Position = null;
                liveModel.State =liveData.getJSONObject(i).has("state") ? liveData.getJSONObject(i).getInt("state"): null;

                liveDataModelList.add(     liveModel  )  ;
                         }
            HashMap<String, ArrayList<ScheduleModel>> busesSchedule = new HashMap<String, ArrayList<ScheduleModel>>();
            Iterator<String> iter = schedule.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                try {
                    ArrayList<ScheduleModel> scheduleModuleList = new ArrayList<ScheduleModel>();
                    JSONArray scheduleForSingleBus = schedule.getJSONArray(key);
                    for (int i = 0; i < scheduleForSingleBus.length(); i++) {
                        scheduleModuleList.add(new ScheduleModel(scheduleForSingleBus.getJSONObject(i).getString("destStationTime"), scheduleForSingleBus.getJSONObject(i).getString("device"), scheduleForSingleBus.getJSONObject(i).getString("text")));
                    }
                    busesSchedule.put(key, scheduleModuleList);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error executing", e);
                    return resultData;
                    // Something went wrong!
                }
            }

            // Extract the Place descriptions from the results
            resultData = new BusesLiveData();

            resultData.LiveData = liveDataModelList;
            resultData.ScheduleData = busesSchedule;


        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultData;

    }

    @Override
    protected void onPostExecute(BusesLiveData result) {
        TextView arriveTimeTextView;
        TextView arriveDelay;
        TextView arriveInTextView;
        TextView distanceTextView;
        TextView busLine;
        TableRow rowBusLine;
        //TableLayout.LayoutParams llp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        TableRow.LayoutParams llp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        llp.setMargins(5, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
        TableLayout table = (TableLayout) rootView.findViewById(R.id.busesTable);
        Integer tableChildCount = table.getChildCount();
        if (tableChildCount > 1) {
            int tableRowNumber = 1;
            Boolean isValidRow = true;
            while (isValidRow) {
                View tableRowToRemove = table.getChildAt(tableRowNumber);
                if (tableRowToRemove instanceof TableRow) {
                    ((TableRow) tableRowToRemove).removeAllViews();
                    table.removeView(tableRowToRemove);
                } else {
                    isValidRow = false;
                }


            }
        }
        int rowCounter = 0;
        int rowChildCounter = 0;
        for (LiveDataModel liveData : result.LiveData) {
            rowBusLine = new TableRow(context);
            rowBusLine.setId(1000 + rowCounter);

            busLine = new TextView(context);
            busLine.setId(1001 + rowChildCounter);
            busLine.setText(liveData.Line);
            busLine.setTypeface(Typeface.SERIF, Typeface.BOLD);
            busLine.setLayoutParams(llp);
            //     busLine.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

            arriveTimeTextView = new TextView(context);
            arriveTimeTextView.setText(liveData.ArriveTime);
            arriveTimeTextView.setId(1002 + rowChildCounter);
            arriveTimeTextView.setLayoutParams(llp);
            arriveTimeTextView.setTextSize(16);
          //  arriveTimeTextView.setTextColor(Color.GREEN);


            arriveDelay = new TextView(context);
            arriveDelay.setText(liveData.Delay);
            arriveDelay.setId(1003 + rowChildCounter);
            arriveDelay.setLayoutParams(llp);
            if(liveData.Delay != null && liveData.Delay.startsWith("-"))
            {
                arriveDelay.setTextColor(Color.GREEN);
            }
            else
            {
                arriveDelay.setTextColor(Color.RED);
            }


            arriveInTextView = new TextView(context);
            arriveInTextView.setText(liveData.ArriveIn );
            arriveInTextView.setId(1004 + rowChildCounter);
            arriveInTextView.setLayoutParams(llp);


            TableRow.LayoutParams distanceLayout = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            distanceLayout.setMargins(20, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
            distanceTextView = new TextView(context);
            distanceTextView.setText(liveData.DistanceLeft);
            distanceTextView.setId(1005 + rowChildCounter);
            distanceTextView.setLayoutParams(distanceLayout);

            rowBusLine.addView(busLine);
            rowBusLine.addView(arriveTimeTextView);
            rowBusLine.addView(arriveInTextView);
            rowBusLine.addView(arriveDelay);
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
