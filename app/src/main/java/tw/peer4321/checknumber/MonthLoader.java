package tw.peer4321.checknumber;

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
public class MonthLoader {

    private static final String TAG = "MonthLoader";
    private List<String> months;

    public MonthLoader(final BrowseFragment fragment) {
        months = new ArrayList<>();
        months.add("讀取中");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> list = new ArrayList<>();
                    list.add("--");
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();
                    String host = fragment.getString(R.string.server_ip);
                    String port = fragment.getString(R.string.server_port);
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
                    months.clear();
                    months = list;
                    //fragment.refreshSpinner();
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

    public List<String> getAllLabels() {
        return months;
    }

}
