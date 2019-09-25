package com.jianqingc.nectar.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import java.text.DecimalFormat;
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
 * Created by HuangMengxue on 17/5/11.
 */

public class ImageDetailFragment extends Fragment{
    Bundle bundle;
    String imageID;
    String imageType;
    View myView;
    String name;
    String status;
    String visibility;
    String protect;
    String format;
    String size;
    String createTime;


    public ImageDetailFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_image_detail, container, false);
        bundle = getArguments();
        imageID = bundle.getString("ImageID");
        imageType = bundle.getString("ImageType");

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Image Detail");

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        HttpRequestController.getInstance(getActivity().getApplicationContext()).showImageDetail(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                setView(result);
                mOverlayDialog.dismiss();
            }
        }, getActivity().getApplicationContext(), imageID);


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
                HttpRequestController.getInstance(getActivity().getApplicationContext()).showImageDetail(new HttpRequestController.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        setView(result);
                        mOverlayDialog.dismiss();
                    }
                }, getActivity().getApplicationContext(), imageID);
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
                ImageFragment imFragment = new ImageFragment();
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, imFragment, imFragment.getTag()).commit();
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
            name = JSONResult.getString("imageName");
            status = JSONResult.getString("imageStatus");
            createTime = JSONResult.getString("imageCreatedTime");
            visibility = JSONResult.getString("imageVisibility");
            protect = JSONResult.getString("imageProtected");
            format = JSONResult.getString("imageDiskFormat");
            size = JSONResult.getString("imageSize");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TextView nameTV = (TextView)myView.findViewById(R.id.imageNameID);
        TextView typeTV= (TextView)myView.findViewById(R.id.imageTypeID);
        TextView statusTV = (TextView)myView.findViewById(R.id.imageStatusID);
        TextView publicTV = (TextView)myView.findViewById(R.id.imagePublicID);
        TextView protectedTV = (TextView)myView.findViewById(R.id.imageProtectedID);
        TextView formatTV = (TextView)myView.findViewById(R.id.imageFormatID);
        TextView sizeTV = (TextView)myView.findViewById(R.id.imageSizeID);
        TextView createTTV = (TextView)myView.findViewById(R.id.imageCTID);

        System.out.println(name);
        nameTV.setText(name);
        typeTV.setText(imageType);
        String first = status.substring(0, 1).toUpperCase();
        String rest = status.substring(1, status.length());
        String newStr = new StringBuffer(first).append(rest).toString();
        statusTV.setText(newStr);
        String bool;
        if(visibility.equals("public")){
            bool="Yes";
        }else{
            bool="No";
        }
        publicTV.setText(bool);
        String bool2;
        if(protect.equals("false")){
            bool2="No";
        }else{
            bool2="Yes";
        }
        protectedTV.setText(bool2);
        formatTV.setText(format.toUpperCase());
        DecimalFormat df= new DecimalFormat("######0.00");
        Double sizeM= Integer.parseInt(size)/1024.0/1024.0;
        sizeTV.setText(df.format(sizeM)+" M");
        createTTV.setText(createTime);

    }

}
