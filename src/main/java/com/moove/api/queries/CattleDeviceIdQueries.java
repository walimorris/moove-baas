package com.moove.api.queries;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.moove.api.models.Device;
import com.moove.api.models.HerdMetaData;
import com.moove.api.utils.DynamoUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.moove.api.utils.DynamoUtils.getSkipUpdateNullAttributesMapperConfig;

public class CattleDeviceIdQueries {

    /**
     * GetItem request on device using Global Secondary Index
     *
     * @param amazonDynamoDB {@link AmazonDynamoDB}
     * @param gsi1pk global secondary index pk
     * @param gsi1sk global secondary index sk
     * @param logger {@link LambdaLogger}
     *
     * @return {@link Device}
     */
    public static Device getDeviceItemByDeviceIndex(AmazonDynamoDB amazonDynamoDB, String gsi1pk, String gsi1sk, LambdaLogger logger) {
        DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);

        Device deviceItem = new Device();
        deviceItem.setGsi1pk(gsi1pk);
        deviceItem.setGsi1sk(gsi1sk);

        try {
            return mapper.load(Device.class, deviceItem);
        } catch (Exception e) {
            logger.log("Error on GetItem for device item with id: " + gsi1pk);
        }
        return null;
    }

    /**
     * PutItem request on a {@link Device} entity.
     *
     * @param amazonDynamoDB {@link AmazonDynamoDB}
     * @param herdId herd id the device belongs to. HERD#
     * @param deviceId device id
     * @param latitude latitude coordinate
     * @param longitude longitude coordinate
     * @param ttl time to live
     * @param status status of the device (in/out) of geofence
     * @param gsi1pk global secondary index partition key
     * @param gsi1sk global secondary index sort key
     * @param logger {@link LambdaLogger}
     *
     * @return boolean
     */
    public static boolean putDeviceItem(AmazonDynamoDB amazonDynamoDB, String herdId, String deviceId, String latitude, String longitude,
                                                     long ttl, String status, String gsi1pk, String gsi1sk, LambdaLogger logger) {
        Device deviceItem = new Device();
        deviceItem.setHerdId(herdId);
        deviceItem.setDeviceId(deviceId);
        deviceItem.setLatitude(latitude);
        deviceItem.setLongitude(longitude);
        deviceItem.setTtl(ttl);
        deviceItem.setStatus(status);
        deviceItem.setGsi1pk(gsi1pk);
        deviceItem.setGsi1sk(gsi1sk);

        DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);

        try {
            mapper.save(deviceItem);
        } catch (Exception e) {
            logger.log("Error on Cattle Table PutItem with device id " + deviceId + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean updateDeviceItem(AmazonDynamoDB amazonDynamoDB, LambdaLogger logger, String pk, String sk, String status) {
        Device deviceItem = new Device();
        deviceItem.setHerdId(pk);
        deviceItem.setDeviceId(sk);
        deviceItem.setStatus(status);

        DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);

        try {
            mapper.save(deviceItem, getSkipUpdateNullAttributesMapperConfig());
        } catch (Exception e) {
            logger.log("Error on Cattle Table UpdateItem with herdId PK: " + pk + " deviceId SK: " +
                    sk + ", " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * PutItem request on herd meta-data item for the metaDataLimits attribute that contains
     * a list of coordinates for geofence polygon.
     *
     * @param amazonDynamoDB {@link AmazonDynamoDB}
     * @param logger {@link LambdaLogger}
     * @param pk partition key on table (HERD#123)
     * @param coordinateLimits list of coordinates for geofence polygon
     *
     * @return boolean
     * @see DynamoUtils#getSkipUpdateNullAttributesMapperConfig()
     */
    public static boolean putHerdMetaDataLimits(AmazonDynamoDB amazonDynamoDB, LambdaLogger logger, String pk, ArrayList<String>
            coordinateLimits) {

        DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);

        HerdMetaData herdMetaDataLimits = new HerdMetaData();

        Map<String, List<String>> coordinates = new HashMap<>();
        coordinates.put("coordinateLimits", coordinateLimits);

        herdMetaDataLimits.setHerdId("HERD#" + pk);
        herdMetaDataLimits.setMetaKey("META");
        herdMetaDataLimits.setMetaDataLimits(coordinates);

        try {
            mapper.save(herdMetaDataLimits, getSkipUpdateNullAttributesMapperConfig());
        } catch(Exception e) {
            logger.log("Error Putting coordinate limits on herdId: " + pk);
            return false;
        }
        return true;
    }

    /**
     * PutItem request on herd meta-data item for the metaData timestamp attribute that
     * contains a timestamp for the latest device data added to the herd.
     *
     * @param amazonDynamoDB {@link AmazonDynamoDB}
     * @param logger {@link LambdaLogger}
     * @param herdId herdId (HERD#123) is the partition key
     * @param timestamp time formatted string
     *
     * @return boolean
     * @see DynamoUtils#getSkipUpdateNullAttributesMapperConfig()
     */
    public static boolean putHerdMetaData(AmazonDynamoDB amazonDynamoDB, LambdaLogger logger, String herdId, String timestamp) {
        DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);

        HerdMetaData herdMetaData = new HerdMetaData();

        Map<String, String> metaData = new HashMap<>();
        metaData.put("latestTimestamp", timestamp);

        herdMetaData.setHerdId(herdId);
        herdMetaData.setMetaKey("META");
        herdMetaData.setMetaData(metaData);

        try {
            mapper.save(herdMetaData, getSkipUpdateNullAttributesMapperConfig());
        } catch (Exception e) {
            logger.log("Error putting metadata on herdId: " + herdId);
            return false;
        }
        return true;
    }
}
