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

/**
 * A simple {@link Fragment} subclass.

 * create an instance of this fragment.
 */
public class CreateContainerFragment extends Fragment {
    View myView;
    public static CreateContainerFragment instanceLI = null;
    public String containerName = " ";
    public String chooseAccess;

    List<String> accessType;

    public CreateContainerFragment(){
        instanceLI = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_create_container, container, false);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("New Container");

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);

        final EditText name = (EditText) myView.findViewById(R.id.newContainerName);
        final Spinner access = (Spinner) myView.findViewById(R.id.containerAccess);
        final Button create = (Button) myView.findViewById(R.id.createaContainerButton);
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
                                ContainerFragment containerFragment = new ContainerFragment();
                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, containerFragment, containerFragment.getTag()).commit();

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();

            }
        });

        final List<String> access_list;
        access_list = new ArrayList<String>();
        ArrayAdapter<String> arr_adapter_type;
        access_list.add("Private");
        access_list.add("Public");

        //new adapter
        arr_adapter_type = new ArrayAdapter<String>(CreateContainerFragment.this.getActivity(),android.R.layout.simple_spinner_item,access_list);
        // set the format of the adapter
        arr_adapter_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set the adapter
        access.setAdapter(arr_adapter_type);

        access.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String chooseAccess = access_list.get(arg2);
                //System.out.println("check:"+chooseName);
                //set to show the chosen
                arg0.setVisibility(View.VISIBLE);
                //String
                instanceLI.chooseAccess = chooseAccess;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        create.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                containerName = name.getText().toString();
                boolean valid = checkInputValid();
                if(!valid){
                    Toast.makeText(getActivity().getApplicationContext(),"Please fill in necessary information", Toast.LENGTH_SHORT).show();

                } else {
                    mOverlayDialog.show();
                    HttpRequestController.getInstance(getActivity().getApplicationContext()).createContainer(new HttpRequestController.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            if (result.equals("success")){
                                Toast.makeText(getActivity().getApplicationContext(),"create Container successfully", Toast.LENGTH_SHORT).show();
                                TimerTask task = new TimerTask(){
                                    @Override
                                    public void run() {
                                        mOverlayDialog.dismiss();
                                        FragmentManager manager = getFragmentManager();
                                        ContainerFragment containerFragment = new ContainerFragment();
                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, containerFragment, containerFragment.getTag()).commit();

                                    }
                                };
                                timer.schedule(task,4000);
                            } else {
                                mOverlayDialog.dismiss();
                            }
                        }
                    },containerName,chooseAccess);
                }

            }
        });
        return myView;

    }

    private boolean checkInputValid(){

        boolean valid=true;
        if(containerName==" "){
            valid=false;
        }

        return valid;
    }
}