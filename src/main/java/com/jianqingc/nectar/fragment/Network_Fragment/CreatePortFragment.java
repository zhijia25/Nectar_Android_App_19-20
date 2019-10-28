package com.jianqingc.nectar.fragment.Network_Fragment;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

public class CreatePortFragment extends Fragment {
    View myView;

    Bundle bundle1;
    Bundle bundle2;
    public static CreatePortFragment instanceCS = null;
//    public String chooseIPVersion;
//    public String setAddress = " ";
    public String setName = " ";
    String chooseAdminState;
    public String networkID;
    boolean admin_state;

    public Map versionMap = new HashMap<String,Integer>();

    List<String> adminState_list;

    private RadioAdapter adapter;

    public CreatePortFragment(){
        instanceCS = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_create_port,container,false);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("New Port");
        bundle1 = getArguments();
        networkID = bundle1.getString("networkID",networkID);

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);

        final EditText name = (EditText) myView.findViewById(R.id.newPortName);
//        final EditText networkAddress = (EditText) myView.findViewById(R.id.SubnetNetworkAddress);
        final Spinner adminState = (Spinner) myView.findViewById(R.id.newPortAdminState);

        final Button create = (Button) myView.findViewById(R.id.createaPortButton);

        final java.util.Timer timer = new java.util.Timer(true);

        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabLeft.setVisibility(View.VISIBLE);
        fabLeft.setEnabled(true);
        fabLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                PortFragment portFragment = new PortFragment();
                bundle2 = new Bundle();
                bundle2.putString("networkID",networkID);
                portFragment.setArguments(bundle2);
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, portFragment, portFragment.getTag()).commit();
            }
        });


        ArrayAdapter<String> arr_adapter_admin;
        adminState_list = new ArrayList<String>();
        adminState_list.add("UP");
        adminState_list.add("DOWN");
        arr_adapter_admin = new ArrayAdapter<String>(CreatePortFragment.this.getActivity(), android.R.layout.simple_spinner_item,adminState_list);

        arr_adapter_admin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        adminState.setAdapter(arr_adapter_admin);

        adminState.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                chooseAdminState = adminState_list.get(arg2);
                arg0.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        create.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                setName = name.getText().toString();
//                setAddress = networkAddress.getText().toString();

                if(chooseAdminState.equals("UP")){
                    admin_state = true;
                } else {
                    admin_state = false;
                }

                boolean vaild = checkInputValid();
                if(!vaild){
                    Toast.makeText(getActivity().getApplicationContext(), "Please fill in ncecessary information", Toast.LENGTH_SHORT).show();

                } else {
                    mOverlayDialog.show();
                    HttpRequest.getInstance(getActivity()).createPort(new HttpRequest.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            if(result.equals("success")){
                                Toast.makeText(getActivity().getApplicationContext(), "Create Port successfully", Toast.LENGTH_SHORT).show();
                                TimerTask task = new TimerTask(){
                                    @Override
                                    public void run() {
                                        mOverlayDialog.dismiss();
                                        FragmentManager manager = getFragmentManager();
                                        bundle2 = new Bundle();
                                        PortFragment portFragment = new PortFragment();
                                        bundle2.putString("networkID",networkID);
                                        portFragment.setArguments(bundle2);
                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, portFragment, portFragment.getTag()).commit();
                                    }
                                };
                                timer.schedule(task, 4000);
                            } else {
                                mOverlayDialog.dismiss();
                            }
                        }
                    },setName,networkID,admin_state);
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