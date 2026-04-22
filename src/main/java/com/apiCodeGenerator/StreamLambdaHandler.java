package com.apiCodeGenerator;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * AWS Lambda handler for Spring Boot.
 * This class translates AWS Lambda events into Spring Boot web requests.
 */
@Slf4j
public class StreamLambdaHandler implements RequestStreamHandler {
    private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            // Load the Spring Boot application
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(ApiCodeGeneratorApplication.class);
            
            // Set any required profiles (e.g., prod)
            // handler.activateSpringProfiles("prod");
            
        } catch (ContainerInitializationException e) {
            // if we fail here, there is nothing we can do but throw a runtime exception
            log.error("Could not initialize Spring Boot application", e);
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        // Automatically strips the /default/ApiCodeGenerator prefix if it exists in the request
        handler.stripBasePath("/default/ApiCodeGenerator");
        handler.proxyStream(inputStream, outputStream, context);
    }
}
