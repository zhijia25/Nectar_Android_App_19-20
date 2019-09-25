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
 */
public class ImageFragment extends Fragment {
    View myView;
    ArrayList<String[]> imageListArray;
    JSONArray listImageResultArrayP;
    Bundle bundle;
    String imageID;
    String imageType;
    String owner;

    public ImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_images, container, false);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Images");

        final java.util.Timer timer = new java.util.Timer(true);
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();
        final DecimalFormat df= new DecimalFormat("######0.00");
        //List Images
        HttpRequestController.getInstance(getContext()).listImageProject(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    /**
                     * Display instance Info with the Listview and Fundapter
                     * You can also use simple ArrayAdapter to replace Fundatper.
                     */
                    listImageResultArrayP = new JSONArray(result);
                    imageListArray = new ArrayList<String[]>();
                    Log.d(TAG, "iiiiiiiiiiiiii");
                    for (int i = 0; i < listImageResultArrayP.length(); i++) {
                        String status=listImageResultArrayP.getJSONObject(i).getString("imageStatus");
                        String first = status.substring(0, 1).toUpperCase();
                        String rest = status.substring(1, status.length());
                        String newStatus = new StringBuffer(first).append(rest).toString();
                        String type=listImageResultArrayP.getJSONObject(i).getString("imageType");
                        String first2 = type.substring(0, 1).toUpperCase();
                        String rest2 = type.substring(1, type.length());
                        String newType = new StringBuffer(first2).append(rest2).toString();

                        Double size=Integer.parseInt(listImageResultArrayP.getJSONObject(i).getString("imageSize"))/1024.0/1024.0;

                        String[] instanceList = {
                                listImageResultArrayP.getJSONObject(i).getString("imageName"),
                                newStatus,
                                df.format(size)+" M",
                                newType,
                                listImageResultArrayP.getJSONObject(i).getString("imageId"),
                                listImageResultArrayP.getJSONObject(i).getString("imageOwner")
                        };

                        imageListArray.add(instanceList);
                    }

                    System.out.println(imageListArray);
                    HttpRequestController.getInstance(getContext()).listImageOfficial(new HttpRequestController.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            try {
                                /**
                                 * Display instance Info with the Listview and Fundapter
                                 * You can also use simple ArrayAdapter to replace Fundatper.
                                 */
                                listImageResultArrayP = new JSONArray(result);
                                for (int i = 0; i < listImageResultArrayP.length(); i++) {
                                    String status=listImageResultArrayP.getJSONObject(i).getString("imageStatus");
                                    String first = status.substring(0, 1).toUpperCase();
                                    String rest = status.substring(1, status.length());
                                    String newStatus = new StringBuffer(first).append(rest).toString();
                                    Double size=Integer.parseInt(listImageResultArrayP.getJSONObject(i).getString("imageSize"))/1024.0/1024.0;
                                    String[] instanceList = {
                                            listImageResultArrayP.getJSONObject(i).getString("imageName"),
                                            newStatus,
                                            df.format(size)+" M",
                                            "Image",
                                            listImageResultArrayP.getJSONObject(i).getString("imageId"),
                                            listImageResultArrayP.getJSONObject(i).getString("imageOwner")
                                    };
                                    System.out.println(listImageResultArrayP.length());
                                    imageListArray.add(instanceList);
                                }

                                BindDictionary<String[]> dictionary = new BindDictionary<String[]>();

                                dictionary.addStringField(R.id.imageNameLI, new StringExtractor<String[]>() {
                                    @Override
                                    public String getStringValue(String[] instanceList, int position) {
                                        return (instanceList[0]);
                                    }
                                });

                                dictionary.addStringField(R.id.imageStatusLI, new StringExtractor<String[]>() {
                                    @Override
                                    public String getStringValue(String[] instanceList, int position) {
                                        return (instanceList[1]);
                                    }
                                });
                                dictionary.addStringField(R.id.imageSizeLI, new StringExtractor<String[]>() {
                                    @Override
                                    public String getStringValue(String[] instanceList, int position) {
                                        return (instanceList[2]);
                                    }
                                });
                                dictionary.addStringField(R.id.imageTypeLI, new StringExtractor<String[]>() {
                                    @Override
                                    public String getStringValue(String[] instanceList, int position) {
                                        return (instanceList[3]);
                                    }
                                });



                                FunDapter adapter = new FunDapter(ImageFragment.this.getActivity(), imageListArray, R.layout.image_list_pattern, dictionary);
                                ListView imageLV = (ListView) myView.findViewById(R.id.listViewImage);
                                adapter.notifyDataSetChanged();
                                imageLV.setAdapter(adapter);
                                setListViewHeightBasedOnChildren(imageLV);

                                AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        /**
                                         * Clicking on the items in the Listview will lead to Instance Detail Fragment.
                                         */
                                        bundle = new Bundle();

                                            imageID = imageListArray.get(position)[4];
                                            imageType= imageListArray.get(position)[3];
                                            owner=imageListArray.get(position)[5];
                                            bundle.putString("ImageID", imageID);
                                            bundle.putString("ImageType", imageType);

                                            final Dialog dialogLI = new Dialog(getActivity(),R.style.ActionSheetDialogStyle);
                                            View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.launch_image_dialog, null);

                                            TextView viewDetailImage = (TextView) inflate.findViewById(R.id.viewImageDetail);
                                            TextView deleteImage = (TextView) inflate.findViewById(R.id.deleteImage);

                                            viewDetailImage.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    FragmentTransaction ft = getActivity().getSupportFragmentManager()
                                                            .beginTransaction();
                                                    ImageDetailFragment imageDetailFragment = new ImageDetailFragment();
                                                    imageDetailFragment.setArguments(bundle);
                                                    ft.replace(R.id.relativelayout_for_fragment, imageDetailFragment, imageDetailFragment.getTag());
                                                    ft.commit();
                                                    dialogLI.dismiss();

                                                }
                                            });

                                            deleteImage.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    SharedPreferences sharedPreferences = getContext().getApplicationContext().getSharedPreferences("nectar_android", 0);
                                                    String projectID = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
                                                    if(!owner.equals(projectID)){
                                                        Toast.makeText(getActivity().getApplicationContext(), "You do not have right to delete it!", Toast.LENGTH_SHORT).show();
                                                        dialogLI.dismiss();
                                                    }else{
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                        builder.setMessage("Delete this image?")
                                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        mOverlayDialog.show();
                                                                        HttpRequestController.getInstance(getActivity().getApplicationContext()).deleteImage(new HttpRequestController.VolleyCallback() {
                                                                            @Override
                                                                            public void onSuccess(String result) {
                                                                                if(result.equals("success")) {
                                                                                    Toast.makeText(getActivity().getApplicationContext(), "Delete the image Succeed", Toast.LENGTH_SHORT).show();
                                                                                    TimerTask task = new TimerTask() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            mOverlayDialog.dismiss();
                                                                                            FragmentManager manager = getFragmentManager();
                                                                                            ImageFragment imFragment = new ImageFragment();
                                                                                            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, imFragment, imFragment.getTag()).commit();
                                                                                        }
                                                                                    };
                                                                                    /**
                                                                                     * Delay 7 secs after the button onclick method is called.
                                                                                     * Wait for server status update. The server status is not modified in real-time.
                                                                                     */
                                                                                    timer.schedule(task, 4000);
                                                                                } else{
                                                                                    mOverlayDialog.dismiss();
                                                                                    Toast.makeText(getActivity().getApplicationContext(), "Fail to delete this image", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        }, imageID);
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

                                    }
                                };
                                imageLV.setOnItemClickListener(onListClick);
                                mOverlayDialog.dismiss();



                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, getActivity());



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, getActivity());


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
                ImageFragment imFragment = new ImageFragment();

                ft.replace(R.id.relativelayout_for_fragment, imFragment, imFragment.getTag()).commit();
                //System.out.println("hihihiheeeee");

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
