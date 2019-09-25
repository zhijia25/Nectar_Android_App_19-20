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
 * Created by HuangMengxue on 17/5/18.
 */

public class VolumeSnapshotDetailFragment extends Fragment{
    View myView;
    Bundle bundle;
    String snapshotId;
    String name;
    String id;
    String status;
    String size;
    String description;
    String volumeName;
    String volumeZone;
    String createTime;
    String volume;

    public VolumeSnapshotDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        bundle = getArguments();
        snapshotId = bundle.getString("snapshotId");
        //Toast.makeText(getActivity().getApplicationContext(), "volumeId: "+volumeId, Toast.LENGTH_SHORT).show();
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_volume_snapshot_detail, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Volume Snapshot Detail");

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();


        HttpRequestController.getInstance(getActivity().getApplicationContext()).showVolumeSnapshotDetail(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                setView(result);
                mOverlayDialog.dismiss();
            }
        }, getActivity().getApplicationContext(), snapshotId);

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
                HttpRequestController.getInstance(getActivity().getApplicationContext()).showVolumeSnapshotDetail(new HttpRequestController.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        setView(result);
                        mOverlayDialog.dismiss();
                    }
                }, getActivity().getApplicationContext(), snapshotId);
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
                VolumeSnapshotFragment vsFragment = new VolumeSnapshotFragment();
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vsFragment, vsFragment.getTag()).commit();
            }
        });


        return myView;
    }


    public void setView(String result){
        /**
         * Set the textviews and buttons according to the instance status.
         */

        TextView nameSD = (TextView)myView.findViewById(R.id.nameSD);
        TextView idSD = (TextView)myView.findViewById(R.id.idSD);
        TextView sizeSD = (TextView)myView.findViewById(R.id.sizeSD);
        TextView descriptionSD= (TextView)myView.findViewById(R.id.descriptionSD);
        final TextView volumeNameSD = (TextView)myView.findViewById(R.id.volumeNameSD);
        final TextView zoneSD = (TextView)myView.findViewById(R.id.zoneSD);


        TextView createSD = (TextView)myView.findViewById(R.id.createSD);
        TextView statusSD = (TextView)myView.findViewById(R.id.statusSD);
        try {
            JSONObject JSONResult = new JSONObject(result);
            name = JSONResult.getString("vName");
            id = JSONResult.getString("vID");
            size = JSONResult.getString("vSize");
            description = JSONResult.getString("vDescription");
            volume = JSONResult.getString("vVolume");
            createTime=JSONResult.getString("vCreate");
            status=JSONResult.getString("vStatus");



            HttpRequestController.getInstance(getActivity().getApplicationContext()).showVolumeDetail(new HttpRequestController.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            try{
                                JSONObject volumeO = new JSONObject(result);
                                String vName=volumeO.getString("vName");
                                //vList.add(vname);
                                String vZone=volumeO.getString("vZone");
                                volumeNameSD.setText(vName);
                                zoneSD.setText(vZone);
                            }catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, getActivity().getApplicationContext(), volume);







        } catch (JSONException e) {
            e.printStackTrace();
        }

        nameSD.setText(name);
        idSD.setText(id);
        sizeSD.setText(size+" G");
        if(description.length()==0){
            description="null";
        }
        descriptionSD.setText(description);

        String stat=status;
        String first3 = stat.substring(0, 1).toUpperCase();
        String rest3 = stat.substring(1, stat.length());
        String newstat = new StringBuffer(first3).append(rest3).toString();
        statusSD.setText(newstat);
        createSD.setText(createTime);





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
