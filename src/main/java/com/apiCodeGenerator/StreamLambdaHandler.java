package com.apiCodeGenerator;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
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
    private static final SpringBootLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            // FORCED FIX: These MUST be set before the handler is initialized
            System.setProperty("server.servlet.encoding.enabled", "false");
            System.setProperty("spring.main.allow-bean-definition-overriding", "true");
            
            // In v3.0.0, use the unified class with the HttpApiV2 factory method
            handler = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(ApiCodeGeneratorApplication.class);

            handler.activateSpringProfiles("prod");
            
            // IMPORTANT: Tell the handler to treat ZIP files as binary data
            handler.getContainerConfig().addBinaryContentTypes("application/zip", "application/octet-stream");
            
        } catch (ContainerInitializationException e) {
            log.error("Could not initialize Spring Boot application", e);
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        // Strip the stage prefix if it exists, otherwise keep the path as is
        handler.stripBasePath("/default");
        handler.proxyStream(inputStream, outputStream, context);
    }
}
