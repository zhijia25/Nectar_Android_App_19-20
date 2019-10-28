package com.jianqingc.nectar.fragment.Database_Fragment;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigurationGroupFragment extends Fragment {

    View myView;
//    JSONArray volumeFragmentResultArray;
    JSONArray configGroupFragmentResultArray;
    Bundle bundle;
    List<String> attachArray;
    String attachID;
    String attachmentID;
//    String volumeID;
    JSONArray instanceResult;
    String selectInstanceID;
    boolean bootEV;
    String availibilityZoneVolume;
    String zoneServer;
    String configGroupId;


    public ConfigurationGroupFragment() {
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
        myView = inflater.inflate(R.layout.fragment_configuration_group, container, false);
        setHasOptionsMenu(true);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Configuration Groups");

        final Timer timer = new Timer(true);
//        final Button volume = (Button) myView.findViewById(R.id.volume_button1);
//        final Button volumeS = (Button) myView.findViewById(R.id.volumeSnapshot_button1);
//        volume.setEnabled(true);
//        volume.setVisibility(View.VISIBLE);
//        volumeS.setEnabled(true);
//        volumeS.setVisibility(View.VISIBLE);
//        volume.setBackgroundColor(Color.parseColor("#e6e6e6"));
//        volume.setTextColor(Color.parseColor("#8c8c8c"));

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();
        // Inflate the layout for this fragment
        HttpRequest.getInstance(getContext()).listConfigGroup(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    configGroupFragmentResultArray = new JSONArray(result);
//                    attachArray=new ArrayList<String>();

                    final ArrayList<String[]> configGroupListArray = new ArrayList<String[]>();


                    //do something with jsonArray
                    for (int i=0; i<configGroupFragmentResultArray.length();i++){
//                        String status=configGroupFragmentResultArray.getJSONObject(i).getString("volumeStatus");
//                        String first = status.substring(0, 1).toUpperCase();
//                        String rest = status.substring(1, status.length());
//                        String newStatus = new StringBuffer(first).append(rest).toString();
                        String[] configGroupList ={
                                configGroupFragmentResultArray.getJSONObject(i).getString("configGroupId"),
                                configGroupFragmentResultArray.getJSONObject(i).getString("configGroupName"),
                                configGroupFragmentResultArray.getJSONObject(i).getString("configGroupDescription"),
                                configGroupFragmentResultArray.getJSONObject(i).getString("configGroupDatastore"),
                                configGroupFragmentResultArray.getJSONObject(i).getString("configGroupVersion")
                        };
                        configGroupListArray.add(configGroupList);
                    }
                    BindDictionary<String[]> dictionary = new BindDictionary<String[]>();
                    dictionary.addStringField(R.id.configrationGroupNameLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return (volumeList[1]);
                        }
                    });
                    dictionary.addStringField(R.id.configrationGroupDatastoreLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return (volumeList[3]);
                        }
                    });
                    dictionary.addStringField(R.id.configrationGroupVersionLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return volumeList[4];
                        }
                    });
                    dictionary.addStringField(R.id.configrationGroupDescriptionLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return volumeList[2];
                        }
                    });
//                    dictionary.addStringField(R.id.volumeDescriptionTV, new StringExtractor<String[]>() {
//                        @Override
//                        public String getStringValue(String[] volumeList, int position) {
//                            return volumeList[4];
//                        }
//                    });

                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */
                    FunDapter adapter = new FunDapter(ConfigurationGroupFragment.this.getActivity(),configGroupListArray,R.layout.configuration_group_list_pattern,dictionary);
                    ListView configGroupLV = (ListView) myView.findViewById(R.id.listViewConfigurationGroup);
                    configGroupLV.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    setListViewHeightBasedOnChildren(configGroupLV);





                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            bundle = new Bundle();
                            configGroupId = configGroupListArray.get(position)[0];
                            bundle.putString("configGroupId", configGroupId);
//                                configGroupId=configGroupFragmentResultArray.getJSONObject(position).getString("configGroupId");
//                                availibilityZoneVolume=configGroupFragmentResultArray.getJSONObject(position).getString("volumeAZ");
//                                bundle.putString("configGroupId" , configGroupFragmentResultArray.getJSONObject(position).getString("volumeId"));


                            final Dialog dialogLV = new Dialog(getActivity(),R.style.ActionSheetDialogStyle);
                            View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.launch_config_group_dialog, null);

                            TextView viewDetailV = (TextView) inflate.findViewById(R.id.detailConfigGroup);
                            TextView instancesV = (TextView) inflate.findViewById(R.id.instanceConfigGroup);
                            TextView deleteV = (TextView) inflate.findViewById(R.id.deleteCGD);

                            //view detail
                            viewDetailV.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    FragmentTransaction ft = getActivity().getSupportFragmentManager()
                                            .beginTransaction();
                                    ConfigurationGroupDetailFragment configurationGroupDetailFragment = new ConfigurationGroupDetailFragment();
                                    configurationGroupDetailFragment.setArguments(bundle);
                                    ft.replace(R.id.relativelayout_for_fragment, configurationGroupDetailFragment,configurationGroupDetailFragment.getTag());
                                    ft.commit();
                                    dialogLV.dismiss();

                                }
                            });

                            ///////// instances
                            instancesV.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    FragmentTransaction ft = getActivity().getSupportFragmentManager()
                                            .beginTransaction();
                                    ConfigurationGroupInstanceFragment configurationGroupInstanceFragment = new ConfigurationGroupInstanceFragment();
                                    configurationGroupInstanceFragment.setArguments(bundle);
                                    ft.replace(R.id.relativelayout_for_fragment, configurationGroupInstanceFragment,configurationGroupInstanceFragment.getTag());
                                    ft.commit();
                                    dialogLV.dismiss();

                                }
                            });


                            deleteV.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Are you sure to delete this configuration group?")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    mOverlayDialog.show();
                                                    HttpRequest.getInstance(getActivity().getApplicationContext()).deleteConfigGroup(new HttpRequest.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            if(result.equals("success")) {
                                                                Toast.makeText(getActivity().getApplicationContext(), "Delete the configuration group Succeed", Toast.LENGTH_SHORT).show();
                                                                TimerTask task = new TimerTask() {
                                                                    @Override
                                                                    public void run() {
                                                                        mOverlayDialog.dismiss();
                                                                        FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                        ConfigurationGroupFragment vFragment = new ConfigurationGroupFragment();
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
                                                                ConfigurationGroupFragment vFragment = new ConfigurationGroupFragment();
                                                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
                                                            }
                                                        }
                                                    }, configGroupId);
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


                        }
                    };
                    configGroupLV.setOnItemClickListener(onListClick);
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
//        volumeS.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                //Snackbar.make(view, "Refreshing...", Snackbar.LENGTH_SHORT).show();
//                //mOverlayDialog.show();
//                //mOverlayDialog.dismiss();
//                //reload this page
//                FragmentTransaction ft = getActivity().getSupportFragmentManager()
//                        .beginTransaction();
//                VolumeSnapshotFragment vsFragment = new VolumeSnapshotFragment();
//
//                ft.replace(R.id.relativelayout_for_fragment, vsFragment, vsFragment.getTag()).commit();
//                //System.out.println("hihihiheeeee");
//
//            }
//        });

        /*
         * Set Key Pair button
         */
//        volume.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Snackbar.make(view, "Refreshing...", Snackbar.LENGTH_SHORT).show();
//                mOverlayDialog.show();
//                mOverlayDialog.dismiss();
//                refresh();
//
//            }
//        });

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
                ConfigurationGroupFragment vFragment = new ConfigurationGroupFragment();

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
        menu.findItem(R.id.create_configuration_group).setVisible(true);
        //menu.findItem(R.id.import_KP).setVisible(true);

    }

}
