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


/**
 * Created by HuangMengxue on 17/5/10.
 */

public class KeyPairDetailFragment extends Fragment{
    View myView;
    String kpName;
    String name;
    String fingerprint;
    String createTime;
    String publicKey;
    Bundle bundle;

    public KeyPairDetailFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_keypair_detail, container, false);
        bundle = getArguments();
        kpName = bundle.getString("keyPairName");

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Keypair Detail");


        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        HttpRequestController.getInstance(getActivity().getApplicationContext()).showKeyPairDetail(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                setView(result);
                mOverlayDialog.dismiss();
            }
        }, getActivity().getApplicationContext(), kpName);




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
                HttpRequestController.getInstance(getActivity().getApplicationContext()).showKeyPairDetail(new HttpRequestController.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        setView(result);
                        mOverlayDialog.dismiss();
                    }
                }, getActivity().getApplicationContext(), kpName);
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
                KeyPairFragment kpFragment = new KeyPairFragment();
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, kpFragment, kpFragment.getTag()).commit();
            }
        });
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

    private void setView(String result){
        /**
         * Set the textviews and buttons according to the instance status.
         */

        try {
            JSONObject JSONResult = new JSONObject(result);
            name = JSONResult.getString("kpName");
            fingerprint = JSONResult.getString("kpFingerPrint");
            createTime = JSONResult.getString("kpCreateTime");
            publicKey = JSONResult.getString("kpPublicKey");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TextView nameTV = (TextView)myView.findViewById(R.id.kpNameDKP);
        TextView fingerprintTV= (TextView)myView.findViewById(R.id.kpFingerprintDLP);
        TextView createtimeTV = (TextView)myView.findViewById(R.id.kpCreateTDKP);
        TextView publickeyTV = (TextView)myView.findViewById(R.id.kpPublicKDKP);

        nameTV.setText(name);
        fingerprintTV.setText(fingerprint);
        createtimeTV.setText(createTime);
        publickeyTV.setText(publicKey);

    }


}
