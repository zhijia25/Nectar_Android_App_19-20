package com.jianqingc.nectar.fragment.Orchestration_Fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.jianqingc.nectar.R;
import com.jianqingc.nectar.httpRequest.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by HuangMengxue on 17/5/11.
 */

public class TemplateVersionsDetailFragment extends Fragment{
    Bundle bundle;
    String templateVersionsID;
    String attributes;
    View myView;
    String name;
    String status;
    String format;
    ArrayList<String[]> templateVersionsDetailListArray;
    JSONArray listTemplateVersionsDetailResultArrayP;


    public TemplateVersionsDetailFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_template_versions_detail, container, false);
        bundle = getArguments();
        templateVersionsID = bundle.getString("templateVersionsID");
//        imageType = bundle.getString("ImageType");

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Template Version Detail");

        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        HttpRequest.getInstance(getActivity().getApplicationContext()).showTemplateVersionDetail(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */
                    listTemplateVersionsDetailResultArrayP = new JSONArray(result);
                    templateVersionsDetailListArray = new ArrayList<String[]>();
                    Log.d(TAG, "iiiiiiiiiiiiii");
//                    Log.d("Result",result);

                    for (int i = 0; i < listTemplateVersionsDetailResultArrayP.length(); i++) {
                        String[] instanceList = {
                                listTemplateVersionsDetailResultArrayP.getJSONObject(i).getString("functions"),
                                listTemplateVersionsDetailResultArrayP.getJSONObject(i).getString("description"),
                        };

                        templateVersionsDetailListArray.add(instanceList);
                    }

//                    System.out.println(testListArray);


                    BindDictionary<String[]> dictionary = new BindDictionary<String[]>();

                    dictionary.addStringField(R.id.templateVersionsFunctionLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] instanceList, int position) {
                            return (instanceList[0]);
                        }
                    });

                    dictionary.addStringField(R.id.templateVersionsDescriptionLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] instanceList, int position) {
                            return (instanceList[1]);
                        }
                    });
//
                    FunDapter adapter = new FunDapter(TemplateVersionsDetailFragment.this.getActivity(), templateVersionsDetailListArray, R.layout.template_versions_detail_list_pattern, dictionary);
                    ListView templateVersionsDetailLV = (ListView) myView.findViewById(R.id.listViewTemplateVersionsDetail);
                    adapter.notifyDataSetChanged();
                    templateVersionsDetailLV.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(templateVersionsDetailLV);
//

//                    templateVersionsDetailLV.setOnItemClickListener(onListClick);
                    mOverlayDialog.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, getActivity().getApplicationContext(), templateVersionsID);


        /**
         * Set refresh/back button.
         */
        FloatingActionButton fabRight = (FloatingActionButton) getActivity().findViewById(R.id.fabRight);
        fabRight.setVisibility(View.VISIBLE);
        fabRight.setEnabled(true);
        fabRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(view, "Refreshing...", Snackbar.LENGTH_SHORT).show();
                mOverlayDialog.show();
                mOverlayDialog.dismiss();
                //reload this page
                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                TemplateVersionsDetailFragment imFragment = new TemplateVersionsDetailFragment();

                ft.replace(R.id.relativelayout_for_fragment, imFragment, imFragment.getTag()).commit();
                //System.out.println("hihihiheeeee");

            }
        });
        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabLeft.setVisibility(View.VISIBLE);
        fabLeft.setEnabled(true);
        fabLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                TemplateVersionsFragment imFragment = new TemplateVersionsFragment();
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

    public void setListViewHeightBasedOnChildren(ListView listView) {
        // Get the adapter for the list
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        // listAdapter.getCount() can get the number of the items
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {

            View listItem = listAdapter.getView(i, null, listView);
            // Calculate the height and width of a item
            listItem.measure(0, 0);
            // calculate the total height
            totalHeight += listItem.getMeasuredHeight()*1.1;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()get the height of the divider
        // params.height can finally get the total height to display
        listView.setLayoutParams(params);
    }



}
