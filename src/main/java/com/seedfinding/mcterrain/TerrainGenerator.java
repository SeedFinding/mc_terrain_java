package com.seedfinding.mcterrain;

import com.seedfinding.mcterrain.terrain.EndTerrainGenerator;
import com.seedfinding.mcterrain.terrain.NetherTerrainGenerator;
import com.seedfinding.mcterrain.terrain.OverworldTerrainGenerator;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.block.Block;
import com.seedfinding.mccore.block.Blocks;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mccore.version.UnsupportedVersion;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class TerrainGenerator {

	protected boolean amplified;
	protected final BiomeSource biomeSource;
	protected final MCVersion version;
	// only 5 blocks are registered here END_STONE, AIR, LAVA, WATER, NETHERRACK, STONE
	public static Predicate<Block> WORLD_SURFACE_WG = b -> b.getId() != Blocks.AIR.getId();
	// only fluid have no colliders
	public static Predicate<Block> OCEAN_FLOOR_WG = b -> b.getId() != Blocks.AIR.getId() && b.getId() != Blocks.WATER.getId() && b.getId() != Blocks.LAVA.getId();
	// there is more predicate but terrainutils is not ready yet

	public TerrainGenerator(BiomeSource biomeSource) {
		this.biomeSource = biomeSource;
		this.version = biomeSource.getVersion();
		this.amplified = false;
		if(this.version.isOlderThan(MCVersion.v1_8)) {
			throw new UnsupportedVersion(this.version, "chunk generator");
		}
		if(this.version.isOlderThan(MCVersion.v1_11)) {
			System.err.println("WARNING THIS VERSION IS UNTESTED YET");
		}
	}

	public TerrainGenerator setAmplified(boolean amplified){
		this.amplified=amplified;
		return this;
	}

	public static Factory factory(Dimension dimension) {
		if(dimension == Dimension.OVERWORLD) return OverworldTerrainGenerator::new;
		else if(dimension == Dimension.NETHER) return NetherTerrainGenerator::new;
		else if(dimension == Dimension.END) return EndTerrainGenerator::new;
		return null;
	}

	public static TerrainGenerator of(Dimension dimension, BiomeSource biomeSource) {
		Factory factory = factory(dimension);
		return factory == null ? null : factory.create(biomeSource);
	}

	public static TerrainGenerator of(BiomeSource biomeSource) {
		Factory factory = factory(biomeSource.getDimension());
		return factory == null ? null : factory.create(biomeSource);
	}

	public abstract Dimension getDimension();

	public BiomeSource getBiomeSource() {
		return biomeSource;
	}

	public MCVersion getVersion() {
		return version;
	}

	public long getWorldSeed() {
		return biomeSource.getWorldSeed();
	}

	public int getSeaLevel() {
		return 63;
	}

	public int getMinWorldHeight() {
		return 0;
	}

	public int getMaxWorldHeight() {
		return getWorldHeight() - getMinWorldHeight();
	}

	public abstract int getWorldHeight();

	public abstract Block getDefaultBlock();

	public abstract Block getDefaultFluid();

	protected abstract void sampleNoiseColumnOld(double[] buffer, int x, int z, double depth, double scale);

	/**
	 * Returns the lowest non fluid block (this means not water nor air) y value - 1 to accommodate its position
	 */
	public int getHeightInGround(int x, int z) {
		return this.getHeightOnGround(x, z) - 1;
	}

	/**
	 * Returns the lowest non fluid block (this means not water nor air) y value
	 */
	public abstract int getHeightOnGround(int x, int z);

	/**
	 * Returns the first y value that match a given predicate
	 */
	public abstract int getFirstHeightInColumn(int x, int z, Predicate<Block> predicate);

	/**
	 * Returns the block column at x,z of length: worldHeight, this column has 3 blocks tops, default block, default fluid and air
	 */
	public abstract Block[] getColumnAt(int x, int z);

	/**
	 * Returns the block column at x,z of length: worldHeight, this column has 3 blocks tops, default block, default fluid and air
	 * You must provide the jigsaw application for it to return the correct results, use #getColumnAt(x,z) if not.
	 */
	public abstract Block[] getColumnAt(int x, int z, List<Pair<Supplier<Integer>, BlockBox>> jigsawBoxes, List<BPos> jigsawJunction);

	/**
	 * Compute the column and replace the blocks with their biomes counterpart,
	 * warning this implementation use the non jigsaw hulk and will not be accurate for those (1.14+)
	 */
	public abstract Block[] getBiomeColumnAt(int x,int z);

	/**
	 * Compute the column and replace the blocks with their biomes counterpart,
	 * accounting for jigsaw shennanigans
	 */
	public abstract Block[] getBiomeColumnAt(int x, int z, List<Pair<Supplier<Integer>, BlockBox>> jigsawBoxes, List<BPos> jigsawJunction);

	/**
	 * Compute the column and replace the lower part with the bedrock (aka as a full surface chunk)
	 * warning this is the non jigsaw one for 1.13- version, also this will generate a FULL chunk (can not do otherwise)
	 */
	public abstract Block[] getBedrockColumnAt(int x,int z);

	/**
	 * Compute the column and replace the lower part with the bedrock (aka as a full surface chunk)
	 * warning this is the jigsaw one for 1.14+ version, also this will generate a FULL chunk (can not do otherwise)
	 */
	public abstract Block[] getBedrockColumnAt(int x, int z, List<Pair<Supplier<Integer>, BlockBox>> jigsawBoxes, List<BPos> jigsawJunction);

	/**
	 * Returns the block at x,y,z, this block can be 3 blocks tops, default block, default fluid and air
	 * If out of bounds, return Empty.
	 */
	public abstract Optional<Block> getBlockAt(int x, int y, int z);

	/**
	 * {@link #getBlockAt(int, int, int)}
	 * @param pos the world position
	 * @return the block or Empty
	 */
	public Optional<Block> getBlockAt(BPos pos){
		return getBlockAt(pos.getX(),pos.getY(),pos.getZ());
	}

	@FunctionalInterface
	public interface Factory {
		TerrainGenerator create(BiomeSource biomeSource);
	}

}
