# Project documentation

## 1. Architecture description
In order to fulfil all the requirements, we have come up with the following solution for each part of the requested architecture:

- The <u>Client</u> application adds the sales data from each store to an AWS S3 bucket and publishes an SNS notification with the following format: "bucketName;fileName".
- The <u>Worker</u> is implemented in two ways: a lambda function and a Java application. 
  Both implementations produce the same output, which is a csv file with processed information to be used in the next step.

>##### Case 1
>- The SNS sends the message to a SQS queue which can store the messages in case the lambda is unavailable for whatever reason.
>- If everything is working correctly, the notification in the SQS is handled by the lambda function.
>- The output file is generated in the same S3 bucket as the input, although with a different name to make the date easily extractable for the next step (the input file is deleted).
>- The SQS message is automatically deleted by the lambda function if the SQS event is handled properly.

>##### Case 2
>- The SNS sends the message to a SQS queue, so that the messages can stay in memory even if the Worker application is not running.
>- The Worker application which is running in a EC2 instance checks the SQS queue periodically (with a configurable interval) to receive the messages.
>- The output and input is handled in the same way as case 1.
>- The SQS message gets explicitly deleted by the Java application.

- When given a date, the <u>Consolidator</u> reads the pre-processed files to output interesting statistical data in the console.

## 2. Architecture explanation
### Why use these AWS services?

- It is required that the stores can upload their daily sales file to a cloud storage, which is exactly what the S3 offers. 
  Not only can it store files, it is also simple to integrate it with other AWS services, which can simplify the processing later down the line.
- The SNS service is a choice we have taken because it can not only send messages to other parts of the system, but can also be configured to notify other applications.
- The SQS service has been inserted between the SNS and the Worker in order to store the messages in case of a downtime, to guarantee that every uploaded file gets processed at some point with no data loss.
- The Lambda service is one obvious solution as it allows to assign triggers to automate the processing of sales files, in other terms, we have an automated Worker.
- The EC2 is used to run the Worker differently, which is not triggered by an SQS event, but with periodic checks.
  Its main purpose is to compare its performance to a Lambda implementation.

### Lambda vs Java application

- As required, we have done some testing regarding the processing time of the sales files in both implementations.
- For the Lambda implementation, we observe times between 7 and 8 seconds.
- For the Java application, we observe times between 4 and 5 seconds.
- Although the difference isn't much in the absolute, it still is massive in relative terms, which shows the benefits of the application approach.
- But in terms of simplicity and architectural logic, it seems that the Lambda would be the preferred solution.
  Not only can it entirely automate the Worker, it can also handle SQS events, which allows for a faster reaction required by the users.
  Furthermore, if is far easier for the users to work with the Lambda which only requires configuring the Java code and triggers.
  The EC2 would require some additional work for setting it up, maintaining and troubleshooting since it is a service allowing many other operations.