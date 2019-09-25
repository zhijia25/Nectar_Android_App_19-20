package com.jianqingc.nectar.fragment;

import java.text.DecimalFormat;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class SubnetFragment extends Fragment {
    Bundle bundle;
    Bundle bundle2;
    Bundle bundle3;
    View myView;
    ArrayList<String[]> subnetListArray;
    JSONArray listSubnetResultArrayP;
    String networkID;
    String subnetID;
    String subnetIpVersion;
    String subnetAddress;

    public SubnetFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_subnet, container, false);
        setHasOptionsMenu(true);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Subnets");
        bundle = getArguments();
        networkID = bundle.getString("networkID");

        bundle2 = new Bundle();
        bundle2.putString("networkID",networkID);

        final java.util.Timer timer = new java.util.Timer(true);
        final Dialog mOverlayDialog = new Dialog(getActivity(),android.R.style.Theme_Panel);
        final Button create = (Button)myView.findViewById(R.id.createSubnetButton);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabLeft.setVisibility(View.VISIBLE);
        fabLeft.setEnabled(true);
        fabLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();

                NetworkFragment networkFragment = new NetworkFragment();

                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, networkFragment, networkFragment.getTag()).commit();
            }
        });


        HttpRequestController.getInstance(getContext()).listSubnet(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    listSubnetResultArrayP = new JSONArray(result);
                    subnetListArray = new ArrayList<String[]>();
                    for(int i = 0; i< listSubnetResultArrayP.length();i++)
                    {
                        String name = listSubnetResultArrayP.getJSONObject(i).getString("subnetName");
                        String address = listSubnetResultArrayP.getJSONObject(i).getString("cidr");
                        String ipVersion = listSubnetResultArrayP.getJSONObject(i).getString("ip_version");
                        String subnetID = listSubnetResultArrayP.getJSONObject(i).getString("subnetID");
                        String allocation_pools = listSubnetResultArrayP.getJSONObject(i).getString("allocation_pools");

                        String [] subnetList={
                                name,
                                address,
                                ipVersion,
                                subnetID,
                                allocation_pools,
                                networkID


                        };
                        subnetListArray.add(subnetList);

                    }

                    BindDictionary<String []> dictionary = new BindDictionary<String[]>();
                    dictionary.addStringField(R.id.SubnetNameLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[0];
                        }
                    });

                    dictionary.addStringField(R.id.SubnetNetworkAddressLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[1];
                        }
                    });

                    dictionary.addStringField(R.id.SubnetIPVersionLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[2];
                        }
                    });

                    FunDapter adapter = new FunDapter(SubnetFragment.this.getActivity(), subnetListArray, R.layout.subnet_list_pattern,dictionary);
                    ListView subnetLV = (ListView) myView.findViewById(R.id.listViewSubnet);
                    adapter.notifyDataSetChanged();
                    subnetLV.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(subnetLV);

                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                            String subnetName = subnetListArray.get(position)[0];
                            final String subnetID = subnetListArray.get(position)[3];
                            String networkID = subnetListArray.get(position)[5];
                            String ipVersion = subnetListArray.get(position)[2];
                            String allocation_pools = subnetListArray.get(position)[4];
                            String address = subnetListArray.get(position)[1];
                            bundle3 = new Bundle();
                            bundle3.putString("subnetName",subnetName);
                            bundle3.putString("subnetID",subnetID);
                            bundle3.putString("networkID",networkID);
                            bundle3.putString("ipVersion",ipVersion);
                            bundle3.putString("allocation_pools",allocation_pools);
                            bundle3.putString("address",address);

                            final Dialog dialogSN = new Dialog(getActivity(),R.style.ActionSheetDialogStyle);
                            View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.launch_subnet_dialog,null);

                            TextView viewDetailSubnet = (TextView) inflate.findViewById(R.id.viewSubnetDetail);
                            TextView deleteSubnet = (TextView) inflate.findViewById(R.id.deleteSubnet);

                            viewDetailSubnet.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                    SubnetDetailFragment subnetDetailFragment = new SubnetDetailFragment();
                                    subnetDetailFragment.setArguments(bundle3);
                                    ft.replace(R.id.relativelayout_for_fragment,subnetDetailFragment,subnetDetailFragment.getTag()).commit();
                                    dialogSN.dismiss();
                                }
                            });

                            deleteSubnet.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    SharedPreferences sharedPreferences = getContext().getApplicationContext().getSharedPreferences("nectar_android",0);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Delete this Subnet?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {
                                            mOverlayDialog.show();
                                            HttpRequestController.getInstance(getActivity().getApplicationContext()).deleteSubnet(new HttpRequestController.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    if (result.equals("success")){
                                                        Toast.makeText(getActivity().getApplicationContext(),"Delete the Subnet Succeed", Toast.LENGTH_SHORT).show();
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
                                                        Toast.makeText(getActivity().getApplicationContext(),"Fail to delete this Subnet", Toast.LENGTH_SHORT).show();

                                                    }

                                                }
                                            },subnetID);
                                        }
                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                                    dialogSN.dismiss();
                                }
                            });
                            dialogSN.setContentView(inflate);
                            Window dialogWindow = dialogSN.getWindow();
                            dialogWindow.setGravity(Gravity.BOTTOM);
                            //get the attributes of the window

                            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                            lp.y =20;
                            dialogWindow.setAttributes(lp);
                            dialogSN.show();

                        }
                    };

                subnetLV.setOnItemClickListener(onListClick);
                mOverlayDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },getActivity(),networkID);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                CreateSubnetFragment csf  = new CreateSubnetFragment();
                bundle3 = new Bundle();
                bundle3.putString("networkID",networkID);
                csf.setArguments(bundle3);
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, csf, csf.getTag()).commit();

            }
        });

    return myView;
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