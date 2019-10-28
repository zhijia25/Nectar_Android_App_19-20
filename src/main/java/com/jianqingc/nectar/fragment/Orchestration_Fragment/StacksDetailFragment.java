package com.jianqingc.nectar.fragment.Orchestration_Fragment;


import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jianqingc.nectar.R;
import com.jianqingc.nectar.httpRequest.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class StacksDetailFragment extends Fragment {
    View myView;
    String instanceId;
    String stackId;
    String instanceName ;
    String stackName;
    String zone ;
    String address ;
    String instanceStatus ;
    String stackStatus;
    String created;
    String creationTime;
    String image;
    String security;
    String key;
    String volume;
    String description;
    String rollback;
    String status;
    String statusReason;


    public static StacksDetailFragment test=null;
    public String testResult=null;
    public StacksDetailFragment() {
        // Required empty public constructor
        test=this;
    }

    /**
     * Enable and disable buttons and set Buttons color.
     * @param btn
     */
    public void enable(Button btn){
        btn.setEnabled(true);
        btn.setVisibility(View.VISIBLE);
        btn.setBackgroundColor(Color.parseColor("#ffcc00"));
        if (btn.getText().equals("DELETE")){
            btn.setBackgroundColor(Color.parseColor("#FF4040"));
        }
        btn.setTextColor(Color.parseColor("#ffffff"));
    }
    public void disable(Button btn){
        btn.setEnabled(false);
        btn.setVisibility(View.VISIBLE);
        btn.setBackgroundColor(Color.parseColor("#e6e6e6"));
        btn.setTextColor(Color.parseColor("#8c8c8c"));

    }
    public void hide(Button btn){
        btn.setEnabled(false);
        btn.setVisibility(View.GONE);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_stack_detail, container, false);
        Bundle bundle = getArguments();
        stackId = bundle.getString("stackID");
        stackName = bundle.getString("stackName");
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Stack Detail");


        final Button suspendBtn = (Button)myView.findViewById(R.id.stackSuspendBtn);
        final Button resumeBtn = (Button)myView.findViewById(R.id.stackResumeBtn);
        final Button deleteBtn = (Button)myView.findViewById(R.id.stackDeleteBtn);
//        final Button rebootBtn = (Button)myView.findViewById(R.id.rebootBtn);
        final Button checkBtn = (Button)myView.findViewById(R.id.stackCheckBtn);
        final Timer timer = new Timer(true);

        /**
         * set spinner which is actually a dialog
         */
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();
        HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleStack(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                setView(result);
                //InstanceDetailFragment.test.testResult=result;
                //System.out.println(result);
                //System.out.println(test.testResult);
                mOverlayDialog.dismiss();
            }
        }, getActivity().getApplicationContext(), stackName, stackId);
        System.out.println(testResult);

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
                HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleStack(new HttpRequest.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        setView(result);
                        mOverlayDialog.dismiss();
                    }
                }, getActivity().getApplicationContext(), stackName, stackId);
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
                StacksFragment stackFragment = new StacksFragment();
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, stackFragment, stackFragment.getTag()).commit();
            }
        });
        /**


         * set Suspend button onclick
         */
        suspendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Suspend this stack")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOverlayDialog.show();
                                HttpRequest.getInstance(getActivity().getApplicationContext()).suspendStack(new HttpRequest.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if(result.equals("success")) {
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleStack(new HttpRequest.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            setView(result);
                                                            mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Suspend StackSucceed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }, getActivity().getApplicationContext(), stackName, stackId);
                                                }
                                            };
                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 7000);
                                        } else{
                                            HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleStack(new HttpRequest.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    setView(result);
                                                    mOverlayDialog.dismiss();
                                                }
                                            }, getActivity().getApplicationContext(), stackName, stackId);
                                        }
                                    }
                                }, stackName, stackId);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
        /**
         * set Resume button onclick
         */
        resumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Resume this stack?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOverlayDialog.show();
                                HttpRequest.getInstance(getActivity().getApplicationContext()).resumeStack(new HttpRequest.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if(result.equals("success")) {
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleStack(new HttpRequest.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            setView(result);
                                                            mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Resume Stack Succeed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }, getActivity().getApplicationContext(), stackName, stackId);
                                                }
                                            };
                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 7000);
                                        } else{
                                            HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleStack(new HttpRequest.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    setView(result);
                                                    mOverlayDialog.dismiss();
                                                }
                                            }, getActivity().getApplicationContext(), stackName, stackId);
                                        }
                                    }
                                }, stackName, stackId);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

        //set check button
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Check this stack?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOverlayDialog.show();
                                HttpRequest.getInstance(getActivity().getApplicationContext()).checkStack(new HttpRequest.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if(result.equals("success")) {
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleStack(new HttpRequest.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            setView(result);
                                                            mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Check Stack Succeed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }, getActivity().getApplicationContext(), stackName, stackId);
                                                }
                                            };
                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 7000);
                                        } else{
                                            HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleStack(new HttpRequest.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    setView(result);
                                                    mOverlayDialog.dismiss();
                                                }
                                            }, getActivity().getApplicationContext(), stackName, stackId);
                                        }
                                    }
                                }, stackName, stackId);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });


        /**
         * set Delete button onclick
         */
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Delete this Stack?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOverlayDialog.show();
                                HttpRequest.getInstance(getActivity().getApplicationContext()).deleteStack(new HttpRequest.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if(result.equals("success")) {
                                            Toast.makeText(getActivity().getApplicationContext(), "Delete Stack Succeed", Toast.LENGTH_SHORT).show();
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    mOverlayDialog.dismiss();
                                                    FragmentManager manager = getFragmentManager();
                                                    StacksFragment stackFragment = new StacksFragment();
                                                    manager.beginTransaction().replace(R.id.relativelayout_for_fragment, stackFragment, stackFragment.getTag()).commit();
                                                }
                                            };
                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 4000);
                                        } else{
                                            HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleStack(new HttpRequest.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    setView(result);
                                                    mOverlayDialog.dismiss();
                                                }
                                            }, getActivity().getApplicationContext(), stackName, stackId);
                                        }
                                    }
                                }, stackName, stackId);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
        /**
         * set Snapshot button onclick
         */
//        final EditText input = new EditText(getActivity());
//        final AlertDialog.Builder builderSnapshot = new AlertDialog.Builder(getActivity());
//        final AlertDialog alertDialog = builderSnapshot.create();


        return myView;
    }

    @Override
    public void onPause() {
        /**
         *  Remove refresh and back buttons when this fragment is hiden.
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






    public void setView(String result){
        /**
         * Set the textviews and buttons according to the instance status.
         */

//        final TextView instanceImageNameTV = (TextView)myView.findViewById(R.id.instanceImageNameTV);
//        final TextView instanceVolumeTV = (TextView)myView.findViewById(R.id.instanceVolumeTV);
        try {
            JSONObject JSONResult = new JSONObject(result);
            stackId = JSONResult.getString("id");
            stackName = JSONResult.getString("name");
            creationTime = JSONResult.getString("creationTime");
            description = JSONResult.getString("description");
            rollback = JSONResult.getString("disable_rollback");
            status = JSONResult.getString("status");
            statusReason = JSONResult.getString("statusReason");
//            key=JSONResult.getString("key");
//            String imageID=JSONResult.getString("image");
//            HttpRequest.getInstance(getActivity().getApplicationContext()).showImageDetail(new HttpRequest.VolleyCallback() {
//                @Override
//                public void onSuccess(String result) {
//                    try{
//                        JSONObject imageO = new JSONObject(result);
//                        image=imageO.getString("imageName");
//                        instanceImageNameTV.setText(image);
//                    }catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, getActivity().getApplicationContext(), imageID);

//            int vNum=JSONResult.getInt("volNum");
//            volume="";

//            if(vNum==0){
//                volume="None";
//            }else{
//                for(int i=0; i<vNum;i++){
//                    String vID=JSONResult.getString("volume"+i);
//                    HttpRequest.getInstance(getActivity().getApplicationContext()).showVolumeDetail(new HttpRequest.VolleyCallback() {
//                        @Override
//                        public void onSuccess(String result) {
//                            try{
//                                JSONObject volumeO = new JSONObject(result);
//                                String vname=volumeO.getString("vName");
//                                //vList.add(vname);
//                                String former=instanceVolumeTV.getText().toString();
//                                instanceVolumeTV.setText(former+" "+vname);
//                            }catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }, getActivity().getApplicationContext(), vID);
//
//                }
//
//            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView stackIdTV = (TextView)myView.findViewById(R.id.stackIdTV);
        TextView stackNameTV= (TextView)myView.findViewById(R.id.stackNameTV);
        TextView stackCreationTimeTV = (TextView)myView.findViewById(R.id.stackCreationTimeTV);
        TextView stackDescriptionTV = (TextView)myView.findViewById(R.id.stackDescriptionTV);
        TextView stackRollbackTV = (TextView)myView.findViewById(R.id.stackRollbackTV);
        TextView stackStatusTV = (TextView)myView.findViewById(R.id.stackStatusTV);

        TextView stackReasonTV = (TextView)myView.findViewById(R.id.stackReasonTV);
//        TextView instanceKeyPairTV = (TextView)myView.findViewById(R.id.instanceKeyPairTV);

//        Button startBtn = (Button)myView.findViewById(R.id.startBtn);
//        Button stopBtn = (Button)myView.findViewById(R.id.stopBtn);
//        Button pauseBtn = (Button)myView.findViewById(R.id.pauseBtn);
//        Button unpauseBtn = (Button)myView.findViewById(R.id.unpauseBtn);
        Button suspendBtn = (Button)myView.findViewById(R.id.stackSuspendBtn);
        Button resumeBtn = (Button)myView.findViewById(R.id.stackResumeBtn);
        Button checkBtn = (Button)myView.findViewById(R.id.stackCheckBtn);
        Button deleteBtn = (Button)myView.findViewById(R.id.stackDeleteBtn);
//        Button rebootBtn = (Button)myView.findViewById(R.id.rebootBtn);
//        Button snapshotBtn = (Button)myView.findViewById(R.id.snapshotBtn);
        stackIdTV.setText(stackId);
        stackNameTV.setText(stackName);
        stackCreationTimeTV.setText(creationTime);
        stackDescriptionTV.setText(description);
        stackRollbackTV.setText(rollback);
        stackStatusTV.setText(status);
        stackReasonTV.setText(statusReason);
//


        //String created;
        //String image;
        //String security;
        //String key;
        //String volume;
        /**
         * set status text color
         */

        /**
         * Set Buttons visibility
         */




                enable(suspendBtn);
                enable(resumeBtn);
                enable(checkBtn);
                enable(deleteBtn);



    }


}
