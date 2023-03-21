package com.moove.api.queries;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.moove.api.models.Device;
import com.moove.api.models.HerdMetaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CattleDeviceIdQueries {

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

    public static boolean putHerdMetaDataLimits(AmazonDynamoDB amazonDynamoDB, LambdaLogger logger, String pk, ArrayList<String>
            coordinateLimits) {

        DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);

        HerdMetaData herdMetaDataLimits = new HerdMetaData();

        Map<String, List<String>> coordinates = new HashMap<>();
        coordinates.put("coordinateLimits", coordinateLimits);

        herdMetaDataLimits.setHerdId(pk);
        herdMetaDataLimits.setMetaKey("META");
        herdMetaDataLimits.setMetaDataLimits(coordinates);

        try {
            mapper.save(herdMetaDataLimits);
        } catch(Exception e) {
            logger.log("Error Putting coordinate limits on herdId: " + pk);
            return false;
        }
        return true;
    }
}
