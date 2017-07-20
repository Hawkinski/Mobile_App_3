package com.example.sachin.fms.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sachin.fms.R;
import com.example.sachin.fms.adapterPackage.Adapter;
import com.example.sachin.fms.dataSets.AddedData;
import com.example.sachin.fms.dataSets.MaterialData;
import com.example.sachin.fms.dataSets.XmlData;
import com.example.sachin.fms.others.WebServiceConnection;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddUsedMaterialActivity extends AppCompatActivity implements Adapter.SetOnItemClick {


    private AutoCompleteTextView textView;

    private SharedPreferences sp;
    private List<AddedData> data = new ArrayList<>();
    private List<XmlData> unitList = new ArrayList<>();

    private RecyclerView material_recycler;
    private TextView p_code;
    private EditText p_des;
    private TextView unit;
    private EditText qt;
    private String taskId, callId;
    private HashMap<String, MaterialData> mDatabyCode;
    private HashMap<String, MaterialData> mDatabyDes;
    private ProgressDialog pdialog;

    private Spinner unitSpinner;
    private List<String> unitCodeList = new ArrayList<>();

    private HashMap<String, String> unitHash = new HashMap<>();

    private List<AddedData> list = new ArrayList<>();


    private String NameSpace;
    private String Soap_Action;
    private String URL;
    private WebServiceConnection connection;
    private SoapSerializationEnvelope envelope;
    private SoapObject request;
    private HttpTransportSE transportSE;
    private Adapter material_adapter;
    private ArrayAdapter<String> adapter_3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_used_material);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar;
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Material Used");
        }

        assert toolbar != null;
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        connection = new WebServiceConnection();
        NameSpace = connection.NameSpace;
        Soap_Action = connection.Soap_Action;
        URL = connection.URL;


        pdialog = new ProgressDialog(this);


        sp = this.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        Bundle b = getIntent().getExtras();
        taskId = b.getString(getString(R.string.task_number));
        callId = sp.getString(getString(R.string.call_number), "null");


        textView = (AutoCompleteTextView) findViewById(R.id.search_view);

        TextView task_no = (TextView) findViewById(R.id.task_no);

        if (task_no != null) {
            task_no.setText(taskId);
        }


        Button btn = (Button) findViewById(R.id.add);
        Button btn2 = (Button) findViewById(R.id.save);

        p_des = (EditText) findViewById(R.id.product_des);
        p_code = (TextView) findViewById(R.id.product_code);
        qt = (EditText) findViewById(R.id.quantity);
        unitSpinner = (Spinner) findViewById(R.id.unitSpinner);


        unitCodeList.add("Select UOM");
        material_recycler = (RecyclerView) findViewById(R.id.material_recycler);


        adapter_3 = new ArrayAdapter<String>(AddUsedMaterialActivity.this, android.R.layout.simple_spinner_item, unitCodeList) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);

                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);

                return view;
            }
        };

        adapter_3.setDropDownViewResource(R.layout.spinner_item);
        unitSpinner.setAdapter(adapter_3);


        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    AddCheck();


                }
            });
        }

        if (btn2 != null) {
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    Check();

                }
            });
        }


        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/XML");
            File cert = new File(dir, "fms_materials.xml");

            InputStream is = new FileInputStream(cert);


            XmlPullParserFactory obj = XmlPullParserFactory.newInstance();
            XmlPullParser parser = obj.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

            parser.setInput(is, null);
            xmlparse xml = new xmlparse(parser);
            xml.execute();


        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }


    }


    public void AddCheck() {
        String edit = textView.getText().toString();
        String str4 = qt.getText().toString();

        boolean cancel_1 = false;

        if (TextUtils.isEmpty(edit)) {
            textView.setError("Please select the material");
            cancel_1 = true;
        }
        if (TextUtils.isEmpty(str4)) {
            qt.setError(getString(R.string.error_field_required));
            cancel_1 = true;
        }
        if (!cancel_1) {
            data = new ArrayList<AddedData>();
            data = fill();

            List<AddedData> addedDatas = new ArrayList<>();
            addedDatas.addAll(data);

            textView.setText("");
            p_code.setText("");
            p_des.setText("");
            unitSpinner.setSelection(0);
            qt.setText("");
            material_adapter = new Adapter(AddUsedMaterialActivity.this, addedDatas);
            material_adapter.SetOnDeleteClick(this);
            material_recycler.setAdapter(material_adapter);
            material_recycler.setLayoutManager(new LinearLayoutManager(AddUsedMaterialActivity.this));


        }


    }

    public void Check() {

        boolean cancel = false;
        String str = p_code.getText().toString();
        String str2 = p_des.getText().toString();
        String str3 = unitSpinner.getSelectedItem().toString();
        String str4 = qt.getText().toString();


        if (data.isEmpty()) {
            if (TextUtils.isEmpty(str)) {
                p_code.setError(getString(R.string.error_field_required));
                cancel = true;
            }
            if (TextUtils.isEmpty(str2)) {
                p_des.setError(getString(R.string.error_field_required));
                cancel = true;
            }
            if (TextUtils.isEmpty(str3)) {
                unit.setError(getString(R.string.error_field_required));
                cancel = true;
            }
            if (TextUtils.isEmpty(str4)) {
                qt.setError(getString(R.string.error_field_required));
                cancel = true;
            }

        }


        else if (!data.isEmpty()) {
            cancel = false;
        }


        if (!cancel) {
            if (list.isEmpty()) {
                Toast.makeText(AddUsedMaterialActivity.this, "Please click on Add button to add some material.", Toast.LENGTH_SHORT).show();

            } else {
                Save save = new Save();
                save.execute();
            }

        }

    }

    @Override
    public void onItemClick(View v, int position) {

        material_adapter.removeAt(position);
        list.remove(position);


    }


    public List<AddedData> fill() {


        String code = p_code.getText().toString();
        String des = p_des.getText().toString();
        String uom = unitSpinner.getSelectedItem().toString();
        String count = (qt.getText().toString());


        list.add(new AddedData(code, count, R.drawable.ic_clear_black_24dp, des, uom));

        return list;
    }

    public void printProducts(ArrayList<Table> products) {
        data = new ArrayList<>();
        mDatabyCode = new HashMap<>();
        mDatabyDes = new HashMap<>();

        ArrayList<String> code_list = new ArrayList<>();
        for (Table current : products) {
            code_list.add(current.code);
            code_list.add(current.name);


            mDatabyCode.put(current.code, new MaterialData(current.code, current.name, current.uom, "10", 5));
            mDatabyDes.put(current.name, new MaterialData(current.code, current.name, current.uom, "10", 5));


        }
        textView = (AutoCompleteTextView) findViewById(R.id.search_view);
        final ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, code_list);
        textView.setAdapter(adapter1);


        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if (mDatabyDes.containsKey(textView.getText().toString())) {
                    p_code.setText(mDatabyDes.get(textView.getText().toString()).product_code);
                    p_des.setText(mDatabyDes.get(textView.getText().toString()).product_des);

                    GetUnitList getUnitList = new GetUnitList(mDatabyDes.get(textView.getText().toString()).product_code);
                    getUnitList.execute();

                    int item = adapter_3.getPosition(mDatabyDes.get(textView.getText().toString()).uom);

                    unitSpinner.setSelection(item);

                } else if (mDatabyCode.containsKey(textView.getText().toString())) {
                    p_code.setText(mDatabyCode.get(textView.getText().toString()).product_code);
                    p_des.setText(mDatabyCode.get(textView.getText().toString()).product_des);
                    unit.setText(mDatabyCode.get(textView.getText().toString()).uom);


                    GetUnitList getUnitList = new GetUnitList(mDatabyCode.get(textView.getText().toString()).product_code);
                    getUnitList.execute();
                    int item = adapter_3.getPosition(mDatabyCode.get(textView.getText().toString()).uom);
                    unitSpinner.setSelection(item);

                } else if (textView.getText().toString().isEmpty()) {
                    p_code.setText("");
                    p_des.setText("");
                    unitCodeList = new ArrayList<String>();
                    unitCodeList.add("Select UOM");
                    adapter_3 = new ArrayAdapter<String>(AddUsedMaterialActivity.this, android.R.layout.simple_spinner_item, unitCodeList) {
                        @Override
                        public boolean isEnabled(int position) {
                            return position != 0;
                        }

                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            View view = super.getDropDownView(position, convertView, parent);

                            TextView tv = (TextView) view;
                            tv.setTextColor(Color.BLACK);

                            return view;
                        }
                    };

                    adapter_3.setDropDownViewResource(R.layout.spinner_item);
                    unitSpinner.setAdapter(adapter_3);
                    unitSpinner.setSelection(0);


                }

            }
        });


    }

    public boolean parseUnit(String xml) throws XmlPullParserException, IOException {

        ArrayList<XmlData> products = null;
        XmlData current = null;
        boolean isSuccess = false;
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();

        InputStream is = new ByteArrayInputStream(xml.getBytes());

        parser.setInput(is, "UTF-8");

        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = null;
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    products = new ArrayList<>();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equals("Table")) {
                        current = new XmlData();
                    } else if (current != null) {
                        switch (name) {
                            case "CODE":
                                current.code = parser.nextText();

                                break;
                            case "DESCRIPTION":
                                current.description = parser.nextText();

                                break;

                        }


                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("Table") && current != null) {
                        if (products != null) {
                            products.add(current);
                        }
                    }
            }
            eventType = parser.next();
            isSuccess = printUnit(products);

        }


        return isSuccess;
    }

    public boolean printUnit(ArrayList<XmlData> xmlList) {

        unitList = new ArrayList<>();
        boolean isSuccess = false;
        for (XmlData current : xmlList) {

            unitList.add(new XmlData(current.code, current.description));
            isSuccess = true;


        }


        return isSuccess;
    }

    public class GetUnitList extends AsyncTask<String, String, List<XmlData>> {


        String z = "";


        String code = "";
        boolean isSuccess = false;

        public GetUnitList(String code) {
            this.code = code;
        }

        public void onPreExecute() {
            pdialog.show();
        }

        public void onPostExecute(List<XmlData> s) {

            pdialog.dismiss();

            if (isSuccess) {
                unitCodeList.clear();
                for (int i = 0; i < unitList.size(); i++) {

                    unitCodeList.add(unitList.get(i).code);
                    unitHash.put(unitList.get(i).code, unitList.get(i).code);

                }

                adapter_3 = new ArrayAdapter<String>(AddUsedMaterialActivity.this, android.R.layout.simple_spinner_item, unitCodeList) {
                    @Override
                    public boolean isEnabled(int position) {
                        return position != 0;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);

                        TextView tv = (TextView) view;
                        tv.setTextColor(Color.BLACK);

                        return view;
                    }
                };

                adapter_3.setDropDownViewResource(R.layout.spinner_item);
                unitSpinner.setAdapter(adapter_3);

            }

        }


        @Override
        protected List<XmlData> doInBackground(String... params) {


            PropertyInfo c, compcode;

            request = new SoapObject(NameSpace, "getUnitList");

            compcode = new PropertyInfo();
            compcode.setName("compCode");
            compcode.setType(String.class);
            compcode.setValue(sp.getString(getString(R.string.company_code), ""));
            request.addProperty(compcode);

            c = new PropertyInfo();
            c.setName("code");
            c.setType(String.class);
            c.setValue(code);
            request.addProperty(c);


            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;


            try {

                transportSE.call(Soap_Action + "getUnitList", envelope);

                if (transportSE.responseDump != null) {
                    isSuccess = parseUnit(transportSE.responseDump);
                } else {
                    isSuccess = false;
                }

            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }

    public class Save extends AsyncTask<String, String, String> {

        List<AddedData> list = new ArrayList<>(data);
        List<String> code = new ArrayList<>();
        List<String> p_name = new ArrayList<>();
        List<String> qtlist = new ArrayList<>();
        List<String> uom = new ArrayList<>();

        boolean isSuccess = false;
        String z = "";


        public void onPreExecute() {
            pdialog.setMessage("Saving....");
            pdialog.setIndeterminate(true);
            pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pdialog.show();
            pdialog.setCancelable(false);

            if (!list.isEmpty()) {

                for (int i = 0; i < list.size(); i++) {
                    code.add(list.get(i).code);
                    qtlist.add(list.get(i).quantity);
                    uom.add(list.get(i).uom);
                    p_name.add(list.get(i).des);

                }
            } else {
                Toast.makeText(AddUsedMaterialActivity.this, "Please Add some material before requesting.", Toast.LENGTH_SHORT).show();

            }


        }

        public void onPostExecute(String r) {

            pdialog.dismiss();
            if (isSuccess && z.equalsIgnoreCase("Added")) {

                Toast.makeText(AddUsedMaterialActivity.this, "Material has been issued", Toast.LENGTH_SHORT).show();

                AddUsedMaterialActivity.this.finish();


            } else {
                Toast.makeText(AddUsedMaterialActivity.this, r, Toast.LENGTH_SHORT).show();
            }


        }

        @Override
        protected String doInBackground(String... params) {

            PropertyInfo compcode, user, task_id, loc_, doc_no, call_, codeinfo, p_nameinfo, qtlistinfo, uominfo, empcode;


            for (int i = 0; i < code.size(); i++) {
                request = new SoapObject(NameSpace, "saveUsedMaterial");
                compcode = new PropertyInfo();
                compcode.setName("compCode");
                compcode.setType(String.class);
                compcode.setValue(sp.getString(getString(R.string.company_code), ""));
                request.addProperty(compcode);


                task_id = new PropertyInfo();
                task_id.setName("task_id");
                task_id.setType(String.class);
                task_id.setValue(taskId);
                request.addProperty(task_id);


                call_ = new PropertyInfo();
                call_.setName("callNo");
                call_.setType(String.class);
                call_.setValue(callId);
                request.addProperty(call_);

                codeinfo = new PropertyInfo();
                codeinfo.setName("c");
                codeinfo.setType(String.class);
                codeinfo.setValue(code.get(i));
                request.addProperty(codeinfo);


                p_nameinfo = new PropertyInfo();
                p_nameinfo.setName("name");
                p_nameinfo.setType(String.class);
                p_nameinfo.setValue(p_name.get(i));
                request.addProperty(p_nameinfo);


                qtlistinfo = new PropertyInfo();
                qtlistinfo.setName("qt");
                qtlistinfo.setType(String.class);
                qtlistinfo.setValue(qtlist.get(i));
                request.addProperty(qtlistinfo);


                uominfo = new PropertyInfo();
                uominfo.setName("u");
                uominfo.setType(String.class);
                uominfo.setValue(uom.get(i));
                request.addProperty(uominfo);


                envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(request);
                transportSE = new HttpTransportSE(URL);
                transportSE.debug = true;


                try {

                    transportSE.call(Soap_Action + "saveUsedMaterial", envelope);
                    SoapObject result_1 = (SoapObject) envelope.getResponse();
                    //xml = result_1.toString();

                    Log.e("HHH", transportSE.responseDump);
                    if (result_1.getPropertyCount() != 0) {
                        z = result_1.getProperty(0).toString();
                        isSuccess = true;

                    } else {
                        isSuccess = false;
                    }


                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }

            }


            return null;
        }
    }

    class Table {
        public String code;
        public String name;
        public String uom;


    }

    public class xmlparse extends AsyncTask<String, String, String> {
        ArrayList<Table> products = null;
        int eventType;
        Table current = null;
        XmlPullParser parser;

        public xmlparse(XmlPullParser parser) {
            this.parser = parser;
        }

        public void onPostExecute(String s) {

            pdialog.dismiss();
            printProducts(products);
        }

        public void onPreExecute() {
            pdialog.setMessage("Loading....");
            pdialog.setIndeterminate(true);
            pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pdialog.show();
            pdialog.setCancelable(false);

        }

        @Override
        protected String doInBackground(String... params) {

            try {
                eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String name = null;
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            products = new ArrayList<>();
                            break;
                        case XmlPullParser.START_TAG:
                            name = parser.getName();
                            if (name.equals("Table")) {
                                current = new Table();
                            } else if (current != null) {
                                switch (name) {
                                    case "CODE":
                                        current.code = parser.nextText();

                                        break;
                                    case "NAME":
                                        current.name = parser.nextText();

                                        break;
                                    case "UOM":
                                        current.uom = parser.nextText();
                                        break;
                                }
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            name = parser.getName();
                            if (name.equalsIgnoreCase("Table") && current != null) {
                                products.add(current);
                            }
                    }
                    eventType = parser.next();

                }


            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


}
