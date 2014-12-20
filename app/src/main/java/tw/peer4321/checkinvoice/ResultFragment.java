package tw.peer4321.checkinvoice;

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
public class ResultFragment extends Fragment {

    private final String TAG = "ResultFragment";
    private SwipeRefreshLayout swipeLayout;
    private MonthLoader monthLoader;
    private ArrayAdapter<String> dataAdapter;
    private Spinner monthSpinner;
    private MyBaseAdapter listAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_result, container, false);
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swResult);
        swipeLayout.setOnRefreshListener(new slRefreshListener());
        dataAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner = (Spinner) v.findViewById(R.id.spResultMonth);
        monthSpinner.setAdapter(dataAdapter);
        monthSpinner.setOnItemSelectedListener(new spSelectedListener());
        monthLoader = new MonthLoader(this, dataAdapter);
        ListView listView = (ListView) v.findViewById(R.id.resultView);
        listAdapter = new MyBaseAdapter(this, v);
        listView.setAdapter(listAdapter);
        return v;
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

    private class spSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (parent.getSelectedItem() != null) {
                String str = parent.getSelectedItem().toString();
                Log.d(TAG, "Selected: " + str);
                if ("--".equals(str) || "讀取中...".equals(str)) {
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
            listAdapter.notifyDataSetChanged();
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private class MyBaseAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private ResultLoader results;

        public MyBaseAdapter(Fragment f, View v) {
            this.mInflater = LayoutInflater.from(v.getContext());
            results = new ResultLoader(f, this);
        }

        public void clear() {
            results.clear();
        }

        public void update(String year, String month) {
            results.update(year, month);
        }

        public void error() {
            results.error();
        }

        @Override
        public int getCount() {
            return results.getResults().size();
        }

        @Override
        public Object getItem(int position) {
            return results.getResults().get(position);
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
            holder.title.setText(((ResultLoader.Result) getItem(position)).getTitle());
            holder.memo.setText(((ResultLoader.Result) getItem(position)).getText());
            return convertView;
        }
    }

    static class ViewHolder {
        TextView title;
        TextView memo;
    }
}
