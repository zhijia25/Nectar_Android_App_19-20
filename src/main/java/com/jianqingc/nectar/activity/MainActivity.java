package com.jianqingc.nectar.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ikidou.fragmentBackHandler.BackHandlerHelper;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jianqingc.nectar.fragment.Container_Infra_Fragment.ClusterTemplateFragment;
import com.jianqingc.nectar.httpRequest.HttpRequest;
import com.jianqingc.nectar.fragment.AboutFragment;
import com.jianqingc.nectar.fragment.Container_Infra_Fragment.ClustersFragment;
import com.jianqingc.nectar.fragment.Container_Infra_Fragment.CreateClusterFragment;
import com.jianqingc.nectar.fragment.Network_Fragment.AddRuleSGFragment;
import com.jianqingc.nectar.fragment.Database_Fragment.ConfigurationGroupFragment;
import com.jianqingc.nectar.fragment.Object_Fragment.ContainerFragment;
import com.jianqingc.nectar.fragment.Alarm_Fragment.CreateAlarmFragment;
import com.jianqingc.nectar.fragment.Database_Fragment.CreateConfigurationGroupFragment;
import com.jianqingc.nectar.fragment.Object_Fragment.CreateContainerFragment;
import com.jianqingc.nectar.fragment.Database_Fragment.CreateDatabaseBackupFragment;
import com.jianqingc.nectar.fragment.Database_Fragment.CreateDatabaseInstanceFragment;
import com.jianqingc.nectar.fragment.Network_Fragment.CreateFloatingFragment;
import com.jianqingc.nectar.fragment.Network_Fragment.CreateNetworkFragment;
import com.jianqingc.nectar.fragment.Network_Fragment.CreateRouterFragment;
import com.jianqingc.nectar.fragment.Orchestration_Fragment.CreateStackFragment;
import com.jianqingc.nectar.fragment.Volume_Fragment.CreateVolumeFragment;
import com.jianqingc.nectar.fragment.Database_Fragment.DatabaseBackupFragment;
import com.jianqingc.nectar.fragment.Database_Fragment.DatabaseInstancesFragment;
import com.jianqingc.nectar.fragment.Network_Fragment.FloatingFragment;
import com.jianqingc.nectar.fragment.Compute_Fragment.InstanceFragment;
import com.jianqingc.nectar.fragment.Compute_Fragment.KeyPairFragment;
import com.jianqingc.nectar.fragment.Compute_Fragment.LaunchInstanceImageFragment;
import com.jianqingc.nectar.fragment.Network_Fragment.NetworkFragment;
import com.jianqingc.nectar.fragment.Compute_Fragment.OverviewFragment;
import com.jianqingc.nectar.R;
import com.jianqingc.nectar.fragment.Network_Fragment.RouterFragment;
import com.jianqingc.nectar.fragment.ResourceTypesFragment;
import com.jianqingc.nectar.fragment.Orchestration_Fragment.StacksFragment;
import com.jianqingc.nectar.fragment.Orchestration_Fragment.TemplateVersionsFragment;
import com.jianqingc.nectar.fragment.Volume_Fragment.VolumeFragment;
import com.jianqingc.nectar.fragment.Network_Fragment.AccessAndSecurityFragment;
import com.jianqingc.nectar.fragment.Compute_Fragment.ImageFragment;


import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    NavigationView navigationView;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Nectar Cloud");
        //toolbar.setTitleTextColor();
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("nectar_android", 0);


        /**
         * FloatingActionButton Left and right are Back Button and Refresh Button in Instance Detail Fragment
         */
        FloatingActionButton fabRight = (FloatingActionButton) findViewById(R.id.fabRight);
        FloatingActionButton fabLeft = (FloatingActionButton) findViewById(R.id.fabLeft);
        fabRight.setVisibility(View.GONE);
        fabRight.setEnabled(false);
        fabLeft.setVisibility(View.GONE);
        fabLeft.setEnabled(false);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);
        TextView tenantNameInNavHeader = (TextView) hView.findViewById(R.id.tenantNameInNavHeader);
        tenantNameInNavHeader.setText(sharedPreferences.getString("tenantName", "error in retrieving tenantName from shared preference"));
        TextView userNameInNavHeader = (TextView) hView.findViewById(R.id.userNameInNavHeader);
        userNameInNavHeader.setText(sharedPreferences.getString("username", "error in retrieving username from shared preference"));

        navigationView.setNavigationItemSelectedListener(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        /**
         * Set the default view as Overview Fragment
         */
        FragmentManager manager = getSupportFragmentManager();
        OverviewFragment overviewFragment = new OverviewFragment();
        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, overviewFragment, overviewFragment.getTag()).commit();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!BackHandlerHelper.handleBackPress(this)) {
            super.onBackPressed();
        }

    }

    @Override
    //This is an optional menu, if it is necessary, the return value can be setted to true to
    // display the menu option.
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.launch_instance).setVisible(false);
        menu.findItem(R.id.add_SG).setVisible(false);
        menu.findItem(R.id.add_KP).setVisible(false);
        menu.findItem(R.id.import_KP).setVisible(false);
        menu.findItem(R.id.add_rule_sg).setVisible(false);
        menu.findItem(R.id.create_volume_sg).setVisible(false);
        menu.findItem(R.id.create_alarm).setVisible(false);
        menu.findItem(R.id.create_container).setVisible(false);
        menu.findItem(R.id.create_floatingIP).setVisible(false);
        menu.findItem(R.id.create_router).setVisible(false);
        menu.findItem(R.id.create_network).setVisible(false);
        menu.findItem(R.id.create_stack).setVisible(false);
        menu.findItem(R.id.create_configuration_group).setVisible(false);
        menu.findItem(R.id.create_database_instance).setVisible(false);
        menu.findItem(R.id.create_database_backup).setVisible(false);
        menu.findItem(R.id.create_cluster).setVisible(false);

        //return true;
        return true;
    }

    //invalidateOptionMenu()


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        final FragmentManager manager = getSupportFragmentManager();
        final Dialog mOverlayDialog = new Dialog(this, android.R.style.Theme_Panel); //display an invisible overlay dialog to prevent user interaction and pressing back
        mOverlayDialog.setCancelable(false);
        mOverlayDialog.setContentView(R.layout.loading_dialog);

        //noinspection SimplifiableIfStatement
        if (id == R.id.launch_instance) {

                    LaunchInstanceImageFragment fra=new LaunchInstanceImageFragment();
                    manager.beginTransaction().replace(R.id.relativelayout_for_fragment, fra, fra.getTag()).commit();

            return true;
        }
        else if (id == R.id.add_SG) {
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.create_sg, null);
            final EditText newsgName = (EditText) textEntryView.findViewById(R.id.createSGName);
            final EditText newsgDescription = (EditText)textEntryView.findViewById(R.id.createSGDescription);
            AlertDialog.Builder builderSecurityGroup = new AlertDialog.Builder(this);
            builderSecurityGroup.setTitle("Create Security Group");
            builderSecurityGroup.setIcon(R.drawable.nectar_app_icon);
            //builderSecurityGroup.setIcon(android.R.drawable.ic_dialog_info);
            builderSecurityGroup.setView(textEntryView);
            builderSecurityGroup.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = newsgName.getText().toString();
                            String description = newsgDescription.getText().toString();
                            dialog.dismiss();
                            mOverlayDialog.show();
                            HttpRequest.getInstance(getApplicationContext()).createSecurityGroup(new HttpRequest.VolleyCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    if(result.equals("success")) {
                                        mOverlayDialog.dismiss();
                                        System.out.println("How many time of creation?");
                                        Toast.makeText(getApplicationContext(), "Create Security Group successfully", Toast.LENGTH_SHORT).show();

                                        AccessAndSecurityFragment accessAndSecurityFragment = new AccessAndSecurityFragment();
                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, accessAndSecurityFragment, accessAndSecurityFragment.getTag()).commit();



                                    } else{
                                        mOverlayDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Fail to create Security Group", Toast.LENGTH_SHORT).show();
                                        AccessAndSecurityFragment accessAndSecurityFragment = new AccessAndSecurityFragment();
                                        manager.beginTransaction().replace(R.id.relativelayout_for_fragment, accessAndSecurityFragment, accessAndSecurityFragment.getTag()).commit();

                                    }
                                }
                            }, name,description);
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

            return true;
        } else if (id == R.id.add_KP) {
            final java.util.Timer timer = new java.util.Timer(true);
            final EditText input = new EditText(this);
            final AlertDialog.Builder builderSnapshot = new AlertDialog.Builder(this);
            builderSnapshot.setMessage("Please enter the name of new Key Pair:").setView(input)
                    .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                                String name=input.getText().toString();
                                dialog.dismiss();
                                mOverlayDialog.show();
                                HttpRequest.getInstance(getApplicationContext()).createKeyPair(new HttpRequest.VolleyCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        if(result.equals("success")) {

                                            Toast.makeText(getApplicationContext(), "Create new key pair successfully", Toast.LENGTH_SHORT).show();
                                            TimerTask task = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    mOverlayDialog.dismiss();
                                                    FragmentManager manager = getSupportFragmentManager();
                                                    KeyPairFragment vFragment = new KeyPairFragment();
                                                    manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();

                                                }
                                            };
                                            /**
                                             * Delay 7 secs after the button onclick method is called.
                                             * Wait for server status update. The server status is not modified in real-time.
                                             */
                                            timer.schedule(task, 4000);


                                        } else{
                                            mOverlayDialog.dismiss();
                                            Toast.makeText(getApplicationContext(), "Fail to create a new key pair", Toast.LENGTH_SHORT).show();
                                            FragmentManager manager = getSupportFragmentManager();
                                            KeyPairFragment vFragment = new KeyPairFragment();
                                            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, vFragment, vFragment.getTag()).commit();

                                        }
                                    }
                                }, name);


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


            return true;
        } else if (id == R.id.import_KP) {
            LayoutInflater factory2 = LayoutInflater.from(this);
            final View textEntryView2 = factory2.inflate(R.layout.import_kp, null);
            final EditText importkpName = (EditText) textEntryView2.findViewById(R.id.importKPName);
            final EditText importkpPublicKey = (EditText)textEntryView2.findViewById(R.id.importKPPK);
            AlertDialog.Builder builderImportKP = new AlertDialog.Builder(this);
            builderImportKP.setTitle("Import Key Pair");
            builderImportKP.setIcon(R.drawable.nectar_app_icon);
            //builderSecurityGroup.setIcon(android.R.drawable.ic_dialog_info);
            builderImportKP.setView(textEntryView2);
            builderImportKP.setPositiveButton("Import", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = importkpName.getText().toString();
                    String publicKey = importkpPublicKey.getText().toString();
                    dialog.dismiss();
                    mOverlayDialog.show();
                    HttpRequest.getInstance(getApplicationContext()).importKeyPair(new HttpRequest.VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            if(result.equals("success")) {
                                mOverlayDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Import Key Pair successfully", Toast.LENGTH_SHORT).show();

                                KeyPairFragment kpFragment = new KeyPairFragment();
                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, kpFragment, kpFragment.getTag()).commit();



                            } else{
                                mOverlayDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Fail to import key pair", Toast.LENGTH_SHORT).show();
                                KeyPairFragment kpFragment = new KeyPairFragment();
                                manager.beginTransaction().replace(R.id.relativelayout_for_fragment, kpFragment, kpFragment.getTag()).commit();

                            }
                        }
                    }, name,publicKey);
                }
            })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alertDialog2 = builderImportKP.create();
            alertDialog2.show();

            return true;
        }else if (id == R.id.add_rule_sg) {
            SharedPreferences sharedPreferences =  getApplicationContext().getSharedPreferences("nectar_android", 0);
            String securityGID=sharedPreferences.getString("chosenSecurityGroup", "Error Getting Security Group");
            System.out.println("hahaha2: "+securityGID);
            Bundle bundle = new Bundle();
            bundle.putString("securityGroupId", securityGID);
            FragmentTransaction ft = getSupportFragmentManager()
                    .beginTransaction();
            AddRuleSGFragment arFragment = new AddRuleSGFragment();
            arFragment.setArguments(bundle);
            ft.replace(R.id.relativelayout_for_fragment, arFragment, arFragment.getTag()).commit();

            return true;
        }else if (id == R.id.create_volume_sg) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            CreateVolumeFragment cvFragment = new CreateVolumeFragment();
            ft.replace(R.id.relativelayout_for_fragment, cvFragment, cvFragment.getTag()).commit();
            return true;
        } else if (id == R.id.create_alarm) {
            CreateAlarmFragment caf = new CreateAlarmFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, caf, caf.getTag()).commit();
            return true;
        } else if (id == R.id.create_container) {
            CreateContainerFragment ccf = new CreateContainerFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, ccf, ccf.getTag()).commit();
            return true;
        } else if (id == R.id.create_floatingIP) {
            CreateFloatingFragment cff = new CreateFloatingFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, cff, cff.getTag()).commit();
            return true;
        } else if(id == R.id.create_router) {
            CreateRouterFragment crf = new CreateRouterFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, crf, crf.getTag()).commit();
            return true;
        } else if (id == R.id.create_network){
            CreateNetworkFragment cnf = new CreateNetworkFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, cnf, cnf.getTag()).commit();
            return true;
        } else if (id == R.id.create_stack){
            CreateStackFragment csf = new CreateStackFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, csf, csf.getTag()).commit();
            return true;
        } else if (id == R.id.create_configuration_group){
            CreateConfigurationGroupFragment csf = new CreateConfigurationGroupFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, csf, csf.getTag()).commit();
            return true;
        } else if (id == R.id.create_database_instance){
            CreateDatabaseInstanceFragment cdi = new CreateDatabaseInstanceFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, cdi, cdi.getTag()).commit();
            return true;
        } else if (id == R.id.create_database_backup){
            CreateDatabaseBackupFragment cdb = new CreateDatabaseBackupFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, cdb, cdb.getTag()).commit();
            return true;
        } else if (id == R.id.create_cluster){
            CreateClusterFragment cdb = new CreateClusterFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, cdb, cdb.getTag()).commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager manager = getSupportFragmentManager();
        /**
         * To created more items in the Navigation Drawer, simply add more entries in res/menu/activity_main_drawer.xml and define actions as below.
         */
        if (id == R.id.nav_overview) {
            OverviewFragment overviewFragment = new OverviewFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, overviewFragment, overviewFragment.getTag()).commit();
        } else if (id == R.id.nav_instances) {
            InstanceFragment instanceFragment = new InstanceFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, instanceFragment, instanceFragment.getTag()).commit();
        } else if (id == R.id.nav_volumes) {
            VolumeFragment volumeFragment = new VolumeFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, volumeFragment, volumeFragment.getTag()).commit();
        } else if (id == R.id.nav_images) {
            ImageFragment imageFragment = new ImageFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, imageFragment, imageFragment.getTag()).commit();
        } else if (id == R.id.nav_accessAndSecurity) {
            AccessAndSecurityFragment accessAndSecurityFragment = new AccessAndSecurityFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, accessAndSecurityFragment, accessAndSecurityFragment.getTag()).commit();
        } else if (id == R.id.nav_about) {
            AboutFragment aboutFragment = new AboutFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, aboutFragment, aboutFragment.getTag()).commit();
        } else if (id == R.id.nav_cluster) {
            ClustersFragment clustersFragment = new ClustersFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, clustersFragment, clustersFragment.getTag()).commit();
        } else if (id == R.id.nav_cluster_template) {
            ClusterTemplateFragment clusterTemplateFragment = new ClusterTemplateFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, clusterTemplateFragment, clusterTemplateFragment.getTag()).commit();
        } else if (id == R.id.nav_container) {
            ContainerFragment containerFragment = new ContainerFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment,containerFragment,containerFragment.getTag()).commit();
        } else if (id == R.id.nav_floatingIP) {
            FloatingFragment floatingFragment = new FloatingFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, floatingFragment, floatingFragment.getTag()).commit();
        } else if (id == R.id.nav_networks) {
            NetworkFragment networkFragment = new NetworkFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, networkFragment, networkFragment.getTag()).commit();
        } else if (id == R.id.nav_router) {
            RouterFragment routerFragment = new RouterFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment,routerFragment,routerFragment.getTag()).commit();
        } else if (id == R.id.nav_resourceTypes) {
            ResourceTypesFragment resourceTypesFragment = new ResourceTypesFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment,resourceTypesFragment,resourceTypesFragment.getTag()).commit();
        } else if (id == R.id.nav_templateVersions) {
            TemplateVersionsFragment templateVersionsFragment = new TemplateVersionsFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, templateVersionsFragment, templateVersionsFragment.getTag()).commit();
        } else if (id == R.id.nav_stack) {
            StacksFragment stacksFragment = new StacksFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, stacksFragment, stacksFragment.getTag()).commit();
        } else if (id == R.id.nav_database_instance) {
            DatabaseInstancesFragment databaseInstancesFragment = new DatabaseInstancesFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, databaseInstancesFragment, databaseInstancesFragment.getTag()).commit();
        } else if (id == R.id.nav_configuration_group) {
            ConfigurationGroupFragment configurationGroupFragment = new ConfigurationGroupFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, configurationGroupFragment, configurationGroupFragment.getTag()).commit();
        } else if (id == R.id.nav_database_backup) {
            DatabaseBackupFragment databaseBackupFragment = new DatabaseBackupFragment();
            manager.beginTransaction().replace(R.id.relativelayout_for_fragment, databaseBackupFragment, databaseBackupFragment.getTag()).commit();
        }
        else if (id == R.id.nav_signout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure to sign out?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                            SharedPreferences sharedPreferences =  getApplicationContext().getSharedPreferences("nectar_android", 0);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            /**
                             * Disable Auto Login
                             */
                            editor.putBoolean("isSignedOut",true);
                            editor.apply();
                            startActivity(i);
                            Toast.makeText(getApplicationContext(), "Successfully Signed Out!", Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.jianqingc.nectar/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);

    }

    @Override
    public void onStop() {
        super.onStop();
//        this.finish();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.jianqingc.nectar/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }



}
