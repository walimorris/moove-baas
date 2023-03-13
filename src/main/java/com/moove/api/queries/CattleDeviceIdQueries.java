package com.moove.api.queries;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.moove.api.models.CattleItem;

import static com.moove.api.utils.DynamoUtils.getPutItemMapperConfig;

public class CattleDeviceIdQueries {

    /**
     * Queries a AmazonDynamoDB Table for a {@link CattleItem} and returns the item, otherwise returns
     * a {@link CattleItem} that is capable of setting the attribute values on the object.
     *
     * @param amazonDynamoDB {@link AmazonDynamoDB}
     * @param deviceId cattle deviceId
     * @param logger {@link LambdaLogger}
     *
     * @see CattleItem
     *
     * @return {@link CattleItem}
     */
    public static CattleItem queryCattleItemByDeviceId(AmazonDynamoDB amazonDynamoDB, String deviceId, LambdaLogger logger) {
        DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);

        CattleItem cattleItem = new CattleItem();
        cattleItem.setDeviceId(Integer.parseInt(deviceId));

        DynamoDBQueryExpression<CattleItem> queryExpression = new DynamoDBQueryExpression<CattleItem>()
                .withConsistentRead(false)
                .withHashKeyValues(cattleItem);

        try {
            return mapper.query(CattleItem.class, queryExpression).get(0);
        } catch (Exception e) {

            /*
            Return a CattleItem Object if the queried Item is not found on the cattle-herd table.
            This signifies that the Item doesn't exist and allows us to create it from this empty
            CattleItem object
             */
            logger.log("Error querying " + deviceId + " on the " + "cattle-herd. Attempting to build Item.");
            return new CattleItem();
        }
    }

    public static boolean updateCattleItemAttributes(AmazonDynamoDB amazonDynamoDB, String deviceId, String latitude,
                                              String longitude, LambdaLogger logger) {

        DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);
        CattleItem cattleItemResult = queryCattleItemByDeviceId(amazonDynamoDB, deviceId, logger);

        // Received a hit on a CattleItem with the given deviceId
        if (cattleItemResult.getDeviceId() != -1) {
            cattleItemResult.setLatitude(latitude);
            cattleItemResult.setLongitude(longitude);
            try {
                mapper.save(cattleItemResult);
            } catch (Exception e) {
                logger.log("Error updating Cattle Item with device id " + deviceId + ": " + e.getMessage());
                return false;
            }
            // Didn't get a CattleItem hit, item isn't in table
            // Add given attribute properties to Put new CattleItem
        } else {
            if (String.valueOf(deviceId).equals("0")) {
                cattleItemResult.setDeviceId(Integer.parseInt(deviceId));
            } else {
                cattleItemResult.setDeviceId(Integer.parseInt(deviceId));
            }
            cattleItemResult.setLatitude(latitude);
            cattleItemResult.setLongitude(longitude);

            try {
                // Need to add DynamoDBMapper configuration save behavior because the default save behavior
                // is an UpdateItem request on DynamoDB Table
                mapper.save(cattleItemResult, getPutItemMapperConfig());
            } catch (Exception e) {
                logger.log("Error putting Cattle Item with device id " + deviceId + ": " + e.getMessage());
                return false;
            }
        }
        return true;
    }
}
