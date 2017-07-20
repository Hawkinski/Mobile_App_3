package com.example.sachin.fms.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sachin.fms.R;
import com.example.sachin.fms.classes.GetPRSList;
import com.example.sachin.fms.dataSets.XmlData;
import com.example.sachin.fms.others.WebServiceConnection;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PRSActivity extends AppCompatActivity {

    private String taskId, call_id;
    private SharedPreferences sp;
    private ProgressDialog pbar;
    private List<XmlData> problemList = new ArrayList<>();
    private List<XmlData> solutionList = new ArrayList<>();
    private List<XmlData> causeList = new ArrayList<>();
    private HashMap<String, XmlData> mDatabyCode1, mDatabyCode2, mDatabyCode3;
    private HashMap<String, XmlData> mDatabyDes1, mDatabyDes2, mDatabyDes3;
    private AutoCompleteTextView search_view1, search_view2, search_view3;
    private TextView prob_text, cause_text, solution_text;

    private int taskCount = 0;
    private Button save_btn;
    private Handler handler;

    private WebServiceConnection connection;

    private SoapSerializationEnvelope envelope;
    private SoapObject request;
    private HttpTransportSE transportSE;
    private String NameSpace;
    private String Soap_Action;
    private String URL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sp = this.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        taskId = sp.getString(getString(R.string.task_number), "null");

        call_id = sp.getString(getString(R.string.call_number), "null");

        CollapsingToolbarLayout cl = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        assert cl != null;
        cl.setTitle("Task No:" + taskId);
        assert toolbar != null;
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        pbar = new ProgressDialog(this);
        pbar.setMessage("Loading... ");
        pbar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pbar.setIndeterminate(true);
        pbar.setCancelable(false);

        //pbar.show();
        prob_text = (TextView) findViewById(R.id.prob_text);
        cause_text = (TextView) findViewById(R.id.cause_text);
        solution_text = (TextView) findViewById(R.id.solution_text);
        save_btn = (Button) findViewById(R.id.save_btn);


        connection = new WebServiceConnection();
        NameSpace = connection.NameSpace;
        Soap_Action = connection.Soap_Action;
        URL = connection.URL;

        Bundle b;
        b = getIntent().getExtras();

        if (b.getString("probCode") != null && !b.getString("probCode").equalsIgnoreCase("anyType{}")) {
            prob_text.setText(b.getString("probCode") + " / " + b.getString("probDesc"));
        }
        if (b.getString("causeCode") != null && !b.getString("causeCode").equalsIgnoreCase("anyType{}")) {
            cause_text.setText(b.getString("causeCode") + " / " + b.getString("causeDesc"));
        }
        if (b.getString("solutionCode") != null && !b.getString("solutionCode").equalsIgnoreCase("anyType{}")) {
            solution_text.setText(b.getString("solutionCode") + " / " + b.getString("solutionDesc"));
        }


        GetPRSList getPRSList = new GetPRSList(PRSActivity.this, PRSActivity.this, pbar, "Problem", sp.getString(getString(R.string.scope), null), 0, null, sp.getString(getString(R.string.company_code), "001"));
        getPRSList.execute();

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] probStr = null;
                String causeStr[] = null;
                String solutionStr[] = null;
                if (prob_text.getText() != null) {
                    probStr = prob_text.getText().toString().split("/");
                }
                if (cause_text.getText() != null) {
                    causeStr = cause_text.getText().toString().split("/");

                }
                if (solution_text.getText() != null) {
                    solutionStr = solution_text.getText().toString().split("/");

                }

                if (probStr == null && causeStr == null && solutionStr == null) {
                    Toast.makeText(PRSActivity.this, "Please select problem, root cause and solution before pressing save.", Toast.LENGTH_LONG).show();
                } else {
                    SavePRS s = new SavePRS(probStr[0], causeStr[0], solutionStr[0]);
                    s.execute();
                }


            }
        });
    }


    public void fillPRSList(List<XmlData> list, int type) {

        if (type == 0) {
            this.problemList = list;
            GetPRSList getPRSList2 = new GetPRSList(PRSActivity.this, PRSActivity.this, pbar, "Cause", sp.getString(getString(R.string.scope), null), 1, null, sp.getString(getString(R.string.company_code), "001"));
            getPRSList2.execute();


        } else if (type == 1) {
            this.causeList = list;
            GetPRSList getPRSList3 = new GetPRSList(PRSActivity.this, PRSActivity.this, pbar, "Solution", sp.getString(getString(R.string.scope), null), 2, null, sp.getString(getString(R.string.company_code), "001"));
            getPRSList3.execute();

        } else if (type == 2) {
            this.solutionList = list;
            pbar.dismiss();
            printProducts();
        }


    }


    public void printProducts() {
        mDatabyCode1 = new HashMap<>();
        mDatabyCode2 = new HashMap<>();
        mDatabyCode3 = new HashMap<>();

        mDatabyDes1 = new HashMap<>();
        mDatabyDes2 = new HashMap<>();
        mDatabyDes3 = new HashMap<>();

        ArrayList<String> code_list_1 = new ArrayList<>();
        ArrayList<String> code_list_2 = new ArrayList<>();
        ArrayList<String> code_list_3 = new ArrayList<>();

        for (XmlData current : problemList) {
            code_list_1.add(current.code);
            code_list_1.add(current.description);


            mDatabyCode1.put(current.code, new XmlData(current.code, current.description));
            mDatabyDes1.put(current.description, new XmlData(current.code, current.description));


        }
        for (XmlData current : causeList) {
            code_list_2.add(current.code);
            code_list_2.add(current.description);


            mDatabyCode2.put(current.code, new XmlData(current.code, current.description));
            mDatabyDes2.put(current.description, new XmlData(current.code, current.description));


        }
        for (XmlData current : solutionList) {
            code_list_3.add(current.code);
            code_list_3.add(current.description);


            mDatabyCode3.put(current.code, new XmlData(current.code, current.description));
            mDatabyDes3.put(current.description, new XmlData(current.code, current.description));


        }
        search_view1 = (AutoCompleteTextView) findViewById(R.id.search_view);
        search_view2 = (AutoCompleteTextView) findViewById(R.id.search_view2);
        search_view3 = (AutoCompleteTextView) findViewById(R.id.search_view3);


        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, code_list_1);
        search_view1.setAdapter(adapter1);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, code_list_2);
        search_view2.setAdapter(adapter2);

        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, code_list_3);
        search_view3.setAdapter(adapter3);


        search_view1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if (mDatabyDes1.containsKey(search_view1.getText().toString())) {
                    prob_text.setText(mDatabyDes1.get(search_view1.getText().toString()).code + " / " + mDatabyDes1.get(search_view1.getText().toString()).description);

                    pbar.show();
                    GetPRSList getPRSList = new GetPRSList(PRSActivity.this, PRSActivity.this, pbar, "Cause", sp.getString(getString(R.string.scope), null), 1, mDatabyDes1.get(search_view1.getText().toString()).code, sp.getString(getString(R.string.company_code), "001"));
                    getPRSList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                } else if (mDatabyCode1.containsKey(search_view1.getText().toString())) {
                    prob_text.setText(mDatabyCode1.get(search_view1.getText().toString()).code + " / " + mDatabyCode1.get(search_view1.getText().toString()).description);

                    pbar.show();

                    GetPRSList getPRSList = new GetPRSList(PRSActivity.this, PRSActivity.this, pbar, "Cause", sp.getString(getString(R.string.scope), null), 1, mDatabyCode1.get(search_view1.getText().toString()).code, sp.getString(getString(R.string.company_code), "001"));
                    getPRSList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                } else if (search_view1.getText().toString().isEmpty()) {
                    prob_text.setText("");

                }

            }
        });


        search_view2.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if (mDatabyDes2.containsKey(search_view2.getText().toString())) {
                    cause_text.setText(mDatabyDes2.get(search_view2.getText().toString()).code + " / " + mDatabyDes2.get(search_view2.getText().toString()).description);

                } else if (mDatabyCode2.containsKey(search_view2.getText().toString())) {
                    cause_text.setText(mDatabyCode2.get(search_view2.getText().toString()).code + " / " + mDatabyCode2.get(search_view2.getText().toString()).description);


                } else if (search_view2.getText().toString().isEmpty()) {
                    cause_text.setText("");

                }

            }
        });

        search_view3.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if (mDatabyDes3.containsKey(search_view3.getText().toString())) {
                    solution_text.setText(mDatabyDes3.get(search_view3.getText().toString()).code + " / " + mDatabyDes3.get(search_view3.getText().toString()).description);

                } else if (mDatabyCode3.containsKey(search_view3.getText().toString())) {
                    solution_text.setText(mDatabyCode3.get(search_view3.getText().toString()).code + " / " + mDatabyCode3.get(search_view3.getText().toString()).description);


                } else if (search_view3.getText().toString().isEmpty()) {
                    solution_text.setText("");

                }

            }
        });


    }


    public class SavePRS extends AsyncTask<String, String, String> {


        String probCode, causeCode, solutionCode, z;

        boolean isSuccess;

        public SavePRS(String probCode, String causeCode, String solutionCode) {

            this.probCode = probCode;
            this.causeCode = causeCode;
            this.solutionCode = solutionCode;


        }

        public void onPreExecute() {
            pbar.show();
        }

        public void onPostExecute(String s) {
            pbar.dismiss();

            if (isSuccess) {
                Toast.makeText(PRSActivity.this, "Saved!! ", Toast.LENGTH_SHORT).show();

                PRSActivity.this.finish();

            } else {
                Toast.makeText(PRSActivity.this, "Database Error,please check your connection", Toast.LENGTH_SHORT).show();

            }


        }


        @Override
        protected String doInBackground(String... params) {
            PropertyInfo pCode, cCode, sCode, compCode, taskNo, callNo;

            request = new SoapObject(NameSpace, "updatePRS");

            callNo = new PropertyInfo();
            callNo.setName("callNo");
            callNo.setType(String.class);
            callNo.setValue(call_id);
            request.addProperty(callNo);


            taskNo = new PropertyInfo();
            taskNo.setName("taskNo");
            taskNo.setType(String.class);
            taskNo.setValue(taskId);
            request.addProperty(taskNo);


            compCode = new PropertyInfo();
            compCode.setName("compCode");
            compCode.setType(String.class);
            compCode.setValue(sp.getString(getString(R.string.company_code), ""));
            request.addProperty(compCode);

            sCode = new PropertyInfo();
            sCode.setName("solutionCode");
            sCode.setType(String.class);
            sCode.setValue(solutionCode);
            request.addProperty(sCode);


            cCode = new PropertyInfo();
            cCode.setName("causeCode");
            cCode.setType(String.class);
            cCode.setValue(causeCode);
            request.addProperty(cCode);

            pCode = new PropertyInfo();
            pCode.setName("probCode");
            pCode.setType(String.class);
            pCode.setValue(probCode);
            request.addProperty(pCode);


            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.setOutputSoapObject(request);
            envelope.dotNet = true;
            transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;

            try {

                transportSE.call(Soap_Action + "updatePRS", envelope);
                SoapObject result = (SoapObject) envelope.getResponse();
                if (result.getPropertyCount() != 0 && result.getProperty(0).toString().equalsIgnoreCase("updated")) {
                    z = result.getProperty(0).toString();
                    isSuccess = true;

                } else {
                    isSuccess = false;
                }
            } catch (Exception e) {

            }
            return z;
        }
    }


}
