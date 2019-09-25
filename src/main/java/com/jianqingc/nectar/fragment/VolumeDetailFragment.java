package com.jianqingc.nectar.fragment;


import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
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

import com.jianqingc.nectar.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class VolumeDetailFragment extends Fragment {
    View myView;
    Bundle bundle;
    String volumeId;
    String name;
    String id;
    String status;
    String size;
    String description;
    String attach;
    String zone;
    String bootable;
    String encrypted;
    String createTime;

    public VolumeDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bundle = getArguments();
        volumeId = bundle.getString("volumeId");
        //Toast.makeText(getActivity().getApplicationContext(), "volumeId: "+volumeId, Toast.LENGTH_SHORT).show();
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_volume_detail, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Volume Detail");

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        HttpRequestController.getInstance(getActivity().getApplicationContext()).showVolumeDetail(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                setView(result);
                mOverlayDialog.dismiss();
            }
        }, getActivity().getApplicationContext(), volumeId);

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
                HttpRequestController.getInstance(getActivity().getApplicationContext()).showVolumeDetail(new HttpRequestController.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        setView(result);
                        mOverlayDialog.dismiss();
                    }
                }, getActivity().getApplicationContext(), volumeId);
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
                VolumeFragment vFragment = new VolumeFragment();
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
            }
        });



        return myView;
    }

    public void setView(String result){
        /**
         * Set the textviews and buttons according to the instance status.
         */

        TextView nameVD = (TextView)myView.findViewById(R.id.nameVD);
        TextView idVD = (TextView)myView.findViewById(R.id.idVD);
        TextView sizeVD = (TextView)myView.findViewById(R.id.sizeVD);
        TextView descriptionVD= (TextView)myView.findViewById(R.id.descriptionVD);
        final TextView attachVD = (TextView)myView.findViewById(R.id.attachVD);
        TextView zoneVD = (TextView)myView.findViewById(R.id.zoneVD);
        TextView bootableVD = (TextView)myView.findViewById(R.id.bootableVD);
        TextView encryptVD = (TextView)myView.findViewById(R.id.encryptVD);

        TextView createVD = (TextView)myView.findViewById(R.id.createVD);
        TextView statusVD = (TextView)myView.findViewById(R.id.statusVD);
        try {
            JSONObject JSONResult = new JSONObject(result);
            name = JSONResult.getString("vName");
            id = JSONResult.getString("vID");
            size = JSONResult.getString("vSize");
            description = JSONResult.getString("vDescription");
            zone = JSONResult.getString("vZone");
            bootable=JSONResult.getString("vBootable");
            encrypted=JSONResult.getString("vEncrypted");
            createTime=JSONResult.getString("vCreate");
            status=JSONResult.getString("vStatus");


            int iNum=JSONResult.getInt("vnumA");
            attach="";

            if(iNum==0){
                attach="None";
            }else{
                for(int i=0; i<iNum;i++){
                    String sID=JSONResult.getString("server"+i);
                    final String device=JSONResult.getString("device"+i);
                    HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            try{
                                JSONObject volumeO = new JSONObject(result);
                                String sname=volumeO.getString("name");
                                //vList.add(vname);
                                String former=attachVD.getText().toString();
                                attachVD.setText(former+"Attached to "+sname +" on "+ device+"\n");
                            }catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, getActivity().getApplicationContext(), sID);

                }

            }



        } catch (JSONException e) {
            e.printStackTrace();
        }

        nameVD.setText(name);
        idVD.setText(id);
        sizeVD.setText(size+" G");
        if(description.length()==0){
            description="null";
        }
        descriptionVD.setText(description);
        zoneVD.setText(zone);
        String boot=bootable;
        String first = boot.substring(0, 1).toUpperCase();
        String rest = boot.substring(1, boot.length());
        String newboot = new StringBuffer(first).append(rest).toString();
        bootableVD.setText(newboot);
        String encrpt=encrypted;
        String first2 = encrpt.substring(0, 1).toUpperCase();
        String rest2 = encrpt.substring(1, encrpt.length());
        String newencrpt = new StringBuffer(first2).append(rest2).toString();
        encryptVD.setText(newencrpt);
        createVD.setText(createTime);
        String stat=status;
        String first3 = stat.substring(0, 1).toUpperCase();
        String rest3 = stat.substring(1, stat.length());
        String newstat = new StringBuffer(first3).append(rest3).toString();
        statusVD.setText(newstat);
        attachVD.setText(attach);





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
