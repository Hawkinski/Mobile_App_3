package com.example.sachin.fms.classes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.example.sachin.fms.R;
import com.example.sachin.fms.activities.PRSActivity;
import com.example.sachin.fms.activities.TaskStartActivity;
import com.example.sachin.fms.dataSets.TaskListData;
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
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sachin on 31,May,2017
 * Hawkinski,
 * Dubai, UAE.
 */
public class GetPRSList extends AsyncTask<String,String,  List<XmlData>> {


    private Context context;

    private ProgressDialog progressDialog;

    private WebServiceConnection connection;

    private String NameSpace;
    private String Soap_Action;
    private String URL;
    private SoapSerializationEnvelope envelope;
    private SoapObject request;
    private SoapPrimitive response;
    private HttpTransportSE transportSE;
    private SharedPreferences sp;
    private List<XmlData> data;

    private boolean isSuccess;

    private String root,scope;
    private int type;

    private PRSActivity activity;
    private String probCode,compCode;
    public GetPRSList(Context context,PRSActivity activity, ProgressDialog progressDialog,String root, String scope, int type, String probCode,String compCode){
        this.context =context;
        this.progressDialog=progressDialog;
        this.connection = new WebServiceConnection();
        this.NameSpace = connection.NameSpace;
        this.Soap_Action = connection.Soap_Action;
        this.URL = connection.URL;
        this.root=root;
        this.type =type;

        this.activity= activity;
        this.probCode =probCode;
        this.scope=scope;
        this.compCode=compCode;



    }

    public void onPreExecute(){

    }

    public void onPostExecute( List<XmlData> s){

        if(s!=null){

            activity.fillPRSList(s,type);



           /* String fileName = null;
            if(type == 0){
                fileName ="fms_problem.xml";

            }
            else if(type == 1){
                fileName ="fms_cause.xml";


            }
            else if(type == 2){
                fileName ="fms_solution.xml";


            }*/
            /*File newxmlfile = new File(Environment.getExternalStorageDirectory()+fileName);
            try{
                boolean flag =newxmlfile.createNewFile();
                if(!flag){
                    Toast.makeText(context,"File cannot be created. Please contact Frontline support.",Toast.LENGTH_LONG).show();
                }
            }catch(IOException e){
                Log.e("IOException", "exception in createNewFile() method");
            }
            //we have to bind the new file with a FileOutputStream
            FileOutputStream fileos = null;
            try{
                fileos = new FileOutputStream(newxmlfile);

            }catch(FileNotFoundException e){
                Log.e("FileNotFoundException", "can't create FileOutputStream");
            }
            createXML(fileos,s,root);*/

        }
        else{
            String msg = null;
            if(type == 0){
                msg ="No data available in Problem Master";

            }
            else if(type == 1){
                msg ="No data available in Root Cause Master";


            }
            else if(type == 2){
                msg ="No data available in Solution Master";


            }
            Toast.makeText(context,msg,Toast.LENGTH_LONG).show();


        }


    }



    public void createXML(FileOutputStream fileos, List<XmlData> dataList, String root){


        //we create a XmlSerializer in order to write xml data
        XmlSerializer serializer = Xml.newSerializer();
        try {
            //we set the FileOutputStream as output for the serializer, using UTF-8 encoding
            serializer.setOutput(fileos, "UTF-8");
            //Write <?xml declaration with encoding (if encoding not null) and standalone flag (if standalone not null)
            serializer.startDocument(null, Boolean.valueOf(true));
            //set indentation option
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            //start a tag called "root"

            for(int i=0;i<dataList.size();i++){
                serializer.startTag(null,root);
                //i indent code just to have a view similar to xml-tree
                serializer.startTag(null, "Code");
                serializer.attribute(null, "attribute", "value");
                serializer.endTag(null, "Description");

                serializer.endTag(null,root);
                serializer.endDocument();
            }
            serializer.flush();
            //finally we close the file stream
            fileos.close();



        } catch (Exception e) {
            Log.e("Exception","error occurred while creating xml file");
        }
    }

    @Override
    protected  List<XmlData> doInBackground(String... params) {
        PropertyInfo prob_code, comp_code, task_scope, workprogresscode, empcode, compcode, pg,tt;

        String methodName= null;

        if(type == 0){
            methodName ="getProblemList";
            request = new SoapObject(NameSpace, methodName);

            task_scope = new PropertyInfo();
            task_scope.setName("scope");
            task_scope.setType(String.class);
            task_scope.setValue(scope);
            request.addProperty(task_scope);
        }
        else if(type == 1){
            methodName ="getCauseList";

            request = new SoapObject(NameSpace, methodName);
            prob_code = new PropertyInfo();
            prob_code.setName("probCode");
            prob_code.setType(String.class);
            prob_code.setValue(probCode);
            request.addProperty(prob_code);
        }
        else if(type == 2){
            methodName ="getSolutionList";

            request = new SoapObject(NameSpace,methodName);
            task_scope = new PropertyInfo();
            task_scope.setName("scope");
            task_scope.setType(String.class);
            task_scope.setValue(scope);
            request.addProperty(task_scope);
        }


        List<XmlData> list = new ArrayList<>();



        comp_code = new PropertyInfo();
        comp_code.setName("compCode");
        comp_code.setType(String.class);
        comp_code.setValue(compCode);
        request.addProperty(comp_code);

        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        transportSE = new HttpTransportSE(URL);
        transportSE.debug = true;


        try {

            if(methodName!=null){
                transportSE.call(Soap_Action + methodName, envelope);
                if (transportSE.responseDump != null) {
                    list = parse(transportSE.responseDump);
                }
                else {
                    list = null;
                }
            }
            else{
                Toast.makeText(context,"Web Service is not available. Please try again.",Toast.LENGTH_LONG).show();
                list = null;

            }



        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }


        return list;
    }




    public List<XmlData> parse(String xml) throws XmlPullParserException, IOException {

        ArrayList<XmlData> products = null;
        List<XmlData>list = new ArrayList<>();

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
                            case "Code":
                                current.code = parser.nextText();

                                break;
                            case "Description":
                                current.description = parser.nextText();

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
            data.add(new XmlData(current.code,current.description));
        }
        return data;
    }



}