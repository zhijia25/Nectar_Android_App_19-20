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

public class CreateNetworkFragment extends Fragment {
    View myView;
    public static CreateNetworkFragment instanceCR = null;
    public String NetworkName =" ";
    public String chooseAdminState;
    public String chooseExternalNetwork;
    public String networkID;
    public boolean admin_state;

    List<String> adminState;
    List<String> externalNetwork;

    private RadioAdapter adapter;

    public CreateNetworkFragment(){
        instanceCR = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_create_network, container, false);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("New Network");

        //set spinner

        final Dialog mOverlayDialog = new Dialog(getActivity(),android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);

        final EditText name = (EditText) myView.findViewById(R.id.newNetworkName);
        final Spinner adminState = (Spinner) myView.findViewById(R.id.NetworkAdminState);


        final Button create = (Button) myView.findViewById(R.id.createaNetworkButton);


        final java.util.Timer timer = new java.util.Timer(true);

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
                                NetworkFragment networkFragment = new NetworkFragment();
                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, networkFragment, networkFragment.getTag()).commit();

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();

            }
        });

        final List<String> adminState_list;

        ArrayAdapter<String> arr_adapter_admin;
        adminState_list = new ArrayList<String>();
        adminState_list.add("UP");
        adminState_list.add("DOWN");
        arr_adapter_admin = new ArrayAdapter<String>(CreateNetworkFragment.this.getActivity(), android.R.layout.simple_spinner_item,adminState_list);

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



        create.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {


                if(chooseAdminState.equals("UP")){
                    admin_state = true;
                } else {
                    admin_state = false;
                }

                NetworkName = name.getText().toString();
                boolean valid = checkInputValid();
                if(!valid){
                    Toast.makeText(getActivity().getApplicationContext(),"Please fill in necessary information", Toast.LENGTH_SHORT).show();

                } else {
                    mOverlayDialog.show();
                    HttpRequestController.getInstance(getActivity().getApplicationContext()).createNetwork(new HttpRequestController.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            if(result.equals("success")){
                                Toast.makeText(getActivity().getApplicationContext(), " Create Network successfully", Toast.LENGTH_SHORT).show();
                                TimerTask task = new TimerTask(){
                                    @Override
                                    public void run() {
                                        mOverlayDialog.dismiss();
                                        FragmentManager manager = getFragmentManager();
                                        NetworkFragment NetworkFragment = new NetworkFragment();
                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, NetworkFragment,NetworkFragment.getTag()).commit();

                                    }

                                };
                                timer.schedule(task, 4000);
                            } else {
                                mOverlayDialog.dismiss();
                            }
                        }
                    },NetworkName,admin_state);

                }
            }
        });
        return myView;



    }

    private boolean checkInputValid(){

        boolean valid=true;
        if(NetworkName==" "){
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