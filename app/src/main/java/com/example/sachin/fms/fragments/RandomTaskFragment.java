package com.example.sachin.fms.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sachin.fms.R;
import com.example.sachin.fms.activities.AssignedTaskActivity;
import com.example.sachin.fms.activities.TaskDetailsActivity;
import com.example.sachin.fms.activities.TaskStartActivity;
import com.example.sachin.fms.adapterPackage.RVAdapter;
import com.example.sachin.fms.classes.EndlessRecyclerViewScrollListener;
import com.example.sachin.fms.classes.FillTaskList2;
import com.example.sachin.fms.classes.FillTaskList3;
import com.example.sachin.fms.dataSets.TaskListData;
import com.example.sachin.fms.others.WebServiceConnection;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RandomTaskFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RandomTaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RandomTaskFragment extends Fragment implements RVAdapter.itemClick,FillTaskList2.FillTaskInterface,FillTaskList3.FillTaskInterface{
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private static final String ARG_PARAM1 = "param1";
private static final String ARG_PARAM2 = "param2";
private String taskType="RANDOM";

// TODO: Rename and change types of parameters
private String mParam1;
private String mParam2;
private EndlessRecyclerViewScrollListener scrollListener;
private RecyclerView recyclerView;
private SwipeRefreshLayout swipeRefreshLayout;
private RVAdapter radapter;
private InspectionTaskFragment.OnFragmentInteractionListener mListener;
private SharedPreferences sp;
private ProgressDialog pdialog;
private List<TaskListData> data = new ArrayList<>();
private String NameSpace;
private String Soap_Action;
private String URL;
private WebServiceConnection connection;

private SoapSerializationEnvelope envelope;
private SoapObject request;
private SoapPrimitive response;
private HttpTransportSE transportSE;
private AssignedTaskActivity activity;
private int taskCount,flag;
private boolean isLoaded =false,isVisibleToUser;
public RandomTaskFragment() {
        // Required empty public constructor

        }
@SuppressLint("ValidFragment")
public RandomTaskFragment (SharedPreferences sp, ProgressDialog pdialog, int flag) {
        this.sp = sp;
        this.pdialog=pdialog;
        this.flag=flag;
        }


/**
 * Use this factory method to create a new instance of
 * this fragment using the provided parameters.
 *
 * @param param1 Parameter 1.
 * @param param2 Parameter 2.
 * @return A new instance of fragment InspectionTaskFragment.
 */
// TODO: Rename and change types and number of parameters
public static RandomTaskFragment newInstance(String param1, String param2) {
    RandomTaskFragment fragment = new RandomTaskFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
        }

@Override
public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        mParam1 = getArguments().getString(ARG_PARAM1);
        mParam2 = getArguments().getString(ARG_PARAM2);

        }
        }

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_random_task, container, false);

        recyclerView = (RecyclerView)view.findViewById(R.id.randomRecyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getActivity());

        recyclerView.setLayoutManager(linearLayoutManager);
        connection = new WebServiceConnection();
        NameSpace = connection.NameSpace;
        Soap_Action = connection.Soap_Action;
        URL = connection.URL;



        if(isAdded()){
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
@Override
public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {


        FillTaskList2 fill = new FillTaskList2(flag,RandomTaskFragment.this.getActivity(),null,null,null,null,RandomTaskFragment.this,sp,(page+1),pdialog,taskType);
        fill.execute();






        }
        };
        recyclerView.addOnScrollListener(scrollListener);
        }

        return view;
        }

   /* @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser=isVisibleToUser;
        if(isVisibleToUser && isAdded() ){
            data = new ArrayList<>();
            FillTaskList3 fill = new FillTaskList3(flag,this.getActivity(),null,null,this,null,sp,1,pdialog,taskType);
            fill.execute();
            isLoaded =true;
        }
    }*/


@Override
public void onResume(){
        super.onResume();
        data = new ArrayList<>();
        FillTaskList3 fill = new FillTaskList3(flag,this.getActivity(),null,null,null,null,this,sp,1,pdialog,taskType);
        fill.execute();
        }


// TODO: Rename method, update argument and hook method into UI event
public void onButtonPressed(Uri uri) {
        if (mListener != null) {
        mListener.onFragmentInteraction(uri);
        }
        }

   /* @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

@Override
public void onDetach() {
        super.onDetach();
        mListener = null;
        }

/**
 * This interface must be implemented by activities that contain this
 * fragment to allow an interaction in this fragment to be communicated
 * to the activity and potentially other fragments contained in that
 * activity.
 * <p>
 * See the Android Training lesson <a href=
 * "http://developer.android.com/training/basics/fragments/communicating.html"
 * >Communicating with Other Fragments</a> for more information.
 */
public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    void onFragmentInteraction(Uri uri);
}

    @Override
    public void fillTaskList(List<TaskListData> list) {

        if(isAdded()){
            if(data.size() == 0){

                if(list.size() == 0){
                    String msg = "No Random task available.";
                    data.clear();

//                    Toast.makeText(this.getActivity(),msg,Toast.LENGTH_LONG).show();
                    radapter = new RVAdapter(this.getActivity(), data, flag,taskType);
                    radapter.SetOnClick(RandomTaskFragment.this);
                    recyclerView.setAdapter(radapter);
                    radapter.notifyDataSetChanged();
                }
                else{
                    this.data.addAll(list);
                    radapter = new RVAdapter(this.getActivity(), data, flag,taskType);
                    radapter.SetOnClick(RandomTaskFragment.this);
                    recyclerView.setAdapter(radapter);
                    radapter.notifyDataSetChanged();
                }

            }
            else{

                this.data.addAll(list);
                radapter = new RVAdapter(this.getActivity(), data, flag,taskType);
                radapter.SetOnClick(RandomTaskFragment.this);
                recyclerView.setAdapter(radapter);
                radapter.notifyDataSetChanged();

            }
        }






    }

    @Override
    public void fillTaskList2(List<TaskListData> list) {

        if(isAdded()){
            if(data.size() == 0){

                if(list.size() == 0){
                    String msg ;
                    msg = "No Random task available.";
                    data.clear();

//                    Toast.makeText(this.getActivity(),msg,Toast.LENGTH_LONG).show();
                    radapter = new RVAdapter(this.getActivity(), data, flag,taskType);
                    radapter.SetOnClick(RandomTaskFragment.this);
                    recyclerView.setAdapter(radapter);
                    radapter.notifyDataSetChanged();
                }
                else{
                    this.data=list;
                    radapter = new RVAdapter(this.getActivity(), data, flag,taskType);
                    radapter.SetOnClick(RandomTaskFragment.this);
                    recyclerView.setAdapter(radapter);
                    radapter.notifyDataSetChanged();
                }

            }
            else{

                this.data=list;
                radapter = new RVAdapter(this.getActivity(), data, flag,taskType);
                radapter.SetOnClick(RandomTaskFragment.this);
                recyclerView.setAdapter(radapter);
                radapter.notifyDataSetChanged();

            }

        }

    }
    @Override
    public void itemClick(View v, int position) {
        Consume c = new Consume(position);
        c.execute();
    }
public class Consume extends AsyncTask<String, String, Integer> {


    int start = 0;
    int position = 0;

    public Consume(int position) {
        this.position = position;
    }

    public void onPreExecute() {
        pdialog.show();
    }

    public void onPostExecute(Integer s) {
        pdialog.dismiss();

        if (s == 1) {
            Intent i = new Intent(RandomTaskFragment.this.getActivity(), TaskStartActivity.class);

            SharedPreferences.Editor edit = sp.edit();
            edit.putString(getString(R.string.task_number), data.get(position).task_id);
            edit.putString(getString(R.string.reported_date), data.get(position).date);
            edit.putString(getString(R.string.reported_time), data.get(position).time);
            edit.putString(getString(R.string.building), data.get(position).building);
            edit.putString(getString(R.string.location), data.get(position).location);
            edit.putString(getString(R.string.call_number), data.get(position).call_no);
            edit.putString(getString(R.string.ppm_work_order_no), data.get(position).ppm_no);
            edit.putString(getString(R.string.location_code), data.get(position).location_code);
            edit.putString(getString(R.string.building_code), data.get(position).building_code);
            edit.putString(getString(R.string.reported_datetime), data.get(position).reported_dt);
            edit.putBoolean("isInspection", data.get(position).isInspection);
            edit.putBoolean("isRandom", data.get(position).isRandom);

            edit.putString(getString(R.string.asset), data.get(position).asset);
            edit.putString(getString(R.string.scope), data.get(position).scope);


            edit.apply();
            //i.putExtra(getString(R.string.task_number),data.get(position).task_id);
            // i.putExtra(getString(R.string.call_number),data.get(position).call_no);

            startActivity(i);
        } else {
            Intent i = new Intent(RandomTaskFragment.this.getActivity(), TaskDetailsActivity.class);

            SharedPreferences.Editor edit = sp.edit();
            edit.putString(getString(R.string.task_number), data.get(position).task_id);
            edit.putString(getString(R.string.reported_date), data.get(position).date);
            edit.putString(getString(R.string.reported_time), data.get(position).time);
            edit.putString(getString(R.string.building), data.get(position).building);
            edit.putString(getString(R.string.location), data.get(position).location);
            edit.putString(getString(R.string.call_number), data.get(position).call_no);
            edit.putString(getString(R.string.ppm_work_order_no), data.get(position).ppm_no);
            edit.putString(getString(R.string.location_code), data.get(position).location_code);
            edit.putString(getString(R.string.building_code), data.get(position).building_code);
            edit.putString(getString(R.string.reported_datetime), data.get(position).reported_dt);
            edit.putBoolean("isInspection", data.get(position).isInspection);
            edit.putBoolean("isRandom", data.get(position).isRandom);

            edit.putString(getString(R.string.asset), data.get(position).asset);
            edit.putString(getString(R.string.scope), data.get(position).scope);

            edit.apply();
            //i.putExtra(getString(R.string.task_number),data.get(position).task_id);
            // i.putExtra(getString(R.string.call_number),data.get(position).call_no);

            startActivity(i);
        }
    }


    @Override
    protected Integer doInBackground(String... params) {
        PropertyInfo task_no, compcode, call_;

        request = new SoapObject(NameSpace, "getWorkStatus");

        task_no = new PropertyInfo();
        task_no.setName("task_no");
        task_no.setType(String.class);
        task_no.setValue(data.get(position).task_id);
        request.addProperty(task_no);

        //Log.e("Task_id", data.get(position).task_id);

        compcode = new PropertyInfo();
        compcode.setName("compcode");
        compcode.setType(String.class);
        compcode.setValue(sp.getString(getString(R.string.company_code), ""));
        request.addProperty(compcode);


        call_ = new PropertyInfo();
        call_.setName("call_no");
        call_.setType(String.class);
        call_.setValue(data.get(position).call_no);
        request.addProperty(call_);

        // Log.e("Task_id", data.get(position).call_no);


        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        envelope.dotNet = true;
        transportSE = new HttpTransportSE(URL);
        transportSE.debug = true;

        try {

            transportSE.call(Soap_Action + "getWorkStatus", envelope);
            response = (SoapPrimitive) envelope.getResponse();
            start = Integer.parseInt(response.toString());
            //Log.e("error", response.toString());
//

        } catch (Exception e) {
            //  Log.e("error", e.getMessage());
            //Toast.makeText(TaskStatusUpdateActivity.this,e.getMessage()+"Connection Error, Please Check your Internet Connection",Toast.LENGTH_LONG).show();

        }
        return start;
    }
}
}

