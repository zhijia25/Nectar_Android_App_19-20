package com.jianqingc.nectar.bean;

public class CreateClusterBean {

    /**
     * name : k8s
     * discovery_url : null
     * master_count : 2
     * cluster_template_id : 0562d357-8641-4759-8fed-8173f02c9633
     * node_count : 2
     * create_timeout : 60
     * keypair : my_keypair
     * master_flavor_id : null
     * labels : {}
     * flavor_id : null
     */

    private String name;
    private String discovery_url;
    private int master_count;
    private String cluster_template_id;
    private int node_count;
    private int create_timeout;
    private String keypair;
    private String master_flavor_id;
    private LabelsBean labels;
    private String flavor_id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getDiscovery_url() {
        return discovery_url;
    }

    public void setDiscovery_url(String discovery_url) {
        this.discovery_url = discovery_url;
    }

    public int getMaster_count() {
        return master_count;
    }

    public void setMaster_count(int master_count) {
        this.master_count = master_count;
    }

    public String getCluster_template_id() {
        return cluster_template_id;
    }

    public void setCluster_template_id(String cluster_template_id) {
        this.cluster_template_id = cluster_template_id;
    }

    public int getNode_count() {
        return node_count;
    }

    public void setNode_count(int node_count) {
        this.node_count = node_count;
    }

    public int getCreate_timeout() {
        return create_timeout;
    }

    public void setCreate_timeout(int create_timeout) {
        this.create_timeout = create_timeout;
    }

    public String getKeypair() {
        return keypair;
    }

    public void setKeypair(String keypair) {
        this.keypair = keypair;
    }

    public String getMaster_flavor_id() {
        return  master_flavor_id;
    }

    public void setMaster_flavor_id(String master_flavor_id) {
        this.master_flavor_id = master_flavor_id;
    }

    public LabelsBean getLabels() {
        return labels;
    }

    public void setLabels(LabelsBean labels) {
        this.labels = labels;
    }

    public String getFlavor_id() {
        return flavor_id;
    }

    public void setFlavor_id(String flavor_id) {
        this.flavor_id = flavor_id;
    }

    public static class LabelsBean {

    }
}
