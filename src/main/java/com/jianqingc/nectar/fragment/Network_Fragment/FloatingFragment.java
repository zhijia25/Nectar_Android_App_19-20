package com.jianqingc.nectar.fragment.Network_Fragment;

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
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class FloatingFragment extends Fragment {
    View myView;
    ArrayList<String[]> floatingListArray;
    JSONArray listFloatingResultArrayP;
    Bundle bundle;
    String floatingID;
    String IPaddress;
    String resourcePool;
    String status;

    public FloatingFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_floating, container , false);
        setHasOptionsMenu(true);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Floating IP");

        final java.util.Timer timer = new java.util.Timer(true);
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        HttpRequest.getInstance(getContext()).listFloatingIP(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    listFloatingResultArrayP = new JSONArray(result);

                    floatingListArray = new ArrayList<String[]>();
                    for (int i=0; i<listFloatingResultArrayP.length();i++)
                    {
                        String id = listFloatingResultArrayP.getJSONObject(i).getString("floatingID");
                        String floating_ip_address = listFloatingResultArrayP.getJSONObject(i).getString("floating_ip_address");
                        String floating_network_id = listFloatingResultArrayP.getJSONObject(i).getString("floating_network_id");
                        String description = listFloatingResultArrayP.getJSONObject(i).getString("description");
                        status = listFloatingResultArrayP.getJSONObject(i).getString("status");
                        if (floating_network_id.equals("e48bdd06-cc3e-46e1-b7ea-64af43c74ef8")) {
                            resourcePool = "melbourne";
                        } else if (floating_network_id.equals("058b38de-830a-46ab-9d95-7a614cb06f1b")){
                            resourcePool = "QRIScloud";

                        }else if (floating_network_id.equals("24dbaea8-c8ab-43dc-ba5c-0babc141c20e")){
                            resourcePool = "tasmania";
                        }

                        String [] floatingList = {
                                floating_ip_address,
                                resourcePool,
                                status,
                                id,
                                description
                        };
                        floatingListArray.add(floatingList);

                    }
                    BindDictionary<String[]> dictionary = new BindDictionary<String[]>();
                    dictionary.addStringField(R.id.FloatingAddressLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[0];
                        }
                    });

                    dictionary.addStringField(R.id.FloatingPoolLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[1];
                        }
                    });

                    dictionary.addStringField(R.id.FloatingStateLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[2];
                        }
                    });
                    dictionary.addStringField(R.id.FloatingDescriptionLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[4];
                        }
                    });

                    FunDapter adapter = new FunDapter(FloatingFragment.this.getActivity(), floatingListArray, R.layout.floating_list_pattern, dictionary);
                    ListView floatingLV = (ListView) myView.findViewById(R.id.listViewFloating);
                    adapter.notifyDataSetChanged();
                    floatingLV.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(floatingLV);

                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener(){
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            bundle = new Bundle();
                            final String floatingID = floatingListArray.get(position)[3];

                            final Dialog dialogFL = new Dialog(getActivity(),R.style.ActionSheetDialogStyle);
                            View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.launch_floating_dialog,null);

                            TextView releaseFloatingIP = (TextView) inflate.findViewById(R.id.deleteFloatingIP);

                            releaseFloatingIP.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    SharedPreferences sharedPreferences = getContext().getApplicationContext().getSharedPreferences("nectar_android",0);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Release this Floating IP? Once a floating IP is released, there is no guarantee the same IP can be allocated again.")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    mOverlayDialog.show();
                                                    HttpRequest.getInstance(getActivity().getApplicationContext()).deleteFloatingIP(new HttpRequest.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            if(result.equals("success")){
                                                                Toast.makeText(getActivity().getApplicationContext(), "Release the floating IP Succeed", Toast.LENGTH_SHORT).show();
                                                                TimerTask task = new TimerTask(){
                                                                    @Override
                                                                    public void run() {
                                                                        mOverlayDialog.dismiss();
                                                                        FragmentManager manager = getFragmentManager();
                                                                        FloatingFragment floatingFragment = new FloatingFragment();
                                                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, floatingFragment, floatingFragment.getTag()).commit();

                                                                    }
                                                                };
                                                                timer.schedule(task,4000);

                                                            } else {
                                                                mOverlayDialog.dismiss();
                                                                Toast.makeText(getActivity().getApplicationContext(),"Fail to Release this floating IP", Toast.LENGTH_SHORT).show();

                                                            }
                                                        }
                                                    },floatingID);

                                                }
                                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();
                                    dialogFL.dismiss();



                                }
                            });

                            dialogFL.setContentView(inflate);
                            Window dialogWindow = dialogFL.getWindow();
                            dialogWindow.setGravity(Gravity.BOTTOM);

                            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                            lp.y = 20;
                            dialogWindow.setAttributes(lp);
                            dialogFL.show();
                        }
                    };



                    // set the item list click action later
                    floatingLV.setOnItemClickListener(onListClick);

                    mOverlayDialog.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },getActivity());



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
                FloatingFragment imFragment = new FloatingFragment();

                ft.replace(R.id.relativelayout_for_fragment, imFragment, imFragment.getTag()).commit();
                //System.out.println("hihihiheeeee");

            }
        });

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.create_floatingIP).setVisible(true);
    }


}