package com.jianqingc.nectar.fragment.Database_Fragment;


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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jianqingc.nectar.R;
import com.jianqingc.nectar.httpRequest.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class DatabaseInstanceDetailFragment extends Fragment {
    View myView;
    //    String instanceId;
    String databaseInstanceId;
    //    String instanceName ;
    String databaseInstanceName;
    String databaseInstanceDatastore;
    String databaseInstanceVersion;
    String databaseInstanceCreates;
    String databaseInstanceUpdated;
    String databaseInstanceStatus;
    String databaseInstanceVolume;
    JSONArray instanceResult;
    String password;
    String selectInstanceID;
    String rootEnabled;


    public static DatabaseInstanceDetailFragment test = null;
    public String testResult = null;

    public DatabaseInstanceDetailFragment() {
        // Required empty public constructor
        test = this;
    }

    /**
     * Enable and disable buttons and set Buttons color.
     *
     * @param btn
     */
    public void enable(Button btn) {
        btn.setEnabled(true);
        btn.setVisibility(View.VISIBLE);
        btn.setBackgroundColor(Color.parseColor("#ffcc00"));
        if (btn.getText().equals("DELETE")) {
            btn.setBackgroundColor(Color.parseColor("#FF4040"));
        }
        btn.setTextColor(Color.parseColor("#ffffff"));
    }

    public void disable(Button btn) {
        btn.setEnabled(false);
        btn.setVisibility(View.VISIBLE);
        btn.setBackgroundColor(Color.parseColor("#e6e6e6"));
        btn.setTextColor(Color.parseColor("#8c8c8c"));

    }

    public void hide(Button btn) {
        btn.setEnabled(false);
        btn.setVisibility(View.GONE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_database_instance_detail, container, false);
        Bundle bundle = getArguments();
        databaseInstanceId = bundle.getString("databaseInstanceID");
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Database Instance Detail");
//        final Bundle bundle1 = new Bundle();
//        bundle1.putString("databaseInstanceId", databaseInstanceId);

        final Button resizeBtn = (Button) myView.findViewById(R.id.databaseInstanceResizeVolumeBtn);
        final Button attachBtn = (Button) myView.findViewById(R.id.databaseInstanceAttachBtn);
        final Button detachBtn = (Button) myView.findViewById(R.id.databaseInstanceDetachBtn);
        final Button restartBtn = (Button) myView.findViewById(R.id.databaseInstanceRestartBtn);
        final Button deleteBtn = (Button) myView.findViewById(R.id.databaseInstanceDeleteBtn);
        final Button createBackupBtn = (Button) myView.findViewById(R.id.databaseInstanceCreateBackupBtn);
        final Button enableRootBtn = (Button) myView.findViewById(R.id.databaseInstanceEnableRootBtn);
        final Button disableRootBtn = (Button) myView.findViewById(R.id.databaseInstanceDisableRootBtn);
        final TextView enableRoot = (TextView) myView.findViewById(R.id.databaseInstanceRootPasswordTV);




        final Timer timer = new Timer(true);

        /**
         * set spinner which is actually a dialog
         */
        final Dialog mOverlayDialog = new Dialog(getActivity(), android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);
        mOverlayDialog.show();
        HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleDatabaseInstance(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                setView(result);
                //InstanceDetailFragment.test.testResult=result;
                //System.out.println(result);
                //System.out.println(test.testResult);
                mOverlayDialog.dismiss();
            }
        }, getActivity().getApplicationContext(), databaseInstanceId);
//        System.out.println(testResult);

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
                HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleDatabaseInstance(new HttpRequest.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        setView(result);
                        mOverlayDialog.dismiss();
                    }
                }, getActivity().getApplicationContext(), databaseInstanceId);
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
                DatabaseInstancesFragment databaseinstanceFragment = new DatabaseInstancesFragment();
                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, databaseinstanceFragment, databaseinstanceFragment.getTag()).commit();
            }
        });


        /**
         * set Restart button onclick
         */
        restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Restart insatnce ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOverlayDialog.show();
                                HttpRequest.getInstance(getActivity().getApplicationContext()).databaseInstanceRestart(new HttpRequest.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if (result.equals("success")) {
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleDatabaseInstance(new HttpRequest.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            setView(result);
                                                            mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Restart Instance Succeed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }, getActivity().getApplicationContext(), databaseInstanceId);
                                                }
                                            };
                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 3000);
                                        } else {
                                            HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleDatabaseInstance(new HttpRequest.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    setView(result);
                                                    mOverlayDialog.dismiss();
                                                }
                                            }, getActivity().getApplicationContext(), databaseInstanceId);
                                        }
                                    }
                                }, databaseInstanceId);
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

        /*
         * resize the volume
         * */
        resizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * set resize button onclick
                 */
                final EditText input = new EditText(getActivity());
                final AlertDialog.Builder builderSnapshot = new AlertDialog.Builder(getActivity());
                builderSnapshot.setMessage("Please enter the new size of volume:").setView(input)
                        .setPositiveButton("Resize Volume", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newSize = input.getText().toString();
                                if (newSize.length() == 0) {
                                    Toast.makeText(getActivity().getApplicationContext(), "Please enter a new size", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    int sizeInt = Integer.parseInt(newSize);
                                    //System.out.println(sizeInt);
                                    dialog.dismiss();
                                    mOverlayDialog.show();
                                    HttpRequest.getInstance(getActivity().getApplicationContext()).resizeDatabaseInstanceVolume(new HttpRequest.VolleyCallback() {
                                        @Override
                                        public void onSuccess(String result) {
                                            if (result.equals("success")) {

                                                Toast.makeText(getActivity().getApplicationContext(), "Resize Volume successfully", Toast.LENGTH_SHORT).show();
                                                TimerTask task = new TimerTask() {
                                                    @Override
                                                    public void run() {
                                                        HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleDatabaseInstance(new HttpRequest.VolleyCallback() {
                                                            @Override
                                                            public void onSuccess(String result) {
                                                                setView(result);
                                                                mOverlayDialog.dismiss();
                                                            }
                                                        }, getActivity(), databaseInstanceId);
                                                    }
                                                };
                                                /**
                                                 * Delay 7 secs after the button onclick method is called.
                                                 * Wait for server status update. The server status is not modified in real-time.
                                                 */
                                                timer.schedule(task, 4000);


                                            } else {
                                                mOverlayDialog.dismiss();
                                                Toast.makeText(getActivity().getApplicationContext(), "Fail to resize volume", Toast.LENGTH_SHORT).show();
                                                FragmentManager manager = getActivity().getSupportFragmentManager();
                                                DatabaseInstanceDetailFragment vFragment = new DatabaseInstanceDetailFragment();
                                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();

                                            }
                                        }
                                    }, sizeInt, databaseInstanceId);
                                }

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alertDialog = builderSnapshot.create();
                alertDialog.show();
//                dialogLV.dismiss();

            }
        });



        /*
         * attach configuration group
         * */
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater factory = LayoutInflater.from(getActivity());
                final View textEntryView = factory.inflate(R.layout.attach_configuration_group, null);
                final Spinner selectNameAV = (Spinner) textEntryView.findViewById(R.id.selectConfigGroupNameAV);
                AlertDialog.Builder builderSecurityGroup = new AlertDialog.Builder(getActivity());
                builderSecurityGroup.setTitle("Attach To Configuration Group");
                builderSecurityGroup.setIcon(R.drawable.nectar_app_icon);
                //builderSecurityGroup.setIcon(android.R.drawable.ic_dialog_info);
                builderSecurityGroup.setView(textEntryView);

                //List configuration group
                HttpRequest.getInstance(getContext()).listConfigGroup(new HttpRequest.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            /**
                             * Display instance Info with the Listview and Fundapter
                             * You can also use simple ArrayAdapter to replace Fundatper.
                             */
                            final List<String> data_list;
                            data_list = new ArrayList<String>();
                            ArrayAdapter<String> arr_adapter;
                            final List<String> id_list;
//                            final List<String> zone_list= new ArrayList<String>();
                            id_list = new ArrayList<String>();
                            data_list.add("Select an configuration group");
                            id_list.add("nothing");
//                            zone_list.add("nothing");

                            instanceResult = new JSONArray(result);
                            //System.out.println(falvorlist);
                            for (int i = 0; i < instanceResult.length(); i++) {
                                data_list.add(instanceResult.getJSONObject(i).getString("configGroupName"));
                                id_list.add(instanceResult.getJSONObject(i).getString("configGroupId"));
//                                zone_list.add(instanceResult.getJSONObject(i).getString("zone"));
                            }

                            //New an Adapter
                            arr_adapter = new ArrayAdapter<String>(DatabaseInstanceDetailFragment.this.getActivity(), android.R.layout.simple_spinner_item, data_list);
                            //Set the format of the adapter
                            arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            //set the adapter
                            selectNameAV.setAdapter(arr_adapter);

                            selectNameAV.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                    String chooseName = data_list.get(arg2);
                                    //Set to show the chosen
                                    arg0.setVisibility(View.VISIBLE);

                                    for (int i = 0; i < data_list.size(); i++) {
                                        if (data_list.get(i) == chooseName) {
                                            selectInstanceID = id_list.get(i);
//                                            zoneServer=zone_list.get(i);
                                        }
                                    }

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> arg0) {
                                    // TODO Auto-generated method stub
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, getActivity());


                builderSecurityGroup.setPositiveButton("Attach Configuration Group", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectInstanceID.equals("nothing")) {
                            Toast.makeText(getActivity().getApplicationContext(), "Please select an instance", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            dialog.dismiss();
                            mOverlayDialog.show();
                            HttpRequest.getInstance(getActivity().getApplicationContext()).attachConfigGroup(new HttpRequest.VolleyCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    if (result.equals("success")) {

                                        //mOverlayDialog.dismiss();
                                        Toast.makeText(getActivity().getApplicationContext(), "Attach Configuration Group successfully", Toast.LENGTH_SHORT).show();
                                        TimerTask task = new TimerTask() {
                                            @Override
                                            public void run() {
                                                HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleDatabaseInstance(new HttpRequest.VolleyCallback() {
                                                    @Override
                                                    public void onSuccess(String result) {
                                                        setView(result);
                                                        hide(attachBtn);
                                                        enable(detachBtn);
                                                        mOverlayDialog.dismiss();
                                                    }
                                                }, getActivity(), databaseInstanceId);
                                            }
                                        };
                                        /**
                                         * Delay 7 secs after the button onclick method is called.
                                         * Wait for server status update. The server status is not modified in real-time.
                                         */
                                        timer.schedule(task, 4000);

                                    } else {
                                        mOverlayDialog.dismiss();
                                        Toast.makeText(getActivity().getApplicationContext(), "Fail to attach configuration group", Toast.LENGTH_SHORT).show();
                                        FragmentManager manager = getActivity().getSupportFragmentManager();
                                        DatabaseInstanceDetailFragment vFragment = new DatabaseInstanceDetailFragment();
                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();

                                    }
                                }
                            }, databaseInstanceId, selectInstanceID);

                        }


                    }
                })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alertDialog = builderSecurityGroup.create();
                alertDialog.show();
//                dialogLV.dismiss();


            }
        });


        /*
         * detach configuration group
         * */

        detachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure to detach this configuration group?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOverlayDialog.show();
                                HttpRequest.getInstance(getActivity().getApplicationContext()).detachConfigGroup(new HttpRequest.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if (result.equals("success")) {

                                            //mOverlayDialog.dismiss();
                                            Toast.makeText(getActivity().getApplicationContext(), "Detach Configuration Group successfully", Toast.LENGTH_SHORT).show();
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleDatabaseInstance(new HttpRequest.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            setView(result);
                                                            hide(detachBtn);
                                                            enable(attachBtn);
                                                            mOverlayDialog.dismiss();
                                                        }
                                                    }, getActivity(), databaseInstanceId);
                                                }
                                            };
                                            /**                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 1000);


                                        } else {
                                            mOverlayDialog.dismiss();
                                            Toast.makeText(getActivity().getApplicationContext(), "Fail to detach volume", Toast.LENGTH_SHORT).show();
                                            //Toast.makeText(getActivity().getApplicationContext(), "Detach Volume successfully", Toast.LENGTH_SHORT).show();
                                            FragmentManager manager = getActivity().getSupportFragmentManager();
                                            DatabaseInstanceDetailFragment vFragment = new DatabaseInstanceDetailFragment();
                                            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();

                                        }
                                    }
                                }, databaseInstanceId);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();


//                dialogLV.dismiss();


            }
        });

        /*
        * root enabled
        * */
        HttpRequest.getInstance(getActivity().getApplicationContext()).showManageRootDetail(new HttpRequest.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                TextView showRoot = (TextView) myView.findViewById(R.id.databaseInstanceRootEnabledTV);
                try {
                    JSONObject JSONResult = new JSONObject(result);
                    rootEnabled = JSONResult.getString("rootEnabled");
                    showRoot.setText(rootEnabled);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                mOverlayDialog.dismiss();
            }
        }, getActivity().getApplicationContext(), databaseInstanceId);

        /*
        * enable root
        * */
        enableRootBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Enable Root ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOverlayDialog.show();
                                HttpRequest.getInstance(getActivity().getApplicationContext()).enableRoot(new HttpRequest.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {

                                            final TextView enableRoot = (TextView) myView.findViewById(R.id.databaseInstanceRootPasswordTV);

                                            try {
                                                JSONObject JSONResult = new JSONObject(result);
                                                password = JSONResult.getString("rootPassword");
                                                enableRoot.setText(password);
                                            }
                                            catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            HttpRequest.getInstance(getActivity().getApplicationContext()).showManageRootDetail(new HttpRequest.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    TextView showRoot = (TextView) myView.findViewById(R.id.databaseInstanceRootEnabledTV);
                                                    try {
                                                        JSONObject JSONResult = new JSONObject(result);
                                                        rootEnabled = JSONResult.getString("rootEnabled");
                                                        showRoot.setText(rootEnabled);
                                                    }
                                                    catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    mOverlayDialog.dismiss();
                                                }
                                            }, getActivity().getApplicationContext(), databaseInstanceId);
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleDatabaseInstance(new HttpRequest.VolleyCallback() {
                                                        @Override
                                                        public void onSuccess(String result) {
                                                            setView(result);
                                                            hide(enableRootBtn);
                                                            enable(disableRootBtn);
                                                            mOverlayDialog.dismiss();
                                                            Toast.makeText(getActivity().getApplicationContext(), "Enable Root Succeed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }, getActivity().getApplicationContext(), databaseInstanceId);
                                                }
                                            };
                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 3000);

                                    }
                                }, getActivity().getApplicationContext(),databaseInstanceId);
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


        /*
        * disable root
        * */
        disableRootBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Disable root?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOverlayDialog.show();
                                HttpRequest.getInstance(getActivity().getApplicationContext()).disableRoot(new HttpRequest.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if (result.equals("success")) {
                                            Toast.makeText(getActivity().getApplicationContext(), "Disable Root Succeed", Toast.LENGTH_SHORT).show();
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    mOverlayDialog.dismiss();
                                                    FragmentManager manager = getFragmentManager();
                                                    DatabaseInstanceDetailFragment instanceFragment = new DatabaseInstanceDetailFragment();
                                                    manager.beginTransaction().replace(R.id.relativelayout_for_fragment, instanceFragment, instanceFragment.getTag()).commit();
                                                }
                                            };
                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 4000);
                                        } else {
                                            HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleDatabaseInstance(new HttpRequest.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    setView(result);
                                                    hide(disableRootBtn);
                                                    enable(enableRootBtn);
                                                    enableRoot.setText("-");
                                                    mOverlayDialog.dismiss();
                                                }
                                            }, getActivity().getApplicationContext(), databaseInstanceId);
                                        }
                                    }
                                }, databaseInstanceId);
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


        /*
        * create backup
        * */
        createBackupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                CreateDatabaseBackupFragment createDatabaseBackupFragment = new CreateDatabaseBackupFragment();

                ft.replace(R.id.relativelayout_for_fragment, createDatabaseBackupFragment,createDatabaseBackupFragment.getTag());
                ft.commit();
//                dialogLV.dismiss();

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
                                HttpRequest.getInstance(getActivity().getApplicationContext()).deleteDatabaseInstance(new HttpRequest.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if (result.equals("success")) {
                                            Toast.makeText(getActivity().getApplicationContext(), "Delete Instance Succeed", Toast.LENGTH_SHORT).show();
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    mOverlayDialog.dismiss();
                                                    FragmentManager manager = getFragmentManager();
                                                    DatabaseInstancesFragment instanceFragment = new DatabaseInstancesFragment();
                                                    manager.beginTransaction().replace(R.id.relativelayout_for_fragment, instanceFragment, instanceFragment.getTag()).commit();
                                                }
                                            };
                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 4000);
                                        } else {
                                            HttpRequest.getInstance(getActivity().getApplicationContext()).listSingleDatabaseInstance(new HttpRequest.VolleyCallback() {
                                                @Override
                                                public void onSuccess(String result) {
                                                    setView(result);
                                                    mOverlayDialog.dismiss();
                                                }
                                            }, getActivity().getApplicationContext(), databaseInstanceId);
                                        }
                                    }
                                }, databaseInstanceId);
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


    public void setView(String result) {
        /**
         * Set the textviews and buttons according to the instance status.
         */

//        final TextView instanceImageNameTV = (TextView)myView.findViewById(R.id.instanceImageNameTV);
//        final TextView instanceVolumeTV = (TextView)myView.findViewById(R.id.instanceVolumeTV);
        try {
            JSONObject JSONResult = new JSONObject(result);
            databaseInstanceId = JSONResult.getString("id");
            databaseInstanceName = JSONResult.getString("name");
            databaseInstanceDatastore = JSONResult.getString("datastore");
            databaseInstanceVersion = JSONResult.getString("version");
            databaseInstanceCreates = JSONResult.getString("created");
            databaseInstanceUpdated = JSONResult.getString("updated");
            databaseInstanceStatus = JSONResult.getString("status");
            databaseInstanceVolume = JSONResult.getString("volume");
//


        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView databaseinstanceIdTV = (TextView) myView.findViewById(R.id.databaseInstanceIDTV);
        TextView databaseinstanceNameTV = (TextView) myView.findViewById(R.id.databaseInstanceNameTV);
        TextView databaseInstanceDatastoreTV = (TextView) myView.findViewById(R.id.databaseInstanceDatastoreTV);
        TextView databaseInstanceVersionTV = (TextView) myView.findViewById(R.id.databaseInstanceVersionTV);
        TextView databaseInstanceCreatedTV = (TextView) myView.findViewById(R.id.databaseInstanceCreatedTV);
        TextView databaseInstanceUpdatedTimeTV = (TextView) myView.findViewById(R.id.databaseInstanceUpdatedTimeTV);
        TextView databaseInstanceStatusTV = (TextView) myView.findViewById(R.id.databaseInstanceStatusTV);
        TextView databaseInstacenVolumeTV = (TextView) myView.findViewById(R.id.databaseInstanceVolumeTV);
        Button enableRootBtn = (Button) myView.findViewById(R.id.databaseInstanceEnableRootBtn);
        Button disableRootBtn = (Button) myView.findViewById(R.id.databaseInstanceDisableRootBtn);

        Button resizeBtn = (Button) myView.findViewById(R.id.databaseInstanceResizeVolumeBtn);
        Button attachBtn = (Button) myView.findViewById(R.id.databaseInstanceAttachBtn);
        Button deleteBtn = (Button) myView.findViewById(R.id.databaseInstanceDeleteBtn);
        Button restartBtn = (Button) myView.findViewById(R.id.databaseInstanceRestartBtn);
        Button detachBtn = (Button) myView.findViewById(R.id.databaseInstanceDetachBtn);
        Button createBackupBtn = (Button) myView.findViewById(R.id.databaseInstanceCreateBackupBtn);

        databaseinstanceIdTV.setText(databaseInstanceId);
        databaseinstanceNameTV.setText(databaseInstanceName);
        databaseInstanceDatastoreTV.setText(databaseInstanceDatastore);
        databaseInstanceVersionTV.setText(databaseInstanceVersion);
        databaseInstanceCreatedTV.setText(databaseInstanceCreates);
        databaseInstanceUpdatedTimeTV.setText(databaseInstanceUpdated);
        databaseInstanceStatusTV.setText(databaseInstanceStatus);
        databaseInstacenVolumeTV.setText(databaseInstanceVolume);


        /**
         * Set Buttons visibility
         */
        switch (databaseInstanceStatus) {
            case "ERROR":
                disable(resizeBtn);
                disable(attachBtn);
                disable(restartBtn);
                hide(detachBtn);
                disable(enableRootBtn);
                hide(disableRootBtn);
                disable(createBackupBtn);
                enable(deleteBtn);
                break;
            default:
                enable(resizeBtn);
                enable(attachBtn);
                hide(detachBtn);
                enable(enableRootBtn);
                hide(disableRootBtn);
                enable(restartBtn);
                enable(createBackupBtn);
                enable(deleteBtn);
                break;
        }

    }


}
