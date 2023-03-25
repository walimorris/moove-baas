package com.moove.api;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.json.JSONObject;

import static com.moove.api.queries.CattleDeviceIdQueries.updateDeviceItem;
import static com.moove.api.utils.DynamoUtils.getAmazonDynamoDBClient;

public class SendStatusUpdate {
    private static final String REGION = System.getenv("region");

    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("inside SendStatusUpdate");
        AmazonDynamoDB amazonDynamoDBClient = getAmazonDynamoDBClient(REGION);

        if (event.getBody() != null) {
            loadStatusChange(event, amazonDynamoDBClient, logger);
        } else {
            logger.log("Body is empty for SendStatusUpdate");
        }
        amazonDynamoDBClient.shutdown();

        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setStatusCode(200);
        return response;
    }

    private static String collectStatus(String statusJSON, LambdaLogger logger) {
        JSONObject statusJson = new JSONObject(statusJSON);
        return String.valueOf(statusJson.get("status"));
    }

    private static void loadStatusChange(APIGatewayV2HTTPEvent event, AmazonDynamoDB amazonDynamoDB, LambdaLogger logger) {
        String status = collectStatus(event.getBody(), logger);
        String herdId = event.getQueryStringParameters().get("pk");
        String SK = event.getQueryStringParameters().get("sk");
        if (herdId != null && SK != null) {
            boolean isStatusUpdated = updateDeviceItem(amazonDynamoDB, logger, herdId, SK, status);
            if (isStatusUpdated) {
                logger.log("Status updated to " + status + " in herdId: " + herdId + " deviceId SK: " + SK);
            } else {
                logger.log("Error updating status on herdId: " + herdId + " deviceId SK: " + SK);
            }
        }
    }
}
