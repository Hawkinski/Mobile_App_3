package com.example.sachin.fms.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sachin.fms.R;
import com.example.sachin.fms.classes.DownloadFile;
import com.example.sachin.fms.classes.RunTimePermission;
import com.example.sachin.fms.dataSets.PermissionData;
import com.example.sachin.fms.dataSets.XmlData;
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
import java.util.HashMap;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final String TAG = "FirebaseIDService";
    private Consume mAuthTask = null;
    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private boolean saved = false;
    private View mProgressView;
    private View mLoginFormView;
    private SharedPreferences sp, sp1;
    private HashMap<String, String> data = new HashMap<>();
    private String email;
    private String password;
    private List<XmlData> userInfo;
    private employee emp = new employee();
    //Web service connection reference
    private String NameSpace;
    private String URL, URL3;

    private String Soap_Action;
    private WebServiceConnection connection;
    private RunTimePermission rp;
    private String[] permissionGroup;

    private SoapSerializationEnvelope envelope, envelope2;
    private SoapObject request, request2;
    private HttpTransportSE transportSE;
    private ProgressDialog progressDialog;
    private List<PermissionData> permissionDataList;

    private View root_view;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        root_view = findViewById(R.id.root_view);

        mEmailView = (EditText) findViewById(R.id.email);

        connection = new WebServiceConnection();
        NameSpace = connection.NameSpace;
        Soap_Action = connection.Soap_Action;
        URL = connection.URL;
        URL3 = connection.URL3;
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });


        sp1 = LoginActivity.this.getSharedPreferences("pref", MODE_PRIVATE);

        if (sp1.getString("temp", null) != null) {

            mEmailView.setText(sp1.getString("temp", null));

        }

        rp = new RunTimePermission(this, this.getCurrentFocus());


        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        assert mEmailSignInButton != null;
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });


        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        if (saved) {
            mEmailView.setText(sp.getString(getString(R.string.user_cd), " "));
            mPasswordView.setText(sp.getString(getString(R.string.user_pwd), " "));

        }

        if (rp.checkBuildVersion()) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            permissionGroup = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE


            };
            permissionDataList = new ArrayList<>();
            permissionDataList = rp.checkPermission(permissionGroup);
            if (permissionDataList.size() == 1 && permissionDataList.size() != 0) {
                if (!permissionDataList.get(0).isGranted) {

                    rp.requestPermission(this, new String[]{permissionDataList.get(0).code});

                }

            } else {
                List<String> tempList = new ArrayList<>();
                String[] str = null;

                for (int i = 0; i < permissionDataList.size(); i++) {
                    if (!permissionDataList.get(i).isGranted) {
                        tempList.add(permissionDataList.get(i).code);
                    }

                }
                if (tempList.size() != 0) {
                    str = tempList.toArray(new String[0]);
                    rp.requestPermission(this, str);


                }

            }
        }


    }
    /**
     * Callback received when a permissions request has been completed.
     */


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.


            ConnectivityManager cm = (ConnectivityManager) LoginActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info == null) {
                Snackbar.make(root_view, "Internet connection is not available", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null).show();
            } else {


                Consume c = new Consume(email, password);
                c.execute();
            }


            //mAuthTask = new UserLoginTask(email, password);
            //mAuthTask.execute();
        }
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit").setMessage("Are you Sure you want to exit?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LoginActivity.super.onBackPressed();
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }).create().show();


    }

    public boolean Parser(String xml) throws XmlPullParserException, IOException {

        boolean isSuccess = false;
        ArrayList<employee> products = null;
        employee current = null;
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

                    // Log.e("tag name", name);


                    if (name.equals("Table")) {
                        current = new employee();
                    } else if (current != null) {
                        switch (name) {
                            case "USER_PWD":
                                current.pwd = (parser.nextText());

                                break;
                            case "MOBUSR_MAIL":
                                current.mail = (parser.nextText());

                                break;
                            case "USER_CD":
                                current.cd = (parser.nextText());

                                break;
                            case "USER_NAME":
                                current.emp_name = (parser.nextText());

                                break;
                            case "EMP_CODE":
                                current.emp_code = (parser.nextText());

                                break;
                            case "COMP_CODE":
                                current.comp_code = (parser.nextText());

                                break;
                            case "MOBUSR_DESG":
                                current.emp_desg = (parser.nextText());

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
            isSuccess = printProducts(products);
        }
        return isSuccess;


    }

    public boolean printProducts(ArrayList<employee> list) {

        List<employee> taskcount = new ArrayList<>();

        boolean isSuccess = false;
        for (employee current : list) {
            taskcount.add(new employee(current.emp_name, current.cd, current.pwd, current.mail, current.emp_code));
            emp.emp_name = current.emp_name;
            emp.pwd = current.pwd;
            emp.mail = current.mail;
            emp.cd = current.cd;
            emp.emp_code = current.emp_code;
            emp.emp_desg = current.emp_desg;
            emp.comp_code = current.comp_code;

            isSuccess = true;
        }

        return isSuccess;

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };


    }

    /**
     * Represents an asynchronous login/registration web service task used to authenticate
     * the user.
     */


    public class Consume extends AsyncTask<String, String, String> {

        private final String mEmail;
        private String mPassword, assign, completed, progress;

        private String z = "";
        private boolean isSuccess = false;
        private boolean isSuccess_1 = false;

        private String deviceId;

        public Consume(String email, String password) {
            mEmail = email;
            data.put("temp", mEmail);

            mPassword = password;

        }

        public void onPreExecute() {
            progressDialog.setMessage("Authenticating....");
            progressDialog.show();

            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = telephonyManager.getDeviceId();

        }

        public void onPostExecute(String s) {


            mAuthTask = null;
            progressDialog.hide();


            if (isSuccess && isSuccess_1) {

                //  if(checkBox.isChecked()){
                //saved=true;
                //   }
                //Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                sp = LoginActivity.this.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                SharedPreferences.Editor editor = sp1.edit();
                edit.clear();
                edit.putString(getString(R.string.user_cd), emp.cd);
                edit.putString(getString(R.string.employee_code), emp.emp_code);
                edit.putString(getString(R.string.user_name), emp.emp_name);
                edit.putString(getString(R.string.user_pwd), emp.pwd);
                edit.putString(getString(R.string.company_code), emp.comp_code);
                edit.putString(getString(R.string.employee_designation), emp.emp_desg);


                edit.putString(getString(R.string.assigned_code), assign);
                edit.putString(getString(R.string.work_completed_code), completed);
                edit.putString(getString(R.string.work_progress_code), progress);

                editor.putString("temp", data.get("temp"));


                editor.apply();
                edit.apply();

                String cd = sp.getString(getString(R.string.user_cd), null);

                if (cd != null) {
                    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                    Log.d(TAG, "Refreshed token: " + refreshedToken);

                    // TODO: Implement this method to send any registration to your app's servers.

                    SaveId save = new SaveId(refreshedToken);
                    save.execute();


                }


               /* Intent i = new Intent(LoginActivity.this, LandingActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();*/

            } else {
                mPasswordView.setError("wrong User id and password");
                mPasswordView.requestFocus();

            }
        }


        @Override
        protected String doInBackground(String... params) {

            PropertyInfo uname, pwd, comcode, mac;


            request = new SoapObject(NameSpace, "Login");

            uname = new PropertyInfo();
            uname.setName("userName");
            uname.setType(String.class);
            uname.setValue(email);
            request.addProperty(uname);

            pwd = new PropertyInfo();
            pwd.setName("password");
            pwd.setType(String.class);
            pwd.setValue(password);
            request.addProperty(pwd);

            mac = new PropertyInfo();
            mac.setName("deviceId");
            mac.setType(String.class);
            mac.setValue(deviceId);
            request.addProperty(mac);

            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            envelope2 = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope2.dotNet = true;

            envelope2.setOutputSoapObject(request2);
            transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;

            try {
                userInfo = new ArrayList<>();

                transportSE.call(Soap_Action + "Login", envelope);
                SoapObject result = (SoapObject) envelope.getResponse();


                //Log.e("employee", result.toString());

                if (transportSE.requestDump == null) {
                    isSuccess = false;
                    isSuccess_1 = false;
                } else {
                    isSuccess = Parser(transportSE.responseDump);

                }


                /*if(result.getPropertyCount()!=0 && !result.toString().equals("anyType{}")){
                    emp.emp_name= result.getProperty("name").toString();
                    emp.pwd= result.getProperty("pass").toString();
                    emp.mail= result.getProperty("email").toString();
                    emp.cd= result.getProperty("cd").toString();
                    emp.emp_code=result.getProperty("empcode").toString();
                    isSuccess=true;
                    z="Login Successful";

                }
                else {
                    z="wrong user name and password";
                    isSuccess=false;
                }*/


                if (isSuccess) {
                    request2 = new SoapObject(NameSpace, "StatusClass");

                    comcode = new PropertyInfo();
                    comcode.setName("comcode");
                    comcode.setType(String.class);
                    comcode.setValue(emp.comp_code);
                    request2.addProperty(comcode);
                    envelope2 = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope2.dotNet = true;

                    envelope2.setOutputSoapObject(request2);
                    transportSE = new HttpTransportSE(URL);
                    transportSE.call(Soap_Action + "StatusClass", envelope2);
                    SoapObject result2 = (SoapObject) envelope2.getResponse();
                    if (result2.getPropertyCount() != 0) {
                        assign = result2.getProperty(0).toString();
                        progress = result2.getProperty(1).toString();
                        completed = result2.getProperty(2).toString();
                        isSuccess_1 = true;
                        z = "Login Successful";
                    } else {
                        z = "wrong user name and password";
                        isSuccess_1 = false;
                    }
                } else {
                    z = "wrong user name and password";
                    isSuccess_1 = false;
                }


            } catch (HttpResponseException e) {
                e.printStackTrace();
                e.getMessage();
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
                z = e.getMessage();
            }

            return z;
        }
    }

    public class SaveId extends AsyncTask<String, String, String> {


        private String z = "";
        private String token;
        private boolean isSuccess = false;

        public SaveId(String token) {
            this.token = token;


        }

        public void onPreExecute() {
            progressDialog.setMessage("Redirecting....");
            progressDialog.show();


        }

        public void onPostExecute(String s) {


            if (isSuccess) {

                GetMaterialList get = new GetMaterialList();
                get.execute();


            } else {
                mPasswordView.setError("wrong User id and password");
                mPasswordView.requestFocus();

            }
        }


        @Override
        protected String doInBackground(String... params) {

            PropertyInfo empcode, mobid, compcode, flag;
            Log.d("COMPCODE", sp.getString(getString(R.string.company_code), "null"));
            Log.d("EMPCODE", sp.getString(getString(R.string.employee_code), "null"));

            request = new SoapObject(NameSpace, "notificationLogin");

            empcode = new PropertyInfo();
            empcode.setName("empcode");
            empcode.setType(String.class);
            empcode.setValue(sp.getString(getString(R.string.employee_code), ""));
            request.addProperty(empcode);

            mobid = new PropertyInfo();
            mobid.setName("id");
            mobid.setType(String.class);
            mobid.setValue(token);
            request.addProperty(mobid);


            compcode = new PropertyInfo();
            compcode.setName("compcode");
            compcode.setType(String.class);
            compcode.setValue(sp.getString(getString(R.string.company_code), ""));
            request.addProperty(compcode);

            flag = new PropertyInfo();
            flag.setName("flag");
            flag.setType(String.class);
            flag.setValue("1");
            request.addProperty(flag);

            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;

            try {
                Log.d("MOB>ID", token);

                transportSE.call(Soap_Action + "notificationLogin", envelope);
                SoapObject result = (SoapObject) envelope.getResponse();
                if (result.getPropertyCount() != 0 && result.getProperty(0).toString().equalsIgnoreCase("inserted")) {
                    Log.d("MOB>ID", token);
                    isSuccess = true;
                }


            } catch (Exception ex) {
                isSuccess = false;
                //   Log.e("MOB>ID","rooorrr");

            }

            return z;
        }
    }


    public class GetMaterialList extends AsyncTask<String, String, String> {


        String fileName;
        boolean isSuccess;

        public void onPreExecute() {

        }

        public void onPostExecute(String s) {

            if (s != null && isSuccess) {

                DownloadFile download = new DownloadFile(URL3 + s, "XML", s);
                download.execute();


                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                Intent i = new Intent(LoginActivity.this, LandingActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();

            } else {
                Toast.makeText(LoginActivity.this, "Failed to generate material list.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(
                        LoginActivity.this, LandingActivity.class);
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
                    isSuccess = true;
                }


            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }


            return fileName;
        }


    }


}





