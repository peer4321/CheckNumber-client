package tw.peer4321.checknumber;

import android.content.Context;
import android.util.Log;

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
    private static String host, port;

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

    public NumberLoader (Context context) {
        records = new ArrayList<>();
        records.add(new Record("安安", "請先在上面選擇月份"));
        host = context.getString(R.string.server_ip);
        port = context.getString(R.string.server_port);
    }

    public NumberLoader(final String user, final String year, final String month) {
        records = new ArrayList<>();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Record> list = new ArrayList<>();
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();
                    Log.d(TAG, "Server: " + host + ":" + port);
                    if (host == null || port == null) throw new IOException();
                    xpp.setInput(new StringReader(
                            HttpReader.getData("http://" + host + ":" + port + "/browse.xml?"+
                            "u="+user+"&y="+year+"&m="+month)));
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
                            Log.d(TAG, "number = "+number+", memo = "+memo);
                            if (number != null && memo != null)
                                list.add(new Record(number, memo));
                        }
                        eventType = xpp.next();
                    }
                    if (list.size() == 0)  list.add(new Record("安安", "這個月份一張發票都沒有喔"));
                    records.clear();
                    records = list;
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
