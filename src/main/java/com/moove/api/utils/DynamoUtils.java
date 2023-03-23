package com.moove.api.utils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreams;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DynamoUtils {

    public static final String HERD_ID = "herdId";
    public static final String DEVICE_ID = "deviceId";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    /**
     * Provides configuration setting for {@link AmazonDynamoDB} and {@link AmazonDynamoDBStreams}
     * clients. Configurations come with default settings for SDK retry, error, and timeout logic,
     * as a few examples, though custom configurations provide a way to customize this SDK logic
     * based on your application needs.
     *
     * @return custom {@link ClientConfiguration}
     */
    public static ClientConfiguration dynamoClientConfiguration() {
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
    public static AmazonDynamoDB getAmazonDynamoDBClient(String region) {
        return AmazonDynamoDBClientBuilder.standard()
                .withRegion(region)
                .withClientConfiguration(dynamoClientConfiguration())
                .build();
    }

    /**
     * DynamoDBMapper save behavior is a UpdateItem request by default. In order to configure a
     * PutItem save behavior this function builds a DynamoDBMapper configuration with the PutItem
     * save behavior.
     *
     * @see DynamoDBMapperConfig.SaveBehavior
     * @see DynamoDBMapperConfig.SaveBehavior#PUT
     * @see DynamoDBMapperConfig.SaveBehavior#DEFAULT
     *
     * @return {@link DynamoDBMapperConfig}
     */
    public static DynamoDBMapperConfig getPutItemMapperConfig() {
        return new DynamoDBMapperConfig.Builder()
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.PUT)
                .build();
    }

    /**
     * An Update is the default save behavior for {@link DynamoDBMapper#save(Object)} operations.
     * This method allows the client to be explicit on update save behavior.
     *
     * @return {@link DynamoDBMapperConfig}
     */
    public static DynamoDBMapperConfig getUpdateItemMapperConfig() {
        return new DynamoDBMapperConfig.Builder()
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE)
                .build();
    }

    /**
     * {@link DynamoDBMapper#save(Object)} has a default Update save behavior and updates a specific
     * attribute, which includes NULL attributes on the data model. Therefore, NULL attributes on a
     * save operation will also be updated to NULL. The {@link SaveBehavior#UPDATE_SKIP_NULL_ATTRIBUTES}
     * ignores NULL attributes on the model object passed to {@link DynamoDBMapper#save(Object)}. This
     * allows our table to retain attributes it already stores.
     *
     * @return {@link DynamoDBMapperConfig}
     */
    public static DynamoDBMapperConfig getSkipUpdateNullAttributesMapperConfig() {
        return new DynamoDBMapperConfig.Builder()
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
                .build();
    }

    /**
     * Converts a {@link String} coordinate value to a double.
     *
     * @param coordinate latitude or longitude coordinate
     * @return double
     */
    public static double convertCoordinateToDouble(String coordinate) {
        return Double.parseDouble(coordinate);
    }

    /**
     * Based on the query operation, return items may contain extra "decor" such as brackets or
     * commas. In order to operate on the return item values, this method removes the decor in
     * order to obtain the raw value.
     *
     * @param str {@link String}
     * @return {@link String}
     */
    public static String stripDecor(String str) {
        return str.substring(3, str.length() - 2).trim();
    }

    /**
     * Gets current timestamp in format HH:mm:ss
     *
     * @return {@link String}
     */
    public static String getTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        String currentDate = dateFormat.format(date.getTime());
        String currentTime = timeFormat.format(date.getTime());
        return currentDate + "T" + currentTime;
    }

    /**
     * Gets a time to live epoch value in seconds based on the given days in the future.
     *
     * @param days days in future
     * @return long
     */
    public static long getTTL(int days) {
        return Instant.now().plus(days, ChronoUnit.DAYS).getEpochSecond();
    }
}
