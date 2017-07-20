package com.example.sachin.fms.classes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.example.sachin.fms.R;
import com.example.sachin.fms.activities.AssignedTaskActivity;
import com.example.sachin.fms.activities.LandingActivity;
import com.example.sachin.fms.activities.LoginActivity;
import com.example.sachin.fms.activities.TaskStatusUpdateActivity;
import com.example.sachin.fms.dataSets.TaskCount;
import com.example.sachin.fms.dataSets.employee;
import com.example.sachin.fms.others.WebServiceConnection;
import com.google.firebase.iid.FirebaseInstanceId;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sachin on 10,June,2017
 * Hawkinski,
 * Dubai, UAE.
 */
public class GetTaskCount extends AsyncTask<String, String, String> {

    private ProgressDialog pDialog;
    private String regularCount,PPMCount,inspectionCount,completedCount;

    private String z = "";
    private boolean isSuccess = false;
    private boolean isSuccess_1 = false;
    private String NameSpace;
    private String URL,URL3;

    private String Soap_Action;
    private WebServiceConnection connection;
    private int flag;

    private Context context;
    private SoapSerializationEnvelope envelope;
    private SoapObject request;
    private HttpTransportSE transportSE;
    private SharedPreferences sp;
    private int activity;
    private TaskCount taskCount;
    private int count;
    public GetTaskCount(ProgressDialog pDialog,SharedPreferences sp,Context context,int flag, int activity) {

        this.pDialog =pDialog;
        this.sp=sp;
        this.context=context;
        this.flag=flag;
        this.activity=activity;

        this.connection = new WebServiceConnection();
        this.NameSpace = connection.NameSpace;
        this.Soap_Action = connection.Soap_Action;
        this.URL = connection.URL;

    }

    public void onPreExecute() {
        pDialog.setMessage("Loading....");
        pDialog.show();

    }

    public void onPostExecute(String s) {

        if (isSuccess_1) {

            if(activity == 1){


                Intent i = new Intent(context, TaskStatusUpdateActivity.class);
                i.putExtra("count_1",taskCount.regularCount);
                i.putExtra("count_2",taskCount.inspectionCount);
                i.putExtra("count_3",taskCount.PPMCount);
                i.putExtra("count_4",taskCount.randomCount);

                i.putExtra("count_5",taskCount.completedCount);
                context.startActivity(i);
            }
            else{
                Intent i = new Intent(context, AssignedTaskActivity.class);
                i.putExtra("count_1",taskCount.regularCount);
                i.putExtra("count_2",taskCount.inspectionCount);
                i.putExtra("count_3",taskCount.PPMCount);
                i.putExtra("count_4",taskCount.randomCount);

                context.startActivity(i);
            }


        }
    }


    @Override
    protected String doInBackground(String... params) {

        PropertyInfo compcode, acode, completedcode, workprogresscode, empcode,f,option;

        request = new SoapObject(NameSpace, "getTaskCount");

        compcode = new PropertyInfo();
        compcode.setName("compcode");
        compcode.setType(String.class);
        compcode.setValue(sp.getString(context.getString(R.string.company_code), ""));
        request.addProperty(compcode);

        acode = new PropertyInfo();
        acode.setName("assignedcode");
        acode.setType(String.class);
        acode.setValue(sp.getString(context.getString(R.string.assigned_code), ""));
        request.addProperty(acode);

        completedcode = new PropertyInfo();
        completedcode.setName("completedcode");
        completedcode.setType(String.class);
        completedcode.setValue(sp.getString(context.getString(R.string.work_completed_code), ""));
        request.addProperty(completedcode);

        workprogresscode = new PropertyInfo();
        workprogresscode.setName("workprogresscode");
        workprogresscode.setType(String.class);
        workprogresscode.setValue(sp.getString(context.getString(R.string.work_progress_code), ""));
        request.addProperty(workprogresscode);

        empcode = new PropertyInfo();
        empcode.setName("empcode");
        empcode.setType(String.class);
        empcode.setValue(sp.getString(context.getString(R.string.employee_code), ""));
        request.addProperty(empcode);

        f  = new PropertyInfo();
        f.setName("flag");
        f.setType(Integer.class);
        f.setValue(flag);
        request.addProperty(f);

        option  = new PropertyInfo();
        option.setName("option");
        option.setType(Integer.class);
        option.setValue(activity);
        request.addProperty(option);

        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        transportSE = new HttpTransportSE(URL);
        transportSE.debug = true;

        try {

                transportSE.call(Soap_Action + "getTaskCount", envelope);
                SoapObject result2 = (SoapObject) envelope.getResponse();

            if(flag == 4){
                taskCount = Parser_1(transportSE.responseDump);
                isSuccess_1 = true;


            }
            else{
                count = Parser_2(transportSE.responseDump);
                isSuccess_1 = true;
            }


        } catch (HttpResponseException e) {
            e.printStackTrace();
            e.getMessage();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            z = e.getMessage();
        }

        return null;
    }


    public TaskCount Parser_1(String xml) throws XmlPullParserException, IOException {

        boolean isSuccess = false;
        TaskCount current = null;
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
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();

                    // Log.e("tag name", name);


                    if (name.equals("Table")) {
                        current = new TaskCount();
                    } else if (current != null) {
                        switch (name) {
                            case "REGULAR_COUNT":
                                current.regularCount = Integer.parseInt(parser.nextText());

                                break;
                            case "INSP_COUNT":
                                current.inspectionCount = Integer.parseInt(parser.nextText());

                                break;
                            case "PPM_COUNT":
                                current.PPMCount = Integer.parseInt(parser.nextText());

                                break;
                            case "COMP_COUNT":
                                current.completedCount = Integer.parseInt(parser.nextText());

                                break;
                            case "RANDOM_COUNT":
                                current.randomCount = Integer.parseInt(parser.nextText());

                                break;
                        }

                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();

            }
            eventType = parser.next();
        }
        return current;


    }



    public int Parser_2(String xml) throws XmlPullParserException, IOException {

        int current = 0;
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
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();

                    // Log.e("tag name", name);


                    if (name.equals("Table")) {
                        current=0;
                    } else {
                        switch (name) {
                            case "TASK_COUNT":
                                current = Integer.parseInt (parser.nextText());
                                break;

                        }

                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
            }
            eventType = parser.next();
        }
        return current;
    }

}
