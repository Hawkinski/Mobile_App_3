package com.example.sachin.fms.adapterPackage;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.sachin.fms.R;
import com.example.sachin.fms.dataSets.TaskListData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Sachin on 24,September,2016
 * Hawkinski,
 * Dubai, UAE.
 */

// A recycler view adapter which uses View_Holder Class to display content row by row

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.View_Holder> implements Filterable {

    public final int type;
    Context context;
    int count = 0;
    String taskType;
    List<TaskListData> taskList = Collections.emptyList();
    itemClick click;
    SharedPreferences sp;

    public RVAdapter(Context context, List<TaskListData> list, int type, String taskType) {
        this.context = context;
        this.taskList = list;

        this.type = type;
        this.taskType = taskType;
        this.sp = context.getSharedPreferences(context.getString(R.string.preferences), Context.MODE_PRIVATE);


    }

    @Override
    public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;

        if (type == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_task_list_content, parent, false);


        } else if (type == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_content, parent, false);


        }
        View_Holder holder = new View_Holder(view);


        return holder;
    }

    @Override
    public void onBindViewHolder(View_Holder holder, int position) {


        if(taskType.equalsIgnoreCase("PPM")){
            holder.layout.setBackgroundColor(ContextCompat.getColor(context,R.color.primaryRed));

        }
        else if(taskType.equalsIgnoreCase("M")){
            holder.layout.setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimary));

        }
        else if(taskType.equalsIgnoreCase("INSP")){
            holder.layout.setBackgroundColor(ContextCompat.getColor(context,R.color.primaryBlue));

        }
        else if(taskType.equalsIgnoreCase("CT")){
            holder.layout.setBackgroundColor(ContextCompat.getColor(context,R.color.primaryGreen));

        }
        else if(taskType.equalsIgnoreCase("RANDOM")){
            holder.layout.setBackgroundColor(ContextCompat.getColor(context,R.color.colorTeal));

        }
        holder.taskid.setText(taskList.get(position).task_id);
        holder.callno.setText(taskList.get(position).call_no);

        holder.location.setText(taskList.get(position).location);
        holder.des.setText(taskList.get(position).des);
        holder.priority.setText(taskList.get(position).priority);
        holder.date.setText(taskList.get(position).date);
        holder.time.setText(taskList.get(position).time);
        if (taskList.get(position).status_code.equalsIgnoreCase(sp.getString(context.getString(R.string.work_completed_code), ""))) {
            holder.completed_sign.setVisibility(View.VISIBLE);
        } else {
            holder.completed_sign.setVisibility(View.GONE);

        }


    }

    public void SetOnClick(itemClick click) {
        this.click = click;

    }

    public void insert(int position, TaskListData data) {
        taskList.add(position, data);

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void remove(TaskListData data) {
        int position = taskList.indexOf(data);
        taskList.remove(position);
        notifyItemRemoved(position);
    }
    public void setFilter(List<TaskListData> taskList) {
        this.taskList = new ArrayList<>();
        this.taskList .addAll(taskList);
        notifyDataSetChanged();
    }

    public void updateList(List<TaskListData> data) {
        taskList = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, R.anim.bounce_interpolator);
        viewHolder.itemView.setAnimation(animAnticipateOvershoot);
    }


    public interface itemClick {
        void itemClick(View v, int position);
    }

    // View_Holder Class to hold the content
    public class View_Holder extends RecyclerView.ViewHolder implements AdapterView.OnClickListener {

        CardView cv;
        TextView date, location, des, priority, time, taskid, callno;
        ImageView img;
        RelativeLayout rl;

        TableLayout layout;
        RelativeLayout completed_sign;


        public View_Holder(View itemView) {
            super(itemView);

            taskid = (TextView) itemView.findViewById(R.id.task_no);
            callno = (TextView) itemView.findViewById(R.id.call_no);

            location = (TextView) itemView.findViewById(R.id.location);
            priority = (TextView) itemView.findViewById(R.id.priority);
            des = (TextView) itemView.findViewById(R.id.description);
            date = (TextView) itemView.findViewById(R.id.r_date);
            time = (TextView) itemView.findViewById(R.id.r_time);
            rl = (RelativeLayout) itemView.findViewById(R.id.r_layout);
            cv = (CardView) itemView.findViewById(R.id.cardView);
            completed_sign = (RelativeLayout) itemView.findViewById(R.id.completed_sign);
            layout = (TableLayout)itemView.findViewById(R.id.layout);
            itemView.setOnClickListener(this);


        }


        @Override
        public void onClick(View v) {

            if (click != null) {
                click.itemClick(v, getAdapterPosition());
            }

        }

    }


    private Filter fRecords;

    @Override
    public Filter getFilter() {
        if (fRecords == null) {
            fRecords = new RecordFilter();
        }
        return fRecords;
    }

    //filter class
    private class RecordFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            //Implement filter logic
            // if edittext is null return the actual list
            if (constraint == null || constraint.length() == 0) {
                //No need for filter
                results.values = taskList;
                results.count = taskList.size();

            } else {
                //Need Filter
                // it matches the text  entered in the edittext and set the data in adapter list
                ArrayList<TaskListData> fRecords = new ArrayList<TaskListData>();

                for (TaskListData s : taskList) {
                    if (s.task_id.toUpperCase().trim().contains(constraint.toString().toUpperCase().trim())) {
                        fRecords.add(s);
                    }
                }
                results.values = fRecords;
                results.count = fRecords.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,FilterResults results) {

            //it set the data from filter to adapter list and refresh the recyclerview adapter
            taskList = (ArrayList<TaskListData>) results.values;
            notifyDataSetChanged();
        }
    }

 }


