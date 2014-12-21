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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by Peer on 2014/12/18.
 */
public class AddNewFragment extends Fragment {

    private static final String TAG = "AddNewFragment";
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
        View v = inflater.inflate(R.layout.fragment_addnew, container, false);
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swAddNew);
        swipeLayout.setOnRefreshListener(new slRefreshListener());
        dataAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner = (Spinner) v.findViewById(R.id.spAddMonth);
        monthSpinner.setAdapter(dataAdapter);
        monthSpinner.setOnItemSelectedListener(new spSelectedListener());
        monthLoader = new MonthLoader(this, dataAdapter);
        numberText = (EditText) v.findViewById(R.id.etAddNumber);
        numberText.setRawInputType(Configuration.KEYBOARD_12KEY);
        memoText = (EditText) v.findViewById(R.id.etAddMemo);
        Button button = (Button) v.findViewById(R.id.btAddReset);
        button.setOnClickListener(new btResetListener());
        button = (Button) v.findViewById(R.id.btAddSubmit);
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
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                // root element
                Document doc = docBuilder.newDocument();
                Element rootElement = doc.createElement("record");
                doc.appendChild(rootElement);
                // children
                Element userElement = doc.createElement("user");
                userElement.appendChild(doc.createTextNode(user));
                Element yearElement = doc.createElement("year");
                yearElement.appendChild(doc.createTextNode(month.split("-")[0]));
                Element monthElement = doc.createElement("month");
                monthElement.appendChild(doc.createTextNode(month.split("-")[1]));
                Element numberElement = doc.createElement("number");
                numberElement.appendChild(doc.createTextNode(number));
                Element memoElement = doc.createElement("memo");
                memoElement.appendChild(doc.createTextNode(memo));
                rootElement.appendChild(userElement);
                rootElement.appendChild(yearElement);
                rootElement.appendChild(monthElement);
                rootElement.appendChild(numberElement);
                rootElement.appendChild(memoElement);
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                StringWriter writer = new StringWriter();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.transform(new DOMSource(doc), new StreamResult(writer));
                final String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
                //Log.d(TAG, output);
                new Thread() {
                    @Override
                    public void run() {
                        String response = HttpReader.postData("http://"+host+":"+port+"/submit", output.getBytes());
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

            } catch (ParserConfigurationException | TransformerException e) {
                e.printStackTrace();
            }
        }
    }
}
