package tw.peer4321.checkinvoice;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.BaseAdapter;

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
public class NumberLoader {

    private static final String TAG = "NumberLoader";
    private List<Record> records;
    private String user, host, port, year, month;
    private MainActivity activity;
    private BaseAdapter mAdapter;
    private boolean valid;

    public class Record {
        String number;
        String memo;
        public Record (String n, String m) {
            number = n;
            memo = m;
        }
        public String getNumber() { return number; }
        public String getMemo() { return memo; }
    }

    public List<Record> getRecords() {
        return records;
    }

    public NumberLoader (Fragment f, BaseAdapter baseAdapter) {
        activity = (MainActivity) f.getActivity();
        user = activity.getString(R.string.username);
        host = activity.getString(R.string.server_ip);
        port = activity.getString(R.string.server_port);
        mAdapter = baseAdapter;
        this.clear();
    }
    
    public void clear() {
        ArrayList<Record> list = new ArrayList<>();
        list.add(new Record("選擇月份", "請從選單中選擇月份"));
        records = list;
    }
    
    public void error() {
        ArrayList<Record> list = new ArrayList<>();
        list.add(new Record("錯誤", "請重新讀取月份"));
        records = list;
    }
    
    public void update(final String year, final String month) {
        this.year = year;
        this.month = month;
        // do some valid check?
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Record> list = new ArrayList<>();
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();
                    Log.d(TAG, "Server: " + host + ":" + port);
                    if (host == null || port == null) throw new IOException();
                    String url = "http://" + host + ":" + port + "/browse.xml?"+
                            "u=" + user + "&y=" + year + "&m=" + month;
                    xpp.setInput(new StringReader(HttpReader.getData(url)));
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("record")) {
                            String number = "", memo = "";
                            eventType = xpp.next();
                            while (!(eventType == XmlPullParser.END_TAG && xpp.getName().equals("record"))) {
                                if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("number"))
                                    number = xpp.nextText();
                                if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("memo"))
                                    memo = xpp.nextText();
                                eventType = xpp.next();
                            }
                            Log.d(TAG, "number = " + number + ", memo = " + memo);
                            if (number != null && memo != null)
                                list.add(new Record(number, memo));
                        }
                        eventType = xpp.next();
                    }
                    if (list.size() == 0)  list.add(new Record("安安", "這個月份一張發票都沒有喔"));
                    records.clear();
                    records = list;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
