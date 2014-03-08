package com.example.VarnaTraffic;

import VarnaTraffic.Helpers.AutoCompleteListItem;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class BusStopsAutoCompleteAdapter extends ArrayAdapter<AutoCompleteListItem> implements Filterable {
    private ArrayList<AutoCompleteListItem> resultList = new ArrayList<AutoCompleteListItem>();
   private int viewResourceId;
   private Context resourceContext;
   private LayoutInflater vi;
    public BusStopsAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        viewResourceId = textViewResourceId;
        resourceContext = context;
        vi = (LayoutInflater) resourceContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = vi.inflate(viewResourceId, null);
        }
        AutoCompleteListItem listItem = resultList.get(position);
        if (listItem != null) {
            //LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            TextView customerNameLabel = (TextView) v.findViewById(R.id.autoCompleteText);
            AutoCompleteTextView autoComplete = (AutoCompleteTextView) v.findViewById(R.id.autocomplete)  ;
               //       customerNameLabel.inflate(resourceContext,R.id.autocomplete, parent);
           // TextView tv = (TextView) getLayoutInflater().inflate(R.layout.list_item, R.id.autocomplete);
          //  TextView customerNameLabel = (TextView)vi.inflate(R.id.autoCompleteText,null);

            if (customerNameLabel != null) {
//              Log.i(MY_DEBUG_TAG, "getView Customer Name:"+customer.getName());
                customerNameLabel.setText(listItem.getText());
               // customerNameLabel.setWidth(autoComplete.getWidth());
            }
        }
        return v;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }


    @Override
    public AutoCompleteListItem getItem(int index) {
        return resultList.get(index);

    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                synchronized (resultList) {
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList.clear();
                        resultList.addAll(autocomplete(constraint.toString()));

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                       // resultList.clear();
                       // resultList.addAll((ArrayList<AutoCompleteListItem>) results.values);
                        notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    private static final String LOG_TAG = "VarnaTrafficApp";

    private static final String PLACES_API_BASE = "http://varnatraffic.com/Ajax/FindStation";


    private ArrayList<AutoCompleteListItem> autocomplete(String input) {
        ArrayList<AutoCompleteListItem> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
            sb.append("?query=").append(URLEncoder.encode(input, "utf8"));

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
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONArray jArray = new JSONArray(jsonResults.toString());

//            JSONObject jsonObj = new JSONObject(jsonResults.toString());
//            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<AutoCompleteListItem>();
            String atZeroElementId =   jArray.getJSONObject(0).getString("id");
            if(!atZeroElementId.equals("-1"))
            {

            for (int i = 0; i < jArray.length(); i++) {

                resultList.add(new AutoCompleteListItem(jArray.getJSONObject(i).getInt("id"), jArray.getJSONObject(i).getString("text"))) ;
            }

            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }
}