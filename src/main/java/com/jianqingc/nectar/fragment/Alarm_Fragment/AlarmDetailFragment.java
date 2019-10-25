package com.jianqingc.nectar.fragment.Alarm_Fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jianqingc.nectar.R;

/**
 * A simple {@link Fragment} subclass.

 * create an instance of this fragment.
 */
public class AlarmDetailFragment extends Fragment {
    Bundle bundle;
    View myView;
    String alarmID;
    String alarmName;
    String alarmDescription;
    String alarmType;
    String alarmMetric;

    String alarmThreshold;

    String alarmMethod;
    String alarmOperator;
    String alarmGranularity;

    String alarmState;
    String alarmSeverity;

    public AlarmDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_alarm_detail, container, false);
        bundle = getArguments();
        alarmName = bundle.getString("alarmName");
        alarmDescription = bundle.getString("description");
        alarmType = bundle.getString("alarmType");
        alarmMetric = bundle.getString("alarmMetric");
        alarmThreshold = bundle.getString("alarmThreshold");

        alarmMethod = bundle.getString("alarmMethod");
        alarmOperator = bundle.getString("comparison_operator");
        alarmGranularity = bundle.getString("alarmGranularity");
        alarmState = bundle.getString("alarmState");
        alarmSeverity = bundle.getString("alarmSeverity");

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Alarm Detail");

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabLeft.setVisibility(View.VISIBLE);
        fabLeft.setEnabled(true);
        fabLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                AlarmFragment alarmFragment = new AlarmFragment();
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, alarmFragment, alarmFragment.getTag()).commit();
            }
        });

        TextView nameTV =(TextView)myView.findViewById(R.id.alarmNameID);
        TextView descriptionTV = (TextView)myView.findViewById(R.id.alarmDescriptionID);
        TextView typeTV = (TextView)myView.findViewById(R.id.alarmTypeID);
        TextView metricTV = (TextView)myView.findViewById(R.id.alarmMetricID);
        TextView threholdTV = (TextView)myView.findViewById(R.id.alarmThresholdID);
        TextView methodTV = (TextView)myView.findViewById(R.id.alarmaggMethodID);
        TextView operatorTV = (TextView)myView.findViewById(R.id.alarmOperatorID);
        TextView granularityTV = (TextView)myView.findViewById(R.id.alarmGanularityID);
        TextView stateTV = (TextView)myView.findViewById(R.id.alarmStateID);
        TextView severityTV = (TextView)myView.findViewById(R.id.alarmSeverityID);

        nameTV.setText(alarmName);
        System.out.println("alarmName: "+alarmName);
        System.out.println("alarmDes: "+alarmDescription);
        descriptionTV.setText(alarmDescription);
        typeTV.setText(alarmType);
        metricTV.setText(alarmMetric);
        threholdTV.setText(alarmThreshold);
        methodTV.setText(alarmMethod);
        operatorTV.setText(alarmOperator);
        granularityTV.setText(alarmGranularity);
        stateTV.setText(alarmState);
        severityTV.setText(alarmSeverity);
        mOverlayDialog.dismiss();




        return myView;
    }

    @Override
    public void onPause() {
        /**
         *  Remove refresh button when this fragment is hiden.
         */
        super.onPause();
        FloatingActionButton fabRight = (FloatingActionButton) getActivity().findViewById(R.id.fabRight);
        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabRight.setVisibility(View.GONE);
        fabRight.setEnabled(false);
        fabLeft.setVisibility(View.GONE);
        fabLeft.setEnabled(false);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Nectar Cloud");
    }
}
