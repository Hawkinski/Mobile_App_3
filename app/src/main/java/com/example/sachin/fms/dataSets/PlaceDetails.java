package com.example.sachin.fms.dataSets;

import java.io.Serializable;

/**
 * Created by Sachin on 27,May,2017
 * Hawkinski,
 * Dubai, UAE.
 */
public class PlaceDetails implements Serializable {

    //@Key
    public String status;

    //@Key
    public PlaceData result;

    @Override
    public String toString() {
        if (result!=null) {
            return result.toString();
        }
        return super.toString();
    }

}
