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
 * A simple {@link Fragment} subclass.

 */
public class CreateObjectFragment extends Fragment {
    View myView;
    public static CreateObjectFragment ObjectLI = null;

    Bundle bundle;
    Bundle bundle2;

    public String ObjectName = " ";

    public String containerName;



    public CreateObjectFragment() {
        // Required empty public constructor
        ObjectLI = this;
    }




    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_create_object, container, false);
        bundle = getArguments();
        containerName = bundle.getString("containerName");

        Toolbar toolbar =(Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Create pseudi-Object");

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);

        final EditText name = (EditText) myView.findViewById(R.id.newObjectFileName);

        final Button create = (Button)myView.findViewById(R.id.createFile);

        final java.util.Timer timer = new java.util.Timer(true);

        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabLeft.setVisibility(View.VISIBLE);
        fabLeft.setEnabled(true);
        fabLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                ObjectFragment objectFragment = new ObjectFragment();
                bundle2 = new Bundle();
                bundle2.putString("containerName",containerName);
                objectFragment.setArguments(bundle2);

                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, objectFragment, objectFragment.getTag()).commit();
            }
        });

        create.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ObjectName = name.getText().toString();
                boolean valid = checkInputValid();
                if(!valid){
                    Toast.makeText(getActivity().getApplicationContext(),"Please fill in necessary information", Toast.LENGTH_SHORT).show();

                } else {
                    mOverlayDialog.show();
                    HttpRequestController.getInstance(getActivity().getApplicationContext()).createObjectFile(new HttpRequestController.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            if (result.equals("success")){
                                Toast.makeText(getActivity().getApplicationContext(),"create Object successfully", Toast.LENGTH_SHORT).show();
                                TimerTask task = new TimerTask(){
                                    @Override
                                    public void run() {
                                        mOverlayDialog.dismiss();
                                        FragmentManager manager = getFragmentManager();
                                        ObjectFragment objectFragment = new ObjectFragment();
                                        bundle2 = new Bundle();
                                        bundle2.putString("containerName",containerName);
                                        objectFragment.setArguments(bundle2);
                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, objectFragment, objectFragment.getTag()).commit();

                                    }
                                };
                                timer.schedule(task,4000);
                            } else {
                                mOverlayDialog.dismiss();
                            }
                        }
                    },containerName,ObjectName);
                }

            }
        });

        return myView;

    }

    private boolean checkInputValid(){

        boolean valid=true;
        if(ObjectName==" "){
            valid=false;
        }

        return valid;
    }


}
