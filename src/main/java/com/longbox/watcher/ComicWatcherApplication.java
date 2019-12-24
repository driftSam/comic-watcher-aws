package com.longbox.watcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.longbox.watcher.service.ComicWatcherService;

@SpringBootApplication
public class ComicWatcherApplication implements CommandLineRunner {

	@Autowired
	ComicWatcherService service;

	private static Logger LOG = LoggerFactory.getLogger(ComicWatcherApplication.class);
	
	@Bean
	@Primary
	public AmazonS3 amazonS3Client (AWSCredentialsProvider awsCredProvider) {
		return AmazonS3ClientBuilder
				.standard()
				.withCredentials(awsCredProvider)
				.withRegion("us-east-1")
				.build();
	}



	public static void main(String[] args) {
		SpringApplication.run(ComicWatcherApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		LOG.info("watching");
		service.watch();
	}

}
