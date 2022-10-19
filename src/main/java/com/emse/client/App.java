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

/**
 * Lambda function entry point. You can change to use other pojo type or implement
 * a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class App {

        public static void main(String[] args)
    {
        String SampleFileForderPath="C:/Users/quent/Documents/Ecole/2022- 2A/Majeur info/Cloud computing/Project/sales-data/";
        String dateToUpload = "02-10-2022";
        String bucket = "databucket89062";

        S3Client s3Client = S3Client.builder().httpClient(UrlConnectionHttpClient.builder().build()).build();
        SnsClient snsClient = SnsClient.builder().httpClient(UrlConnectionHttpClient.builder().build()).build();
        createBucket(bucket,s3Client); //create bucket if it doesn't exist

        for (int i=1;i<11;i++){ //to upload all the files of a day at once
            String file = dateToUpload+"-store"+i+".csv";
            uploadAFileToABucket(bucket, s3Client, SampleFileForderPath, file);
            publishNotif(List.of("arn:aws:sns:us-east-1:818564790073:StoreSalesTopic", bucket, file), snsClient);
        }

    }

    public static List<Bucket> listBuckets(S3Client s3){
        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse listBucketResponse = s3.listBuckets(listBucketsRequest);
        return listBucketResponse.buckets();
    }

    public static boolean testIfBucketExists(String nameBucket, S3Client s3){
        boolean test=false;
        for (Bucket bucket : listBuckets(s3) ) {
            if(bucket.name().equals(nameBucket)){
                test=true;
            }
        }
        return test;
    }

    public static void createBucket(String nameBucket,S3Client s3){
        if (!testIfBucketExists(nameBucket,s3)) {
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder().bucket(nameBucket).build();
            CreateBucketResponse createBucketResponse = s3.createBucket(createBucketRequest);
        }
    }


    public static void uploadAFileToABucket(String nameBucket,S3Client s3,String path,String name){
        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(nameBucket).key(name).build();
        PutObjectResponse putObjectResponse = s3.putObject(putObjectRequest, RequestBody.fromFile(new File(path+name)));
    }

    public static void publishNotif(List<String> args, SnsClient snsClient) {
        //Region region = Region.US_EAST_1;

        if (args.size() < 3) {
            System.out.println("Missing the Topic ARN, Bucket Name, or File Name arguments");
            System.exit(1);
        }

        String topicARN = args.get(0);
        String bucketName = args.get(1);
        String fileName = args.get(2);

        try {

            PublishRequest request = PublishRequest.builder().message(bucketName + ";" + fileName).topicArn(topicARN)
                    .build();

            PublishResponse snsResponse = snsClient.publish(request);
            System.out.println(
                    snsResponse.messageId() + " Message sent. Status is " + snsResponse.sdkHttpResponse().statusCode());

        } catch (SnsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }


}
