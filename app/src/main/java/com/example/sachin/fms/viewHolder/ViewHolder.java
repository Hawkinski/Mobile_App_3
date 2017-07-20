package com.example.sachin.fms.viewHolder;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.sachin.fms.R;
import com.example.sachin.fms.adapterPackage.RVAdapter;

/**
 * Created by Sachin on 23,May,2017
 * Hawkinski,
 * Dubai, UAE.
 */
// View_Holder Class to hold the content
public class ViewHolder extends RecyclerView.ViewHolder implements AdapterView.OnClickListener {

    public CardView cv;
    public TextView date, location, des, priority, time, taskid, callno;
    public ImageView img;
    public TableLayout rl;
    public RelativeLayout completed_sign;
    public RVAdapter.itemClick click;


    public ViewHolder(View itemView) {
        super(itemView);

        taskid = (TextView) itemView.findViewById(R.id.task_no);
        callno = (TextView) itemView.findViewById(R.id.call_no);

        location = (TextView) itemView.findViewById(R.id.location);
        priority = (TextView) itemView.findViewById(R.id.priority);
        des = (TextView) itemView.findViewById(R.id.description);
        date = (TextView) itemView.findViewById(R.id.r_date);
        time = (TextView) itemView.findViewById(R.id.r_time);
        rl = (TableLayout) itemView.findViewById(R.id.r_layout);
        cv = (CardView) itemView.findViewById(R.id.cardView);
        completed_sign = (RelativeLayout) itemView.findViewById(R.id.completed_sign);

        itemView.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {

        if (click != null) {
            click.itemClick(v, getAdapterPosition());
        }

    }

}