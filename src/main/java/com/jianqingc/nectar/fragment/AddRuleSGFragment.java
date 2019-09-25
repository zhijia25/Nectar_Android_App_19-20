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
 * Created by HuangMengxue on 17/5/11.
 */

public class AddRuleSGFragment extends Fragment{

    View myView;
    String sgID;
    Bundle bundle;
    String chooseRule;
    String chooseDirection;
    String chooseMinPort;
    String chooseMaxPort;
    String chooseCIDR;

    public AddRuleSGFragment() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_add_rule_sg, container, false);
        bundle = getArguments();
        sgID = bundle.getString("securityGroupId");

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("New Rule");

        /**
         * set spinner which is actually a dialog
         */
        final Spinner rule= (Spinner) myView.findViewById(R.id.newRuleSG);
        final Spinner direction= (Spinner) myView.findViewById(R.id.newDirectionSG);
        final EditText fromPort= (EditText) myView.findViewById(R.id.newFromPort);
        final EditText toPort= (EditText) myView.findViewById(R.id.newToPort);
        final Spinner cidr= (Spinner) myView.findViewById(R.id.newCIDRSG);
        final Button create = (Button)myView.findViewById(R.id.addRuleSG);

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);

        final java.util.Timer timer = new java.util.Timer(true);


        final List<String> rule_list;
        rule_list = new ArrayList<String>();
        rule_list.add("Select a Rule");
        rule_list.add("Custom TCP Rule");
        rule_list.add("Custom UDP Rule");
        ArrayAdapter<String> rule_adapter;

        //New an Adapter
        rule_adapter= new ArrayAdapter<String>(AddRuleSGFragment.this.getActivity(), android.R.layout.simple_spinner_item, rule_list);
        //Set the format of the adapter
        rule_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set the adapter
        rule.setAdapter(rule_adapter);

        rule.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String pickRule = rule_list.get(arg2);
                //Set to show the chosen
                arg0.setVisibility(View.VISIBLE);
                chooseRule=pickRule;

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        final List<String> direction_list;
        direction_list = new ArrayList<String>();
        direction_list.add("Select a Direction");
        direction_list.add("Ingress");
        direction_list.add("Egress");
        ArrayAdapter<String> dir_adapter;

        //New an Adapter
        dir_adapter= new ArrayAdapter<String>(AddRuleSGFragment.this.getActivity(), android.R.layout.simple_spinner_item, direction_list);
        //Set the format of the adapter
        dir_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set the adapter
        direction.setAdapter(dir_adapter);

        direction.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String pickDir = direction_list.get(arg2);
                //Set to show the chosen
                arg0.setVisibility(View.VISIBLE);
                chooseDirection=pickDir;

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        final List<String> cidr_list;
        cidr_list = new ArrayList<String>();
        cidr_list.add("Classless Inter-Domain Routing");
        cidr_list.add("0.0.0.0/0");
        cidr_list.add("::/0");
        ArrayAdapter<String> cidr_adapter;

        //New an Adapter
        cidr_adapter= new ArrayAdapter<String>(AddRuleSGFragment.this.getActivity(), android.R.layout.simple_spinner_item, cidr_list);
        //Set the format of the adapter
        cidr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set the adapter
        cidr.setAdapter(cidr_adapter);

        cidr.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String pickCidr = cidr_list.get(arg2);
                //Set to show the chosen
                arg0.setVisibility(View.VISIBLE);
                chooseCIDR=pickCidr;

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });


        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseMinPort=fromPort.getText().toString();
                chooseMaxPort=toPort.getText().toString();

                if(chooseRule.equals("Select a Rule")||chooseDirection.equals("Select a Direction")||chooseCIDR.equals("Classless Inter-Domain Routing")){
                    Toast.makeText(getActivity().getApplicationContext(),"Please fill in necessary information" , Toast.LENGTH_SHORT).show();
                }else{
                    if(Integer.parseInt(chooseMinPort)>Integer.parseInt(chooseMaxPort)){
                        Toast.makeText(getActivity().getApplicationContext(),"The \"to\" port number must be greater than or equal to the \"from\" port number." , Toast.LENGTH_SHORT).show();
                    }else{
                        String protocol;
                        String dir;
                        String ethertype;
                        if(chooseRule.equals("Custom TCP Rule")){
                            protocol="tcp";
                        }else{
                            protocol="udp";
                        }

                        if(chooseDirection.equals("Ingress")){
                            dir="ingress";
                        }else{
                            dir="egress";
                        }

                        if(chooseCIDR.equals("0.0.0.0/0")){
                            ethertype="IPv4";
                        }else{
                            ethertype="IPv6";
                        }

                        mOverlayDialog.show();
                        HttpRequestController.getInstance(getActivity().getApplicationContext()).addNewRule(new HttpRequestController.VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                if (result.equals("success")) {
                                    Toast.makeText(getActivity().getApplicationContext(),"Add new rule successfully" , Toast.LENGTH_SHORT).show();
                                    TimerTask task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            mOverlayDialog.dismiss();
                                            FragmentTransaction ft = getActivity().getSupportFragmentManager()
                                                    .beginTransaction();
                                            ManageRulesSGFragment mrFragment = new ManageRulesSGFragment();
                                            mrFragment.setArguments(bundle);
                                            ft.replace(R.id.relativelayout_for_fragment, mrFragment, mrFragment.getTag()).commit();

                                        }
                                    };
                                    /**
                                     * Delay 7 secs after the button onclick method is called.
                                     * Wait for server status update. The server status is not modified in real-time.
                                     */
                                    timer.schedule(task, 4000);
                                } else {

                                    mOverlayDialog.dismiss();
                                }
                            }

                        }, sgID,protocol,dir,chooseMinPort,chooseMaxPort,chooseCIDR,ethertype);


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
                                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                                        .beginTransaction();
                                ManageRulesSGFragment mrFragment = new ManageRulesSGFragment();
                                mrFragment.setArguments(bundle);
                                ft.replace(R.id.relativelayout_for_fragment, mrFragment, mrFragment.getTag()).commit();

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
