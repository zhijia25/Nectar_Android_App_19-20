package com.jianqingc.nectar.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.ArrayAdapter;

import android.widget.EditText;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.jianqingc.nectar.R;
import com.jianqingc.nectar.controller.HttpRequestController;
import com.jianqingc.nectar.controller.RadioAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.

 * create an instance of this fragment.
 */
public class CreateAlarmFragment extends Fragment {
    View myView;
    public static CreateAlarmFragment instanceCA = null;
    public String chooseType;
    public String chooseMetric;
    public String chooseMethod;
    public String chooseOperator;
    public String chooseState;
    public String chooseSeverity;

    public int setthresholdValue=-1;
    public int setgranularityValue=-1;
    public String setName=" ";
    public String setDescription=" ";

    List<String> typeName;
    List<String> metricName;
    List<String> methodName;
    List<String> operatorName;
    List<String> stateName;
    List<String> severityName;

    private RadioAdapter adapter;




    //private OnFragmentInteractionListener mListener;

    public CreateAlarmFragment() {
        // Required empty public constructor
        instanceCA=this;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.

     */
    // TODO: Rename and change types and number of parameters


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_create_alarm, container, false);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("New Alarm");
        /**
         * set spinner which is actually a dialog
         */
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        // display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);

        final EditText name = (EditText) myView.findViewById(R.id.newAlarmName);
        final EditText description = (EditText) myView.findViewById(R.id.descriptionAlarm);
        final Spinner type = (Spinner) myView.findViewById(R.id.alarmType);
        final Spinner metric = (Spinner) myView.findViewById(R.id.alarmMetric);
        final Spinner method = (Spinner) myView.findViewById(R.id.aggreMethod);
        final Spinner operator = (Spinner) myView.findViewById(R.id.operator);
        final EditText threshold = (EditText) myView.findViewById(R.id.thresholdValue);
        final EditText granularity = (EditText) myView.findViewById(R.id.granularity);
        final Spinner state = (Spinner) myView.findViewById(R.id.alarmState);
        final Spinner severity = (Spinner) myView.findViewById(R.id.severity);

        final Button create = (Button)myView.findViewById(R.id.createalarm);

        final java.util.Timer timer = new java.util.Timer(true);

        //list type

        final List<String> type_list;
        type_list = new ArrayList<String>();
        type_list.add("Select type please");
        ArrayAdapter<String> arr_adapter_type;

        type_list.add("gnocchi_resources_threshold");
        type_list.add("gnocchi_aggregation_by_metrics_threshold");
        type_list.add("gnocchi_aggregation_by_resources_threshold_rule");

        //new adapter
        arr_adapter_type = new ArrayAdapter<String>(CreateAlarmFragment.this.getActivity(),android.R.layout.simple_spinner_item,type_list);
        // set the format of the adapter
        arr_adapter_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set the adapter
        type.setAdapter(arr_adapter_type);


        type.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String chooseName = type_list.get(arg2);
                System.out.println("check:"+chooseName);
                //set to show the chosen
                arg0.setVisibility(View.VISIBLE);
                //String
                instanceCA.chooseType = chooseName;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //list metric

        final List<String> metric_list;
        metric_list = new ArrayList<String>();
        metric_list.add("Select metric please");
        ArrayAdapter<String> arr_adapter_metric;

        metric_list.add("cpu_util");

        //new adapter
        arr_adapter_metric = new ArrayAdapter<String>(CreateAlarmFragment.this.getActivity(),android.R.layout.simple_spinner_item,metric_list);
        // set the format of the adapter
        arr_adapter_metric.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set the adapter
        metric.setAdapter(arr_adapter_metric);

        metric.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String chooseName = metric_list.get(arg2);
                //set to show the chosen
                arg0.setVisibility(View.VISIBLE);
                //String
                instanceCA.chooseMetric = chooseName;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //list method

        final List<String> method_list;
        method_list = new ArrayList<String>();
        method_list.add("Select method please");
        ArrayAdapter<String> arr_adapter_method;

        method_list.add("sum");
        method_list.add("mean");

        //new adapter
        arr_adapter_method = new ArrayAdapter<String>(CreateAlarmFragment.this.getActivity(),android.R.layout.simple_spinner_item,method_list);
        // set the format of the adapter
        arr_adapter_method.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set the adapter
        method.setAdapter(arr_adapter_method);

        method.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String chooseName = method_list.get(arg2);
                //set to show the chosen
                arg0.setVisibility(View.VISIBLE);
                //String
                instanceCA.chooseMethod = chooseName;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //list operator

        final List<String> operator_list;
        operator_list = new ArrayList<String>();
        operator_list.add("Select operator please");
        ArrayAdapter<String> arr_adapter_operator;

        operator_list.add("gt");
        operator_list.add("ge");
        operator_list.add("eq");

        //new adapter
        arr_adapter_operator = new ArrayAdapter<String>(CreateAlarmFragment.this.getActivity(),android.R.layout.simple_spinner_item,operator_list);
        // set the format of the adapter
        arr_adapter_operator.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set the adapter
        operator.setAdapter(arr_adapter_operator);

        operator.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String chooseName = operator_list.get(arg2);
                //set to show the chosen
                arg0.setVisibility(View.VISIBLE);
                //String
                instanceCA.chooseOperator = chooseName;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //list state

        final List<String> state_list;
        state_list = new ArrayList<String>();
        state_list.add("Select state please");
        ArrayAdapter<String> arr_adapter_state;

        state_list.add("insufficient data");
        state_list.add("ok");
        state_list.add("alarm");

        //new adapter
        arr_adapter_state = new ArrayAdapter<String>(CreateAlarmFragment.this.getActivity(),android.R.layout.simple_spinner_item,state_list);
        // set the format of the adapter
        arr_adapter_state.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set the adapter
        state.setAdapter(arr_adapter_state);

        state.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String chooseName = state_list.get(arg2);
                //set to show the chosen
                arg0.setVisibility(View.VISIBLE);
                //String
                instanceCA.chooseState = chooseName;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //list severity

        final List<String> severity_list;
        severity_list = new ArrayList<String>();
        severity_list.add("Select severity please");
        ArrayAdapter<String> arr_adapter_severity;

        severity_list.add("low");
        severity_list.add("critical");
        severity_list.add("moderate");

        //new adapter
        arr_adapter_severity = new ArrayAdapter<String>(CreateAlarmFragment.this.getActivity(),android.R.layout.simple_spinner_item,severity_list);
        // set the format of the adapter
        arr_adapter_severity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set the adapter
        severity.setAdapter(arr_adapter_severity);

        severity.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String chooseName = severity_list.get(arg2);
                //set to show the chosen
                arg0.setVisibility(View.VISIBLE);
                //String
                instanceCA.chooseSeverity = chooseName;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // set teh cancel button
        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabLeft.setVisibility(View.VISIBLE);
        fabLeft.setEnabled(true);

        fabLeft.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure to cancel?")
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FragmentManager manager = getFragmentManager();
                                AlarmFragment alarmFragment = new AlarmFragment();
                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, alarmFragment, alarmFragment.getTag()).commit();

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();

            }
        });

        /**
         * set create button onclick
         */

        create.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                setName = name.getText().toString();
                setDescription = description.getText().toString();
                setthresholdValue = Integer.parseInt(threshold.getText().toString());
                setgranularityValue = Integer.parseInt(granularity.getText().toString());

                boolean valid = checkInputValid();
                if(!valid){
                    Toast.makeText(getActivity().getApplicationContext(),"Please fill in necessary information", Toast.LENGTH_SHORT).show();

                } else {
                        mOverlayDialog.show();
                        HttpRequestController.getInstance(getActivity()
                                .getApplicationContext()).createAlarm(new HttpRequestController.VolleyCallback() {
                                                                          @Override
                                                                          public void onSuccess(String result) {
                                                                              if (result.equals("success")){
                                                                                  Toast.makeText(getActivity().getApplicationContext(),"Create Alarm successfully", Toast.LENGTH_SHORT).show();
                                                                                  TimerTask task = new TimerTask(){
                                                                                      @Override
                                                                                      public void run() {
                                                                                          mOverlayDialog.dismiss();
                                                                                          FragmentManager manager = getFragmentManager();
                                                                                          AlarmFragment alarmFragment = new AlarmFragment();
                                                                                          manager.beginTransaction().replace(R.id.relativelayout_for_fragment,alarmFragment,alarmFragment.getTag()).commit();

                                                                                      }
                                                                                  };
                                                                                  timer.schedule(task,4000);
                                                                              } else {
                                                                                  mOverlayDialog.dismiss();
                                                                              }
                                                                          }
                                                                      },setName,setDescription,chooseType,chooseMetric,setthresholdValue,chooseMethod,chooseOperator,setgranularityValue,chooseState,chooseSeverity);

                }
            }
        });
        return myView;

    }

    private boolean checkInputValid(){

        boolean valid=true;
        if(setName==" "){
            valid=false;
        }

        if(setDescription.equals(" ")){
            valid=false;
        }

        if(setthresholdValue==-1){
            valid=false;
        }
        if(setthresholdValue==-1){
            valid=false;
        }
        if(chooseType=="Select type please"||chooseType==" "){
            valid=false;
        }
        if(chooseMetric=="Select metric please"||chooseMetric==" "){
            valid = false;
        }
        if(chooseMethod=="Select method please"||chooseMethod==" "){
            valid = false;
        }
        if(chooseOperator=="Select operator please"||chooseOperator==" "){
            valid =false;
        }
        if(chooseState=="Select state please"|| chooseState==" "){
            valid=false;
        }
        if(chooseSeverity=="Select type please"|| chooseSeverity==" "){
            valid= false;
        }

        return valid;
    }

    @Override
    public void onPause() {
        /**
         *  Remove refresh button when this fragment is hiden.
         */
        super.onPause();
        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabLeft.setVisibility(View.GONE);
        fabLeft.setEnabled(false);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Nectar Cloud");
    }


}
