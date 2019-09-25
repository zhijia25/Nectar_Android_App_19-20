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
public class SubnetDetailFragment extends Fragment {
    Bundle bundle;
    Bundle bundle2;
    View myView;
    String subnetID;
    String subnetName;
    String subnetPool;
    String cidr;
    String networkID;
    String ipVersion;

    public SubnetDetailFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_subnet_detail,container,false);
        bundle = getArguments();
        subnetID = bundle.getString("subnetID");
        subnetName = bundle.getString("subnetName");
        networkID = bundle.getString("networkID");
        ipVersion = bundle.getString("ipVersion");
        subnetPool = bundle.getString("allocation_pools");
        cidr = bundle.getString("address");

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Subnet Detail");

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        String tempPool = subnetPool.replace(",","\n");
        tempPool = tempPool.replace("[","");
        tempPool = tempPool.replace("]","");
        tempPool = tempPool.replace("\"","");
        tempPool = tempPool.replace("{","");
        tempPool = tempPool.replace("}","");



        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabLeft.setVisibility(View.VISIBLE);
        fabLeft.setEnabled(true);
        fabLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                SubnetFragment subnetFragment = new SubnetFragment();
                bundle2 = new Bundle();
                bundle2.putString("networkID",networkID);
                subnetFragment.setArguments(bundle2);
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, subnetFragment, subnetFragment.getTag()).commit();
            }
        });

        TextView subnetIDTV = (TextView) myView.findViewById(R.id.SubnetID);
        TextView nameTV = (TextView) myView.findViewById(R.id.SubnetNameID);
        TextView netowrkIDTV = (TextView) myView.findViewById(R.id.SubnetNetworkID);
        TextView subnetPoolTV = (TextView) myView.findViewById(R.id.SubnetIPPoolID);
        TextView subnetIPVersionTV = (TextView) myView.findViewById(R.id.SubnetIPVersionID);
        TextView subnetCIDRTV = (TextView) myView.findViewById(R.id.SubnetCIDRID);

        nameTV.setText(subnetName);
        subnetIDTV.setText(subnetID);
        netowrkIDTV.setText(networkID);
        subnetPoolTV.setText(tempPool);
        subnetIPVersionTV.setText(ipVersion);
        subnetCIDRTV.setText(cidr);
        System.out.println("pool: "+ subnetPool);

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