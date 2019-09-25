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

public class CreateRouterFragment extends Fragment {
    View myView;
    public static CreateRouterFragment instanceCR = null;
    public String routerName =" ";
    public String chooseAdminState;
    public String chooseExternalNetwork;
    public String networkID;
    public boolean admin_state;

    List<String> adminState;
    List<String> externalNetwork;

    private RadioAdapter adapter;

    public CreateRouterFragment(){
        instanceCR = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_create_router, container, false);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("New Router");

        //set spinner

        final Dialog mOverlayDialog = new Dialog(getActivity(),android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);

        final EditText name = (EditText) myView.findViewById(R.id.newRouterName);
        final Spinner adminState = (Spinner) myView.findViewById(R.id.RouterAdminState);
        final Spinner externalNetwork = (Spinner) myView.findViewById(R.id.RouterExternal);

        final Button create = (Button) myView.findViewById(R.id.createaRouterButton);

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
                                RouterFragment routerFragment = new RouterFragment();
                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, routerFragment, routerFragment.getTag()).commit();

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();

            }
        });


        final java.util.Timer timer = new java.util.Timer(true);

        final List<String> adminState_list;

        ArrayAdapter<String> arr_adapter_admin;
        adminState_list = new ArrayList<String>();
        adminState_list.add("UP");
        adminState_list.add("DOWN");
        arr_adapter_admin = new ArrayAdapter<String>(CreateRouterFragment.this.getActivity(), android.R.layout.simple_spinner_item,adminState_list);

        arr_adapter_admin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adminState.setAdapter(arr_adapter_admin);

        adminState.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String chooseAdminState = adminState_list.get(arg2);
                arg0.setVisibility(View.VISIBLE);
                instanceCR.chooseAdminState = chooseAdminState;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final List<String> externalNetwork_list;
        externalNetwork_list = new ArrayList<String>();
        externalNetwork_list.add("QRIScloud");
        externalNetwork_list.add("melbourne");
        externalNetwork_list.add("tasmania");
        ArrayAdapter<String> arr_adapter_external;

        arr_adapter_external = new ArrayAdapter<String>(CreateRouterFragment.this.getActivity(),android.R.layout.simple_spinner_item,externalNetwork_list);
        externalNetwork.setAdapter(arr_adapter_external);

        externalNetwork.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String chooseExternalNetwork = externalNetwork_list.get(arg2);
                arg0.setVisibility(View.VISIBLE);
                instanceCR.chooseExternalNetwork = chooseExternalNetwork;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        create.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (chooseExternalNetwork.equals("QRIScloud")){
                    networkID = "058b38de-830a-46ab-9d95-7a614cb06f1b";
                } else if (chooseExternalNetwork.equals("melbourne")) {
                    networkID = "e48bdd06-cc3e-46e1-b7ea-64af43c74ef8";
                } else if (chooseExternalNetwork.equals("tasmania")) {
                    networkID = "24dbaea8-c8ab-43dc-ba5c-0babc141c20e";

                }

                if(chooseAdminState.equals("UP")){
                    admin_state = true;
                } else {
                    admin_state = false;
                }

                routerName = name.getText().toString();
                boolean valid = checkInputValid();
                if(!valid){
                    Toast.makeText(getActivity().getApplicationContext(),"Please fill in necessary information", Toast.LENGTH_SHORT).show();

                } else {
                    mOverlayDialog.show();
                    HttpRequestController.getInstance(getActivity().getApplicationContext()).createRouter(new HttpRequestController.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            if(result.equals("success")){
                                Toast.makeText(getActivity().getApplicationContext(), " Create Router successfully", Toast.LENGTH_SHORT).show();
                                TimerTask task = new TimerTask(){
                                    @Override
                                    public void run() {
                                        mOverlayDialog.dismiss();
                                        FragmentManager manager = getFragmentManager();
                                        RouterFragment routerFragment = new RouterFragment();
                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, routerFragment,routerFragment.getTag()).commit();

                                    }

                                };
                                timer.schedule(task, 4000);
                            } else {
                                mOverlayDialog.dismiss();
                            }
                        }
                    },routerName,networkID,admin_state);

                }
            }
        });
        return myView;



    }

    private boolean checkInputValid(){

        boolean valid=true;
        if(routerName==" "){
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