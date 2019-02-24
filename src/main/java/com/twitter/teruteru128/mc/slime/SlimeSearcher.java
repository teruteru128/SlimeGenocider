package com.twitter.teruteru128.mc.slime;

import java.time.LocalDateTime;
import java.util.BitSet;
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
		long offsetX = -minChunkX;
		long offsetZ = -minChunkZ;
		long searchRangeX = maxChunkX - minChunkX;
		long searchRangeZ = maxChunkZ - minChunkZ;
		BitSet slimeMemo = new BitSet((int) (searchRangeX * searchRangeZ));
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
		for (int i = 0; i < searchSeeds; i++) {
			currentSeed = initialSeed++;
			oracle.setSeed(currentSeed);
			for (chunkZ = minChunkZ; chunkZ < maxChunkZ; chunkZ++) {
				for (chunkX = minChunkX; chunkX < maxChunkX; chunkX++) {
					// TODO getIndex関数化
					slimeMemo.set((int) ((chunkZ + offsetZ) * searchRangeX + chunkX + offsetX),
							oracle.isSlimeChunk(chunkX, chunkZ));
				}
			}
			// TODO 探索アルゴリズム改良
			for (chunkZ = minChunkZ; chunkZ < maxChunkZ; chunkZ++) {
				for (chunkX = minChunkX; chunkX < maxChunkX; chunkX++) {
					if (slimeMemo.get((int) ((chunkZ + offsetZ) * searchRangeX + chunkX + offsetX))) {
						slimeChunkCount = 0;
						chunkCount = 0;
						// TODO チェックを注入可能に
						for (exChunkZ = 0; exChunkZ < countRengeZ; exChunkZ++) {
							for (exChunkX = 0; exChunkX < countRengeX; exChunkX++) {
								chunkCount++;
								if (slimeMemo.get((int) ((chunkZ + offsetZ + exChunkZ) * searchRangeX + chunkX + offsetX
										+ exChunkX))) {
									slimeChunkCount++;
								}
							}
						}
						max = Math.max(slimeChunkCount, max);
						if (slimeChunkCount >= minSlimeChunks) {
							SearchResult result = new SearchResult(currentSeed, chunkX, chunkZ, slimeChunkCount,
									chunkCount);
							results.add(result);
							System.out.printf("won! : %s(%s)%n", result, LocalDateTime.now());
						}
					}
				}
			}
			slimeMemo.clear();
		}
		long taskFinished = System.currentTimeMillis();
		long diff = taskFinished - taskStarted;
		System.out.printf("task finished: max chunk is %d/%d, %.2fseeds/s %s%n", max, countRengeX * countRengeZ,
				((double) searchSeeds / diff) / 1000, max >= minSlimeChunks ? "yay!!!" : "booooo");
		return results;
	}
}
