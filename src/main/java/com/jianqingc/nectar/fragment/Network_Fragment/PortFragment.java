package com.jianqingc.nectar.fragment.Network_Fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
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
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class PortFragment extends Fragment {
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
    String adminState;

    public PortFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_port, container, false);
        setHasOptionsMenu(true);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Ports");
        bundle = getArguments();
        networkID = bundle.getString("networkID");

        bundle2 = new Bundle();
        bundle2.putString("networkID",networkID);

        final java.util.Timer timer = new java.util.Timer(true);
        final Dialog mOverlayDialog = new Dialog(getActivity(),android.R.style.Theme_Panel);
        final Button create = (Button)myView.findViewById(R.id.createPortButton);
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


        HttpRequest.getInstance(getContext()).listPort(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    listSubnetResultArrayP = new JSONArray(result);
                    subnetListArray = new ArrayList<String[]>();
                    for(int i = 0; i< listSubnetResultArrayP.length();i++)
                    {
                        String name = listSubnetResultArrayP.getJSONObject(i).getString("portName");
                        String address = listSubnetResultArrayP.getJSONObject(i).getString("mac_address");
                        String status = listSubnetResultArrayP.getJSONObject(i).getString("status");
                        String portID = listSubnetResultArrayP.getJSONObject(i).getString("portID");
                        String admin_state_up = listSubnetResultArrayP.getJSONObject(i).getString("admin_state_up");
//                        String dns_name = listSubnetResultArrayP.getJSONObject(i).getString("dns_name");
                        String created_at = listSubnetResultArrayP.getJSONObject(i).getString("created_at");

                        if (admin_state_up.equals("true")){
                            adminState = "UP";

                        } else {
                            adminState = "DOWN";
                        }

                        String [] subnetList={
                                name,
                                address,
                                status,
                                portID,
                                adminState,
                                networkID,
//                                dns_name,
                                created_at

                        };
                        subnetListArray.add(subnetList);

                    }

                    BindDictionary<String []> dictionary = new BindDictionary<String[]>();
                    dictionary.addStringField(R.id.PortNameLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[0];
                        }
                    });

                    dictionary.addStringField(R.id.PortMacAddressLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[1];
                        }
                    });

                    dictionary.addStringField(R.id.PortStatusLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[2];
                        }
                    });
                    dictionary.addStringField(R.id.PortAdminStateLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[4];
                        }
                    });

                    FunDapter adapter = new FunDapter(PortFragment.this.getActivity(), subnetListArray, R.layout.port_list_pattern,dictionary);
                    ListView subnetLV = (ListView) myView.findViewById(R.id.listViewPort);
                    adapter.notifyDataSetChanged();
                    subnetLV.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(subnetLV);

                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                            String portName = subnetListArray.get(position)[0];
                            String address = subnetListArray.get(position)[1];
                            String status = subnetListArray.get(position)[2];
                            final String portID = subnetListArray.get(position)[3];
                            String admin_state_up = subnetListArray.get(position)[4];
                            String networkID = subnetListArray.get(position)[5];
//                            String dns_name = subnetListArray.get(position)[6];
                            String created_at = subnetListArray.get(position)[6];



                            bundle3 = new Bundle();
                            bundle3.putString("portName",portName);
                            bundle3.putString("portID",portID);
                            bundle3.putString("networkID",networkID);
                            bundle3.putString("status",status);
                            bundle3.putString("admin_state_up",admin_state_up);
                            bundle3.putString("address",address);
//                            bundle3.putString("dns_name",dns_name);
                            bundle3.putString("created_at",created_at);

                            final Dialog dialogSN = new Dialog(getActivity(),R.style.ActionSheetDialogStyle);
                            View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.launch_port_dialog,null);

                            TextView viewDetailPort = (TextView) inflate.findViewById(R.id.viewPortDetail);
                            TextView deletePort = (TextView) inflate.findViewById(R.id.deletePort);

                            viewDetailPort.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                    PortDetailFragment portDetailFragment = new PortDetailFragment();
                                    portDetailFragment.setArguments(bundle3);
                                    ft.replace(R.id.relativelayout_for_fragment,portDetailFragment,portDetailFragment.getTag()).commit();
                                    dialogSN.dismiss();
                                }
                            });

                            deletePort.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    SharedPreferences sharedPreferences = getContext().getApplicationContext().getSharedPreferences("nectar_android",0);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Delete this Port?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {
                                            mOverlayDialog.show();
                                            HttpRequest.getInstance(getActivity().getApplicationContext()).deletePort(new HttpRequest.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    if (result.equals("success")){
                                                        Toast.makeText(getActivity().getApplicationContext(),"Delete the Port Succeed", Toast.LENGTH_SHORT).show();
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
                                                        Toast.makeText(getActivity().getApplicationContext(),"Fail to delete this Port", Toast.LENGTH_SHORT).show();

                                                    }

                                                }
                                            },portID);
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
                CreatePortFragment cpf  = new CreatePortFragment();
                bundle3 = new Bundle();
                bundle3.putString("networkID",networkID);
                cpf.setArguments(bundle3);
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, cpf, cpf.getTag()).commit();

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