package com.longbox.watcher.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ComicWatcherService {
	@Value("${raw.comics.dir}")
	String dirName;

	@Value("${exchange.name}")
	String exchangeName;

	@Value("${queue.name}")
	String queueName;

	@Autowired
	RabbitTemplate rabbitTemplate;

	public void watch() {
		try {
			WatchService watchService = FileSystems.getDefault().newWatchService();
			Path rawDir = Paths.get(dirName);
			rawDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

			WatchKey key;

			while ((key = watchService.take()) != null) {
				for (WatchEvent<?> event : key.pollEvents()) {
					System.out.println("New Comic Found!");
					System.out.println("File: " + event.context());
					Path comicPath = Paths.get(rawDir.toString(), event.context().toString());
					File lockFile = comicPath.toFile();
					RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
					FileChannel channel = raf.getChannel();
					channel.lock();
					raf.close();
					System.out.println(comicPath.toString());
					rabbitTemplate.convertAndSend(exchangeName, "found", comicPath.toString());
				}
				key.reset();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}