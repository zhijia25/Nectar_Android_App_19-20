package com.jianqingc.nectar.fragment.Container_Infra_Fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import android.widget.EditText;

import com.jianqingc.nectar.R;
import com.jianqingc.nectar.bean.CreateClusterBean;
import com.jianqingc.nectar.httpRequest.HttpRequest;
import com.jianqingc.nectar.util.RadioAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.TimerTask;

public class CreateClusterFragment extends Fragment {

    View myView;

    public static CreateClusterFragment instanceLI=null;

    public int setDockerSize;
    public int setMcount;
    public int setNcount;
    public int setTimeout;

    public CreateClusterBean clusterBean;
    public String setName;
    public String setDiscoveryURL = null;
    JSONArray setLabels = null;
    JSONArray templateList = null;
    public String setClusterTemplate;
    JSONArray falvorlist = null;
    public String setMasterFlavor;
    public String setNodeFlavor;
    JSONArray kpList = null;
    public String chooseKP;

    private RadioAdapter adapter;

    public CreateClusterFragment() {
        // Required empty public constructor
        instanceLI=this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_create_cluster, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.create_cluster));

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);

        final EditText name= (EditText) myView.findViewById(R.id.newClusterName);
        final EditText size= (EditText) myView.findViewById(R.id.DockerVolumeSize1);
        final EditText masterCount= (EditText) myView.findViewById(R.id.masterCount1);
        final EditText nodeCount= (EditText) myView.findViewById(R.id.nodeCount1);
        final EditText dicoverayURL= (EditText) myView.findViewById(R.id.DiscoveryURLID1);
        final EditText timeOut= (EditText) myView.findViewById(R.id.timeOut1);

        final Spinner keyPair= (Spinner) myView.findViewById(R.id.keyPair1);
        final Spinner template= (Spinner) myView.findViewById(R.id.chooseClusterTemplate1);
        final Spinner masterFlavor= (Spinner) myView.findViewById(R.id.masterFlavorID1);
        final Spinner nodeFlavor= (Spinner) myView.findViewById(R.id.nodeFlavorID1);
        final java.util.Timer timer = new java.util.Timer(true);
        final Button create = (Button)myView.findViewById(R.id.createNI);

        //List flavors
        HttpRequest.getInstance(getContext()).listFlavor(new HttpRequest.VolleyCallback() {
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
                    arr_adapter= new ArrayAdapter<String>(CreateClusterFragment.this.getActivity(), android.R.layout.simple_spinner_item, data_list);
                    //Set the format of the adapter
                    arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //set the adapter
                    masterFlavor.setAdapter(arr_adapter);
                    masterFlavor.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                            String chooseName = data_list.get(arg2);
                            System.out.println("NodeFlavor: " + chooseName);
                            //Set to show the chosen
                            arg0.setVisibility(View.VISIBLE);
                            for(int i = 0; i < data_list.size(); i++){
                                if(data_list.get(i).equals(chooseName)){
                                    System.out.println("master"+ id_list.get(i));
//                                    clusterBean.setMaster_flavor_id(id_list.get(i));
                                    instanceLI.setMasterFlavor=id_list.get(i);
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

        HttpRequest.getInstance(getContext()).listFlavor(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */
                    final List<String> data_list;
                    data_list = new ArrayList<String>();
                    ArrayAdapter<String> arr_adapter1;
                    final List<String> id_list;
                    id_list= new ArrayList<String>();

                    falvorlist = new JSONArray(result);
                    System.out.println(falvorlist);
                    for (int i = 0; i < falvorlist.length(); i++) {
                        data_list.add(falvorlist.getJSONObject(i).getString("flavorName"));
                        id_list.add(falvorlist.getJSONObject(i).getString("flavorId"));
                    }

                    //New an Adapter
                    arr_adapter1= new ArrayAdapter<String>(CreateClusterFragment.this.getActivity(), android.R.layout.simple_spinner_item, data_list);
                    //Set the format of the adapter
                    arr_adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    nodeFlavor.setAdapter(arr_adapter1);
                    nodeFlavor.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                            String chooseName = data_list.get(arg2);
                            //Set to show the chosen
                            arg0.setVisibility(View.VISIBLE);
                            for(int i = 0; i < data_list.size(); i++){
                                if(data_list.get(i).equals(chooseName)){
                                    System.out.println("node"+ id_list.get(i));
//                                    clusterBean.setFlavor_id(id_list.get(i));
                                    instanceLI.setNodeFlavor=id_list.get(i);
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

        //List available Templates
        HttpRequest.getInstance(getContext()).listClusterTemplate(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */
                    final List<String> templates_list;
                    templates_list = new ArrayList<String>();
                    templates_list.add("Select template please");
                    ArrayAdapter<String> arr_adapter;
                    final List<String> Templateid_list;
                    Templateid_list= new ArrayList<String>();

                    templateList = new JSONArray(result);
                    for (int i = 0; i < templateList.length(); i++) {
                        templates_list.add(templateList.getJSONObject(i).getString("name"));
                        Templateid_list.add(templateList.getJSONObject(i).getString("uuid"));
                    }

                    //New an Adapter
                    arr_adapter= new ArrayAdapter<String>(CreateClusterFragment.this.getActivity(), android.R.layout.simple_spinner_item, templates_list);
                    //Set the format of the adapter
                    arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //set the adapter
                    template.setAdapter(arr_adapter);

                    template.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                            String chooseName = templates_list.get(arg2);
                            System.out.println("Template: " + chooseName);
                            //Set to show the chosen
                            arg0.setVisibility(View.VISIBLE);
                            //String flavorID;
                            for(int i = 0; i < templates_list.size(); i++){
                                if(templates_list.get(i).equals(chooseName)){
                                    System.out.println("Template_id: " + Templateid_list.get(i));
                                    instanceLI.setClusterTemplate=Templateid_list.get(i);
                                    System.out.println("get_Template_id: " + setMasterFlavor);
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

        //List available Key pairs
        HttpRequest.getInstance(getContext()).listKeyPair(new HttpRequest.VolleyCallback() {
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
                    arr_adapter= new ArrayAdapter<String>(CreateClusterFragment.this.getActivity(), android.R.layout.simple_spinner_item, kp_list);
                    //Set the format of the adapter
                    arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //set the adapter
                    keyPair.setAdapter(arr_adapter);

                    keyPair.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                            String chooseName = kp_list.get(arg2);
                            System.out.println(chooseName);
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

        /**
         * set Create button onclick
         */
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get the name of new instance
                setName = name.getText().toString();
                setDiscoveryURL = dicoverayURL.getText().toString();
                setTimeout = Integer.parseInt(timeOut.getText().toString().trim());
                //Get the size of Docker volume
                setDockerSize = Integer.parseInt(size.getText().toString().trim());
                //Get the count of node
                setMcount = Integer.parseInt(masterCount.getText().toString().trim());
                setNcount = Integer.parseInt(nodeCount.getText().toString().trim());

                boolean valid = checkInputValid();
                System.out.println(valid);
                if(!valid){
                    Toast.makeText(getActivity().getApplicationContext(),"Please fill in necessary information" , Toast.LENGTH_SHORT).show();
                }else{
                    mOverlayDialog.show();
                    HttpRequest.getInstance(getActivity().getApplicationContext()).createCluster(new HttpRequest.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            if (result.equals("success")) {
                                Toast.makeText(getActivity().getApplicationContext(),"Create instance successfully" , Toast.LENGTH_SHORT).show();
                                TimerTask task = new TimerTask() {
                                    @Override
                                    public void run() {
                                        mOverlayDialog.dismiss();
                                        FragmentManager manager = getFragmentManager();
                                        ClustersFragment clustersFragment = new ClustersFragment();
                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, clustersFragment, clustersFragment.getTag()).commit();
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

                    }, setName,setDiscoveryURL,chooseKP,setClusterTemplate,setNodeFlavor,setMasterFlavor,setDockerSize,setMcount,setNcount,setTimeout);

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
        if(setClusterTemplate=="Select template please"||setClusterTemplate == " "){
            valid=false;
        }
        if(chooseKP.equals("Select Key pair please")||chooseKP.equals(" ")){
            valid=false;
        }

        return valid;
    }

}
