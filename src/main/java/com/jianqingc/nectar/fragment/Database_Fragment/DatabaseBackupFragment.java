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
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class DatabaseBackupFragment extends Fragment {

    View myView;
    JSONArray databaseBackupFragmentResultArray;
//    JSONArray configGroupFragmentResultArray;
    Bundle bundle;
    String databaseBackupId;


    public DatabaseBackupFragment() {
        // Required empty public constructor
    }

    private void refresh() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this);
        ft.attach(this);
        ft.commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_database_backup, container, false);
        setHasOptionsMenu(true);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Database Backup");

        final Timer timer = new Timer(true);

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();
        // Inflate the layout for this fragment
        HttpRequest.getInstance(getContext()).listDatabaseBackup(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    databaseBackupFragmentResultArray = new JSONArray(result);
//                    attachArray=new ArrayList<String>();

                    final ArrayList<String[]> configGroupListArray = new ArrayList<String[]>();


                    //do something with jsonArray
                    for (int i = 0; i < databaseBackupFragmentResultArray.length(); i++) {
//                        String status=configGroupFragmentResultArray.getJSONObject(i).getString("volumeStatus");
//                        String first = status.substring(0, 1).toUpperCase();
//                        String rest = status.substring(1, status.length());
//                        String newStatus = new StringBuffer(first).append(rest).toString();
                        String[] configGroupList = {
                                databaseBackupFragmentResultArray.getJSONObject(i).getString("databaseBackupId"),
                                databaseBackupFragmentResultArray.getJSONObject(i).getString("databaseBackupName"),
                                databaseBackupFragmentResultArray.getJSONObject(i).getString("databaseBackupDatabase"),
                                databaseBackupFragmentResultArray.getJSONObject(i).getString("databaseBackupDatastore"),
                                databaseBackupFragmentResultArray.getJSONObject(i).getString("databaseBackupVersion"),
                                databaseBackupFragmentResultArray.getJSONObject(i).getString("databaseBackupStatus")
                        };
                        configGroupListArray.add(configGroupList);
                    }
                    BindDictionary<String[]> dictionary = new BindDictionary<String[]>();
                    dictionary.addStringField(R.id.databaseBackupNameLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return (volumeList[1]);
                        }
                    });
                    dictionary.addStringField(R.id.databaseBackupDatastoreLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return (volumeList[3]);
                        }
                    });
                    dictionary.addStringField(R.id.databaseBackupVersionLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return volumeList[4];
                        }
                    });
                    dictionary.addStringField(R.id.databaseBackupDatabaseLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return volumeList[2];
                        }
                    });
                    dictionary.addStringField(R.id.databaseBackupStatusLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] volumeList, int position) {
                            return volumeList[5];
                        }
                    });

                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */
                    FunDapter adapter = new FunDapter(DatabaseBackupFragment.this.getActivity(), configGroupListArray, R.layout.database_backup_list_pattern, dictionary);
                    ListView configGroupLV = (ListView) myView.findViewById(R.id.listViewDatabaseBackup);
                    configGroupLV.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    setListViewHeightBasedOnChildren(configGroupLV);


                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            bundle = new Bundle();
                            databaseBackupId = configGroupListArray.get(position)[0];
                            bundle.putString("databaseBackupId", databaseBackupId);
//                                configGroupId=configGroupFragmentResultArray.getJSONObject(position).getString("configGroupId");
//                                availibilityZoneVolume=configGroupFragmentResultArray.getJSONObject(position).getString("volumeAZ");
//                                bundle.putString("configGroupId" , configGroupFragmentResultArray.getJSONObject(position).getString("volumeId"));


                            final Dialog dialogLV = new Dialog(getActivity(), R.style.ActionSheetDialogStyle);
                            View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.launch_database_backup_dialog, null);

                            TextView viewDetailV = (TextView) inflate.findViewById(R.id.detailDatabaseBackup);
//                            TextView instancesV = (TextView) inflate.findViewById(R.id.instanceConfigGroup);
                            TextView deleteV = (TextView) inflate.findViewById(R.id.deleteDatabaseBackup);

                            //view detail
                            viewDetailV.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    FragmentTransaction ft = getActivity().getSupportFragmentManager()
                                            .beginTransaction();
                                    DatabaseBackupDetailFragment databaseBackupDetailFragment = new DatabaseBackupDetailFragment();
                                    databaseBackupDetailFragment.setArguments(bundle);
                                    ft.replace(R.id.relativelayout_for_fragment, databaseBackupDetailFragment, databaseBackupDetailFragment.getTag());
                                    ft.commit();
                                    dialogLV.dismiss();

                                }
                            });



                            deleteV.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Are you sure to delete this backup?")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    mOverlayDialog.show();
                                                    HttpRequest.getInstance(getActivity().getApplicationContext()).deleteDatabaseBackup(new HttpRequest.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            if (result.equals("success")) {
                                                                Toast.makeText(getActivity().getApplicationContext(), "Delete the configuration group Succeed", Toast.LENGTH_SHORT).show();
                                                                TimerTask task = new TimerTask() {
                                                                    @Override
                                                                    public void run() {
                                                                        mOverlayDialog.dismiss();
                                                                        FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                        DatabaseBackupFragment vFragment = new DatabaseBackupFragment();
                                                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
                                                                    }
                                                                };
                                                                /**
                                                                 * Delay 7 secs after the button onclick method is called.
                                                                 * Wait for server status update. The server status is not modified in real-time.
                                                                 */
                                                                timer.schedule(task, 4000);
                                                            } else {
                                                                mOverlayDialog.dismiss();
                                                                Toast.makeText(getActivity().getApplicationContext(), "Fail to delete this security group", Toast.LENGTH_SHORT).show();
                                                                FragmentManager manager = getActivity().getSupportFragmentManager();
                                                                DatabaseBackupFragment vFragment = new DatabaseBackupFragment();
                                                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
                                                            }
                                                        }
                                                    }, databaseBackupId);
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
                DatabaseBackupFragment vFragment = new DatabaseBackupFragment();

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
            totalHeight += listItem.getMeasuredHeight() * 1.05;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()get the height of the divider
        // params.height can finally get the total height to display
        listView.setLayoutParams(params);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflate) {
        // TODO Auto-generated method stub
        //super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.create_database_backup).setVisible(true);
        //menu.findItem(R.id.import_KP).setVisible(true);

    }

}
