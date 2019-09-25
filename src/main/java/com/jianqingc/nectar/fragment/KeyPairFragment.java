package com.jianqingc.nectar.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
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
import java.util.TimerTask;

/**
 * Created by HuangMengxue on 17/5/7.
 */

public class KeyPairFragment extends Fragment{
    View myView;
    JSONArray listKPResultArray;
    Bundle bundle;
    String kpName;

    public KeyPairFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        myView = inflater.inflate(R.layout.fragment_list_keypair, container, false);
        setHasOptionsMenu(true);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Security Groups & Keys");
        // Inflate the layout for this fragment
        final java.util.Timer timer = new java.util.Timer(true);
        final Button securityG = (Button) myView.findViewById(R.id.security_groups2);
        final Button keyP = (Button) myView.findViewById(R.id.key_pairs2);
        keyP.setEnabled(true);
        keyP.setVisibility(View.VISIBLE);
        securityG.setEnabled(true);
        securityG.setVisibility(View.VISIBLE);
        keyP.setBackgroundColor(Color.parseColor("#e6e6e6"));
        keyP.setTextColor(Color.parseColor("#8c8c8c"));

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();


        HttpRequestController.getInstance(getContext()).listKeyPair(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */

                    listKPResultArray = new JSONArray(result);
                    ArrayList<String[]> kpListArray = new ArrayList<String[]>();

                    for (int i = 0; i < listKPResultArray.length(); i++) {
                        String[] instanceList = {
                                listKPResultArray.getJSONObject(i).getString("kpName"),
                                listKPResultArray.getJSONObject(i).getString("kpFingerPrint")
                        };
                        kpListArray.add(instanceList);
                    }
                    System.out.println(kpListArray);
                    BindDictionary<String[]> dictionary = new BindDictionary<String[]>();

                    dictionary.addStringField(R.id.kpNameLKP, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] instanceList, int position) {
                            return (instanceList[0]);
                        }
                    });

                    dictionary.addStringField(R.id.kpFingerprintLKP, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] instanceList, int position) {
                            return (instanceList[1]);
                        }
                    });



                    FunDapter adapter = new FunDapter(KeyPairFragment.this.getActivity(), kpListArray, R.layout.keypair_list_pattern, dictionary);
                    ListView kpLV = (ListView) myView.findViewById(R.id.listViewKeyPair);
                    adapter.notifyDataSetChanged();
                    kpLV.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(kpLV);

                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            /**
                             * Clicking on the items in the Listview will lead to Instance Detail Fragment.
                             */
                            bundle = new Bundle();
                            try {
                                kpName = listKPResultArray.getJSONObject(position).getString("kpName");
                                bundle.putString("keyPairName", kpName);

                                final Dialog dialogLI = new Dialog(getActivity(),R.style.ActionSheetDialogStyle);
                                View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.launch_kp_dialog, null);

                                TextView viewDetailKP = (TextView) inflate.findViewById(R.id.viewDetailKPD);
                                TextView deleteKP = (TextView) inflate.findViewById(R.id.deleteKPD);

                                viewDetailKP.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        FragmentTransaction ft = getActivity().getSupportFragmentManager()
                                                .beginTransaction();
                                        KeyPairDetailFragment kpDetailFragment = new KeyPairDetailFragment();
                                        kpDetailFragment.setArguments(bundle);
                                        ft.replace(R.id.relativelayout_for_fragment, kpDetailFragment, kpDetailFragment.getTag());
                                        ft.commit();
                                        dialogLI.dismiss();

                                    }
                                });

                                deleteKP.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage("Delete this key pair?")
                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        mOverlayDialog.show();
                                                        HttpRequestController.getInstance(getActivity().getApplicationContext()).deleteKeyPair(new HttpRequestController.VolleyCallback() {
                                                            @Override
                                                            public void onSuccess(String result) {
                                                                if(result.equals("success")) {
                                                                    Toast.makeText(getActivity().getApplicationContext(), "Delete the key pair Succeed", Toast.LENGTH_SHORT).show();
                                                                    TimerTask task = new TimerTask() {
                                                                        @Override
                                                                        public void run() {
                                                                            mOverlayDialog.dismiss();
                                                                            FragmentManager manager = getFragmentManager();
                                                                            KeyPairFragment kpFragment = new KeyPairFragment();
                                                                            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, kpFragment, kpFragment.getTag()).commit();
                                                                        }
                                                                    };
                                                                    /**
                                                                     * Delay 7 secs after the button onclick method is called.
                                                                     * Wait for server status update. The server status is not modified in real-time.
                                                                     */
                                                                    timer.schedule(task, 4000);
                                                                } else{
                                                                    mOverlayDialog.dismiss();
                                                                    Toast.makeText(getActivity().getApplicationContext(), "Fail to delete this key pair", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        }, kpName);
                                                    }
                                                })
                                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).show();

                                        dialogLI.dismiss();

                                    }
                                });


                                //Set the view to Dialog
                                dialogLI.setContentView(inflate);
                                Window dialogWindow = dialogLI.getWindow();
                                dialogWindow.setGravity(Gravity.BOTTOM);
                                //Get the attributes of teh window
                                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                                lp.y = 20;//Set the distance of the dialog to the bottom
                                //Set the attribute to  the dialog
                                dialogWindow.setAttributes(lp);
                                dialogLI.show();//Show the dialog
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    kpLV.setOnItemClickListener(onListClick);
                    mOverlayDialog.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, getActivity());






        /*
         * Set Security Group button
         */
        securityG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Snackbar.make(view, "Refreshing...", Snackbar.LENGTH_SHORT).show();
                //mOverlayDialog.show();
                //mOverlayDialog.dismiss();
                //reload this page
                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                AccessAndSecurityFragment accessAndSecurityFragment = new AccessAndSecurityFragment();
                ;
                ft.replace(R.id.relativelayout_for_fragment, accessAndSecurityFragment, accessAndSecurityFragment.getTag()).commit();
                //System.out.println("hihihiheeeee");

            }
        });

        /*
         * Set Key Pair button
         */
        keyP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "Refreshing...", Snackbar.LENGTH_SHORT).show();
                mOverlayDialog.show();
                mOverlayDialog.dismiss();
                //reload this page
                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                KeyPairFragment kpFragment = new KeyPairFragment();

                ft.replace(R.id.relativelayout_for_fragment, kpFragment, kpFragment.getTag()).commit();
                //System.out.println("hihihiheeeee");

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
                KeyPairFragment kpFragment = new KeyPairFragment();

                ft.replace(R.id.relativelayout_for_fragment, kpFragment, kpFragment.getTag()).commit();
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
            totalHeight += listItem.getMeasuredHeight()*1.1;
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
        //menu.findItem(R.id.add_KP).setVisible(true);
        menu.findItem(R.id.import_KP).setVisible(true);
        menu.findItem(R.id.add_KP).setVisible(true);

    }
}
