package com.varnatraffic.VarnaTraffic;

import VarnaTraffic.Helpers.AutoCompleteListItem;
import VarnaTraffic.Helpers.BusesLiveData;
import VarnaTraffic.Helpers.ConstantHelper;
import VarnaTraffic.Helpers.LiveDataModel;
import VarnaTraffic.Helpers.MethodHelpers;
import VarnaTraffic.Helpers.ScheduleModel;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.AndroidCharacter;
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
import java.util.Map;

/**
 * Date: 9/18/13
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
        Log.d(LOG_TAG, "Starting Async Task");
        BusesLiveData resultData = null;
        try {

            if (MethodHelpers.isInternetConnectionAvailable(context)) {

                HttpURLConnection conn = null;
                StringBuilder jsonResults = new StringBuilder();
                try {
                    StringBuilder sb = new StringBuilder(PLACES_API_BASE).append("?stationId=").append(params[0]);

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
                    e.printStackTrace();

                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error connecting to Places API", e);
                    e.printStackTrace();

                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error executing", e);
                    e.printStackTrace();

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
                        LiveDataModel liveModel = new LiveDataModel();

                        liveModel.ArriveIn = liveData.getJSONObject(i).has("arriveIn") ? liveData.getJSONObject(i).getString("arriveIn") : null;
                        liveModel.ArriveTime = liveData.getJSONObject(i).has("arriveTime") ? liveData.getJSONObject(i).getString("arriveTime") : null;
                        liveModel.Delay = liveData.getJSONObject(i).has("delay") ? liveData.getJSONObject(i).getString("delay") : null;
                        liveModel.Device = liveData.getJSONObject(i).has("device") ? liveData.getJSONObject(i).getInt("device") : null;
                        liveModel.Direction = liveData.getJSONObject(i).has("direction") ? liveData.getJSONObject(i).getInt("direction") : null;
                        liveModel.DistanceLeft = liveData.getJSONObject(i).has("distanceLeft") ? liveData.getJSONObject(i).getString("distanceLeft") : null;
                        liveModel.Line = liveData.getJSONObject(i).has("line") ? liveData.getJSONObject(i).getString("line") : null;
                        liveModel.Position = null;
                        liveModel.State = liveData.getJSONObject(i).has("state") ? liveData.getJSONObject(i).getInt("state") : null;

                        liveDataModelList.add(liveModel);
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
                            e.printStackTrace();
                        }
                    }

                    // Extract the Place descriptions from the results
                    resultData = new BusesLiveData();

                    resultData.LiveData = liveDataModelList;
                    resultData.ScheduleData = busesSchedule;
                    resultData.IsInternetConnection = true;
                    return resultData;

                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Cannot process JSON results", e);
                    e.printStackTrace();
                }

            } else {
                Log.d(LOG_TAG, "Toast no internet connection");
                resultData = new BusesLiveData();
                resultData.IsInternetConnection = false;
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
        return resultData;

    }

    @Override
    protected void onPostExecute(BusesLiveData result) {
        Resources res = context.getResources();
        if (result != null && result.IsInternetConnection) {


            Log.d(LOG_TAG, "Async PostExecute");

            TextView arriveTimeTextView;
            TextView arriveDelay;
            TextView arriveInTextView;
            TextView distanceTextView;
            TextView busLine;
            TextView emptyHeader;
            TableRow rowBusLine;

            TableRow rowBusScheduleLine;
            TextView busScheduleLine;
            TextView busScheduleTimes;
            TextView busScheduleEmptyRow;

            int dpValue = 2; // margin in dips
            float currentDensity = context.getResources().getDisplayMetrics().density;
            int leftMargin = (int) (dpValue * currentDensity); // margin in pixels

            //TableLayout.LayoutParams llp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            TableRow.LayoutParams llp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams headerNumberLayout = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams llpDelay = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            llpDelay.setMargins(leftMargin, 0, 0, 0);
            llp.setMargins(leftMargin, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
            headerNumberLayout.setMargins(leftMargin, 0, 0, 0);
            //  llp.weight = (float) 0.2;
            TextView busHeaderNumberView = (TextView) rootView.findViewById(R.id.tableHeaderNumber);
            TextView busHeaderArriveView = (TextView) rootView.findViewById(R.id.tableHeaderArrive);
            TextView busHeaderDistanceView = (TextView) rootView.findViewById(R.id.tableHeaderDistance);
            TextView busHeaderDelayView = (TextView) rootView.findViewById(R.id.tableHeaderDelay);
            TextView busHeaderLeftView = (TextView) rootView.findViewById(R.id.tableHeaderLeft);

            int busHeaderNumberWidth = busHeaderNumberView.getWidth();
            int busHeaderArriveWidth = busHeaderArriveView.getWidth();
            int busHeaderDistanceWidth = busHeaderDistanceView.getWidth();
            int busHeaderDelayWidth = busHeaderDelayView.getWidth();
            int busHeaderLeftWidth = busHeaderLeftView.getWidth();

            TableLayout table = (TableLayout) rootView.findViewById(R.id.busesTable);
            DeleteTableChildRows(table, 0);
            int rowCounter = 0;
            int rowChildCounter = 0;
            if (result.LiveData != null && !result.LiveData.isEmpty()) {
                for (LiveDataModel liveData : result.LiveData) {
                    rowBusLine = new TableRow(context);
                    rowBusLine.setId(1000 + rowCounter);
                    TableRow.LayoutParams rowBusLineLayout = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                    rowBusLine.setLayoutParams(rowBusLineLayout);

                    busLine = new TextView(context);
                    busLine.setId(1001 + rowChildCounter);
                    busLine.setText(liveData.Line);
                    busLine.setTypeface(Typeface.SERIF, Typeface.BOLD);
                    //     busLine.setBackgroundColor(Color.GREEN);
                    busLine.setLayoutParams(headerNumberLayout);
                    busLine.setWidth(busHeaderNumberWidth);
                    //     busLine.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

                    arriveTimeTextView = new TextView(context);
                    arriveTimeTextView.setText(liveData.ArriveTime);
                    arriveTimeTextView.setId(1002 + rowChildCounter);
                    //     arriveTimeTextView.setBackgroundColor(Color.BLUE);
                    arriveTimeTextView.setLayoutParams(llp);
                    arriveTimeTextView.setTextSize(16);
                    //  arriveTimeTextView.setTextColor(Color.GREEN);
                    arriveTimeTextView.setWidth(busHeaderArriveWidth);

                    arriveDelay = new TextView(context);
                    arriveDelay.setText(liveData.Delay);
                    arriveDelay.setId(1003 + rowChildCounter);
                    //   arriveDelay.setBackgroundColor(Color.CYAN);
                    arriveDelay.setLayoutParams(llpDelay);
                    if (liveData.Delay != null && liveData.Delay.startsWith("-")) {
                        arriveDelay.setTextColor(Color.GREEN);
                    } else {
                        arriveDelay.setTextColor(Color.RED);
                    }
                    arriveDelay.setWidth(busHeaderDelayWidth);

                    arriveInTextView = new TextView(context);
                    arriveInTextView.setText(liveData.ArriveIn);
                    arriveInTextView.setId(1004 + rowChildCounter);
                    //   arriveInTextView.setBackgroundColor(Color.MAGENTA);
                    arriveInTextView.setLayoutParams(llp);
                    arriveInTextView.setWidth(busHeaderLeftWidth);

                    TableRow.LayoutParams distanceLayout = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                    distanceLayout.setMargins(leftMargin, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
                    distanceTextView = new TextView(context);
                    distanceTextView.setText(liveData.DistanceLeft);
                    distanceTextView.setId(1005 + rowChildCounter);
                    //  distanceTextView.setBackgroundColor(Color.RED);
                    distanceTextView.setLayoutParams(distanceLayout);
                    distanceTextView.setWidth(busHeaderDistanceWidth);

                    rowBusLine.addView(busLine);
                    rowBusLine.addView(arriveTimeTextView);
                    rowBusLine.addView(arriveInTextView);
                    rowBusLine.addView(arriveDelay);
                    rowBusLine.addView(distanceTextView);

                    table.addView(rowBusLine);
                    rowChildCounter++;
                    rowCounter++;

                }
            } else {
                rowBusLine = new TableRow(context);
                rowBusLine.setId(1000 + rowCounter);

                emptyHeader = new TextView(context);
                emptyHeader.setId(1001 + rowChildCounter);
                emptyHeader.setText(res.getString(R.string.tableHeaderEmptyString));
                emptyHeader.setTypeface(Typeface.SERIF, Typeface.BOLD);
                emptyHeader.setLayoutParams(llp);
                rowBusLine.addView(emptyHeader);
                table.addView(rowBusLine);
            }


            TextView busHeaderScheduleLine = (TextView) rootView.findViewById(R.id.tableScheduleHeaderNumber);
            int busHeaderScheduleLineWidth = busHeaderScheduleLine.getWidth();

            TableRow.LayoutParams llpSchedulesTime = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            llpSchedulesTime.setMargins(leftMargin, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
            llpSchedulesTime.weight = (float) 0.5;


            TableRow.LayoutParams llpSchedulesLine = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            llpSchedulesLine.setMargins(leftMargin, 0, 0, 0);


            TableRow.LayoutParams rowScheduleBusLineLayout = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);

            TableLayout tableSchedule = (TableLayout) rootView.findViewById(R.id.busesScheduleTable);
            DeleteTableChildRows(tableSchedule, 0);
            int rowScheduleCounter = 0;
            int rowScheduleChildCounter = 0;
            if (result.ScheduleData != null && !result.ScheduleData.isEmpty()) {
                for (Map.Entry<String, ArrayList<ScheduleModel>> scheduleData : result.ScheduleData.entrySet()) {
                    rowBusScheduleLine = new TableRow(context);
                    rowBusScheduleLine.setId(1000 + rowScheduleCounter);

                    rowBusScheduleLine.setLayoutParams(rowScheduleBusLineLayout);


                    busScheduleLine = new TextView(context);
                    busScheduleLine.setId(1001 + rowChildCounter);
                    busScheduleLine.setText(scheduleData.getKey());
                    busScheduleLine.setTypeface(Typeface.SERIF, Typeface.BOLD);
                    // busScheduleLine.setLayoutParams(llpSchedules);
                    busScheduleLine.setLayoutParams(llpSchedulesLine);
                    //       busScheduleLine.setBackgroundColor(Color.RED);
                    busScheduleLine.setWidth(busHeaderScheduleLineWidth);

                    String busLineScheduleToDisplay = "";
                    for (ScheduleModel scheduleDetail : scheduleData.getValue()) {
                        busLineScheduleToDisplay += scheduleDetail.Text + " ";
                    }
                    busScheduleTimes = new TextView(context);
                    busScheduleTimes.setText(busLineScheduleToDisplay);
                    busScheduleTimes.setId(1002 + rowChildCounter);
                    busScheduleTimes.setLayoutParams(llpSchedulesTime);
                    //      busScheduleTimes.setBackgroundColor(Color.BLUE);

                    rowBusScheduleLine.addView(busScheduleLine);
                    rowBusScheduleLine.addView(busScheduleTimes);
                    tableSchedule.addView(rowBusScheduleLine);
                    rowScheduleCounter++;
                    rowScheduleChildCounter++;

                }
            } else {
                rowBusScheduleLine = new TableRow(context);
                rowBusScheduleLine.setId(1000 + rowCounter);

                emptyHeader = new TextView(context);
                emptyHeader.setId(1001 + rowChildCounter);
                emptyHeader.setText(res.getString(R.string.tableHeaderEmptyString));
                emptyHeader.setTypeface(Typeface.SERIF, Typeface.BOLD);
                emptyHeader.setLayoutParams(llp);
                rowBusScheduleLine.addView(emptyHeader);
                tableSchedule.addView(rowBusScheduleLine);
            }
        } else {


            TextView errorHeader;
            TableRow rowBusLine;
            TableLayout table = (TableLayout) rootView.findViewById(R.id.busesTable);
            TableLayout tableSchedule = (TableLayout) rootView.findViewById(R.id.busesScheduleTable);
            int rowChildCounter = 0;
            int rowCounter = 0;
            TableRow.LayoutParams llp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle(res.getString(R.string.MessageHeader));
            if (result != null) {


                View noInternetConnectionRow = table.findViewById(ConstantHelper.NoInternetConnectionRowId);
                if (!(noInternetConnectionRow instanceof TableRow)) {
                    DeleteTableChildRows(table,0);
                    DeleteTableChildRows(tableSchedule,0);
                    rowBusLine = new TableRow(context);
                    rowBusLine.setId(ConstantHelper.NoInternetConnectionRowId);

                    errorHeader = new TextView(context);
                    errorHeader.setId(1001 + rowChildCounter);
                    errorHeader.setText(res.getString(R.string.noInternetConnectionMessage));
                    errorHeader.setTypeface(Typeface.SERIF, Typeface.BOLD);
                    errorHeader.setLayoutParams(llp);
                    rowBusLine.addView(errorHeader);

                    table.addView(rowBusLine);

                    alertDialog.setMessage(res.getString(R.string.noInternetConnectionMessage));
                    alertDialog.setCancelable(false);
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertDialog.show();
                }
            } else {

                View noInternetConnectionRow = table.findViewById(ConstantHelper.ServerErrorRowId);
                if (!(noInternetConnectionRow instanceof TableRow)) {

                    DeleteTableChildRows(table,0);
                    DeleteTableChildRows(tableSchedule,0);
                    rowBusLine = new TableRow(context);
                    rowBusLine.setId(ConstantHelper.ServerErrorRowId);

                    errorHeader = new TextView(context);
                    errorHeader.setId(1001 + rowChildCounter);
                    errorHeader.setText(res.getString(R.string.noServerAccessMessage));
                    errorHeader.setTypeface(Typeface.SERIF, Typeface.BOLD);
                    errorHeader.setLayoutParams(llp);
                    rowBusLine.addView(errorHeader);
                    table.addView(rowBusLine);

                    alertDialog.setMessage(res.getString(R.string.noServerAccessMessage));
                    alertDialog.setCancelable(false);
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.show();
                }
            }

        }
    }

    private Integer DeleteTableChildRows(TableLayout table, int initialRows) {
        Integer rowsDeleted = 0;
        Integer tableChildCount = table.getChildCount();
        if (tableChildCount > initialRows) {
            int tableRowNumber = initialRows;
            Boolean isValidRow = true;
            while (isValidRow) {
                View tableRowToRemove = table.getChildAt(tableRowNumber);
                if (tableRowToRemove instanceof TableRow) {
                    ((TableRow) tableRowToRemove).removeAllViews();
                    table.removeView(tableRowToRemove);
                    rowsDeleted++;
                } else {
                    isValidRow = false;
                }
            }
        }
        return rowsDeleted;
    }



    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

}
