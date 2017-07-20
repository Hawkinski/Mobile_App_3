package com.example.sachin.fms.classes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.sachin.fms.dataSets.PermissionData;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;

/**
 * Created by Sachin on 27,May,2017
 * Hawkinski,
 * Dubai, UAE.
 */
public class RunTimePermission {

    private Context context;
    private View view;
    private AppCompatActivity activity;
    private  String[] permissionGroup;
    private final int PERMISSION_REQUEST_CODE = 200;

    public RunTimePermission(Context context, View view){
        this.context= context;
        this.view = view;
    }

    public boolean checkBuildVersion(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }


    public List<PermissionData> checkPermission(String [] permission) {
        List<PermissionData> list = new ArrayList<>();
        for (String aPermission : permission) {
            int result = ContextCompat.checkSelfPermission(context, aPermission);
            if (result == PackageManager.PERMISSION_GRANTED) {
                list.add(new PermissionData(aPermission, true));
            } else {
                list.add(new PermissionData(aPermission, false));

            }

        }

        return list;
    }

    public boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(context, permission);

        return result ==PackageManager.PERMISSION_GRANTED ;
    }
    public void requestPermission(AppCompatActivity activity, String[] permissionGroup) {

        this.activity = activity;
        this.permissionGroup =permissionGroup;
        ActivityCompat.requestPermissions(activity, permissionGroup, PERMISSION_REQUEST_CODE);

    }

    /*@Override
    private void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && cameraAccepted)
                        Snackbar.make(view, "Permission Granted", Snackbar.LENGTH_LONG).show();
                    else {

                        Snackbar.make(view, "Permission Denied", Snackbar.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    ActivityCompat.requestPermissions(activity, permissionGroup, PERMISSION_REQUEST_CODE);
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
    }*/
    public void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


}
