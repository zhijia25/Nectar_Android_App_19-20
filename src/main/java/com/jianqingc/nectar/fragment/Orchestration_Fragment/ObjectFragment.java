package com.jianqingc.nectar.fragment.Orchestration_Fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import com.jianqingc.nectar.httpRequest.HttpRequest;
import com.jianqingc.nectar.fragment.Object_Fragment.ContainerFragment;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.TimerTask;


/**
 * create an instance of this fragment.
 */
public class ObjectFragment extends Fragment {
    View myView;
    ArrayList<String[]> objectListArray;
    JSONArray listObjectArrayP;
    Bundle bundle;
    Bundle bundle2;
    Bundle bundle3;
    Bundle bundle4;
    String containerName;
    String objectName;
    String modifiedTime;
    String next_containName;


    public ObjectFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_object, container, false);
        bundle = getArguments();
        containerName = bundle.getString("containerName");
        System.out.println("bundle:"+ bundle.toString());



        FloatingActionButton fabLeft = (FloatingActionButton) getActivity().findViewById(R.id.fabLeft);
        fabLeft.setVisibility(View.VISIBLE);
        fabLeft.setEnabled(true);
        fabLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                ContainerFragment containerFragment = new ContainerFragment();
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, containerFragment, containerFragment.getTag()).commit();
            }
        });

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Object");

        final java.util.Timer timer = new java.util.Timer(true);
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel);
        final Button create = (Button)myView.findViewById(R.id.createObjectButton);
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();

        HttpRequest.getInstance(getContext()).listObject(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    listObjectArrayP = new JSONArray(result);
                    objectListArray = new ArrayList<String[]>();
                    for (int i = 0; i < listObjectArrayP.length(); i++) {

                        objectName = listObjectArrayP.getJSONObject(i).getString("objectName");
                        System.out.println("objectName: " + objectName);
                        modifiedTime = listObjectArrayP.getJSONObject(i).getString("last_modified");
                        //String subObjectName = objectName.substring(0,objectName.length()-1);
                        String subModifiedTime = modifiedTime.substring(0, 10);

                        String[] objectList = {
                                objectName,
                                subModifiedTime

                        };


                        objectListArray.add(objectList);

                    }

                    BindDictionary<String[]> dictionary = new BindDictionary<String[]>();
                    dictionary.addStringField(R.id.ObjectNameLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[0];
                        }
                    });

                    dictionary.addStringField(R.id.ObjectModifiedTimeLI, new StringExtractor<String[]>() {
                        @Override
                        public String getStringValue(String[] item, int position) {
                            return item[1];
                        }
                    });

                    FunDapter adapter = new FunDapter(ObjectFragment.this.getActivity(), objectListArray, R.layout.object_list_pattern, dictionary);
                    ListView objectLV = (ListView) myView.findViewById(R.id.listViewObject);
                    adapter.notifyDataSetChanged();
                    objectLV.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(objectLV);
                    AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener(){
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            bundle2 = new Bundle();
                            objectName = objectListArray.get(position)[0];
                            bundle2.putString("objectName", objectName);
                            bundle2.putString("containerName_next",containerName);
                            final Dialog dialogLO = new Dialog(getActivity(), R.style.ActionSheetDialogStyle);
                            View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.launch_folder_dialog, null);

//                            TextView copyObject = (TextView) inflate.findViewById(R.id.copyObject);
                            TextView deleteObject = (TextView) inflate.findViewById(R.id.deleteObject);
//
//                            copyObject.setOnClickListener(new View.OnClickListener(){
//                                @Override
//                                public void onClick(View view) {
//                                    mOverlayDialog.dismiss();
//                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//
//                                    CopyContainerFragment ccf = new CopyContainerFragment();
//                                    bundle3 = new Bundle();
//                                    bundle3.putString("containerName",containerName);
//                                    bundle3.putString("objectName", objectName);
//                                    dialogLO.dismiss();
//                                    ccf.setArguments(bundle3);
//                                    ft.replace(R.id.relativelayout_for_fragment, ccf, ccf.getTag()).commit();
//
//
//                                }
//                            });

                            /*
                            viewObject.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                    ObjectFragment objectFragment = new ObjectFragment();
                                    bundle3 = new Bundle();
                                    containerName = containerName + objectName;
                                    System.out.println("lll: "+containerName);
                                    bundle3.putString("containerName",containerName);
                                    objectFragment.setArguments(bundle3);
                                    ft.replace(R.id.relativelayout_for_fragment, objectFragment, objectFragment.getTag()).commit();
                                }
                            });
                            */

                            deleteObject.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //SharedPreferences sharedPreferences = getContext().getApplicationContext().getSharedPreferences("nectar_android",0);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Delete this object?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            mOverlayDialog.show();
                                            HttpRequest.getInstance(getActivity().getApplicationContext()).deleteObject(new HttpRequest.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    if (result.equals("success")) {
                                                        Toast.makeText(getActivity().getApplicationContext(), "Delete the  object Succeed", Toast.LENGTH_SHORT).show();
                                                        TimerTask task = new TimerTask() {
                                                            @Override
                                                            public void run() {
                                                                mOverlayDialog.dismiss();
                                                                FragmentManager manager = getFragmentManager();
                                                                ObjectFragment objectFragment = new ObjectFragment();
                                                                bundle3 = new Bundle();
                                                                bundle3.putString("containerName",containerName);
                                                                objectFragment.setArguments(bundle3);
                                                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, objectFragment, objectFragment.getTag()).commit();

                                                            }
                                                        };
                                                        timer.schedule(task, 4000);
                                                    } else {
                                                        mOverlayDialog.dismiss();
                                                        Toast.makeText(getActivity().getApplicationContext(), "Fail to delete this object", Toast.LENGTH_SHORT).show();

                                                    }
                                                }
                                            }, containerName, objectName);
                                        }
                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();
                                    dialogLO.dismiss();
                                }
                            });

                            dialogLO.setContentView(inflate);
                            Window dialogWindow = dialogLO.getWindow();
                            dialogWindow.setGravity(Gravity.BOTTOM);
                            //get the attributes of the window

                            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                            lp.y = 20;
                            dialogWindow.setAttributes(lp);
                            dialogLO.show();


                        }
                    };
                    objectLV.setOnItemClickListener(onListClick);
                    mOverlayDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, getActivity(), containerName);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialogLO = new Dialog(getActivity(), R.style.ActionSheetDialogStyle);
//                View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.launch_create_object_dialog, null);

//                TextView createFolder = (TextView) inflate.findViewById(R.id.createObjectFolder);
//                TextView uploadObject = (TextView) inflate.findViewById(R.id.uploadObject);
                //System.out.println("click");

//                createFolder.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        dialogLO.dismiss();
//                        FragmentManager manager = getFragmentManager();
//                        CreateFolderFragment cff = new CreateFolderFragment();
//                        bundle4 = new Bundle();
//                        bundle4.putString("containerName",containerName);
//                        cff.setArguments(bundle4);
//                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, cff, cff.getTag()).commit();
//
//
//                    }
//                });

//                uploadObject.setOnClickListener(new View.OnClickListener(){
//                    @Override
//                    public void onClick(View view) {
                        dialogLO.dismiss();
                        FragmentManager manager = getFragmentManager();
                        CreateObjectFragment cof = new CreateObjectFragment();
                        bundle4 = new Bundle();
                        cof.setArguments(bundle4);
                        bundle4.putString("containerName",containerName);
                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, cof, cof.getTag()).commit();
//                    }
//                });



//                dialogLO.setContentView(inflate);
                Window dialogWindow = dialogLO.getWindow();
                dialogWindow.setGravity(Gravity.BOTTOM);
                //get the attributes of the window

                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                lp.y = 20;
                dialogWindow.setAttributes(lp);
//                dialogLO.show();

            }
        });



        return myView;
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
            totalHeight += listItem.getMeasuredHeight() * 1.1;
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()get the height of the divider
        // params.height can finally get the total height to display
        listView.setLayoutParams(params);
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