package com.twitter.teruteru128.mc.slime;

import java.time.LocalDateTime;
import java.util.Arrays;
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
		final int minChunkX = (int) this.minChunkX;
		final int maxChunkX = (int) this.maxChunkX;
		final int minChunkZ = (int) this.minChunkZ;
		final int maxChunkZ = (int) this.maxChunkZ;
		final int countRangeX = this.countRengeX;
		final int countRangeZ = this.countRengeZ;
		final int minSlimeChunks = this.minSlimeChunks;
		int searchSeeds = this.searchSeeds;
		final int offsetX = -minChunkX;
		final int offsetZ = -minChunkZ;
		final int searchRangeX = maxChunkX - minChunkX;
		final int searchRangeZ = maxChunkZ - minChunkZ;
		BitSet slimeMemo = new BitSet(searchRangeX * searchRangeZ);
		byte[][] chunkSum = new byte[searchRangeZ][searchRangeX];
		int max = 0;
		LinkedList<SearchResult> results = new LinkedList<>();
		SlimeChunkOracle oracle = new SlimeChunkOracle(0);
		int chunkX = 0;
		int chunkZ = 0;
		long currentSeed = 0;
		long taskStarted = System.currentTimeMillis();
		int z = -1;
		int x = -1;
		for (int i = 0; i < searchSeeds; i++) {
			currentSeed = initialSeed++;
			oracle.setSeed(currentSeed);
			for (chunkZ = minChunkZ; chunkZ < maxChunkZ; chunkZ++) {
				for (chunkX = minChunkX; chunkX < maxChunkX; chunkX++) {
					boolean value = oracle.isSlimeChunk(chunkX, chunkZ);
					// TODO getIndex関数化
					slimeMemo.set((chunkZ + offsetZ) * searchRangeX + chunkX + offsetX, value);
				}
			}
			for (chunkZ = minChunkZ + 1; chunkZ < maxChunkZ - countRangeZ + 2; chunkZ++) {
				for (chunkX = minChunkX + 1; chunkX < maxChunkX - countRangeX + 2; chunkX++) {
					if (slimeMemo.get((chunkZ + offsetZ + 2) * searchRangeX + chunkX + offsetX)
							&& slimeMemo.get((chunkZ + offsetZ + 2) * searchRangeX + chunkX + offsetX + 2)) {
						for (z = -1; z < 3; z++) {
							for (x = -1; x < 3; x++) {
								if (slimeMemo.get((chunkZ + offsetZ + z) * searchRangeX + chunkX + offsetX + x)) {
									chunkSum[chunkZ + offsetZ][chunkX + offsetX] += 1;
								}
							}
						}
					}
				}
			}
			for (chunkZ = minChunkZ + 1; chunkZ < maxChunkZ; chunkZ++) {
				for (chunkX = minChunkX + 1; chunkX < maxChunkX; chunkX++) {
					max = Math.max(chunkSum[chunkZ + offsetZ][chunkX + offsetX], max);
					if (chunkSum[chunkZ + offsetZ][chunkX + offsetX] >= minSlimeChunks) {
						SearchResult result = new SearchResult(currentSeed, chunkX - 1, chunkZ - 1,
								chunkSum[chunkZ + offsetZ][chunkX + offsetX], countRangeZ * countRangeX);
						results.add(result);
						System.out.printf("won! : %s(%s)%n", result, LocalDateTime.now());
						// System.out.printf("%d%n", chunkSum[chunkZ + offsetZ][chunkX + offsetX]);
					}
				}
			}
			for (z = 0; z < searchRangeZ; z++) {
				Arrays.fill(chunkSum[z], (byte) 0);
			}
		}
		long taskFinished = System.currentTimeMillis();
		long diff = taskFinished - taskStarted;
		System.out.printf("task finished: max chunk is %d/%d, %.2fseeds/s %s%n", max, countRangeX * countRangeZ,
				searchSeeds / ((double) diff / 1000), max >= minSlimeChunks ? "yay!!!" : "booooo");
		return results;
	}
}
