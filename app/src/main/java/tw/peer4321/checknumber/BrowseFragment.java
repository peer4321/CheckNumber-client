package tw.peer4321.checknumber;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Peer on 2014/12/18.
 */
public class BrowseFragment extends Fragment {

    private static final String TAG = "BrowseFragment";
    private MonthLoader monthLoader;
    private ArrayAdapter<String> dataAdapter;
    private MyAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        monthLoader = new MonthLoader(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_browse, container, false);
        Spinner spinner = (Spinner) v.findViewById(R.id.month);
        dataAdapter = new ArrayAdapter<>(
                this.getActivity(), android.R.layout.simple_spinner_item, monthLoader.getAllLabels());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        AdapterView.OnItemSelectedListener listener = new MyListener();
        spinner.setOnItemSelectedListener(listener);
        ListView listView = (ListView) v.findViewById(R.id.recordView);
        listAdapter = new MyAdapter(v.getContext());
        listView.setAdapter(listAdapter);
        return v;
    }

    class MyListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (parent.getSelectedItem().toString().equals("--")) {
                listAdapter.update(parent.getContext());
                listAdapter.notifyDataSetChanged();
            }
            else {
                Log.d(TAG, "Selected: " + parent.getSelectedItem().toString());
                Pattern pattern = Pattern.compile("\\d{2,}-\\d{4}");
                Matcher matcher = pattern.matcher(parent.getSelectedItem().toString());
                while (matcher.find()) {
                    String[] ss = matcher.group().split("-");
                    String year = ss[0];
                    String month = ss[1];
                    Log.d(TAG, "year = " + year + ", month = " + month);
                    listAdapter.update(parent.getContext().getString(R.string.username), year, month);
                    Log.d(TAG, "listAdapter has size " + listAdapter.getCount());
                    listAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public class MyAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private NumberLoader records;

        public MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
            records = new NumberLoader(context);
        }

        public void update(Context context) {
            records = new NumberLoader(context);
        }

        public void update(String user, String year, String month) {
            records = new NumberLoader(user, year, month);
        }

        @Override
        public int getCount() {
            return records.getRecords().size();
        }

        @Override
        public Object getItem(int position) {
            return records.getRecords().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = mInflater.inflate(R.layout.record_layout, parent, false);
            TextView tv = (TextView) convertView.findViewById(R.id.textViewTitle);
            tv.setText(((NumberLoader.Record) getItem(position)).getNumber());
            tv = (TextView) convertView.findViewById(R.id.textViewMemo);
            tv.setText(((NumberLoader.Record) getItem(position)).getMemo());
            return convertView;
        }
    }

    public void refreshSpinner() {
        dataAdapter.clear();
        dataAdapter.addAll(monthLoader.getAllLabels());
        dataAdapter.notifyDataSetChanged();
    }

}
