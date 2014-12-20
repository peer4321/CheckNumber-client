package tw.peer4321.checknumber;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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

/**
 * Created by Peer on 2014/12/18.
 */
public class BrowseFragment extends Fragment {

    private static final String TAG = "BrowseFragment";
    private MonthLoader monthLoader;
    private ArrayAdapter<String> dataAdapter;
    private MyAdapter listAdapter;
    private Spinner monthSpinner;
    private SwipeRefreshLayout swipeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        monthLoader = new MonthLoader(this, R.id.spBrowseMonth);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_browse, container, false);
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.laySwipe);
        swipeLayout.setOnRefreshListener(new slRefreshListener());
        monthSpinner = (Spinner) v.findViewById(R.id.spBrowseMonth);
        dataAdapter = new ArrayAdapter<>(
                this.getActivity(), android.R.layout.simple_spinner_item, monthLoader.getMonths());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(dataAdapter);
        monthSpinner.setOnItemSelectedListener(new spSelectedListener());
        ListView listView = (ListView) v.findViewById(R.id.recordView);
        listAdapter = new MyAdapter(this, v);
        listView.setAdapter(listAdapter);
        return v;
    }

    private class spSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (parent.getSelectedItem() != null) {
                String str = parent.getSelectedItem().toString();
                Log.d(TAG, "Selected: " + str);
                if ("--".equals(str)) {
                    listAdapter.clear();
                    listAdapter.notifyDataSetChanged();
                    return;
                }
                else if (str.matches("\\d{2,}-\\d{4}")) {
                    Log.d(TAG, "year = " + str.split("-")[0] + ", month = " + str.split("-")[1]);
                    listAdapter.update(str.split("-")[0], str.split("-")[1]);
                    Log.d(TAG, "listAdapter has size " + listAdapter.getCount());
                    return;
                }
            }
            listAdapter.error();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private class MyAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private NumberLoader records;

        public MyAdapter(Fragment f, View v) {
            this.mInflater = LayoutInflater.from(v.getContext());
            records = new NumberLoader(f, this);
        }

        public void clear() {
            records.clear();
        }

        public void update(String year, String month) {
            records.update(year, month);
        }

        public void error() { records.error(); }
        
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
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.record_layout, parent, false);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.textViewTitle);
                holder.memo = (TextView) convertView.findViewById(R.id.textViewMemo);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.title.setText(((NumberLoader.Record) getItem(position)).getNumber());
            holder.memo.setText(((NumberLoader.Record) getItem(position)).getMemo());
            return convertView;
        }
    }

    static class ViewHolder {
        TextView title;
        TextView memo;
    }

    private class slRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            monthLoader.clear();
            monthLoader.update();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(false);
                    monthSpinner.setSelection(0);
                    //((MainActivity)getActivity()).showToast("Refreshed");
                }
            }, 3000);
        }
    }
}
