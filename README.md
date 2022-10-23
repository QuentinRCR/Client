#  Instructions to run the project

## Step 0: Setup
1. Follow the first part of the attached video to set up the AWS services
2. The following information should be kept somewhere to quickly access it later:
   + The name of the S3 bucket to upload the csv files to
   + The arn of the SNS topic
   + the url of the SQS queue for the Java application version of the Worker
3. Open the Worker project and navigate to the Processor class
4. Modify the value of sqsUrl to the url stored in (2.) right above
5. Compile the code with "mvn package"
6. The generated Worker.jar will have to be uploaded to two places:
   + Upload to the Lambda and set the function to SqsEventHandler::handleRequest
   + Upload to the EC2 with the command:
     >scp -i {security key} {path to Worker.jar} {path of destination}
   + An example command:
     >scp -i "awskey.pem" Worker.jar ec2-user@ec2-52-90-7-118.compute-1.amazonaws.com:/home/ec2-user
7. Configure the Lambda following the video
8. The Java application can be launched with the command:
   >java -jar Worker.jar --interval 120
   
   If the interval option isn't specified, it defaults to 120 seconds
9. Remember that to test one of the implementations of the Worker,
   you should disable the subscription of the other's SNS to SQS
   in order to avoid having unprocessed messages stacking up in one of both queues

## Step 1: Client
1. Open the Client project and navigate to the Client class main function
2. Modify the 3 arguments: 
   + csvFilePath (enter the absolute path of the file to upload)
   + bucketName (see Step 0 - 2.)
   + snsArn (see Step 0 - 2.)
3. Run the main function

   This will upload the file to the S3 bucket and send an SNS notification


## Step 2: Worker

### Lambda
1. Check the Cloudwatch logs to see if the Lambda was triggered correctly
2. Check the bucket to see if files have been processed

   The files will get renamed from DD-MM-YYYY-store.csv to DD-MM-YYYY_store.csv if processed

   This makes it easier to later extract the date from the file name in the Consolidator


### Java Application
1. Watch the console where you launched the application to see what it outputs

   The application version can also be launched after the files were uploaded, since it regularly checks the queue
2. Check the bucket to see if files have been processed

   The files will get renamed from DD-MM-YYYY-store.csv to DD-MM-YYYY_store.csv if processed

   This makes it easier to later extract the date from the file name in the Consolidator


## Step 3: Consolidator
1. Open the Consolidator project and navigate to the Consolidator class main function
2. Modify the 2 arguments:
   + bucketName (see Step 0 - 2.)
   + date (which you wish to get the statistics from)
3. Run the main function
   
   This should display the computed statistics in the console

   The Consolidator does not output any file