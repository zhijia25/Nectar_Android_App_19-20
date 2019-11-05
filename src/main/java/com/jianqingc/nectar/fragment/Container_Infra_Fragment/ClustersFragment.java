package com.jianqingc.nectar.fragment.Container_Infra_Fragment;

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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.github.ikidou.fragmentBackHandler.BackHandlerHelper;
import com.github.ikidou.fragmentBackHandler.FragmentBackHandler;
import com.jianqingc.nectar.R;

import com.jianqingc.nectar.httpRequest.HttpRequest;
import com.tuesda.walker.circlerefresh.CircleRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

public class ClustersFragment extends Fragment implements FragmentBackHandler {

    View myView;
    ArrayList<String[]> clusterListArray;
    JSONArray listClusterResultArray;
    private CircleRefreshLayout mRefreshLayout;
    BindDictionary<String[]> dictionary;
    Bundle bundle;
    String clusterID;


    public ClustersFragment() {

    }

    @Override
    public boolean onBackPressed() {
        boolean handleBackPressed = false;
        if (handleBackPressed) {
            //外理返回键
            return true;
        } else {
            // 如果不包含子Fragment
            // 或子Fragment没有外理back需求
            // 可如直接 return false;
            // 注：如果Fragment/Activity 中可以使用ViewPager 代替 this
            //
            return BackHandlerHelper.handleBackPress(this);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.simple_list_view, container , false);
        setHasOptionsMenu(true);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Cluster");

        final Timer timer = new Timer(true);
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        HttpRequest.getInstance(getContext()).listCluster(new HttpRequest.VolleyCallback() {
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

                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener(){
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            bundle = new Bundle();
                            //final String routerID = routerListArray.get(position)[3];
                            try {
                                clusterID = listClusterResultArray.getJSONObject(position).getString("clusterID");
                                bundle.putString("clusterID" , listClusterResultArray.getJSONObject(position).getString("clusterID"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            final Dialog dialogR = new Dialog(getActivity(),R.style.ActionSheetDialogStyle);
                            View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.dialog,null);
                            TextView Detail = (TextView) inflate.findViewById(R.id.Detail);
                            TextView Delete = (TextView) inflate.findViewById(R.id.Delete);

                            Detail.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                    ClusterDetailFragment clusterDetailFragment = new ClusterDetailFragment();
                                    clusterDetailFragment.setArguments(bundle);
                                    ft.replace(R.id.relativelayout_for_fragment, clusterDetailFragment, clusterDetailFragment.getTag());
                                    ft.commit();
                                    dialogR.dismiss();
                                }
                            });

                            /*
                             * delete
                             * */
                            Delete.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    SharedPreferences sharedPreferences = getContext().getApplicationContext().getSharedPreferences("nectar_android",0);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Delete this cluster?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {
                                            mOverlayDialog.show();
                                            HttpRequest.getInstance(getActivity().getApplicationContext()).deletecluster(new HttpRequest.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    if (result.equals("success")){
                                                        Toast.makeText(getActivity().getApplicationContext(),"Delete the cluster Succeed", Toast.LENGTH_SHORT).show();
                                                        TimerTask task = new TimerTask(){
                                                            @Override
                                                            public void run() {
                                                                mOverlayDialog.dismiss();
                                                                FragmentManager manager = getFragmentManager();
                                                                ClustersFragment clusterFragment = new ClustersFragment();
                                                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, clusterFragment, clusterFragment.getTag()).commit();

                                                            }
                                                        };
                                                        timer.schedule(task,4000);

                                                    } else {
                                                        mOverlayDialog.dismiss();
                                                        Toast.makeText(getActivity().getApplicationContext(),"Fail to delete this Router", Toast.LENGTH_SHORT).show();

                                                    }

                                                }
                                            },clusterID);
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

                    clusterLV.setOnItemClickListener(onListClick);


                    /**
                     * Set refresh/back button.
                     */

                    mRefreshLayout = (CircleRefreshLayout) getActivity().findViewById(R.id.refresh_layout);
                    mRefreshLayout.setOnRefreshListener(
                            new CircleRefreshLayout.OnCircleRefreshListener() {

                                @Override
                                public void refreshing() {
                                    // do something when refresh starts
                                    Log.i(TAG,"Refresh success");
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            System.out.println("hahaha");
                                            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                            ClustersFragment vFragment = new ClustersFragment();
                                            ft.replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
                                        }
                                    },2000);
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
        menu.findItem(R.id.create_cluster).setVisible(true);

    }
}

