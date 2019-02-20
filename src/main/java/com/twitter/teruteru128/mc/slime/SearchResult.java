package com.twitter.teruteru128.mc.slime;

import java.io.Serializable;

class SearchResult implements Serializable {
	private static final long serialVersionUID = 33472L;
	private long seed;
	private long chunkX;
	private long chunkZ;
	private int slimeChunks;
	private int chunks;

	public SearchResult(long seed, long chunkX, long chunkZ, int slimeChunks, int chunks) {
		this.seed = seed;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.slimeChunks = slimeChunks;
		this.chunks = chunks;
	}

	public long getSeed() {
		return seed;
	}

	public long getChunkX() {
		return chunkX;
	}

	public long getChunkZ() {
		return chunkZ;
	}

	public long getX() {
		return chunkX << 4;
	}

	public long getZ() {
		return chunkZ << 4;
	}

	public int getSlimeChunks() {
		return slimeChunks;
	}

	public int getChunks() {
		return chunks;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("'");
		builder.append(seed);
		builder.append(",");
		builder.append(getX());
		builder.append(",");
		builder.append(getZ());
		builder.append(",'");
		builder.append(slimeChunks);
		builder.append("/");
		builder.append(chunks);
		return builder.toString();
	}

}