package com.jianqingc.nectar.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.TimerTask;

/**
 * Created by HuangMengxue on 17/5/10.
 */

public class ManageRulesSGFragment extends Fragment{
    View myView;
    String sgID;
    JSONArray listSGRulesArray;
    String ruleID;
    Bundle bundle;




    public ManageRulesSGFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        myView = inflater.inflate(R.layout.fragment_manage_sg, container, false);
        bundle = getArguments();
        sgID = bundle.getString("securityGroupId");
        setHasOptionsMenu(true);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Security Group Detail");

        final java.util.Timer timer = new java.util.Timer(true);
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        HttpRequestController.getInstance(getActivity().getApplicationContext()).listManageRuleSG(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */

                    listSGRulesArray = new JSONArray(result);
                    ArrayList<String[]> rulesListArray = new ArrayList<String[]>();

                    for (int i = 0; i < listSGRulesArray.length(); i++) {
                        String direction;

                        if(listSGRulesArray.getJSONObject(i).getString("ruleDirection").equals("egress")){
                            direction="Egress";
                        }else{
                            direction="Ingress";
                        }
                        String ipP;
                        if(listSGRulesArray.getJSONObject(i).getString("ruleProtocol").equals("null")){
                            ipP="Any";
                        }else{
                            ipP=listSGRulesArray.getJSONObject(i).getString("ruleProtocol");
                        }
                         String portRange;
                        if(listSGRulesArray.getJSONObject(i).getString("rulePortMin").equals("null")&&
                                listSGRulesArray.getJSONObject(i).getString("rulePortMax").equals("null")){
                            portRange="Any";
                        }else{
                            if(listSGRulesArray.getJSONObject(i).getString("rulePortMin").equals(listSGRulesArray.getJSONObject(i).getString("rulePortMax"))){
                                portRange=listSGRulesArray.getJSONObject(i).getString("rulePortMin");
                            }else{
                                portRange=listSGRulesArray.getJSONObject(i).getString("rulePortMin")+" - "+listSGRulesArray.getJSONObject(i).getString("rulePortMax");
                            }
                        }
                        String remoteIPP;
                        if(listSGRulesArray.getJSONObject(i).getString("ruleRemoteIP").equals("null")){
                            remoteIPP="";
                        }else{
                            remoteIPP=listSGRulesArray.getJSONObject(i).getString("ruleRemoteIP");
                        }
                        String remoteSG;
                        if(listSGRulesArray.getJSONObject(i).getString("ruleRemoteG").equals("null")){
                            remoteSG="";
                        }else{
                            remoteSG=listSGRulesArray.getJSONObject(i).getString("ruleRemoteG");
                        }

                        String[] instanceList = {
                                direction,
                                listSGRulesArray.getJSONObject(i).getString("ruleEtherType"),
                                ipP,
                                portRange,
                                remoteIPP,
                                remoteSG
                        };
                        rulesListArray.add(instanceList);
                    }
                    System.out.println(rulesListArray);
                    BindDictionary<String[]> dictionary = new BindDictionary<String[]>();
                    dictionary.addStringField(R.id.sgDirctionMSG, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] instanceList, int position) {
                            return (instanceList[0]);
                        }
                    });
                    dictionary.addStringField(R.id.sgETMSG, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] instanceList, int position) {
                            return (instanceList[1]);
                        }
                    });
                    dictionary.addStringField(R.id.sgIPPMSG, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] instanceList, int position) {
                            return (instanceList[2]);
                        }
                    });
                    dictionary.addStringField(R.id.sgPRMSG, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] instanceList, int position) {
                            return (instanceList[3]);
                        }
                    });
                    dictionary.addStringField(R.id.sgRIPPMSG, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] instanceList, int position) {
                            return (instanceList[4]);
                        }
                    });
                    dictionary.addStringField(R.id.sgrsgMSG, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] instanceList, int position) {
                            return (instanceList[5]);
                        }
                    });



                    FunDapter adapter = new FunDapter(ManageRulesSGFragment.this.getActivity(), rulesListArray, R.layout.securitygroup_manage_pattern, dictionary);
                    ListView mrLV = (ListView) myView.findViewById(R.id.listViewManageSecurityGroup);
                    adapter.notifyDataSetChanged();
                    mrLV.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(mrLV);

                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            /**
                             * Clicking on the items in the Listview will lead to Instance Detail Fragment.
                             */
                                try {
                                    ruleID = listSGRulesArray.getJSONObject(position).getString("ruleID");


                                    final Dialog dialogLI = new Dialog(getActivity(), R.style.ActionSheetDialogStyle);
                                    View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.launch_sg_mr_dialog, null);
                                    TextView deleteRule = (TextView) inflate.findViewById(R.id.deleteRuleSGD);

                                    deleteRule.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setMessage("Delete this rule?")
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            mOverlayDialog.show();
                                                            HttpRequestController.getInstance(getActivity().getApplicationContext()).deleteRuleSG(new HttpRequestController.VolleyCallback() {
                                                                @Override
                                                                public void onSuccess(String result) {
                                                                    if(result.equals("success")) {
                                                                        Toast.makeText(getActivity().getApplicationContext(), "Delete the rule Successfully", Toast.LENGTH_SHORT).show();
                                                                        TimerTask task = new TimerTask() {
                                                                            @Override
                                                                            public void run() {
                                                                                mOverlayDialog.dismiss();
                                                                                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                                                                                        .beginTransaction();
                                                                                ManageRulesSGFragment mrFragment = new ManageRulesSGFragment();
                                                                                mrFragment.setArguments(bundle);
                                                                                ft.replace(R.id.relativelayout_for_fragment, mrFragment, mrFragment.getTag()).commit();
                                                                            }
                                                                        };
                                                                        /**
                                                                         * Delay 7 secs after the button onclick method is called.
                                                                         * Wait for server status update. The server status is not modified in real-time.
                                                                         */
                                                                        timer.schedule(task, 4000);
                                                                    } else{
                                                                        mOverlayDialog.dismiss();
                                                                        Toast.makeText(getActivity().getApplicationContext(), "Fail to delete this rule", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            }, ruleID);
                                                        }
                                                    })
                                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    }).show();
                                            dialogLI.dismiss();

                                        }
                                    });


                                    //Set the view to Dialog
                                    dialogLI.setContentView(inflate);
                                    Window dialogWindow = dialogLI.getWindow();
                                    dialogWindow.setGravity(Gravity.BOTTOM);
                                    //Get the attributes of teh window
                                    WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                                    lp.y = 20;//Set the distance of the dialog to the bottom
                                    //Set the attribute to  the dialog
                                    dialogWindow.setAttributes(lp);
                                    dialogLI.show();//Show the dialog
                                }catch (JSONException e) {
                                    e.printStackTrace();
                                }

                        }
                    };
                    mrLV.setOnItemClickListener(onListClick);
                    mOverlayDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, getActivity().getApplicationContext(), sgID);



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
                ManageRulesSGFragment mrFragment = new ManageRulesSGFragment();
                mrFragment.setArguments(bundle);
                ft.replace(R.id.relativelayout_for_fragment, mrFragment, mrFragment.getTag()).commit();
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
                AccessAndSecurityFragment sgFragment = new AccessAndSecurityFragment();
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, sgFragment, sgFragment.getTag()).commit();
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflate) {
        // TODO Auto-generated method stub
        //super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.add_rule_sg).setVisible(true);

    }




}
