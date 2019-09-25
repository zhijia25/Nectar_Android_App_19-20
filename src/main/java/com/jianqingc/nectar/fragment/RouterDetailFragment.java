package com.jianqingc.nectar.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import java.text.DecimalFormat;

import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.

 * create an instance of this fragment.
 */
public class RouterDetailFragment extends Fragment {
    Bundle bundle;
    View myView;
    String routerID;
    String routerName;
    String projectID;
    String status;
    String adminState;
    String networkName;
    String networkID;
    String subnetID;
    String ipAddress;
    String snat;

    public RouterDetailFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_router_detail,container,false);
        bundle = getArguments();

        routerID = bundle.getString("routerID");
        routerName = bundle.getString("routerName");
        projectID = bundle.getString("projectID");
        status = bundle.getString("status");
        adminState = bundle.getString("adminState");
        networkName = bundle.getString("networkName");
        networkID = bundle.getString("networkID");
        subnetID = bundle.getString("subnetID");
        ipAddress = bundle.getString("ipaddress");
        snat = bundle.getString("snat");

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Router Detail");

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabLeft.setVisibility(View.VISIBLE);
        fabLeft.setEnabled(true);
        fabLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                RouterFragment routerFragment = new RouterFragment();
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, routerFragment, routerFragment.getTag()).commit();
            }
        });

        TextView nameTV = (TextView) myView.findViewById(R.id.routerNameID);
        TextView routerIDTV = (TextView) myView.findViewById(R.id.routerID);
        TextView routerProjectIDTV =(TextView) myView.findViewById(R.id.routerProjectID);
        TextView statusTV = (TextView) myView.findViewById(R.id.routerStautsID);
        TextView adminStateTV = (TextView) myView. findViewById(R.id.routerAdminStateID);
        TextView networkNameTV = (TextView) myView.findViewById(R.id.routernetworkNamedID);
        TextView networkIDTV = (TextView) myView. findViewById(R.id.routerNetworkID);
        TextView subnetIDTV = (TextView) myView. findViewById(R.id.routerSubnetID);
        TextView ipAddressTV = (TextView) myView.findViewById(R.id.routerIPAddressID);
        TextView snatTV = (TextView) myView.findViewById(R.id.routerSnatID);

        nameTV.setText(routerName);
        routerIDTV.setText(routerID);
        routerProjectIDTV.setText(projectID);
        statusTV.setText(status);
        adminStateTV.setText(adminState);
        networkNameTV.setText(networkName);
        networkIDTV.setText(networkID);
        subnetIDTV.setText(subnetID);
        ipAddressTV.setText(ipAddress);
        snatTV.setText(snat);
        mOverlayDialog.dismiss();

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
}
