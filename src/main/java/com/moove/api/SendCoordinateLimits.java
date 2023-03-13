package com.moove.api;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.moove.api.utils.DynamoUtils;

import java.util.Map;

import static com.moove.api.queries.CattleDeviceIdQueries.updateCattleItemAttributes;
import static com.moove.api.utils.DynamoUtils.getAmazonDynamoDBClient;

public class SendCoordinateLimits {

    public String handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        AmazonDynamoDB amazonDynamoDB = getAmazonDynamoDBClient();
        boolean isLimitsStored = false;

        if (event.getQueryStringParameters() != null) {
            Map<String, String> parameters = event.getQueryStringParameters();

            String deviceId = parameters.get(DynamoUtils.DEVICE_ID).trim();
            String latitude = parameters.get(DynamoUtils.LATITUDE).trim();
            String longitude = parameters.get(DynamoUtils.LONGITUDE).trim();

            if (deviceId.equals(String.valueOf(0))) {
                isLimitsStored = updateCattleItemAttributes(amazonDynamoDB, deviceId, latitude, longitude, logger);
            } else {
                logger.log("Incorrect deviceId, can not store coordinate limits");
            }
        }
        logger.log("Coordinates Limits added to cattle-herding table: " + isLimitsStored);
        amazonDynamoDB.shutdown();
        return "success\n";
    }
}
