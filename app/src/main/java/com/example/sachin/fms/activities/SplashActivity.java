package com.example.sachin.fms.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sachin.fms.R;
import com.example.sachin.fms.classes.DownloadFile;
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
import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    private ImageView rotate;

    private Timer timer;
    private MyTimerTask myTimerTask;
    private String cd, newtask;

    private View root_view;


    private ProgressDialog progressDialog;

    private WebServiceConnection connection;

    private String NameSpace;
    private String Soap_Action;
    private String URL3, URL;
    private SoapSerializationEnvelope envelope;
    private SoapObject request;
    private SoapPrimitive response;
    private HttpTransportSE transportSE;
    private SharedPreferences sp;
    private List<XmlData> data;

    private boolean isSuccess;

    private String root, scope;
    private int type;

    private PRSActivity activity;
    private String probCode, compCode;


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        root_view = findViewById(R.id.root_view);
        // launchTestService();


        sp = this.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        cd = sp.getString(getString(R.string.user_cd), null);

        connection = new WebServiceConnection();
        NameSpace = connection.NameSpace;
        Soap_Action = connection.Soap_Action;
        URL = connection.URL;
        URL3 = connection.URL3;

        rotate = (ImageView) findViewById(R.id.rotate);
        rotate.post(
                new Runnable() {

                    @Override
                    public void run() {
                        rotate.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.rotate));
                    }
                });

        //Calculate the total duration
        int duration = 0;
        ConnectivityManager cm = (ConnectivityManager) SplashActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            Snackbar.make(root_view, "Internet connection is not available", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null).show();
            //Toast.makeText(SplashActivity.this, "Internet connection is not available", Toast.LENGTH_SHORT).show();
        } else {
            timer = new Timer();
            myTimerTask = new MyTimerTask();
            timer.schedule(myTimerTask, 1600);
        }


    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {


            if (cd == null) {
                timer.cancel();
                Intent intent = new Intent(
                        SplashActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finish();
            } else {
                GetMaterialList get = new GetMaterialList();
                get.execute();

            }


        }
    }


    public class GetMaterialList extends AsyncTask<String, String, String> {


        String fileName;

        public void onPreExecute() {

        }

        public void onPostExecute(String s) {

            if (s != null) {


                DownloadFile download = new DownloadFile(URL3 + s, "XML", s);
                download.execute();
                timer.cancel();
                Intent intent = new Intent(
                        SplashActivity.this, LandingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finish();

            } else {
                timer.cancel();
                Toast.makeText(SplashActivity.this, "Failed to generate material list.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(
                        SplashActivity.this, LandingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finish();

            }


        }



        @Override
        protected String doInBackground(String... params) {
            PropertyInfo comp_code;

            request = new SoapObject(NameSpace, "getMaterialList");


            comp_code = new PropertyInfo();
            comp_code.setName("compCode");
            comp_code.setType(String.class);
            comp_code.setValue(sp.getString(getString(R.string.company_code), null));
            request.addProperty(comp_code);

            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;


            try {


                transportSE.call(Soap_Action + "getMaterialList", envelope);

                SoapObject result = (SoapObject) envelope.getResponse();
                if (result.getPropertyCount() != 0 && result.getProperty(0).toString().equalsIgnoreCase("error")) {
                    isSuccess = false;
                } else {
                    fileName = result.getProperty(0).toString();
                }


            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }


            return fileName;
        }


        public List<XmlData> parse(String xml) throws XmlPullParserException, IOException {

            ArrayList<XmlData> products = null;
            List<XmlData> list = new ArrayList<>();

            XmlData current = null;
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
                            assert products != null;
                            products.add(current);
                        }
                }
                eventType = parser.next();
                list = printProducts(products);

            }


            return list;
        }

        public List<XmlData> printProducts(ArrayList<XmlData> xmlList) {

            data = new ArrayList<>();

            for (XmlData current : xmlList) {
                data.add(new XmlData(current.code, current.name, current.uom));
            }
            return data;
        }

    }


}
