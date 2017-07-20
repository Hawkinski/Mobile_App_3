package com.example.sachin.fms.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sachin.fms.R;
import com.example.sachin.fms.adapterPackage.IMGAdapter;
import com.example.sachin.fms.classes.Logout;
import com.example.sachin.fms.others.WebServiceConnection;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TaskCompleteActivity extends AppCompatActivity implements IMGAdapter.setOnImageClick {

    private static final int GRANT_PERMISSION = 990;
    public static SharedPreferences sp, sp2;
    public static String taskId, call_no;
    private static ProgressDialog pdialog;
    private static TextView text;
    private Bitmap images;
    private Bundle b;
    private boolean FAB_status = false;
    private ImageView img;
    private String[] permissionGroup;

    private FloatingActionButton fab, fab1, fab2;
    private Animation show_fab1, hide_fab1, show_fab2, hide_fab2;

    private WebServiceConnection connection;

    private String URL2;


    private String signatureString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_complete);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        connection = new WebServiceConnection();

        URL2 = connection.URL2;

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        b = getIntent().getExtras();
        sp = this.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);

        if (b != null) {
            signatureString = b.getString("string");

        }


        taskId = sp.getString(getString(R.string.task_number), "null");
        call_no = sp.getString(getString(R.string.call_number), "null");

        //Log.e("CALL _ NO ", call_no);
        CollapsingToolbarLayout cl = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        assert cl != null;
        cl.setTitle("Task No: " + taskId);

        // Set up the ViewPager with the sections adapter.

        ActionBar actionBar;
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }


        assert toolbar != null;
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        img = (ImageView) findViewById(R.id.img);

        sp = this.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab_1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab_2);
        pdialog = new ProgressDialog(TaskCompleteActivity.this);

        pdialog.setMessage("Loading Images...");
        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdialog.setIndeterminate(true);
        pdialog.setCancelable(false);


        show_fab1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_fab_1);
        hide_fab1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_fab_1);
        show_fab2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_fab_2);
        hide_fab2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_fab_2);

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
        //load = new LoadImages();
        sp2 = this.getSharedPreferences("permission_preference", MODE_PRIVATE);

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logout logout = new Logout(TaskCompleteActivity.this, sp);
                logout.execute();


                finish();

            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(TaskCompleteActivity.this, LandingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        text = (TextView) findViewById(R.id.emptyText);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            permissionGroup = new String[]{

                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE

            };
        }

        if (signatureString.equalsIgnoreCase("")) {
            text.setVisibility(View.VISIBLE);

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionCheck();
            } else {


                load l = new load();
                l.execute();

            }
        }


    }


    @TargetApi(Build.VERSION_CODES.M)
    public void permissionCheck() {

        if (sp2.getBoolean("permission_write", false) && sp2.getBoolean("permission_read", false)) {
            load l = new load();
            l.execute();
        } else {
            requestPermissions(permissionGroup, GRANT_PERMISSION);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResult) {

        switch (requestCode) {
            case GRANT_PERMISSION: {


                {
                    if (grantResult[0] == PackageManager.PERMISSION_GRANTED && grantResult[1] == PackageManager.PERMISSION_GRANTED) {


                        SharedPreferences.Editor edit = sp2.edit();
                        edit.putBoolean("permission_write", true);
                        edit.putBoolean("permission_read", true);
                        edit.apply();
                        load l = new load();
                        l.execute();
                        //Log.d("Permission", "Permission Granted :" + permission[0]);


                    } else if (grantResult[0] == PackageManager.PERMISSION_DENIED && grantResult[1] == PackageManager.PERMISSION_DENIED) {

                        Toast.makeText(this, "Permission Denied: " + permission[0], Toast.LENGTH_LONG).show();



                    }
                }


            }


        }


    }


    @Override
    public void setOnImageClick(View v, int position) {
        final Dialog nagDialog = new Dialog(TaskCompleteActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        nagDialog.setCancelable(false);
        nagDialog.setContentView(R.layout.content_edit);
        ImageButton btnClose = (ImageButton) nagDialog.findViewById(R.id.imageButton);

        ImageView ivPreview = (ImageView) nagDialog.findViewById(R.id.iv_preview_image);
        ivPreview.setImageBitmap(images);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                nagDialog.dismiss();
            }
        });
        nagDialog.show();

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

    public class load extends AsyncTask<String, String, Bitmap> {


        @Override
        protected Bitmap doInBackground(String... String) {
            String urldisplay = URL2 + signatureString;
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                mIcon11.compress(Bitmap.CompressFormat.JPEG, 90, out);


            } catch (Exception e) {
                //Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;

        }

        public void onPreExecute() {
            pdialog.show();
        }

        public void onPostExecute(Bitmap s) {
            pdialog.dismiss();


            images = s;


            if (images != null) {


                img.setImageBitmap(images);
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Dialog nagDialog = new Dialog(TaskCompleteActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                        nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        nagDialog.setCancelable(false);
                        nagDialog.setContentView(R.layout.content_edit);
                        ImageButton btnClose = (ImageButton) nagDialog.findViewById(R.id.imageButton);



                        ImageView ivPreview = (ImageView) nagDialog.findViewById(R.id.iv_preview_image);
                        ivPreview.setImageBitmap(images);
                        btnClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View arg0) {

                                nagDialog.dismiss();
                            }
                        });
                        nagDialog.show();
                    }
                });


            } else {
                text.setVisibility(View.VISIBLE);

            }

        }
    }


}





















