package tw.peer4321.checknumber;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Peer on 2014/12/19.
 */
public class MonthLoader {

    private static final String TAG = "MonthLoader";
    private List<String> months; // TODO refactor months to ArrayAdapter
    private String host, port;
    private boolean valid;
    private MainActivity activity;
    private int spinnerId;
    
    public MonthLoader(final Fragment fragment, final int id) {
        activity = (MainActivity) fragment.getActivity();
        spinnerId = id;
        host = fragment.getString(R.string.server_ip);
        port = fragment.getString(R.string.server_port);
        this.clear();
        this.update();
    }

    public List<String> getMonths() {
        return months;
    }
    
    public void clear() {
        valid = false;
        List<String> list = new ArrayList<>();
        list.add("讀取中");
        months = list;
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
                            Spinner spinner = (Spinner) activity.findViewById(spinnerId);
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>)spinner.getAdapter();
                            adapter.clear();
                            adapter.addAll(months);
                            adapter.notifyDataSetChanged();
                        }
                    });
                    valid = true;
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                    activity.showToast("讀取月份失敗");
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Spinner spinner = (Spinner) activity.findViewById(spinnerId);
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>)spinner.getAdapter();
                            adapter.clear();
                            ArrayList<String> dummy = new ArrayList<>();
                            dummy.add("壞掉了，請下拉重新載入");
                            adapter.addAll(dummy);
                            adapter.notifyDataSetChanged();
                        }
                    });
                    valid = false;
                }
            }
        }).start();
    }

}
