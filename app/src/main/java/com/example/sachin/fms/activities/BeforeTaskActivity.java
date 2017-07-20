package com.example.sachin.fms.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sachin.fms.R;
import com.example.sachin.fms.classes.DatePicker;
import com.example.sachin.fms.classes.Logout;
import com.example.sachin.fms.classes.RunTimePermission;
import com.example.sachin.fms.classes.TimePicker;
import com.example.sachin.fms.dataSets.PermissionData;
import com.example.sachin.fms.others.WebServiceConnection;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.CAMERA;

public class BeforeTaskActivity extends AppCompatActivity {

    private static final int GRANT_PERMISSION = 990;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private EditText text;
    private ProgressDialog pbar;
    private TextView image_count;
    private SharedPreferences sp, sp2;
    private boolean imageClick = false;
    private List<Bitmap> bitmap = new ArrayList<>();
    private boolean flag;
    private ViewPager viewPager;
    private List<String> encodedImage = new ArrayList<>();
    private int activity;
    private String taskId, call_id;
    private List<Bitmap> originalImage = new ArrayList<>();
    private List<Uri> uriList = new ArrayList<>();
    private Uri tempUri;
    private int temp = 0;
    private boolean FAB_status = false;

    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private Animation show_fab1, hide_fab1, show_fab2, hide_fab2;
    private String[] permissionGroup;


    private String NameSpace;
    private String Soap_Action;
    private String URL;
    private WebServiceConnection connection;

    private RunTimePermission rp;
    private SoapSerializationEnvelope envelope, envelope2;
    private SoapObject request, request2;
    private HttpTransportSE transportSE;
    private List<PermissionData> permissionDataList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_before_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        connection = new WebServiceConnection();
        NameSpace = connection.NameSpace;
        Soap_Action = connection.Soap_Action;
        URL = connection.URL;
        Bundle b = getIntent().getExtras();


        sp = this.getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        taskId = sp.getString(getString(R.string.task_number), "");


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


        EditText date = (EditText) findViewById(R.id.current_date);
        EditText time = (EditText) findViewById(R.id.current_time);
        rp = new RunTimePermission(this, this.getCurrentFocus());



        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
        Date d = new Date();
        String current = dateFormat.format(d);
        String[] str = current.split(" ");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab_1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab_2);

        show_fab1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_fab_1);
        hide_fab1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_fab_1);
        show_fab2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_fab_2);
        hide_fab2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_fab_2);

        activity = b.getInt("activity");



        sp2 = BeforeTaskActivity.this.getSharedPreferences("permission_preference", MODE_PRIVATE);


        if (fab != null) {
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
        }


        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logout logout = new Logout(BeforeTaskActivity.this, sp);
                logout.execute();


                finish();

            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(BeforeTaskActivity.this, LandingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        if (date != null) {
            date.setText(str[0]);
        }
        if (time != null) {
            time.setText(str[1]);
        }
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        if (date != null) {
            date.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.setFocusable(true);
                    v.setFocusableInTouchMode(true);

                    return false;
                }
            });
        }


        if (date != null) {
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
        }

        if (time != null) {
            time.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.setFocusable(true);
                    v.setFocusableInTouchMode(true);

                    return false;
                }
            });
        }

        if (time != null) {
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
        }


        image_count = (TextView) findViewById(R.id.image_count);
        text = (EditText) findViewById(R.id.before_work_description);
        ImageView btn_click = (ImageView) findViewById(R.id.camera_btn);
        Button btn = (Button) findViewById(R.id.before_work_save_btn);

        pbar = new ProgressDialog(this);
        pbar.setMessage("Saving Data.... ");
        pbar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pbar.setIndeterminate(true);
        pbar.setCancelable(false);

        if (btn_click != null) {
            btn_click.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    imageClick = true;
                    if (!rp.checkBuildVersion()) {
                        selectImage();

                    } else {
                        permissionDataList = new ArrayList<>();
                        permissionDataList = rp.checkPermission(permissionGroup);
                        if (permissionDataList.size() == 1 && permissionDataList.size() != 0) {
                            if (permissionDataList.get(0).isGranted) {
                                selectImage();
                            } else {
                                rp.requestPermission(BeforeTaskActivity.this, new String[]{permissionDataList.get(0).code});

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
                                rp.requestPermission(BeforeTaskActivity.this, str);


                            } else {
                                selectImage();

                            }
                        }


                    }


                }
            });
        }


        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    temp = 1;
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putInt("temp", temp);
                    edit.apply();


                    SaveWithoutInsert insert = new SaveWithoutInsert(activity);
                    insert.execute();


                }
            });
        }


        if (activity == 2) {
            TextView text = (TextView) findViewById(R.id.top);
            if (text != null) {
                text.setText(getString(R.string.After_picture));
            }

        } else if (activity == 3) {
            TextView text = (TextView) findViewById(R.id.top);
            if (text != null) {
                text.setText("Take Picture");
            }
        }

        if (rp.checkBuildVersion()) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            permissionGroup = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE

            };
        }

    }


    public boolean selectImage() {

        flag = false;
        final CharSequence[] option = {"Select from Gallery", "Take Photo", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(BeforeTaskActivity.this);
        builder.setTitle("Add");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (option[which].equals("Select from Gallery")) {

                    if (Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)
                            && !Environment.getExternalStorageState().equals(
                            Environment.MEDIA_CHECKING)) {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        flag = true;
                        startActivityForResult(intent, 1);

                    } else {
                        Toast.makeText(BeforeTaskActivity.this,
                                "No activity found to perform this task",
                                Toast.LENGTH_SHORT).show();
                        flag = false;
                    }


                } else if (option[which].equals("Take Photo")) {


                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

                    flag = true;
                    startActivityForResult(intent, 2);

                } else if (option[which].equals("Cancel")) {
                    dialog.dismiss();
                    flag = false;
                }
            }
        });
        builder.show();

        return flag;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case GRANT_PERMISSION:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && cameraAccepted)
                        Snackbar.make(this.getCurrentFocus(), "Permission Granted", Snackbar.LENGTH_LONG).show();
                    else {

                        Snackbar.make(this.getCurrentFocus(), "Permission Denied", Snackbar.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(BeforeTaskActivity.this, CAMERA)) {
                                rp.showMessageOKCancel("You need to grant the permissions to access the features",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    ActivityCompat.requestPermissions(BeforeTaskActivity.this, permissionGroup, GRANT_PERMISSION);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }
    public File setFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        String currentImage = "file:" + image.getAbsolutePath();
        return image;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {

            switch (resultCode) {

                case RESULT_OK:


                    Uri selectedImg = data.getData();
                    uriList.add(selectedImg);

                    try {
                        InputStream stream = getContentResolver().openInputStream(selectedImg);
                        originalImage.add(BitmapFactory.decodeStream(stream));


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    ImageLoader il = new ImageLoader(selectedImg);
                    il.execute();


                    Bitmap originBitmap = null;
                    Uri selectedImage = data.getData();

                    InputStream imageStream;
                    try {
                        imageStream = getContentResolver().openInputStream(
                                selectedImage);
                        originBitmap = BitmapFactory.decodeStream(imageStream);
                    } catch (FileNotFoundException ignored) {
                    }
                    if (originBitmap != null) {

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        originBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                        byte[] imagebyte = stream.toByteArray();

                        encodedImage.add(Base64.encodeToString(imagebyte, Base64.NO_WRAP));
                        Log.e("Base64", Base64.encodeToString(imagebyte, Base64.NO_WRAP));

                    } else {
                        Toast.makeText(BeforeTaskActivity.this, "No image selected", Toast.LENGTH_LONG).show();
                    }


                    break;

                case RESULT_CANCELED:


                    break;

                default:
                    break;
            }


        } else if (requestCode == 2) {

            switch (resultCode) {

                case RESULT_OK:

                    File f = new File(Environment.getExternalStorageDirectory().toString());
                    for (File temp : f.listFiles()) {
                        if (temp.getName().equals("temp.jpg")) {
                            f = temp;

                            tempUri = Uri.fromFile(f);
                            break;
                        }
                    }
                    Bitmap capturedimg = null;
                    try {
                        capturedimg = decodeUri(tempUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();


                   /* File destination = new File(Environment.getExternalStorageDirectory(),
                            System.currentTimeMillis() + ".jpg");
                    FileOutputStream fo;
                    try {
                        destination.createNewFile();
                        fo = new FileOutputStream(destination);
                        fo.write(bytes.toByteArray());
                        fo.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/


                    if (capturedimg != null) {
                        capturedimg.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                    }

                    originalImage.add(capturedimg);

                    if (capturedimg != null) {
                        capturedimg.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
                    }

                    byte[] obyte = bytes.toByteArray();

                    encodedImage.add(Base64.encodeToString(obyte, Base64.NO_WRAP));


                    bitmap.add(capturedimg);


                    if (bitmap.isEmpty()) {
                        image_count.setText("0 Picture Selected");

                    } else if (bitmap.size() > 1) {
                        image_count.setText(bitmap.size() + " Pictures Selected");

                    } else {
                        image_count.setText(bitmap.size() + " Picture Selected");

                    }
                    if (imageClick && flag) {
                        fill_image_adapter(bitmap, originalImage);
                    } else {
                        Toast.makeText(BeforeTaskActivity.this, "ERROR", Toast.LENGTH_LONG).show();
                    }


                    break;

                case RESULT_CANCELED:
                    // Toast.makeText(BeforeTaskActivity.this, "Image Selection is canceled by the user", Toast.LENGTH_SHORT).show();

                    break;

                default:
                    break;

            }


        }


    }


    //Image loader background method

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {


        Bitmap rotatedImage = null;

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);


        // The new size we want to scale to
        final int width = 900;
        final int height = 600;
        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < width
                    || height_tmp / 2 < height) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap map = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);

        try {
            ExifInterface exif = new ExifInterface(selectedImage.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            int angle = 0;
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;
                default:
                    angle = 0;
                    break;


            }
            Matrix matrix = new Matrix();
            if (angle == 0 && viewPager.getHeight() < map.getHeight()) {
                matrix.postRotate(90);

            } else {
                matrix.postRotate(angle);

            }
            rotatedImage = Bitmap.createBitmap(map, 0, 0, map.getWidth(), map.getHeight());


        } catch (IOException e) {
            e.printStackTrace();
        }

        return rotatedImage;


    }


    //downsize;

    @Override
    public void onResume() {
        super.onResume();

    }

    public void fill_image_adapter(List<Bitmap> bitmap, List<Bitmap> originalImage) {
        List<Bitmap> clone = new ArrayList<>(bitmap);
        List<Bitmap> imageClone = new ArrayList<>(originalImage);
        ImageAdapter imageAdapter = new ImageAdapter(BeforeTaskActivity.this, clone, imageClone);
        viewPager.setAdapter(imageAdapter);


    }

    // Fill image method to fill the pager adapter with images

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


//  Save task into database

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

    public class ImageLoader extends AsyncTask<String, String, Bitmap> {

        Uri image_uri;
        boolean isSuccess = false;
        Matrix matrix;
        private Bitmap bmap;
        private Bitmap photo;

        public ImageLoader(Uri uri) {
            this.image_uri = uri;
        }

        public void onPreExecute() {
            pbar.show();

        }

        public void onPostExecute(Bitmap r) {
            pbar.dismiss();


            if (isSuccess) {
                //Toast.makeText(BeforeTaskActivity.this,uriList.get(0).toString(),Toast.LENGTH_LONG).show();

                matrix = new Matrix();
                matrix.postRotate(90);


                Bitmap bit = Bitmap.createScaledBitmap(photo, 900, 600, false);

                bitmap.add(bit);

                if (bitmap.isEmpty()) {
                    image_count.setText("0 Picture Selected");

                } else if (bitmap.size() > 1) {
                    image_count.setText(bitmap.size() + " Pictures Selected");

                } else {
                    image_count.setText(bitmap.size() + " Picture Selected");

                }
                if (imageClick && flag) {
                    fill_image_adapter(bitmap, bitmap);
                }

            }


        }

        @Override
        protected Bitmap doInBackground(String... params) {


            try {
                bmap = decodeUri(image_uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


            Bitmap originBitmap = bmap;
            // Uri selectedImage = data.getData();
            if (originBitmap != null) {

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                originBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] imagebyte = stream.toByteArray();
                photo = BitmapFactory.decodeByteArray(imagebyte, 0, imagebyte.length);


                isSuccess = true;
            } else {
                isSuccess = false;

            }


            return photo;
        }
    }


    public class SaveWithoutInsert extends AsyncTask<String, String, String> {
        int x;
        String remarks = text.getText().toString();

        String img_type = "";

        String z = "";
        boolean isSuccess = false;
        boolean isSuccess_1 = false;

        public SaveWithoutInsert(int x) {
            this.x = x;


        }

        @Override
        public void onPreExecute() {
            pbar.show();

        }

        @Override
        public void onPostExecute(String r) {
            pbar.dismiss();

            if (isSuccess_1) {

                if (x == 1) {
                    Toast.makeText(BeforeTaskActivity.this, "Before Task Data Saved!! ", Toast.LENGTH_SHORT).show();

                    BeforeTaskActivity.this.finish();
                } else if (x == 2) {
                    Toast.makeText(BeforeTaskActivity.this, "After Task Data Saved!! ", Toast.LENGTH_SHORT).show();

                    BeforeTaskActivity.this.finish();

                } else if (x == 3) {
                    Toast.makeText(BeforeTaskActivity.this, "Saved!! ", Toast.LENGTH_SHORT).show();

                    BeforeTaskActivity.this.finish();
                }

            } else {
                Toast.makeText(BeforeTaskActivity.this, "Database Error,please check your connection", Toast.LENGTH_SHORT).show();

            }

        }

        @Override
        protected String doInBackground(String... params) {
            if (x == 1) {
                img_type = "B";
            } else if (x == 2) {
                img_type = "A";
            } else if (x == 3) {
                img_type = "TP";

            }


            PropertyInfo call_no, taskNo, compcode, imagetype, remark, Value;

            {
                for (int i = 0; i < encodedImage.size(); i++) {
                    request = new SoapObject(NameSpace, "saveImage");

                    call_no = new PropertyInfo();
                    call_no.setName("call_no");
                    call_no.setType(String.class);
                    call_no.setValue(call_id);
                    request.addProperty(call_no);


                    taskNo = new PropertyInfo();
                    taskNo.setName("task_id");
                    taskNo.setType(String.class);
                    taskNo.setValue(taskId);
                    request.addProperty(taskNo);


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


                    remark = new PropertyInfo();
                    remark.setName("remarks");
                    remark.setType(String.class);
                    remark.setValue(remarks);
                    request.addProperty(remark);

                    Value = new PropertyInfo();
                    Value.setName("Value");
                    Value.setType(String.class);
                    Value.setValue(encodedImage.get(i));
                    request.addProperty(Value);


                    envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    envelope.setOutputSoapObject(request);
                    envelope.dotNet = true;
                    transportSE = new HttpTransportSE(URL);
                    transportSE.debug = true;

                    try {
                        transportSE.call(Soap_Action + "saveImage", envelope);
                        SoapObject result = (SoapObject) envelope.getResponse();
                        if (result.getPropertyCount() != 0 && result.getProperty(0).toString().equalsIgnoreCase("Saved")) {
                            z = result.getProperty(0).toString();
                            isSuccess_1 = true;

                        } else {
                            isSuccess_1 = false;
                        }
                    } catch (Exception e) {
                        isSuccess_1 = false;


                    }

                }


            }

            return z;
        }
    }

    public class ImageAdapter extends PagerAdapter {


        List<Bitmap> bitmapList = Collections.emptyList();
        Context context;
        List<Bitmap> imageClone = Collections.emptyList();


        public ImageAdapter(Context context, List<Bitmap> bitmapList, List<Bitmap> imageClone) {


            this.context = context;
            this.bitmapList = bitmapList;
            this.imageClone = imageClone;
        }

        @Override
        public int getCount() {
            return bitmapList.size();
        }

        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {


            final ImageView imageView = new ImageView(context);

            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);




            imageView.setImageBitmap(bitmapList.get(position));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog nagDialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                    nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    nagDialog.setCancelable(false);
                    nagDialog.setContentView(R.layout.content_edit);
                    ImageButton btnClose = (ImageButton) nagDialog.findViewById(R.id.imageButton);

                    ImageView ivPreview = (ImageView) nagDialog.findViewById(R.id.iv_preview_image);
                    ivPreview.setImageBitmap(imageClone.get(position));
                    btnClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {

                            nagDialog.dismiss();
                        }
                    });
                    nagDialog.show();
                }
            });
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete").setMessage("Are you sure you want to Delete this Picture")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (imageClone.isEmpty()) {
                                        Toast.makeText(context, "Sorry There are no images to delete", Toast.LENGTH_LONG).show();
                                    } else {
                                        imageClone.remove(position);
                                        bitmapList.remove(position);
                                        bitmap.remove(position);
                                        encodedImage.remove(position);
                                        originalImage.remove(position);
                                        notifyDataSetChanged();
                                        dialog.dismiss();
                                    }

                                }
                            }).create().show();


                    return true;
                }
            });
            imageView.setImageBitmap(bitmapList.get(position));

            if (bitmap.isEmpty()) {
                image_count.setText("0 Picture Selected");

            } else if (bitmap.size() > 1) {
                image_count.setText(bitmap.size() + " Pictures Selected");

            } else {
                image_count.setText(bitmap.size() + " Picture Selected");

            }
            container.addView(imageView, 0);

            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }


    }





}



