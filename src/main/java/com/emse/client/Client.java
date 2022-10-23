package com.emse.client;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class Client {
    public static void main(String[] args) {
        //Enter the absolute path of the file you wish to upload e.g. "/home/user/sales/01-10-2022-store1.csv"
        String csvFilePath= "";
        //Enter the bucket name you wish to upload the csv files to (if it does not already exist, it will be automatically crated
        String bucketName = "";
        //Enter the topic ARN of the SNS you have configured
        String snsArn = "";

        S3Client s3Client = S3Client.builder().httpClient(UrlConnectionHttpClient.builder().build()).build();
        SnsClient snsClient = SnsClient.builder().httpClient(UrlConnectionHttpClient.builder().build()).build();
        createBucket(s3Client, bucketName); //if the bucket does not exist, create a new one

        String[] s = csvFilePath.split("/");
        String fileName = s[s.length -1];

        uploadFileToBucket(s3Client, bucketName, csvFilePath, fileName);
        publishSnsNotification(snsClient, snsArn, bucketName, fileName);
    }

    public static List<Bucket> listBuckets(S3Client s3Client) {
        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse listBucketResponse = s3Client.listBuckets(listBucketsRequest);
        return listBucketResponse.buckets();
    }

    public static boolean bucketExists(S3Client s3Client, String bucketName) {
        return listBuckets(s3Client).stream().map(Bucket::name).collect(Collectors.toList()).contains(bucketName);
    }

    public static void createBucket(S3Client s3Client, String nameBucket){
        if (!bucketExists(s3Client, nameBucket)) {
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder().bucket(nameBucket).build();
            s3Client.createBucket(createBucketRequest);
        }
    }

    public static void uploadFileToBucket(S3Client s3Client, String bucketName, String path, String fileName){
        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(fileName).build();
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(new File(path)));
    }

    public static void publishSnsNotification(SnsClient sns, String arn, String bucketName, String fileName) {
        try {
            PublishRequest request = PublishRequest.builder().message(bucketName + ";" + fileName).topicArn(arn).build();
            PublishResponse snsResponse = sns.publish(request);
            System.out.println(snsResponse.messageId() + " Message sent. Status is " + snsResponse.sdkHttpResponse().statusCode());
        } catch (SnsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
}
