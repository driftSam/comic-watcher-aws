package com.longbox.watcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.longbox.watcher.service.ComicWatcherService;

@SpringBootApplication
public class ComicWatcherApplication implements CommandLineRunner {

	@Autowired
	ComicWatcherService service;

	private static Logger LOG = LoggerFactory.getLogger(ComicWatcherApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ComicWatcherApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		LOG.info("watching");
		service.watch();
	}

}
