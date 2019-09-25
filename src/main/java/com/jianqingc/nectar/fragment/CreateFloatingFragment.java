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

public class CreateFloatingFragment extends Fragment {
    View myView;
    public static CreateFloatingFragment instanceFL = null;
    public String choosePool;

    public String floating_network_id;

    List<String> poolName;

    private RadioAdapter adapter;

    public CreateFloatingFragment(){
        instanceFL = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_create_floating,container,false);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Allocate Floating IP");

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        // display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);

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
                                FloatingFragment floatingFragment = new FloatingFragment();
                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, floatingFragment, floatingFragment.getTag()).commit();

                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();

            }
        });



        final Spinner pool = (Spinner) myView.findViewById(R.id.FloatingPool);

        final Button create = (Button) myView.findViewById(R.id.createaFloatingButton);

        final java.util.Timer timer = new java.util.Timer(true);

        final List<String> pool_list;


        pool_list = new ArrayList<String>();

        ArrayAdapter<String> arr_adapter_pool;

        pool_list.add("QRIScloud");

        pool_list.add("melbourne");

        pool_list.add("tasmania");

        arr_adapter_pool = new ArrayAdapter<String>(CreateFloatingFragment.this.getActivity(),android.R.layout.simple_spinner_item,pool_list);
        arr_adapter_pool.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pool.setAdapter(arr_adapter_pool);



        pool.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String choosePool = pool_list.get(arg2);
                arg0.setVisibility(View.VISIBLE);
                instanceFL.choosePool = choosePool;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //set the cancel button





        create.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                if (choosePool.equals("QRIScloud")){
                    floating_network_id = "058b38de-830a-46ab-9d95-7a614cb06f1b";
                } else if (choosePool.equals("melbourne")) {
                    floating_network_id = "e48bdd06-cc3e-46e1-b7ea-64af43c74ef8";
                } else if (choosePool.equals("tasmania")) {
                    floating_network_id = "24dbaea8-c8ab-43dc-ba5c-0babc141c20e";

                }
                mOverlayDialog.show();
                HttpRequestController.getInstance(getActivity().getApplicationContext()).createFloatingIP(new HttpRequestController.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        if(result.equals("success")){
                            Toast.makeText(getActivity().getApplicationContext(), "Allocate Floating IP successfully", Toast.LENGTH_SHORT).show();
                            TimerTask task = new TimerTask() {
                                @Override
                                public void run() {
                                    mOverlayDialog.dismiss();
                                    FragmentManager manager = getFragmentManager();
                                    FloatingFragment floatingFragment = new FloatingFragment();
                                    manager.beginTransaction().replace(R.id.relativelayout_for_fragment, floatingFragment,floatingFragment.getTag()).commit();

                                }
                            };
                            timer.schedule(task,4000);
                        } else {
                            mOverlayDialog.dismiss();
                        }
                    }
                }, floating_network_id);
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
        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabLeft.setVisibility(View.GONE);
        fabLeft.setEnabled(false);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Nectar Cloud");
    }
}