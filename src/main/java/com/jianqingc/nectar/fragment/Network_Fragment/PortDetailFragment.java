package com.jianqingc.nectar.fragment.Network_Fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jianqingc.nectar.R;

/**
 * A simple {@link Fragment} subclass.

 * create an instance of this fragment.
 */
public class PortDetailFragment extends Fragment {
    Bundle bundle;
    Bundle bundle2;
    View myView;
    String portID;
    String portName;
    String subnetPool;
    String cidr;
    String networkID;
    String ipVersion;
    String status;
    String admin_state_up;
    String address;
    String dns_name;
    String created_at;

    public PortDetailFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_port_detail,container,false);
        bundle = getArguments();
        portID = bundle.getString("portID");
        portName = bundle.getString("portName");
        networkID = bundle.getString("networkID");
        status = bundle.getString("status");
        admin_state_up = bundle.getString("admin_state_up");
        address = bundle.getString("address");
//        dns_name = bundle.getString("dns_name");
        created_at = bundle.getString("created_at");

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Port Detail");

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
                PortFragment portFragment = new PortFragment();
                bundle2 = new Bundle();
                bundle2.putString("networkID",networkID);
                portFragment.setArguments(bundle2);
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, portFragment, portFragment.getTag()).commit();
            }
        });

        TextView portIDTV = (TextView) myView.findViewById(R.id.portID);
        TextView nameTV = (TextView) myView.findViewById(R.id.portNameID);
        TextView netowrkIDTV = (TextView) myView.findViewById(R.id.portNetworkID);
        TextView statusTV = (TextView) myView.findViewById(R.id.portStatus);
        TextView adminStateTV = (TextView) myView.findViewById(R.id.portAdminState);
        TextView addressTV = (TextView) myView.findViewById(R.id.portMacAddressID);
//        TextView dnsNameTV = (TextView) myView.findViewById(R.id.portDnsName);
        TextView createdTV = (TextView) myView.findViewById(R.id.portCreated);

        nameTV.setText(portName);
        portIDTV.setText(portID);
        netowrkIDTV.setText(networkID);
        statusTV.setText(status);
        adminStateTV.setText(admin_state_up);
        addressTV.setText(address);
//        dnsNameTV.setText(dns_name);
        createdTV.setText(created_at);



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