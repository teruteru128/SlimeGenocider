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
		long minChunkX = this.minChunkX;
		long maxChunkX = this.maxChunkX;
		long minChunkZ = this.minChunkZ;
		long maxChunkZ = this.maxChunkZ;
		int countRengeX = this.countRengeX;
		int countRengeZ = this.countRengeZ;
		maxChunkX -= countRengeX - 1;
		maxChunkX -= countRengeZ - 1;
		int minSlimeChunks = this.minSlimeChunks;
		int searchSeeds = this.searchSeeds;
		int max = 0;
		LinkedList<SearchResult> results = new LinkedList<>();
		SlimeChunkOracle oracle = new SlimeChunkOracle(0);
		long chunkX = 0;
		long chunkZ = 0;
		int slimeChunkCount = 0;
		int chunkCount = 0;
		long currentSeed = 0;
		int exChunkX = 0;
		int exChunkZ = 0;
		long taskStarted = System.currentTimeMillis();
		long z = -1;
		long x = -1;
		for (int i = 0; i < searchSeeds; i++) {
			currentSeed = initialSeed++;
			oracle.setSeed(currentSeed);

			// TODO DO TEST
			for (chunkZ = minChunkZ; chunkZ < maxChunkZ; chunkZ += 2) {
				for (chunkX = minChunkX; chunkX < maxChunkX; chunkX += 2) {
					if (oracle.isSlimeChunk(chunkX + 2, chunkZ) && oracle.isSlimeChunk(chunkX + 2, chunkZ + 2)) {
						if (oracle.isSlimeChunk(chunkX, chunkZ) && oracle.isSlimeChunk(chunkX + 2, chunkZ)) {
							// TODO チェックを注入可能に
							for (z = -1; z < 1; z++) {
								for (x = -1; x < 1; x++) {
									slimeChunkCount = 4;
									chunkCount = 4;
									for (exChunkZ = 0; exChunkZ < countRengeZ; exChunkZ++) {
										for (exChunkX = 0; exChunkX < countRengeX; exChunkX++) {
											if ((x + exChunkX) % 2 != 0 || (z + exChunkZ) % 2 != 0) {
												// System.out.printf("%d, %d%n", x + exChunkX, z + exChunkZ);
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
										System.out.printf("won! : %s, %d(x=%d,z=%d,%s)%n", result, chunkCount, x, z,
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
		System.out.printf("task finished: max chunk is %d/%d, %.2fseeds/s %s%n", max, countRengeX * countRengeZ,
				searchSeeds / ((double) diff / 1000), max >= minSlimeChunks ? "yay!!!" : "booooo");
		return results;
	}
}
