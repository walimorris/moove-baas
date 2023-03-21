package com.moove.api.utils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreams;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DynamoUtils {

    public static final String LIMITS_ID = "0";
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

    public static double convertCoordinateToDouble(String coordinate) {
        return Double.parseDouble(coordinate);
    }

    public static String stripDecor(String str) {
        return str.substring(3, str.length() - 2).trim();
    }

    public static String getTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date.getTime());
    }

    public static long getTTL(int days) {
        return Instant.now().plus(days, ChronoUnit.DAYS).getEpochSecond();
    }
}
