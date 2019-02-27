package com.twitter.teruteru128.mc.slime;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

class SlimeSearcher implements Callable<List<SearchResult>> {
	private long initialSeed;
	private long minChunkX;
	private long maxChunkX;
	private long minChunkZ;
	private long maxChunkZ;
	private int countRengeX;
	private int countRengeZ;
	private int minSlimeChunks;
	private int searchSeeds;

	public SlimeSearcher(long initialSeed, long minChunkX, long maxChunkX, long minChunkZ, long maxChunkZ,
			int countRengeX, int countRengeZ, int minSlimeChunks, int searchSeeds) {
		this.initialSeed = initialSeed;
		this.minChunkX = minChunkX;
		this.maxChunkX = maxChunkX;
		this.minChunkZ = minChunkZ;
		this.maxChunkZ = maxChunkZ;
		this.countRengeX = countRengeX;
		this.countRengeZ = countRengeZ;
		this.minSlimeChunks = minSlimeChunks;
		this.searchSeeds = searchSeeds;
	}

	@Override
	public List<SearchResult> call() {
		long initialSeed = this.initialSeed;
		final int minChunkX = (int) this.minChunkX;
		final int maxChunkX = (int) this.maxChunkX;
		final int minChunkZ = (int) this.minChunkZ;
		final int maxChunkZ = (int) this.maxChunkZ;
		final int countRangeX = this.countRengeX;
		final int countRangeZ = this.countRengeZ;
		final int minSlimeChunks = this.minSlimeChunks;
		int searchSeeds = this.searchSeeds;
		int max = 0;
		LinkedList<SearchResult> results = new LinkedList<>();
		SlimeChunkOracle oracle = new SlimeChunkOracle(0);
		int chunkX = 0;
		int chunkZ = 0;
		int exChunkX = 0;
		int exChunkZ = 0;
		int z = -1;
		int x = -1;
		long currentSeed = 0;
		int slimeChunkCount = 0;
		int chunkCount = 0;
		long taskStarted = System.currentTimeMillis();
		for (int i = 0; i < searchSeeds; i++) {
			currentSeed = initialSeed++;
			oracle.setSeed(currentSeed);

			for (chunkZ = minChunkZ; chunkZ < maxChunkZ; chunkZ += 2) {
				for (chunkX = minChunkX; chunkX < maxChunkX; chunkX += 2) {
					if (oracle.isSlimeChunk(chunkX + 2, chunkZ) && oracle.isSlimeChunk(chunkX + 2, chunkZ + 2)) {
						if (oracle.isSlimeChunk(chunkX, chunkZ) && oracle.isSlimeChunk(chunkX + 2, chunkZ)) {
							for (z = -1; z < 1; z++) {
								for (x = -1; x < 1; x++) {
									slimeChunkCount = 4;
									chunkCount = 4;
									for (exChunkZ = 0; exChunkZ < countRangeZ; exChunkZ++) {
										for (exChunkX = 0; exChunkX < countRangeX; exChunkX++) {
											if ((x + exChunkX) % 2 != 0 || (z + exChunkZ) % 2 != 0) {
												chunkCount++;
												if (oracle.isSlimeChunk(chunkX + exChunkX + x, chunkZ + exChunkZ + z)) {
													slimeChunkCount++;
												}
											}
										}
									}
									max = Math.max(slimeChunkCount, max);
									if (slimeChunkCount >= minSlimeChunks) {
										SearchResult result = new SearchResult(currentSeed, chunkX + x, chunkZ + z,
												slimeChunkCount, chunkCount);
										results.add(result);
										System.out.printf("won! : %s, %d(%s)%n", result, chunkCount,
												LocalDateTime.now());
									}
								}
							}
						}
					} else {
						chunkX += 2;
					}
				}
			}

		}
		long taskFinished = System.currentTimeMillis();
		long diff = taskFinished - taskStarted;
		System.out.printf("task finished: max chunk is %d/%d, %.2fseeds/s %s%n", max, countRangeX * countRangeZ,
				searchSeeds / ((double) diff / 1000), max >= minSlimeChunks ? "yay!!!" : "booooo");
		return results;
	}
}
