package com.USWCicrcleLink.server.global.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Value("${cloud.aws.s3.credentials}")
    private String credentialsType;

    @Value("${cloud.aws.s3.region}")
    private String region;

    @Value("${cloud.aws.credentials.access-key:#{null}}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:#{null}}")
    private String secretKey;

    @Bean
    public AmazonS3 amazonS3() {
        AWSCredentialsProvider awsCredentialsProvider;

        if ("instance-profile".equals(credentialsType)) {
            awsCredentialsProvider = new InstanceProfileCredentialsProvider(false);
        } else if (accessKey != null && secretKey != null && !accessKey.isBlank() && !secretKey.isBlank()) {
            awsCredentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
        } else {
            awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();
        }

        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(awsCredentialsProvider) // 선택된 자격 증명 공급자 적용
                .build();
    }
}
