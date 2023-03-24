package com.moove.api;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.moove.api.queries.CattleDeviceIdQueries.putHerdMetaDataLimits;
import static com.moove.api.utils.DynamoUtils.getAmazonDynamoDBClient;

public class SendCoordinateLimits {
    private static final String REGION = System.getenv("region");

    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        AmazonDynamoDB amazonDynamoDBClient = getAmazonDynamoDBClient(REGION);

        if (event.getBody() != null) {
            loadHerdMetaDataLimits(event, amazonDynamoDBClient, logger);
        } else {
            logger.log("Body is empty for SendCoordinateLimits");
        }
        amazonDynamoDBClient.shutdown();

        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setStatusCode(200);

        return response;
    }

    /**
     * Collects {@link ArrayList} of coordinate limits from user geofence creation stored in
     * Json string format. Coordinate limits are in longitude,latitude order in pairs of two.
     *
     * @param coordinatesJSON list of coordinate limits in json string format
     * @param logger {@link LambdaLogger}
     *
     * @return {@link ArrayList} of coordinate limits
     */
    private static ArrayList<String> collectCoordinates(String coordinatesJSON, LambdaLogger logger) {
        JSONObject coordinatesJson = new JSONObject(coordinatesJSON);
        JSONArray coordinatesArray = coordinatesJson.getJSONArray("coordinates");
        ArrayList<String> coordinates = new ArrayList<>();
        for (Object obj : coordinatesArray) {
            String coordinate = String.valueOf(obj);
            coordinates.add(coordinate);
        }
        logger.log("String array: " + coordinates);
        return coordinates;
    }

    /**
     * Get herdId sent from incoming API request event.
     *
     * @param event {@link APIGatewayV2HTTPEvent} incoming request event
     * @param logger {@link LambdaLogger}
     *
     * @return {@link String} herdId
     */
    private static String collectHerdIdQueryParameter(APIGatewayV2HTTPEvent event, LambdaLogger logger) {
        return event.getQueryStringParameters().get("herdId");
    }

    /**
     * Sends a PutItem request on DynamoDB Table, putting the metaDataLimits attribute on
     * the herdId partition it belongs to.
     *
     * @param event {@link APIGatewayV2HTTPEvent} incoming request
     * @param amazonDynamoDB {@link AmazonDynamoDB}
     * @param logger {@link LambdaLogger}
     *
     * @see com.moove.api.models.HerdMetaData
     */
    private static void loadHerdMetaDataLimits(APIGatewayV2HTTPEvent event, AmazonDynamoDB amazonDynamoDB, LambdaLogger logger) {
        ArrayList<String> coordinates = collectCoordinates(event.getBody(), logger);
        String herdId = collectHerdIdQueryParameter(event, logger);
        if (herdId != null && !coordinates.isEmpty()) {

            // add metaDataLimits to DynamoDB partion with herdId
            boolean isMetaLimitsSaved = putHerdMetaDataLimits(amazonDynamoDB, logger, herdId, coordinates);
            if (isMetaLimitsSaved) {
                logger.log("MetaLimits Saved on herdId: " + herdId);
            }
        }
    }
}
