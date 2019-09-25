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
 * Created by HuangMengxue on 17/5/18.
 */

public class VolumeSnapshotFragment extends Fragment  {
    View myView;
    JSONArray volumeSnapshotFragmentResultArray;
    Bundle bundle;
    String snapshotID;
    JSONArray instanceResult;
    String selectInstanceID;
    boolean bootEV;

    public VolumeSnapshotFragment() {
        // Required empty public constructor
    }

    private void refresh(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this);
        ft.attach(this);
        ft.commit();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_volume_snapshot, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Volumes");
        final java.util.Timer timer = new java.util.Timer(true);
        final Button volume2 = (Button) myView.findViewById(R.id.volume_button2);
        final Button volumeS2 = (Button) myView.findViewById(R.id.volumeSnapshot_button2);
        volume2.setEnabled(true);
        volume2.setVisibility(View.VISIBLE);
        volumeS2.setEnabled(true);
        volumeS2.setVisibility(View.VISIBLE);
        volumeS2.setBackgroundColor(Color.parseColor("#e6e6e6"));
        volumeS2.setTextColor(Color.parseColor("#8c8c8c"));

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();


        // Inflate the layout for this fragment
        HttpRequestController.getInstance(getContext()).listVolumeSnapshot(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    volumeSnapshotFragmentResultArray = new JSONArray(result);
                    //attachArray=new ArrayList<String>();

                    ArrayList<String[]> snapshotListArray = new ArrayList<String[]>();


                    //do something with jsonArray
                    for (int i=0; i<volumeSnapshotFragmentResultArray.length();i++){
                        String status=volumeSnapshotFragmentResultArray.getJSONObject(i).getString("snapStatus");
                        String first = status.substring(0, 1).toUpperCase();
                        String rest = status.substring(1, status.length());
                        String newStatus = new StringBuffer(first).append(rest).toString();
                        String[] volumeList ={
                                volumeSnapshotFragmentResultArray.getJSONObject(i).getString("snapName"),
                                newStatus,
                                volumeSnapshotFragmentResultArray.getJSONObject(i).getString("snapDescription"),
                                volumeSnapshotFragmentResultArray.getJSONObject(i).getString("snapID"),
                                volumeSnapshotFragmentResultArray.getJSONObject(i).getString("snapSize")
                        };
                        snapshotListArray.add(volumeList);
                    }
                    BindDictionary<String[]> dictionary = new BindDictionary<String[]>();
                    dictionary.addStringField(R.id.snapshotVSL, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return (volumeList[0]);
                        }
                    });
                    dictionary.addStringField(R.id.statusVSL, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return (volumeList[1]);
                        }
                    });
                    dictionary.addStringField(R.id.discriptionVSL, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return volumeList[2];
                        }
                    });
                    dictionary.addStringField(R.id.idVSL, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return volumeList[3];
                        }
                    });
                    dictionary.addStringField(R.id.sizeVSL, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return (volumeList[4] +" GB");
                        }
                    });

                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */
                    FunDapter adapter = new FunDapter(VolumeSnapshotFragment.this.getActivity(),snapshotListArray,R.layout.volume_snapshot_list_pattern,dictionary);
                    ListView volumeLV = (ListView) myView.findViewById(R.id.listViewVolumeSnapshot);
                    volumeLV.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    setListViewHeightBasedOnChildren(volumeLV);





                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            bundle = new Bundle();
                            try {
                                snapshotID=volumeSnapshotFragmentResultArray.getJSONObject(position).getString("snapID");
                                bundle.putString("snapshotId" , volumeSnapshotFragmentResultArray.getJSONObject(position).getString("snapID"));


                                final Dialog dialogLV = new Dialog(getActivity(),R.style.ActionSheetDialogStyle);
                                View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.luanch_snapshot_dialog, null);

                                TextView detailS = (TextView) inflate.findViewById(R.id.viewDetailSL);
                                TextView editS = (TextView) inflate.findViewById(R.id.editVSL);
                                TextView deleteS = (TextView) inflate.findViewById(R.id.deleteSL);



                                detailS.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        FragmentTransaction ft = getActivity().getSupportFragmentManager()
                                                .beginTransaction();
                                        VolumeSnapshotDetailFragment snapshotDetailFragment = new VolumeSnapshotDetailFragment();
                                        snapshotDetailFragment.setArguments(bundle);
                                        ft.replace(R.id.relativelayout_for_fragment, snapshotDetailFragment,snapshotDetailFragment.getTag());
                                        ft.commit();
                                        dialogLV.dismiss();

                                    }
                                });

                                editS.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        LayoutInflater factory = LayoutInflater.from(getActivity());
                                        final View textEntryView = factory.inflate(R.layout.create_sg, null);
                                        final EditText nameV = (EditText) textEntryView.findViewById(R.id.createSGName);
                                        final EditText decriptionV = (EditText)textEntryView.findViewById(R.id.createSGDescription);
                                        //final CheckBox bootableV = (CheckBox)textEntryView.findViewById(R.id.bootableV);
                                        AlertDialog.Builder builderSecurityGroup = new AlertDialog.Builder(getActivity());
                                        builderSecurityGroup.setTitle("Edit Volume Snapshot");
                                        builderSecurityGroup.setIcon(R.drawable.nectar_app_icon);
                                        //builderSecurityGroup.setIcon(android.R.drawable.ic_dialog_info);
                                        builderSecurityGroup.setView(textEntryView);



                                        builderSecurityGroup.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                String nameNew= nameV.getText().toString();
                                                String descriptionNew= decriptionV.getText().toString();
                                                dialog.dismiss();
                                                mOverlayDialog.show();
                                                HttpRequestController.getInstance(getActivity().getApplicationContext()).editVolumeSnapshot(new HttpRequestController.VolleyCallback() {
                                                    @Override
                                                    public void onSuccess(String result) {
                                                        if(result.equals("success")) {
                                                            //mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Edit Volume Snapshot successfully", Toast.LENGTH_SHORT).show();
                                                            TimerTask task = new TimerTask() {
                                                                @Override
                                                                public void run() {
                                                                    mOverlayDialog.dismiss();
                                                                    FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                    VolumeSnapshotFragment vFragment = new VolumeSnapshotFragment();
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
                                                            Toast.makeText(getActivity().getApplicationContext(), "Fail to edit volume snapshot", Toast.LENGTH_SHORT).show();
                                                            FragmentManager manager = getActivity().getSupportFragmentManager();
                                                            VolumeSnapshotFragment vFragment = new VolumeSnapshotFragment();
                                                            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();

                                                        }
                                                    }
                                                },nameNew, descriptionNew,snapshotID);



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



                                deleteS.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage("Are you sure to delete this volume?")
                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        mOverlayDialog.show();
                                                        HttpRequestController.getInstance(getActivity().getApplicationContext()).deleteVolumeSnapshot(new HttpRequestController.VolleyCallback() {
                                                            @Override
                                                            public void onSuccess(String result) {
                                                                if(result.equals("success")) {
                                                                    Toast.makeText(getActivity().getApplicationContext(), "Delete the volume snapshot Succeed", Toast.LENGTH_SHORT).show();
                                                                    TimerTask task = new TimerTask() {
                                                                        @Override
                                                                        public void run() {
                                                                            mOverlayDialog.dismiss();
                                                                            FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                            VolumeSnapshotFragment vsFragment = new VolumeSnapshotFragment();
                                                                            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vsFragment, vsFragment.getTag()).commit();
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
                                                                    VolumeSnapshotFragment vsFragment = new VolumeSnapshotFragment();
                                                                    manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vsFragment, vsFragment.getTag()).commit();
                                                                }
                                                            }
                                                        }, snapshotID);
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
        volume2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Snackbar.make(view, "Refreshing...", Snackbar.LENGTH_SHORT).show();
                //mOverlayDialog.show();
                //mOverlayDialog.dismiss();
                //reload this page
                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                VolumeFragment vFragment = new VolumeFragment();

                ft.replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
                System.out.println("hihihiheeeee");

            }
        });

        /*
         * Set Key Pair button
         */
        volumeS2.setOnClickListener(new View.OnClickListener() {
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
                refresh();
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
}
