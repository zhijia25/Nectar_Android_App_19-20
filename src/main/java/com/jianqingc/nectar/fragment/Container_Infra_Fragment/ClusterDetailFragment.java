package com.jianqingc.nectar.fragment.Container_Infra_Fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jianqingc.nectar.R;
import com.jianqingc.nectar.httpRequest.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class ClusterDetailFragment extends Fragment {



    Bundle bundle;
    View myView;
    String clusterID;
    String clusterName;
    String COE;
    String discoveryURL;
    String masterCount;
    String nodeCount;
    String keyPair;
    String createTime;
    String upgradeTime;
    String status;

    public ClusterDetailFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bundle = getArguments();
        clusterID = bundle.getString("clusterID");
        //Toast.makeText(getActivity().getApplicationContext(), "volumeId: "+volumeId, Toast.LENGTH_SHORT).show();
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_cluster_detail, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Cluster Detail");

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        HttpRequest.getInstance(getActivity().getApplicationContext()).showClusterDetail(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                setView(result);
                mOverlayDialog.dismiss();
            }
        }, getActivity().getApplicationContext(), clusterID);

        /**
         * Set refresh/back button.
         */
        FloatingActionButton fabRight = (FloatingActionButton) getActivity().findViewById(R.id.fabRight);
        fabRight.setVisibility(View.VISIBLE);
        fabRight.setEnabled(true);
        fabRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOverlayDialog.show();
                HttpRequest.getInstance(getActivity().getApplicationContext()).showClusterDetail(new HttpRequest.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        setView(result);
                        mOverlayDialog.dismiss();
                    }
                }, getActivity().getApplicationContext(), clusterID);
                Snackbar.make(view, "Refreshing...", Snackbar.LENGTH_SHORT).show();
            }
        });
        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabLeft.setVisibility(View.VISIBLE);
        fabLeft.setEnabled(true);
        fabLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                ClustersFragment vFragment = new ClustersFragment();
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
            }
        });
        return myView;
    }

    public void setView(String result){
        /**
         * Set the textviews and buttons according to the instance status.
         */
        TextView VclusterName = (TextView)myView.findViewById(R.id.clusterNameID);
        TextView VclusterID = (TextView)myView.findViewById(R.id.clusterID);
        TextView VCOE = (TextView)myView.findViewById(R.id.clusterCOEID);
        TextView Vdiscoveryurl = (TextView)myView.findViewById(R.id.discoveryURL);
        TextView VmasterCount = (TextView)myView.findViewById(R.id.clusterMatserCountID);
        TextView VnodeCount = (TextView)myView.findViewById(R.id.clusterNodeCountID);
        TextView VkeyPair = (TextView)myView.findViewById(R.id.clusterKeypairID);
        TextView VcreateTime = (TextView)myView.findViewById(R.id.clusterCreateTimeID);
        TextView VupgradeTime = (TextView)myView.findViewById(R.id.clusterUpgradeTimeID);
        TextView Vstatus = (TextView)myView.findViewById(R.id.clusterStatusID);
        try {
            JSONObject JSONResult = new JSONObject(result);
            clusterName = JSONResult.getString("clusterName");
            clusterID = JSONResult.getString("clusterID");
            COE = JSONResult.getString("clusterCOE");
            discoveryURL = JSONResult.getString("discoveryURL");
            masterCount = JSONResult.getString("masterCount");
            nodeCount = JSONResult.getString("nodeCount");
            keyPair = JSONResult.getString("keyPair");
            createTime = JSONResult.getString("createTime");
            upgradeTime = JSONResult.getString("upgradeTime");
            status = JSONResult.getString("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        VclusterName.setText(clusterName);
        VclusterID.setText(clusterID);
        VCOE.setText(COE);
        Vdiscoveryurl.setText(discoveryURL);
        VmasterCount.setText(masterCount);
        VnodeCount.setText(nodeCount);
        VkeyPair.setText(keyPair);
        VcreateTime.setText(createTime);
        VupgradeTime.setText(upgradeTime);
        Vstatus.setText(status);
    }
}
