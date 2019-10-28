package com.jianqingc.nectar.fragment.Database_Fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.jianqingc.nectar.R;
import com.jianqingc.nectar.httpRequest.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DecimalFormat;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigurationGroupInstanceFragment extends Fragment {
    View myView;
    ArrayList<String[]> testListArray;
    JSONArray listTestResultArrayP;
    Bundle bundle;
    String testID;
    String configGroupId;



    private String tenant_id;

    public ConfigurationGroupInstanceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bundle = getArguments();
        configGroupId = bundle.getString("configGroupId");
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_configuration_group_instance, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Instance");

        final java.util.Timer timer = new java.util.Timer(true);
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();
        final DecimalFormat df= new DecimalFormat("######0.00");
        //List Images
        HttpRequest.getInstance(getContext()).listConfigGroupInstances(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */
                    listTestResultArrayP = new JSONArray(result);
                    testListArray = new ArrayList<String[]>();
//                    Log.d(TAG, "iiiiiiiiiiiiii");
//                    Log.d("Result",result);nectar_android

                    for (int i = 0; i < listTestResultArrayP.length(); i++) {
                        String[] instanceList = {
                                listTestResultArrayP.getJSONObject(i).getString("configGroupInstanceId"),
                                listTestResultArrayP.getJSONObject(i).getString("configGroupInstanceName"),
                        };

                        testListArray.add(instanceList);
                    }

//                    System.out.println(testListArray);


                    BindDictionary<String[]> dictionary = new BindDictionary<String[]>();

                    dictionary.addStringField(R.id.configGroupInstanceNameLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] instanceList, int position) {
                            return (instanceList[1]);
                        }
                    });

                    FunDapter adapter = new FunDapter(ConfigurationGroupInstanceFragment.this.getActivity(), testListArray, R.layout.configuration_group_instances_list_pattern, dictionary);
                    ListView resourceTypesLV = (ListView) myView.findViewById(R.id.listViewConfigGroupInstances);
                    adapter.notifyDataSetChanged();
                    resourceTypesLV.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(resourceTypesLV);

                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            /**
                             * Clicking on the items in the Listview will lead to Instance Detail Fragment.
                             */
                            bundle = new Bundle();

                            testID = testListArray.get(position)[0];
//                            testType = testListArray.get(position)[3];
//                            owner = testListArray.get(position)[5];
                            bundle.putString("databaseInstanceID", testID);
//                            bundle.putString("ImageType", imageType);

                                    FragmentTransaction ft = getActivity().getSupportFragmentManager()
                                            .beginTransaction();
                                    DatabaseInstanceDetailFragment databaseInstanceDetailFragment = new DatabaseInstanceDetailFragment();
                                    databaseInstanceDetailFragment.setArguments(bundle);
                                    ft.replace(R.id.relativelayout_for_fragment, databaseInstanceDetailFragment, databaseInstanceDetailFragment.getTag());
                                    ft.commit();
//                                    dialogLI.dismiss();


                        }
                    };

                    resourceTypesLV.setOnItemClickListener(onListClick);
                    mOverlayDialog.dismiss();



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, getActivity().getApplicationContext(), configGroupId);


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
                ConfigurationGroupInstanceFragment imFragment = new ConfigurationGroupInstanceFragment();

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
                ConfigurationGroupFragment vFragment = new ConfigurationGroupFragment();
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();
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
