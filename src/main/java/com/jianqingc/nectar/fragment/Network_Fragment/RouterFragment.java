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
import com.jianqingc.nectar.httpRequest.HttpRequest;
import com.tuesda.walker.circlerefresh.CircleRefreshLayout;

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
public class RouterFragment extends Fragment {
    View myView;
    ArrayList<String[]> routerListArray;
    JSONArray listRouterResultArrayP;
    Bundle bundle;
    String routerName;
    String status;
    String externalNetwork;
    String adminState;
    String chooseAdminState;
    String editRouterName;
    boolean admin_state;
    String newEditRouterName;


    public RouterFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.simple_list_view, container , false);
        setHasOptionsMenu(true);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Router");

        final java.util.Timer timer = new java.util.Timer(true);
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        HttpRequest.getInstance(getContext()).listRouter(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    listRouterResultArrayP = new JSONArray(result);
                    routerListArray = new ArrayList<String[]>();
                    for(int i = 0; i< listRouterResultArrayP.length(); i++)
                    {

                        String name = listRouterResultArrayP.getJSONObject(i).getString("routerName");
                        String status =listRouterResultArrayP.getJSONObject(i).getString("status");
                        String network_id = listRouterResultArrayP.getJSONObject(i).getString("network_id");
                        final String projectID = listRouterResultArrayP.getJSONObject(i).getString("project_id");
                        String subnetID = listRouterResultArrayP.getJSONObject(i).getString("subnet_id");
                        String ipAddress = listRouterResultArrayP.getJSONObject(i).getString("ip_address");
                        String snat_enable = listRouterResultArrayP.getJSONObject(i).getString("enable_snat");
                        String snat;
                        if (snat_enable.equals("true")){
                            snat = "Enabled";
                        } else {
                            snat = "Not Enabled";
                        }



                        if (network_id.equals("e48bdd06-cc3e-46e1-b7ea-64af43c74ef8")) {
                            externalNetwork = "melbourne";
                        } else if (network_id.equals("058b38de-830a-46ab-9d95-7a614cb06f1b")){
                            externalNetwork = "QRIScloud";

                        }else if (network_id.equals("24dbaea8-c8ab-43dc-ba5c-0babc141c20e")){
                            externalNetwork = "tasmania";
                        }



                        String admin_state_up = listRouterResultArrayP.getJSONObject(i).getString("admin_state_up");

                        if (admin_state_up.equals("true")){
                            adminState = "UP";

                        } else {
                            adminState = "DOWN";
                        }
                        String id = listRouterResultArrayP.getJSONObject(i).getString("routerID");

                        String [] routerList = {
                                name,
                                status,
                                externalNetwork,
                                adminState,
                                id,
                                projectID,
                                network_id,
                                subnetID,
                                snat,
                                ipAddress




                        };
                        routerListArray.add(routerList);

                    }
                    BindDictionary<String[]> dictionary = new BindDictionary<String[]>();
                    dictionary.addStringField(R.id.routerNameLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[0];
                        }
                    });
                    dictionary.addStringField(R.id.routerStatusLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[1];
                        }
                    });

                    dictionary.addStringField(R.id.routerExternalLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[2];
                        }
                    });
                    dictionary.addStringField(R.id.routerAdaminStateLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[3];
                        }
                    });

                    FunDapter adapter = new FunDapter(RouterFragment.this.getActivity(), routerListArray, R.layout.router_list_pattern, dictionary);
                    ListView routerLV = (ListView) myView.findViewById(R.id.listView);
                    adapter.notifyDataSetChanged();
                    routerLV.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(routerLV);

                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener(){
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            bundle = new Bundle();
                            //final String routerID = routerListArray.get(position)[3];

                            final Dialog dialogR = new Dialog(getActivity(),R.style.ActionSheetDialogStyle);
                            View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.launch_router_dialog,null);

                            final String routerName = routerListArray.get(position)[0];
                            final String routerID = routerListArray.get(position)[4];
                            String projectID = routerListArray.get(position)[5];
                            String status = routerListArray.get(position)[1];
                            String networkName = routerListArray.get(position)[2];
                            String networkID = routerListArray.get(position)[6];
                            String subnetID = routerListArray.get(position)[7];
                            String snat = routerListArray.get(position)[8];

                            String adminState = routerListArray.get(position)[3];
                            String ipAddress = routerListArray.get(position)[9];

                            bundle.putString("routerName",routerName);
                            bundle.putString("routerID",routerID);
                            bundle.putString("projectID",projectID);
                            bundle.putString("status",status);
                            bundle.putString("networkName",networkName);
                            bundle.putString("networkID",networkID);
                            bundle.putString("subnetID",subnetID);
                            bundle.putString("snat",snat);
                            bundle.putString("adminState",adminState);
                            bundle.putString("ipaddress",ipAddress);



                            TextView viewRouterDetail = (TextView) inflate.findViewById(R.id.viewRouterDetail);

                            TextView deleteRouter = (TextView) inflate.findViewById(R.id.deleteRouter);

                            TextView editRouter = (TextView) inflate.findViewById(R.id.editRouter);

                            viewRouterDetail.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                    RouterDetailFragment routerDetailFragment = new RouterDetailFragment();
                                    routerDetailFragment.setArguments(bundle);
                                    ft.replace(R.id.relativelayout_for_fragment, routerDetailFragment, routerDetailFragment.getTag());
                                    ft.commit();
                                    dialogR.dismiss();
                                }
                            });

                            /*
                            * edit
                            * */
                            editRouter.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    LayoutInflater factory = LayoutInflater.from(getActivity());
                                    final View textEntryView = factory.inflate(R.layout.edit_router, null);
                                    final EditText nameV = (EditText) textEntryView.findViewById(R.id.editName);
                                    final Spinner selectAdminStateAV = (Spinner) textEntryView.findViewById(R.id.adminState);
                                    AlertDialog.Builder builderSecurityGroup = new AlertDialog.Builder(getActivity());
                                    builderSecurityGroup.setTitle("Edit Router");
                                    builderSecurityGroup.setIcon(R.drawable.nectar_app_icon);
                                    //builderSecurityGroup.setIcon(android.R.drawable.ic_dialog_info);
                                    builderSecurityGroup.setView(textEntryView);

                                    final List<String> adminState_list;

                                    ArrayAdapter<String> arr_adapter_admin;
                                    adminState_list = new ArrayList<String>();
                                    adminState_list.add("UP");
                                    adminState_list.add("DOWN");
                                    arr_adapter_admin = new ArrayAdapter<String>(RouterFragment.this.getActivity(), android.R.layout.simple_spinner_item,adminState_list);

                                    arr_adapter_admin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                    selectAdminStateAV.setAdapter(arr_adapter_admin);

                                    selectAdminStateAV.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                                        @Override
                                        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                            chooseAdminState = adminState_list.get(arg2);
                                            arg0.setVisibility(View.VISIBLE);
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> adapterView) {

                                        }
                                    });

                                    builderSecurityGroup.setPositiveButton("Edit Router", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                mOverlayDialog.show();
                                                editRouterName = nameV.getText().toString();
                                            if(chooseAdminState.equals("UP")){
                                                admin_state = true;
                                            } else {
                                                admin_state = false;
                                            }
                                                HttpRequest.getInstance(getActivity().getApplicationContext()).editRouter(new HttpRequest.VolleyCallback() {
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
                                                                    RouterFragment vFragment = new RouterFragment();
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
                                                            Toast.makeText(getActivity().getApplicationContext(), "Fail to edit router", Toast.LENGTH_SHORT).show();
                                                            FragmentManager manager = getActivity().getSupportFragmentManager();
                                                            RouterFragment vFragment = new RouterFragment();
                                                            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();

                                                        }
                                                    }
                                                },editRouterName, admin_state, routerID, routerName);




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
                                    dialogR.dismiss();


                                }
                            });

                            /*
                            * delete
                            * */
                            deleteRouter.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    SharedPreferences sharedPreferences = getContext().getApplicationContext().getSharedPreferences("nectar_android",0);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Delete this Router?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {
                                            mOverlayDialog.show();
                                            HttpRequest.getInstance(getActivity().getApplicationContext()).deleteRouter(new HttpRequest.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    if (result.equals("success")){
                                                        Toast.makeText(getActivity().getApplicationContext(),"Delete the Router Succeed", Toast.LENGTH_SHORT).show();
                                                        TimerTask task = new TimerTask(){
                                                            @Override
                                                            public void run() {
                                                                mOverlayDialog.dismiss();
                                                                FragmentManager manager = getFragmentManager();
                                                                RouterFragment RouterFragment = new RouterFragment();
                                                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, RouterFragment, RouterFragment.getTag()).commit();

                                                            }
                                                        };
                                                        timer.schedule(task,4000);

                                                    } else {
                                                        mOverlayDialog.dismiss();
                                                        Toast.makeText(getActivity().getApplicationContext(),"Fail to delete this Router", Toast.LENGTH_SHORT).show();

                                                    }

                                                }
                                            },routerID);
                                        }
                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                                    dialogR.dismiss();
                                }

                            });

                            //set the view to Dialog

                            dialogR.setContentView(inflate);
                            Window dialogWindow = dialogR.getWindow();
                            dialogWindow.setGravity(Gravity.BOTTOM);
                            //get the attributes of the window

                            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                            lp.y =20;
                            dialogWindow.setAttributes(lp);
                            dialogR.show();

                        }
                    };

                routerLV.setOnItemClickListener(onListClick);
                mOverlayDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /**
                 * Set refresh/back button.
                 */

                CircleRefreshLayout mRefreshLayout = (CircleRefreshLayout) getActivity().findViewById(R.id.refresh_layout);
                mRefreshLayout.setOnRefreshListener(
                        new CircleRefreshLayout.OnCircleRefreshListener() {

                            @Override
                            public void refreshing() {
                                // do something when refresh starts
                                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                RouterFragment vFragment = new RouterFragment();
                                ft.replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
                                Log.i(TAG,"Refresh success");
                            }

                            @Override
                            public void completeRefresh() {
                                // do something when refresh complete
//                                ft.replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
                            }
                        });

            }
        }, getActivity());

    return myView;

    }


    @Override
    public void onPause() {
        /**
         * remove refresh button when this fragment is hiden.
         */
        super.onPause();
        FloatingActionButton fabRight = (FloatingActionButton) getActivity().findViewById(R.id.fabRight);
        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabRight.setVisibility(View.GONE);
        fabRight.setEnabled(false);
        fabLeft.setVisibility(View.GONE);
        Toolbar toolbar =(Toolbar) getActivity().findViewById(R.id.toolbar);
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
            totalHeight += listItem.getMeasuredHeight()*1.1;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()get the height of the divider
        // params.height can finally get the total height to display
        listView.setLayoutParams(params);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.create_router).setVisible(true);

    }
}