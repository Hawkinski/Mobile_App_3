package com.example.sachin.fms.classes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.sachin.fms.R;
import com.example.sachin.fms.activities.PRSActivity;
import com.example.sachin.fms.activities.TaskStartActivity;
import com.example.sachin.fms.others.WebServiceConnection;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * Created by Sachin on 01,June,2017
 * Hawkinski,
 * Dubai, UAE.
 */
public class SavePRS extends AsyncTask<String, String, String> {

    private String probCode, causeCode, solutionCode, z,call_id,taskId;
    private boolean isSuccess;
    private WebServiceConnection connection;

    private SoapSerializationEnvelope envelope;
    private SoapObject request;
    private HttpTransportSE transportSE;
    private ProgressDialog pbar;
    private String NameSpace;
    private String Soap_Action;
    private String URL;
    private SharedPreferences sp;

    private PRSActivity activity;
    public SavePRS(PRSActivity activity, String probCode, String causeCode, String solutionCode, ProgressDialog pbar, String taskNo, String callNo,SharedPreferences sp) {

        this.probCode = probCode;
        this.causeCode = causeCode;
        this.call_id=callNo;
        this.taskId=taskNo;
        this.activity=activity;
        this.pbar =pbar;
        this.sp =sp;
        this.solutionCode = solutionCode;
        connection = new WebServiceConnection();
        NameSpace = connection.NameSpace;
        Soap_Action = connection.Soap_Action;
        URL = connection.URL;

    }

    public void onPreExecute() {
        pbar.dismiss();
        pbar.setMessage("Saving...");
        pbar.show();
    }

    public void onPostExecute(String s) {

        pbar.dismiss();

        if (isSuccess) {
            Toast.makeText(activity, "Saved!! ", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(activity, TaskStartActivity.class);
            i.putExtra(activity.getString(R.string.task_number), taskId);
            i.putExtra("activity", 2);
            i.putExtra(activity.getString(R.string.call_number), call_id);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(i);
            activity.finish();
        } else {
            Toast.makeText(activity, "Database Error,please check your connection", Toast.LENGTH_SHORT).show();

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
        compCode.setValue(sp.getString(activity.getString(R.string.company_code), ""));
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
            isSuccess = false;

            //Log.e("error", e.getMessage());

        }

        return z;

    }
}