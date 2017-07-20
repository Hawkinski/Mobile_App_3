package com.example.sachin.fms.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.sachin.fms.R;
import com.example.sachin.fms.adapterPackage.RVAdapter;
import com.example.sachin.fms.adapterPackage.ViewPagerAdapter;
import com.example.sachin.fms.classes.EndlessRecyclerViewScrollListener;
import com.example.sachin.fms.classes.Logout;
import com.example.sachin.fms.dataSets.TaskCount;
import com.example.sachin.fms.dataSets.TaskListData;
import com.example.sachin.fms.fragments.InspectionTaskFragment;
import com.example.sachin.fms.fragments.PPMTaskFragment;
import com.example.sachin.fms.fragments.RandomTaskFragment;
import com.example.sachin.fms.fragments.RegularTaskFragment;
import com.example.sachin.fms.others.WebServiceConnection;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
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

public class AssignedTaskActivity extends AppCompatActivity {

    private boolean FAB_status = false;
    private boolean isPaused = false;


    private SharedPreferences sp;

    private TextView tabText1, tabText2, tab2Text1, tab2Text2, tab3Text1, tab3Text2, tab4Text1, tab4Text2;

    private FloatingActionButton fab, fab1, fab2;
    private Animation show_fab1, hide_fab1, show_fab2, hide_fab2;
    /**
     * objects of Web services
     *
     * @param savedInstanceState
     */

    private String NameSpace;
    private String Soap_Action;
    private String URL;
    private WebServiceConnection connection;

    private SoapSerializationEnvelope envelope;
    private SoapObject request;
    private HttpTransportSE transportSE;
    private ProgressDialog pdialog;


    private TaskCount taskCount;
    private TabLayout tabLayout;
    private String regularCount, inspectionCount, PPMCount,RandomCount;

    public AssignedTaskActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assigned_task);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar;
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Assigned Task");
        }

        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            toolbar.inflateMenu(R.menu.menu);
        }
        sp = this.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);


        pdialog = new ProgressDialog(this);
        pdialog.setMessage("Loading...");
        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdialog.setIndeterminate(true);
        pdialog.setCancelable(false);


        connection = new WebServiceConnection();
        NameSpace = connection.NameSpace;
        Soap_Action = connection.Soap_Action;
        URL = connection.URL;
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Bundle b = getIntent().getExtras();

        regularCount = Integer.toString(b.getInt("count_1"));
        inspectionCount = Integer.toString(b.getInt("count_2"));
        PPMCount = Integer.toString(b.getInt("count_3"));
        RandomCount = Integer.toString(b.getInt("count_4"));



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
                Logout logout = new Logout(AssignedTaskActivity.this, sp);
                logout.execute();


                finish();

            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(AssignedTaskActivity.this, LandingActivity.class);
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


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        tabText1 = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabText2 = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab2, null);

        tab2Text1 = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tab2Text2 = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab2, null);


        tab3Text1 = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tab3Text2 = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab2, null);

        tab4Text1 = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tab4Text2 = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab2, null);


        tabText1.setText("Maintenance");
        tab2Text1.setText("Inspection");
        tab3Text1.setText("PPM");
        tab4Text1.setText("Random");



        setupTabIcons();


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                Log.d("tab change", "onTabSelected: pos: " + tab.getPosition());
                Window window = AssignedTaskActivity.this.getWindow();

                switch (tab.getPosition()) {
                    case 0:
                        tabLayout.setBackgroundColor(ContextCompat.getColor(AssignedTaskActivity.this, R.color.colorPrimary));
                        toolbar.setBackgroundColor(ContextCompat.getColor(AssignedTaskActivity.this, R.color.colorPrimary));
                        window = AssignedTaskActivity.this.getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            window.setStatusBarColor(ContextCompat.getColor(AssignedTaskActivity.this, R.color.colorPrimaryDark));
                        }
                        break;
                    case 1:
                        tabLayout.setBackgroundColor(ContextCompat.getColor(AssignedTaskActivity.this, R.color.primaryBlue));

                        toolbar.setBackgroundColor(ContextCompat.getColor(AssignedTaskActivity.this, R.color.primaryBlue));
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            window.setStatusBarColor(ContextCompat.getColor(AssignedTaskActivity.this, R.color.primaryDarkBlue));
                        }
                        break;
                    case 2:
                        tabLayout.setBackgroundColor(ContextCompat.getColor(AssignedTaskActivity.this, R.color.primaryRed));

                        toolbar.setBackgroundColor(ContextCompat.getColor(AssignedTaskActivity.this, R.color.primaryRed));
                        window = AssignedTaskActivity.this.getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            window.setStatusBarColor(ContextCompat.getColor(AssignedTaskActivity.this, R.color.primaryDarkRed));
                        }
                        break;
                    case 3:
                        tabLayout.setBackgroundColor(ContextCompat.getColor(AssignedTaskActivity.this, R.color.colorTeal));

                        toolbar.setBackgroundColor(ContextCompat.getColor(AssignedTaskActivity.this, R.color.colorTeal));
                        window = AssignedTaskActivity.this.getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            window.setStatusBarColor(ContextCompat.getColor(AssignedTaskActivity.this, R.color.colorDarkTeal));
                        }
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public AssignedTaskActivity getInstance() {
        return AssignedTaskActivity.this;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new RegularTaskFragment(sp, pdialog, 0), "Maintenance");
        adapter.addFragment(new InspectionTaskFragment(sp, pdialog, 0), "Inspection");
        adapter.addFragment(new PPMTaskFragment(sp, pdialog, 0), "PPM");
        adapter.addFragment(new RandomTaskFragment(sp, pdialog, 0), "Random");

        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {

        tabText2.setText(regularCount);

        tabLayout.getTabAt(0).setCustomView(tabText2);

        tab2Text2.setText(inspectionCount);
        tabLayout.getTabAt(1).setCustomView(tab2Text2);


        tab3Text2.setText(PPMCount);
        tabLayout.getTabAt(2).setCustomView(tab3Text2);

        tab4Text2.setText(RandomCount);
        tabLayout.getTabAt(3).setCustomView(tab4Text2);

        tabLayout.getTabAt(0).setCustomView(tabText1);
        tabLayout.getTabAt(1).setCustomView(tab2Text1);
        tabLayout.getTabAt(2).setCustomView(tab3Text1);
        tabLayout.getTabAt(3).setCustomView(tab4Text1);

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
    public void onPause() {
        super.onPause();
        isPaused = true;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (isPaused) {
            GetTaskCount get = new GetTaskCount();
            get.execute();
        }


    }

    public class GetTaskCount extends AsyncTask<String, String, String> {

        private boolean isSuccess_1 = false;

        public void onPreExecute() {
            pdialog.setMessage("Loading....");
            pdialog.show();

        }

        public void onPostExecute(String s) {

            if (isSuccess_1) {
                regularCount = Integer.toString(taskCount.regularCount);
                inspectionCount = Integer.toString(taskCount.inspectionCount);
                PPMCount = Integer.toString(taskCount.PPMCount);
                RandomCount = Integer.toString(taskCount.randomCount);

                setupTabIcons();


            }
        }


        @Override
        protected String doInBackground(String... params) {

            PropertyInfo compcode, acode, completedcode, workprogresscode, empcode, f, option;

            request = new SoapObject(NameSpace, "getTaskCount");

            compcode = new PropertyInfo();
            compcode.setName("compcode");
            compcode.setType(String.class);
            compcode.setValue(sp.getString(getString(R.string.company_code), ""));
            request.addProperty(compcode);

            acode = new PropertyInfo();
            acode.setName("assignedcode");
            acode.setType(String.class);
            acode.setValue(sp.getString(getString(R.string.assigned_code), ""));
            request.addProperty(acode);

            completedcode = new PropertyInfo();
            completedcode.setName("completedcode");
            completedcode.setType(String.class);
            completedcode.setValue(sp.getString(getString(R.string.work_completed_code), ""));
            request.addProperty(completedcode);

            workprogresscode = new PropertyInfo();
            workprogresscode.setName("workprogresscode");
            workprogresscode.setType(String.class);
            workprogresscode.setValue(sp.getString(getString(R.string.work_progress_code), ""));
            request.addProperty(workprogresscode);

            empcode = new PropertyInfo();
            empcode.setName("empcode");
            empcode.setType(String.class);
            empcode.setValue(sp.getString(getString(R.string.employee_code), ""));
            request.addProperty(empcode);

            f = new PropertyInfo();
            f.setName("flag");
            f.setType(Integer.class);
            f.setValue(4);
            request.addProperty(f);

            option = new PropertyInfo();
            option.setName("option");
            option.setType(Integer.class);
            option.setValue(0);
            request.addProperty(option);

            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            transportSE = new HttpTransportSE(URL);
            transportSE.debug = true;

            try {

                transportSE.call(Soap_Action + "getTaskCount", envelope);


                taskCount = Parser_1(transportSE.responseDump);
                isSuccess_1 = true;


            } catch (HttpResponseException e) {
                e.printStackTrace();
                e.getMessage();
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
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


    }


}
