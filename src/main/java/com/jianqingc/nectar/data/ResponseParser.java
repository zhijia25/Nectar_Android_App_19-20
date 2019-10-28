package com.jianqingc.nectar.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jianqing Chen on 2016/10/1.
 * The ResponseParser is designed to parse the response of Login, List Volumes and List Instances HTTP requests.
 * Store the response into the SharedPreferences after parsing.
 */
public class ResponseParser {
    private static ResponseParser mInstance;
    private Context mApplicationContext;
    private String tenant_id;


    public static ResponseParser getInstance(Context context) {
        if (mInstance == null)
            mInstance = new ResponseParser(context);
        return mInstance;
    }
    public ResponseParser(Context context){
        this.mApplicationContext = context.getApplicationContext();
    }

    public void loginParser(JSONObject loinHeader, JSONObject loginBody){
        String tokenId = null;
        String expires = null;
        String tenantName = null;
        String tenantId = null;
        String tenantDescription = null;
        String username = null;
        String dnsServiceURL = null;
        String computeServiceURL = null;
        String networkServiceURL = null;
        String volumeV3ServiceURL = null;
        String s3ServiceURL = null;
        String alarmingServiceURL = null;
        String imageServiceURL = null;
        String meteringServiceURL = null;
        String cloudformationServiceURL = null;
        String applicationCatalogServiceURL = null;
        String volumeV2ServiceURL = null;
        String ec2ServiceURL = null;
        String orchestrationServiceURL = null;
        String objectStorageServiceURL = null;
        String databaseServiceURL = null;
        String containerInfraURL = null;
        try {
            tokenId = loinHeader.getString("X-Subject-Token");
            JSONObject tokenInfo= loginBody.getJSONObject("token");
            JSONArray serviceCatalog = tokenInfo.getJSONArray("catalog");
            JSONObject user = tokenInfo.getJSONObject("user");
            JSONObject project = tokenInfo.getJSONObject("project");
            expires = tokenInfo.getString("expires_at");
            tenantName= project.getString("name");
            tenantId = project.getString("id");
            tenant_id = tenantId;
            username = user.getString("name");

            dnsServiceURL = serviceCatalog.getJSONObject(18).getJSONArray("endpoints").getJSONObject(2).getString("url");
            computeServiceURL = serviceCatalog.getJSONObject(15).getJSONArray("endpoints").getJSONObject(2).getString("url");
            networkServiceURL = serviceCatalog.getJSONObject(10).getJSONArray("endpoints").getJSONObject(2).getString("url");
            volumeV3ServiceURL = serviceCatalog.getJSONObject(1).getJSONArray("endpoints").getJSONObject(2).getString("url");
            s3ServiceURL = serviceCatalog.getJSONObject(2).getJSONArray("endpoints").getJSONObject(2).getString("url");
            alarmingServiceURL = serviceCatalog.getJSONObject(13).getJSONArray("endpoints").getJSONObject(2).getString("url");
            imageServiceURL = serviceCatalog.getJSONObject(6).getJSONArray("endpoints").getJSONObject(2).getString("url");
            objectStorageServiceURL = serviceCatalog.getJSONObject(17).getJSONArray("endpoints").getJSONObject(2).getString("url");
            meteringServiceURL = serviceCatalog.getJSONObject(8).getJSONArray("endpoints").getJSONObject(2).getString("url");
            cloudformationServiceURL = serviceCatalog.getJSONObject(3).getJSONArray("endpoints").getJSONObject(2).getString("url");
            applicationCatalogServiceURL = serviceCatalog.getJSONObject(0).getJSONArray("endpoints").getJSONObject(2).getString("url");
            volumeV2ServiceURL = serviceCatalog.getJSONObject(4).getJSONArray("endpoints").getJSONObject(2).getString("url");
            ec2ServiceURL = serviceCatalog.getJSONObject(7).getJSONArray("endpoints").getJSONObject(2).getString("url");
            orchestrationServiceURL = serviceCatalog.getJSONObject(12).getJSONArray("endpoints").getJSONObject(2).getString("url");
            databaseServiceURL = serviceCatalog.getJSONObject(20).getJSONArray("endpoints").getJSONObject(2).getString("url");
            containerInfraURL = serviceCatalog.getJSONObject(5).getJSONArray("endpoints").getJSONObject(2).getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPreferences =  mApplicationContext.getSharedPreferences("nectar_android", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("tokenId", tokenId);
        editor.putString("expires",expires);
        editor.putString("tenantName", tenantName);
        editor.putString("tenantId",tenantId);
        editor.putString("tenantDescription", tenantDescription);
        editor.putString("username", username);
        editor.putString("dnsServiceURL", dnsServiceURL);
        editor.putString("computeServiceURL", computeServiceURL);
        editor.putString("networkServiceURL", networkServiceURL);
        editor.putString("volumeV3ServiceURL", volumeV3ServiceURL);
        editor.putString("s3ServiceURL", s3ServiceURL);
        editor.putString("alarmingServiceURL", alarmingServiceURL);
        editor.putString("imageServiceURL", imageServiceURL);
        editor.putString("meteringServiceURL", meteringServiceURL);
        editor.putString("cloudformationServiceURL", cloudformationServiceURL);
        editor.putString("applicationCatalogServiceURL", applicationCatalogServiceURL);
        editor.putString("volumeV2ServiceURL", volumeV2ServiceURL);
        editor.putString("ec2ServiceURL", ec2ServiceURL);
        editor.putString("orchestrationServiceURL", orchestrationServiceURL);
        editor.putString("objectStorageServiceURL", objectStorageServiceURL);
        editor.putString("databaseServiceURL", databaseServiceURL);
        editor.putString("containerInfraURL", containerInfraURL);
        editor.apply();
        }


    public JSONArray listInstance(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray serverArray= responseJSON.getJSONArray("servers");
            for (int i=0;i<serverArray.length();i++){
                JSONObject instanceObject = (JSONObject) serverArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("instanceId", instanceObject.getString("id"));
                resultObject.put("instanceStatus", instanceObject.getString("status"));
                resultObject.put("instanceName", instanceObject.getString("name"));
                resultObject.put("keyName", instanceObject.getString("key_name"));
                resultObject.put("instanceCreatedTime", instanceObject.getString("created"));
                resultObject.put("instanceUpdatedTime", instanceObject.getString("updated"));
                resultObject.put("tenantID", instanceObject.getString("tenant_id"));
                resultObject.put("IPv4Address", instanceObject.getString("accessIPv4"));
                resultObject.put("zone", instanceObject.getString("OS-EXT-AZ:availability_zone"));
                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }


    public JSONArray listAlarm(String response){
        JSONArray resultArray=new JSONArray();
        try{
            JSONArray serverArray =new JSONArray(response);
            for(int i = 0 ; i < serverArray.length();i++)
            {
                JSONObject alarmObject = serverArray.getJSONObject(i);
                JSONObject thresholdRule =alarmObject.getJSONObject("gnocchi_resources_threshold_rule");
                JSONObject resultObject = new JSONObject();
                resultObject.put("AlarmName", alarmObject.getString("name"));
                resultObject.put("severity",alarmObject.getString("severity"));
                resultObject.put("state",alarmObject.getString("state"));
                resultObject.put("alarmID",alarmObject.getString("alarm_id"));
                resultObject.put("metric",thresholdRule.getString("metric"));
                resultObject.put("aggregation_method",thresholdRule.getString("aggregation_method"));
                resultObject.put("threshold",thresholdRule.getInt("threshold"));
                resultObject.put("granularity",thresholdRule.getInt("granularity"));
                resultObject.put("evaluation_periods",thresholdRule.getInt("evaluation_periods"));
                resultObject.put("description",alarmObject.getString("description"));
                resultObject.put("repeat_actions",alarmObject.getString("repeat_actions"));
                resultObject.put("resource_type",thresholdRule.getString("resource_type"));
                resultObject.put("comparison_operator",thresholdRule.getString("comparison_operator"));
                resultObject.put("type",alarmObject.getString("type"));
                resultArray.put(i,resultObject);

            }

            return resultArray;

        } catch(JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listcontainer(String response){
        JSONArray resultArray=new JSONArray();
        try{
            JSONArray serverArray =new JSONArray(response);

            for(int i = 0 ; i < serverArray.length();i++)
            {
                JSONObject containerObject = serverArray.getJSONObject(i);

                JSONObject resultObject = new JSONObject();
                resultObject.put("containerName", containerObject.getString("name"));
                resultObject.put("count",containerObject.getInt("count"));
                resultObject.put("bytes",(containerObject.getDouble("bytes")/1024.0/1024.0));
                resultArray.put(i,resultObject);
            }

            return resultArray;

        } catch(JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listObject(String response){
        JSONArray resultArray=new JSONArray();
        try{
            JSONArray serverArray =new JSONArray(response);



            for(int i = 0 ; i < serverArray.length();i++)
            {

                JSONObject Object = serverArray.getJSONObject(i);

                JSONObject resultObject = new JSONObject();
                resultObject.put("objectName", Object.getString("name"));
                resultObject.put("last_modified",Object.getString("last_modified"));

                resultArray.put(i,resultObject);

            }

            return resultArray;

        } catch(JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listImage(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray serverArray= responseJSON.getJSONArray("images");
            for (int i=0;i<serverArray.length();i++){
                JSONObject instanceObject = (JSONObject) serverArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("imageId", instanceObject.getString("id"));
                resultObject.put("imageStatus", instanceObject.getString("status"));
                resultObject.put("imageName", instanceObject.getString("name"));
                resultObject.put("imageOwner", instanceObject.getString("owner"));
                resultObject.put("imageVisibility", instanceObject.getString("visibility"));
                resultObject.put("imageProtected", instanceObject.getString("protected"));
                resultObject.put("imageChecksum", instanceObject.getString("checksum"));
                resultObject.put("imageCreatedTime", instanceObject.getString("created_at"));
                resultObject.put("imageUpdatedTime", instanceObject.getString("updated_at"));
                resultObject.put("imageSize", instanceObject.getString("size"));
                resultObject.put("imageDiskFormat", instanceObject.getString("disk_format"));
                resultObject.put("imageContainerFormat", instanceObject.getString("container_format"));
                resultObject.put("imageMinDisk", instanceObject.getString("min_disk"));
                resultObject.put("imageType", instanceObject.getString("image_type"));
                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listImageOfficial(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray serverArray= responseJSON.getJSONArray("images");
            for (int i=0;i<serverArray.length();i++){
                JSONObject instanceObject = (JSONObject) serverArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("imageId", instanceObject.getString("id"));
                resultObject.put("imageStatus", instanceObject.getString("status"));
                resultObject.put("imageName", instanceObject.getString("name"));
                resultObject.put("imageOwner", instanceObject.getString("owner"));
                resultObject.put("imageVisibility", instanceObject.getString("visibility"));
                resultObject.put("imageProtected", instanceObject.getString("protected"));
                resultObject.put("imageChecksum", instanceObject.getString("checksum"));
                resultObject.put("imageCreatedTime", instanceObject.getString("created_at"));
                resultObject.put("imageUpdatedTime", instanceObject.getString("updated_at"));
                resultObject.put("imageSize", instanceObject.getString("size"));
                resultObject.put("imageDiskFormat", instanceObject.getString("disk_format"));
                resultObject.put("imageContainerFormat", instanceObject.getString("container_format"));
                resultObject.put("imageMinDisk", instanceObject.getString("min_disk"));
                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONObject listImageDetail(String response){
        JSONObject resultObject = new JSONObject();
        try {
            JSONObject responseJSON = new JSONObject(response);

                resultObject.put("imageId", responseJSON.getString("id"));
                resultObject.put("imageStatus", responseJSON.getString("status"));
                resultObject.put("imageName", responseJSON.getString("name"));
                resultObject.put("imageOwner", responseJSON.getString("owner"));
                resultObject.put("imageVisibility", responseJSON.getString("visibility"));
                resultObject.put("imageProtected", responseJSON.getString("protected"));
                resultObject.put("imageChecksum", responseJSON.getString("checksum"));
                resultObject.put("imageCreatedTime", responseJSON.getString("created_at"));
                resultObject.put("imageUpdatedTime", responseJSON.getString("updated_at"));
                resultObject.put("imageSize", responseJSON.getString("size"));
                resultObject.put("imageDiskFormat", responseJSON.getString("disk_format"));
                resultObject.put("imageContainerFormat", responseJSON.getString("container_format"));
                resultObject.put("imageMinDisk", responseJSON.getString("min_disk"));

            return resultObject;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    public JSONArray listKeyPair(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray serverArray= responseJSON.getJSONArray("keypairs");
            for (int i=0;i<serverArray.length();i++){
                JSONObject instanceObject = (JSONObject) serverArray.get(i);
                JSONObject kpObject= instanceObject.getJSONObject("keypair");
                JSONObject resultObject = new JSONObject();
                resultObject.put("kpPublicKey", kpObject.getString("public_key"));
                resultObject.put("kpName", kpObject.getString("name"));
                resultObject.put("kpFingerPrint", kpObject.getString("fingerprint"));
                resultArray.put(i,resultObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONObject listkeypairDetail(String response){
        JSONObject resultObject = new JSONObject();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONObject keyPair = (JSONObject) responseJSON.getJSONObject("keypair");
            resultObject.put("kpPublicKey", keyPair.getString("public_key"));
            resultObject.put("kpName", keyPair.getString("name"));
            resultObject.put("kpFingerPrint", keyPair.getString("fingerprint"));
            resultObject.put("kpCreateTime", keyPair.getString("created_at"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    public JSONArray listAvabilityZone(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray serverArray= responseJSON.getJSONArray("availabilityZoneInfo");
            for (int i=0;i<serverArray.length();i++){
                JSONObject instanceObject = (JSONObject) serverArray.get(i);
                JSONObject state= instanceObject.getJSONObject("zoneState");
                JSONObject resultObject = new JSONObject();
                resultObject.put("azHosts", instanceObject.getString("hosts"));
                resultObject.put("azName", instanceObject.getString("zoneName"));
                resultObject.put("azState", state.getBoolean("available"));
                resultArray.put(i,resultObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listFlavor(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray flavorArray= responseJSON.getJSONArray("flavors");
            for (int i=0;i<flavorArray.length();i++) {
                JSONObject flavorObject = (JSONObject) flavorArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("flavorId", flavorObject.getString("id"));
                resultObject.put("flavorName", flavorObject.getString("name"));
                resultArray.put(i, resultObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listSecurityGroup(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray securityArray= responseJSON.getJSONArray("security_groups");
            for (int i=0;i<securityArray.length();i++) {
                JSONObject sgObject = (JSONObject) securityArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("sgId", sgObject.getString("id"));
                resultObject.put("sgName", sgObject.getString("name"));
                resultObject.put("sgDescription", sgObject.getString("description"));
                resultObject.put("sgTenantID", sgObject.getString("tenant_id"));
                resultArray.put(i, resultObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listRulesSG(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONObject securityGroup= responseJSON.getJSONObject("security_group");
            JSONArray rules= securityGroup.getJSONArray("security_group_rules");
            for (int i=0;i<rules.length();i++) {
                JSONObject rule = (JSONObject) rules.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("ruleDirection", rule.getString("direction"));
                resultObject.put("ruleEtherType", rule.getString("ethertype"));
                resultObject.put("ruleProtocol", rule.getString("protocol"));
                resultObject.put("rulePortMin", rule.getString("port_range_min"));
                resultObject.put("rulePortMax", rule.getString("port_range_max"));
                resultObject.put("ruleRemoteIP", rule.getString("remote_ip_prefix"));
                resultObject.put("ruleRemoteG", rule.getString("remote_group_id"));
                resultObject.put("ruleID", rule.getString("id"));
                resultArray.put(i, resultObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONObject listVolumeDetail(String response){
        JSONObject resultObject = new JSONObject();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONObject volume = (JSONObject) responseJSON.getJSONObject("volume");
            resultObject.put("vStatus", volume.getString("status"));
            resultObject.put("vID", volume.getString("id"));
            resultObject.put("vName", volume.getString("name"));
            resultObject.put("vCreate", volume.getString("created_at"));
            resultObject.put("vSize", volume.getString("size"));
            resultObject.put("vDescription", volume.getString("description"));
            resultObject.put("vZone", volume.getString("availability_zone"));
            resultObject.put("vBootable", volume.getString("bootable"));

            resultObject.put("vEncrypted", volume.getString("encrypted"));
            JSONArray attachArray=volume.getJSONArray("attachments");
            int numA=attachArray.length();
            resultObject.put("vnumA", numA);
            for(int j=0;j<numA;j++){
                JSONObject vObject= (JSONObject) attachArray.get(j);
                String serverID=vObject.getString("server_id");
                resultObject.put("server"+j, serverID);
                String device=vObject.getString("device");
                resultObject.put("device"+j, device);

            }

            //Add attaching servers latter

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    //listVolume
    public JSONArray listVolume(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray volumesArray= responseJSON.getJSONArray("volumes");
            for (int i=0;i<volumesArray.length();i++){
                JSONObject instanceObject = (JSONObject) volumesArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("volumeId", instanceObject.getString("id"));
                resultObject.put("volumeSize", instanceObject.getString("size"));
                resultObject.put("volumeStatus", instanceObject.getString("status"));
                resultObject.put("volumeName", instanceObject.getString("name"));
                resultObject.put("volumeDescription", instanceObject.getString("description"));
                resultObject.put("volumeAZ", instanceObject.getString("availability_zone"));

                JSONArray attachA=instanceObject.getJSONArray("attachments");
                int numA=attachA.length();
                resultObject.put("volumeNumAttach",numA);
                for(int j=0;j<numA;j++){
                    JSONObject object=(JSONObject) attachA.get(j);
                    String attachID=object.getString("attachment_id");
                    resultObject.put("vattach"+j, attachID);
                    String device=object.getString("server_id");
                    resultObject.put("vserver"+j, device);

                }

                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listVolumeType(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray volumesArray= responseJSON.getJSONArray("volume_types");
            for (int i=0;i<volumesArray.length();i++){
                JSONObject instanceObject = (JSONObject) volumesArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("typeId", instanceObject.getString("id"));
                resultObject.put("typeName", instanceObject.getString("name"));

                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listSnapshot(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray volumesArray= responseJSON.getJSONArray("snapshots");
            for (int i=0;i<volumesArray.length();i++){
                JSONObject instanceObject = (JSONObject) volumesArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("snapID", instanceObject.getString("id"));
                resultObject.put("snapSize", instanceObject.getString("size"));
                resultObject.put("snapStatus", instanceObject.getString("status"));
                resultObject.put("snapName", instanceObject.getString("name"));
                resultObject.put("snapDescription", instanceObject.getString("description"));

                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONObject listVolumeSnapshotDetail(String response){
        JSONObject resultObject = new JSONObject();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONObject volume = (JSONObject) responseJSON.getJSONObject("snapshot");
            resultObject.put("vStatus", volume.getString("status"));
            resultObject.put("vID", volume.getString("id"));
            resultObject.put("vName", volume.getString("name"));
            resultObject.put("vCreate", volume.getString("created_at"));
            resultObject.put("vSize", volume.getString("size"));
            resultObject.put("vDescription", volume.getString("description"));
            resultObject.put("vVolume", volume.getString("volume_id"));


            //Add attaching servers latter

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    public JSONArray listFloatingIP(String response){
        JSONArray resultArray = new JSONArray();

        try {
            JSONObject responseJSON = new JSONObject(response);



            JSONArray serverArray = responseJSON.getJSONArray("floatingips");
            for(int i=0; i<serverArray.length();i++)
            {
                JSONObject floatingObject = serverArray.getJSONObject(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("floatingID",floatingObject.getString("id"));
                resultObject.put("floating_ip_address",floatingObject.getString("floating_ip_address"));
                resultObject.put("floating_network_id",floatingObject.getString("floating_network_id"));
                resultObject.put("status",floatingObject.getString("status"));
                resultObject.put("description",floatingObject.getString("description"));
                resultArray.put(i,resultObject);

            }

            return resultArray;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;

    }

    public JSONArray listRouter(String response){
        JSONArray resultArray = new JSONArray();

        try {
            JSONObject responseJSON = new JSONObject(response);



            JSONArray serverArray = responseJSON.getJSONArray("routers");
            for(int i=0; i<serverArray.length();i++)
            {
                JSONObject routerObject = serverArray.getJSONObject(i);
                JSONObject resultObject = new JSONObject();

                JSONObject externalNetworkObject = routerObject.getJSONObject("external_gateway_info");

                JSONArray fixedipsArray = externalNetworkObject.getJSONArray(("external_fixed_ips"));


                resultObject.put("routerID",routerObject.getString("id"));
                resultObject.put("routerName",routerObject.getString("name"));
                resultObject.put("admin_state_up",routerObject.getString("admin_state_up"));
                resultObject.put("status",routerObject.getString("status"));
                resultObject.put("project_id",routerObject.getString("project_id"));

                resultObject.put("network_id", externalNetworkObject.getString("network_id"));
                resultObject.put("enable_snat", externalNetworkObject.getString("enable_snat"));

                resultObject.put("project_id","project_id");


                //the json object of internal array :external_fixed_ips
                for (int j=0; j< fixedipsArray.length();j++)
                {
                    JSONObject tempObject = fixedipsArray.getJSONObject(j);

                    resultObject.put("subnet_id",tempObject.getString("subnet_id"));
                    resultObject.put("ip_address",tempObject.getString("ip_address"));
                }
                resultArray.put(i,resultObject);

            }

            return resultArray;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;

    }

    public JSONArray listNetwork(String response) {
        JSONArray resultArray = new JSONArray();

        try {
            JSONObject responseJSON = null;
            responseJSON = new JSONObject(response);
            JSONArray serverArray = responseJSON.getJSONArray("networks");
            int count = 0;
            for (int i = 0; i < serverArray.length(); i++)
            {
                JSONObject networksObject = serverArray.getJSONObject(i);
                JSONObject resultObject = new JSONObject();
                String tenant_id = networksObject.getString("tenant_id");
                if(tenant_id.equals(this.tenant_id)){


                    resultObject.put("networkName",networksObject.getString("name"));
                    resultObject.put("tenant_id",tenant_id);
                    resultObject.put("status",networksObject.getString("status"));
                    resultObject.put("shared",networksObject.getString("shared"));
                    resultObject.put("admin_state_up",networksObject.getString("admin_state_up"));
                    resultObject.put("networkid",networksObject.getString("id"));
                    resultObject.put("router_external",networksObject.getString("router:external"));
                    resultObject.put("mtu",networksObject.getString("mtu"));
                    JSONArray subnetArray = networksObject.getJSONArray("subnets");
                    String subnet_array = subnetArray.toString();
                    resultObject.put("subnet_array",subnet_array);
                    resultArray.put(count,resultObject);
                    count++;
                }

            }

            return resultArray;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONArray listSubnet(String response, String networkID){
        JSONArray resultArray = new JSONArray();

        try {
            JSONObject responseJSON = null;
            responseJSON = new JSONObject(response);
            JSONArray serverArray = responseJSON.getJSONArray("subnets");
            int count = 0;
            for (int i = 0; i < serverArray.length(); i++) {
                JSONObject subnetObject = serverArray.getJSONObject(i);
                JSONObject resultObject = new JSONObject();
                String network_id = subnetObject.getString("network_id");
                if (network_id.equals(networkID)) {

                    resultObject.put("subnetName", subnetObject.getString("name"));
                    resultObject.put("subnetID", subnetObject.getString("id"));
                    resultObject.put("cidr", subnetObject.getString("cidr"));
                    resultObject.put("network_id", subnetObject.getString("network_id"));
                    resultObject.put("ip_version", subnetObject.getString("ip_version"));
                    JSONArray allocation_pools_array = subnetObject.getJSONArray("allocation_pools");
                    String allocationString = allocation_pools_array.toString();
                    resultObject.put("allocation_pools", allocationString);
                    resultArray.put(count,resultObject);
                    count++;
                }
            }


            return resultArray;


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resultArray;
    }


    /*
    * list ports
    * */
    public JSONArray listPort(String response, String networkID){
        JSONArray resultArray = new JSONArray();

        try {
            JSONObject responseJSON = null;
            responseJSON = new JSONObject(response);
            JSONArray serverArray = responseJSON.getJSONArray("ports");
            int count = 0;
            for (int i = 0; i < serverArray.length(); i++) {
                JSONObject subnetObject = serverArray.getJSONObject(i);
                JSONObject resultObject = new JSONObject();
                String network_id = subnetObject.getString("network_id");
                if (network_id.equals(networkID)) {

                    resultObject.put("portName", subnetObject.getString("name"));
                    resultObject.put("portID", subnetObject.getString("id"));
                    resultObject.put("mac_address", subnetObject.getString("mac_address"));
                    resultObject.put("network_id", subnetObject.getString("network_id"));
                    resultObject.put("status", subnetObject.getString("status"));
                    resultObject.put("admin_state_up", subnetObject.getString("admin_state_up"));
//                    resultObject.put("dns_name", subnetObject.getString("dns_name"));
                    resultObject.put("created_at", subnetObject.getString("created_at"));

//                    JSONArray allocation_pools_array = subnetObject.getJSONArray("allocation_pools");
//                    String allocationString = allocation_pools_array.toString();
//                    resultObject.put("allocation_pools", allocationString);
                    resultArray.put(count,resultObject);
                    count++;
                }
            }


            return resultArray;


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resultArray;
    }

    ///////////
    ///test for updata
    public JSONArray listResourceTypes(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray serverArray= responseJSON.getJSONArray("resource_types");
            for (int i=0;i<serverArray.length();i++){
//                JSONObject instanceObject = (JSONObject) serverArray.get(i);
                String resourceTypeTem = serverArray.get(i).toString() ;
                JSONObject resultObject = new JSONObject();
                resultObject.put("resourceTypesName", resourceTypeTem);
                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    ///////////////////
    /////test for resource type data
    //////////////////

    public JSONObject listResourceTypesDetail(String response)  {
        JSONObject resultObject = new JSONObject();
        try {
            JSONObject responseJSON = new JSONObject(response);

            resultObject.put("resourceType", responseJSON.getString("resource_type"));
            resultObject.put("resourceTypeAttributes", responseJSON.getString("attributes"));
            resultObject.put("resourceTypeProperties", responseJSON.getString("properties"));


            return resultObject;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }
    /////////////////////
    // list template version
    public JSONArray listTemplateVersions(String response)  {
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray serverArray= responseJSON.getJSONArray("template_versions");
            for (int i=0;i<serverArray.length();i++){
                JSONObject instanceObject = (JSONObject) serverArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("version", instanceObject.getString("version"));
                resultObject.put("type", instanceObject.getString("type"));
                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }


    //////////////////
    //list template version detail
    public JSONArray listTemplateVersionsDetail(String response)  {
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray serverArray= responseJSON.getJSONArray("template_functions");
            for (int i=0;i<serverArray.length();i++){
                JSONObject instanceObject = (JSONObject) serverArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("functions", instanceObject.getString("functions"));
                resultObject.put("description", instanceObject.getString("description"));
                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    ////////////////
    // list stacks
    public JSONArray listStacks(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray stacskArray= responseJSON.getJSONArray("stacks");
            for (int i=0;i<stacskArray.length();i++){
                JSONObject instanceObject = (JSONObject) stacskArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("stackName", instanceObject.getString("stack_name"));
                resultObject.put("stackStatus", instanceObject.getString("stack_status"));
                resultObject.put("stackCreationTime", instanceObject.getString("creation_time"));
                resultObject.put("stackID", instanceObject.getString("id"));

                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }


    ///////////////////
    // list database instances
    public JSONArray listDatabaseInstances(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray stacskArray= responseJSON.getJSONArray("instances");
            for (int i=0;i<stacskArray.length();i++){
                JSONObject instanceObject = (JSONObject) stacskArray.get(i);
                JSONObject resultObject = new JSONObject();
                JSONObject tempObject = new JSONObject();
                resultObject.put("databaseInstancekName", instanceObject.getString("name"));

                tempObject = instanceObject.getJSONObject("datastore");
                resultObject.put("databaseInstanceType", tempObject.getString("type"));
                resultObject.put("databaseInstanceVersion", tempObject.getString("version"));

                resultObject.put("databaseInstanceVolume", instanceObject.getString("volume"));
                resultObject.put("databaseInstanceStatues", instanceObject.getString("status"));
                resultObject.put("databaseInstanceID", instanceObject.getString("id"));

                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }



    /////////////
    // list configuration groups
    public JSONArray listConfigGroups(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray volumesArray= responseJSON.getJSONArray("configurations");
            for (int i=0;i<volumesArray.length();i++){
                JSONObject instanceObject = (JSONObject) volumesArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("configGroupId", instanceObject.getString("id"));
                resultObject.put("configGroupName", instanceObject.getString("name"));
                resultObject.put("configGroupDescription", instanceObject.getString("description"));
                resultObject.put("configGroupDatastore", instanceObject.getString("datastore_name"));
                resultObject.put("configGroupVersion", instanceObject.getString("datastore_version_name"));
                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    /*
    * show configuration group details
    */
    public JSONObject listConfigGroupDetail(String response){
        JSONObject resultObject = new JSONObject();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONObject configDetail = (JSONObject) responseJSON.getJSONObject("configuration");
            resultObject.put("configId", configDetail.getString("id"));
            resultObject.put("configName", configDetail.getString("name"));
            resultObject.put("configDescription", configDetail.getString("description"));
            resultObject.put("configDatastore", configDetail.getString("datastore_name"));
            resultObject.put("configVersion", configDetail.getString("datastore_version_name"));
            resultObject.put("configCreated", configDetail.getString("created"));
            resultObject.put("configUpdated", configDetail.getString("updated"));

            //Add attaching servers latter

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /*
    * list configuration group instances
    * */
    public JSONArray listConfigGroupInstances(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray serverArray= responseJSON.getJSONArray("instances");
            for (int i=0;i<serverArray.length();i++){
                JSONObject instanceObject = (JSONObject) serverArray.get(i);
//                String resourceTypeTem = serverArray.get(i).toString() ;
                JSONObject resultObject = new JSONObject();
                resultObject.put("configGroupInstanceId", instanceObject.getString("id"));
                resultObject.put("configGroupInstanceName", instanceObject.getString("name"));
                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }


    /*
    * list datastores
    * */
    public JSONArray listDatastores(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray volumesArray= responseJSON.getJSONArray("datastores");
            for (int i=0;i<volumesArray.length();i++){
                JSONObject instanceObject = (JSONObject) volumesArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("datastoreId", instanceObject.getString("id"));
                resultObject.put("datastoreName", instanceObject.getString("name"));
                resultObject.put("datastoreVersion", instanceObject.getJSONArray("versions").getJSONObject(0).getString("name"));
                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    /*
    * list database flavors
    * */
    public JSONArray listDatabaseFlavors(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray volumesArray= responseJSON.getJSONArray("flavors");
            for (int i=0;i<volumesArray.length();i++){
                JSONObject instanceObject = (JSONObject) volumesArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("databaseFlavorId", instanceObject.getString("id"));
                resultObject.put("databaseFlavorsName", instanceObject.getString("name"));
                resultObject.put("databaseFlavorLink", instanceObject.getJSONArray("links").getJSONObject(0).getString("href"));
//                resultObject.put("datastoreVersion", instanceObject.getJSONArray("versions").getJSONObject(0).getString("name"));
                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    /*
    * list database backups
    * */
    public JSONArray listDatabaseBackups(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray volumesArray= responseJSON.getJSONArray("backups");
            for (int i=0;i<volumesArray.length();i++){
                JSONObject instanceObject = (JSONObject) volumesArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("databaseBackupId", instanceObject.getString("id"));
                resultObject.put("databaseBackupName", instanceObject.getString("name"));
                resultObject.put("databaseBackupDatabase", instanceObject.getString("instance_id"));
                resultObject.put("databaseBackupDatastore", instanceObject.getJSONObject("datastore").getString("type"));
                resultObject.put("databaseBackupVersion", instanceObject.getJSONObject("datastore").getString("version"));
                resultObject.put("databaseBackupStatus", instanceObject.getString("status"));

                resultArray.put(i,resultObject);
            }
            return resultArray;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    /*
    * list database backup detail
    * */
    public JSONObject listDatabaseBackupDetail(String response){
        JSONObject resultObject = new JSONObject();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONObject configDetail = (JSONObject) responseJSON.getJSONObject("backup");
            resultObject.put("databaseBackupId", configDetail.getString("id"));
            resultObject.put("databaseBackupName", configDetail.getString("name"));
            resultObject.put("databaseBackupDescription", configDetail.getString("description"));
            resultObject.put("databaseBackupDatastore", configDetail.getJSONObject("datastore").getString("type"));
            resultObject.put("databaseBackupVersion", configDetail.getJSONObject("datastore").getString("version"));
            resultObject.put("databaseBackupCreated", configDetail.getString("created"));
            resultObject.put("databaseBackupUpdated", configDetail.getString("updated"));
            resultObject.put("databaseBackupDatabase", configDetail.getString("instance_id"));
            resultObject.put("databaseBackupStatus", configDetail.getString("status"));

            //Add attaching servers latter

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /*
    * enable root
    * */
    public JSONObject enableRoot(String response){
        JSONObject resultObject = new JSONObject();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONObject configDetail = (JSONObject) responseJSON.getJSONObject("user");
            resultObject.put("instanceName", configDetail.getString("name"));
            resultObject.put("rootPassword", configDetail.getString("password"));

            //Add attaching servers latter

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /*
    * show root
    * */
    public JSONObject showRoot(String response){
        JSONObject resultObject = new JSONObject();
        try {
            JSONObject responseJSON = new JSONObject(response);
//            JSONObject configDetail = (JSONObject) responseJSON.getJSONObject("user");
            resultObject.put("rootEnabled", responseJSON.getString("rootEnabled"));
//            resultObject.put("rootPassword", configDetail.getString("password"));

            //Add attaching servers latter

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    /*
    List Clusters
    **/

    public JSONArray listCluster(String response){
        JSONArray resultArray = new JSONArray();

        try {
            JSONObject responseJSON = null;
            responseJSON = new JSONObject(response);
            JSONArray clustersArray = responseJSON.getJSONArray("clusters");
            int count = 0;
            for (int i = 0; i < clustersArray.length(); i++) {
                JSONObject clusterObject = clustersArray.getJSONObject(i);
                JSONObject resultObject = new JSONObject();


                resultObject.put("clusterName", clusterObject.getString("name"));
                resultObject.put("clusterID", clusterObject.getString("uuid"));
                resultObject.put("status", clusterObject.getString("status"));
                resultObject.put("master_count", clusterObject.getString("master_count"));
                resultObject.put("node_count", clusterObject.getString("node_count"));
                resultObject.put("keyPair", clusterObject.getString("keypair"));
                resultArray.put( count, resultObject);
                count++;

            }


            return resultArray;


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resultArray;
    }

    public JSONObject listClusterDetail(String response){
        JSONObject resultObject = new JSONObject();
        try {
            JSONObject responseJSON = new JSONObject(response);
//            JSONObject cluster = (JSONObject) responseJSON.getJSONObject("clusters");
            resultObject.put("status", responseJSON.getString("status"));
            resultObject.put("clusterID", responseJSON.getString("uuid"));
            resultObject.put("clusterCOE", responseJSON.getString("coe_version"));
            resultObject.put("discoveryURL", responseJSON.getString("discovery_url"));
            resultObject.put("clusterName", responseJSON.getString("name"));
            resultObject.put("createTime", responseJSON.getString("created_at"));
            resultObject.put("nodeCount", responseJSON.getString("node_count"));
            resultObject.put("masterCount", responseJSON.getString("master_count"));
            resultObject.put("keyPair", responseJSON.getString("keypair"));
            resultObject.put("upgradeTime", responseJSON.getString("updated_at"));

            //Add attaching servers latter

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }

    public JSONArray listClusterTemplate(String response){
        JSONArray resultArray= new JSONArray();
        try {
            JSONObject responseJSON = new JSONObject(response);
            JSONArray TemplateArray= responseJSON.getJSONArray("clustertemplates");
            for (int i=0;i<TemplateArray.length();i++) {
                JSONObject TemplateObject = (JSONObject) TemplateArray.get(i);
                JSONObject resultObject = new JSONObject();
                resultObject.put("name",TemplateObject.getString("name"));
                resultObject.put("uuid",TemplateObject.getString("uuid"));
                resultObject.put("keypair_id",TemplateObject.getString("keypair_id"));
                resultObject.put("image_id",TemplateObject.getString("image_id"));
                resultObject.put("coe",TemplateObject.getString("coe"));
                resultObject.put("created_at",TemplateObject.getString("created_at"));
                resultObject.put("dns_nameserver",TemplateObject.getString("dns_nameserver"));
                resultArray.put(i, resultObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    public JSONObject listClusterTemplateDetail(String response){
        JSONObject resultObject = new JSONObject();
        try {
            JSONObject responseJSON = new JSONObject(response);
            resultObject.put("name",responseJSON.getString("name"));
            resultObject.put("uuid",responseJSON.getString("uuid"));
            resultObject.put("keypair_id",responseJSON.getString("keypair_id"));
            resultObject.put("image_id",responseJSON.getString("image_id"));
            resultObject.put("coe",responseJSON.getString("coe"));
            resultObject.put("created_at",responseJSON.getString("created_at"));
            resultObject.put("dns_nameserver",responseJSON.getString("dns_nameserver"));

            //Add attaching servers latter

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultObject;
    }


}
