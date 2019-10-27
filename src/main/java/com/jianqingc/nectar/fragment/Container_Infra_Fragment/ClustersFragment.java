package com.jianqingc.nectar.fragment.Container_Infra_Fragment;

import android.app.Dialog;
import android.content.Context;
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
import android.support.v7.widget.RecyclerView;
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
import com.jianqingc.nectar.controller.HttpRequestController;
import com.jianqingc.nectar.fragment.Compute_Fragment.InstanceDetailFragment;
import com.jianqingc.nectar.fragment.Compute_Fragment.InstanceFragment;
import com.jianqingc.nectar.fragment.Network_Fragment.RouterDetailFragment;
import com.jianqingc.nectar.fragment.Network_Fragment.RouterFragment;
import com.tuesda.walker.circlerefresh.CircleRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

public class ClustersFragment extends Fragment {

    View myView;
    ArrayList<String[]> clusterListArray;
    JSONArray listClusterResultArray;
    private CircleRefreshLayout mRefreshLayout;
    BindDictionary<String[]> dictionary;
    Bundle bundle;
    String clusterName;
    String status;
    String editClusterName;
    String newEditClusterName;


    public ClustersFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.simple_list_view, container , false);
        setHasOptionsMenu(true);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Cluster");

        final java.util.Timer timer = new java.util.Timer(true);
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();


        HttpRequestController.getInstance(getContext()).listCluster(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    listClusterResultArray = new JSONArray(result);
                    clusterListArray = new ArrayList<String[]>();
                    for (int i = 0; i < listClusterResultArray.length(); i++) {

                        String name = listClusterResultArray.getJSONObject(i).getString("clusterName");
                        String status = listClusterResultArray.getJSONObject(i).getString("status");
                        String UUID = listClusterResultArray.getJSONObject(i).getString("clusterID");

                        String[] clusters = {
                                name,
                                status,
                                UUID
                        };
                        clusterListArray.add(clusters);
                    }

                    dictionary = new BindDictionary<String[]>();
                    dictionary.addStringField(R.id.clusterNameLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[0];
                        }
                    });
                    dictionary.addStringField(R.id.clusterStatusLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[1];
                        }
                    });
                    dictionary.addStringField(R.id.clusterUUIDLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[2];
                        }
                    });

                    FunDapter adapter = new FunDapter(ClustersFragment.this.getActivity(), clusterListArray, R.layout.cluster_list_pattern, dictionary);
                    ListView clusterLV = (ListView) myView.findViewById(R.id.listView);
                    adapter.notifyDataSetChanged();
                    clusterLV.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(clusterLV);
                    mOverlayDialog.dismiss();

                    /**
                     * Set refresh/back button.
                     */

                    mRefreshLayout = (CircleRefreshLayout) getActivity().findViewById(R.id.refresh_layout);
                    mRefreshLayout.setOnRefreshListener(
                            new CircleRefreshLayout.OnCircleRefreshListener() {

                                @Override
                                public void refreshing() {
                                    // do something when refresh starts
                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                    ClustersFragment vFragment = new ClustersFragment();
                                    ft.replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
                                    Log.i(TAG,"Refresh success");
                                }

                                @Override
                                public void completeRefresh() {
                                    // do something when refresh complete
                                    //                                ft.replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
                                }
                            });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

