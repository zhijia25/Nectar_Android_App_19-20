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
import com.tuesda.walker.circlerefresh.CircleRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

public class ClusterTemplateFragment extends Fragment {



    View myView;
    ArrayList<String[]> clusterTemplateListArray;
    JSONArray listClusterTemplateResultArray;
    private CircleRefreshLayout mRefreshLayout;
    BindDictionary<String[]> dictionary;
    Bundle bundle;
    String UUID;

    public ClusterTemplateFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.simple_list_view, container , false);
        setHasOptionsMenu(true);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("ClusterTemplate");

        final java.util.Timer timer = new java.util.Timer(true);
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();


        HttpRequest.getInstance(getContext()).listClusterTemplate(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    listClusterTemplateResultArray = new JSONArray(result);
                    clusterTemplateListArray = new ArrayList<String[]>();
                    for (int i = 0; i < listClusterTemplateResultArray.length(); i++) {

                        String name = listClusterTemplateResultArray.getJSONObject(i).getString("name");
                        String COE = listClusterTemplateResultArray.getJSONObject(i).getString("coe");
                        String UUID = listClusterTemplateResultArray.getJSONObject(i).getString("uuid");

                        String[] templates = {
                                name,
                                COE,
                                UUID
                        };
                        clusterTemplateListArray.add(templates);
                    }

                    dictionary = new BindDictionary<String[]>();
                    dictionary.addStringField(R.id.clusterTemplateNameLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[0];
                        }
                    });
                    dictionary.addStringField(R.id.clusterTemplateCOELI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[1];
                        }
                    });
                    dictionary.addStringField(R.id.clusterTemplateUUIDLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[2];
                        }
                    });

                    FunDapter adapter = new FunDapter(ClusterTemplateFragment.this.getActivity(), clusterTemplateListArray, R.layout.cluster_template_list_pattern, dictionary);
                    ListView clusterTemplateLV = (ListView) myView.findViewById(R.id.listView);
                    adapter.notifyDataSetChanged();
                    clusterTemplateLV.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(clusterTemplateLV);
                    mOverlayDialog.dismiss();

                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener(){
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            bundle = new Bundle();
                            try {
                                UUID = listClusterTemplateResultArray.getJSONObject(position).getString("uuid");
                                bundle.putString("uuid" , listClusterTemplateResultArray.getJSONObject(position).getString("uuid"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            final Dialog dialogR = new Dialog(getActivity(),R.style.ActionSheetDialogStyle);
                            View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.dialog,null);
                            TextView Detail = (TextView) inflate.findViewById(R.id.Detail);

                            Detail.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                    ClusterTemplateDetailFragment clusterTemplateDetailFragment = new ClusterTemplateDetailFragment();
                                    clusterTemplateDetailFragment.setArguments(bundle);
                                    ft.replace(R.id.relativelayout_for_fragment, clusterTemplateDetailFragment, clusterTemplateDetailFragment.getTag());
                                    ft.commit();
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

                    clusterTemplateLV.setOnItemClickListener(onListClick);


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
                                    ClusterTemplateFragment vFragment = new ClusterTemplateFragment();
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

}
