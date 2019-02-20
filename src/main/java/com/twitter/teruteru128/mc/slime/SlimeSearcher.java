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
		for (int i = 0; i < searchSeeds; i++) {
			// XXX イニシャルシードをインクリメントしてカレントシードに設定しても問題ないのでは？
			currentSeed = initialSeed++;
			oracle.setSeed(currentSeed);
			for (chunkX = minChunkX; chunkX < maxChunkX; chunkX++) {
				for (chunkZ = minChunkZ; chunkZ < maxChunkZ; chunkZ++) {
					if (oracle.isSlimeChunk(chunkX, chunkZ)) {
						slimeChunkCount = 0;
						chunkCount = 0;
						// TODO チェックを注入可能に
						for (exChunkX = 0; exChunkX < countRengeX; exChunkX++) {
							for (exChunkZ = 0; exChunkZ < countRengeZ; exChunkZ++) {
								chunkCount++;
								if (oracle.isSlimeChunk(chunkX + exChunkX, chunkZ + exChunkZ)) {
									slimeChunkCount++;
								}
							}
						}
						max = Math.max(slimeChunkCount, max);
						if (slimeChunkCount >= minSlimeChunks) {
							SearchResult result = new SearchResult(currentSeed, chunkX, chunkZ, slimeChunkCount,
									chunkCount);
							results.add(result);
							System.out.printf("won! : %s,%s(%s)%n", result, Thread.currentThread().toString(),
									LocalDateTime.now());
						}
					}
				}
			}
		}
		System.out.printf("max : %d%n", max);
		return results;
	}
}
