package com.moove.api;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.moove.api.utils.DynamoUtils;

import java.util.Map;

import static com.moove.api.queries.CattleDeviceIdQueries.putDeviceItem;
import static com.moove.api.utils.DynamoUtils.getAmazonDynamoDBClient;
import static com.moove.api.utils.DynamoUtils.getTTL;

public class SendLocation {

    private static final String REGION = System.getenv("region");

    public String handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        LambdaLogger logger = context.getLogger();

        AmazonDynamoDB amazonDynamoDB = getAmazonDynamoDBClient(REGION);

        // created properties to build device items in herd partition
        long ttl = getTTL(30);
        String status = "in";
        String gsi1pk;
        String gsi1sk = DynamoUtils.getTimeStamp();

        boolean isCattleItemUpdated = false;

        if (event.getQueryStringParameters() != null) {

            Map<String, String> requestParameters = event.getQueryStringParameters();

            String herdId = requestParameters.get(DynamoUtils.HERD_ID).trim();
            String deviceId = requestParameters.get(DynamoUtils.DEVICE_ID).trim();
            String latitude = requestParameters.get(DynamoUtils.LATITUDE).trim();
            String longitude = requestParameters.get(DynamoUtils.LONGITUDE).trim();

            // set gsipk tp deviceId
            gsi1pk = deviceId;
            deviceId = gsi1sk + "#" + deviceId;
            herdId = "HERD#" + herdId;

            isCattleItemUpdated = putDeviceItem(amazonDynamoDB, herdId, deviceId, latitude, longitude,
                    ttl, status, gsi1pk, gsi1sk, logger);
        } else {
            logger.log("QueryString Parameters are null");
        }
        logger.log("Cattle Item has been added to DDB Streams table: " + isCattleItemUpdated);
        amazonDynamoDB.shutdown();
        return "success\n";
    }
}
