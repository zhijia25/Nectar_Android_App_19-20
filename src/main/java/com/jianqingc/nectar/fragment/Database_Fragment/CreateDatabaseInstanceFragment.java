package com.jianqingc.nectar.fragment.Database_Fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.jianqingc.nectar.R;
import com.jianqingc.nectar.httpRequest.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by HuangMengxue on 17/5/18.
 */

public class CreateDatabaseInstanceFragment extends Fragment{

    View myView;
    String newName;
    String newDescription;
    int newSize;
    String newZone;
    JSONArray azList;
    String newTypeID;
    JSONArray typeList;
    public String setName = " ";
    public String setDescription = " ";
    public String setDatastore = " ";
    public String networkID;
    JSONArray instanceResult;
    String selectDatastoreID;
    String selectDatastoreVersion;
    String selectDatastoreName;
    String InstanceLocality;
    JSONArray flavorList;
    String flavorRef;


    public CreateDatabaseInstanceFragment() {
        // Required empty public constructor

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_create_database_instance, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("New Instance");

        final EditText newNameCV= (EditText) myView.findViewById(R.id.newDatabaseInstanceNameCV);
        final EditText newVolumeSizeCV= (EditText) myView.findViewById(R.id.newVolumeSizeCV);
        final Spinner datastoreCV= (Spinner) myView.findViewById(R.id.datastoreCV);
        final Spinner localityCV= (Spinner) myView.findViewById(R.id.localityCV);
        final Spinner newAvailabilityCV= (Spinner) myView.findViewById(R.id.newDatabaseInstanceAvailabilityCV);


//        final EditText newDescriptionCV= (EditText) myView.findViewById(R.id.newDescriptionCV);
        final Button createVCV = (Button)myView.findViewById(R.id.createDatabaseInstanceVCV);

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);

        final java.util.Timer timer = new java.util.Timer(true);


        //List Availability zones
        HttpRequest.getInstance(getContext()).listAvailabilityZone(new HttpRequest.VolleyCallback() {
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
                    arr_adapter= new ArrayAdapter<String>(CreateDatabaseInstanceFragment.this.getActivity(), android.R.layout.simple_spinner_item, az_list);
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

        /*list database flavor
        * */
        HttpRequest.getInstance(getActivity().getApplicationContext()).listDatabaselistDatabaseFlavor(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d("databaseFlavor", result);
                try {
                    flavorList =  new JSONArray(result);
                    final List<String> link_list;
                    link_list = new ArrayList<String>();
                    for (int i = 0; i < flavorList.length(); i++) {
                        link_list.add(flavorList.getJSONObject(i).getString("databaseFlavorLink"));
                        flavorRef = link_list.get(0);
                    }

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }



                mOverlayDialog.dismiss();
            }
        }, getActivity());


        //List Databasestore
        HttpRequest.getInstance(getContext()).listDatastores(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */
                    final List<String> data_list;
                    data_list = new ArrayList<String>();
                    ArrayAdapter<String> arr_adapter;
                    final List<String> id_list;
                    final List<String> version_list= new ArrayList<String>();
                    final List<String> choice_list= new ArrayList<String>();
                    id_list = new ArrayList<String>();
                    data_list.add("Select an datastore");
                    choice_list.add("Select an datastore");
                    id_list.add("nothing");
                    version_list.add("nothing");

                    instanceResult = new JSONArray(result);
                    //System.out.println(falvorlist);
                    for (int i = 0; i < instanceResult.length(); i++) {
                        data_list.add(instanceResult.getJSONObject(i).getString("datastoreName"));
                        id_list.add(instanceResult.getJSONObject(i).getString("datastoreId"));
                        version_list.add(instanceResult.getJSONObject(i).getString("datastoreVersion"));
                        choice_list.add(instanceResult.getJSONObject(i).getString("datastoreName") + " " + instanceResult.getJSONObject(i).getString("datastoreVersion"));
                    }

                    //New an Adapter
                    arr_adapter = new ArrayAdapter<String>(CreateDatabaseInstanceFragment.this.getActivity(), android.R.layout.simple_spinner_item, choice_list);
                    //Set the format of the adapter
                    arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //set the adapter
                    datastoreCV.setAdapter(arr_adapter);

                    datastoreCV.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                            String chooseName = data_list.get(arg2);
                            //Set to show the chosen
                            arg0.setVisibility(View.VISIBLE);

                            for (int i = 0; i < data_list.size(); i++) {
                                if (data_list.get(i) == chooseName) {
                                    selectDatastoreID = id_list.get(i);
                                    selectDatastoreVersion = version_list.get(i);
                                    selectDatastoreName = chooseName;
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

        /*
        * list locality
        * */
        final List<String> locality_list;

        ArrayAdapter<String> arr_adapter_admin;
        locality_list = new ArrayList<String>();
        locality_list.add("None");
        locality_list.add("affinity");
        locality_list.add("anti-affinity");
        arr_adapter_admin = new ArrayAdapter<String>(CreateDatabaseInstanceFragment.this.getActivity(), android.R.layout.simple_spinner_item,locality_list);

        arr_adapter_admin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        localityCV.setAdapter(arr_adapter_admin);

        localityCV.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String chooselocality = locality_list.get(arg2);
                arg0.setVisibility(View.VISIBLE);
                InstanceLocality = chooselocality;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        /*
        * create database instance
        * */

        createVCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lowDatastoreName = selectDatastoreName.toLowerCase();
                newName=newNameCV.getText().toString();
//                newDescription=newDescriptionCV.getText().toString();
                String size=newVolumeSizeCV.getText().toString();

                if(newName.length()==0||size.length()==0||newZone.equals("Select a Availability Zone please")){
                    Toast.makeText(getActivity().getApplicationContext(),"Please fill in necessary information" , Toast.LENGTH_SHORT).show();
                }else{
                    newSize=Integer.parseInt(size);
                    if(newSize<=0){
                        Toast.makeText(getActivity().getApplicationContext(),"The size must be larger than 0." , Toast.LENGTH_SHORT).show();
                    }else{

                        mOverlayDialog.show();
                        HttpRequest.getInstance(getActivity().getApplicationContext()).createdatabaseInstance(new HttpRequest.VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                if (result.equals("success")) {
                                    Toast.makeText(getActivity().getApplicationContext(),"Create new instance successfully" , Toast.LENGTH_SHORT).show();
                                    TimerTask task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            mOverlayDialog.dismiss();
                                            FragmentTransaction ft = getActivity().getSupportFragmentManager()
                                                    .beginTransaction();
                                            DatabaseInstancesFragment vFragment = new DatabaseInstancesFragment();

                                            ft.replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();

                                        }
                                    };
                                    /**
                                     * Delay 7 secs after the button onclick method is called.
                                     * Wait for server status update. The server status is not modified in real-time.
                                     */
                                    timer.schedule(task, 4000);
                                } else {
                                    Toast.makeText(getActivity().getApplicationContext(),"Fail to create a new instance" , Toast.LENGTH_SHORT).show();
                                    mOverlayDialog.dismiss();
                                }
                            }

                        }, newName,newZone,selectDatastoreVersion, lowDatastoreName, newSize, InstanceLocality, flavorRef);


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
                                DatabaseInstancesFragment vFragment = new DatabaseInstancesFragment();
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
