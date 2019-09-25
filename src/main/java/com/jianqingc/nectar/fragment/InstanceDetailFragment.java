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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
import com.jianqingc.nectar.R;
import com.jianqingc.nectar.controller.HttpRequestController;
import java.util.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class InstanceDetailFragment extends Fragment {
    View myView;
    String instanceId;
    String instanceName ;
    String zone ;
    String address ;
    String instanceStatus ;
    String created;
    String image;
    String security;
    String key;
    String volume;


    public static InstanceDetailFragment test=null;
    public String testResult=null;
    public InstanceDetailFragment() {
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
        myView = inflater.inflate(R.layout.fragment_instance_detail, container, false);
        Bundle bundle = getArguments();
        instanceId = bundle.getString("instanceId");
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Instance Detail");

        final Button startBtn = (Button)myView.findViewById(R.id.startBtn);
        final Button stopBtn = (Button)myView.findViewById(R.id.stopBtn);
        final Button pauseBtn = (Button)myView.findViewById(R.id.pauseBtn);
        final Button unpauseBtn = (Button)myView.findViewById(R.id.unpauseBtn);
        final Button suspendBtn = (Button)myView.findViewById(R.id.suspendBtn);
        final Button resumeBtn = (Button)myView.findViewById(R.id.resumeBtn);
        final Button deleteBtn = (Button)myView.findViewById(R.id.deleteBtn);
        final Button rebootBtn = (Button)myView.findViewById(R.id.rebootBtn);
        final Button snapshotBtn = (Button)myView.findViewById(R.id.snapshotBtn);
        final java.util.Timer timer = new java.util.Timer(true);

        /**
         * set spinner which is actually a dialog
         */
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();
        HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                setView(result);
                //InstanceDetailFragment.test.testResult=result;
                //System.out.println(result);
                //System.out.println(test.testResult);
                mOverlayDialog.dismiss();
            }
        }, getActivity().getApplicationContext(), instanceId);
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
                HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        setView(result);
                        mOverlayDialog.dismiss();
                    }
                }, getActivity().getApplicationContext(), instanceId);
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
                InstanceFragment instanceFragment = new InstanceFragment();
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, instanceFragment, instanceFragment.getTag()).commit();
            }
        });
        /**
         * Set Pause button onclick
         */
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Pause this instance?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /**
                         * The spinner
                         */
                        mOverlayDialog.show();
                        HttpRequestController.getInstance(getActivity().getApplicationContext()).pause(new HttpRequestController.VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                if(result.equals("success")) {
                                    TimerTask task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    setView(result);
                                                    mOverlayDialog.dismiss();
                                                    Toast.makeText(getActivity().getApplicationContext(), "Pause Instance Succeed", Toast.LENGTH_SHORT).show();
                                                }
                                            }, getActivity().getApplicationContext(), instanceId);
                                        }
                                    };
                                    /**
                                     * Delay 7 secs after the button onclick method is called.
                                     * Wait for server status update. The server status is not modified in real-time.
                                     */
                                    timer.schedule(task, 7000);
                                } else{
                                    HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                        @Override
                                        public void onSuccess(String result) {
                                            setView(result);
                                            mOverlayDialog.dismiss();
                                        }
                                    }, getActivity().getApplicationContext(), instanceId);
                                }
                            }
                        }, instanceId);
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
         * set Unpause button onclick
         */
        unpauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Unpause this instance?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOverlayDialog.show();
                                HttpRequestController.getInstance(getActivity().getApplicationContext()).unpause(new HttpRequestController.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if(result.equals("success")) {
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            setView(result);
                                                            mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Unpause Instance Succeed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }, getActivity().getApplicationContext(), instanceId);
                                                }
                                            };
                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 7000);
                                        } else{
                                            HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    setView(result);
                                                    mOverlayDialog.dismiss();
                                                }
                                            }, getActivity().getApplicationContext(), instanceId);
                                        }
                                    }
                                }, instanceId);
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
         * set Stop button onclick
         */
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Stop this instance?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOverlayDialog.show();
                                HttpRequestController.getInstance(getActivity().getApplicationContext()).stop(new HttpRequestController.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if(result.equals("success")) {
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            setView(result);
                                                            //System.out.println(test.testResult);
                                                            mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Stop Instance Succeed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }, getActivity().getApplicationContext(), instanceId);
                                                }
                                            };
                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 7000);
                                        } else{
                                            HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    setView(result);
                                                    mOverlayDialog.dismiss();
                                                }
                                            }, getActivity().getApplicationContext(), instanceId);
                                        }
                                    }
                                }, instanceId);
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
         * set Start button onclick
         */
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Start this instance?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOverlayDialog.show();
                                HttpRequestController.getInstance(getActivity().getApplicationContext()).start(new HttpRequestController.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if(result.equals("success")) {
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            setView(result);
                                                            mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Start Instance Succeed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }, getActivity().getApplicationContext(), instanceId);
                                                }
                                            };
                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 7000);
                                        } else{
                                            HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    setView(result);
                                                    mOverlayDialog.dismiss();
                                                }
                                            }, getActivity().getApplicationContext(), instanceId);
                                        }
                                    }
                                }, instanceId);
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
         * set Suspend button onclick
         */
        suspendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Suspend this instance?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOverlayDialog.show();
                                HttpRequestController.getInstance(getActivity().getApplicationContext()).suspend(new HttpRequestController.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if(result.equals("success")) {
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            setView(result);
                                                            mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Suspend Instance Succeed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }, getActivity().getApplicationContext(), instanceId);
                                                }
                                            };
                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 7000);
                                        } else{
                                            HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    setView(result);
                                                    mOverlayDialog.dismiss();
                                                }
                                            }, getActivity().getApplicationContext(), instanceId);
                                        }
                                    }
                                }, instanceId);
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
                builder.setMessage("Resume this instance?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOverlayDialog.show();
                                HttpRequestController.getInstance(getActivity().getApplicationContext()).resume(new HttpRequestController.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if(result.equals("success")) {
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            setView(result);
                                                            mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Resume Instance Succeed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }, getActivity().getApplicationContext(), instanceId);
                                                }
                                            };
                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 7000);
                                        } else{
                                            HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    setView(result);
                                                    mOverlayDialog.dismiss();
                                                }
                                            }, getActivity().getApplicationContext(), instanceId);
                                        }
                                    }
                                }, instanceId);
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
         * set Reboot button onclick
         */
        rebootBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Reboot this instance?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOverlayDialog.show();
                                HttpRequestController.getInstance(getActivity().getApplicationContext()).reboot(new HttpRequestController.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if(result.equals("success")) {
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            setView(result);
                                                            mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Reboot Instance Succeed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }, getActivity().getApplicationContext(), instanceId);
                                                }
                                            };
                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 7000);
                                        } else{
                                            HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    setView(result);
                                                    mOverlayDialog.dismiss();
                                                }
                                            }, getActivity().getApplicationContext(), instanceId);
                                        }
                                    }
                                }, instanceId);
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
                builder.setMessage("Delete this instance?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOverlayDialog.show();
                                HttpRequestController.getInstance(getActivity().getApplicationContext()).delete(new HttpRequestController.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if(result.equals("success")) {
                                            Toast.makeText(getActivity().getApplicationContext(), "Delete Instance Succeed", Toast.LENGTH_SHORT).show();
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    mOverlayDialog.dismiss();
                                                    FragmentManager manager = getFragmentManager();
                                                    InstanceFragment instanceFragment = new InstanceFragment();
                                                    manager.beginTransaction().replace(R.id.relativelayout_for_fragment, instanceFragment, instanceFragment.getTag()).commit();
                                                }
                                            };
                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 4000);
                                        } else{
                                            HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    setView(result);
                                                    mOverlayDialog.dismiss();
                                                }
                                            }, getActivity().getApplicationContext(), instanceId);
                                        }
                                    }
                                }, instanceId);
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
        final EditText input = new EditText(getActivity());
        final AlertDialog.Builder builderSnapshot = new AlertDialog.Builder(getActivity());
        builderSnapshot.setMessage("Please enter the name of this snapshot:").setView(input)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String snapshotName = input.getText().toString();
                        dialog.dismiss();
                        mOverlayDialog.show();
                        HttpRequestController.getInstance(getActivity().getApplicationContext()).snapshot(new HttpRequestController.VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                if(result.equals("success")) {
                                    TimerTask task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    setView(result);
                                                    mOverlayDialog.dismiss();
                                                    Toast.makeText(getActivity().getApplicationContext(), "Snapshot Succeed", Toast.LENGTH_SHORT).show();
                                                }
                                            }, getActivity().getApplicationContext(), instanceId);
                                        }
                                    };
                                    /**
                                     * Delay 7 secs after the button onclick method is called.
                                     * Wait for server status update. The server status is not modified in real-time.
                                     */
                                    timer.schedule(task, 7000);
                                } else{
                                    HttpRequestController.getInstance(getActivity().getApplicationContext()).listSingleInstance(new HttpRequestController.VolleyCallback() {
                                        @Override
                                        public void onSuccess(String result) {
                                            setView(result);
                                            mOverlayDialog.dismiss();
                                        }
                                    }, getActivity().getApplicationContext(), instanceId);
                                }
                            }
                        }, instanceId,snapshotName);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog alertDialog = builderSnapshot.create();

        snapshotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.show();
            }
        });

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

        final TextView instanceImageNameTV = (TextView)myView.findViewById(R.id.instanceImageNameTV);
        final TextView instanceVolumeTV = (TextView)myView.findViewById(R.id.instanceVolumeTV);
        try {
            JSONObject JSONResult = new JSONObject(result);
            instanceId = JSONResult.getString("id");
            instanceName = JSONResult.getString("name");
            zone = JSONResult.getString("zone");
            address = JSONResult.getString("address");
            instanceStatus = JSONResult.getString("status");
            created=JSONResult.getString("created");
            security=JSONResult.getString("securityg");
            key=JSONResult.getString("key");
            String imageID=JSONResult.getString("image");
            HttpRequestController.getInstance(getActivity().getApplicationContext()).showImageDetail(new HttpRequestController.VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    try{
                        JSONObject imageO = new JSONObject(result);
                        image=imageO.getString("imageName");
                        instanceImageNameTV.setText(image);
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, getActivity().getApplicationContext(), imageID);

            int vNum=JSONResult.getInt("volNum");
            volume="";

            if(vNum==0){
                volume="None";
            }else{
                for(int i=0; i<vNum;i++){
                    String vID=JSONResult.getString("volume"+i);
                    HttpRequestController.getInstance(getActivity().getApplicationContext()).showVolumeDetail(new HttpRequestController.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            try{
                                JSONObject volumeO = new JSONObject(result);
                                String vname=volumeO.getString("vName");
                                //vList.add(vname);
                                String former=instanceVolumeTV.getText().toString();
                                instanceVolumeTV.setText(former+" "+vname);
                            }catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, getActivity().getApplicationContext(), vID);

                }

            }



        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView instanceIdTV = (TextView)myView.findViewById(R.id.instanceIdTV);
        TextView instanceNameTV= (TextView)myView.findViewById(R.id.instanceNameTV);
        TextView instanceZoneTV = (TextView)myView.findViewById(R.id.instanceZoneTV);
        TextView instanceIPAddressTV = (TextView)myView.findViewById(R.id.instanceIPAddressTV);
        TextView instanceStatusTV = (TextView)myView.findViewById(R.id.instanceStatusTV);
        TextView instanceCreateTimeTV = (TextView)myView.findViewById(R.id.instanceCreateTimeTV);

        TextView instanceSecurityTV = (TextView)myView.findViewById(R.id.instanceSecurityTV);
        TextView instanceKeyPairTV = (TextView)myView.findViewById(R.id.instanceKeyPairTV);

        Button startBtn = (Button)myView.findViewById(R.id.startBtn);
        Button stopBtn = (Button)myView.findViewById(R.id.stopBtn);
        Button pauseBtn = (Button)myView.findViewById(R.id.pauseBtn);
        Button unpauseBtn = (Button)myView.findViewById(R.id.unpauseBtn);
        Button suspendBtn = (Button)myView.findViewById(R.id.suspendBtn);
        Button resumeBtn = (Button)myView.findViewById(R.id.resumeBtn);
        Button deleteBtn = (Button)myView.findViewById(R.id.deleteBtn);
        Button rebootBtn = (Button)myView.findViewById(R.id.rebootBtn);
        Button snapshotBtn = (Button)myView.findViewById(R.id.snapshotBtn);
        instanceIdTV.setText(instanceId);
        instanceNameTV.setText(instanceName);
        instanceZoneTV.setText(zone);
        instanceIPAddressTV.setText(address);
        instanceStatusTV.setText(instanceStatus);
        instanceCreateTimeTV.setText(created);
        instanceImageNameTV.setText(image);
        instanceSecurityTV.setText(security);
        instanceKeyPairTV.setText(key);
        instanceVolumeTV.setText(volume);



        //String created;
        //String image;
        //String security;
        //String key;
        //String volume;
        /**
         * set status text color
         */
        switch (instanceStatus) {
            case "ACTIVE":
                instanceStatusTV.setTextColor(Color.parseColor("#2eb82e"));//green
                break;
            case "DELETED":
            case "ERROR":
            case "PAUSED":
            case "SHUTOFF":
            case "SUSPENDED":
                instanceStatusTV.setTextColor(Color.parseColor("#ff0000"));//red
                break;
            default:
                instanceStatusTV.setTextColor(Color.parseColor("#e9a92a"));//orange
                break;
        }
        /**
         * Set Buttons visibility
         */
        switch (instanceStatus) {
            case "ACTIVE":
                enable(pauseBtn);
                enable(suspendBtn);
                enable(stopBtn);
                enable(rebootBtn);
                enable(snapshotBtn);
                enable(deleteBtn);
                hide(unpauseBtn);
                hide(resumeBtn);
                hide(startBtn);
                break;
            case "SUSPENDED":
                disable(pauseBtn);
                hide(unpauseBtn);
                enable(resumeBtn);
                hide(suspendBtn);
                disable(stopBtn);
                hide(startBtn);
                disable(rebootBtn);
                enable(snapshotBtn);
                enable(deleteBtn);
                break;
            case "PAUSED":
                enable(unpauseBtn);
                hide(pauseBtn);
                disable(suspendBtn);
                hide(resumeBtn);
                disable(stopBtn);
                hide(startBtn);
                disable(rebootBtn);
                enable(snapshotBtn);
                enable(deleteBtn);
                break;
            case "SHUTOFF":
                disable(pauseBtn);
                hide(unpauseBtn);
                disable(suspendBtn);
                hide(resumeBtn);
                hide(stopBtn);
                enable(startBtn);
                enable(rebootBtn);
                enable(snapshotBtn);
                enable(deleteBtn);
                break;
            default:
                disable(pauseBtn);
                hide(unpauseBtn);
                disable(suspendBtn);
                hide(resumeBtn);
                disable(stopBtn);
                hide(startBtn);
                disable(rebootBtn);
                disable(snapshotBtn);
                disable(deleteBtn);
                break;
        }

    }


}
