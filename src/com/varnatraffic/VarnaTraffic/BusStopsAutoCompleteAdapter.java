package com.varnatraffic.VarnaTraffic;

import VarnaTraffic.Helpers.AutoCompleteListItem;
import VarnaTraffic.Helpers.MethodHelpers;

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
   private LayoutInflater contexInflater;
    private Boolean isInternetConnectionAvailable = true;
    public BusStopsAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        viewResourceId = textViewResourceId;
        resourceContext = context;
        contexInflater = (LayoutInflater) resourceContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Log.e(LOG_TAG, "getView");
        View view = convertView;
        if (view == null) {
            view = contexInflater.inflate(viewResourceId, null);
        }
        if(resultList.size() > 0) {
            AutoCompleteListItem listItem = resultList.get(position);
            if (listItem != null) {
                //LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                TextView customerNameLabel = (TextView) view.findViewById(R.id.autoCompleteText);
                AutoCompleteTextView autoComplete = (AutoCompleteTextView) view.findViewById(R.id.autocomplete);
                //       customerNameLabel.inflate(resourceContext,R.id.autocomplete, parent);
                // TextView tv = (TextView) getLayoutInflater().inflate(R.layout.list_item, R.id.autocomplete);
                //  TextView customerNameLabel = (TextView)vi.inflate(R.id.autoCompleteText,null);

                if (customerNameLabel != null) {
//              Log.i(MY_DEBUG_TAG, "getView Customer Name:"+customer.getName());
                    customerNameLabel.setText(listItem.getText());
                    // customerNameLabel.setWidth(autoComplete.getWidth());
                }
            }
        }
        return view;
    }

    @Override
    public int getCount() {    Log.e(LOG_TAG, "getCount");
        try {
            return resultList.size();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }


    @Override
    public AutoCompleteListItem getItem(int index) {    Log.e(LOG_TAG, "getItem");
        if(resultList.size() > 0) {
            return resultList.get(index);
        }
        else {
        return new AutoCompleteListItem();
        }
    }

    @Override
    public Filter getFilter() {
        Log.e(LOG_TAG, "getFilter");

        try {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    Log.e(LOG_TAG, "performFiltering");
                    FilterResults filterResults = new FilterResults();
                    try {

                        synchronized (resultList) {
                            if (constraint != null) {
                                // Retrieve the autocomplete results.
                                resultList.clear();

                                ArrayList<AutoCompleteListItem> autocompleteHttpResult = autocomplete(constraint.toString());
                                if (autocompleteHttpResult != null) {
                                    resultList.addAll(autocompleteHttpResult);
                                }

                                // Assign the data to the FilterResults
                                filterResults.values = resultList;
                                filterResults.count = resultList.size();
                            }
                            return filterResults;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return filterResults;
                    }
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    Log.e(LOG_TAG, "publishREsults");
                    try {
                        if (results != null && results.count > 0) {
                            // resultList.clear();
                            // resultList.addAll((ArrayList<AutoCompleteListItem>) results.values);
                            notifyDataSetChanged();
                        } else {
                            if (!isInternetConnectionAvailable) {
                                Toast.makeText(resourceContext, R.string.noInternetConnectionMessage, Toast.LENGTH_SHORT).show();
                            }

                            notifyDataSetInvalidated();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            };
            return filter;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static final String LOG_TAG = "VarnaTrafficApp";

    private static final String VT_API_BASE = "http://varnatraffic.com/Ajax/FindStation";


    private ArrayList<AutoCompleteListItem> autocomplete(String input) {Log.e(LOG_TAG, "autocomplete");
        try {
            ArrayList<AutoCompleteListItem> resultList = null;
            if (MethodHelpers.isInternetConnectionAvailable(resourceContext)) {
                isInternetConnectionAvailable = true;
                HttpURLConnection conn = null;
                StringBuilder jsonResults = new StringBuilder();
                try {
                    StringBuilder sb = new StringBuilder(VT_API_BASE);
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
                    Log.e(LOG_TAG, "Error processing URL", e);
                    return resultList;
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error connecting to API", e);
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
                    String atZeroElementId = jArray.getJSONObject(0).getString("id");
                    if (!atZeroElementId.equals("-1")) {

                        for (int i = 0; i < jArray.length(); i++) {

                            resultList.add(new AutoCompleteListItem(jArray.getJSONObject(i).getInt("id"), jArray.getJSONObject(i).getString("text")));
                        }

                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Cannot process JSON results", e);
                }
            } else {
                isInternetConnectionAvailable = false;
            }

            Log.e(LOG_TAG, "autocomplete size: 0" + resultList.size());

            return resultList;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }
}