package com.moove.api.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.util.List;
import java.util.Map;

@DynamoDBTable(tableName = "Cattle")
public class HerdMetaData {
    private String herdId;
    private String metaKey;
    private Map<String, List<String>> metaDataLimits;
    private Map<String, String> metaData;

    public HerdMetaData() {}

    @DynamoDBHashKey(attributeName = "PK")
    public String getHerdId() {
        return this.herdId;
    }

    public void setHerdId(String herdId) {
        this.herdId = herdId;
    }

    @DynamoDBRangeKey(attributeName = "SK")
    public String getMetaKey() {
        return this.metaKey;
    }

    public void setMetaKey(String metaKey) {
        this.metaKey = metaKey;
    }

    @DynamoDBAttribute(attributeName = "metaDataLimits")
    public Map<String, List<String>> getMetaDataLimits() {
        return this.metaDataLimits;
    }

    public void setMetaDataLimits(Map<String, List<String>> metaDataLimits) {
        this.metaDataLimits = metaDataLimits;
    }

    @DynamoDBAttribute(attributeName = "metaData")
    public Map<String, String> getMetaData() {
        return this.metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }
}
