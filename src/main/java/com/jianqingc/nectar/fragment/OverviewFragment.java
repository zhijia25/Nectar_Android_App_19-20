package com.jianqingc.nectar.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.jianqingc.nectar.R;
import com.jianqingc.nectar.activity.LoginActivity;
import com.jianqingc.nectar.activity.MainActivity;
import com.jianqingc.nectar.controller.HttpRequestController;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class OverviewFragment extends Fragment {
    View myView;
    public OverviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_overview, container, false);
        // Inflate the layout for this fragment

        HttpRequestController.getInstance(getContext()).listOverview(new HttpRequestController.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                /**
                 * Draw pie charts
                 * Detailed documentation of AChartEngine @ www.achartengine.org/
                 */
                try {
                    JSONObject jsonResult = new JSONObject(result);
                    int totalInstancesUsed = Integer.parseInt(jsonResult.getJSONObject("limits").getJSONObject("absolute").getString("totalInstancesUsed"));
                    int maxTotalInstances = Integer.parseInt(jsonResult.getJSONObject("limits").getJSONObject("absolute").getString("maxTotalInstances"));
                    int availableInstances = maxTotalInstances-totalInstancesUsed;
                    int totalRAMUsed = Integer.parseInt(jsonResult.getJSONObject("limits").getJSONObject("absolute").getString("totalRAMUsed"));
                    int maxTotalRAMSize = Integer.parseInt(jsonResult.getJSONObject("limits").getJSONObject("absolute").getString("maxTotalRAMSize"));
                    int availableRAM = maxTotalRAMSize-totalRAMUsed;
                    int totalCoresUsed = Integer.parseInt(jsonResult.getJSONObject("limits").getJSONObject("absolute").getString("totalCoresUsed"));
                    int maxTotalCores = Integer.parseInt(jsonResult.getJSONObject("limits").getJSONObject("absolute").getString("maxTotalCores"));
                    int availableCores = maxTotalCores-totalCoresUsed;


                    int[] colors = new int[]{Color.parseColor("#8a0c1c"),Color.parseColor("#D3D3D3")};

                    CategorySeries instanceSeries = new CategorySeries("Pie Graph");
                    instanceSeries.add("Used",totalInstancesUsed);
                    instanceSeries.add("Available",availableInstances);
                    CategorySeries RAMSeries = new CategorySeries("Pie Graph");
                    RAMSeries.add("Used",totalRAMUsed);
                    RAMSeries.add("Available",availableRAM);
                    CategorySeries coresSeries = new CategorySeries("Pie Graph");
                    coresSeries.add("Used",totalCoresUsed);
                    coresSeries.add("Available",availableCores);

                    DefaultRenderer instanceRenderer = new DefaultRenderer();
                    DefaultRenderer RAMRenderer = new DefaultRenderer();
                    DefaultRenderer coresRenderer = new DefaultRenderer();
                    for (int color:colors){
                        SimpleSeriesRenderer r = new SimpleSeriesRenderer();
                        r.setColor(color);
                        instanceRenderer.addSeriesRenderer(r);
                        RAMRenderer.addSeriesRenderer(r);
                        coresRenderer.addSeriesRenderer(r);
                    }

                    instanceRenderer.setStartAngle(270);
                    instanceRenderer.setClickEnabled(false);
                    instanceRenderer.setPanEnabled(false);
                    instanceRenderer.setShowLegend(false);
                    instanceRenderer.setZoomEnabled(false);
                    instanceRenderer.setChartTitleTextSize(36);
                    instanceRenderer.setLabelsTextSize(34);
                    instanceRenderer.setLabelsColor(Color.BLACK);
                    //instanceRenderer.setMargins(new int[]{40,50,35,50});
                    RAMRenderer.setStartAngle(270);
                    RAMRenderer.setClickEnabled(false);
                    RAMRenderer.setPanEnabled(false);
                    RAMRenderer.setShowLegend(false);
                    RAMRenderer.setZoomEnabled(false);
                    RAMRenderer.setChartTitleTextSize(36);
                    RAMRenderer.setLabelsTextSize(34);
                    RAMRenderer.setLabelsColor(Color.BLACK);
                    coresRenderer.setStartAngle(270);
                    coresRenderer.setClickEnabled(false);
                    coresRenderer.setPanEnabled(false);
                    coresRenderer.setShowLegend(false);
                    coresRenderer.setZoomEnabled(false);
                    coresRenderer.setChartTitleTextSize(36);
                    coresRenderer.setLabelsTextSize(34);
                    coresRenderer.setLabelsColor(Color.BLACK);
                    instanceRenderer.setChartTitle("Instances\nUsed "+ totalInstancesUsed + " of " + maxTotalInstances);
                    RAMRenderer.setChartTitle("RAM\nUsed "+ totalRAMUsed + "MB of " + maxTotalRAMSize +"MB");
                    coresRenderer.setChartTitle("VCPUs\nUsed "+ totalCoresUsed + " of " + maxTotalCores);
                    GraphicalView instancesGraphicalView = ChartFactory.getPieChartView(getActivity(),instanceSeries,instanceRenderer);
                    GraphicalView RAMGraphicalView = ChartFactory.getPieChartView(getActivity(),RAMSeries,RAMRenderer);
                    GraphicalView coresGraphicalView = ChartFactory.getPieChartView(getActivity(),coresSeries,coresRenderer);
                    LinearLayout instancesGraphicalLayout = (LinearLayout)myView.findViewById(R.id.instancesGraphicalLayout);
                    LinearLayout RAMGraphicalLayout = (LinearLayout)myView.findViewById(R.id.RAMGraphicalLayout);
                    LinearLayout coresGraphicalLayout = (LinearLayout)myView.findViewById(R.id.coresGraphicalLayout);
                    instancesGraphicalLayout.addView(instancesGraphicalView,0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,900));
                    RAMGraphicalLayout.addView(RAMGraphicalView,0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 900));
                    coresGraphicalLayout.addView(coresGraphicalView,0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 900));

                    /**
                    if (alertRAM >0.49){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("RAM Reminder");
                        builder.setIcon(R.mipmap.ic_launcher);
                        builder.setMessage("Available RAM space is less than 25%.");
                        builder.setPositiveButton("OK",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.setCancelable(true);
                        builder.show();

                    }

                    if(alertCores > 0.49){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("CPU Reminder");
                        builder.setMessage("CPU Useage is higher than 75%");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.setCancelable(true);
                        builder.show();

                    }
                     **/




                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, getActivity());
        return myView;
    }

}
