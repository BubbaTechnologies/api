package com.bubbaTech.api.aws;

import com.bubbaTech.api.info.ServiceLogger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

@Service
public class LambdaService {
    @Value("${system.aws.accessKey}")
    private String accessKey;
    @Value("${system.aws.privateKey}")
    private String privateKey;
    private ServiceLogger logger;

    public Boolean useLambda(String service, JSONObject requestBody) {
        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, privateKey);
            LambdaClient lambdaClient = LambdaClient.builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName(service)
                    .payload(SdkBytes.fromUtf8String(requestBody.toJSONString()))
                    .build();

            InvokeResponse invokeResponse = lambdaClient.invoke(invokeRequest);
            if (invokeResponse.statusCode() != 200) {
                String errorMessage = "Unable to connect to emailClient";
                logger.error(errorMessage);
                throw new Exception(errorMessage);
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
