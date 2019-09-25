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
public class CopyContainerFragment extends Fragment {
    View myView;
    public static CopyContainerFragment instanceLI = null;
    JSONArray containerlist = null;
    public String chooseContainer;
    public String choosePath = " ";
    public String newObjectName = " ";
    public String preContainerName;
    public String objectName;
    Bundle bundle;

    public CopyContainerFragment(){
        instanceLI = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_copy_container, container, false);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Copy Object");
        /**
         * set spinner
         */

        bundle = getArguments();
        preContainerName = bundle.getString("containerName");
        objectName = bundle.getString("objectName");


        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);

        final EditText newName = (EditText) myView.findViewById(R.id.destinationContainerName);
        final EditText path = (EditText) myView.findViewById(R.id.copyPath);
        final Spinner newContainer = (Spinner) myView.findViewById(R.id.destinationContainer);
        final Button copy = (Button) myView.findViewById(R.id.copyObjectButton);
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

        HttpRequestController.getInstance(getContext()).listContainer(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                final List<String> container_list;
                System.out.println("night "+ result);
                container_list = new ArrayList<String>();
                ArrayAdapter<String> arr_adapter;

                try {
                    containerlist = new JSONArray(result);
                    System.out.println("length: "+ containerlist.length());
                    for(int i =0; i < containerlist.length(); i++){
                        container_list.add(containerlist.getJSONObject(i).getString("containerName"));
                        System.out.println("ccc: "+ containerlist.getJSONObject(i).getString("containerName"));
                    }
                    arr_adapter = new ArrayAdapter<String>(CopyContainerFragment.this.getActivity(), android.R.layout.simple_spinner_dropdown_item,container_list);
                    arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    newContainer.setAdapter(arr_adapter);
                    newContainer.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                        @Override
                        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                            chooseContainer = container_list.get(arg2);
                            arg0.setVisibility(View.VISIBLE);

                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, getActivity());

        copy.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                choosePath = path.getText().toString();
                newObjectName = newName.getText().toString();
                boolean valid = checkInputValid();
                if(!valid){
                    Toast.makeText(getActivity().getApplicationContext(),"Please fill in necessary information", Toast.LENGTH_SHORT).show();

                } else {
                    mOverlayDialog.show();
                    HttpRequestController.getInstance(getActivity().getApplicationContext()).copyObject(new HttpRequestController.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            if(result.equals("success")){
                                Toast.makeText(getActivity().getApplicationContext(), "Copy Object successfully", Toast.LENGTH_SHORT).show();
                                TimerTask task = new TimerTask() {
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
                    },preContainerName,objectName,chooseContainer,choosePath,newObjectName);
                    mOverlayDialog.dismiss();


                }


            }
        });
    return myView;
    }

    private boolean checkInputValid(){

        boolean valid=true;
        if(newObjectName==" "){
            valid=false;
        }

        return valid;
    }
}