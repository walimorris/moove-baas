package com.moove.api;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.moove.api.utils.DynamoUtils;

import java.util.Map;

import static com.moove.api.queries.CattleDeviceIdQueries.updateCattleItemAttributes;
import static com.moove.api.utils.DynamoUtils.getAmazonDynamoDBClient;

public class SendLocation {

    private static final String REGION = System.getenv("region");

    public String handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        LambdaLogger logger = context.getLogger();

        AmazonDynamoDB amazonDynamoDB = getAmazonDynamoDBClient(REGION);

        // properties from POST request
        String herdId;
        String deviceId;
        String latitude;
        String longitude;

        // created properties to build device items in herd partion
        long ttl;
        String status = "in";
        String gsi1pk;
        String gsi1sk = DynamoUtils.getTimeStamp();

        boolean isCattleItemUpdated = false;

        if (event.getQueryStringParameters() != null) {

            Map<String, String> requestParameters = event.getQueryStringParameters();

            herdId = requestParameters.get(DynamoUtils.HERD_ID).trim();
            deviceId = requestParameters.get(DynamoUtils.DEVICE_ID).trim();
            latitude = requestParameters.get(DynamoUtils.LATITUDE).trim();
            longitude = requestParameters.get(DynamoUtils.LONGITUDE).trim();

            // set gsipk tp deviceId
            gsi1pk = deviceId;

            isCattleItemUpdated = updateCattleItemAttributes(amazonDynamoDB, deviceId, latitude, longitude, logger);
        } else {
            logger.log("QueryString Parameters are null");
        }
        logger.log("Cattle Item has been added to DDB Streams table: " + isCattleItemUpdated);
        amazonDynamoDB.shutdown();
        return "success\n";
    }
}
