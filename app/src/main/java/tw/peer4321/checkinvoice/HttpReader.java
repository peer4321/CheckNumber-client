package tw.peer4321.checkinvoice;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by Peer on 2014/12/19.
 */
public class HttpReader {
    private final static String TAG = "HttpReader";
    public static String getData(String urlPath) {
        try {
            Log.d(TAG, "getData: " + urlPath);
            HttpGet httpGet = new HttpGet(urlPath);
            HttpParams httpParams = new BasicHttpParams();
            int timeoutConnection = 10000;
            int timeoutSocket = 10000;
            HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpResponse response = httpClient.execute(httpGet);
            if (response != null) {
                return EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public static String postData(String urlPath, byte[] data) {
        try {
            Log.d(TAG, "postData: " + urlPath);
            HttpPost httpPost = new HttpPost(urlPath);
            httpPost.setHeader("Content", "text/xml");
            httpPost.setEntity(new ByteArrayEntity(data));
            HttpParams httpParams = new BasicHttpParams();
            int timeoutConnection = 10000;
            int timeoutSocket = 10000;
            HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpResponse response = httpClient.execute(httpPost);
            if (response != null) {
                return EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
