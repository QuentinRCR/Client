# Project documentation

## Proposed solution architecture
- The client application add the stores to an AWS s3 bucket and publish an SNS notification for each store with "bucketName;fileName" as a message
##### Case 1
- The SNS send the request to an SNS queue so that if the lambda function is not working, the message remains in queue
- If everything is working correctly, the notification in the SNS is treated by the lambda function.
- The input file is delete and the output file is create
- The SQS message is automatically deleted by the lambda function
##### Case 2
- The SNS send the request to another SNS queue
- This queue is read by the java application that is run in a EC2 instance
- This java application periodically check if there is any new message on the queue
- If it is the case, the input files are delete and output files is created
- The SQS message is deleted by the java application


- The consolidator reads all the documents, select only the documents that are outputs from the worker and that are at the correct date
- It compiles the results of this day and displays them as logs

## Justification of the proposed architecture
### Justification of AWS services used and the architecture
- The files need to stored somewhere, so we use the Amazon S3 which is the service to do that
- We use the SNS service to do an interface to be able to choose which type of worker is going to treat the fill
- We send the SNS notification to a SQS queue because the SQS has a memory in case the worker part is not working
- Using a lambda function is the logical choice because we need to execute an action as soon as a file arrives
- We are forced to use a EC2 instance to run the java application because it is the tool to run a server indefinitely which allows us to check periodically if there is a notification

## Qualitative comparison between the Worker implemented