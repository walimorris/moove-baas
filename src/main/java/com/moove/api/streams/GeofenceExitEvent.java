package com.moove.api.streams;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

public class GeofenceExitEvent {
    public String handleRequest(ScheduledEvent event, Context context) {
        LambdaLogger logger = context.getLogger();

        logger.log(event.getDetail().toString());
        return "success\n";
    }
}
