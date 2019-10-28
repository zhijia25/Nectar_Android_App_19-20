package com.jianqingc.nectar.fragment.Database_Fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
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
import com.jianqingc.nectar.util.RadioAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

public class CreateConfigurationGroupFragment extends Fragment {
    View myView;

    Bundle bundle1;
    Bundle bundle2;
    public static CreateConfigurationGroupFragment instanceCS = null;
    public String chooseIPVersion;
    public String setAddress = " ";
    public String setName = " ";
    public String setDescription = " ";
    public String setDatastore = " ";
    public String networkID;
    JSONArray instanceResult;
    String selectDatastoreID;
    String selectDatastoreVersion;
    String selectDatastoreName;

    public Map versionMap = new HashMap<String,Integer>();

    List<String> ipversionList;

    private RadioAdapter adapter;

    public CreateConfigurationGroupFragment(){
        instanceCS = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_create_configuration_group,container,false);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("New Configuration Group");
//        bundle1 = getArguments();
//        networkID = bundle1.getString("networkID",networkID);

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);

        final EditText name = (EditText) myView.findViewById(R.id.newConfigGroupName);
        final EditText description = (EditText) myView.findViewById(R.id.ConfigGroupDescription);
        final Spinner datastore = (Spinner) myView.findViewById(R.id.ConfigGroupDatastore);

        final Button create = (Button) myView.findViewById(R.id.createaConfigGroupButton);

        final java.util.Timer timer = new java.util.Timer(true);

        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabLeft.setVisibility(View.VISIBLE);
        fabLeft.setEnabled(true);
        fabLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                ConfigurationGroupFragment configurationGroupFragment = new ConfigurationGroupFragment();
//                bundle2 = new Bundle();
//                bundle2.putString("networkID",networkID);
//                subnetFragment.setArguments(bundle2);
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, configurationGroupFragment, configurationGroupFragment.getTag()).commit();
            }
        });

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
                    arr_adapter = new ArrayAdapter<String>(CreateConfigurationGroupFragment.this.getActivity(), android.R.layout.simple_spinner_item, choice_list);
                    //Set the format of the adapter
                    arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //set the adapter
                    datastore.setAdapter(arr_adapter);

                    datastore.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
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



        create.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                setName = name.getText().toString();
                setDescription = description.getText().toString();
//                int versionNumber = (int) versionMap.get(chooseIPVersion);

                boolean vaild = checkInputValid();
                if(!vaild){
                    Toast.makeText(getActivity().getApplicationContext(), "Please fill in ncecessary information", Toast.LENGTH_SHORT).show();

                } else {
                    mOverlayDialog.show();
                    HttpRequest.getInstance(getActivity()).createConfigGroup(new HttpRequest.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            if(result.equals("success")){
                                Toast.makeText(getActivity().getApplicationContext(), "Create Configuration Group successfully", Toast.LENGTH_SHORT).show();
                                TimerTask task = new TimerTask(){
                                    @Override
                                    public void run() {
                                        mOverlayDialog.dismiss();
                                        FragmentManager manager = getFragmentManager();
//                                        bundle2 = new Bundle();
                                        ConfigurationGroupFragment configurationGroupFragment = new ConfigurationGroupFragment();
//                                        bundle2.putString("networkID",networkID);
//                                        subnetFragment.setArguments(bundle2);
                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, configurationGroupFragment, configurationGroupFragment.getTag()).commit();
                                    }
                                };
                                timer.schedule(task, 4000);
                            } else {
                                mOverlayDialog.dismiss();
                            }
                        }
                    },setName,selectDatastoreName, setDescription);
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
        return valid;
    }
    @Override
    public void onPause() {
        /**
         * remove refresh button when this fragment is hiden.
         */
        super.onPause();
        FloatingActionButton fabRight = (FloatingActionButton) getActivity().findViewById(R.id.fabRight);
        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabRight.setVisibility(View.GONE);
        fabRight.setEnabled(false);
        fabLeft.setVisibility(View.GONE);
        Toolbar toolbar =(Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Nectar Cloud");
    }
}