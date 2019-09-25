package com.jianqingc.nectar.fragment;

import java.text.DecimalFormat;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
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
 * Activities that contain this fragment must implement the
 * create an instance of this fragment.
 */
public class ContainerFragment extends Fragment {

    View myView;
    ArrayList<String[]> containerListArray;
    JSONArray listContainerArrayP;
    Bundle bundle;
    String containerName;
    String containerCount;
    String containerByte;

    public ContainerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    // TODO: Rename and change types and number of parameters

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_container, container, false);
        setHasOptionsMenu(true);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Container");

        final java.util.Timer timer = new java.util.Timer(true);
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();
        final DecimalFormat df = new DecimalFormat("######0.00");

        HttpRequestController.getInstance(getContext()).listContainer(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    listContainerArrayP = new JSONArray(result);
                    containerListArray = new ArrayList<String[]>();

                    for (int i=0; i < listContainerArrayP.length();i++) {
                        String name = listContainerArrayP.getJSONObject(i).getString("containerName");
                        String count = Integer.toString(listContainerArrayP.getJSONObject(i).getInt("count"));
                        String bytes = Double.toString(Double.parseDouble(df.format(listContainerArrayP.getJSONObject(i).getDouble("bytes"))));
                        String []containerList = {
                                name,
                                count,
                                bytes+"MB"
                        };

                        containerListArray.add(containerList);
                    }
                    BindDictionary<String[]> dictionary = new BindDictionary<String[]>();
                    dictionary.addStringField(R.id.containerNameLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[0];
                        }
                    });

                    dictionary.addStringField(R.id.containerCountLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[1];
                        }
                    });

                    dictionary.addStringField(R.id.containerBytesLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[2];
                        }
                    });

                    FunDapter adapter = new FunDapter(ContainerFragment.this.getActivity(), containerListArray, R.layout.container_list_pattern, dictionary);
                    ListView containerLV = (ListView) myView.findViewById(R.id.listViewcontainer);
                    adapter.notifyDataSetChanged();
                    containerLV.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(containerLV);

                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener(){
                        @Override
                        public void onItemClick(AdapterView<?> present, View view, int position, long l) {
                            bundle = new Bundle();
                            containerName = containerListArray.get(position)[0];
                            bundle.putString("containerName",containerName);

                                final Dialog dialogLC = new Dialog(getActivity(),R.style.ActionSheetDialogStyle);
                                View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.launch_container_dialog, null);
                                TextView publicContainer = (TextView) inflate.findViewById(R.id.publicContainer);
                                TextView privateContainer = (TextView) inflate.findViewById(R.id.privateContainer);
                                TextView viewFolder = (TextView) inflate.findViewById(R.id.viewContainerFolder);
                                TextView deleteContainer = (TextView) inflate.findViewById(R.id.deleteContainer);

                                publicContainer.setOnClickListener(new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage("Public this container?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                mOverlayDialog.show();
                                                HttpRequestController.getInstance(getActivity().getApplicationContext()).publicContainer(new HttpRequestController.VolleyCallback() {
                                                    @Override
                                                    public void onSuccess(String result) {
                                                        if(result.equals("success")){
                                                            Toast.makeText(getActivity().getApplicationContext(), "Public container Succeed",Toast.LENGTH_SHORT).show();
                                                            TimerTask task = new TimerTask(){
                                                                @Override
                                                                public void run() {
                                                                    mOverlayDialog.dismiss();
                                                                    FragmentManager manager = getFragmentManager();
                                                                    ContainerFragment containerFragment = new ContainerFragment();
                                                                    manager.beginTransaction().replace(R.id.relativelayout_for_fragment, containerFragment, containerFragment.getTag()).commit();
                                                                }
                                                            };
                                                            timer.schedule(task, 4000);
                                                        } else {
                                                            mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Fail to public this container", Toast.LENGTH_SHORT).show();

                                                        }
                                                    }
                                                },containerName);

                                            }
                                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int i) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                                        dialogLC.dismiss();

                                    }
                                });

                            viewFolder.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                    ObjectFragment objectFragment = new ObjectFragment();
                                    objectFragment.setArguments(bundle);
                                    ft.replace(R.id.relativelayout_for_fragment, objectFragment, objectFragment.getTag()).commit();
                                    dialogLC.dismiss();

                                }
                            });

                            privateContainer.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Private this container?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            mOverlayDialog.show();
                                            HttpRequestController.getInstance(getActivity().getApplicationContext()).privateContainer(new HttpRequestController.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    if(result.equals("success")){
                                                        Toast.makeText(getActivity().getApplicationContext(), "Private container Succeed",Toast.LENGTH_SHORT).show();
                                                        TimerTask task = new TimerTask(){
                                                            @Override
                                                            public void run() {
                                                                mOverlayDialog.dismiss();
                                                                FragmentManager manager = getFragmentManager();
                                                                ContainerFragment containerFragment = new ContainerFragment();
                                                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, containerFragment, containerFragment.getTag()).commit();
                                                            }
                                                        };
                                                        timer.schedule(task, 4000);
                                                    } else {
                                                        mOverlayDialog.dismiss();
                                                        Toast.makeText(getActivity().getApplicationContext(), "Fail to private this container", Toast.LENGTH_SHORT).show();

                                                    }
                                                }
                                            },containerName);

                                        }
                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                                    dialogLC.dismiss();

                                }
                            });

                            deleteContainer.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Delete this container?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            mOverlayDialog.show();
                                            HttpRequestController.getInstance(getActivity().getApplicationContext()).deleteContainer(new HttpRequestController.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    if(result.equals("success")){
                                                        Toast.makeText(getActivity().getApplicationContext(), "Delete container Succeed",Toast.LENGTH_SHORT).show();
                                                        TimerTask task = new TimerTask(){
                                                            @Override
                                                            public void run() {
                                                                mOverlayDialog.dismiss();
                                                                FragmentManager manager = getFragmentManager();
                                                                ContainerFragment containerFragment = new ContainerFragment();
                                                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, containerFragment, containerFragment.getTag()).commit();
                                                            }
                                                        };
                                                        timer.schedule(task, 4000);
                                                    } else {
                                                        mOverlayDialog.dismiss();
                                                        Toast.makeText(getActivity().getApplicationContext(), "Fail to delete this container", Toast.LENGTH_SHORT).show();

                                                    }
                                                }
                                            },containerName);

                                        }
                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                                    dialogLC.dismiss();

                                }
                            });

                            dialogLC.setContentView(inflate);
                            Window dialogWindow = dialogLC.getWindow();
                            dialogWindow.setGravity(Gravity.BOTTOM);
                            //get the attributes of the window

                            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                            lp.y =20;
                            dialogWindow.setAttributes(lp);
                            dialogLC.show();


                        }
                    };


                    containerLV.setOnItemClickListener(onListClick);
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
                ContainerFragment alarmFragment = new ContainerFragment();
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
        menu.findItem(R.id.create_container).setVisible(true);
    }

}
