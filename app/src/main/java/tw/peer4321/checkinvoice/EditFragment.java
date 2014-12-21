package tw.peer4321.checkinvoice;

import android.content.res.Configuration;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Created by Peer on 2014/12/18.
 */
public class EditFragment extends Fragment {

    private static final String TAG = "EditFragment";
    private MonthLoader monthLoader; // TODO: Merge MonthLoader with BrowseFragment?
    private ArrayAdapter<String> dataAdapter;
    private SwipeRefreshLayout swipeLayout;

    Spinner monthSpinner;
    EditText numberText;
    EditText memoText;
    private String user;
    private String month;
    private String number;
    private String memo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit, container, false);
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swEdit);
        swipeLayout.setOnRefreshListener(new slRefreshListener());
        dataAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner = (Spinner) v.findViewById(R.id.spEditMonth);
        monthSpinner.setAdapter(dataAdapter);
        monthSpinner.setOnItemSelectedListener(new spSelectedListener());
        monthLoader = new MonthLoader(this, dataAdapter);
        numberText = (EditText) v.findViewById(R.id.etEditNumber);
        numberText.setRawInputType(Configuration.KEYBOARD_12KEY);
        memoText = (EditText) v.findViewById(R.id.etEditMemo);
        Button button = (Button) v.findViewById(R.id.btEditReset);
        button.setOnClickListener(new btResetListener());
        button = (Button) v.findViewById(R.id.btEditSubmit);
        button.setOnClickListener(new btSubmitListener());
        return v;
    }

    private class spSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (parent.getSelectedItem() != null) {
                month = parent.getSelectedItem().toString();
                Log.d(TAG, "Month is: " + month);
            }
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
                    monthSpinner.setSelection(0);
                    //((MainActivity)getActivity()).showToast("Refreshed");
                }
            }, 3000);
        }
    }

    private class btResetListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // TODO: Confirm action
            resetView();
            Toast.makeText(v.getContext(), "所有欄位已被重設", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetView() {
        month = null;
        number = null;
        memo = null;
        monthSpinner.setSelection(0);
        numberText.setText(null);
        memoText.setText(null);
    }

    private class btSubmitListener implements View.OnClickListener {
        private String host;
        private String port;
        
        @Override
        public void onClick(View v) {
            // TODO: Confirm action
            if (month == null || !month.matches("\\d{2,}-\\d{4}")) {
                Toast.makeText(v.getContext(), "請選擇有效月份", Toast.LENGTH_SHORT).show();
                return;
            }
            number = numberText.getText().toString();
            if (number == null || number.length() < 8) {
                Toast.makeText(v.getContext(), "請輸入有效發票號碼", Toast.LENGTH_SHORT).show();
                return;
            }
            memo = memoText.getText().toString();
            user = v.getContext().getString(R.string.username);
            host = v.getContext().getString(R.string.server_ip);
            port = v.getContext().getString(R.string.server_port);
            buildXmlAndSubmit();
            /*
            Toast.makeText(v.getContext(), "發票號碼已送出", Toast.LENGTH_SHORT).show();
            Toast.makeText(v.getContext(), "使用者："+user, Toast.LENGTH_SHORT).show();
            Toast.makeText(v.getContext(), "發票月份："+month, Toast.LENGTH_SHORT).show();
            Toast.makeText(v.getContext(), "發票號碼："+number, Toast.LENGTH_SHORT).show();
            Toast.makeText(v.getContext(), "備註："+memo, Toast.LENGTH_SHORT).show();
            */
            //resetView();
        }

        private void buildXmlAndSubmit() {
            final StringBuilder sb = new StringBuilder();
            sb.append("<record>")
                    .append("<user>").append(user).append("</user>")
                    .append("<year>").append(month.split("-")[0]).append("</year>")
                    .append("<month>").append(month.split("-")[1]).append("</month>")
                    .append("<number>").append(number).append("</number>")
                    .append("<memo>").append(memo).append("</memo>")
                    .append("</record>");
            new Thread() {
                @Override
                public void run() {
                    String response = HttpReader.postData("http://" + host + ":" + port + "/submit",
                            sb.toString().getBytes());
                    if (response != null) {
                        switch (response) {
                            case "Inserted":
                                ((MainActivity) getActivity()).showToast("新增成功");
                                break;
                            case "Updated":
                                ((MainActivity) getActivity()).showToast("更新成功");
                                break;
                            default:
                                ((MainActivity) getActivity()).showToast("上傳失敗");
                                break;
                        }
                    }
                }
            }.start();
        }
    }
}
