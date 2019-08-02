package com.longbox.watcher.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
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
		WatchService watchService;
		try {
			watchService = FileSystems.getDefault().newWatchService();

			Path rawDir = Paths.get(dirName);
			rawDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

			WatchKey key;

			while ((key = watchService.take()) != null) {

				for (WatchEvent<?> event : key.pollEvents()) {
					Path eventPath = Paths.get(rawDir.toString(), event.context().toString());
					System.out.println("EventPath: " + eventPath.toString());
					if (Files.isDirectory(eventPath)) {
						System.out.println("DIR!");
						// THERE ARE LOCK ISSUES SOMEWHERE AROUND HERE
						DirectoryStream<Path> stream = Files.newDirectoryStream(eventPath);
						stream.forEach(path -> {
							try {
								System.out.println("in lamda: " + path.toString());
								sendMessage(path);

							} catch (IOException e) { // TODO Auto-generated catch block
								e.printStackTrace();
							}

						});
					} else {
						System.out.println("NOT DIR!");
						sendMessage(Paths.get(eventPath.toString()));
					}
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

	private void sendMessage(Path comicPath) throws IOException {
		System.out.println("COMIC PATH: " + comicPath.toString());
		File lockFile = comicPath.toFile();
		RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
		FileChannel channel = raf.getChannel();
		channel.lock();
		raf.close();
		rabbitTemplate.convertAndSend(exchangeName, "found", comicPath.toString());
	}
}
