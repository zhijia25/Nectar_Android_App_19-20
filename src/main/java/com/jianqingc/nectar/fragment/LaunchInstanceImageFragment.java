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
 * Created by HuangMengxue on 17/4/28.
 */

/**
 * A simple {@link Fragment} subclass.
 */
public class LaunchInstanceImageFragment extends Fragment{

    View myView;
    public static LaunchInstanceImageFragment instanceLI=null;
    JSONArray falvorlist=null;
    public String chooseFlavor;
    public String chooseImage=" ";
    List<String> imageName;
    List<String> imageID;
    JSONArray imagelist=null;
    public String chooseKP;
    JSONArray kpList=null;
    public String chooseAZ;
    JSONArray azList=null;
    public String setName=" ";
    public List<String> chooseSG;
    JSONArray sgList=null;
    List<String> sgID;
    private RadioAdapter adapter;


    public LaunchInstanceImageFragment() {
        // Required empty public constructor
        instanceLI=this;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_launch_instance_image, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("New Instance");
        /**
         * set spinner which is actually a dialog
         */
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);

        final EditText name= (EditText) myView.findViewById(R.id.newInsName);
        final Spinner flavor= (Spinner) myView.findViewById(R.id.flavor);
        final Spinner image= (Spinner) myView.findViewById(R.id.imageNameNI);
        final Spinner keyPair= (Spinner) myView.findViewById(R.id.keyPairNI);
        final Spinner availabilityZone= (Spinner) myView.findViewById(R.id.availabilityZoneNI);
        final ListView securityGroup = (ListView) myView.findViewById(R.id.securityGroupNI);
        final Button create = (Button)myView.findViewById(R.id.createNI);
        final java.util.Timer timer = new java.util.Timer(true);

        //List flavors
        HttpRequestController.getInstance(getContext()).listFlavor(new HttpRequestController.VolleyCallback() {
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
                    id_list= new ArrayList<String>();

                    falvorlist = new JSONArray(result);
                    System.out.println(falvorlist);
                    for (int i = 0; i < falvorlist.length(); i++) {
                        data_list.add(falvorlist.getJSONObject(i).getString("flavorName"));
                        id_list.add(falvorlist.getJSONObject(i).getString("flavorId"));
                    }

                    //New an Adapter
                    arr_adapter= new ArrayAdapter<String>(LaunchInstanceImageFragment.this.getActivity(), android.R.layout.simple_spinner_item, data_list);
                    //Set the format of the adapter
                    arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //set the adapter
                    flavor.setAdapter(arr_adapter);

                    flavor.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                            String chooseName = data_list.get(arg2);
                            //Set to show the chosen
                            arg0.setVisibility(View.VISIBLE);
                            String flavorID;
                            for(int i = 0; i < data_list.size(); i++){
                                if(data_list.get(i)==chooseName){
                                    instanceLI.chooseFlavor=id_list.get(i);
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

        //List Images
        HttpRequestController.getInstance(getContext()).listImageProject(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */
                    imageName = new ArrayList<String>();
                    imageName.add("Select Image please");
                    ArrayAdapter<String> arr_adapter;
                    imageID= new ArrayList<String>();

                    imagelist = new JSONArray(result);
                    //System.out.println(falvorlist);
                    for (int i = 0; i < imagelist.length(); i++) {
                        imageName.add(imagelist.getJSONObject(i).getString("imageName"));
                        imageID.add(imagelist.getJSONObject(i).getString("imageId"));
                    }

                    HttpRequestController.getInstance(getContext()).listImageOfficial(new HttpRequestController.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            try {
                                /**
                                 * Display instance Info with the Listview and Fundapter
                                 * You can also use simple ArrayAdapter to replace Fundatper.
                                 */

                                ArrayAdapter<String> arr_adapter;


                                imagelist = new JSONArray(result);
                                //System.out.println(falvorlist);
                                for (int i = 0; i < imagelist.length(); i++) {
                                    imageName.add(imagelist.getJSONObject(i).getString("imageName"));
                                    imageID.add(imagelist.getJSONObject(i).getString("imageId"));
                                }

                                //New an Adapter
                                arr_adapter= new ArrayAdapter<String>(LaunchInstanceImageFragment.this.getActivity(), android.R.layout.simple_spinner_item, imageName);
                                //Set the format of the adapter
                                arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                //set the adapter
                                image.setAdapter(arr_adapter);

                                image.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                        String chooseName = imageName.get(arg2);
                                        //Set to show the chosen
                                        arg0.setVisibility(View.VISIBLE);
                                        for(int i = 1; i < imageName.size(); i++){
                                            if(imageName.get(i)==chooseName){
                                                int location=i-1;
                                                instanceLI.chooseImage=imageID.get(location);
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



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, getActivity());


        //List available Key pairs
        HttpRequestController.getInstance(getContext()).listKeyPair(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */
                    final List<String> kp_list;
                    kp_list = new ArrayList<String>();
                    kp_list.add("Select Key pair please");
                    ArrayAdapter<String> arr_adapter;

                    kpList = new JSONArray(result);
                    System.out.println(kpList);
                    for (int i = 0; i < kpList.length(); i++) {
                        kp_list.add(kpList.getJSONObject(i).getString("kpName"));
                    }

                    //New an Adapter
                    arr_adapter= new ArrayAdapter<String>(LaunchInstanceImageFragment.this.getActivity(), android.R.layout.simple_spinner_item, kp_list);
                    //Set the format of the adapter
                    arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //set the adapter
                    keyPair.setAdapter(arr_adapter);

                    keyPair.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                            String chooseName = kp_list.get(arg2);
                            //Set to show the chosen
                            arg0.setVisibility(View.VISIBLE);
                            //String flavorID;
                            instanceLI.chooseKP=chooseName;


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

        //List Availability zones
        HttpRequestController.getInstance(getContext()).listAvailabilityZone(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */
                    final List<String> az_list;
                    az_list = new ArrayList<String>();
                    az_list.add("Select Availability Zone please");
                    ArrayAdapter<String> arr_adapter;

                    azList = new JSONArray(result);
                    //System.out.println(kpList);
                    for (int i = 0; i < azList.length(); i++) {
                        if(azList.getJSONObject(i).getBoolean("azState")==true){
                            az_list.add(azList.getJSONObject(i).getString("azName"));
                        }
                    }

                    //New an Adapter
                    arr_adapter= new ArrayAdapter<String>(LaunchInstanceImageFragment.this.getActivity(), android.R.layout.simple_spinner_item, az_list);
                    //Set the format of the adapter
                    arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //set the adapter
                    availabilityZone.setAdapter(arr_adapter);

                    availabilityZone.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                            String chooseName = az_list.get(arg2);
                            System.out.println("check2:"+chooseName);
                            //Set to show the chosen
                            arg0.setVisibility(View.VISIBLE);
                            //String flavorID;
                            instanceLI.chooseAZ=chooseName;
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


        //Start to input the data of security groups
        HttpRequestController.getInstance(getContext()).listSecurityGroup(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */
                    final List<String> sg_list;
                    sg_list = new ArrayList<String>();
                    sgID=new ArrayList<String>();
                    RadioAdapter arr_adapter;

                    sgList = new JSONArray(result);
                    System.out.println(sgList);
                    for (int i = 0; i < sgList.length(); i++) {
                        sg_list.add(sgList.getJSONObject(i).getString("sgName"));
                        sgID.add(sgList.getJSONObject(i).getString("sgId"));
                    }

                    System.out.println(sg_list);
                    //New an Adapter
                    arr_adapter= new RadioAdapter(LaunchInstanceImageFragment.this.getActivity(), sg_list);
                    //set the adapter
                    securityGroup.setAdapter(arr_adapter);
                    setListViewHeightBasedOnChildren(securityGroup);



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, getActivity());



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
                                FragmentManager manager = getFragmentManager();
                                InstanceFragment instanceFragment = new InstanceFragment();
                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, instanceFragment, instanceFragment.getTag()).commit();

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



        /**
         * set Create button onclick
         */
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get selected security groups
                chooseSG=new ArrayList<String>();
                long[] authorsId = getListSelectededItemIds(securityGroup);
                String id;
                for (int i = 0; i < authorsId.length; i++) {
                    id = sgID.get((int) authorsId[i]);
                    chooseSG.add(id);
                }
                System.out.println(chooseFlavor);
                //Get the name of new instance
                setName=name.getText().toString();
                System.out.println(chooseKP);
                System.out.println(chooseAZ);
                boolean valid=checkInputValid();
                System.out.println(valid);
                if(!valid){
                    Toast.makeText(getActivity().getApplicationContext(),"Please fill in necessary information" , Toast.LENGTH_SHORT).show();
                }else{
                    mOverlayDialog.show();
                    HttpRequestController.getInstance(getActivity().getApplicationContext()).launchServer(new HttpRequestController.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            if (result.equals("success")) {
                                Toast.makeText(getActivity().getApplicationContext(),"Create instance successfully" , Toast.LENGTH_SHORT).show();
                                TimerTask task = new TimerTask() {
                                    @Override
                                    public void run() {
                                        mOverlayDialog.dismiss();


                                        FragmentManager manager = getFragmentManager();
                                        InstanceFragment instanceFragment = new InstanceFragment();
                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, instanceFragment, instanceFragment.getTag()).commit();


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

                    }, setName,chooseFlavor,chooseImage,chooseKP,chooseAZ,chooseSG);

                }

                //Toast.makeText(getActivity().getApplicationContext(),"The test result is "+chooseAZ , Toast.LENGTH_SHORT).show();


            }
        });



        return myView;
    }

    private boolean checkInputValid(){
        boolean valid=true;
        if(setName==" "){
            valid=false;
        }
        if(chooseImage=="Select Image please"||chooseImage==" "){
            valid=false;
        }

        return valid;
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


    // To avoid using getCheckItemIds()
    public long[] getListSelectededItemIds(ListView listView) {

        long[] ids = new long[listView.getCount()];//getCount() can get the number of items in the ListView
        //the number of chosen items
        int checkedTotal = 0;
        for (int i = 0; i < listView.getCount(); i++) {
            //If the item is chosen
            if (listView.isItemChecked(i)) {
                ids[checkedTotal++] = i;
            }
        }

        if (checkedTotal < listView.getCount()) {

            final long[] selectedIds = new long[checkedTotal];
            //copy ids
            System.arraycopy(ids, 0, selectedIds, 0, checkedTotal);
            return selectedIds;
        } else {
            //if the user select all the items
            return ids;
        }
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
            totalHeight += listItem.getMeasuredHeight()*1.2;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()get the height of the divider
        // params.height can finally get the total height to display
        listView.setLayoutParams(params);
    }



}


