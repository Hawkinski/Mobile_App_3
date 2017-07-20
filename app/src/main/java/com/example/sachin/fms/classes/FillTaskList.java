package com.example.sachin.fms.classes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.sachin.fms.R;
import com.example.sachin.fms.activities.AssignedTaskActivity;
import com.example.sachin.fms.activities.TaskStatusUpdateActivity;
import com.example.sachin.fms.adapterPackage.Adapter;
import com.example.sachin.fms.adapterPackage.RVAdapter;
import com.example.sachin.fms.dataSets.TaskListData;
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
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sachin on 28,May,2017
 * Hawkinski,
 * Dubai, UAE.
 */


/**
 * Asynchronous class to fill the recycler view list with task data
 */

public class FillTaskList extends AsyncTask<String, String, List<TaskListData>>{
    private ProgressDialog pdialog;

    int x = 0;
    boolean isSuccess = false;
    List<TaskListData> d = new ArrayList<>();
    private String xml;
    private AppCompatActivity activity;
    private TaskStatusUpdateActivity activity1;
    private AssignedTaskActivity activity2;

    private RecyclerView recyclerView;
    private RVAdapter radapter;
    private List<TaskListData> data;
    private List<TaskListData> data_2;

    private WebServiceConnection connection;

    private String NameSpace;
    private String Soap_Action;
    private String URL;
    private SoapSerializationEnvelope envelope;
    private SoapObject request;
    private SoapPrimitive response;
    private HttpTransportSE transportSE;
    private SharedPreferences sp;

    private String taskType;
    int pageNo=0;
    public FillTaskList(int i,AppCompatActivity activity, TaskStatusUpdateActivity activity1,AssignedTaskActivity activity2, SharedPreferences sp, int pageNo,ProgressDialog pdialog, String taskType) {
        this.x = i;
        this.sp = sp;
        this.pageNo=pageNo;


        this.taskType=taskType;
        this.activity1 =activity1;
        this.activity2 =activity2;
        this.activity =activity;
        this.pdialog =pdialog;
        connection = new WebServiceConnection();
        NameSpace = connection.NameSpace;
        Soap_Action = connection.Soap_Action;
        URL = connection.URL;
    }

    public void onPreExecute() {
        pdialog.setMessage("Getting Data...");
        pdialog.show();

    }

    public void onPostExecute(List<TaskListData> list) {

        pdialog.dismiss();

        if(list!=null){


            if(x == 0){
                if(activity2!=null){


//                    activity2.fillTaskList(list);

                }else{
                    Toast.makeText(activity, "Parent Activity is Null.Please try again", Toast.LENGTH_SHORT).show();

                }

            }
            else{
                if(activity1!=null){

//                    activity1.fillTaskList(list);

                }
                else{
                    Toast.makeText(activity, "Parent Activity is Null.Please try again", Toast.LENGTH_SHORT).show();

                }

            }

        }
        else{
            Toast.makeText(activity, "No data available", Toast.LENGTH_SHORT).show();

        }
    }

    public interface FillTaskInterface{

        void fillTaskList(List<TaskListData> list);
    }

    @Override
    protected List<TaskListData> doInBackground(String... params) {

        PropertyInfo task, acode, completedcode, workprogresscode, empcode, compcode, pg,tt;
        List<TaskListData> listDatas= new ArrayList<>();

        request = new SoapObject(NameSpace, "getTaskList");

        task = new PropertyInfo();
        task.setName("option");
        task.setType(Integer.class);
        task.setValue(x);
        request.addProperty(task);

        acode = new PropertyInfo();
        acode.setName("assignedcode");
        acode.setType(String.class);
        acode.setValue(sp.getString(activity.getString(R.string.assigned_code), ""));
        request.addProperty(acode);

        completedcode = new PropertyInfo();
        completedcode.setName("completedcode");
        completedcode.setType(String.class);
        completedcode.setValue(sp.getString(activity.getString(R.string.work_completed_code), ""));
        request.addProperty(completedcode);

        workprogresscode = new PropertyInfo();
        workprogresscode.setName("workprogresscode");
        workprogresscode.setType(String.class);
        workprogresscode.setValue(sp.getString(activity.getString(R.string.work_progress_code), ""));
        request.addProperty(workprogresscode);

        empcode = new PropertyInfo();
        empcode.setName("empcode");
        empcode.setType(String.class);
        empcode.setValue(sp.getString(activity.getString(R.string.employee_code), ""));
        request.addProperty(empcode);

        compcode = new PropertyInfo();
        compcode.setName("compcode");
        compcode.setType(String.class);
        compcode.setValue(sp.getString(activity.getString(R.string.company_code), ""));
        request.addProperty(compcode);


        pg = new PropertyInfo();
        pg.setName("pageNo");
        pg.setType(String.class);
        pg.setValue(Integer.toString(pageNo));
        request.addProperty(pg);



        tt = new PropertyInfo();
        tt.setName("taskType");
        tt.setType(String.class);
        tt.setValue(taskType);
        request.addProperty(tt);





        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        transportSE = new HttpTransportSE(URL);
        transportSE.debug = true;


        try {

            transportSE.call(Soap_Action + "getTaskList", envelope);


            if (transportSE.responseDump != null) {
                listDatas = parse(transportSE.responseDump);
            }
            else {
                listDatas = null;
            }


        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }


        return listDatas;
    }




    public List<TaskListData> parse(String xml) throws XmlPullParserException, IOException {

        ArrayList<TaskListData> products = null;
        List<TaskListData>list = new ArrayList<>();

        TaskListData current = null;
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
                        current = new TaskListData();
                    } else if (current != null) {
                        switch (name) {
                            case "TASK_NO":
                                current.task_id = parser.nextText();

                                break;
                            case "TH_CALL_NO":
                                current.call_no = parser.nextText();

                                break;
                            case "REPORT_DATETIME":
                                current.date = parser.nextText();
                                break;
                            case "BUILDING":
                                current.building = parser.nextText();
                                break;
                            case "LOCATION":
                                current.location = parser.nextText();
                                break;
                            case "COMPLAINTS":
                                current.des = parser.nextText();
                                break;
                            case "PRIORITY":
                                current.priority = parser.nextText();
                                break;
                            case "TH_SEVERITY":
                                current.priority_code = parser.nextText();
                                break;
                            case "TH_STATUS":
                                current.status_code = parser.nextText();
                                break;
                            case "TH_WODOCNO":
                                current.ppm_no = parser.nextText();
                                break;
                            case "TH_LOCATION":
                                current.location_code = parser.nextText();
                                break;
                            case "TH_BUILDING":
                                current.building_code = parser.nextText();
                                break;
                            case "TH_INSPECTION":
                                current.isInspection = Boolean.parseBoolean(parser.nextText());
                                break;
                            case "TH_RANDOM_TASK":
                                current.isRandom = Boolean.parseBoolean(parser.nextText());
                                break;
                            case "TH_ASSET":
                                current.asset = parser.nextText();
                                break;
                            case "TH_SCOPE":
                                current.scope = parser.nextText();
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

    public List<TaskListData> printProducts(ArrayList<TaskListData> xmlList) {

        data = new ArrayList<>();
        data_2 = new ArrayList<>();

        for (TaskListData current : xmlList) {


            FormatDate f = new FormatDate();
            String newDate = f.FormatDate(current.date.substring(0, 10));

            if (current.ppm_no != null && !current.ppm_no.isEmpty()) {
                data.add(new TaskListData(current.task_id, current.call_no, newDate, current.location, current.des, current.priority, current.date.substring(11, 16), current.building, current.status_code, current.priority_code, current.ppm_no, current.date, current.location_code, current.building_code, current.isInspection,current.scope, current.asset, current.isRandom));

                data_2.add(new TaskListData(current.task_id, current.call_no, newDate, current.location, current.des, current.priority, current.date.substring(11, 16), current.building, current.status_code, current.priority_code, current.ppm_no, current.date, current.location_code, current.building_code, current.isInspection,current.scope, current.asset, current.isRandom));

            } else {
                data.add(new TaskListData(current.task_id, current.call_no, newDate, current.location, current.des, current.priority, current.date.substring(11, 16), current.building, current.status_code, current.priority_code, "0", current.date, current.location_code, current.building_code, current.isInspection,current.scope, current.asset, current.isRandom));

                data_2.add(new TaskListData(current.task_id, current.call_no, newDate, current.location, current.des, current.priority, current.date.substring(11, 16), current.building, current.status_code, current.priority_code, "0", current.date, current.location_code, current.building_code, current.isInspection,current.scope, current.asset, current.isRandom));

            }



        }


        return data;
    }
}