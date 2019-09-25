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
public class NetworkDetailFragment extends Fragment {
    Bundle bundle;
    View myView;
    String networkName;
    String networkID;
    String networkStatus;
    String networkShared;
    String networkAdminState;
    String externalNetwork;
    String mtu;
    String projectID;

    public NetworkDetailFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_network_detail, container, false);
        bundle = getArguments();
        networkName = bundle.getString("networkName");
        networkID = bundle.getString("networkID");
        networkStatus = bundle.getString("networkStatus");
        networkShared = bundle.getString("networkShared");
        networkAdminState = bundle.getString("networkAdminState");
        externalNetwork = bundle.getString("externalRouter");
        mtu = bundle.getString("mtu");
        projectID= bundle.getString("projectID");

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Network Detail");

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
                NetworkFragment networkFragment = new NetworkFragment();
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, networkFragment, networkFragment.getTag()).commit();
            }
        });

        TextView nameTV = (TextView) myView.findViewById(R.id.NetworkNameID);
        TextView IDTV = (TextView) myView.findViewById(R.id.NetworkID);
        TextView projectIDTV = (TextView) myView.findViewById(R.id.NetworkProjectID);
        TextView statusTV = (TextView) myView.findViewById(R.id.NetworkStatusID);
        TextView sharedTV = (TextView) myView.findViewById(R.id.NetworkSharedID);
        TextView adminStateTV = (TextView) myView.findViewById(R.id.NetworkAdminStateID);
        TextView externalTV =(TextView) myView.findViewById(R.id.NetworkExternalID);
        TextView mtuTV = (TextView) myView.findViewById(R.id.NetworkMTUID);

        nameTV.setText(networkName);
        IDTV.setText(networkID);
        projectIDTV.setText(projectID);
        statusTV.setText(networkStatus);
        sharedTV.setText(networkShared);
        adminStateTV.setText(networkAdminState);
        externalTV.setText(externalNetwork);
        mtuTV.setText(mtu);
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