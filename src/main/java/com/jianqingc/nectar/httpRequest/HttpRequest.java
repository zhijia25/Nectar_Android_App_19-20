package com.jianqingc.nectar.httpRequest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;
import com.android.volley.AuthFailureError;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.jianqingc.nectar.activity.LoginActivity;
import com.jianqingc.nectar.data.ResponseParser;
import com.jianqingc.nectar.model.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jianqing Chen on 2016/10/2.
 */
public class HttpRequest {
    private Context mApplicationContext;
    private static HttpRequest mInstance;
    private SharedPreferences sharedPreferences;

    public interface VolleyCallback {
        void onSuccess(String result);
    }

    public static HttpRequest getInstance(Context context) {
        if (mInstance == null)
            mInstance = new HttpRequest(context);
        return mInstance;

    }

    public HttpRequest(Context context) {
        this.mApplicationContext = context.getApplicationContext();
    }

    /**
     * Login Http Request sent to Keystone.
     *
     * @param tenantName
     * @param username
     * @param password
     * @param context
     */

    public void loginHttp(String tenantName, String username, String password, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        final String loginUri = "https://keystone.rc.nectar.org.au:5000/v3/auth/tokens";
        /**
         * Assemble Json Object According to NeCTAR API documentation
         */
        JSONObject identityDomain = new JSONObject();
        JSONObject user = new JSONObject();
        JSONObject passwordOuter = new JSONObject();
        JSONObject identity = new JSONObject();
        JSONObject scopeId = new JSONObject();
        JSONObject project = new JSONObject();
        JSONObject scope = new JSONObject();
        JSONObject auth = new JSONObject();
        JSONObject send = new JSONObject();
        JSONArray jsa = new JSONArray();
        jsa.put("password");

        try {
            identityDomain.put("id","default");
            user.put("password", password);
            user.put("name", username);
            user.put("domain", identityDomain);
            passwordOuter.put("user", user);
            identity.put("password", passwordOuter);
            identity.put("methods", jsa);
            scopeId.put("id","default");
            project.put("name",tenantName);
            project.put("domain", scopeId);
            scope.put("project",project);
            auth.put("identity", identity);
            auth.put("scope", scope);
            send.put("auth", auth);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LoginModel loginModel = new LoginModel();
        JsonRequest request = loginModel.loginRequest(loginUri,send, context, mApplicationContext);
        Network.getInstance(mApplicationContext).addToRequestQueue(request);
    }

    /**
     * List Overview Http Request
     * Pass the String response to Overview Fragment. Overview Fragment can then draw graphs based on the response.
     *
     * @param callback
     * @param context
     */
    public void listOverview(final VolleyCallback callback, final Context context) {
        ListOverviewModel listOverviewModel = new ListOverviewModel();
        StringRequest stringRequest = listOverviewModel.listOverview(callback,context,mApplicationContext,sharedPreferences);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List Instance Http Request showing the servers detail
     *
     * @param callback
     * @param context
     */
    public void listInstance(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL = "/servers/detail";
        String fullURL = computeServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListInstanceModel listInstanceModel = new ListInstanceModel();
        StringRequest stringRequest = listInstanceModel.listInstance(fullURL,token, mApplicationContext,callback,context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List flavor Http Request showing the available flavors
     *
     * @param callback
     * @param context
     */

    public void listFlavor(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL = "/flavors";
        String fullURL = computeServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListFlavorModel listFlavorModel = new ListFlavorModel();
        StringRequest stringRequest = listFlavorModel.listFlavor(fullURL,token,mApplicationContext,callback,context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List key pair Http Request showing the available key pairs
     *
     * @param callback
     * @param context
     */
    public void listKeyPair(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL = "/os-keypairs";
        String fullURL = computeServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListKeyPairModel listKeyPairModel = new ListKeyPairModel();
        StringRequest stringRequest = listKeyPairModel.listKeyPair(fullURL,token, mApplicationContext, callback,context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List rules for the specific security group the user clicks in the AccessAndSecurityFragment listview
     *
     * @param callback
     * @param context
     * @param kpName
     */
    public void showKeyPairDetail(final VolleyCallback callback, final Context context, String kpName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL = "/os-keypairs/";
        String fullURL = computeServiceURL + partURL + kpName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ShowKeyPairDetailModel showKeyPairDetailModel = new ShowKeyPairDetailModel();
        StringRequest stringRequest = showKeyPairDetailModel.showKeyPairDetail(fullURL,token,kpName,mApplicationContext,callback,context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * Delete key pair
     *
     * @param callback
     * @param kpName
     */
    public void deleteKeyPair(final VolleyCallback callback, String kpName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL = "/os-keypairs/";
        String fullURL = computeServiceURL + partURL + kpName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DeleteKeyPairModel deleteKeyPairModel = new DeleteKeyPairModel();
        StringRequest stringRequest = deleteKeyPairModel.deleteKeyPair(fullURL,token,kpName, mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List availability zone Http Request showing the availability zones
     *
     * @param callback
     * @param context
     */
    public void listAvailabilityZone(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL = "/os-availability-zone";
        String fullURL = computeServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListAvailabilityZoneModel listAvailabilityZoneModel = new ListAvailabilityZoneModel();
        StringRequest stringRequest = listAvailabilityZoneModel.listavailabilityZone(fullURL,token, mApplicationContext, callback,  context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List security group Http Request showing the available security groups
     *
     * @param callback
     * @param context
     */
    public void listSecurityGroup(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "/v2.0/security-groups";
        String fullURL = networkServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListSecurityGroupModel listSecurityGroupModel = new ListSecurityGroupModel();
        StringRequest stringRequest = listSecurityGroupModel.listSecurityGroup(fullURL, token,  mApplicationContext, callback,context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * Delete security group
     *
     * @param callback
     * @param sgID
     */
    public void deleteSecurityGroup(final VolleyCallback callback, String sgID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "/v2.0/security-groups/";
        String fullURL = computeServiceURL + partURL + sgID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DeleteSecurityGroupModel deleteSecurityGroupModel = new DeleteSecurityGroupModel();
        StringRequest stringRequest = deleteSecurityGroupModel.deleteSecurityGroup(fullURL, token,mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List rules for the specific security group the user clicks in the AccessAndSecurityFragment listview
     *
     * @param callback
     * @param context
     * @param sgId
     */
    public void listManageRuleSG(final VolleyCallback callback, final Context context, String sgId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "/v2.0/security-groups/";
        String fullURL = computeServiceURL + partURL + sgId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListManageRuleSGModel listManageRuleSGModel = new ListManageRuleSGModel();
        StringRequest stringRequest = listManageRuleSGModel.listManageRuleSG(fullURL,token,mApplicationContext,callback,context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * Delete a rule
     *
     * @param callback
     * @param ruleID
     */
    public void deleteRuleSG(final VolleyCallback callback, String ruleID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "/v2.0/security-group-rules/";
        String fullURL = computeServiceURL + partURL + ruleID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DeleteRuleSGModel deleteRuleSGModel = new DeleteRuleSGModel();
        StringRequest stringRequest = deleteRuleSGModel.deleteRuleSG(fullURL,token, mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List alarm Http Request showing the created alarms of current project
     *
     * @param callback
     * @param context
     */
    public void listAlarmProject(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String alarmingServiceURL = sharedPreferences.getString("alarmingServiceURL", "Error Getting Compute URL");
        String partURL = "/v2/alarms";
        String fullURL = alarmingServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListAlarmProjectModel listAlarmProjectModel = new ListAlarmProjectModel();
        StringRequest stringRequest = listAlarmProjectModel.listAlarmProject(fullURL,token,mApplicationContext, callback,context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List image Http Request showing the available images of current project
     *
     * @param callback
     * @param context
     */


    public void listImageProject(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String imageServiceURL = sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL");
        String partURL = "/v2/images";
        String fullURL = imageServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListImageProjectModel listImageProjectModel = new ListImageProjectModel();
        StringRequest stringRequest = listImageProjectModel.listImageProject(fullURL,token, mApplicationContext,callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List image Http Request showing the available images of NECTAR Official
     *
     * @param callback
     * @param context
     */

    public void listImageOfficial(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String imageServiceURL = sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL");
        String partURL = "/v2/images?owner=";
        String owner = sharedPreferences.getString("tenantId", "Error Getting Project Id");
        String fullURL = imageServiceURL + partURL + owner;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListImageOfficialModel listImageOfficialModel = new ListImageOfficialModel();
        StringRequest stringRequest = listImageOfficialModel.listImageOfficial(fullURL,  token, mApplicationContext,  callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * Get the detailed info of a specific image
     *
     * @param callback
     * @param context
     * @param id
     */

    public void showImageDetail(final VolleyCallback callback, final Context context, String id) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String imageServiceURL = sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL");
        String partURL = "/v2/images/";
        String fullURL = imageServiceURL + partURL + id;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ShowImageDetailModel showImageDetailModel = new ShowImageDetailModel();
        StringRequest stringRequest = showImageDetailModel.showImageDetail(fullURL,  token, mApplicationContext,  callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * Delete image
     *
     * @param callback
     * @param imageID
     */
    public void deleteImage(final VolleyCallback callback, String imageID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL");
        String partURL = "/v2/images/";
        String fullURL = computeServiceURL + partURL + imageID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DeleteImageModel deleteImageModel = new DeleteImageModel();
        StringRequest stringRequest = deleteImageModel.deleteImage(fullURL,  token, mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List Instance Detail for the specific instance the user clicks in the InstanceFragment listview
     *
     * @param callback
     * @param context
     * @param instanceId
     */
    public void listSingleInstance(final VolleyCallback callback, final Context context, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL= "/servers/";
        String fullURL = computeServiceURL + partURL + instanceId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListSingleInstanceModel listSingleInstanceModel = new ListSingleInstanceModel();
        StringRequest stringRequest = listSingleInstanceModel.listSingleInstance(fullURL,  token, mApplicationContext,  callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List available volume type
     *
     * @param callback
     * @param context
     */
    public void listVolumeType(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String volumeV3ServiceURL = sharedPreferences.getString("volumeV3ServiceURL", "Error Getting Volume URL v3");
        String partURL = "/types";
        String fullURL = volumeV3ServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListVolumeTypeModel listVolumeTypeModel = new ListVolumeTypeModel();
        StringRequest stringRequest = listVolumeTypeModel.listVolumeType(fullURL,  token, mApplicationContext,  callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List Volume Snapshot Http Request
     *
     * @param callback
     * @param context
     */
    public void listVolumeSnapshot(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String volumeV3ServiceURL = sharedPreferences.getString("volumeV3ServiceURL", "Error Getting Volume URL v3");
        String partURL = "/snapshots/detail";
        String fullURL = volumeV3ServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListVolumeSnapshotModel listVolumeSnapshotModel = new ListVolumeSnapshotModel();
        StringRequest stringRequest = listVolumeSnapshotModel.listVolumeSnapshot(fullURL,  token, mApplicationContext,  callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List Volume Http Request
     *
     * @param callback
     * @param context
     */
    public void listVolume(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String volumeV3ServiceURL = sharedPreferences.getString("volumeV3ServiceURL", "Error Getting Volume URL v3");
        String partURL = "/volumes/detail";
        String fullURL = volumeV3ServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListVolumeModel listVolumeModel = new ListVolumeModel();
        StringRequest stringRequest = listVolumeModel.listVolume(fullURL,  token, mApplicationContext,  callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * Get the detailed info of a specific volume snapshot
     *
     * @param callback
     * @param context
     * @param snapshotid
     */

    public void showVolumeSnapshotDetail(final VolleyCallback callback, final Context context, String snapshotid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String volumeV3ServiceURL = sharedPreferences.getString("volumeV3ServiceURL", "Error Getting Volume URL v3");
        String partURL = "/snapshots/";
        String fullURL = volumeV3ServiceURL + partURL + snapshotid;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ShowVolumeSnapshotDetailMdoel showVolumeSnapshotDetailMdoel = new ShowVolumeSnapshotDetailMdoel();
        StringRequest stringRequest = showVolumeSnapshotDetailMdoel.showVolumeSnapshotDetail(fullURL,  token, mApplicationContext,  callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * Get the detailed info of a specific volume
     *
     * @param callback
     * @param context
     * @param volumeid
     */

    public void showVolumeDetail(final VolleyCallback callback, final Context context, String volumeid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String volumeV3ServiceURL = sharedPreferences.getString("volumeV3ServiceURL", "Error Getting Volume URL v3");
        String partURL = "/volumes/";
        String fullURL = volumeV3ServiceURL + partURL + volumeid;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ShowVolumeDetailModel showVolumeDetailModel = new ShowVolumeDetailModel();
        StringRequest stringRequest = showVolumeDetailModel.showVolumeDetail(fullURL,  token, mApplicationContext,  callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * Server action: Pause Http Request
     *
     * @param callback
     * @param instanceId
     */
    public void pause(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL1 = "/servers/";
        String partURL2 = "/action";
        String fullURL = computeServiceURL + partURL1 + instanceId + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("pause", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PauseModel pauseModel = new PauseModel();
        JsonObjectRequest jsonObjectRequest = pauseModel.pause(fullURL,  token, json,  mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Server action: Unpause Http Request
     *
     * @param callback
     * @param instanceId
     */
    public void unpause(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL1 = "/servers/";
        String partURL2 = "/action";
        String fullURL = computeServiceURL + partURL1 + instanceId + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("unpause", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        UnpauseModel unpauseModel = new UnpauseModel();
        JsonObjectRequest jsonObjectRequest = unpauseModel.unpause(fullURL,  token, json,  mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * Server action: Stop Http Request
     *
     * @param callback
     * @param instanceId
     */
    public void stop(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL1 = "/servers/";
        String partURL2 = "/action";
        String fullURL = computeServiceURL + partURL1 + instanceId + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("os-stop", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StopModel stopModel = new StopModel();
        JsonObjectRequest jsonObjectRequest = stopModel.stop(fullURL,  token, json,  mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * Server action: Pause Start Request
     *
     * @param callback
     * @param instanceId
     */
    public void start(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL1 = "/servers/";
        String partURL2 = "/action";
        String fullURL = computeServiceURL + partURL1 + instanceId + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("os-start", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StartModel startModel = new StartModel();
        JsonObjectRequest jsonObjectRequest = startModel.start(fullURL,  token, json,  mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Server action: Suspend Http Request
     *
     * @param callback
     * @param instanceId
     */
    public void suspend(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL1 = "/servers/";
        String partURL2 = "/action";
        String fullURL = computeServiceURL + partURL1 + instanceId + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("suspend", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SuspendModel suspendModel = new SuspendModel();
        JsonObjectRequest jsonObjectRequest = suspendModel.suspend(fullURL,  token, json,  mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * Server action: Resume Http Request
     *
     * @param callback
     * @param instanceId
     */
    public void resume(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL1 = "/servers/";
        String partURL2 = "/action";
        String fullURL = computeServiceURL + partURL1 + instanceId + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("resume", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ResumeModel resumeModel = new ResumeModel();
        JsonObjectRequest jsonObjectRequest = resumeModel.resume(fullURL,  token, json,  mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Server action: Reboot Http Request
     *
     * @param callback
     * @param instanceId
     */
    public void reboot(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL1 = "/servers/";
        String partURL2 = "/action";
        String fullURL = computeServiceURL + partURL1 + instanceId + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("type", "HARD");
            json1.put("reboot", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RebootModel rebootModel = new RebootModel();
        JsonObjectRequest jsonObjectRequest = rebootModel.reboot(fullURL,  token, json1,  mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Server action: Delete Http Request
     *
     * @param callback
     * @param instanceId
     */
    public void delete(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL1 = "/servers/";
        String partURL2 = "/action";
        String fullURL = computeServiceURL + partURL1 + instanceId + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("forceDelete", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DeleteModel deleteModel = new DeleteModel();
        JsonObjectRequest jsonObjectRequest = deleteModel.delete(fullURL,  token,json, mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Server action: Snapshot Http Request
     *
     * @param callback
     * @param instanceId
     * @param snapshotName
     */
    public void snapshot(final VolleyCallback callback, String instanceId, String snapshotName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONObject json3 = new JSONObject();
        try {
            json3.put("meta_var", "meta_val");
            json2.put("metadata", json3);
            json2.put("name", snapshotName);
            json1.put("createImage", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SnapshotModel snapshotModel = new SnapshotModel();
        JsonObjectRequest jsonObjectRequest = snapshotModel.snapshot(fullURL,  token,  json1,  mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Luanch a new server
     *
     * @param callback
     * @param name
     * @param flavor
     * @param image
     * @param kp
     * @param az
     * @param sg
     */
    public void launchServer(final VolleyCallback callback, String name, String flavor, String image, String kp, String az, List<String> sg) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONArray sgArray = new JSONArray();
        if (sg.size() != 0) {
            for (int i = 0; i < sg.size(); i++) {
                JSONObject sgChoose = new JSONObject();
                try {
                    sgChoose.put("name", sg.get(i));
                    sgArray.put(sgChoose);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name", name);
            json2.put("imageRef", image);
            json2.put("flavorRef", flavor);
            if (az != "Select Availability Zone please") {
                json2.put("availability_zone", az);
            }
            if (kp != "Select Key pair please") {
                json2.put("key_name", kp);
            }
            if (sg.size() != 0) {
                json2.put("security_groups", sgArray);
            }
            json1.put("server", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LaunchServerModel launchServerModel = new LaunchServerModel();
        JsonObjectRequest jsonObjectRequest = launchServerModel.launchServer(fullURL, token,json1, mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * create a new security group
     *
     * @param callback
     * @param name
     * @param description
     */
    public void createSecurityGroup(final VolleyCallback callback, String name, String description) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "/v2.0/security-groups";
        String fullURL = computeServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name", name);
            json2.put("description", description);
            json1.put("security_group", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CreateSecurityGroupModel createSecurityGroupModel = new CreateSecurityGroupModel();
        JsonObjectRequest jsonObjectRequest = createSecurityGroupModel.createSecurityGroup(fullURL, token, json1, mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * edit an existing security group
     *
     * @param callback
     * @param sgid
     * @param name
     * @param description
     */
    public void editSecurityGroup(final VolleyCallback callback, String sgid, String name, String description) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "/v2.0/security-groups/";
        String fullURL = computeServiceURL + partURL + sgid;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name", name);
            json2.put("description", description);
            json1.put("security_group", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        EditSecurityGroupModel editSecurityGroupModel = new EditSecurityGroupModel();
        JsonObjectRequest jsonObjectRequest = editSecurityGroupModel.editSecurityGroup(fullURL,token, json1, mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * import a key pair with a public key
     *
     * @param callback
     * @param name
     * @param publicKey
     */
    public void importKeyPair(final VolleyCallback callback, String name, String publicKey) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL = "/os-keypairs";
        String fullURL = computeServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name", name);
            json2.put("public_key", publicKey);
            json1.put("keypair", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ImportKeyPairModel importKeyPairModel = new ImportKeyPairModel();
        JsonObjectRequest jsonObjectRequest = importKeyPairModel.importKeyPair(fullURL,token, json1,  mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * Create a new key pair
     *
     * @param callback
     * @param name
     */
    public void createKeyPair(final VolleyCallback callback, String name) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL = "/os-keypairs";
        String fullURL = computeServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name", name);
            json1.put("keypair", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CreateKeyPairModel createKeyPairModel = new CreateKeyPairModel();
        JsonObjectRequest jsonObjectRequest = createKeyPairModel.createKeyPair(fullURL,token,  json1,  mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Luanch a new server
     *
     * @param callback
     * @param sgID
     * @param protocol
     * @param dir
     * @param minPort
     * @param maxPort
     * @param cidr
     * @param ethertype
     */
    public void addNewRule(final VolleyCallback callback, String sgID, String protocol, String dir, String minPort, String maxPort, String cidr, String ethertype) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "/v2.0/security-group-rules";
        String fullURL = computeServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("security_group_id", sgID);
            json2.put("protocol", protocol);
            json2.put("direction", dir);
            json2.put("port_range_min", minPort);
            json2.put("port_range_max", maxPort);
            json2.put("remote_ip_prefix", cidr);
            json2.put("ethertype", ethertype);

            json1.put("security_group_rule", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AddNewRuleModel addNewRuleModel = new AddNewRuleModel();
        JsonObjectRequest jsonObjectRequest = addNewRuleModel.addNewRule(fullURL,token,json1, mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * attach a volume to a instance
     *
     * @param callback
     * @param instanceID
     * @param mountpoint
     * @param volumeid
     */
    public void attachVolume(final VolleyCallback callback, String instanceID, String mountpoint, String volumeid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String partURL1 = "/servers/";
        String partURL2 = "/os-volume_attachments";
        String fullURL = computeServiceURL + partURL1 + instanceID + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("volumeId", volumeid);
            json2.put("device", mountpoint);
            json1.put("volumeAttachment", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AttachVolumeModel attachVolumeModel = new AttachVolumeModel();
        JsonObjectRequest jsonObjectRequest = attachVolumeModel.attachVolume(fullURL, token, json1,mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * attach a volume to a instance
     *
     * @param callback
     * @param attachID
     * @param serverid
     */
    public void detachVolume(final VolleyCallback callback, String attachID, String serverid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + serverid + "/os-volume_attachments/" + attachID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DetachVolumeModel detachVolumeModel = new DetachVolumeModel();
        StringRequest stringRequest = detachVolumeModel.detachVolume(fullURL,token,mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * edit an existing volume
     *
     * @param callback
     * @param name
     * @param description
     * @param volumeid
     */
    public void editVolume(final VolleyCallback callback, String name, String description, String volumeid) {
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = sharedPreferences.getString("volumeV3ServiceURL", "Error Getting volumeV3ServiceURL");
        String partURL = "/volumes/";
        String fullURL = volumeV3ServiceURL + partURL + volumeid;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name", name);
            json2.put("description", description);
            json1.put("volume", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        EditVolumeModel editVolumeModel = new EditVolumeModel();
        JsonObjectRequest jsonObjectRequest = editVolumeModel.editVolume(fullURL,token,json1,mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * extend the size of a volume
     *
     * @param callback
     * @param newSize
     * @param volumeid
     */
    public void extendVolume(final VolleyCallback callback, int newSize, String volumeid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String volumeV3ServiceURL = sharedPreferences.getString("volumeV3ServiceURL", "Error Getting volumeV3ServiceURL");
        String partURL1 = "/volumes/";
        String partURL2 = "/action";
        String fullURL = volumeV3ServiceURL + partURL1 + volumeid + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("new_size", newSize);
            json1.put("os-extend", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ExtendVolumeModel extendVolumeModel = new ExtendVolumeModel();
        JsonObjectRequest jsonObjectRequest = extendVolumeModel.extendVolume(fullURL,token, json1,  mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * Delete volume
     *
     * @param callback
     * @param volumeid
     */
    public void deleteVolume(final VolleyCallback callback, String volumeid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String volumeV3ServiceURL = sharedPreferences.getString("volumeV3ServiceURL", "Error Getting Compute URL");
        String partURL = "/volumes/";
        String fullURL = volumeV3ServiceURL + partURL + volumeid;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DeleteVolumeModel deleteVolumeModel = new DeleteVolumeModel();
        StringRequest stringRequest = deleteVolumeModel.deleteVolume(fullURL, token,  mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * create a snapshot based on an existing volume
     *
     * @param callback
     * @param name
     * @param description
     * @param volumeid
     */
    public void createVolumeSnapshot(final VolleyCallback callback, String name, String description, String volumeid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String volumeV3ServiceURL = sharedPreferences.getString("volumeV3ServiceURL", "Error Getting Compute URL");
        String partURL = "/snapshots";
        String fullURL = volumeV3ServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name", name);
            json2.put("volume_id", volumeid);
            json2.put("description", description);
            json1.put("snapshot", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CreateVolumeSnapshotModel createVolumeSnapshotModel = new CreateVolumeSnapshotModel();
        JsonObjectRequest jsonObjectRequest = createVolumeSnapshotModel.createVolumeSnapshot(fullURL,token, json1, mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * Luanch a new server
     *
     * @param callback
     * @param name
     * @param description
     * @param size
     * @param zone
     * @param type
     */
    public void createVolume(final VolleyCallback callback, String name, String description, int size, String zone, String type) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String volumeV3ServiceURL = sharedPreferences.getString("volumeV3ServiceURL", "Error Getting volumeV3ServiceURL");
        String partURL = "/volumes";
        String fullURL = volumeV3ServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("size", size);
            json2.put("availability_zone", zone);
            json2.put("description", description);
            json2.put("name", name);
            json2.put("volume_type", type);
            JSONObject json3 = new JSONObject();
            json2.put("metadata", json3);
            json2.put("consistenucygroup_id", null);
            json1.put("volume", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CreateVolumeModel createVolumeModel = new CreateVolumeModel();
        JsonObjectRequest jsonObjectRequest = createVolumeModel.createVolume(fullURL,token, json1,  mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     * Delete volume snapshot
     *
     * @param callback
     * @param snapshotID
     */
    public void deleteVolumeSnapshot(final VolleyCallback callback, String snapshotID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String volumeV3ServiceURL = sharedPreferences.getString("volumeV3ServiceURL", "Error Getting volumeV3ServiceURL");
        String partURL = "/snapshots/";
        String fullURL = volumeV3ServiceURL + partURL + snapshotID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DeleteVolumeSnapshotModel deleteVolumeSnapshotModel = new DeleteVolumeSnapshotModel();
        StringRequest stringRequest = deleteVolumeSnapshotModel.deleteVolumeSnapshot(fullURL, token,mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * edit an existing volume snapshot
     *
     * @param callback
     * @param name
     * @param description
     * @param volumeSnapshotid
     */
    public void editVolumeSnapshot(final VolleyCallback callback, String name, String description, String volumeSnapshotid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String volumeV3ServiceURL = sharedPreferences.getString("volumeV3ServiceURL", "Error Getting volumeV3ServiceURL");
        String partURL = "/snapshots/";
        String fullURL = volumeV3ServiceURL + partURL + volumeSnapshotid;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name", name);
            json2.put("description", description);
            json1.put("snapshot", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        EditVolumeSnapshotModel editVolumeSnapshotModel = new EditVolumeSnapshotModel();
        JsonObjectRequest jsonObjectRequest = editVolumeSnapshotModel.editVolumeSnapshot(fullURL, token,json1,mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * edit an alarm
     *
     * @param callback
     * @param name
     * @param description
     * @param type
     * @param metric
     * @param threshold
     * @param operator
     * @param granularity
     * @param state
     * @param severity
     */
    public void createAlarm(final VolleyCallback callback, String name, String description, String type,
                            String metric, int threshold, String method, String operator, int granularity, String state,
                            String severity) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String alarmServiceURL = sharedPreferences.getString("alarmingServiceURL", "Error Getting alarmServiceURL");
        String partURL = "/v2/alarms";
        String fullURL = alarmServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();

        try {
            json1.put("name", name);
            json1.put("description", description);
            json1.put("type", type);
            json2.put("metric", metric);
            json2.put("resource_id", "INSTANCE_ID");
            json2.put("resource_type", "instance");
            json2.put("threshold", threshold);
            json2.put("aggregation_method", method);
            json2.put("comparison_operator", operator);
            json2.put("granularity", granularity);
            json2.put("evaluation_periods", 3);
            json1.put("gnocchi_resources_threshold_rule", json2);
            json1.put("state", state);
            json1.put("severity", severity);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        CreateAlarmModel createAlarmModel = new CreateAlarmModel();
        JsonObjectRequest jsonObjectRequest = createAlarmModel.createAlarm(fullURL,  token,json1,mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);


    }

    public void deleteAlarm(final VolleyCallback callback, String alarmID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String alarmingServiceURL = sharedPreferences.getString("alarmingServiceURL", "Error Getting Compute URL");
        String partURL = "/v2/alarms/";
        String fullURL = alarmingServiceURL + partURL + alarmID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DeleteAlarmModel deleteAlarmModel = new DeleteAlarmModel();
        StringRequest stringRequest = deleteAlarmModel.deleteAlarm(fullURL, token,mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void listContainer(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting objectStorageServiceURL");
        String partURL = "?format=json";
        String fullURL = objectStorageServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListContainerModel listContainerModel = new ListContainerModel();
        StringRequest stringRequest = listContainerModel.listContainer(fullURL, token,mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void publicContainer(final VolleyCallback callback, String containerName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting objectStorageServiceURL");
        String partURL = "/";
        String fullURL = objectStorageServiceURL + partURL + containerName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        PublicContainerModel publicContainerModel = new PublicContainerModel();
        StringRequest stringRequest = publicContainerModel.publicContainer(fullURL, token,mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);


    }

    public void privateContainer(final VolleyCallback callback, String containerName) {

        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting objectStorageServiceURL");
        String partURL = "/";
        String fullURL = objectStorageServiceURL + partURL + containerName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        PrivateContainerModel privateContainerModel = new PrivateContainerModel();
        StringRequest stringRequest = privateContainerModel.privateContainer(fullURL, token,mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);


    }

    public void deleteContainer(final VolleyCallback callback, String containerName) {

        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting objectStorageServiceURL");
        String partURL = "/";
        String fullURL = objectStorageServiceURL + partURL + containerName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DeleteContainerModel deleteContainerModel = new DeleteContainerModel();
        StringRequest stringRequest = deleteContainerModel.deleteContainer(fullURL, token,mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);


    }

    public void listObject(final VolleyCallback callback, final Context context, String containerName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting objectStorageServiceURL");
        String partURL1 = "/";
        String partURL2 = "?format=json";
        String fullURL = objectStorageServiceURL + partURL1 + containerName + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListObjectModel listObjectModel = new ListObjectModel();
        StringRequest stringRequest = listObjectModel.listObject(fullURL, token,mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void deleteObject(final VolleyCallback callback, String containerName, String ObjectName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting objectStorageServiceURL");
        String partURL = "/";
        String fullURL = objectStorageServiceURL + partURL + containerName + partURL + ObjectName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DeleteObjectModel deleteObjectModel = new DeleteObjectModel();
        StringRequest stringRequest = deleteObjectModel.deleteObject(fullURL, token,mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void createFolder(final VolleyCallback callback, String containerName, String ObjectName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting objectStorageServiceURL");
        String partURL = "/";
        String fullURL = objectStorageServiceURL + partURL + containerName + partURL + ObjectName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        CreateFolderModel createFolderModel = new CreateFolderModel();
        StringRequest stringRequest = createFolderModel.createFolder(fullURL, token,mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);

    }

    public void createContainer(final VolleyCallback callback, String containerName, String access) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting Compute URl");
        String partURL = "/";
        String fullURL = objectStorageServiceURL + partURL + containerName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        final String accessValue;
        if (access.equals("Private")) {
            accessValue = "";
        } else {
            accessValue = ".r:*";
        }
        CreateContainerModel createContainerModel = new CreateContainerModel();
        StringRequest stringRequest = createContainerModel.createContainer(fullURL, token,accessValue,mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);

    }

    public void createObjectFile(final VolleyCallback callback, String containerName, String ObjectName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting Compute URl");
        String partURL = "/";
        String fullURL = objectStorageServiceURL + partURL + containerName + partURL + ObjectName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        CreateObjectFileModel createObjectFileModel = new CreateObjectFileModel();
        StringRequest stringRequest = createObjectFileModel.createObjectFile(fullURL, token,mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);

    }

    public void copyObject(final VolleyCallback callback, final String preContainer, final String preObjectName, String destionationContainer, String path, String newObjectName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL", "Error Getting Compute URl");
        String partURL = "/";
        String fullURL = objectStorageServiceURL + partURL + destionationContainer + partURL + path + newObjectName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        CopyObjectModel copyObjectModel = new CopyObjectModel();
        StringRequest stringRequest = copyObjectModel.copyObject(fullURL,token,preContainer, preObjectName, mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);

    }

    public void listFloatingIP(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "v2.0/floatingips";
        String fullURL = networkServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListFloatingIPModel listFloatingIPModel = new ListFloatingIPModel();
        StringRequest stringRequest = listFloatingIPModel.listFloatingIP(fullURL, token,mApplicationContext,callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void deleteFloatingIP(final VolleyCallback callback, String floatingID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "v2.0/floatingips/";
        String fullURL = networkServiceURL + partURL + floatingID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DeleteFloatingIPModel deleteFloatingIPModel = new DeleteFloatingIPModel();
        StringRequest stringRequest = deleteFloatingIPModel.deleteFloatingIP(fullURL, token,mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void createFloatingIP(final VolleyCallback callback, String floating_network_id) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "v2.0/floatingips";
        String fullURL = networkServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();

        try {
            json2.put("floating_network_id", floating_network_id);
            json1.put("floatingip", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CreateFloatingIPModel createFloatingIPModel = new CreateFloatingIPModel();
        JsonObjectRequest jsonObjectRequest = createFloatingIPModel.createFloatingIP(fullURL,token,json1, mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    public void listRouter(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "v2.0/routers";
        String fullURL = networkServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListRouterModel listRouterModel = new ListRouterModel();
        StringRequest stringRequest = listRouterModel.listRouter(fullURL, token,mApplicationContext,callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void deleteRouter(final VolleyCallback callback, String routerID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        String fullURL = networkServiceURL + "v2.0/routers/" + routerID;

        DeleteRouterModel deleteRouterModel = new DeleteRouterModel();
        StringRequest stringRequest = deleteRouterModel.deleteRouter(fullURL,token,mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void createRouter(final VolleyCallback callback, String routerName, String networkID, boolean admin_state) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = networkServiceURL + "v2.0/routers";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONObject json3 = new JSONObject();

        try {
            json3.put("network_id", networkID);
            json2.put("external_gateway_info", json3);
            json2.put("name", routerName);
            json2.put("admin_state_up", admin_state);
            json1.put("router", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CreateRouterMdoel createRouterMdoel = new CreateRouterMdoel();
        JsonObjectRequest jsonObjectRequest = createRouterMdoel.createRouter(fullURL,token, json1, mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }

    public void listNetwork(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "v2.0/networks";
        String fullURL = networkServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListNetworkModel listNetworkModel = new ListNetworkModel();
        StringRequest stringRequest = listNetworkModel.listNetwork(fullURL,token,mApplicationContext, callback,context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void listSubnet(final VolleyCallback callback, final Context context, final String networkID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "v2.0/subnets";
        String fullURL = networkServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListSubnetModel listSubnetModel = new ListSubnetModel();
        StringRequest stringRequest = listSubnetModel.listSubnet(fullURL, token, networkID, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void deleteNetwork(final VolleyCallback callback, String networkID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "v2.0/networks/";
        String fullURL = networkServiceURL + partURL + networkID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DeleteNetworkModel deleteNetworkModel = new DeleteNetworkModel();
        StringRequest stringRequest = deleteNetworkModel.deleteNetwork(fullURL, token, mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void createNetwork(final VolleyCallback callback, String networkName, boolean admin_state) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "v2.0/networks";
        String fullURL = networkServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {

            json2.put("name", networkName);
            json2.put("admin_state_up", admin_state);
            json1.put("network", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CreateNetworkModel createNetworkModel= new CreateNetworkModel();
        JsonObjectRequest jsonObjectRequest = createNetworkModel.createNetwork(fullURL, token, json1, mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }

    public void deleteSubnet(final VolleyCallback callback, String SubnetID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "v2.0/subnets/";
        String fullURL = networkServiceURL + partURL + SubnetID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DeleteSubnetModel deleteSubnetModel = new DeleteSubnetModel();
        StringRequest stringRequest = deleteSubnetModel.deleteSubnet(fullURL, token, mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void createSubnet(final VolleyCallback callback, String subnetName, String networkID, String networkAddress, int version) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "v2.0/subnets";
        String fullURL = networkServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {

            json2.put("name", subnetName);
            json2.put("cidr", networkAddress);
            json2.put("ip_version", version);
            json2.put("network_id", networkID);
            json1.put("subnet", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CreateSubnetModel createSubnetModel = new CreateSubnetModel();
        JsonObjectRequest jsonObjectRequest = createSubnetModel.createSubnet(fullURL, token, json1, mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }


    ////////////////////////
    // test for update


    /*
     * list ports
     * */
    public void listPort(final VolleyCallback callback, final Context context, final String networkID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "v2.0/ports";
        String fullURL = networkServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListPortModel listPortModel = new ListPortModel();
        StringRequest stringRequest = listPortModel.ListPortModel(fullURL,token, networkID,  mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /*
     * delete port
     * */
    public void deletePort(final VolleyCallback callback, String portID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "v2.0/ports/";
        String fullURL = networkServiceURL + partURL + portID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DeletePortModel deletePortModel = new DeletePortModel();
        StringRequest stringRequest = deletePortModel.deletePort(fullURL,token, mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /*
     * create port
     * */
    public void createPort(final VolleyCallback callback, String portName, String networkID, boolean admin_state) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "v2.0/ports";
        String fullURL = networkServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();


        try {
            json2.put("name", portName);
            json2.put("admin_state_up", admin_state);
            json2.put("network_id", networkID);
            json1.put("port", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CreatePortModel createPortModel = new CreatePortModel();
        JsonObjectRequest jsonObjectRequest = createPortModel.createPort(fullURL,token, json1,  mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }



    // show resource type detail
    public void showResourceTypesDetail(final VolleyCallback callback, final Context context, String id) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "Error Getting Compute URL");
        String partURL = "/resource_types/";
        String fullURL = orchestrationServiceURL + partURL + id;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ShowResourceTypesDetailModel showResourceTypesDetailModel = new ShowResourceTypesDetailModel();
        StringRequest stringRequest = showResourceTypesDetailModel.showResourceTypesDetail(fullURL,token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /////////////////////////////////////////
    //////list resource types
    public void listResourceTypes(final VolleyCallback callback, final Context context)   {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "ERROR");
        String partURL = "/resource_types";
        String fullURL = orchestrationServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListResourceTypesModel listResourceTypesModel = new ListResourceTypesModel();
        StringRequest stringRequest = listResourceTypesModel.listResourceTypes(fullURL,token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /////////////////
    // show template versions

    public void listTemplateVersions(final VolleyCallback callback, final Context context)     {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "ERROR");
        String partURL = "/template_versions";
        String fullURL = orchestrationServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListTemplateVersionsModel listTemplateVersionsModel = new ListTemplateVersionsModel();
        StringRequest stringRequest = listTemplateVersionsModel.listTemplateVersions(fullURL,token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    ////////////////////
    // show template version detail
    public void showTemplateVersionDetail(final VolleyCallback callback, final Context context, String id) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "Error Getting Compute URL");
        String partURL1 = "/template_versions/";
        String partURL2 = "/functions";
        String fullURL = orchestrationServiceURL + partURL1 + id + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ShowTemplateVersionDetailModel showTemplateVersionDetailModel = new ShowTemplateVersionDetailModel();
        StringRequest stringRequest = showTemplateVersionDetailModel.showTemplateVersionDetail(fullURL,token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /////////////////////
    //show stacks list
    public void listStacks(final VolleyCallback callback, final Context context)     {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "ERROR");
        String partURL = "/stacks";
        String fullURL = orchestrationServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListStacksModel listStacksModel = new ListStacksModel();
        StringRequest stringRequest = listStacksModel.listStacks(fullURL,token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    ////////////////////////
    // list stack detail and actions
    public void listSingleStack(final VolleyCallback callback, final Context context, String stackName, String stackId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "Error Getting Compute URL");
        String partURL1 = "/stacks/";
        String partURL2 = "/";
        String fullURL = orchestrationServiceURL + partURL1 + stackName + partURL2 +stackId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListSingleStackModel listSingleStackModel = new ListSingleStackModel();
        StringRequest stringRequest = listSingleStackModel.listSingleStack(fullURL,token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /////////////////////
    //suspend stack
    public void suspendStack(final VolleyCallback callback, String stackName, String stackId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "Error Getting Compute URL");
        String partURL1 = "/stacks/";
        String partURL2 = "/";
        String partURL3 = "/actions";
        String fullURL = orchestrationServiceURL + partURL1 + stackName + partURL2 +stackId + partURL3;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json = new JSONObject();
        try {
            json.put("suspend", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SuspendStackModel suspendStackModel = new SuspendStackModel();
        JsonObjectRequest jsonObjectRequest = suspendStackModel.suspendStack(fullURL,token, json,  mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    ///////////////
    // resume stack
    public void resumeStack(final VolleyCallback callback, String stackName, String stackId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "Error Getting Compute URL");
        String partURL1 = "/stacks/";
        String partURL2 = "/";
        String partURL3= "/actions";
        String fullURL = orchestrationServiceURL + partURL1 + stackName + partURL2 +stackId + partURL3;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("resume", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ResumeModel resumeModel = new ResumeModel();
        JsonObjectRequest jsonObjectRequest = resumeModel.resume(fullURL,token, json,  mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    //////////////////
    // check stack
    public void checkStack(final VolleyCallback callback, String stackName, String stackId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "Error Getting Compute URL");
        String partURL1 = "/stacks/";
        String partURL2 = "/";
        String partURL3 = "/actions";
        String fullURL = orchestrationServiceURL + partURL1 + stackName + partURL2 +stackId + partURL3;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        CheckStackModel checkStackModel = new CheckStackModel();
        JsonObjectRequest jsonObjectRequest = checkStackModel.checkStack(fullURL,token,  mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    //////////////////
    // delete stack
    public void deleteStack(final VolleyCallback callback, String stackName, String stackId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "Error Getting Compute URL");
        String partURL1 = "/stacks/";
        String partURL2 = "/";
        String fullURL = orchestrationServiceURL + partURL1 + stackName + partURL2 +stackId ;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DeleteStackModel deleteStackModel = new DeleteStackModel();
        StringRequest stringRequest = deleteStackModel.deleteStack(fullURL,token,  mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }



    ////////////////////
    // create stack
    public void createStack(final VolleyCallback callback, String stackName, String templateSource, Integer timeoutMins) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String orchestrationServiceURL = sharedPreferences.getString("orchestrationServiceURL", "Error Getting Compute URL");
        String partURL = "/stacks";
        String fullURL = orchestrationServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("stack_name", stackName);
            json.put("template_url", templateSource);
            json.put("timeout_mins", timeoutMins);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CreateStackModel createStackModel = new CreateStackModel();
        JsonObjectRequest jsonObjectRequest = createStackModel.createStack(fullURL,token, json,  mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }

    ///////////////////
    // list database instance
    public void listDatabaseInstances(final VolleyCallback callback, final Context context)     {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "ERROR");
        String partURL = "/instances";
        String fullURL = databaseServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListDatabaseInstancesModel listDatabaseInstancesModel = new ListDatabaseInstancesModel();
        StringRequest stringRequest = listDatabaseInstancesModel.listDatabaseInstances(fullURL, token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    // list database instance detail and actions
    public void listSingleDatabaseInstance(final VolleyCallback callback, final Context context, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL = "/instances/";
        String fullURL = databaseServiceURL + partURL + instanceId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListSingleDatabaseInstanceModel listSingleDatabaseInstanceModel = new ListSingleDatabaseInstanceModel();
        StringRequest stringRequest = listSingleDatabaseInstanceModel.listSingleDatabaseInstance(fullURL, token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }



    /////////////////
    // restart database instance volume
    public void databaseInstanceRestart(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL1 = "/instances/";
        String partURL2 = "/action";
        String fullURL = databaseServiceURL + partURL1 + instanceId + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        JSONObject json1 = new JSONObject();
        try {
            json.put("restart", json1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DatabaseInstanceRestartModel databaseInstanceRestartModel = new DatabaseInstanceRestartModel();
        JsonObjectRequest jsonObjectRequest = databaseInstanceRestartModel.databaseInstanceRestart(fullURL,token, json,  mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /*
     * resize database instance volume
     * */
    public void resizeDatabaseInstanceVolume(final VolleyCallback callback, int newSize, String databaseInstanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL1 = "/instances/";
        String partURL2 = "/action";
        String fullURL = databaseServiceURL + partURL1 + databaseInstanceId + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONObject json3 = new JSONObject();
        try {
            json2.put("size", newSize);
            json1.put("volume", json2);
            json3.put("resize", json1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ResizeDatabaseInstanceVolumeModel resizeDatabaseInstanceVolumeModel = new ResizeDatabaseInstanceVolumeModel();
        JsonObjectRequest jsonObjectRequest = resizeDatabaseInstanceVolumeModel.resizeDatabaseInstanceVolume(fullURL,token, json3,  mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /*
     * attach configuration group to database instance
     * */
    public void attachConfigGroup(final VolleyCallback callback, String databaseInstanceId, String configGroupId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partToken = "/instances/";
        String fullURL = databaseServiceURL + partToken + databaseInstanceId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("configuration", configGroupId);
            json1.put("instance", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AttachConfigGroupModel attachConfigGroupModel = new AttachConfigGroupModel();
        JsonObjectRequest jsonObjectRequest = attachConfigGroupModel.attachConfigGroup(fullURL,token, json1,  mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /*
     * detach configuration group
     * */
    public void detachConfigGroup(final VolleyCallback callback, String databaseInstanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL =  "/instances/";
        String fullURL = databaseServiceURL + partURL + databaseInstanceId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json1.put("instance", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DetachConfigGroupModel detachConfigGroupModel = new DetachConfigGroupModel();
        JsonObjectRequest jsonObjectRequest = detachConfigGroupModel.detachConfigGroup(fullURL,token, json1,  mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /*
     * delete database instance
     * */
    public void deleteDatabaseInstance(final VolleyCallback callback, String databaseInstanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL = "/instances/";
        String fullURL = databaseServiceURL + partURL + databaseInstanceId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DeleteDatabaseInstanceModel deleteDatabaseInstanceModel = new DeleteDatabaseInstanceModel();
        StringRequest stringRequest = deleteDatabaseInstanceModel.deleteDatabaseInstance(fullURL,token, mApplicationContext,  callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /*
     * create database instance
     * */
    public void createdatabaseInstance(final VolleyCallback callback, String setName, String availabilityZone, String datastoreVersion, String datastoreType, int volumeSize, String locality, String flavorRef) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL = "/instances";
        String fullURL = databaseServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONObject json3 = new JSONObject();
        JSONObject json4 = new JSONObject();
        JSONObject json5 = new JSONObject();
        JSONObject json6 = new JSONObject();
        Integer temp = 1;

        try {
            json2.put("name",setName);
            json2.put("flavorRef", flavorRef);
            json2.put("availability_zone", availabilityZone);
            json4.put("version", datastoreVersion);
            json4.put("type", datastoreType);
            json2.put("datastore",json4);
            json1.put("size", volumeSize);
            json2.put("volume",json1);

            if (locality != "None")
            {
                json2.put("locality", locality);
            }
            json5.put("instance", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CreatedatabaseInstanceModel createdatabaseInstanceModel = new CreatedatabaseInstanceModel();
        JsonObjectRequest jsonObjectRequest = createdatabaseInstanceModel.createdatabaseInstance(fullURL,token, json5,  mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }


    /*
     * list configuration group and action
     * */
    public void listConfigGroup(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL = "/configurations";
        String fullURL = databaseServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListConfigGroupModel listConfigGroupModel = new ListConfigGroupModel();
        StringRequest stringRequest = listConfigGroupModel.listConfigGroup(fullURL, token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    //////////////////
    // delete configuration group
    public void deleteConfigGroup(final VolleyCallback callback, String configGroupId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL = "/configurations/";
        String fullURL = databaseServiceURL + partURL + configGroupId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        callback.onSuccess("success");

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Delete  successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Delete failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        DeleteConfigGrouModel deleteConfigGrouModel = new DeleteConfigGrouModel();
        StringRequest stringRequest1 = deleteConfigGrouModel.deleteConfigGrou(fullURL, token, mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    //////////
    // show configuration group detail
    public void showConfigGroupDetail(final VolleyCallback callback, final Context context, String configGroupId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL = "/configurations/";
        String fullURL = databaseServiceURL + partURL + configGroupId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ShowConfigGroupDetailModel showConfigGroupDetailModel = new ShowConfigGroupDetailModel();
        StringRequest stringRequest = showConfigGroupDetailModel.showConfigGroupDetail(fullURL, token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
     * list configuration group instance
     * */
    public void listConfigGroupInstances(final VolleyCallback callback, final Context context, String configGroupId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL1 = "/configurations/";
        String partURL2 = "/instances";
        String fullURL = databaseServiceURL + partURL1 + configGroupId + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListConfigGroupInstancesModel listConfigGroupInstancesModel = new ListConfigGroupInstancesModel();
        StringRequest stringRequest = listConfigGroupInstancesModel.listConfigGroupInstances(fullURL, token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /*
     * create configuration group
     * */
    public void createConfigGroup(final VolleyCallback callback, String setName, String selectDatastoreName, String setDescription) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/configurations";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONObject json3 = new JSONObject();
        JSONObject json4 = new JSONObject();
        String newDatastore = selectDatastoreName.toLowerCase();

        try {
            json1.put("type", newDatastore);
            json2.put("datastore",json1);
            json2.put("values", json3);
            json2.put("name",setName);
            if (setDescription != null)
            {
                json2.put("description", setDescription);
            }
            json4.put("configuration",json2);

            Log.d("createConfig", json4.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CreateConfigGroupModel createConfigGroupModel = new CreateConfigGroupModel();
        JsonObjectRequest jsonObjectRequest = createConfigGroupModel.createConfigGroup(fullURL,token,json4, mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }


    /*
     * list data store
     * */
    public void listDatastores(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL = "/datastores";
        String fullURL = databaseServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListDatastoresModel listDatastoresModel = new ListDatastoresModel();
        StringRequest stringRequest = listDatastoresModel.listDatastores(fullURL, token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
     * list flavor
     * */
    public void listDatabaselistDatabaseFlavor(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String fullURL = databaseServiceURL + "/flavors";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListDatabaselistDatabaseFlavorModel listDatabaselistDatabaseFlavorModel = new ListDatabaselistDatabaseFlavorModel();
        StringRequest stringRequest = listDatabaselistDatabaseFlavorModel.listDatabaselistDatabaseFlavor(fullURL, token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
     * list database backup
     * */
    public void listDatabaseBackup(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL = "/backups";
        String fullURL = databaseServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListDatabaseBackupModel listDatabaseBackupModel = new ListDatabaseBackupModel();
        StringRequest stringRequest = listDatabaseBackupModel.listDatabaseBackup(fullURL, token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
     * delete database backup
     * */
    public void deleteDatabaseBackup(final VolleyCallback callback, String backupId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL = "/backups/";
        String fullURL = databaseServiceURL + partURL + backupId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DeleteDatabaseBackupModel deleteDatabaseBackupModel = new DeleteDatabaseBackupModel();
        StringRequest stringRequest = deleteDatabaseBackupModel.deleteDatabaseBackup(fullURL, token, mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
     * show database backup detail
     * */
    public void showDatabaseBackupDetail(final VolleyCallback callback, final Context context, String backupId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL = "/backups/";
        String fullURL = databaseServiceURL + partURL + backupId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ShowDatabaseBackupDetailModel showDatabaseBackupDetailModel = new ShowDatabaseBackupDetailModel();
        StringRequest stringRequest = showDatabaseBackupDetailModel.showDatabaseBackupDetail(fullURL, token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
     * create instance backup
     * */
    public void createInstanceBackup(final VolleyCallback callback, String setName, String selectInstanceId, String setDescription) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL = "/backups";
        String fullURL = databaseServiceURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json2 = new JSONObject();
        JSONObject json4 = new JSONObject();
        Integer temp = 0;

        try {
            json2.put("incremental",temp);
            json2.put("instance", selectInstanceId);
            json2.put("name",setName);
            if (setDescription != null)
            {
                json2.put("description", setDescription);
            }
            json4.put("backup",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CreateInstanceBackupModel createInstanceBackupModel = new CreateInstanceBackupModel();
        JsonObjectRequest jsonObjectRequest = createInstanceBackupModel.createInstanceBackup(fullURL,token,json4, mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }

    /*
     * show root manage detail
     * */
    public void showManageRootDetail(final VolleyCallback callback, final Context context, String databaseInstanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL1 = "/instances/";
        String partURL2 = "/root";
        String fullURL = databaseServiceURL + partURL1 + databaseInstanceId + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ShowManageRootDetailModel showManageRootDetailModel = new ShowManageRootDetailModel();
        StringRequest stringRequest = showManageRootDetailModel.showManageRootDetail(fullURL, token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /*
     * enable instance root
     * */
    public void enableRoot(final VolleyCallback callback, final Context context, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL1 = "/instances/";
        String partURL2 = "/root";
        String fullURL = databaseServiceURL + partURL1 + instanceId + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        EnableRootModel enableRootModel = new EnableRootModel();
        StringRequest stringRequest = enableRootModel.enableRoot(fullURL, token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
     * disable instance root
     * */
    public void disableRoot(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String databaseServiceURL = sharedPreferences.getString("databaseServiceURL", "Error Getting Compute URL");
        String partURL1 = "/instances/";
        String partURL2 = "/root";
        String fullURL = databaseServiceURL + partURL1+ instanceId + partURL2;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        DisableRootModel disableRootModel = new DisableRootModel();
        StringRequest stringRequest = disableRootModel.disableRoot(fullURL, token, mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /*
     * edit router
     * */
    public void editRouter(final VolleyCallback callback, String editRouterName, boolean admin_state, String routerID, String routerName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String partURL = "v2.0/routers/";
        String fullURL = networkServiceURL + partURL + routerID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();

        try {
            if (editRouterName.equals("")) {
                json2.put("name", routerName);
            }
            else {
                json2.put("name", editRouterName);
            }
            json2.put("admin_state_up", admin_state);
            json1.put("router", json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        EditRouterModel editRouterModel = new EditRouterModel();
        JsonObjectRequest jsonObjectRequest = editRouterModel.editRouter(fullURL, token, json1, mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }

    /*
    Contariner Service
     **/
    public void listCluster(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String containerInfraURL = sharedPreferences.getString("containerInfraURL", "Error Getting Compute URL");
        String partURL = "/clusters";
        String fullURL = containerInfraURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListClusterModel listClusterModel = new ListClusterModel();
        StringRequest stringRequest = listClusterModel.listCluster(fullURL, token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * Get the detailed info of a specific volume
     *
     * @param callback
     * @param context
     * @param clusterid
     */

    public void showClusterDetail(final VolleyCallback callback, final Context context, String clusterid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String volumeV3ServiceURL = sharedPreferences.getString("containerInfraURL", "Error Getting Volume URL v3");
        String partURL = "/clusters/";
        String fullURL = volumeV3ServiceURL + partURL + clusterid;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ShowClusterDetailModel showClusterDetailModel = new ShowClusterDetailModel();
        StringRequest stringRequest = showClusterDetailModel.showClusterDetail(fullURL, token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void deletecluster(final VolleyCallback callback, String clusterID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String containerInfraURL = sharedPreferences.getString("containerInfraURL", "Error Getting Compute URL");
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        String fullURL = containerInfraURL + "/clusters/" + clusterID;
        DeleteclusterModel deleteclusterModel = new DeleteclusterModel();
        StringRequest stringRequest = deleteclusterModel.deletecluster(fullURL, token, mApplicationContext, callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }



    /*
    Create a new Container Cluster.
    **/
    public void createCluster(final VolleyCallback callback, String clusterName, String disUrl, String keyPair,String clusterTemplateID,  String nodeFlavorID, String masterFlavorID,int dockerSize,int masterCount, int nodeCount, int createTimeout) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String containerInfraURL = sharedPreferences.getString("containerInfraURL", "Error Getting Compute URL");
        String partURL = "/clusters";
        String fullURL = containerInfraURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        try {
            json1.put("name", clusterName);
            System.out.println("name: " + clusterName);
            json1.put("discovery_url", disUrl);
            System.out.println("discovery_url: " + disUrl);
            json1.put("master_count", masterCount);
            System.out.println("master_count: " + masterCount);
            json1.put("cluster_template_id", clusterTemplateID);
            System.out.println("cluster_template_id: " + clusterTemplateID);
            json1.put("node_count", nodeCount);
            System.out.println("node_count: " + nodeCount);
            json1.put("docker_volume_size", dockerSize);
            System.out.println("docker_volume_size: " + dockerSize);
            json1.put("create_timeout", createTimeout);
            System.out.println("create_timeout: " + createTimeout);
            json1.put("keypair", keyPair);
            System.out.println("keypair: " + keyPair);
            json1.put("master_flavor_id", masterFlavorID);
            System.out.println("master_flavor_id: " + masterFlavorID);
            json1.put("flavor_id", nodeFlavorID);
            System.out.println("flavor_id: " + nodeFlavorID);
            json1.put("labels", null);
            System.out.println("labels: " + null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CreateClusterModel createClusterModel = new CreateClusterModel();
        JsonObjectRequest jsonObjectRequest = createClusterModel.createCluster(fullURL, token, json1, mApplicationContext,callback);
        Network.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }

    /**
     * List flavor Http Request showing the available cluster Template
     *
     * @param callback
     * @param context
     */

    public void listClusterTemplate(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String containerInfraURL = sharedPreferences.getString("containerInfraURL", "Error Getting Compute URL");
        String partURL = "/clustertemplates";
        String fullURL = containerInfraURL + partURL;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ListClusterTemplateModel listClusterTemplateModel = new ListClusterTemplateModel();
        StringRequest stringRequest = listClusterTemplateModel.listClusterTemplate(fullURL, token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * Get the detailed info of a specific volume
     *
     * @param callback
     * @param context
     * @param uuid
     */

    public void showClusterTemplateDetail(final VolleyCallback callback, final Context context, String uuid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String containerInfraURL = sharedPreferences.getString("containerInfraURL", "Error Getting Volume URL v3");
        String partURL = "/clustertemplates/";
        String fullURL = containerInfraURL + partURL + uuid;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        ShowClusterTemplateDetailModel showClusterTemplateDetailModel = new ShowClusterTemplateDetailModel();
        StringRequest stringRequest = showClusterTemplateDetailModel.showClusterTemplateDetail(fullURL, token, mApplicationContext, callback, context);
        Network.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    ///////////////////////////////////////
// 将字符串写入到文本文件中
    public void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    // 生成文件
    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }


    }

    public static boolean createFile(File fileName)throws Exception{
        boolean flag=false;
        try{
            if(!fileName.exists()){
                fileName.createNewFile();
                flag=true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }


}
