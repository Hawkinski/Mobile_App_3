package com.example.sachin.fms.dataSets;

/**
 * Created by Sachin on 27,May,2017
 * Hawkinski,
 * Dubai, UAE.
 */
public class PermissionData {

    public String code;
    public boolean isGranted;


    public PermissionData (String code,boolean isGranted){
        this.isGranted = isGranted;
        this.code=code;
    }

}
