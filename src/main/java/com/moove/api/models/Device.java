package com.moove.api.models;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

@DynamoDBTable(tableName = "Cattle")
public class Device {
    private String herdId;
    private String deviceId;
    private String latitude;
    private String longitude;
    private long ttl;
    private String status;
    private String gsi1pk;
    private String gsi1sk;

    public Device() {}

    @DynamoDBHashKey(attributeName = "PK")
    public String getHerdId() {
        return herdId;
    }

    public void setHerdId(String herdId) {
        this.herdId = herdId;
    }

    @DynamoDBRangeKey(attributeName = "SK")
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @DynamoDBAttribute(attributeName = "latitude")
    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    @DynamoDBAttribute(attributeName = "longitude")
    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @DynamoDBAttribute(attributeName = "ttl")
    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    @DynamoDBAttribute(attributeName = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @DynamoDBAttribute(attributeName = "GSI1PK")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "device-index")
    public String getGsi1pk() {
        return this.gsi1pk;
    }

    public void setGsi1pk(String gsi1pk) {
        this.gsi1pk = gsi1pk;
    }

    @DynamoDBAttribute(attributeName = "GSI1SK")
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "device-index")
    public String getGsi1sk() {
        return this.gsi1sk;
    }

    public void setGsi1sk(String gsi1sk) {
        this.gsi1sk = gsi1sk;
    }
}
