package tw.peer4321.checkinvoice;

import android.os.PatternMatcher;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            number = n; memo = m;
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
                List<Record> list = new ArrayList<>();
                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();
                    Log.d(TAG, "Server: " + host + ":" + port);
                    if (host == null || port == null) throw new IOException();
                    String url = "http://" + host + ":" + port + "/browse.xml?"+
                            "u=" + user + "&y=" + year + "&m=" + month;
                    xpp.setInput(new StringReader(HttpReader.getData(url)));
                    int eventType = xpp.getEventType();
                    String name;
                    String number = "", memo = "";
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                name = xpp.getName();
                                if (name != null) switch (name) {
                                    case "record":
                                        number = memo = ""; break;
                                    case "number":
                                        number = xpp.nextText(); break;
                                    case "memo":
                                        memo = xpp.nextText(); break;
                                }
                            case XmlPullParser.END_TAG:
                                name = xpp.getName();
                                if ("record".equals(name)) {
                                    if (!number.equals("") && !memo.equals("")) {
                                        Log.d(TAG, "number = " + number + ", memo = " + memo);
                                        list.add(new Record(number, memo));
                                    }
                                }
                        }
                        eventType = xpp.next();
                    }
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }
                if (list.size() == 0)  list.add(new Record("安安", "這個月份一張發票都沒有喔"));
                records = list;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }
    
    public void delete(final String year, final String month, final String number) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String message = null;
                final String display;
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<record>")
                            .append("<user>").append(user).append("</user>")
                            .append("<year>").append(year).append("</year>")
                            .append("<month>").append(month).append("</month>")
                            .append("<number>").append(number).append("</number>")
                            .append("</record>");
                    Log.d(TAG, "Server: " + host + ":" + port);
                    if (host == null || port == null) throw new IOException();
                    String response = HttpReader.postData("http://"+host+":"+port+"/delete.xml",
                            sb.toString().getBytes());
                    if (response != null) {
                        Pattern pattern = Pattern.compile("<message>[\\w\\s]*</message>");
                        Matcher match = pattern.matcher(response);
                        if (match.find()) {
                            message = match.group().split("<message>")[1].split("</message>")[0];
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (message == null) {
                    display = "刪除失敗";
                }
                else switch (message) {
                    case "Succeed":
                        display = "刪除成功";
                        break;
                    case "Nothing":
                        display = "找不到指定項目";
                        break;
                    default:
                        display = "刪除失敗";
                }
                activity.showToast(display);
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
