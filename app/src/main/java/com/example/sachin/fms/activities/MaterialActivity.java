package com.example.sachin.fms.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.sachin.fms.R;
import com.example.sachin.fms.adapterPackage.SearchAdapter;
import com.example.sachin.fms.classes.Logout;
import com.example.sachin.fms.dataSets.MaterialData;
import com.example.sachin.fms.dataSets.XmlData;
import com.example.sachin.fms.others.WebServiceConnection;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MaterialActivity extends AppCompatActivity implements SearchAdapter.SetOnItemClick {


    private RecyclerView rv;
    private InputStream inputStream;
    private XmlPullParserFactory obj;
    private XmlPullParser parser;

    private SharedPreferences sp;
    private SearchAdapter adapter;
    private List<MaterialData> data;
    private List<XmlData> unitList;

    private ProgressDialog pdialog;
    private boolean FAB_status = false;

    private String doc_no, taskId, callId;
    private Bundle b;
    private int count = 0;
    private FloatingActionButton fab, fab1, fab2, fab4;
    private Animation show_fab1, hide_fab1, show_fab2, hide_fab2;


    private String NameSpace;
    private String Soap_Action;
    private String URL;
    private WebServiceConnection connection;

    private SoapSerializationEnvelope envelope;
    private SoapObject request;
    private SoapPrimitive response;
    private HttpTransportSE transportSE;
    private SharedPreferences.Editor edittemp;

    private int tempCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout cl = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        ActionBar actionBar;
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Material Request");
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

        sp = this.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);


        rv = (RecyclerView) findViewById(R.id.rv);
        b = getIntent().getExtras();
        taskId = b.getString(getString(R.string.task_number));
        doc_no = b.getString(getString(R.string.doc_number));
        callId = sp.getString(getString(R.string.call_number), "null");


        pdialog = new ProgressDialog(this);
        pdialog.setMessage("Loading...");
        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdialog.setIndeterminate(true);
        pdialog.setCancelable(false);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab_1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab_2);
        fab4 = (FloatingActionButton) findViewById(R.id.fab4);

        show_fab1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_fab_1);
        hide_fab1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_fab_1);
        show_fab2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_fab_2);
        hide_fab2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_fab_2);




        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logout logout = new Logout(MaterialActivity.this, sp);
                logout.execute();


                finish();

            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(MaterialActivity.this, LandingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getApplication(), AddMaterialActivity.class);
                i.putExtra(getString(R.string.task_number), taskId);
                i.putExtra(getString(R.string.doc_number), doc_no);
                i.putExtra("test", "hide");

                startActivity(i);
                finish();

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!FAB_status) {
                    expandFAB();
                    FAB_status = true;


                } else {
                    hideFAB();
                    FAB_status = false;
                }

            }
        });


        GetData data = new GetData();
        data.execute();



    }

    @Override
    public void itemClick(View v, int position) {

    }

    public boolean parse(String xml) throws XmlPullParserException, IOException {

        ArrayList<MaterialData> products = null;
        MaterialData current = null;
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
                        current = new MaterialData();
                    } else if (current != null) {
                        switch (name) {
                            case "TD_PCD":
                                current.product_code = parser.nextText();

                                break;
                            case "TD_DESC":
                                current.product_des = parser.nextText();

                                break;
                            case "TD_UNIT":
                                current.uom = parser.nextText();
                                break;
                            case "TD_QTY":
                                current.quantity = parser.nextText();
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
            isSuccess = print(products);

        }


        return isSuccess;
    }

    public boolean print(ArrayList<MaterialData> xmlList) {

        data = new ArrayList<>();
        boolean isSuccess = false;
        for (MaterialData current : xmlList) {
            tempCount++;
            // edittemp.putInt("No",temp);
            //edittemp.apply();
            data.add(new MaterialData(current.product_code, current.product_des, current.uom, current.quantity, tempCount));
            isSuccess = true;


        }


        return isSuccess;
    }

    public void expandFAB() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fab1.getLayoutParams();
        params.rightMargin += (int) (fab1.getWidth() * 1.7);
        params.bottomMargin += (int) (fab1.getHeight() * 0.25);
        fab1.setLayoutParams(params);
        fab1.startAnimation(show_fab1);
        fab1.setClickable(true);


        FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) fab2.getLayoutParams();
        params1.rightMargin += (int) (fab2.getWidth() * 1.5);
        params1.bottomMargin += (int) (fab2.getHeight() * 1.5);
        fab2.setLayoutParams(params1);
        fab2.setAnimation(show_fab2);
        fab2.setClickable(true);


    }

    public void hideFAB() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fab1.getLayoutParams();
        params.rightMargin -= (int) (fab1.getWidth() * 1.7);
        params.bottomMargin -= (int) (fab1.getHeight() * 0.25);
        fab1.setLayoutParams(params);
        fab1.setAnimation(hide_fab1);
        fab1.setClickable(false);

        FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) fab2.getLayoutParams();
        params1.rightMargin -= (int) (fab2.getWidth() * 1.5);
        params1.bottomMargin -= (int) (fab2.getHeight() * 1.5);
        fab2.setLayoutParams(params1);
        fab2.setAnimation(hide_fab2);
        fab2.setClickable(false);


    }



    @Override
    public void onBackPressed() {

        ResetDocNo reset = new ResetDocNo();
        reset.execute();

    }

    public class GetData extends AsyncTask<String, String, List<MaterialData>> {


        String z = "";
        int num = 0;
        int hid = 0;

        boolean isSuccess = false;

        public void onPreExecute() {
            pdialog.show();
        }

        public void onPostExecute(List<MaterialData> s) {

            pdialog.dismiss();

            if (!isSuccess) {
                Toast.makeText(MaterialActivity.this, "0 Material requested", Toast.LENGTH_SHORT).show();
            } else {
                adapter = new SearchAdapter(MaterialActivity.this, data);
                rv.setAdapter(adapter);
                rv.setLayoutManager(new LinearLayoutManager(MaterialActivity.this));
            }

        }


        @Override
        protected List<MaterialData> doInBackground(String... params) {

            tempCount = 0;

            PropertyInfo doc, compcode;

            request = new SoapObject(NameSpace, "getMaterialData");

            doc = new PropertyInfo();
            doc.setName("docNo");
            doc.setType(String.class);
            doc.setValue(doc_no);
            request.addProperty(doc);


            compcode = new PropertyInfo();
            compcode.setName("compCode");
            compcode.setType(String.class);
            compcode.setValue(sp.getString(getString(R.string.company_code), ""));
            request.addProperty(compcode);


            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;


            try {

                transportSE.call(Soap_Action + "getMaterialData", envelope);


                if (transportSE.responseDump != null) {
                    isSuccess = parse(transportSE.responseDump);
                } else {
                    isSuccess = false;
                }

            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }

    class Table {
        public String code;
        public String name;
        public String uom;

    }

    public class ResetDocNo extends AsyncTask<String, String, List<XmlData>> {


        String z = "";


        boolean isSuccess = false;

        public void onPreExecute() {
            pdialog.show();
        }

        public void onPostExecute(List<XmlData> s) {

            pdialog.dismiss();

            if (isSuccess) {
                MaterialActivity.this.finish();

            } else {
                Toast.makeText(MaterialActivity.this, "Error occurred while resetting document no.Please contact Forontline.", Toast.LENGTH_SHORT).show();

            }

        }


        @Override
        protected List<XmlData> doInBackground(String... params) {


            PropertyInfo task_id, compcode, call_, doc;

            request = new SoapObject(NameSpace, "resetMaterialDocNo");

            compcode = new PropertyInfo();
            compcode.setName("compCode");
            compcode.setType(String.class);
            compcode.setValue(sp.getString(getString(R.string.company_code), ""));
            request.addProperty(compcode);


            task_id = new PropertyInfo();
            task_id.setName("taskNo");
            task_id.setType(String.class);
            task_id.setValue(taskId);
            request.addProperty(task_id);

            doc = new PropertyInfo();
            doc.setName("docNo");
            doc.setType(String.class);
            doc.setValue(doc_no);
            request.addProperty(doc);

            call_ = new PropertyInfo();
            call_.setName("callNo");
            call_.setType(String.class);
            call_.setValue(callId);
            request.addProperty(call_);


            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;


            try {

                transportSE.call(Soap_Action + "resetMaterialDocNo", envelope);

                SoapObject result = (SoapObject) envelope.getResponse();
                isSuccess = !(result.getPropertyCount() != 0 && result.getProperty(0).toString().equalsIgnoreCase("error"));

            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }

}
