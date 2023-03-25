package com.moove.api.streams;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import java.util.Map;

import static com.moove.api.utils.DynamoUtils.*;

public class GeofenceExitEvent {
    private static final String HERD_ID = "PK";
    private static final String DEVICE_ID = "GSI1PK";
    private static final String DATE_TIME = "GSI1SK";
    private static final String STATUS = "status";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";

    public String handleRequest(DynamodbEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        triggerGeofenceEvent(event, logger);
        return "success\n";
    }

    /**
     * Collects the old image and new image updates from DynamoDB Streams triggered event.
     * Triggers a geofence alert only on updated status attributes that have changed and
     * creates and send message to subscribers.
     *
     * @param event Streams {@link DynamodbEvent}
     * @param logger {@link LambdaLogger}
     */
    private static void triggerGeofenceEvent(DynamodbEvent event, LambdaLogger logger) {
        for (DynamodbStreamRecord record : event.getRecords()) {
            Map<String, AttributeValue> newRecordKeys = record.getDynamodb().getNewImage();
            Map<String, AttributeValue> oldRecordKeys = record.getDynamodb().getOldImage();
            logger.log(event.toString());
            logger.log(newRecordKeys.toString());
            logger.log(oldRecordKeys.toString());
            logger.log(String.valueOf(newRecordKeys.get(DEVICE_ID)));

            // new properties of value
            String herdId = stripDecor(String.valueOf(newRecordKeys.get((HERD_ID))));
            String deviceId = stripDecor(String.valueOf(newRecordKeys.get(DEVICE_ID)));

            String strippedDateTime = stripDecor(String.valueOf(newRecordKeys.get(DATE_TIME)));

            String date = stripDateTimeGSISortKey(strippedDateTime, "date");
            String time = stripDateTimeGSISortKey(strippedDateTime, "time");

            String newStatus = stripDecor(String.valueOf(newRecordKeys.get(STATUS)));
            String latitude = stripDecor(String.valueOf(newRecordKeys.get(LATITUDE)));
            String longitude = stripDecor(String.valueOf(newRecordKeys.get(LONGITUDE)));

            // old properties of value
            String oldStatus = stripDecor(String.valueOf(oldRecordKeys.get(STATUS)));

            // only trigger event if status has been updated, otherwise we don't want to trigger event
            // due to a different attribute change
            if (!newStatus.equals(oldStatus)) {
                String message = createMessage(herdId, deviceId, date, time, latitude, longitude, newStatus);
                logger.log(message);
            }
        }
    }

    /**
     * Creates the geofence status change message with clear data points for the receiver.
     *
     * @param herdId herdId
     * @param deviceId deviceId
     * @param date date
     * @param time time
     * @param latitude latitude
     * @param longitude longitude
     * @param status in/out geofence
     *
     * @return {@link String}
     */
    private static String createMessage(String herdId, String deviceId, String date, String time,
                                        String latitude, String longitude, String status) {

        StringBuilder message = new StringBuilder();
        message.append(String.format("Alert for herd: %s\n", herdId))
                .append(String.format("Cattle DeviceId: %s\n", deviceId))
                .append(String.format("Latitude: %s\n", latitude))
                .append(String.format("Longitude: %s\n", longitude));

        if (status.equals("out")) {
            message.append("Status: has exited geofence area\n");
        } else {
            message.append("Status: has entered geofence area\n");
        }
        message.append(String.format("@date %s\n", date))
                .append(String.format("@time %s\n", time));

        return message.toString();
    }
}
