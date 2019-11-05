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

public class ClusterTemplateDetailFragment extends Fragment {


    Bundle bundle;
    View myView;
    String clusterID;
    String clusterName;
    String COE;
    String image;
    String dns;
    String keyPair;
    String createTime;

    public ClusterTemplateDetailFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bundle = getArguments();
        clusterID = bundle.getString("uuid");
        //Toast.makeText(getActivity().getApplicationContext(), "volumeId: "+volumeId, Toast.LENGTH_SHORT).show();
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_cluster_template_detail, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Cluster Template Detail");

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        HttpRequest.getInstance(getActivity().getApplicationContext()).showClusterTemplateDetail(new HttpRequest.VolleyCallback() {
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
                HttpRequest.getInstance(getActivity().getApplicationContext()).showClusterTemplateDetail(new HttpRequest.VolleyCallback() {
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
                ClusterTemplateFragment vFragment = new ClusterTemplateFragment();
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
            }
        });
        return myView;
    }

    public void setView(String result){
        /**
         * Set the textviews and buttons according to the instance status.
         */
        TextView VclusterName = (TextView)myView.findViewById(R.id.clusterTemplateNameID);
        TextView VclusterID = (TextView)myView.findViewById(R.id.clusterTemplateID);
        TextView VCOE = (TextView)myView.findViewById(R.id.clusterTemplateCOEID);
        TextView VkeyPair = (TextView)myView.findViewById(R.id.clusterTemplateKeypairURLID);
        TextView Vimage = (TextView)myView.findViewById(R.id.clusterTemplateImageID);
        TextView Vdns= (TextView)myView.findViewById(R.id.clusterTemplateDNSID);
        TextView VcreateTime = (TextView)myView.findViewById(R.id.clusterTemplateCreateTimeID);
        try {
            JSONObject JSONResult = new JSONObject(result);
            clusterName = JSONResult.getString("name");
            clusterID = JSONResult.getString("uuid");
            COE = JSONResult.getString("coe");
            keyPair = JSONResult.getString("keypair");
            dns = JSONResult.getString("dns_nameserver");
            image = JSONResult.getString("image_id");
            createTime = JSONResult.getString("create_at");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        VclusterName.setText(clusterName);
        VclusterID.setText(clusterID);
        VCOE.setText(COE);
        Vimage.setText(image);
        Vdns.setText(dns);
        VkeyPair.setText(keyPair);
        VcreateTime.setText(createTime);
    }


}
