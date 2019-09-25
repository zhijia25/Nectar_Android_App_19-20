package com.jianqingc.nectar.fragment;


import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.jianqingc.nectar.R;
import com.jianqingc.nectar.controller.HttpRequestController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;


/**
 * A simple {@link Fragment} subclass.
 */
public class VolumeFragment extends Fragment {

    View myView;
    JSONArray volumeFragmentResultArray;
    Bundle bundle;
    List<String> attachArray;
    String attachID;
    String attachmentID;
    String volumeID;
    JSONArray instanceResult;
    String selectInstanceID;
    boolean bootEV;
    String availibilityZoneVolume;
    String zoneServer;


    public VolumeFragment() {
        // Required empty public constructor
    }
    private void refresh(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this);
        ft.attach(this);
        ft.commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_volume, container, false);
        setHasOptionsMenu(true);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Volumes");

        final java.util.Timer timer = new java.util.Timer(true);
        final Button volume = (Button) myView.findViewById(R.id.volume_button1);
        final Button volumeS = (Button) myView.findViewById(R.id.volumeSnapshot_button1);
        volume.setEnabled(true);
        volume.setVisibility(View.VISIBLE);
        volumeS.setEnabled(true);
        volumeS.setVisibility(View.VISIBLE);
        volume.setBackgroundColor(Color.parseColor("#e6e6e6"));
        volume.setTextColor(Color.parseColor("#8c8c8c"));

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();
        // Inflate the layout for this fragment
        HttpRequestController.getInstance(getContext()).listVolume(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    volumeFragmentResultArray = new JSONArray(result);
                    attachArray=new ArrayList<String>();

                    ArrayList<String[]> volumeListArray = new ArrayList<String[]>();


                    //do something with jsonArray
                    for (int i=0; i<volumeFragmentResultArray.length();i++){
                        String status=volumeFragmentResultArray.getJSONObject(i).getString("volumeStatus");
                        String first = status.substring(0, 1).toUpperCase();
                        String rest = status.substring(1, status.length());
                        String newStatus = new StringBuffer(first).append(rest).toString();
                        String[] volumeList ={
                                volumeFragmentResultArray.getJSONObject(i).getString("volumeId"),
                                volumeFragmentResultArray.getJSONObject(i).getString("volumeSize"),
                                newStatus,
                                volumeFragmentResultArray.getJSONObject(i).getString("volumeName"),
                                volumeFragmentResultArray.getJSONObject(i).getString("volumeDescription")
                        };
                        volumeListArray.add(volumeList);
                    }
                    BindDictionary<String[]> dictionary = new BindDictionary<String[]>();
                    dictionary.addStringField(R.id.volumeIdTV, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return (volumeList[0]);
                        }
                    });
                    dictionary.addStringField(R.id.volumeSizeTV, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return (volumeList[1] +" GB");
                        }
                    });
                    dictionary.addStringField(R.id.volumeStatusTV, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return volumeList[2];
                        }
                    });
                    dictionary.addStringField(R.id.volumeNameTV, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return volumeList[3];
                        }
                    });
                    dictionary.addStringField(R.id.volumeDescriptionTV, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return volumeList[4];
                        }
                    });

                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */
                    FunDapter adapter = new FunDapter(VolumeFragment.this.getActivity(),volumeListArray,R.layout.volume_list_pattern,dictionary);
                    ListView volumeLV = (ListView) myView.findViewById(R.id.listViewVolume);
                    volumeLV.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    setListViewHeightBasedOnChildren(volumeLV);





                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            bundle = new Bundle();
                            try {
                                volumeID=volumeFragmentResultArray.getJSONObject(position).getString("volumeId");
                                availibilityZoneVolume=volumeFragmentResultArray.getJSONObject(position).getString("volumeAZ");
                                bundle.putString("volumeId" , volumeFragmentResultArray.getJSONObject(position).getString("volumeId"));


                                final Dialog dialogLV = new Dialog(getActivity(),R.style.ActionSheetDialogStyle);
                                View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.launch_volume_dialog, null);

                                TextView viewDetailV = (TextView) inflate.findViewById(R.id.detailVLV);
                                TextView extendVLV = (TextView) inflate.findViewById(R.id.extendVLV);
                                TextView editV = (TextView) inflate.findViewById(R.id.editVLV);
                                TextView attachV = (TextView) inflate.findViewById(R.id.AttachVLV);
                                TextView detachV=(TextView) inflate.findViewById(R.id.dettachVLV);
                                TextView snapshotV = (TextView) inflate.findViewById(R.id.snapshotVLV);
                                TextView deleteV = (TextView) inflate.findViewById(R.id.deleteVLV);

                                View attachL=(View) inflate.findViewById(R.id.AttachVLV1);
                                View dettachL=(View) inflate.findViewById(R.id.dettachVLV1);

                                int numA=volumeFragmentResultArray.getJSONObject(position).getInt("volumeNumAttach");
                                if(numA==0){
                                    detachV.setVisibility(View.GONE);
                                    dettachL.setVisibility(View.GONE);
                                }else{
                                    attachV.setVisibility(View.GONE);
                                    attachL.setVisibility(View.GONE);
                                    attachID=volumeFragmentResultArray.getJSONObject(position).getString("vserver"+0);
                                    attachmentID=volumeFragmentResultArray.getJSONObject(position).getString("vattach"+0);
                                }



                                viewDetailV.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        FragmentTransaction ft = getActivity().getSupportFragmentManager()
                                                .beginTransaction();
                                        VolumeDetailFragment volumeDetailFragment = new VolumeDetailFragment();
                                        volumeDetailFragment.setArguments(bundle);
                                        ft.replace(R.id.relativelayout_for_fragment, volumeDetailFragment,volumeDetailFragment.getTag());
                                        ft.commit();
                                        dialogLV.dismiss();

                                    }
                                });





                                extendVLV.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        /**
                                         * set Snapshot button onclick
                                         */
                                        final EditText input = new EditText(getActivity());
                                        final AlertDialog.Builder builderSnapshot = new AlertDialog.Builder(getActivity());
                                        builderSnapshot.setMessage("Please enter the new size of volume:").setView(input)
                                                .setPositiveButton("Extend Volume", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        String newSize = input.getText().toString();
                                                        if(newSize.length()==0){
                                                            Toast.makeText(getActivity().getApplicationContext(), "Please enter a new size", Toast.LENGTH_SHORT).show();
                                                            dialog.dismiss();
                                                        }else{
                                                            int sizeInt=Integer.parseInt(newSize);
                                                            //System.out.println(sizeInt);
                                                            dialog.dismiss();
                                                            mOverlayDialog.show();
                                                            HttpRequestController.getInstance(getActivity().getApplicationContext()).extendVolume(new HttpRequestController.VolleyCallback() {
                                                                @Override
                                                                public void onSuccess(String result) {
                                                                    if(result.equals("success")) {

                                                                        Toast.makeText(getActivity().getApplicationContext(), "Extend Volume successfully", Toast.LENGTH_SHORT).show();
                                                                        TimerTask task = new TimerTask() {
                                                                            @Override
                                                                            public void run() {
                                                                                mOverlayDialog.dismiss();
                                                                                FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                                VolumeFragment vFragment = new VolumeFragment();
                                                                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();

                                                                            }
                                                                        };
                                                                        /**
                                                                         * Delay 7 secs after the button onclick method is called.
                                                                         * Wait for server status update. The server status is not modified in real-time.
                                                                         */
                                                                        timer.schedule(task, 4000);


                                                                    } else{
                                                                        mOverlayDialog.dismiss();
                                                                        Toast.makeText(getActivity().getApplicationContext(), "Fail to extend volume", Toast.LENGTH_SHORT).show();
                                                                        FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                        VolumeFragment vFragment = new VolumeFragment();
                                                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();

                                                                    }
                                                                }
                                                            }, sizeInt,volumeID);
                                                        }

                                                    }
                                                })
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                        AlertDialog alertDialog = builderSnapshot.create();
                                        alertDialog.show();
                                        dialogLV.dismiss();

                                    }
                                });





                                editV.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        LayoutInflater factory = LayoutInflater.from(getActivity());
                                        final View textEntryView = factory.inflate(R.layout.create_sg, null);
                                        final EditText nameV = (EditText) textEntryView.findViewById(R.id.createSGName);
                                        final EditText decriptionV = (EditText)textEntryView.findViewById(R.id.createSGDescription);
                                        //final CheckBox bootableV = (CheckBox)textEntryView.findViewById(R.id.bootableV);
                                        AlertDialog.Builder builderSecurityGroup = new AlertDialog.Builder(getActivity());
                                        builderSecurityGroup.setTitle("Edit Volume");
                                        builderSecurityGroup.setIcon(R.drawable.nectar_app_icon);
                                        //builderSecurityGroup.setIcon(android.R.drawable.ic_dialog_info);
                                        builderSecurityGroup.setView(textEntryView);

                                        //bootableV.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                                        //    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                // TODO Auto-generated method stub
                                         //       if(isChecked)
                                        //        {
                                         //           bootEV=true;
                                         //       }
                                         //       else{
                                         //           bootEV=false;
                                         ///       }
                                         //   }
                                       // });



                                        builderSecurityGroup.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                String nameNew= nameV.getText().toString();
                                                String descriptionNew= decriptionV.getText().toString();
                                                dialog.dismiss();
                                                mOverlayDialog.show();
                                                HttpRequestController.getInstance(getActivity().getApplicationContext()).editVolume(new HttpRequestController.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            if(result.equals("success")) {
                                                                //mOverlayDialog.dismiss();
                                                                Toast.makeText(getActivity().getApplicationContext(), "Edit Volume successfully", Toast.LENGTH_SHORT).show();
                                                                TimerTask task = new TimerTask() {
                                                                    @Override
                                                                    public void run() {
                                                                        mOverlayDialog.dismiss();
                                                                        FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                        VolumeFragment vFragment = new VolumeFragment();
                                                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
                                                                    }
                                                                };
                                                                /**
                                                                 * Delay 7 secs after the button onclick method is called.
                                                                 * Wait for server status update. The server status is not modified in real-time.
                                                                 */
                                                                timer.schedule(task, 4000);

                                                            } else{
                                                                mOverlayDialog.dismiss();
                                                                Toast.makeText(getActivity().getApplicationContext(), "Fail to edit volume", Toast.LENGTH_SHORT).show();
                                                                FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                VolumeFragment vFragment = new VolumeFragment();
                                                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();

                                                            }
                                                        }
                                                    },nameNew, descriptionNew,volumeID);



                                            }
                                        })
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                        AlertDialog alertDialog = builderSecurityGroup.create();
                                        alertDialog.show();

                                        dialogLV.dismiss();

                                    }
                                });



                                attachV.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        LayoutInflater factory = LayoutInflater.from(getActivity());
                                        final View textEntryView = factory.inflate(R.layout.attach_volume, null);
                                        final Spinner selectNameAV = (Spinner) textEntryView.findViewById(R.id.selectNameAV);
                                        AlertDialog.Builder builderSecurityGroup = new AlertDialog.Builder(getActivity());
                                        builderSecurityGroup.setTitle("Attach To Instance");
                                        builderSecurityGroup.setIcon(R.drawable.nectar_app_icon);
                                        //builderSecurityGroup.setIcon(android.R.drawable.ic_dialog_info);
                                        builderSecurityGroup.setView(textEntryView);

                                        //List flavors
                                        HttpRequestController.getInstance(getContext()).listInstance(new HttpRequestController.VolleyCallback() {
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
                                                    final List<String> zone_list= new ArrayList<String>();
                                                    id_list= new ArrayList<String>();
                                                    data_list.add("Select an instance");
                                                    id_list.add("nothing");
                                                    zone_list.add("nothing");

                                                    instanceResult = new JSONArray(result);
                                                    //System.out.println(falvorlist);
                                                    for (int i = 0; i < instanceResult.length(); i++) {
                                                        data_list.add(instanceResult.getJSONObject(i).getString("instanceName"));
                                                        id_list.add(instanceResult.getJSONObject(i).getString("instanceId"));
                                                        zone_list.add(instanceResult.getJSONObject(i).getString("zone"));

                                                    }

                                                    //New an Adapter
                                                    arr_adapter= new ArrayAdapter<String>(VolumeFragment.this.getActivity(), android.R.layout.simple_spinner_item, data_list);
                                                    //Set the format of the adapter
                                                    arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                    //set the adapter
                                                    selectNameAV.setAdapter(arr_adapter);

                                                    selectNameAV.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                                                        @Override
                                                        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                                            String chooseName = data_list.get(arg2);
                                                            //Set to show the chosen
                                                            arg0.setVisibility(View.VISIBLE);

                                                            for(int i = 0; i < data_list.size(); i++){
                                                                if(data_list.get(i)==chooseName){
                                                                    selectInstanceID=id_list.get(i);
                                                                    zoneServer=zone_list.get(i);
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


                                        builderSecurityGroup.setPositiveButton("Attach Volume", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if(selectInstanceID.equals("nothing")){
                                                    Toast.makeText(getActivity().getApplicationContext(), "Please select an instance", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                }else if(!zoneServer.equals(availibilityZoneVolume)){
                                                    Toast.makeText(getActivity().getApplicationContext(), "The availability zone of volume and instance must be the same!", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                }else{
                                                    dialog.dismiss();
                                                    mOverlayDialog.show();
                                                    HttpRequestController.getInstance(getActivity().getApplicationContext()).attachVolume(new HttpRequestController.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            if(result.equals("success")) {
                                                                //mOverlayDialog.dismiss();
                                                                Toast.makeText(getActivity().getApplicationContext(), "Attach Volume successfully", Toast.LENGTH_SHORT).show();
                                                                TimerTask task = new TimerTask() {
                                                                    @Override
                                                                    public void run() {
                                                                        mOverlayDialog.dismiss();
                                                                        FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                        VolumeFragment vFragment = new VolumeFragment();
                                                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
                                                                    }
                                                                };
                                                                /**
                                                                 * Delay 7 secs after the button onclick method is called.
                                                                 * Wait for server status update. The server status is not modified in real-time.
                                                                 */
                                                                timer.schedule(task, 4000);


                                                            } else{
                                                                mOverlayDialog.dismiss();
                                                                Toast.makeText(getActivity().getApplicationContext(), "Fail to attach volume", Toast.LENGTH_SHORT).show();
                                                                FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                VolumeFragment vFragment = new VolumeFragment();
                                                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();

                                                            }
                                                        }
                                                    },selectInstanceID, "/dev/vdc",volumeID);

                                                }


                                            }
                                        })
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                        AlertDialog alertDialog = builderSecurityGroup.create();
                                        alertDialog.show();
                                        dialogLV.dismiss();


                                    }
                                });


                                detachV.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage("Are you sure to detach this volume?")
                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        mOverlayDialog.show();
                                                        HttpRequestController.getInstance(getActivity().getApplicationContext()).detachVolume(new HttpRequestController.VolleyCallback() {
                                                            @Override
                                                            public void onSuccess(String result) {
                                                                if(result.equals("success")) {
                                                                    //mOverlayDialog.dismiss();
                                                                    Toast.makeText(getActivity().getApplicationContext(), "Detach Volume successfully", Toast.LENGTH_SHORT).show();
                                                                    TimerTask task = new TimerTask() {
                                                                        @Override
                                                                        public void run() {
                                                                            mOverlayDialog.dismiss();
                                                                            FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                            VolumeFragment vFragment = new VolumeFragment();
                                                                            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
                                                                        }
                                                                    };
                                                                    /**
                                                                     * Delay 7 secs after the button onclick method is called.
                                                                     * Wait for server status update. The server status is not modified in real-time.
                                                                     */
                                                                    timer.schedule(task, 4000);


                                                                } else{
                                                                    mOverlayDialog.dismiss();
                                                                    Toast.makeText(getActivity().getApplicationContext(), "Fail to detach volume", Toast.LENGTH_SHORT).show();
                                                                    //Toast.makeText(getActivity().getApplicationContext(), "Detach Volume successfully", Toast.LENGTH_SHORT).show();
                                                                    FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                    VolumeFragment vFragment = new VolumeFragment();
                                                                    manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();

                                                                }
                                                            }
                                                        },volumeID, attachID);
                                                    }
                                                })
                                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).show();




                                        dialogLV.dismiss();


                                    }
                                });




                                snapshotV.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        LayoutInflater factory = LayoutInflater.from(getActivity());
                                        final View textEntryView = factory.inflate(R.layout.create_sg, null);
                                        final EditText nameV = (EditText) textEntryView.findViewById(R.id.createSGName);
                                        final EditText decriptionV = (EditText)textEntryView.findViewById(R.id.createSGDescription);
                                        AlertDialog.Builder builderSecurityGroup = new AlertDialog.Builder(getActivity());
                                        builderSecurityGroup.setTitle("Create Volume Snapshot");
                                        builderSecurityGroup.setIcon(R.drawable.nectar_app_icon);
                                        //builderSecurityGroup.setIcon(android.R.drawable.ic_dialog_info);
                                        builderSecurityGroup.setView(textEntryView);

                                        builderSecurityGroup.setPositiveButton("Create Snapshot", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                String nameNew= nameV.getText().toString();
                                                String descriptionNew= decriptionV.getText().toString();
                                                dialog.dismiss();
                                                mOverlayDialog.show();
                                                HttpRequestController.getInstance(getActivity().getApplicationContext()).createVolumeSnapshot(new HttpRequestController.VolleyCallback() {
                                                    @Override
                                                    public void onSuccess(String result) {
                                                        if(result.equals("success")) {
                                                            //mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Create Volume Snapshot successfully", Toast.LENGTH_SHORT).show();
                                                            TimerTask task = new TimerTask() {
                                                                @Override
                                                                public void run() {
                                                                    mOverlayDialog.dismiss();
                                                                    FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                    VolumeFragment vFragment = new VolumeFragment();
                                                                    manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
                                                                }
                                                            };
                                                            /**
                                                             * Delay 7 secs after the button onclick method is called.
                                                             * Wait for server status update. The server status is not modified in real-time.
                                                             */
                                                            timer.schedule(task, 4000);

                                                        } else{
                                                            mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Fail to create volume snapshot", Toast.LENGTH_SHORT).show();
                                                            FragmentManager manager = getActivity().getSupportFragmentManager();
                                                            VolumeFragment vFragment = new VolumeFragment();
                                                            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();

                                                        }
                                                    }
                                                },nameNew, descriptionNew,volumeID);



                                            }
                                        })
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                        AlertDialog alertDialog = builderSecurityGroup.create();
                                        alertDialog.show();

                                        dialogLV.dismiss();

                                    }
                                });




                                deleteV.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage("Are you sure to delete this volume?")
                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        mOverlayDialog.show();
                                                        HttpRequestController.getInstance(getActivity().getApplicationContext()).deleteVolume(new HttpRequestController.VolleyCallback() {
                                                            @Override
                                                            public void onSuccess(String result) {
                                                                if(result.equals("success")) {
                                                                    Toast.makeText(getActivity().getApplicationContext(), "Delete the volume Succeed", Toast.LENGTH_SHORT).show();
                                                                    TimerTask task = new TimerTask() {
                                                                        @Override
                                                                        public void run() {
                                                                            mOverlayDialog.dismiss();
                                                                            FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                            VolumeFragment vFragment = new VolumeFragment();
                                                                            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
                                                                        }
                                                                    };
                                                                    /**
                                                                     * Delay 7 secs after the button onclick method is called.
                                                                     * Wait for server status update. The server status is not modified in real-time.
                                                                     */
                                                                    timer.schedule(task, 4000);
                                                                } else{
                                                                    mOverlayDialog.dismiss();
                                                                    Toast.makeText(getActivity().getApplicationContext(), "Fail to delete this security group", Toast.LENGTH_SHORT).show();
                                                                    FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                    VolumeFragment vFragment = new VolumeFragment();
                                                                    manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
                                                                }
                                                            }
                                                        }, volumeID);
                                                    }
                                                })
                                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).show();
                                        dialogLV.dismiss();

                                    }
                                });




                                //Set the view to Dialog
                                dialogLV.setContentView(inflate);
                                Window dialogWindow = dialogLV.getWindow();
                                dialogWindow.setGravity(Gravity.BOTTOM);
                                //Get the attributes of teh window
                                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                                lp.y = 20;//Set the distance of the dialog to the bottom
                                //Set the attribute to  the dialog
                                dialogWindow.setAttributes(lp);
                                dialogLV.show();//Show the dialog



                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    volumeLV.setOnItemClickListener(onListClick);
                    mOverlayDialog.dismiss();
                    //refresh
                    //refresh();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, getActivity());


        /*
         * Set Security Group button
         */
        volumeS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Snackbar.make(view, "Refreshing...", Snackbar.LENGTH_SHORT).show();
                //mOverlayDialog.show();
                //mOverlayDialog.dismiss();
                //reload this page
                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                VolumeSnapshotFragment vsFragment = new VolumeSnapshotFragment();

                ft.replace(R.id.relativelayout_for_fragment, vsFragment, vsFragment.getTag()).commit();
                //System.out.println("hihihiheeeee");

            }
        });

        /*
         * Set Key Pair button
         */
        volume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "Refreshing...", Snackbar.LENGTH_SHORT).show();
                mOverlayDialog.show();
                mOverlayDialog.dismiss();
                refresh();

            }
        });

        /**
         * Set refresh/back button.
         */
        FloatingActionButton fabRight = (FloatingActionButton) getActivity().findViewById(R.id.fabRight);
        fabRight.setVisibility(View.VISIBLE);
        fabRight.setEnabled(true);
        fabRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "Refreshing...", Snackbar.LENGTH_SHORT).show();
                mOverlayDialog.show();
                mOverlayDialog.dismiss();
                //reload this page
                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                VolumeFragment vFragment = new VolumeFragment();

                ft.replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
                //System.out.println("hihihiheeeee");

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

    public void setListViewHeightBasedOnChildren(ListView listView) {
        // Get the adapter for the list
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        // listAdapter.getCount() can get the number of the items
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {

            View listItem = listAdapter.getView(i, null, listView);
            // Calculate the height and width of a item
            listItem.measure(0, 0);
            // calculate the total height
            totalHeight += listItem.getMeasuredHeight()*1.05;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()get the height of the divider
        // params.height can finally get the total height to display
        listView.setLayoutParams(params);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflate) {
        // TODO Auto-generated method stub
        //super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.create_volume_sg).setVisible(true);
        //menu.findItem(R.id.import_KP).setVisible(true);

    }

}
