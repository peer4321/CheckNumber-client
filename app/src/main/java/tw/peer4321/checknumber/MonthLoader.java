package tw.peer4321.checknumber;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peer on 2014/12/19.
 */
public class MonthLoader {

    private static final String TAG = "MonthLoader";
    private List<String> months; // TODO refactor months to ArrayAdapter
    private String host, port;
    private boolean valid;
    private MainActivity activity;
    private ArrayAdapter<String> mAdapter;
    
    public MonthLoader(final Fragment fragment, ArrayAdapter<String> arrayAdapter) {
        Log.d(TAG, "constructor");
        activity = (MainActivity) fragment.getActivity();
        host = fragment.getString(R.string.server_ip);
        port = fragment.getString(R.string.server_port);
        mAdapter = arrayAdapter;
        clear();
        update();
    }

    public void clear() {
        valid = false;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAdapter != null) {
                    mAdapter.clear();
                    mAdapter.add("讀取中...");
                    mAdapter.notifyDataSetChanged();
                }
                else Log.e(TAG, "mAdapter == null, unable to update view");
            }
        });
    }
    
    public void update() {
        if (valid && months.size() > 1) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> list = new ArrayList<>();
                    list.add("--");
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();
                    Log.d(TAG, "Server: "+host+":"+port);
                    if (host == null || port == null) throw new IOException();
                    xpp.setInput(new StringReader(
                            HttpReader.getData("http://"+host+":"+port+"/months.xml")));
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("m")) {
                            list.add(xpp.nextText());
                        }
                        eventType = xpp.next();
                    }
                    //months.clear();
                    months = list;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mAdapter != null) {
                                mAdapter.clear();
                                mAdapter.addAll(months);
                                mAdapter.notifyDataSetChanged();
                            }
                            else Log.e(TAG, "mAdapter == null, unable to update view");
                        }
                    });
                    valid = true;
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                    activity.showToast("讀取月份失敗");
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mAdapter != null) {
                                ArrayList<String> dummy = new ArrayList<>();
                                dummy.add("壞掉了，請下拉重新載入");
                                mAdapter.clear();
                                mAdapter.addAll(dummy);
                                mAdapter.notifyDataSetChanged();
                            }
                            else Log.e(TAG, "mAdapter == null, unable to update view");
                        }
                    });
                    valid = false;
                }
            }
        }).start();
    }
    
    private void sleep(int msec) {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private String randGarbage() {
        return ((int)(Math.random()*2) == 0)?"AAAAAA":"";
    }

}
