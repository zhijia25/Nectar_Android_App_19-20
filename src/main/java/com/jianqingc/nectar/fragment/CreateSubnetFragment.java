package com.jianqingc.nectar.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import java.util.HashMap;
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

import java.util.Map;
import java.util.TimerTask;

public class CreateSubnetFragment extends Fragment {
    View myView;

    Bundle bundle1;
    Bundle bundle2;
    public static CreateSubnetFragment instanceCS = null;
    public String chooseIPVersion;
    public String setAddress = " ";
    public String setName = " ";
    public String networkID;

    public Map versionMap = new HashMap<String,Integer>();

    List<String> ipversionList;

    private RadioAdapter adapter;

    public CreateSubnetFragment(){
        instanceCS = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_create_subnet,container,false);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("New Subnet");
        bundle1 = getArguments();
        networkID = bundle1.getString("networkID",networkID);

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);

        final EditText name = (EditText) myView.findViewById(R.id.newSubnetName);
        final EditText networkAddress = (EditText) myView.findViewById(R.id.SubnetNetworkAddress);
        final Spinner ipversion = (Spinner) myView.findViewById(R.id.SubnetIPVersion);

        final Button create = (Button) myView.findViewById(R.id.createaSubnetButton);

        final java.util.Timer timer = new java.util.Timer(true);

        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabLeft.setVisibility(View.VISIBLE);
        fabLeft.setEnabled(true);
        fabLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                SubnetFragment subnetFragment = new SubnetFragment();
                bundle2 = new Bundle();
                bundle2.putString("networkID",networkID);
                subnetFragment.setArguments(bundle2);
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, subnetFragment, subnetFragment.getTag()).commit();
            }
        });


        final List<String> ipversion_list;
        ipversion_list = new ArrayList<String>();

        ipversion_list.add("IPv4");
        ipversion_list.add("IPv6");

        versionMap.put("IPv4",4);
        versionMap.put("IPv6",6);

        ArrayAdapter<String> arr_adapter_version;

        arr_adapter_version = new ArrayAdapter<String>(CreateSubnetFragment.this.getActivity(), android.R.layout.simple_spinner_item, ipversion_list);

        arr_adapter_version.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ipversion.setAdapter(arr_adapter_version);

        ipversion.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long l) {
                String chooseVersion = ipversion_list.get(arg2);
                arg0.setVisibility(View.VISIBLE);
                instanceCS.chooseIPVersion = chooseVersion;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        create.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                setName = name.getText().toString();
                setAddress = networkAddress.getText().toString();
                int versionNumber = (int) versionMap.get(chooseIPVersion);

                boolean vaild = checkInputValid();
                if(!vaild){
                    Toast.makeText(getActivity().getApplicationContext(), "Please fill in ncecessary information", Toast.LENGTH_SHORT).show();

                } else {
                    mOverlayDialog.show();
                    HttpRequestController.getInstance(getActivity()).createSubnet(new HttpRequestController.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            if(result.equals("success")){
                                Toast.makeText(getActivity().getApplicationContext(), "Create Subnet successfully", Toast.LENGTH_SHORT).show();
                                TimerTask task = new TimerTask(){
                                    @Override
                                    public void run() {
                                        mOverlayDialog.dismiss();
                                        FragmentManager manager = getFragmentManager();
                                        bundle2 = new Bundle();
                                        SubnetFragment subnetFragment = new SubnetFragment();
                                        bundle2.putString("networkID",networkID);
                                        subnetFragment.setArguments(bundle2);
                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, subnetFragment, subnetFragment.getTag()).commit();
                                    }
                                };
                                timer.schedule(task, 4000);
                            } else {
                                mOverlayDialog.dismiss();
                            }
                        }
                    },setName,networkID,setAddress,versionNumber);
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
        if (setAddress.equals(" ")){
            valid = false;
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