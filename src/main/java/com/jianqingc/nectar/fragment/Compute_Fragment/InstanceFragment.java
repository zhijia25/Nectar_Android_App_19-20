package com.jianqingc.nectar.fragment.Compute_Fragment;


import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.Menu;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.jianqingc.nectar.R;
import com.jianqingc.nectar.httpRequest.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;


import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class InstanceFragment extends Fragment {

    View myView;
    JSONArray instanceFragmentResultArray;

    public InstanceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_instance, container, false);
        setHasOptionsMenu(true);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Instances");

        /**
         * set spinner which is actually a dialog
         */
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();
        // Inflate the layout for this fragment
        HttpRequest.getInstance(getContext()).listInstance(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */

                    instanceFragmentResultArray = new JSONArray(result);
                    ArrayList<String[]> instanceListArray = new ArrayList<String[]>();
                    for (int i = 0; i < instanceFragmentResultArray.length(); i++) {
                        String[] instanceList = {
                                instanceFragmentResultArray.getJSONObject(i).getString("instanceId"),
                                instanceFragmentResultArray.getJSONObject(i).getString("instanceName"),
                                instanceFragmentResultArray.getJSONObject(i).getString("instanceStatus"),
                                instanceFragmentResultArray.getJSONObject(i).getString("instanceUpdatedTime"),
                        };
                        instanceListArray.add(instanceList);
                    }
                    BindDictionary<String[]> dictionary = new BindDictionary<String[]>();
                    dictionary.addStringField(R.id.instanceIdTV, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] instanceList, int position) {
                            return ("ID: " + instanceList[0]);
                        }
                    });
                    dictionary.addStringField(R.id.instanceNameTV, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] instanceList, int position) {
                            return ("Name: " + instanceList[1]);
                        }
                    });
                    dictionary.addStringField(R.id.instanceStatusTV, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] instanceList, int position) {
                            return instanceList[2];
                        }
                    });
                    dictionary.addStringField(R.id.instanceUpdatedTV, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] instanceList, int position) {
                            return ("Updated at: " + instanceList[3]);
                        }
                    });

                  FunDapter adapter = new FunDapter(InstanceFragment.this.getActivity(), instanceListArray, R.layout.instance_list_pattern, dictionary);
                    ListView instanceLV = (ListView) myView.findViewById(R.id.listViewInstance);
                    adapter.notifyDataSetChanged();
                    instanceLV.setAdapter(adapter);


                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            /**
                             * Clicking on the items in the Listview will lead to Instance Detail Fragment.
                             */
                            Bundle bundle = new Bundle();
                            try {
                                String instanceId = instanceFragmentResultArray.getJSONObject(position).getString("instanceId");
                                bundle.putString("instanceId", instanceId);
                                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                                        .beginTransaction();
                                InstanceDetailFragment instanceDetailFragment = new InstanceDetailFragment();
                                instanceDetailFragment.setArguments(bundle);
                                ft.replace(R.id.relativelayout_for_fragment, instanceDetailFragment, instanceDetailFragment.getTag());
                                ft.commit();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    instanceLV.setOnItemClickListener(onListClick);
                    mOverlayDialog.dismiss();

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
                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                InstanceFragment instanceFragment = new InstanceFragment();
                ft.replace(R.id.relativelayout_for_fragment, instanceFragment, instanceFragment.getTag()).commit();

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflate) {
        // TODO Auto-generated method stub
        //super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.launch_instance).setVisible(true);

        // set the instance warning



    }
}
