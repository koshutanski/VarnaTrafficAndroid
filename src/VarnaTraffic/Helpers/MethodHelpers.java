package VarnaTraffic.Helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


public class MethodHelpers {

    public static Boolean isInternetConnectionAvailable(Context context) {
        Boolean isConnected = false;
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return isConnected;
    }
}
