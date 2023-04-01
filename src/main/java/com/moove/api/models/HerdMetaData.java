package com.moove.api.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.util.List;
import java.util.Map;

/**
 * com.moove.api.models.HerdMetaData models metadata that stores metadata attributes for a single
 * herd in a DynamoDB Table.
 * <p>
 * A single table contains many herds and a single herd will have many devices. As such, a single
 * device will be modeled to a single herd.
 * </p>
 */
@DynamoDBTable(tableName = "Cattle")
public class HerdMetaData {
    private String herdId;
    private String metaKey;
    private Map<String, List<String>> metaDataLimits;
    private Map<String, String> metaData;

    public HerdMetaData() {}

    /**
     * Get herdId.
     *
     * @return {@link String}
     */
    @DynamoDBHashKey(attributeName = "PK")
    public String getHerdId() {
        return this.herdId;
    }

    /**
     * Set herdId.
     *
     * @param herdId {@link String}
     */
    public void setHerdId(String herdId) {
        this.herdId = herdId;
    }

    /**
     * Get meta key. Note: querying the meta sort key ('META') on herd in the DynamoDB Table
     * returns the attributes that pertain to metadata on the herd.
     *
     * @return {@link String}
     */
    @DynamoDBRangeKey(attributeName = "SK")
    public String getMetaKey() {
        return this.metaKey;
    }

    /**
     * Set meta key.
     *
     * @param metaKey {@link String}
     */
    public void setMetaKey(String metaKey) {
        this.metaKey = metaKey;
    }

    /**
     * Get MetaDataLimits. Note: metaDataLimits is the name of a metadata attribute of type
     * {@link Map} on the DynamoDB table. This value maps to a {@link List} of coordinates
     * that depict a geofence.
     *
     * @return {@link Map}
     */
    @DynamoDBAttribute(attributeName = "metaDataLimits")
    public Map<String, List<String>> getMetaDataLimits() {
        return this.metaDataLimits;
    }

    /**
     * Set MetaDataLimits.
     *
     * @param metaDataLimits {@link Map}
     */
    public void setMetaDataLimits(Map<String, List<String>> metaDataLimits) {
        this.metaDataLimits = metaDataLimits;
    }

    /**
     * Get MetaData. Note: metaData is the name of a metadata attribute of the type {@link Map}
     * on the DynamoDB table. The map attribute maps a set of keys to various metaData values.
     * One such value can be: latestTimestamp -> 2023-03-28T21:59:46.
     *
     * @return {@link Map}
     */
    @DynamoDBAttribute(attributeName = "metaData")
    public Map<String, String> getMetaData() {
        return this.metaData;
    }

    /**
     * Set MetaData.
     *
     * @param metaData {@link Map}
     */
    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }
}
