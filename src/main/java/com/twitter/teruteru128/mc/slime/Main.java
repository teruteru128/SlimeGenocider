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
import java.time.Duration;
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
		// 北西のチャンク座標
		final long minSearchChunkX = -313;
		final long minSearchChunkZ = -313;
		// 南東のチャンク座標
		final long maxSearchChunkX = 312;
		final long maxSearchChunkZ = 312;
		final int xRange = 4;
		final int zRange = 4;
		final int minSlimeChunk = 15;
		final int numberOfSections = 128;
		final int processors = Runtime.getRuntime().availableProcessors();
		final int useThreads = processors * 3 / 4;
		final int tasksPerSection = useThreads * 12;
		final int searcherTaskSize = 65536;
		ExecutorService service = Executors.newWorkStealingPool(useThreads);
		SecureRandom random = new SecureRandom();
		List<SlimeSearcher> tasks = new LinkedList<>();
		Path outPath = Paths.get(".", "out.csv");
		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
		LocalDateTime generalStart = LocalDateTime.now();
		LocalDateTime sectionStart = LocalDateTime.now();
		LocalDateTime sectionFinish = null;
		System.out.printf("start : %s%n", formatter.format(generalStart));
		try (FileChannel channel = FileChannel.open(outPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
			FileLock lock = channel.tryLock();
			// Lockのnullチェックやらないといけないのめんどい
			if (Objects.isNull(lock)) {
				// Fileのロックが取れませんでした
				throw new RuntimeException(MessageFormat.format("other process locked. file={0}", outPath));
			}
			try (PrintStream stream = new PrintStream(Channels.newOutputStream(channel))) {

				int totalSlimeChunkSeeds = 0;
				long sectionSeedSize = 0;
				long searchedSeedSize = 0;
				// 一度に8スレッドずつを32回
				for (int i = 0; i < numberOfSections; i++) {
					sectionStart = LocalDateTime.now();
					sectionSeedSize = 0;
					for (int j = 0; j < tasksPerSection; j++) {
						tasks.add(new SlimeSearcher(random.nextLong(), minSearchChunkX, maxSearchChunkX,
								minSearchChunkZ, maxSearchChunkZ, xRange, zRange, minSlimeChunk, searcherTaskSize));
						sectionSeedSize += searcherTaskSize;
					}
					List<Future<List<SearchResult>>> futures = service.invokeAll(tasks);
					sectionFinish = LocalDateTime.now();
					searchedSeedSize += sectionSeedSize;
					int subtotal = 0;
					for (Future<List<SearchResult>> future : futures) {
						for (SearchResult result : future.get()) {
							stream.println(result);
							subtotal++;
						}
					}
					stream.flush();
					Duration diff = Duration.between(sectionStart, sectionFinish);
					totalSlimeChunkSeeds += subtotal;
					System.out.printf(
							"subtotal : This section is %d seeds searched, %d seed(s) found and saved. %.2fseeds/s Total %d seeds searched.(%s)%n",
							sectionSeedSize, subtotal, sectionSeedSize / ((double) diff.toMillis() / 1000),
							searchedSeedSize, formatter.format(sectionFinish));
					tasks.clear();
				}
				System.out.printf("total : %d seeds found in %d seeds. %.2fseeds/s (%s)%n", totalSlimeChunkSeeds,
						searchedSeedSize,
						searchedSeedSize / ((double) Duration.between(generalStart, sectionFinish).toMillis() / 1000),
						formatter.format(LocalDateTime.now()));
			}
		} catch (InterruptedException | ExecutionException | IOException e) {
			throw new RuntimeException(e);
		} finally {
			service.shutdown();
		}
	}

}
