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
 * Created by HuangMengxue on 17/5/18.
 */

public class CreateVolumeFragment extends Fragment{

    View myView;
    String newName;
    String newDescription;
    int newSize;
    String newZone;
    JSONArray azList;
    String newTypeID;
    JSONArray typeList;
    public CreateVolumeFragment() {
        // Required empty public constructor

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_create_volume, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("New Volume");

        final Spinner typeCV= (Spinner) myView.findViewById(R.id.typeCV);
        final Spinner newAvailabilityCV= (Spinner) myView.findViewById(R.id.newAvailabilityCV);
        final EditText newSizeCV= (EditText) myView.findViewById(R.id.newSizeCV);
        final EditText newNameCV= (EditText) myView.findViewById(R.id.newNameCV);
        final EditText newDescriptionCV= (EditText) myView.findViewById(R.id.newDescriptionCV);
        final Button createVCV = (Button)myView.findViewById(R.id.createVCV);

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);

        final java.util.Timer timer = new java.util.Timer(true);


        //List Availability zones
        HttpRequestController.getInstance(getContext()).listAvailabilityZone(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */
                    final List<String> az_list;
                    az_list = new ArrayList<String>();
                    az_list.add("Select a Availability Zone please");
                    ArrayAdapter<String> arr_adapter;

                    azList = new JSONArray(result);
                    //System.out.println(kpList);
                    for (int i = 0; i < azList.length(); i++) {
                        if(azList.getJSONObject(i).getBoolean("azState")==true){
                            az_list.add(azList.getJSONObject(i).getString("azName"));
                        }
                    }

                    //New an Adapter
                    arr_adapter= new ArrayAdapter<String>(CreateVolumeFragment.this.getActivity(), android.R.layout.simple_spinner_item, az_list);
                    //Set the format of the adapter
                    arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //set the adapter
                    newAvailabilityCV.setAdapter(arr_adapter);

                    newAvailabilityCV.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                            String chooseName = az_list.get(arg2);
                            //Set to show the chosen
                            arg0.setVisibility(View.VISIBLE);
                            //String flavorID;
                            newZone=chooseName;
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                            // TODO Auto-generated method stub
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, getActivity());


        //List Availability zones
        HttpRequestController.getInstance(getContext()).listVolumeType(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */
                    final List<String> type_list;
                    type_list = new ArrayList<String>();
                    final List<String> id_list;
                    id_list = new ArrayList<String>();
                    type_list.add("Select a type please");
                    id_list.add("None");
                    ArrayAdapter<String> arr_adapter;

                    typeList = new JSONArray(result);
                    //System.out.println(kpList);
                    for (int i = 0; i < typeList.length(); i++) {

                        type_list.add(typeList.getJSONObject(i).getString("typeName"));
                        id_list.add(typeList.getJSONObject(i).getString("typeId"));

                    }

                    //New an Adapter
                    arr_adapter= new ArrayAdapter<String>(CreateVolumeFragment.this.getActivity(), android.R.layout.simple_spinner_item, type_list);
                    //Set the format of the adapter
                    arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //set the adapter
                    typeCV.setAdapter(arr_adapter);

                    typeCV.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                            String chooseName = type_list.get(arg2);
                            //Set to show the chosen
                            arg0.setVisibility(View.VISIBLE);
                            for(int i = 1; i < type_list.size(); i++){
                                if(type_list.get(i)==chooseName){
                                    newTypeID=id_list.get(i);
                                }
                            }
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                            // TODO Auto-generated method stub
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, getActivity());



        createVCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newName=newNameCV.getText().toString();
                newDescription=newDescriptionCV.getText().toString();
                String size=newSizeCV.getText().toString();

                if(newName.length()==0||size.length()==0||newZone.equals("Select a Availability Zone please")){
                    Toast.makeText(getActivity().getApplicationContext(),"Please fill in necessary information" , Toast.LENGTH_SHORT).show();
                }else{
                    newSize=Integer.parseInt(size);
                    if(newSize<=0){
                        Toast.makeText(getActivity().getApplicationContext(),"The size must be larger than 0." , Toast.LENGTH_SHORT).show();
                    }else{


                        mOverlayDialog.show();
                        HttpRequestController.getInstance(getActivity().getApplicationContext()).createVolume(new HttpRequestController.VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                if (result.equals("success")) {
                                    Toast.makeText(getActivity().getApplicationContext(),"Create new volume successfully" , Toast.LENGTH_SHORT).show();
                                    TimerTask task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            mOverlayDialog.dismiss();
                                            FragmentTransaction ft = getActivity().getSupportFragmentManager()
                                                    .beginTransaction();
                                            VolumeFragment vFragment = new VolumeFragment();

                                            ft.replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();

                                        }
                                    };
                                    /**
                                     * Delay 7 secs after the button onclick method is called.
                                     * Wait for server status update. The server status is not modified in real-time.
                                     */
                                    timer.schedule(task, 4000);
                                } else {
                                    Toast.makeText(getActivity().getApplicationContext(),"Fail to create a new volume" , Toast.LENGTH_SHORT).show();
                                    mOverlayDialog.dismiss();
                                }
                            }

                        }, newName,newDescription,newSize,newZone,newTypeID);


                    }
                }


            }
        });



        //Set the cancel button
        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabLeft.setVisibility(View.VISIBLE);
        fabLeft.setEnabled(true);

        fabLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure to cancel?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FragmentManager manager = getFragmentManager();
                                VolumeFragment vFragment = new VolumeFragment();
                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

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
