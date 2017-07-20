package com.example.sachin.fms.activities;

import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.sachin.fms.R;
import com.example.sachin.fms.classes.DatePicker;
import com.example.sachin.fms.classes.DrawCanvas;
import com.example.sachin.fms.classes.Logout;
import com.example.sachin.fms.classes.RunTimePermission;
import com.example.sachin.fms.classes.TimePicker;
import com.example.sachin.fms.dataSets.TaskListData;
import com.example.sachin.fms.others.WebServiceConnection;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class CloseActivity extends AppCompatActivity {
    DrawCanvas sign, draw;


    private Button btn, save;
    private EditText date, time, text;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private SharedPreferences sp;
    private ProgressDialog pdialog;
    private LinearLayout scroll;
    private Bitmap bitmap;
    private int activity;
    private String taskId, call_id;
    private String encodedString;

    private RatingBar ratingbar1, ratingbar2, ratingbar3, ratingbar4;

    /**
     * objects of Web services
     *
     * @param savedInstanceState
     */

    private String NameSpace;
    private String Soap_Action;
    private String URL;
    private WebServiceConnection connection;

    private SoapSerializationEnvelope envelope, envelope2;
    private SoapObject request, request2;
    private SoapPrimitive response;
    private HttpTransportSE transportSE;
    private TaskListData listData = new TaskListData();


    private FloatingActionButton fab, fab1, fab2;
    private Animation show_fab1, hide_fab1, show_fab2, hide_fab2;
    private boolean FAB_status = false;
    private Bundle b;

    private RunTimePermission rp;
    private Dialog feedBackDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_close);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        connection = new WebServiceConnection();
        NameSpace = connection.NameSpace;
        Soap_Action = connection.Soap_Action;
        URL = connection.URL;


        b = getIntent().getExtras();
        sp = this.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        taskId = sp.getString(getString(R.string.task_number), "null");
        call_id = sp.getString(getString(R.string.call_number), "null");

        ActionBar actionBar;
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Task No. " + taskId);
        }

        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }


        pdialog = new ProgressDialog(this);
        pdialog.setMessage("Saving...");
        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdialog.setIndeterminate(true);
        pdialog.setCancelable(false);



        sign = (DrawCanvas) findViewById(R.id.drawing);
        btn = (Button) findViewById(R.id.button);



        date = (EditText) findViewById(R.id.current_date);
        text = (EditText) findViewById(R.id.before_work_description);
        time = (EditText) findViewById(R.id.current_time);

        save = (Button) findViewById(R.id.before_work_save_btn);
        scroll = (LinearLayout) findViewById(R.id.scroll);
        activity = b.getInt("activity");
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
        final Date d = new Date();
        String current = dateFormat.format(d);
        String[] str = current.split(" ");

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab_1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab_2);
        //fab3 = (FloatingActionButton) findViewById(R.id.fab_3);

        show_fab1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_fab_1);
        hide_fab1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_fab_1);
        show_fab2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_fab_2);
        hide_fab2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_fab_2);

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logout logout = new Logout(CloseActivity.this, sp);
                logout.execute();


                finish();

            }
        });


        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(CloseActivity.this, LandingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
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
        date.setText(str[0]);
        time.setText(str[1]);

        date.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);

                return false;
            }
        });

        text.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);

                return false;
            }
        });


        date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                    datePicker = new DatePicker(v);
                    FragmentTransaction F = getFragmentManager().beginTransaction();
                    datePicker.show(F, "DatePicker");

                }
            }
        });

        time.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);

                return false;
            }
        });

        time.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    timePicker = new TimePicker(v);
                    FragmentTransaction F = getFragmentManager().beginTransaction();
                    timePicker.show(F, "TimePicker");
                }

            }
        });




        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                sign.ClearCanvas();



            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                HashMap<String, Bitmap> _list;
                _list = sign.getBitmap();

                bitmap = _list.get("sign");

                if (bitmap.sameAs(_list.get("empty"))) {
                    Toast.makeText(CloseActivity.this, "Please take the signature before closing the task", Toast.LENGTH_LONG).show();


                } else {


                    byte[] b = sign.getBytes();

                    encodedString = Base64.encodeToString(b, Base64.DEFAULT);


                    feedBackDialog = new Dialog(CloseActivity.this);
                    feedBackDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    feedBackDialog.setContentView(R.layout.feedback_dialog_layout);
                    feedBackDialog.setCancelable(false);
                    feedBackDialog.setCanceledOnTouchOutside(false);
                    feedBackDialog.show();





                    ratingbar1 = (RatingBar) feedBackDialog.findViewById(R.id.rating_bar1);
                    ratingbar2 = (RatingBar) feedBackDialog.findViewById(R.id.rating_bar2);
                    ratingbar3 = (RatingBar) feedBackDialog.findViewById(R.id.rating_bar3);
                    ratingbar4 = (RatingBar) feedBackDialog.findViewById(R.id.rating_bar4);

                    Button save_feedback = (Button) feedBackDialog.findViewById(R.id.save_feedback);

                    save_feedback.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            String rating = String.valueOf(ratingbar1.getRating());
                            String rating2 = String.valueOf(ratingbar2.getRating());
                            String rating3 = String.valueOf(ratingbar3.getRating());
                            String rating4 = String.valueOf(ratingbar4.getRating());


                            if (rating.equalsIgnoreCase("0") || rating2.equalsIgnoreCase("0") || rating3.equalsIgnoreCase("0") || rating4.equalsIgnoreCase("0")) {
                                Toast.makeText(getApplicationContext(), "Please rate our service.", Toast.LENGTH_LONG).show();
                            } else {
                                SaveBeforeTask save = new SaveBeforeTask(activity, getRatingCodeFromString(rating), getRatingCodeFromString(rating2), getRatingCodeFromString(rating3), getRatingCodeFromString(rating4));
                                save.execute();
                            }


                        }

                    });


                }


            }
        });
        rp = new RunTimePermission(this, this.getCurrentFocus());

        if (rp.checkBuildVersion()) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

        }


    }


    public String getRatingCodeFromString(String str) {

        String rate;
        switch (str) {

            case "1.0":
                rate = "V";
                break;
            case "2.0":
                rate = "P";
                break;
            case "3.0":
                rate = "A";
                break;
            case "4.0":
                rate = "G";
                break;
            case "5.0":
                rate = "E";
                break;
            default:
                rate = "V";
                break;

        }
        return rate;

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

        Log.e("reported date formatted", sp.getString(getString(R.string.reported_time), "null"));



    }

    public class SaveBeforeTask extends AsyncTask<String, String, String> {
        int x;
        String remarks = text.getText().toString();

        String img_type, resp, quality, speed, attitude;
        String feedback;

        String z = "";
        boolean isSuccess = false;
        boolean isSuccess_1 = false;

        public SaveBeforeTask(int x, String quality, String speed, String resp, String attitude) {
            this.x = x;
            this.quality = quality;
            this.speed = speed;
            this.resp = resp;
            this.attitude = attitude;
        }

        @Override
        public void onPreExecute() {
            pdialog.show();

        }

        @Override
        public void onPostExecute(String r) {
            pdialog.dismiss();
            if (isSuccess_1 && z.equalsIgnoreCase("Saved")) {

                feedBackDialog.dismiss();
                Toast.makeText(CloseActivity.this, "Task has been Completed!", Toast.LENGTH_SHORT).show();
                CloseActivity.this.finish();

            } else {
                Toast.makeText(CloseActivity.this, z, Toast.LENGTH_SHORT).show();

            }

        }

        @Override
        protected String doInBackground(String... params) {
            img_type = "C";


            feedback = quality + "," + speed + "," + resp + "," + attitude;
            PropertyInfo fb, call_no, taskid, compcode, imagetype, remark, Value, userCode;

            {
                request = new SoapObject(NameSpace, "closeTask");

                call_no = new PropertyInfo();
                call_no.setName("call_no");
                call_no.setType(String.class);
                call_no.setValue(call_id);
                request.addProperty(call_no);


                taskid = new PropertyInfo();
                taskid.setName("task_id");
                taskid.setType(String.class);
                taskid.setValue(taskId);
                request.addProperty(taskid);


                compcode = new PropertyInfo();
                compcode.setName("compcode");
                compcode.setType(String.class);
                compcode.setValue(sp.getString(getString(R.string.company_code), ""));
                request.addProperty(compcode);

                imagetype = new PropertyInfo();
                imagetype.setName("imagetype");
                imagetype.setType(String.class);
                imagetype.setValue(img_type);
                request.addProperty(imagetype);


                userCode = new PropertyInfo();
                userCode.setName("userCode");
                userCode.setType(String.class);
                userCode.setValue(sp.getString(getString(R.string.user_cd), ""));
                request.addProperty(userCode);


                remark = new PropertyInfo();
                remark.setName("remarks");
                remark.setType(String.class);
                remark.setValue(remarks);
                request.addProperty(remark);

                Value = new PropertyInfo();
                Value.setName("Value");
                Value.setType(String.class);
                Value.setValue(encodedString);
                request.addProperty(Value);


                fb = new PropertyInfo();
                fb.setName("feedback");
                fb.setType(String.class);
                fb.setValue(feedback);
                request.addProperty(fb);

                envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);
                envelope.dotNet = true;
                transportSE = new HttpTransportSE(URL);
                transportSE.debug = true;

                try {


                    transportSE.call(Soap_Action + "closeTask", envelope);
                    SoapObject result = (SoapObject) envelope.getResponse();
                    if (result.getPropertyCount() != 0 && result.getProperty(0).toString().equalsIgnoreCase("Saved")) {
                        z = result.getProperty(0).toString();
                        isSuccess_1 = true;

                    } else {
                        isSuccess_1 = false;
                    }


                } catch (Exception e) {

                }
            }

            return z;
        }
    }

}