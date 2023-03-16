package com.moove.api.streams;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.location.LocationClient;
import software.amazon.awssdk.services.location.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.moove.api.utils.DynamoUtils.*;

public class StartCoordinatesWorkflow {

    private static final String DEVICE = "Device";
    private static final String HERD_TRACKER = "Herd-tracker";
    private static final String HERD_TRACKER_KEY = System.getenv("herd_tracker_key");
    private static final String REGION = System.getenv("region");
    private static final String AWS_ACCOUNT_ID = System.getenv("aws_account_id");
    private static final String HERD_GEOFENCE_ID = System.getenv("geofence_id");

    public String handleRequest(DynamodbEvent event, Context context) {
        LambdaLogger logger = context.getLogger();

        LocationClient locationClient = LocationClient.builder()
                .region(Region.of(REGION))
                .build();

        ListGeofenceCollectionsRequest listGeofenceCollectionsRequest = ListGeofenceCollectionsRequest.builder()
                .maxResults(1)
                .build();

        ListGeofenceCollectionsResponse geofenceCollectionsResult = locationClient.listGeofenceCollections(listGeofenceCollectionsRequest);
        List<ListGeofenceCollectionsResponseEntry> geofenceResponseEntries = geofenceCollectionsResult.entries();

        String herdGeofenceCollectionName = null;
        ListGeofenceCollectionsResponseEntry herdGeofenceEntry = null;
        if (geofenceResponseEntries.isEmpty()) {
            logger.log("NO Geofence Collections");
        } else {
            logger.log("FOUND GEOFENCE COLLECTION");
            for (ListGeofenceCollectionsResponseEntry entry : geofenceResponseEntries) {
                if (entry.collectionName().equals("HerdGeofence")) {
                    herdGeofenceEntry = entry;
                }
            }
        }
        String herdGeofenceId = null;
        if (herdGeofenceEntry != null) {
            herdGeofenceCollectionName = herdGeofenceEntry.collectionName();
            ListGeofencesRequest listGeofencesRequest = ListGeofencesRequest.builder()
                    .collectionName(herdGeofenceCollectionName)
                    .build();

            ListGeofencesResponse geofencesResult = locationClient.listGeofences(listGeofencesRequest);
            List<ListGeofenceResponseEntry> geofenceResponseEntry = geofencesResult.entries();
            if (!geofenceResponseEntry.isEmpty()) {
                for (ListGeofenceResponseEntry entry : geofenceResponseEntry) {
                    herdGeofenceId = entry.geofenceId();
                }
            } else {
                // create geofence
                double latitude = 00.00000; // 39.963222
                double longitude = -00.000000; // -83.444424
                Circle circle = Circle.builder()
                        .center(latitude, longitude)
                        .radius(100.0)
                        .build();

                GeofenceGeometry geofenceGeometry = GeofenceGeometry.builder()
                        .circle(circle)
                        .build();

                PutGeofenceRequest putGeofenceRequest = PutGeofenceRequest.builder()
                        .collectionName("HerdGeofence")
                        .geofenceId(HERD_GEOFENCE_ID)
                        .geometry(geofenceGeometry)
                        .build();


                // create tracker
                CreateTrackerRequest createTrackerRequest = CreateTrackerRequest.builder()
                        .trackerName(HERD_TRACKER)
                        .description("Tracker for cattle-herd devices")
                        .kmsKeyId(HERD_TRACKER_KEY)
                        .positionFiltering(PositionFiltering.DISTANCE_BASED)
                        .build();

                locationClient.putGeofence(putGeofenceRequest);
                locationClient.createTracker(createTrackerRequest);


                // associate tracker with geofence
                AssociateTrackerConsumerRequest associateTrackerConsumerRequest = AssociateTrackerConsumerRequest.builder()
                        .trackerName(createTrackerRequest.trackerName())
                        .consumerArn(String.format("arn:aws:geo:%s:%s:geofence-collection/%s", REGION, AWS_ACCOUNT_ID, herdGeofenceCollectionName))
                        .build();

                locationClient.associateTrackerConsumer(associateTrackerConsumerRequest);

            }
        }

        GetGeofenceRequest getGeofenceRequest = GetGeofenceRequest.builder()
                .geofenceId(herdGeofenceId)
                .collectionName(herdGeofenceCollectionName)
                .build();

        GetGeofenceResponse getGeofenceResponse = locationClient.getGeofence(getGeofenceRequest);
        GeofenceGeometry herdGeometry = getGeofenceResponse.geometry();

        // batch evaluate positions can only evaluate at most 10 positions
        // in this case, we have to add a list of ten positions to a list
        // that holds all position lists
        List<List<DevicePositionUpdate>> devicePositionUpdates = new ArrayList<>();
        List<DevicePositionUpdate> devicePositions = new ArrayList<>();

        List<DynamodbStreamRecord> eventRecords = event.getRecords();
        for (DynamodbStreamRecord record : eventRecords) {
            Map<String, AttributeValue> newRecordKeys = record.getDynamodb().getNewImage();

            logger.log("new image=" + newRecordKeys.toString());

            double latitude = convertCoordinateToDouble(stripDecor(String.valueOf(newRecordKeys.get(LATITUDE))));
            double longitude = convertCoordinateToDouble(stripDecor(String.valueOf(newRecordKeys.get(LONGITUDE))));
            String deviceId = stripDecor(String.valueOf(newRecordKeys.get(DEVICE_ID)));

            DevicePositionUpdate devicePositionUpdate = DevicePositionUpdate.builder()
                    .deviceId(DEVICE + deviceId + "-EXAMPLE")
                    .position(latitude, longitude)
                    .sampleTime(Instant.EPOCH)
                    .build();

            BatchUpdateDevicePositionRequest batchUpdateDevicePositionRequest = BatchUpdateDevicePositionRequest.builder()
                    .trackerName(HERD_TRACKER)
                    .updates(devicePositionUpdate)
                    .build();

            BatchUpdateDevicePositionResponse batchUpdateDevicePositionResponse = locationClient.batchUpdateDevicePosition(batchUpdateDevicePositionRequest);
            logger.log("any batch update errors = " + batchUpdateDevicePositionResponse.hasErrors());
            logger.log("batch update error size=" + batchUpdateDevicePositionResponse.errors().size());
            if (batchUpdateDevicePositionResponse.hasErrors()) {
                List<BatchUpdateDevicePositionError> errors = batchUpdateDevicePositionResponse.errors();
                for (BatchUpdateDevicePositionError error: errors) {
                    logger.log("Error deviceId" + error.deviceId() + "error msg: " + error.error().message());
                }
            }

            // store 10 device positions in a single list, when that list contains 10 position
            // updates, add that list of 10 to the overall devicePositionUpdates list and clear
            // the device positions list to allow the store of more device position updates
            devicePositions.add(devicePositionUpdate);
            if (devicePositions.size() == 10) {
                devicePositionUpdates.add(devicePositions);
                devicePositions = new ArrayList<>();
            }
        }

        // device position updates list will hold and multi list that contain no more than
        // 10 device position updates. Here, we pull each list of ten and batch evaluate
        // the device positions in the list to a geofence collection
        for (List<DevicePositionUpdate> devicePositionUpdate : devicePositionUpdates) {
            BatchEvaluateGeofencesRequest evaluateGeofencesRequest = BatchEvaluateGeofencesRequest.builder()
                    .collectionName(herdGeofenceCollectionName)
                    .devicePositionUpdates(devicePositionUpdate)
                    .build();

            locationClient.batchEvaluateGeofences(evaluateGeofencesRequest);
        }

        return "success\n";
    }
}
