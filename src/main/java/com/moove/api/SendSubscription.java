package com.moove.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import org.json.JSONObject;

import static com.moove.api.utils.DynamoUtils.getAmazonSNSClient;

public class SendSubscription {
    private static final String REGION = System.getenv("region");
    private static final String SNS_ARN = System.getenv("sns_topic_arn");

    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        LambdaLogger logger = context.getLogger();

        String email = decodeEmailAddressFromRequest(event, logger);
        AmazonSNS amazonSNS = getAmazonSNSClient(REGION);
        subscribeRancherToSNSTopic(amazonSNS, logger, email);

        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setStatusCode(200);
        return response;
    }

    /**
     * Decodes email address sent from subscribe request.
     *
     * @param event {@link APIGatewayV2HTTPEvent}
     * @param logger {@link LambdaLogger}
     *
     * @return {@link String}
     */
    private static String decodeEmailAddressFromRequest(APIGatewayV2HTTPEvent event, LambdaLogger logger) {
        JSONObject emailJson = new JSONObject(event.getBody());
        return emailJson.getString("email");
    }

    /**
     * Subscribe email address to SNS topic.
     *
     * @param amazonSNS {@link AmazonSNS}
     * @param logger {@link LambdaLogger}
     *
     * @param email email endpoint to subscribe to topic
     */
    private static void subscribeRancherToSNSTopic(AmazonSNS amazonSNS, LambdaLogger logger, String email) {
        SubscribeRequest subscribeRequest = new SubscribeRequest()
                .withTopicArn(SNS_ARN)
                .withProtocol("email")
                .withEndpoint(email)
                .withReturnSubscriptionArn(false);
        SubscribeResult subscribeResult = amazonSNS.subscribe(subscribeRequest);
        logger.log("subscribe result status = " + subscribeResult.getSdkHttpMetadata().getHttpStatusCode());
    }
}
