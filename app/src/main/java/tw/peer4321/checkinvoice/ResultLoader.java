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
 * Created by Peer on 2014/12/21.
 */
public class ResultLoader {

    private static final String TAG = "ResultLoader";
    private List<Result> results;
    private String user, host, port, year, month;
    private MainActivity activity;
    private BaseAdapter mAdapter;
    private boolean valid;

    public class Result {
        String title;
        String text;
        public Result (String title, String text) {
            this.title = title;
            this.text = text;
        }
        public Result (String number, String type, String ref, String len, String memo) {
            this.title = number;
            StringBuilder sb = new StringBuilder();
            switch (type) {
                case "special":
                    sb.append("特獎: ");
                    break;
                case "normal":
                    sb.append("普獎: ");
                    break;
                case "wildcard":
                    sb.append("增開六獎: ");
                    break;
                default:
                    sb.append("見鬼啦: ");
            }
            sb.append(ref).append(" (相同").append(len).append("碼)\n").append(memo);
            this.text = sb.toString();
        }
        public String getTitle() { return title; }
        public String getText() { return text; }
    }

    public List<Result> getResults() {
        return results;
    }

    public ResultLoader (Fragment f, BaseAdapter baseAdapter) {
        activity = (MainActivity) f.getActivity();
        user = activity.getString(R.string.username);
        host = activity.getString(R.string.server_ip);
        port = activity.getString(R.string.server_port);
        mAdapter = baseAdapter;
        this.clear();
    }

    public void clear() {
        ArrayList<Result> list = new ArrayList<>();
        list.add(new Result("選擇月份", "請從選單中選擇月份"));
        results = list;
    }

    public void error() {
        ArrayList<Result> list = new ArrayList<>();
        list.add(new Result("錯誤", "請重新讀取月份"));
        results = list;
    }


    public void update(final String year, final String month) {
        this.year = year;
        this.month = month;
        // do some valid check?
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Result> list = new ArrayList<>();
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();
                    Log.d(TAG, "Server: " + host + ":" + port);
                    if (host == null || port == null) throw new IOException();
                    String url = "http://" + host + ":" + port + "/result.xml?"+
                            "u=" + user + "&y=" + year + "&m=" + month;
                    xpp.setInput(new StringReader(HttpReader.getData(url)));
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            if (xpp.getName().equals("result")) {
                                Log.d(TAG, "result");
                                eventType = xpp.next();
                                while (!(eventType == XmlPullParser.END_TAG && xpp.getName().equals("result"))) {
                                    String number = "", type = "", ref = "", len = "", memo = "";
                                    if (eventType == XmlPullParser.START_TAG)
                                        type = xpp.getName();
                                    if (type.equals("special") || type.equals("normal") || type.equals("wildcard")) {
                                        Log.d(TAG, "result: " + type);
                                        while (!(eventType == XmlPullParser.END_TAG && xpp.getName().equals(type))) {
                                            if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("number"))
                                                number = xpp.nextText();
                                            else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("memo"))
                                                memo = xpp.nextText();
                                            else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("ref"))
                                                ref = xpp.nextText();
                                            else if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("length"))
                                                len = xpp.nextText();
                                            eventType = xpp.next();
                                        }
                                        Log.d(TAG, "number: " + number);
                                        Log.d(TAG, "ref: " + ref);
                                        list.add(new Result(number, type, ref, len, memo));
                                    }
                                    eventType = xpp.next();
                                }
                            }
                            else if (xpp.getName().equals("error")) {
                                String msg = xpp.nextText();
                                Log.d(TAG, "Error: "+msg);
                                switch (msg) {
                                    case "No Record":
                                        list.add(new Result(":(", "發票號碼還沒開獎啦"));
                                        break;
                                    case "Need update":
                                        list.add(new Result(":(", "資料庫好像還沒準備好"));
                                        break;
                                    default:
                                        list.add(new Result("WTF", msg));
                                }
                                break;
                            }
                        }
                        eventType = xpp.next();
                    }
                    if (list.size() == 0)  list.add(new Result(":(", "發票都沒中獎哭哭喔"));
                    results.clear();
                    results = list;
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
