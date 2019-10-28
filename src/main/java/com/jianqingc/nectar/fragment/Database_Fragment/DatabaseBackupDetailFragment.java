package com.jianqingc.nectar.fragment.Database_Fragment;


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

/**
 * A simple {@link Fragment} subclass.
 */
public class DatabaseBackupDetailFragment extends Fragment {
    View myView;
    Bundle bundle;
//    String volumeId;
    String databaseBackupId;
    String name;
    String id;
    String status;
    String size;
    String description;
    String attach;
    String zone;
    String bootable;
    String database;
    String createTime;
    String datastore;
    String version ;
    String created;
    String updated;

    public DatabaseBackupDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bundle = getArguments();
        databaseBackupId = bundle.getString("databaseBackupId");
        //Toast.makeText(getActivity().getApplicationContext(), "volumeId: "+volumeId, Toast.LENGTH_SHORT).show();
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_database_backup_detail, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Backup Detail");

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        HttpRequest.getInstance(getActivity().getApplicationContext()).showDatabaseBackupDetail(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                setView(result);
                mOverlayDialog.dismiss();
            }
        }, getActivity().getApplicationContext(), databaseBackupId);

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
                HttpRequest.getInstance(getActivity().getApplicationContext()).showDatabaseBackupDetail(new HttpRequest.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        setView(result);
                        mOverlayDialog.dismiss();
                    }
                }, getActivity().getApplicationContext(), databaseBackupId);
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
                DatabaseBackupFragment vFragment = new DatabaseBackupFragment();
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
            }
        });



        return myView;
    }

    public void setView(String result){
        /**
         * Set the textviews and buttons according to the instance status.
         */

        TextView nameTV = (TextView)myView.findViewById(R.id.databaseBackupNameTV);
        TextView idTV = (TextView)myView.findViewById(R.id.databaseBackupIdTV);
        TextView descriptionTV = (TextView)myView.findViewById(R.id.databaseBackupDescriptionTV);
        TextView datastoreTV= (TextView)myView.findViewById(R.id.databaseBackupDatastoreTV);
        TextView versionTV = (TextView)myView.findViewById(R.id.databaseBackupVersionTV);
        TextView createdTV = (TextView)myView.findViewById(R.id.databaseBackupCreatedTV);
        TextView updatecTV = (TextView)myView.findViewById(R.id.databaseBackupUpdatedTV);
//        TextView encryptVD = (TextView)myView.findViewById(R.id.encryptVD);
//
        TextView databaseVD = (TextView)myView.findViewById(R.id.databaseBackupDatabaseTV);
        TextView statusVD = (TextView)myView.findViewById(R.id.databaseBackupStatusTV);
        try {
            JSONObject JSONResult = new JSONObject(result);
            name = JSONResult.getString("databaseBackupName");
            id = JSONResult.getString("databaseBackupId");
            description = JSONResult.getString("databaseBackupDescription");
            datastore = JSONResult.getString("databaseBackupDatastore");
            version = JSONResult.getString("databaseBackupVersion");
            created=JSONResult.getString("databaseBackupCreated");
            updated =JSONResult.getString("databaseBackupUpdated");
            database=JSONResult.getString("databaseBackupDatabase");
            status=JSONResult.getString("databaseBackupStatus");



        } catch (JSONException e) {
            e.printStackTrace();
        }

        nameTV.setText(name);
        idTV.setText(id);
        descriptionTV.setText(description);
        datastoreTV.setText(datastore);
        versionTV.setText(version);
        createdTV.setText(created);
        updatecTV.setText(updated);
        databaseVD.setText(database);
        statusVD.setText(status);


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
