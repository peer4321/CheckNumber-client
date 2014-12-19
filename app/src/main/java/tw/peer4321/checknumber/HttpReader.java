package tw.peer4321.checknumber;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by Peer on 2014/12/19.
 */
public class HttpReader {
    public static String getData(String urlPath) {
        try {
            Log.d("HttpReader", "getData: " + urlPath);
            HttpGet httpGet = new HttpGet(urlPath);
            HttpParams httpParams = new BasicHttpParams();
            int timeoutConnection = 10000;
            int timeoutSocket = 10000;
            HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpResponse response = httpClient.execute(httpGet);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
