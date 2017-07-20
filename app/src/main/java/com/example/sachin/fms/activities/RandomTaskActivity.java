package com.example.sachin.fms.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sachin.fms.R;
import com.example.sachin.fms.classes.GetLBUCSAMList;
import com.example.sachin.fms.dataSets.XmlData;
import com.example.sachin.fms.others.WebServiceConnection;
import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

//import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class RandomTaskActivity extends AppCompatActivity implements  View.OnClickListener {

    private String taskId, call_id;
    private SharedPreferences sp;
    private ProgressDialog pbar;
    private List<XmlData> locationList = new ArrayList<>();
    private List<XmlData> buildingList  = new ArrayList<>();
    private List<XmlData> unitList = new ArrayList<>();
    private List<XmlData> contractList = new ArrayList<>();
    private List<XmlData> scopeList = new ArrayList<>();
    private List<XmlData> assetList = new ArrayList<>();
    private List<XmlData> mainList = new ArrayList<>();

//    private ZXingScannerView mScannerView;

    private IntentIntegrator qrScan;
    private HashMap<String, XmlData> mDatabyCode1, mDatabyCode2, mDatabyCode3,mDatabyCode4,mDatabyCode5,mDatabyCode6,mDatabyCode7;
    private HashMap<String, XmlData> mDatabyDes1, mDatabyDes2, mDatabyDes3,mDatabyDes4,mDatabyDes5,mDatabyDes6,mDatabyDes7;
    private AutoCompleteTextView search_view1, search_view2, search_view3,search_view4,search_view5,search_view6,search_view7;
    private TextView asset_text,location_text, building_text, unit_text,contract_text,scope_text,main_text;

    private EditText remarks_text;
    private int taskCount = 0;
    private Button save_btn;
    private Handler handler;

    private List<XmlData> unitScanDetails= null;
    private WebServiceConnection connection;

    private SoapSerializationEnvelope envelope;
    private SoapObject request;
    private HttpTransportSE transportSE;
    private String NameSpace;
    private String Soap_Action;
    private String URL;

    private  ImageButton qr_scan_btn, asset_scan_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sp = this.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);


        ActionBar actionBar;
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Create Random Task");
        }

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
        location_text = (TextView) findViewById(R.id.location_text);
        building_text = (TextView) findViewById(R.id.building_text);
        unit_text = (TextView) findViewById(R.id.unit_text);
        contract_text = (TextView) findViewById(R.id.contract_text);
        scope_text = (TextView) findViewById(R.id.scope_text);
        asset_text = (TextView) findViewById(R.id.asset_text);
        main_text = (TextView) findViewById(R.id.main_text);
        remarks_text = (EditText) findViewById(R.id.remarks);

        save_btn = (Button) findViewById(R.id.save_btn);
        qr_scan_btn  = (ImageButton) findViewById(R.id.qr_scan_btn);
        asset_scan_btn  = (ImageButton) findViewById(R.id.asset_scan_btn);


        connection = new WebServiceConnection();
        NameSpace = connection.NameSpace;
        Soap_Action = connection.Soap_Action;
        URL = connection.URL;



        GetLBUCSAMList getLBUCSAMList = new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, null,null, 0, null,null, sp.getString(getString(R.string.company_code), "001"));
        getLBUCSAMList.execute();


        GetLBUCSAMList getLBUCSAMList2 = new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, null,null, 4, null,null, sp.getString(getString(R.string.company_code), "001"));
        getLBUCSAMList2.execute();

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                validate();

            }
        });

       /* qr_scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                QrScanner(v);
            }
        });*/
        qr_scan_btn.setOnClickListener(this);
        qrScan = new IntentIntegrator(this);
        asset_scan_btn.setOnClickListener(this);

       /* asset_scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QrScanner(v);

            }
        });*/



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        result.getFormatName();
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                //if qr contains data
                try {
                    //converting the data to json







                    if(data.getIntExtra("type",0) == 0){
                        JSONObject obj = null;


                        Toast.makeText(RandomTaskActivity.this,"unit",Toast.LENGTH_LONG).show();

                        String locationCode ="",locationDesc="",buildingCode="",buildingDesc="",unitCode="",unitDesc="";

                        obj = new JSONObject(result.getContents());


                        locationCode = obj.getString("locationCode");
                        locationDesc = obj.getString("locationDesc");
                        buildingCode = obj.getString("buildingCode");
                        buildingDesc = obj.getString("buildingDesc");
                        unitCode = obj.getString("unitCode");
                        unitDesc = obj.getString("unitDesc");

                        if(locationCode!=""){
                            unitScanDetails = new ArrayList<>();
                            unitScanDetails.add(0,new XmlData(locationCode,locationDesc));
                            unitScanDetails.add(1,new XmlData(buildingCode,buildingDesc));
                            unitScanDetails.add(2,new XmlData(unitCode,unitDesc));

                        }
                        else{
                            unitScanDetails = null;
                        }


                        if(unitScanDetails != null){
                            if(unitScanDetails.get(0).code !=null) {
                                location_text.setText(unitScanDetails.get(0).code +" / "+unitScanDetails.get(0).description);

                            }
                            if(unitScanDetails.get(1).code !=null) {
                                building_text.setText(unitScanDetails.get(1).code +" / "+unitScanDetails.get(1).description);

                            }

                            if(unitScanDetails.get(2).code !=null) {
                                unit_text.setText(unitScanDetails.get(2).code +" / "+unitScanDetails.get(2).description);

                            }

                        }
                        else{
                            Toast.makeText(RandomTaskActivity.this,"This QR code is not valid.Please enter the details manually.",Toast.LENGTH_LONG).show();

                        }
                        // populate location autocomplete
                        GetLBUCSAMList getLBUCSAMList = new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, null,null, 0, null,null, sp.getString(getString(R.string.company_code), "001"));
                        getLBUCSAMList.execute();

                        // populate scope autocomplete
                        GetLBUCSAMList getLBUCSAMList1 = new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, null,null, 4, null,null, sp.getString(getString(R.string.company_code), "001"));
                        getLBUCSAMList1.execute();

                        // populate building autocomplete
                        GetLBUCSAMList getLBUCSAMList2 = new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, unitScanDetails.get(0).code , null, 1, null, null, sp.getString(getString(R.string.company_code), "001"));
                        getLBUCSAMList2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        // populate unit autocomplete
                        GetLBUCSAMList getLBUCSAMList3 = new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, unitScanDetails.get(0).code, null, 2,unitScanDetails.get(1).code, null, sp.getString(getString(R.string.company_code), "001"));
                        getLBUCSAMList3.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                        // populate contract autocomplete
                        GetLBUCSAMList getLBUCSAMList4= new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, unitScanDetails.get(0).code, null, 3, unitScanDetails.get(1).code, null, sp.getString(getString(R.string.company_code), "001"));
                        getLBUCSAMList4.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                    }
                    else if(data.getIntExtra("type",0) == 1){
                        Toast.makeText(RandomTaskActivity.this,"asset",Toast.LENGTH_LONG).show();

                        JSONObject obj = null;
                        String assetCode ="",assetDesc="";
                        obj = new JSONObject(result.getContents());


                        assetCode = obj.getString("assetCode");
                        assetDesc = obj.getString("assetDesc");

                        if(assetCode!=""){
                            unitScanDetails = new ArrayList<>();
                            unitScanDetails.add(0,new XmlData(assetCode,assetDesc));

                        } else{
                            unitScanDetails = null;
                        }


                        if(unitScanDetails != null){
                            if(unitScanDetails.get(0).code !=null) {
                                asset_text.setText(unitScanDetails.get(0).code +" / "+unitScanDetails.get(0).description);

                            }


                        }
                        else{
                            Toast.makeText(RandomTaskActivity.this,"This QR code is not valid.Please enter the details manually.",Toast.LENGTH_LONG).show();

                        }
                    }





                } catch (JSONException e) {
                    e.printStackTrace();
                    //if control comes here
                    //that means the encoded format not matches
                    //in this case you can display whatever data is available on the qrcode
                    //to a toast
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    public void onClick(View view) {
        //initiating the qr code scan

        Bundle b = new Bundle();

        if(view == qr_scan_btn){


            qrScan.addExtra("type",0);

            qrScan.initiateScan();
        }
        else  if(view == asset_scan_btn){
            qrScan.addExtra("type",0);

            qrScan.initiateScan();
        }




    }

    public void validate(){

        String[] locationStr = null;
        String buildingStr[] = null;
        String unitStr[] = null;
        String[] contractStr = null;
        String scopeStr[] = null;
        String assetStr[] = null;
        String mainStr[] = null;
        String remarks= null;


        remarks = remarks_text.getText().toString();


        location_text.setError(null);
        building_text.setError(null);
        unit_text.setError(null);
        contract_text.setError(null);
        scope_text.setError(null);
        asset_text.setError(null);
        main_text.setError(null);

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(location_text.getText())|| location_text.getText().toString().equalsIgnoreCase("Code / Description")) {

            location_text.setError(getString(R.string.error_field_required));
            focusView = location_text;
            cancel = true;
        }
        else{
            locationStr = location_text.getText().toString().split("/");

        }
        if (TextUtils.isEmpty(building_text.getText())|| building_text.getText().toString().equalsIgnoreCase("Code / Description")) {

            building_text.setError(getString(R.string.error_field_required));
            focusView = building_text;
            cancel = true;
        }
        else{
            buildingStr = building_text.getText().toString().split("/");

        }


        if (TextUtils.isEmpty(unit_text.getText())|| unit_text.getText().toString().equalsIgnoreCase("Code / Description")) {
            unit_text.setError(getString(R.string.error_field_required));
            focusView = unit_text;
            cancel = true;

        }
        else{
            unitStr = unit_text.getText().toString().split("/");

        }

        if (TextUtils.isEmpty(contract_text.getText())|| contract_text.getText().toString().equalsIgnoreCase("Code / Description")) {

            contract_text.setError(getString(R.string.error_field_required));
            focusView = contract_text;
            cancel = true;

        }
        else{
            contractStr = contract_text.getText().toString().split("/");

        }
        if (TextUtils.isEmpty(scope_text.getText())|| scope_text.getText().toString().equalsIgnoreCase("Code / Description")) {
            scope_text.setError(getString(R.string.error_field_required));
            focusView = scope_text;
            cancel = true;
        }
        else{
            scopeStr = scope_text.getText().toString().split("/");

        }

        if (TextUtils.isEmpty(asset_text.getText())|| asset_text.getText().toString().equalsIgnoreCase("Code / Description")) {
            asset_text.setError(getString(R.string.error_field_required));
            focusView = asset_text;
            cancel = true;
        }
        else{
            assetStr = asset_text.getText().toString().split("/");;

        }
        if (TextUtils.isEmpty(main_text.getText())|| main_text.getText().toString().equalsIgnoreCase("Code / Description")) {
            main_text.setError(getString(R.string.error_field_required));
            focusView = main_text;
            cancel = true;

        }
        else{
            mainStr = main_text.getText().toString().split("/");

        }


        if(cancel){
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else{
            Save s = new Save(locationStr[0],buildingStr[0],unitStr[0],contractStr[0],scopeStr[0],assetStr[0],mainStr[0],remarks);
            s.execute();
        }



    }


    public void fillLBUCSAList(List<XmlData> list, int type) {

        pbar.dismiss();
        if (type == 0) {
            this.locationList = list;
            searchLocation();

        } else if (type == 1) {
            this.buildingList = list;
            searchBuilding();


        } else if (type == 2) {
            this.unitList = list;
            searchUnit();

        }
        else if (type == 3) {
            this.contractList = list;
            searchContract();

        }
        else if (type == 4) {
            this.scopeList = list;

            searchScope();
        }
        else if (type == 5) {
            this.assetList = list;
            searchAsset();

        }
        else if (type == 6) {
            this.mainList = list;
            searchMaintenance();


        }

    }


    public void searchLocation() {
        mDatabyCode1 = new HashMap<>();
        mDatabyDes1 = new HashMap<>();

        ArrayList<String> code_list_1 = new ArrayList<>();


        for (XmlData current : locationList) {
            code_list_1.add(current.code);
            code_list_1.add(current.description);


            mDatabyCode1.put(current.code, new XmlData(current.code, current.description));
            mDatabyDes1.put(current.description, new XmlData(current.code, current.description));


        }


        search_view1 = (AutoCompleteTextView) findViewById(R.id.search_view);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, code_list_1);
        search_view1.setAdapter(adapter1);

        if(locationList.size() == 0){
            search_view1.setHint("Location is not available.");
            search_view1.setHintTextColor(ContextCompat.getColor(this,R.color.errorColor));
        }
        else{
            search_view1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                    if (mDatabyDes1.containsKey(search_view1.getText().toString())) {
                        location_text.setText(mDatabyDes1.get(search_view1.getText().toString()).code + " / " + mDatabyDes1.get(search_view1.getText().toString()).description);

                        pbar.show();
                        GetLBUCSAMList getLBUCSAMList = new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, mDatabyDes1.get(search_view1.getText().toString()).code, null, 1, null, null, sp.getString(getString(R.string.company_code), "001"));

                        getLBUCSAMList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        building_text.setText("Code / Description");
                        contract_text.setText("Code / Description");
                        unit_text.setText("Code / Description");
                        scope_text.setText("Code / Description");

                    } else if (mDatabyCode1.containsKey(search_view1.getText().toString())) {
                        location_text.setText(mDatabyCode1.get(search_view1.getText().toString()).code + " / " + mDatabyCode1.get(search_view1.getText().toString()).description);

                        pbar.show();

                        GetLBUCSAMList getLBUCSAMList = new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, mDatabyCode1.get(search_view1.getText().toString()).code, null, 1, null, null, sp.getString(getString(R.string.company_code), "001"));

                        getLBUCSAMList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        building_text.setText("Code / Description");

                        contract_text.setText("Code / Description");
                        unit_text.setText("Code / Description");
                        scope_text.setText("Code / Description");

                    } else if (search_view1.getText().toString().isEmpty()) {
                        location_text.setText("Code / Description");
                        building_text.setText("Code / Description");
                        contract_text.setText("Code / Description");
                        unit_text.setText("Code / Description");
                        scope_text.setText("Code / Description");

                    }

                }
            });

        }



    }

    public void searchBuilding() {


        mDatabyDes2 = new HashMap<>();
        mDatabyCode2 = new HashMap<>();


        ArrayList<String> code_list_2 = new ArrayList<>();

        for (XmlData current : buildingList) {
            code_list_2.add(current.code);
            code_list_2.add(current.description);


            mDatabyCode2.put(current.code, new XmlData(current.code, current.description));
            mDatabyDes2.put(current.description, new XmlData(current.code, current.description));


        }
        search_view2 = (AutoCompleteTextView) findViewById(R.id.search_view2);


        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, code_list_2);
        search_view2.setAdapter(adapter2);

        if(buildingList.size() == 0){
            search_view2.setHint("Building is not available.");
            search_view2.setHintTextColor(ContextCompat.getColor(this,R.color.errorColor));
        }
        else{
            search_view2.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if (mDatabyDes2.containsKey(search_view2.getText().toString())) {
                        building_text.setText(mDatabyDes2.get(search_view2.getText().toString()).code + " / " + mDatabyDes2.get(search_view2.getText().toString()).description);

                        pbar.show();


                        String[] locStr = location_text.getText().toString().split("/");

                        GetLBUCSAMList getLBUCSAMList = new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, locStr[0], null, 2, mDatabyDes2.get(search_view2.getText().toString()).code, null, sp.getString(getString(R.string.company_code), "001"));

                        getLBUCSAMList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        unit_text.setText("Code / Description");
                        contract_text.setText("Code / Description");
                        scope_text.setText("Code / Description");


                    }
                    else if (mDatabyCode2.containsKey(search_view2.getText().toString())) {
                        building_text.setText(mDatabyCode2.get(search_view2.getText().toString()).code + " / " + mDatabyCode2.get(search_view2.getText().toString()).description);
                        pbar.show();
                        String[] locStr = location_text.getText().toString().split("/");

                        GetLBUCSAMList getLBUCSAMList = new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, locStr[0], null, 2, mDatabyCode2.get(search_view2.getText().toString()).code, null, sp.getString(getString(R.string.company_code), "001"));

                        getLBUCSAMList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        unit_text.setText("Code / Description");
                        contract_text.setText("Code / Description");
                        scope_text.setText("Code / Description");

                    }
                    else if (search_view2.getText().toString().isEmpty()) {
                        building_text.setText("Code / Description");
                        unit_text.setText("Code / Description");
                        contract_text.setText("Code / Description");
                        scope_text.setText("Code / Description");

                    }

                }
            });
        }



    }
    public void searchUnit() {


        mDatabyCode3 = new HashMap<>();
        mDatabyDes3 = new HashMap<>();

        ArrayList<String> code_list_3 = new ArrayList<>();

        for (XmlData current : unitList) {
            code_list_3.add(current.code);
            code_list_3.add(current.description);


            mDatabyCode3.put(current.code, new XmlData(current.code, current.description));
            mDatabyDes3.put(current.description, new XmlData(current.code, current.description));


        }
        search_view3 = (AutoCompleteTextView) findViewById(R.id.search_view3);

        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, code_list_3);
        search_view3.setAdapter(adapter3);


        if(unitList.size() == 0){
            search_view3.setHint("Unit is not available.");
            search_view3.setHintTextColor(ContextCompat.getColor(this,R.color.errorColor));
        }
        else{
            search_view3.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                    if (mDatabyDes3.containsKey(search_view3.getText().toString())) {
                        unit_text.setText(mDatabyDes3.get(search_view3.getText().toString()).code + " / " + mDatabyDes3.get(search_view3.getText().toString()).description);

                        pbar.show();
                        String[] locStr = location_text.getText().toString().split("/");
                        String[] buildStr = building_text.getText().toString().split("/");

                        GetLBUCSAMList getLBUCSAMList = new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, locStr[0], null, 3, buildStr[0], null, sp.getString(getString(R.string.company_code), "001"));

                        getLBUCSAMList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                    } else if (mDatabyCode3.containsKey(search_view3.getText().toString())) {
                        unit_text.setText(mDatabyCode3.get(search_view3.getText().toString()).code + " / " + mDatabyCode3.get(search_view3.getText().toString()).description);

                        pbar.show();
                        String[] locStr = location_text.getText().toString().split("/");
                        String[] buildStr = building_text.getText().toString().split("/");

                        GetLBUCSAMList getLBUCSAMList = new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, locStr[0],null, 3, buildStr[0], null, sp.getString(getString(R.string.company_code), "001"));

                        getLBUCSAMList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                    } else if (search_view3.getText().toString().isEmpty()) {
                        unit_text.setText("");

                    }

                }
            });
        }



    }

    public void searchContract() {

        mDatabyDes4 = new HashMap<>();
        mDatabyCode4 = new HashMap<>();

        ArrayList<String> code_list_4 = new ArrayList<>();

        for (XmlData current : contractList) {
            code_list_4.add(current.code);
            code_list_4.add(current.description);


            mDatabyCode4.put(current.code, new XmlData(current.code, current.description));
            mDatabyDes4.put(current.description, new XmlData(current.code, current.description));


        }
        search_view4 = (AutoCompleteTextView) findViewById(R.id.search_view4);


        ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, code_list_4);
        search_view4.setAdapter(adapter4);

        if(contractList.size() == 0){
            search_view4.setHint("Contract is not available.");
            search_view4.setHintTextColor(ContextCompat.getColor(this,R.color.errorColor));
        }
        else{
            search_view4.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                    if (mDatabyDes4.containsKey(search_view4.getText().toString())) {
                        contract_text.setText(mDatabyDes4.get(search_view4.getText().toString()).code + " / " + mDatabyDes4.get(search_view4.getText().toString()).description);


                    } else if (mDatabyCode4.containsKey(search_view4.getText().toString())) {
                        contract_text.setText(mDatabyCode4.get(search_view4.getText().toString()).code + " / " + mDatabyCode4.get(search_view4.getText().toString()).description);


                    } else if (search_view4.getText().toString().isEmpty()) {
                        contract_text.setText("");

                    }

                }
            });
        }



    }


    public void searchScope() {

        mDatabyDes5 = new HashMap<>();
        mDatabyCode5 = new HashMap<>();

        ArrayList<String> code_list_5 = new ArrayList<>();

        for (XmlData current : scopeList) {
            code_list_5.add(current.code);
            code_list_5.add(current.description);


            mDatabyCode5.put(current.code, new XmlData(current.code, current.description));
            mDatabyDes5.put(current.description, new XmlData(current.code, current.description));


        }
        search_view5 = (AutoCompleteTextView) findViewById(R.id.search_view5);
        ArrayAdapter<String> adapter5 = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, code_list_5);
        search_view5.setAdapter(adapter5);

        if(scopeList.size() == 0){
            search_view5.setHint("Scope is not available.");
            search_view5.setHintTextColor(ContextCompat.getColor(this,R.color.errorColor));
        }
        else{
            search_view5.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                    if (mDatabyDes5.containsKey(search_view5.getText().toString())) {
                        scope_text.setText(mDatabyDes5.get(search_view5.getText().toString()).code + " / " + mDatabyDes5.get(search_view5.getText().toString()).description);

                        pbar.show();

                        GetLBUCSAMList getLBUCSAMList = new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, null, null, 5, null, mDatabyDes5.get(search_view5.getText().toString()).code, sp.getString(getString(R.string.company_code), "001"));

                        getLBUCSAMList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                        GetLBUCSAMList getLBUCSAMList2 = new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, null, null, 6, null, mDatabyDes5.get(search_view5.getText().toString()).code, sp.getString(getString(R.string.company_code), "001"));

                        getLBUCSAMList2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                        asset_text.setText("Code / Description");
                        main_text.setText("Code / Description");


                    } else if (mDatabyCode5.containsKey(search_view5.getText().toString())) {
                        scope_text.setText(mDatabyCode5.get(search_view5.getText().toString()).code + " / " + mDatabyCode5.get(search_view5.getText().toString()).description);

                        pbar.show();

                        GetLBUCSAMList getLBUCSAMList = new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, null, null, 5, null, mDatabyCode5.get(search_view5.getText().toString()).code, sp.getString(getString(R.string.company_code), "001"));

                        getLBUCSAMList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        GetLBUCSAMList getLBUCSAMList2 = new GetLBUCSAMList(RandomTaskActivity.this, RandomTaskActivity.this, pbar, null, null, 6, null, mDatabyCode5.get(search_view5.getText().toString()).code, sp.getString(getString(R.string.company_code), "001"));

                        getLBUCSAMList2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        asset_text.setText("Code / Description");
                        main_text.setText("Code / Description");

                    } else if (search_view5.getText().toString().isEmpty()) {
                        scope_text.setText("Code / Description");
                        asset_text.setText("Code / Description");
                        main_text.setText("Code / Description");

                    }

                }
            });
        }


    }

    public void searchAsset() {


        mDatabyDes6 = new HashMap<>();
        mDatabyCode6 = new HashMap<>();


        ArrayList<String> code_list_6 = new ArrayList<>();

        for (XmlData current : assetList) {
            code_list_6.add(current.code);
            code_list_6.add(current.description);


            mDatabyCode6.put(current.code, new XmlData(current.code, current.description));
            mDatabyDes6.put(current.description, new XmlData(current.code, current.description));


        }
        search_view6 = (AutoCompleteTextView) findViewById(R.id.search_view6);


        ArrayAdapter<String> adapter6 = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, code_list_6);
        search_view6.setAdapter(adapter6);



        if(assetList.size() == 0){
            search_view6.setHint("Asset is not available.");
            search_view6.setHintTextColor(ContextCompat.getColor(this,R.color.errorColor));
        }

        else{
            search_view6.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                    if (mDatabyDes6.containsKey(search_view6.getText().toString())) {
                        asset_text.setText(mDatabyDes6.get(search_view6.getText().toString()).code + " / " + mDatabyDes6.get(search_view6.getText().toString()).description);


                    } else if (mDatabyCode6.containsKey(search_view6.getText().toString())) {
                        asset_text.setText(mDatabyCode6.get(search_view6.getText().toString()).code + " / " + mDatabyCode6.get(search_view6.getText().toString()).description);


                    } else if (search_view6.getText().toString().isEmpty()) {
                        asset_text.setText("");

                    }

                }
            });

        }


    }
    public void searchMaintenance() {


        mDatabyCode7 = new HashMap<>();
        mDatabyDes7 = new HashMap<>();

        ArrayList<String> code_list_7 = new ArrayList<>();


        for (XmlData current : mainList) {
            code_list_7.add(current.code);
            code_list_7.add(current.description);


            mDatabyCode7.put(current.code, new XmlData(current.code, current.description));
            mDatabyDes7.put(current.description, new XmlData(current.code, current.description));


        }
        search_view7 = (AutoCompleteTextView) findViewById(R.id.search_view7);

        ArrayAdapter<String> adapter7 = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, code_list_7);
        search_view7.setAdapter(adapter7);


        if(mainList.size() == 0){
            search_view7.setHint("Maintenance Code is not available.");
            search_view7.setHintTextColor(ContextCompat.getColor(this,R.color.errorColor));
        }
        else{
            search_view7.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                    if (mDatabyDes7.containsKey(search_view7.getText().toString())) {
                        main_text.setText(mDatabyDes7.get(search_view7.getText().toString()).code + " / " + mDatabyDes7.get(search_view7.getText().toString()).description);

                    } else if (mDatabyCode7.containsKey(search_view7.getText().toString())) {
                        main_text.setText(mDatabyCode7.get(search_view7.getText().toString()).code + " / " + mDatabyCode7.get(search_view7.getText().toString()).description);


                    } else if (search_view7.getText().toString().isEmpty()) {
                        main_text.setText("");

                    }

                }
            });
        }


    }

   /* @Override
    public void handleResult(Result result) {


    *//*    if(data.getIntExtra("type",0) == 0){

            location_text.setText(result.getString("locationCode") + " / "+obj.getString("locationDesc"));
            building_text.setText(result.getString("buildingCode") + " / "+obj.getString("buildingDesc"));
            unit_text.setText(obj.getString("unitCode") + " / "+obj.getString("unitDesc"));

        }
        else if(data.getIntExtra("type",0) == 1){
            asset_text.setText(obj.getString("assetCode") + " / "+obj.getString("assetDesc"));

        }*//*

        // Do something with the result here

        JSONObject obj = null;
        String locationCode ="",locationDesc="",buildingCode="",buildingDesc="",unitCode="",unitDesc="";
        try {
            obj = new JSONObject(result.getText());


             locationCode = obj.getString("locationCode");
             locationDesc = obj.getString("locationDesc");
             buildingCode = obj.getString("buildingCode");
             buildingDesc = obj.getString("buildingDesc");
             unitCode = obj.getString("unitCode");
             unitDesc = obj.getString("unitDesc");

            if(locationCode!=""){
                unitScanDetails = new ArrayList<>();
                unitScanDetails.add(0,new XmlData(locationCode,locationDesc));
                unitScanDetails.add(1,new XmlData(buildingCode,buildingDesc));
                unitScanDetails.add(2,new XmlData(unitCode,unitDesc));

            }


            mScannerView.stopCamera();
            mScannerView.removeAllViews();



        } catch (JSONException e) {
            e.printStackTrace();
        }



       *//* Log.v("TAG", result.getText()); // Prints scan results
        // Prints the scan format (qrcode, pdf417 etc.)
        Log.v("TAG", result.getBarcodeFormat().toString());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setMessage(locationCode +" / "+ locationDesc  +" == " +buildingCode +"/"+buildingDesc +"==="+unitCode+"/"+unitDesc);
        AlertDialog alert1 = builder.create();
        alert1.show();*//*

    }*/

    /*public void QrScanner(View view){
       mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view<br />
        setContentView(mScannerView);
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.<br />
        mScannerView.startCamera();         // Start camera<br />
    }

    @Override
    public void onPause() {
            super.onPause();
            mScannerView.stopCamera();   // Stop camera on pause<br />
    }

    @Override
    public void onResume(){
        super.onResume();

        if(unitScanDetails != null){
            if(unitScanDetails.get(0).code !=null) {
                location_text.setText(unitScanDetails.get(0).code +" / "+unitScanDetails.get(0).description);

            }
            if(unitScanDetails.get(1).code !=null) {
                building_text.setText(unitScanDetails.get(1).code +" / "+unitScanDetails.get(1).description);

            }

            if(unitScanDetails.get(2).code !=null) {
                unit_text.setText(unitScanDetails.get(2).code +" / "+unitScanDetails.get(2).description);

            }

            }

    }*/



    public class Save extends AsyncTask<String, String, String> {


        String  z;
        String locationCode,  buildingCode, unitCode, contractNo
                , scopeCode ,assetCode,  mainCode, service;

        boolean isSuccess;

        public Save(String locationCode, String buildingCode, String unitCode, String contractNo
                , String scopeCode ,String assetCode, String mainCode, String service) {

            this.locationCode = locationCode;
            this.buildingCode = buildingCode;
            this.unitCode = unitCode;
            this.contractNo = contractNo;

            this.scopeCode = scopeCode;
            this.assetCode = assetCode;
            this.mainCode = mainCode;
            this.service = service;


        }

        public void onPreExecute() {
            pbar.show();
        }

        public void onPostExecute(String s) {
            pbar.dismiss();

            if (isSuccess) {
                Toast.makeText(RandomTaskActivity.this, "Random Task has been created.!! ", Toast.LENGTH_SHORT).show();

                RandomTaskActivity.this.finish();

            } else {
                Toast.makeText(RandomTaskActivity.this, "Database Error,please check your connection", Toast.LENGTH_SHORT).show();

            }


        }


        @Override
        protected String doInBackground(String... params) {
            PropertyInfo lCode, bCode,empCode, uCode, compCode, cCode, sCode, aCode,mCode,remarks, userCd, userName;

            request = new SoapObject(NameSpace, "createRandomTask");




            lCode = new PropertyInfo();
            lCode.setName("locationCode");
            lCode.setType(String.class);
            lCode.setValue(locationCode);
            request.addProperty(lCode);

            bCode = new PropertyInfo();
            bCode.setName("buildingNo");
            bCode.setType(String.class);
            bCode.setValue(buildingCode);
            request.addProperty(bCode);

            uCode = new PropertyInfo();
            uCode.setName("unitCode");
            uCode.setType(String.class);
            uCode.setValue(unitCode);
            request.addProperty(uCode);


            cCode = new PropertyInfo();
            cCode.setName("contractNo");
            cCode.setType(String.class);
            cCode.setValue(contractNo);
            request.addProperty(cCode);


            sCode = new PropertyInfo();
            sCode.setName("scopeCode");
            sCode.setType(String.class);
            sCode.setValue(scopeCode);
            request.addProperty(sCode);

            aCode = new PropertyInfo();
            aCode.setName("assetCode");
            aCode.setType(String.class);
            aCode.setValue(assetCode);
            request.addProperty(aCode);

            compCode = new PropertyInfo();
            compCode.setName("compCode");
            compCode.setType(String.class);
            compCode.setValue(sp.getString(getString(R.string.company_code), ""));
            request.addProperty(compCode);

            mCode = new PropertyInfo();
            mCode.setName("maintenanceCode");
            mCode.setType(String.class);
            mCode.setValue(mainCode);
            request.addProperty(mCode);


            remarks = new PropertyInfo();
            remarks.setName("remarks");
            remarks.setType(String.class);
            remarks.setValue(service);
            request.addProperty(remarks);

            userCd = new PropertyInfo();
            userCd.setName("userCode");
            userCd.setType(String.class);
            userCd.setValue(sp.getString(getString(R.string.user_cd),""));
            request.addProperty(userCd);

            userName = new PropertyInfo();
            userName.setName("userName");
            userName.setType(String.class);
            userName.setValue(sp.getString(getString(R.string.user_name),""));
            request.addProperty(userName);



            empCode = new PropertyInfo();
            empCode.setName("empCode");
            empCode.setType(String.class);
            empCode.setValue(sp.getString(getString(R.string.employee_code),""));
            request.addProperty(empCode);


            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.setOutputSoapObject(request);
            envelope.dotNet = true;
            transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;

            try {

                transportSE.call(Soap_Action + "createRandomTask", envelope);
                SoapObject result = (SoapObject) envelope.getResponse();
                if (result.getPropertyCount() != 0 && result.getProperty(0).toString().equalsIgnoreCase("saved")) {
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
