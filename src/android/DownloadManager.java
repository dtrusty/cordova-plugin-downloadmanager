package downloadmanager;

import android.content.Context;
import android.content.*;
import android.net.Uri;
import android.os.Environment;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.webkit.CookieManager;
import android.widget.Toast;
import android.database.Cursor;

/**
 * This class echoes a string called from JavaScript.
 */
public class DownloadManager extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("download")) {
            String message = args.getString(0);
            String filename = args.getString(1);
            String downloadingMessage = args.getString(2);
            String downloadedMessage = args.getString(3);
            this.startDownload(message, filename, downloadingMessage, downloadedMessage, callbackContext);
            return true;
        }
        return false;
    }

    private void startDownload(String message, String filename, String downloadingMessage, final String downloadedMessage,
                               CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {

            final android.app.DownloadManager downloadManager = (android.app.DownloadManager) cordova.getActivity().getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
            Uri Download_Uri = Uri.parse(message);
            android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(Download_Uri);

            //add any cookies
            String cookies = CookieManager.getInstance().getCookie(message);
            if(cookies != null){
                request.addRequestHeader("cookie", cookies);
            }
            
            //Restrict the types of networks over which this download may proceed.
            request.setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI | android.app.DownloadManager.Request.NETWORK_MOBILE);
            //Set whether this download may proceed over a roaming connection.
            request.setAllowedOverRoaming(false);
            //Set the title of this download, to be displayed in notifications (if enabled).
            request.setTitle(filename);
            //Set a description of this download, to be displayed in notifications (if enabled)
            request.setDescription("DataSync File Download.");
            //Set the local destination for the downloaded file to a path within the application's external files directory            
            //request.setDestinationInExternalFilesDir(cordova.getActivity().getApplicationContext(), Environment.DIRECTORY_DOWNLOADS, filename);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

            //Set visiblity after download is complete
            request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            final long downloadReference = downloadManager.enqueue(request);

            Toast.makeText(cordova.getActivity().getApplicationContext(), downloadingMessage,
                    Toast.LENGTH_LONG).show();

            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                        long downloadId = intent.getLongExtra(
                                android.app.DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                        android.app.DownloadManager.Query query = new android.app.DownloadManager.Query();
                        query.setFilterById(downloadReference);
                        Cursor c = downloadManager.query(query);
                        if (c.moveToFirst()) {
                            int columnIndex = c
                                    .getColumnIndex(android.app.DownloadManager.COLUMN_STATUS);
                            if (android.app.DownloadManager.STATUS_SUCCESSFUL == c
                                    .getInt(columnIndex)) {

                                Toast.makeText(context, downloadedMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            };

            cordova.getActivity().getApplicationContext().registerReceiver(receiver, new IntentFilter(
                    android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE));

            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}