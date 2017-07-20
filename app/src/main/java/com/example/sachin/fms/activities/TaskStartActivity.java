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
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sachin.fms.R;
import com.example.sachin.fms.classes.Logout;
import com.example.sachin.fms.others.WebServiceConnection;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;


public class TaskStartActivity extends AppCompatActivity {

    private CardView bt_pic_card, at_pic_card, status_card, task_details_card, signature_card,
            mr_card, mu_card, remarks_card,
            prs_card, ct_card, tp_card, risk_card, ppm_card;
    private boolean clicks = false;
    private boolean update = false;
    private String taskId, call_no, wo_no, reported_date, reported_time;
    private int x = 0;
    private boolean edit_before = false;
    private boolean completed = false;
    private SharedPreferences sp;

    private boolean FAB_status = false;
    private ProgressDialog pbar;


    private  TextView ppm_card_text;
    private ArrayList<String> imageStringC;
    private FloatingActionButton fab, fab1, fab2;
    private Animation show_fab1, hide_fab1, show_fab2, hide_fab2;

    private String NameSpace;
    private String Soap_Action;
    private String URL;
    private WebServiceConnection connection;

    private SoapSerializationEnvelope envelope;
    private SoapObject request;
    private SoapPrimitive response;
    private HttpTransportSE transportSE;

    private String signatureString = "";
    private int value;


    private String taskType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        connection = new WebServiceConnection();
        NameSpace = connection.NameSpace;
        Soap_Action = connection.Soap_Action;
        URL = connection.URL;

        sp = this.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        taskId = sp.getString(getString(R.string.task_number), "null");
        call_no = sp.getString(getString(R.string.call_number), "null");
        wo_no = sp.getString(getString(R.string.ppm_work_order_no), "null");

        Bundle b = getIntent().getExtras();

        if (b != null) {
            value = b.getInt("status");
        }

        bt_pic_card = (CardView) findViewById(R.id.bt_pic_card);
        at_pic_card = (CardView) findViewById(R.id.at_pic_card);
        status_card = (CardView) findViewById(R.id.status_card);
        task_details_card = (CardView) findViewById(R.id.task_details_card);
        signature_card = (CardView) findViewById(R.id.signature_card);
        mr_card = (CardView) findViewById(R.id.mr_card);
        mu_card = (CardView) findViewById(R.id.mu_card);
        remarks_card = (CardView) findViewById(R.id.remarks_card);
        prs_card = (CardView) findViewById(R.id.prs_card);
        ct_card = (CardView) findViewById(R.id.ct_card);
        tp_card = (CardView) findViewById(R.id.tp_card);
        risk_card = (CardView) findViewById(R.id.risk_card);
        ppm_card = (CardView) findViewById(R.id.ppm_card);

        ppm_card_text= (TextView)findViewById(R.id.text8);

        pbar = new ProgressDialog(this);
        pbar.setMessage("Loading... ");
        pbar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pbar.setIndeterminate(true);
        pbar.setCancelable(false);

        enableOrDisableBtn();


        // Log.e("CALL _ NO ", call_no);
        reported_date = sp.getString(getString(R.string.reported_date), "");
        reported_time = sp.getString(getString(R.string.reported_time), "");

        CollapsingToolbarLayout cl = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        assert cl != null;
        cl.setTitle("Task No: " + taskId);


        Check check = new Check();
        check.execute();


        ActionBar actionBar;
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("");
        }

        assert toolbar != null;
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });



        TextView txt = (TextView) findViewById(R.id.txt0);
        assert txt != null;


        txt.setText(reported_date + "/ " + reported_time.substring(0, 5));


        bt_pic_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                {

                    Intent i = new Intent(TaskStartActivity.this, BeforeTaskActivity.class);
                    i.putExtra("activity", 1);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(i);

                }


            }
        });
        at_pic_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                {
                    Intent i = new Intent(TaskStartActivity.this, BeforeTaskActivity.class);
                    i.putExtra("activity", 2);

                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(i);
                }


            }
        });
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab_1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab_2);

        show_fab1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_fab_1);
        hide_fab1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_fab_1);
        show_fab2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_fab_2);
        hide_fab2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_fab_2);

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logout logout = new Logout(TaskStartActivity.this, sp);
                logout.execute();


                finish();

            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(TaskStartActivity.this, LandingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        task_details_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplication(), TaskDetailsActivity.class);

                i.putExtra("test", "hide");
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


                startActivity(i);
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
        ct_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TaskStartActivity.this, CloseActivity.class);
                i.putExtra("activity", 3);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(i);
            }
        });

        signature_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fill_Images fill = new Fill_Images();
                fill.execute();

            }
        });

        risk_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TaskStartActivity.this, SafetyRulesActivity.class);
                i.putExtra("activity", 5);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


                startActivity(i);
            }
        });

        ppm_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TaskStartActivity.this, PPMWorkOrderListActivity.class);
                i.putExtra("activity", 5);
                i.putExtra("isRandom", 1);

                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(i);

            }
        });
        mr_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                GetDoc_No doc = new GetDoc_No();
                doc.execute();

            }
        });
        mu_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent i = new Intent(TaskStartActivity.this, MaterialUsedActivity.class);
                i.putExtra("activity", 6);
                i.putExtra(getString(R.string.task_number), taskId);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(i);


            }
        });
        status_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                GetStatus get = new GetStatus();
                get.execute();


            }
        });
        prs_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetSavedPRS get = new GetSavedPRS();
                get.execute();
            }
        });

        tp_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    Intent i = new Intent(TaskStartActivity.this, BeforeTaskActivity.class);
                    i.putExtra("activity", 3);

                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(i);


                } catch (Exception ex) {

                    Log.e("Exception", "error occurred while creating xml file");
                }


            }
        });


        remarks_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetSavedOR get = new GetSavedOR();
                get.execute();
            }
        });
    }

    private void enableOrDisableBtn() {
        pbar.show();
        boolean flag = sp.getBoolean("isInspection", false);

        if (flag) {


            tp_card.setVisibility(View.VISIBLE);
            remarks_card.setVisibility(View.VISIBLE);

            bt_pic_card.setVisibility(View.GONE);
            at_pic_card.setVisibility(View.GONE);
            ct_card.setVisibility(View.GONE);
            mu_card.setVisibility(View.GONE);
            mr_card.setVisibility(View.GONE);
            risk_card.setVisibility(View.GONE);
            prs_card.setVisibility(View.GONE);


        }
        else if (!wo_no.equalsIgnoreCase("null") && !wo_no.equalsIgnoreCase("0")) {
            ppm_card.setVisibility(View.VISIBLE);
            prs_card.setVisibility(View.GONE);

        } else {
            ppm_card.setVisibility(View.GONE);

        }
        boolean isRandom = sp.getBoolean("isRandom", false);

        if(isRandom){
            ppm_card_text.setText("Maintenance Checklist");
            ppm_card.setVisibility(View.VISIBLE);

            prs_card.setVisibility(View.GONE);
        }

        pbar.dismiss();


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
    public void onResume() {
        super.onResume();

        Check check = new Check();
        check.execute();


    }

    private class GetSavedPRS extends AsyncTask<String, String, String> {

        boolean isSuccess;
        String pc, pd, cc, cd, sc, sd;

        public void onPreExecute() {

            pbar.show();
        }

        public void onPostExecute(String s) {
            pbar.dismiss();
            if (isSuccess) {
                Intent i = new Intent(TaskStartActivity.this, PRSActivity.class);
                i.putExtra("activity", 5);
                i.putExtra("probCode", pc);
                i.putExtra("probDesc", pd);
                i.putExtra("causeCode", cc);
                i.putExtra("causeDesc", cd);
                i.putExtra("solutionCode", sc);
                i.putExtra("solutionDesc", sd);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(i);

            }


        }


        @Override
        protected String doInBackground(String... params) {
            PropertyInfo compcode, task_id, call_id;

            request = new SoapObject(NameSpace, "getSavedPRS");

            compcode = new PropertyInfo();
            compcode.setName("compCode");
            compcode.setType(String.class);
            compcode.setValue(sp.getString(getString(R.string.company_code), ""));
            request.addProperty(compcode);

            call_id = new PropertyInfo();
            call_id.setName("callNo");
            call_id.setType(String.class);
            call_id.setValue(call_no);
            request.addProperty(call_id);

            task_id = new PropertyInfo();
            task_id.setName("taskNo");
            task_id.setType(String.class);
            task_id.setValue(taskId);
            request.addProperty(task_id);

            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;


            try {

                transportSE.call(Soap_Action + "getSavedPRS", envelope);
                SoapObject result2 = (SoapObject) envelope.getResponse();
                if (result2.getPropertyCount() != 0) {
                    pc = result2.getProperty(0).toString();
                    pd = result2.getProperty(1).toString();
                    cc = result2.getProperty(2).toString();
                    cd = result2.getProperty(3).toString();
                    sc = result2.getProperty(4).toString();
                    sd = result2.getProperty(5).toString();

                    isSuccess = true;
                } else {
                    isSuccess = false;
                }


            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }


            return pc;
        }
    }

    private class GetSavedOR extends AsyncTask<String, String, String> {

        boolean isSuccess;
        String observation, reason;

        public void onPreExecute() {

            pbar.show();
        }

        public void onPostExecute(String s) {
            pbar.dismiss();
            if (isSuccess) {
                Intent i = new Intent(TaskStartActivity.this, ORActivity.class);
                i.putExtra("activity", 5);
                i.putExtra("observation", observation);
                i.putExtra("reason", reason);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(i);
            }


        }


        @Override
        protected String doInBackground(String... params) {
            PropertyInfo compcode, task_id, call_id;

            request = new SoapObject(NameSpace, "getSavedOR");

            compcode = new PropertyInfo();
            compcode.setName("compCode");
            compcode.setType(String.class);
            compcode.setValue(sp.getString(getString(R.string.company_code), ""));
            request.addProperty(compcode);

            call_id = new PropertyInfo();
            call_id.setName("callNo");
            call_id.setType(String.class);
            call_id.setValue(call_no);
            request.addProperty(call_id);

            task_id = new PropertyInfo();
            task_id.setName("taskNo");
            task_id.setType(String.class);
            task_id.setValue(taskId);
            request.addProperty(task_id);

            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;


            try {

                transportSE.call(Soap_Action + "getSavedOR", envelope);
                SoapObject result2 = (SoapObject) envelope.getResponse();
                if (result2.getPropertyCount() != 0) {
                    observation = result2.getProperty(0).toString();
                    reason = result2.getProperty(1).toString();

                    isSuccess = true;
                } else {
                    isSuccess = false;
                }


            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }


            return observation;
        }
    }

    public class GetStatus extends AsyncTask<String, String, String> {

        String current_status = "";
        String current_reading = "";
        String default_uom = "";

        String z = "";
        boolean isSuccess = false;

        public void onPreExecute() {
            pbar.show();
        }

        public void onPostExecute(String s) {
            pbar.dismiss();

            if (isSuccess) {
                Intent i = new Intent(TaskStartActivity.this, StatusChangeActivity.class);
                i.putExtra("activity", 5);
                i.putExtra("current_status", current_status);
                i.putExtra("current_reading", current_reading);
                i.putExtra("default_uom", default_uom);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(i);
            } else {
                Toast.makeText(TaskStartActivity.this, s, Toast.LENGTH_SHORT).show();

            }


        }

        @Override
        protected String doInBackground(String... params) {
            PropertyInfo compcode, task_id, call_id;

            request = new SoapObject(NameSpace, "getUpdatedStatus");

            compcode = new PropertyInfo();
            compcode.setName("compCode");
            compcode.setType(String.class);
            compcode.setValue(sp.getString(getString(R.string.company_code), ""));
            request.addProperty(compcode);

            call_id = new PropertyInfo();
            call_id.setName("callNo");
            call_id.setType(String.class);
            call_id.setValue(call_no);
            request.addProperty(call_id);

            task_id = new PropertyInfo();
            task_id.setName("taskNo");
            task_id.setType(String.class);
            task_id.setValue(taskId);
            request.addProperty(task_id);

            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;


            try {

                transportSE.call(Soap_Action + "getUpdatedStatus", envelope);
                SoapObject object = (SoapObject) envelope.getResponse();

                if (object.getPropertyCount() != 0) {
                    current_status = object.getPropertyAsString(0);
                    current_reading = object.getPropertyAsString(1);
                    default_uom = object.getPropertyAsString(2);

                    z = current_status;
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }


            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }


            return z;
        }
    }

    public class GetDoc_No extends AsyncTask<String, String, String> {

        String doc_no = "";
        String z = "";
        boolean isSuccess = false;

        public void onPreExecute() {
            pbar.show();
        }

        public void onPostExecute(String s) {
            pbar.dismiss();

            if (isSuccess && !s.equalsIgnoreCase("0")) {
                //Toast.makeText(TaskStartActivity.this,s,Toast.LENGTH_SHORT).show();
                Intent i = new Intent(TaskStartActivity.this, MaterialActivity.class);
                i.putExtra("activity", 6);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra(getString(R.string.task_number), taskId);
                i.putExtra(getString(R.string.doc_number), doc_no);
                startActivity(i);
            } else {
                Toast.makeText(TaskStartActivity.this, "Failed to generate Document number. Please try again.", Toast.LENGTH_SHORT).show();

            }


        }


        @Override
        protected String doInBackground(String... params) {

            PropertyInfo compcode, call, task_id;

            request = new SoapObject(NameSpace, "generateMaterialDocNo");

            compcode = new PropertyInfo();
            compcode.setName("compCode");
            compcode.setType(String.class);
            compcode.setValue(sp.getString(getString(R.string.company_code), ""));
            request.addProperty(compcode);

            call = new PropertyInfo();
            call.setName("callNo");
            call.setType(String.class);
            call.setValue(call_no);
            request.addProperty(call);

            task_id = new PropertyInfo();
            task_id.setName("taskNo");
            task_id.setType(String.class);
            task_id.setValue(taskId);
            request.addProperty(task_id);

            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;


            try {

                transportSE.call(Soap_Action + "generateMaterialDocNo", envelope);
                SoapObject object = (SoapObject) envelope.getResponse();

                if (object.getPropertyCount() != 0) {
                    doc_no = object.getPropertyAsString(0);
                    z = doc_no;
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }


            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }


            return z;
        }
    }

    public class Fill_Images extends AsyncTask<String, String, String> {

        boolean isSuccess = false;
        String z = "";


        public void onPostExecute(String s) {
            pbar.dismiss();
            if (isSuccess) {

                Intent i = new Intent(TaskStartActivity.this, TaskCompleteActivity.class);
                i.putExtra("activity", 4);


                // tinyDB.putListString("imageStringA",imageStringA);
                // tinyDB.putListString("imageStringB",imageStringB);
                // tinyDB.putListString("imageStringC",imageStringC);
                //i.putExtra(getString(R.string.task_number),taskId);
                i.putExtra("string", signatureString);
                startActivity(i);

            } else {
                Intent i = new Intent(TaskStartActivity.this, TaskCompleteActivity.class);
                i.putExtra("activity", 4);


                // imageStringA.clear();
                // imageStringB.clear();
                // imageStringC.clear();

                // tinyDB.putListString("imageStringA",imageStringA);
                // tinyDB.putListString("imageStringB",imageStringB);
                //tinyDB.putListString("imageStringC",imageStringC);
                // i.putExtra(getString(R.string.task_number),taskId);
                i.putExtra("string", signatureString);
                startActivity(i);
            }
        }

        public void onPreExecute() {
            pbar.show();
        }

        @Override
        protected String doInBackground(String... params) {

            PropertyInfo compcode, task_id, call_, type;

            request = new SoapObject(NameSpace, "getImages");
            imageStringC = new ArrayList<>();

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

            call_ = new PropertyInfo();
            call_.setName("callNo");
            call_.setType(String.class);
            call_.setValue(call_no);
            request.addProperty(call_);

            type = new PropertyInfo();
            type.setName("type");
            type.setType(String.class);
            type.setValue("C");
            request.addProperty(type);

            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;


            try {

                transportSE.call(Soap_Action + "getImages", envelope);
                SoapObject object = (SoapObject) envelope.getResponse();

                if (object.getPropertyCount() != 0) {
                    // imageStringC.add(object.getPropertyAsString(0));
                    signatureString = object.getProperty(0).toString();
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }


            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }


            return z;
        }
    }

    public class Check extends AsyncTask<String, String, String> {


        boolean isSuccess = false;
        String z = "";
        private String statusCode = "";


        public void onPreExecute() {
            pbar.show();

        }

        public void onPostExecute(String r) {

            pbar.dismiss();
            if (isSuccess) {

                if (statusCode.equals(sp.getString(getString(R.string.work_completed_code), ""))) {

                    boolean flag = sp.getBoolean(getString(R.string.isInspection), false);

                    if (!flag) {

                        // Log.e("WORK COMPLETED equal", statusCode);
                        SharedPreferences.Editor edit = sp.edit();
                        edit.putBoolean("task_completed_" + taskId, true);
                        edit.apply();
                        signature_card.setVisibility(View.VISIBLE);

                        bt_pic_card.setVisibility(View.GONE);
                        at_pic_card.setVisibility(View.GONE);
                        ct_card.setVisibility(View.GONE);
                        mu_card.setVisibility(View.GONE);
                        mr_card.setVisibility(View.GONE);
                        risk_card.setVisibility(View.GONE);
                        status_card.setVisibility(View.GONE);
                        ppm_card.setVisibility(View.GONE);
                        remarks_card.setVisibility(View.GONE);
                        prs_card.setVisibility(View.GONE);

                    } else {
                        SharedPreferences.Editor edit = sp.edit();
                        edit.putBoolean("task_completed_" + taskId, true);
                        edit.apply();
                        signature_card.setVisibility(View.GONE);

                        remarks_card.setVisibility(View.GONE);
                        tp_card.setVisibility(View.GONE);
                        status_card.setVisibility(View.GONE);

                        bt_pic_card.setVisibility(View.GONE);
                        at_pic_card.setVisibility(View.GONE);
                        ct_card.setVisibility(View.GONE);
                        mu_card.setVisibility(View.GONE);
                        mr_card.setVisibility(View.GONE);
                        ppm_card.setVisibility(View.GONE);

                        risk_card.setVisibility(View.GONE);
                        prs_card.setVisibility(View.GONE);
                    }



                }

            } else {
                Toast.makeText(TaskStartActivity.this, "Data connection problem", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {

            PropertyInfo compcode, task_id;

            request = new SoapObject(NameSpace, "checkWorkStatus");

            compcode = new PropertyInfo();
            compcode.setName("compcode");
            compcode.setType(String.class);
            compcode.setValue(sp.getString(getString(R.string.company_code), ""));
            request.addProperty(compcode);


            task_id = new PropertyInfo();
            task_id.setName("task_id");
            task_id.setType(String.class);
            task_id.setValue(taskId);
            request.addProperty(task_id);

            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;


            try {

                transportSE.call(Soap_Action + "checkWorkStatus", envelope);
                SoapObject object = (SoapObject) envelope.getResponse();

                if (object.getPropertyCount() != 0) {
                    statusCode = object.getPropertyAsString(0);
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }


            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }


            return z;
        }
    }


}
