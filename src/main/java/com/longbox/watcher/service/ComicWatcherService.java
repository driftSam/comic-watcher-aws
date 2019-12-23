package com.longbox.watcher.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.stream.Stream;

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

	@SuppressWarnings("unchecked")
	public void watch() {
		try {
			WatchService watchService = FileSystems.getDefault().newWatchService();
			Path dir = Paths.get(dirName);
			WatchKey key = dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

			while ((key = watchService.take()) != null) {

				for (WatchEvent<?> event : key.pollEvents()) {
					Path eventPath = ((WatchEvent<Path>) event).context();
					Path child = dir.resolve(eventPath);
					System.out.println("Dir: " + dir.toString());
					System.out.println("EventPath: " + eventPath.toString());
					System.out.println("Child: " + child.toString());
					if (Files.isDirectory(child)) {
						System.out.println("DIR!");
						// TimeUnit.SECONDS.sleep(5);
						Stream<Path> paths = Files.walk(child);

						paths.filter(Files::isRegularFile).forEach(path -> {
							locker(path);
							System.out.println(path.toString());

						});
						;
						paths.close();
					} else {
						System.out.println("NOT DIR!");
						locker(child);
					}
				}

				key.reset();
				System.out.println("done");
			}
		} catch (

		IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// TODO Auto-generated catch block
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private void locker(Path path) {
		File lockFile = path.toFile();
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(lockFile, "rw");

			FileChannel channel = raf.getChannel();
			channel.lock();
			raf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
