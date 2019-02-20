package com.twitter.teruteru128.mc.slime;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

	public static void main(String[] args) {
		ExecutorService service = Executors.newWorkStealingPool();
		SecureRandom random = new SecureRandom();
		List<SlimeSearcher> tasks = new LinkedList<>();
		Path outPath = Paths.get(".", "out.csv");
		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
		System.out.printf("start : %s%n", formatter.format(LocalDateTime.now()));
		try (FileChannel channel = FileChannel.open(outPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
			FileLock lock = channel.tryLock();
			// Lockのnullチェックやらないといけないのめんどい
			if (Objects.isNull(lock)) {
				// Fileのロックが取れませんでした
				throw new RuntimeException(MessageFormat.format("other process locked. file={0}", outPath));
			}
			try (PrintStream stream = new PrintStream(Channels.newOutputStream(channel))) {

				int total = 0;
				// 一度に8スレッドずつを32回
				for (int i = 0; i < 128; i++) {
					for (int j = 0; j < 128; j++) {
						tasks.add(new SlimeSearcher(random.nextLong(), -313, 312, -313, 312, 4, 4, 14, 1 << 16));
					}
					List<Future<List<SearchResult>>> futures = service.invokeAll(tasks);
					int subtotal = 0;
					for (Future<List<SearchResult>> future : futures) {
						for (SearchResult result : future.get()) {
							stream.println(result);
							subtotal++;
						}
					}
					System.out.printf("subtotal : %d(%s)%n", subtotal, formatter.format(LocalDateTime.now()));
					total += subtotal;
					tasks.clear();
				}
				System.out.printf("total : %d(%s)%n", total, formatter.format(LocalDateTime.now()));
			}
		} catch (InterruptedException | ExecutionException | IOException e) {
			throw new RuntimeException(e);
		} finally {
			service.shutdown();
		}
	}

}
