package com.moove.api.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;

/**
 * com.moove.api.models.Device models a Device Entity that stores device data in DynamoDB Table.
 * <p>
 * A single table contains many herds and a single herd will have many devices. As such, a single
 * device will be modeled to a single herd.
 * </p>
 */
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

    /**
     * Get device herdId.
     *
     * @return {@link String}
     */
    @DynamoDBHashKey(attributeName = "PK")
    public String getHerdId() {
        return herdId;
    }

    /**
     * Set device herdId.
     *
     * @param herdId {@link String}
     */
    public void setHerdId(String herdId) {
        this.herdId = herdId;
    }

    /**
     * Get deviceId.
     * <p>
     * The deviceId sort key should containthe deviceId. To accommodate different
     * access patterns the SK should be designed with another attribute, such as a
     * timestamp. Example: 2023-30-03T21:59:39#deviceId. This allows this model
     * to than store the deviceId and timestamp as GSI partition-key and sort-key,
     * respectively, in order to establish various access patterns on the table data.
     * An example would be sorting device by latest timestamp or the inverse, querying
     * all devices in a certain year or range.
     * </p>
     * @return {@link String}
     */
    @DynamoDBRangeKey(attributeName = "SK")
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Set deviceId.
     *
     * @param deviceId {@link String}
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Get device latitude.
     *
     * @return {@link String}
     */
    @DynamoDBAttribute(attributeName = "latitude")
    public String getLatitude() {
        return latitude;
    }

    /**
     * Set device latitude.
     *
     * @param latitude {@link String}
     */
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    /**
     * Get device longitude.
     *
     * @return {@link String}
     */
    @DynamoDBAttribute(attributeName = "longitude")
    public String getLongitude() {
        return longitude;
    }

    /**
     * Set device longitude.
     *
     * @param longitude {@link String}
     */
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /**
     * Get device data ttl.
     * <p>
     * TTL needs to be enabled on a DynamoDB Table and values input in epoch time
     * with a Number attribute type. This can purge outdated data and reduce cost
     * for storage on DynamoDB tables.
     * </p>
     *
     * @return long
     */
    @DynamoDBAttribute(attributeName = "ttl")
    public long getTtl() {
        return ttl;
    }

    /**
     * Set device data ttl.
     *
     * @param ttl long
     */
    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    /**
     * Get device status.
     * <p>
     * Device status is dependent on a geofence, coordinates stored in the META-DATA
     * attribute, which determines if a device's latitude/longitude values are within
     * those coordinates.
     * </p>
     *
     * @return {@link String}
     * @see HerdMetaData#getMetaDataLimits()
     */
    @DynamoDBAttribute(attributeName = "status")
    public String getStatus() {
        return status;
    }

    /**
     * Set device status.
     *
     * @param status {@link String}
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get GSI1PK value.
     * <p>
     * This value will be the deviceId projected into the global secondary index.
     * </p>
     *
     * @return {@link String}
     * @see Device#getDeviceId()
     */
    @DynamoDBAttribute(attributeName = "GSI1PK")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "device-index")
    public String getGsi1pk() {
        return this.gsi1pk;
    }

    /**
     * Set GSI1PK.
     *
     * @param gsi1pk {@link String}
     */
    public void setGsi1pk(String gsi1pk) {
        this.gsi1pk = gsi1pk;
    }

    /**
     * Get GSI1SK.
     *
     * @return {@link String}
     * @see Device#getDeviceId()
     */
    @DynamoDBAttribute(attributeName = "GSI1SK")
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "device-index")
    public String getGsi1sk() {
        return this.gsi1sk;
    }

    /**
     * Set GSI1SK.
     *
     * @param gsi1sk {@link String}
     */
    public void setGsi1sk(String gsi1sk) {
        this.gsi1sk = gsi1sk;
    }
}
