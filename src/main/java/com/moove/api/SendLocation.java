package com.moove.api;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreams;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior;
import com.moove.api.models.CattleItem;

import java.util.Map;

public class SendLocation {
    public static final String DEVICE_ID = "deviceId";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    public String handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        LambdaLogger logger = context.getLogger();

        AmazonDynamoDB amazonDynamoDB = getAmazonDynamoDBClient();

        String deviceId;
        String latitude;
        String longitude;

        boolean isCattleItemUpdated = false;

        if (event.getQueryStringParameters() != null) {

            Map<String, String> requestParameters = event.getQueryStringParameters();

            deviceId = requestParameters.get(DEVICE_ID).trim();
            latitude = requestParameters.get(LATITUDE).trim();
            longitude = requestParameters.get(LONGITUDE).trim();

            isCattleItemUpdated = updateCattleItemAttributes(amazonDynamoDB, deviceId, latitude, longitude, logger);
        } else {
            logger.log("QueryString Parameters are null");
        }
        logger.log("Cattle Item has been added to DDB Streams table: " + isCattleItemUpdated);
        amazonDynamoDB.shutdown();
        return "success\n";
    }

    /**
     * Provides configuration setting for {@link AmazonDynamoDB} and {@link AmazonDynamoDBStreams}
     * clients. Configurations come with default settings for SDK retry, error, and timeout logic,
     * as a few examples, though custom configurations provide a way to customize this SDK logic
     * based on your application needs.
     *
     * @return custom {@link ClientConfiguration}
     */
    private static ClientConfiguration dynamoClientConfiguration() {
        return new ClientConfiguration()
                .withConnectionTimeout(120)
                .withMaxErrorRetry(3)
                .withThrottledRetries(true);
    }

    /**
     * Builds a {@link AmazonDynamoDB} client with a custom configuration for connection timeout,
     * maximumErrorRetries, and Throtted retries set to true.
     *
     * @return {@link AmazonDynamoDB} client
     */
    private static AmazonDynamoDB getAmazonDynamoDBClient() {
        return AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_WEST_2)
                .withClientConfiguration(dynamoClientConfiguration())
                .build();
    }

    /**
     * DynamoDBMapper save behavior is a UpdateItem request by default. In order to configure a
     * PutItem save behavior this function builds a DynamoDBMapper configuration with the PutItem
     * save behavior.
     *
     * @see SaveBehavior
     * @see SaveBehavior#PUT
     * @see SaveBehavior#DEFAULT
     *
     * @return {@link DynamoDBMapperConfig}
     */
    public static DynamoDBMapperConfig getPutItemMapperConfig() {
        return new DynamoDBMapperConfig.Builder()
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.PUT)
                .build();
    }

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
    public CattleItem queryCattleItemByDeviceId(AmazonDynamoDB amazonDynamoDB, String deviceId, LambdaLogger logger) {
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
            logger.log("Error querying " + deviceId + " on the " + "cattle-herd");
            return new CattleItem();
        }
    }

    /**
     * Puts or Updates initial Cattle device data (deviceId, latitude, longitude) on DynamoDB Table,
     * given the results from querying the given cattle deviceId.
     *
     * @param amazonDynamoDB {@link AmazonDynamoDB} client
     * @param deviceId cattle deviceId
     * @param latitude device latitude
     * @param longitude device longitude
     * @param logger {@link LambdaLogger
     * }
     * @see CattleItem
     *
     * @return boolean
     */
    public boolean updateCattleItemAttributes(AmazonDynamoDB amazonDynamoDB, String deviceId, String latitude,
                                              String longitude, LambdaLogger logger) {

        DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);
        CattleItem cattleItemResult = queryCattleItemByDeviceId(amazonDynamoDB, deviceId, logger);

        // Received a hit on a CattleItem with the given deviceId
        if (cattleItemResult.getDeviceId() != - 1) {
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
            cattleItemResult.setDeviceId(Integer.parseInt(deviceId));
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
