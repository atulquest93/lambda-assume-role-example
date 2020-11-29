package com.quest.organization.datapipeline.movement;

import java.util.Arrays;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;

public class LambdaAssumeRole implements RequestHandler<S3Event, String> {

	@Override
	public String handleRequest(S3Event s3Event, Context context) {

		String clientRegion = "eu-west-2";
		String targetRoleArn = "target-role-arn";
		String assumedRoleName = "target-assumed-role";

		AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard().withRegion(clientRegion)
				.build();
		//build sts client, pass role to be assumed. 

		AssumeRoleRequest roleRequest = new AssumeRoleRequest().withRoleArn(targetRoleArn)
				.withRoleSessionName(assumedRoleName);

		AssumeRoleResult assumeRoleResult = stsClient.assumeRole(roleRequest);

		Credentials sessionCredentials = assumeRoleResult.getCredentials();

		BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
				sessionCredentials.getAccessKeyId(), sessionCredentials.getSecretAccessKey(),
				sessionCredentials.getSessionToken());

		AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(basicSessionCredentials);

		try {
			
			AmazonS3 s3client = AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider)
					.withRegion(clientRegion).build(); //create service, with assumed credintails. 

			System.out.println(Arrays.toString(s3client.listBuckets().toArray())); //print buckets

		} catch (AmazonServiceException ase) {

			ase.printStackTrace();
		} catch (AmazonClientException ace) {

			ace.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "SUCCESS";
	}
}
