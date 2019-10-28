package com.jianqingc.nectar.fragment.Network_Fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.jianqingc.nectar.R;
import com.jianqingc.nectar.httpRequest.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class NetworkFragment extends Fragment {
    View myView;
    ArrayList<String[]> networkListArray;
    JSONArray listNetworkResultArrayP;
    Bundle bundle;
    String networkID;
    String networkName;
    String networkSubnets;
    String networkShared;
    String networkStatus;
    String name_cidr_list = "";

    String []subnetName = null;
    String []subnetCIDR = null;

    Map subnetInfo = new HashMap();


    public NetworkFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_network, container, false);
        setHasOptionsMenu(true);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Networks");

        final java.util.Timer timer = new java.util.Timer(true);
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        HttpRequest.getInstance(getContext()).listNetwork(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    listNetworkResultArrayP = new JSONArray(result);
                    networkListArray = new ArrayList<String[]>();

                    for (int i=0; i<listNetworkResultArrayP.length();i++)
                    {
                        String adminState;
                        String name = listNetworkResultArrayP.getJSONObject(i).getString("networkName");
                        String tenant_id = listNetworkResultArrayP.getJSONObject(i).getString("tenant_id");
                        String status = listNetworkResultArrayP.getJSONObject(i).getString("status");
                        String shared = listNetworkResultArrayP.getJSONObject(i).getString("shared");
                        String admin_state_up = listNetworkResultArrayP.getJSONObject(i).getString("admin_state_up");
                        String projectID = listNetworkResultArrayP.getJSONObject(i).getString("tenant_id");
                        if (admin_state_up.equals("true")){
                            adminState = "UP";
                        } else {
                            adminState = "DOWN";
                        }
                        String networkid = listNetworkResultArrayP.getJSONObject(i).getString("networkid");
                        networkID = networkid;
                        String router_external = listNetworkResultArrayP.getJSONObject(i).getString("router_external");
                        String mtu = listNetworkResultArrayP.getJSONObject(i).getString("mtu");
                        String subnetList = listNetworkResultArrayP.getJSONObject(i).getString("subnet_array");
                        //String []subnet_list = subnetList.split("");


                        String tempSubnetList = subnetList.replace(",","\n");
                        tempSubnetList = tempSubnetList.replace("[","");
                        tempSubnetList = tempSubnetList.replace("]","");
                        tempSubnetList = tempSubnetList.replace("\"","");



                        //add a iterator to send request to get the detail of subnets


                        //System.out.println("atest "+tempSubnetList);
                        System.out.println(name+" "+tempSubnetList);
                        String []subnetIDList = tempSubnetList.split("\n");
                        String numOfSubnet;
                        if(tempSubnetList.equals("")){
                            numOfSubnet = "0 subnets";
                        } else {
                            numOfSubnet= subnetIDList.length + " subnets";
                        }

                        String []networkList = {
                                name,
                                tenant_id,
                                status,
                                shared,
                                adminState,
                                networkid,
                                router_external,
                                mtu,

                                numOfSubnet,
                                projectID,
                                tempSubnetList


                        };
                        networkListArray.add(networkList);
                    }
                    BindDictionary<String[]> dictionary = new BindDictionary<String[]>();
                    dictionary.addStringField(R.id.networkNameLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[0];
                        }
                    });

                    dictionary.addStringField(R.id.networkSubnetsLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[8];
                        }
                    });

                    dictionary.addStringField(R.id.networkSharedLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[3];
                        }
                    });


                    dictionary.addStringField(R.id.networkStatusLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[2];
                        }
                    });

                    dictionary.addStringField(R.id.networkAdminStateLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[4];
                        }
                    });

                    FunDapter adapter = new FunDapter(NetworkFragment.this.getActivity(), networkListArray, R.layout.network_list_pattern, dictionary);
                    ListView networkLV = (ListView) myView.findViewById(R.id.listViewNetwork);
                    adapter.notifyDataSetChanged();
                    networkLV.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(networkLV);

                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                            bundle = new Bundle();
                                String networkName = networkListArray.get(position)[0];
                                final String networkID = networkListArray.get(position)[5];
                                String networkStatus = networkListArray.get(position)[2];
                                String networkShared = networkListArray.get(position)[3];
                                String networkAdminState = networkListArray.get(position)[4];
                                String externalRouter = networkListArray.get(position)[6];
                                String mtu = networkListArray.get(position)[7];
                                String projectID = networkListArray.get(position)[9];
                                String sublist = networkListArray.get(position)[10];

                                bundle.putString("networkName",networkName);
                                bundle.putString("networkID",networkID);
                                bundle.putString("networkStatus",networkStatus);
                                bundle.putString("networkShared",networkShared);
                                bundle.putString("networkAdminState",networkAdminState);
                                bundle.putString("externalRouter",externalRouter);
                                bundle.putString("mtu",mtu);
                                bundle.putString("projectID",projectID);

                                final Dialog dialogNW = new Dialog(getActivity(),R.style.ActionSheetDialogStyle);
                                View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.launch_network_dialog,null);

                                TextView viewDetailNetwork = (TextView) inflate.findViewById(R.id.viewNetworksDetail);
                                TextView deleteNetwork = (TextView) inflate.findViewById(R.id.deleteNetworks);
                                TextView viewSubnets = (TextView) inflate.findViewById(R.id.viewSubnets);
                                TextView viewPorts = (TextView) inflate.findViewById(R.id.viewPorts);

                                viewDetailNetwork.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                        NetworkDetailFragment networkDetailFragment = new NetworkDetailFragment();
                                        networkDetailFragment.setArguments(bundle);
                                        ft.replace(R.id.relativelayout_for_fragment, networkDetailFragment, networkDetailFragment.getTag());
                                        ft.commit();
                                        dialogNW.dismiss();
                                    }
                                });

                                viewSubnets.setOnClickListener(new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view) {
                                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                        SubnetFragment subnetFragment = new SubnetFragment();
                                        subnetFragment.setArguments(bundle);
                                        ft.replace(R.id.relativelayout_for_fragment, subnetFragment, subnetFragment.getTag());
                                        ft.commit();
                                        dialogNW.dismiss();

                                    }
                                });

                                viewPorts.setOnClickListener(new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view) {
                                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                        PortFragment portFragment = new PortFragment();
                                        portFragment.setArguments(bundle);
                                        ft.replace(R.id.relativelayout_for_fragment, portFragment, portFragment.getTag());
                                        ft.commit();
                                        dialogNW.dismiss();

                                    }
                                });


                                deleteNetwork.setOnClickListener(new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view) {
                                        SharedPreferences sharedPreferences = getContext().getApplicationContext().getSharedPreferences("nectar_android",0);
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage("Delete this Network?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int i) {
                                                mOverlayDialog.show();
                                                HttpRequest.getInstance(getActivity().getApplicationContext()).deleteNetwork(new HttpRequest.VolleyCallback() {
                                                    @Override
                                                    public void onSuccess(String result) {
                                                        if (result.equals("success")){
                                                            Toast.makeText(getActivity().getApplicationContext(),"Delete the Network Succeed", Toast.LENGTH_SHORT).show();
                                                            TimerTask task = new TimerTask(){
                                                                @Override
                                                                public void run() {
                                                                    mOverlayDialog.dismiss();
                                                                    FragmentManager manager = getFragmentManager();
                                                                    NetworkFragment networkFragment = new NetworkFragment();
                                                                    manager.beginTransaction().replace(R.id.relativelayout_for_fragment, networkFragment, networkFragment.getTag()).commit();

                                                                }
                                                            };
                                                            timer.schedule(task,4000);

                                                        } else {
                                                            mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(),"Fail to delete this Network", Toast.LENGTH_SHORT).show();

                                                        }

                                                    }
                                                },networkID);
                                            }
                                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int i) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                                        dialogNW.dismiss();
                                    }
                                });
                                dialogNW.setContentView(inflate);
                                Window dialogWindow = dialogNW.getWindow();
                                dialogWindow.setGravity(Gravity.BOTTOM);

                                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                                lp.y =  20;
                                dialogWindow.setAttributes(lp);
                                dialogNW.show();

                        }
                    };

                    networkLV.setOnItemClickListener(onListClick);
                    mOverlayDialog.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },getActivity());

        //need to get the info of subnets;




        return  myView;
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
            totalHeight += listItem.getMeasuredHeight()*1.1;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()get the height of the divider
        // params.height can finally get the total height to display
        listView.setLayoutParams(params);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.create_network).setVisible(true);
//        menu.findItem(R.id.create_stack).setVisible(true);
    }
}