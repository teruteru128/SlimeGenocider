package com.twitter.teruteru128.mc.slime;

import java.util.Random;

/**
 * @author toolbox4minecraft https://github.com/toolbox4minecraft/amidst Released under the GPL v3 Licenses.
 * */
public class SlimeChunkOracle {
	private long seed;
	private Random random = new Random();

	public SlimeChunkOracle(long seed) {
		this.seed = seed;
	}

	public boolean isSlimeChunk(long chunkX, long chunkY) {
		Random random = this.random;
		random.setSeed(getSeed((int) chunkX, (int) chunkY));
		return isSlimeChunk(random);
	}

	/**
	 * Make sure this uses integers for the parameters chunkX and chunkY and long
	 * for the seed. This ensure the overflow works as in Minecraft.
	 */
	private long getSeed(int chunkX, int chunkY) {
		return seed + chunkX * chunkX * 0x4c1906 + chunkX * 0x5ac0db + chunkY * chunkY * 0x4307a7L + chunkY * 0x5f24f
				^ 0x3ad8025f;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	private boolean isSlimeChunk(Random random) {
		return random.nextInt(10) == 0;
	}
}
