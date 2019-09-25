package com.jianqingc.nectar.fragment;

import java.text.DecimalFormat;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.jianqingc.nectar.R;
import com.jianqingc.nectar.controller.HttpRequestController;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AlarmFragment extends Fragment {
    View myView;
    ArrayList<String[]> alarmListArray;
    JSONArray listAlarmResultArrayP;
    Bundle bundle;
    String alarmID;
    String alarmType;
    String owner;
    String resource_type;
    String comparison_operator;
    String description;





    public AlarmFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_alarm, container, false);
        setHasOptionsMenu(true);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Alarm");

        final java.util.Timer timer = new java.util.Timer(true);
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();
        //final DecimalFormat df = new DecimalFormat("######0.00");

        //List Alarm
        HttpRequestController.getInstance(getContext()).listAlarmProject(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try{
                    /**
                     *  Display instance info with the alarm view and Fundapter
                     *  you can also use simple ArrayAdapter to replace Fundapter.
                     */
                    //result list
                    listAlarmResultArrayP = new JSONArray(result);
                    //System.out.println("result:aaaaa");
                    //System.out.println(result);
                    //System.out.println(listAlarmResultArrayP);

                    alarmListArray = new ArrayList<String[]>();
                    for (int i=0; i<listAlarmResultArrayP.length();i++)
                    {
                        String name = listAlarmResultArrayP.getJSONObject(i).getString("AlarmName");
                        //System.out.println(name+" "+i);
                        String metric = listAlarmResultArrayP.getJSONObject(i).getString("metric");

                        String aggregation_method = listAlarmResultArrayP.getJSONObject(i).getString("aggregation_method");
                        int threshold = listAlarmResultArrayP.getJSONObject(i).getInt("threshold");
                        String thresholdString = Integer.toString(threshold);

                        alarmID = listAlarmResultArrayP.getJSONObject(i).getString("alarmID");
                        alarmType = listAlarmResultArrayP.getJSONObject(i).getString("type");
                        resource_type = listAlarmResultArrayP.getJSONObject(i).getString("resource_type");
                        comparison_operator = listAlarmResultArrayP.getJSONObject(i).getString("comparison_operator");
                        description = listAlarmResultArrayP.getJSONObject(i).getString("description");

                        String state = listAlarmResultArrayP.getJSONObject(i).getString("state");
                        String severity = listAlarmResultArrayP.getJSONObject(i).getString("severity");
                        String granularity = listAlarmResultArrayP.getJSONObject(i).getString("granularity").toString();




                        String [] alarmList = {
                                name,
                                metric,
                                aggregation_method,
                                thresholdString,
                                alarmID,
                                alarmType,
                                resource_type,
                                comparison_operator,
                                description,
                                state,
                                severity,
                                granularity


                        };
                        alarmListArray.add(alarmList);
                    }
                    BindDictionary<String[]> dictionary = new BindDictionary<String[]>();
                    dictionary.addStringField(R.id.alarmNameLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] alarmList, int position) {
                            return (alarmList[0]);
                        }
                    });

                    dictionary.addStringField(R.id.alarmMetricLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] alarmList, int position) {
                            return alarmList[1];
                        }
                    });

                    dictionary.addStringField(R.id.alarmMethodLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] alarmList, int position) {
                            return alarmList[2];
                        }
                    });

                    dictionary.addStringField(R.id.alarmThresholdLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] alarmList, int position) {
                            return alarmList[3];
                        }
                    });

                    FunDapter adapter = new FunDapter(AlarmFragment.this.getActivity(), alarmListArray, R.layout.alarm_list_pattern, dictionary);
                    ListView alarmLV = (ListView) myView.findViewById(R.id.listViewAlarm);
                    adapter.notifyDataSetChanged();
                    alarmLV.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(alarmLV);

                    //delete and view details
                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            /**
                             * clicking on the items in the listview will lead to Instance Detail Fragment
                             */
                            bundle = new Bundle();

                            String alarmName = alarmListArray.get(position)[0];
                            String metric = alarmListArray.get(position)[1];
                            String alarmMethod = alarmListArray.get(position)[2];
                            String threshold = alarmListArray.get(position)[3];


                            //bundle.putString("AlarmID",alarmID);
                            alarmID = alarmListArray.get(position)[4];
                            alarmType = alarmListArray.get(position)[5];
                            resource_type = alarmListArray.get(position)[6];
                            comparison_operator = alarmListArray.get(position)[7];
                            description = alarmListArray.get(position)[8];

                            String state = alarmListArray.get(position)[9];
                            String severity = alarmListArray.get(position)[10];
                            String granularity = alarmListArray.get(position)[11];

                            bundle.putString("AlarmID",alarmID);
                            //System.out.println("bundle_id: "+alarmID);
                            bundle.putString("alarmType",alarmType);
                            bundle.putString("resource_type",resource_type);
                            bundle.putString("comparison_operator",comparison_operator);
                            bundle.putString("description",description);

                            bundle.putString("alarmName",alarmName);
                            bundle.putString("alarmMetric",metric);
                            bundle.putString("alarmMethod",alarmMethod);
                            bundle.putString("alarmThreshold",threshold);

                            bundle.putString("alarmState",state);
                            bundle.putString("alarmSeverity",severity);
                            bundle.putString("alarmGranularity",granularity);

                            /**
                             * need to add some param later
                             */

                            final Dialog dialogCA = new Dialog(getActivity(),R.style.ActionSheetDialogStyle);
                            View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.launch_alarm_dialog,null);

                            TextView viewDetailAlarm = (TextView) inflate.findViewById(R.id.viewAlarmDetail);
                            TextView deleteAlarm = (TextView) inflate.findViewById(R.id.deleteAlarm);

                            viewDetailAlarm.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                    AlarmDetailFragment alarmDetailFragment = new AlarmDetailFragment();
                                    alarmDetailFragment.setArguments(bundle);
                                    ft.replace(R.id.relativelayout_for_fragment, alarmDetailFragment, alarmDetailFragment.getTag());
                                    ft.commit();
                                    dialogCA.dismiss();
                                }
                            });

                            deleteAlarm.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    SharedPreferences sharedPreferences = getContext().getApplicationContext().getSharedPreferences("nectar_android",0);
                                    String projectID = sharedPreferences.getString("tenantId","Error Getting Compute URL");
                                    //if(!owner.equals(projectID)){
                                      //  Toast.makeText(getActivity().getApplicationContext(), "You do not have right to delete it!", Toast.LENGTH_SHORT).show();
                                        //dialogCA.dismiss();

                                   // } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage("Delete this Alarm?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int i) {
                                                mOverlayDialog.show();
                                                HttpRequestController.getInstance(getActivity().getApplicationContext()).deleteAlarm(new HttpRequestController.VolleyCallback() {
                                                    @Override
                                                    public void onSuccess(String result) {
                                                        if (result.equals("success")){
                                                            Toast.makeText(getActivity().getApplicationContext(),"Delete the alarm Succeed", Toast.LENGTH_SHORT).show();
                                                            TimerTask task = new TimerTask(){
                                                                @Override
                                                                public void run() {
                                                                    mOverlayDialog.dismiss();
                                                                    FragmentManager manager = getFragmentManager();
                                                                    AlarmFragment alarmFragment = new AlarmFragment();
                                                                    manager.beginTransaction().replace(R.id.relativelayout_for_fragment, alarmFragment, alarmFragment.getTag()).commit();


                                                                }
                                                            };
                                                            timer.schedule(task,4000);

                                                        } else {
                                                            mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(),"Fail to delete this alarm", Toast.LENGTH_SHORT).show();

                                                        }

                                                    }
                                                },alarmID);
                                            }
                                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int i) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                                        dialogCA.dismiss();
                                    }
                               // }
                            });

                            //set the view to Dialog

                            dialogCA.setContentView(inflate);
                            Window dialogWindow = dialogCA.getWindow();
                            dialogWindow.setGravity(Gravity.BOTTOM);
                            //get the attributes of the window

                            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                            lp.y =20;
                            dialogWindow.setAttributes(lp);
                            dialogCA.show();



                        }
                    };
                    alarmLV.setOnItemClickListener(onListClick);
                    mOverlayDialog.dismiss();






                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        },getActivity());

        /**
         * Set refresh/back button.
         */
        FloatingActionButton fabRight = (FloatingActionButton) getActivity().findViewById(R.id.fabRight);
        fabRight.setEnabled(true);
        fabRight.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Snackbar.make(view,"Refreshing...", Snackbar.LENGTH_SHORT).show();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                AlarmFragment alarmFragment = new AlarmFragment();
                ft.replace(R.id.relativelayout_for_fragment,alarmFragment,alarmFragment.getTag()).commit();
            }
        });

        return myView;
    }

    @Override
    public void onPause() {
        /**
         * remove refresh button when this fragment is hiden.
         */
        super.onPause();
        FloatingActionButton fabRight = (FloatingActionButton) getActivity().findViewById(R.id.fabRight);
        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabRight.setVisibility(View.GONE);
        fabRight.setEnabled(false);
        fabLeft.setVisibility(View.GONE);
        Toolbar toolbar =(Toolbar) getActivity().findViewById(R.id.toolbar);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        /**
         * need to modify later
         */
        menu.findItem(R.id.create_alarm).setVisible(true);
    }


}
