package tw.peer4321.checkinvoice;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
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
    private MyBaseAdapter listAdapter;
    private Spinner monthSpinner;
    private SwipeRefreshLayout swipeLayout;
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_browse, container, false);
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swBrowse);
        swipeLayout.setOnRefreshListener(new slRefreshListener());
        dataAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner = (Spinner) v.findViewById(R.id.spBrowseMonth);
        monthSpinner.setAdapter(dataAdapter);
        monthSpinner.setOnItemSelectedListener(new spSelectedListener());
        monthLoader = new MonthLoader(this, dataAdapter);
        listView = (ListView) v.findViewById(R.id.recordView);
        listAdapter = new MyBaseAdapter(this, v);
        listView.setAdapter(listAdapter);
        listView.setOnScrollListener(new lsScrollListener());
        registerForContextMenu(listView);
        return v;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        Log.d(TAG, "onCreateContextMenu");
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        NumberLoader.Record item = (NumberLoader.Record) listAdapter.getItem(info.position);
        String number = item.getNumber();
        if (number.matches("\\d{8}")) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_record, menu);
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d(TAG, "onContextItemSelected");
        MainActivity activity = (MainActivity) getActivity();
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        NumberLoader.Record record = (NumberLoader.Record) listAdapter.getItem(info.position);
        int monthPosition = monthSpinner.getSelectedItemPosition();
        String number = record.getNumber(), memo = record.getMemo();
        switch (item.getItemId()) {
            case R.id.recordEdit:
                Log.d(TAG, "edit");
                activity.setEditPage(monthPosition, number, memo);
                return true;
            case R.id.recordDelete:
                Log.d(TAG, "delete");
                // TODO: Confirm check
                String str = dataAdapter.getItem(monthPosition);
                String year = str.split("-")[0], month = str.split("-")[1];
                listAdapter.delete(year, month, number);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private class spSelectedListener implements AdapterView.OnItemSelectedListener {
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
                    listAdapter.update(str.split("-")[0], str.split("-")[1]);
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

    private class slRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            monthLoader.clear();
            monthLoader.update();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(false);
                    //monthSpinner.setSelection(0);
                    //((MainActivity)getActivity()).showToast("Refreshed");
                }
            }, 3000);
        }
    }

    private class MyBaseAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private NumberLoader records;

        public MyBaseAdapter(Fragment f, View v) {
            this.mInflater = LayoutInflater.from(v.getContext());
            records = new NumberLoader(f, this);
        }

        public void clear() {
            records.clear();
        }

        public void update(String year, String month) {
            records.update(year, month);
        }
        
        public void delete(String year, String month, String number) {
            records.delete(year, month, number);
            records.update(year, month);
        }

        public void error() {
            records.error();
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

    /**
     * Fix ListView with SwipeRefreshLayout
     * http://nlopez.io/swiperefreshlayout-with-listview-done-right/
     */
    private class lsScrollListener implements AbsListView.OnScrollListener {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int topRowVerticalPosition =
                (listView == null || listView.getChildCount() == 0) ?
                    0 : listView.getChildAt(0).getTop();
            swipeLayout.setEnabled(topRowVerticalPosition >= 0);
        }
    }
}
