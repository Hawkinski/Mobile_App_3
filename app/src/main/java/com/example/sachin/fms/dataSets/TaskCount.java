package com.example.sachin.fms.dataSets;

/**
 * Created by Sachin on 24,September,2016
 * Hawkinski,
 * Dubai, UAE.
 */
public class TaskCount {
    public String newTask, viewedTask;
    public int  regularCount,completedCount,PPMCount,inspectionCount,randomCount;

    public TaskCount() {

    }

    public TaskCount(String newtask , String viewedtask) {
        this.newTask = newtask;
        this.viewedTask = viewedtask;

    }

}
