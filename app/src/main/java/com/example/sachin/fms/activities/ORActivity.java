package com.example.sachin.fms.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sachin.fms.R;
import com.example.sachin.fms.others.WebServiceConnection;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class ORActivity extends AppCompatActivity {

    private String taskId, call_id;
    private SharedPreferences sp;
    private ProgressDialog pbar;
    private TextView observation_text, reason_text;
    private Button save_btn;
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
        setContentView(R.layout.activity_or);
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

        observation_text = (TextView) findViewById(R.id.observation_text);
        reason_text = (TextView) findViewById(R.id.reason_text);
        save_btn = (Button) findViewById(R.id.save_btn);


        connection = new WebServiceConnection();
        NameSpace = connection.NameSpace;
        Soap_Action = connection.Soap_Action;
        URL = connection.URL;

        Bundle b;
        b = getIntent().getExtras();

        if (b.getString("observation") != null) {
            observation_text.setText(b.getString("observation"));
        }
        if (b.getString("reason") != null) {
            reason_text.setText(b.getString("reason"));
        }
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (observation_text.getText() == null && reason_text.getText() == null) {
                    Toast.makeText(ORActivity.this, "Please enter observation and recommendation before pressing save.", Toast.LENGTH_LONG).show();
                } else {
                    SaveOR s = new SaveOR(observation_text.getText().toString(), reason_text.getText().toString());
                    s.execute();
                }


            }
        });
    }

    public class SaveOR extends AsyncTask<String, String, String> {


        String observation, reason, z;

        boolean isSuccess;

        public SaveOR(String observation, String reason) {

            this.observation = observation;
            this.reason = reason;


        }

        public void onPreExecute() {
            pbar.show();
        }

        public void onPostExecute(String s) {
            pbar.dismiss();

            if (isSuccess) {
                Toast.makeText(ORActivity.this, "Saved!! ", Toast.LENGTH_SHORT).show();

                ORActivity.this.finish();
            } else {
                Toast.makeText(ORActivity.this, "Database Error,please check your connection", Toast.LENGTH_SHORT).show();

            }


        }


        @Override
        protected String doInBackground(String... params) {
            PropertyInfo o, r, compCode, taskNo, callNo;

            request = new SoapObject(NameSpace, "updateOR");

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

            o = new PropertyInfo();
            o.setName("observation");
            o.setType(String.class);
            o.setValue(observation);
            request.addProperty(o);


            r = new PropertyInfo();
            r.setName("reason");
            r.setType(String.class);
            r.setValue(reason);
            request.addProperty(r);


            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.setOutputSoapObject(request);
            envelope.dotNet = true;
            transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;

            try {

                transportSE.call(Soap_Action + "updateOR", envelope);
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

