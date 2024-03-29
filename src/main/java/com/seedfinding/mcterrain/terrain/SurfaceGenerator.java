package com.seedfinding.mcterrain.terrain;

import com.seedfinding.mcbiome.biome.Biome;
import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.block.Block;
import com.seedfinding.mccore.block.Blocks;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.data.Triplet;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcnoise.noise.NoiseSampler;
import com.seedfinding.mcnoise.perlin.OctavePerlinNoiseSampler;
import com.seedfinding.mcnoise.perlin.PerlinNoiseSampler;
import com.seedfinding.mcnoise.simplex.OctaveSimplexNoiseSampler;
import com.seedfinding.mcnoise.utils.MathHelper;
import com.seedfinding.mcseed.lcg.LCG;
import com.seedfinding.mcterrain.TerrainGenerator;
import com.seedfinding.mcterrain.utils.NoiseSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.seedfinding.mcnoise.utils.MathHelper.maintainPrecision;
import static com.seedfinding.mcterrain.utils.MathHelper.*;

public abstract class SurfaceGenerator extends TerrainGenerator {

	protected static final float[] BIOME_WEIGHT_TABLE = new float[25];
	protected static final float[] BEARD_KERNEL = new float[13824];

	static {
		for(int rx = -2; rx <= 2; ++rx) {
			for(int rz = -2; rz <= 2; ++rz) {
				float f = 10.0F / sqrt((float)(rx * rx + rz * rz) + 0.2F);
				BIOME_WEIGHT_TABLE[rx + 2 + (rz + 2) * 5] = f;
			}
		}
		for(int i = 0; i < 24; ++i) {
			for(int j = 0; j < 24; ++j) {
				for(int k = 0; k < 24; ++k) {
					BEARD_KERNEL[i * 24 * 24 + j * 24 + k] = (float)computeContribution(j - 12, k - 12, i - 12);
				}
			}
		}
	}

	protected final OctavePerlinNoiseSampler depthNoise;
	protected final ChunkRand random;
	private final int chunkHeight;
	private final int chunkWidth;
	private final int noiseSizeX;
	private final int noiseSizeY;
	private final int noiseSizeZ;
	private final NoiseSettings noiseSettings;
	private final OctavePerlinNoiseSampler minLimitPerlinNoise;
	private final OctavePerlinNoiseSampler maxLimitPerlinNoise;
	private final OctavePerlinNoiseSampler mainPerlinNoise;
	private final NoiseSampler surfaceDepthNoise;
	private final OctavePerlinNoiseSampler scaleNoise;
	private final double densityFactor;
	private final double densityOffset;
	private final Map<Long, double[]> noiseColumnCache = new HashMap<>();
	private final Map<Long, Block[]> columnCache = new HashMap<>();
	private final Map<Long, Block[]> jigsawColumnCache = new HashMap<>();
	private final Map<Long, Block[]> biomeColumnCache = new HashMap<>();
	private final Map<Long, Block[]> biomeJigsawColumnCache = new HashMap<>();
	private final Map<Long, Long> chunkSeeds = new HashMap<>();
	private final Map<Long, Long> jigsawChunkSeeds = new HashMap<>();
	private final int worldHeight;

	private static final LCG SCALE_ADVANCE = LCG.JAVA.combine(2620);
	private static final LCG SURFACE_ADVANCE = LCG.JAVA.combine(1048);

	public SurfaceGenerator(BiomeSource biomeSource,
							int worldHeight,
							int horizontalNoiseResolution,
							int verticalNoiseResolution,
							NoiseSettings noiseSettings,
							double densityFactor,
							double densityOffset,
							boolean useSimplexNoise) {
		super(biomeSource);
		this.worldHeight = worldHeight;
		this.chunkHeight = verticalNoiseResolution * 4;
		this.chunkWidth = horizontalNoiseResolution * 4;
		this.noiseSettings = noiseSettings;
		this.noiseSizeX = 16 / this.chunkWidth;
		this.noiseSizeY = worldHeight / this.chunkHeight;
		this.noiseSizeZ = 16 / this.chunkWidth;
		this.densityFactor = densityFactor;
		this.densityOffset = densityOffset;

		this.random = new ChunkRand(biomeSource.getWorldSeed());
		if(version.isOlderThan(MCVersion.v1_15)) {
			// those lines can be simplified to setting the lacunarity correctly to the lowest (FIXME)
			this.minLimitPerlinNoise = new OctavePerlinNoiseSampler(this.random, 16);
			this.maxLimitPerlinNoise = new OctavePerlinNoiseSampler(this.random, 16);
			this.mainPerlinNoise = new OctavePerlinNoiseSampler(this.random, 8);
		} else {
			this.minLimitPerlinNoise = new OctavePerlinNoiseSampler(this.random, IntStream.rangeClosed(-15, 0));
			this.maxLimitPerlinNoise = new OctavePerlinNoiseSampler(this.random, IntStream.rangeClosed(-15, 0));
			this.mainPerlinNoise = new OctavePerlinNoiseSampler(this.random, IntStream.rangeClosed(-7, 0));
		}

		if(version.isOlderOrEqualTo(MCVersion.v1_13_2) && getDimension() == Dimension.END) {
			this.surfaceDepthNoise = null;
		} else {
			if(version.isOlderOrEqualTo(MCVersion.v1_13_2) && getDimension() == Dimension.NETHER) {
				this.random.advance(SURFACE_ADVANCE);
			}

			if(version.isOlderThan(MCVersion.v1_15)) {
				this.surfaceDepthNoise = useSimplexNoise ? new OctaveSimplexNoiseSampler(this.random, 4) :
					new OctavePerlinNoiseSampler(this.random, 4);
			} else {
				this.surfaceDepthNoise = useSimplexNoise ? new OctaveSimplexNoiseSampler(this.random, IntStream.rangeClosed(-3, 0)) :
					new OctavePerlinNoiseSampler(this.random, IntStream.rangeClosed(-3, 0));
			}
		}


		if(version.isOlderOrEqualTo(MCVersion.v1_13_2)) {
			// this is important as nextInt can skip...
			scaleNoise = new OctavePerlinNoiseSampler(this.random, 10);
		} else {
			scaleNoise = null;
			this.random.advance(SCALE_ADVANCE);
		}

		if(version.isOlderThan(MCVersion.v1_15)) {
			this.depthNoise = new OctavePerlinNoiseSampler(this.random, 16);
		} else {
			this.depthNoise = new OctavePerlinNoiseSampler(this.random, IntStream.rangeClosed(-15, 0));
		}

		// this.EndNoise=new OctavePerlinNoiseSampler(new ChunkRand(biomeSource.getWorldSeed()), 4);

	}

	public int getMinWorldHeight() {
		return 0;
	}

	public int getMaxWorldHeight() {
		return getWorldHeight() - getMinWorldHeight();
	}

	public int getWorldHeight() {
		return worldHeight;
	}

	public abstract Block getDefaultBlock();

	public abstract Block getDefaultFluid();

	public abstract int getBedrockRoofPosition();

	public abstract int getBedrockFloorPosition();

	public int noiseSizeY() {
		return this.noiseSizeY + 1;
	}

	public double getMaxNoiseY() {
		return (double)this.noiseSizeY() - 4.0D;
	}

	public double getMinNoiseY() {
		return 0.0D;
	}

	private double sampleNoise(int x, int y, int z) {
		double xzScale = NoiseSettings.COORDINATE_SCALE * noiseSettings.samplingSettings.xzScale;
		double yScale = NoiseSettings.HEIGHT_SCALE * noiseSettings.samplingSettings.yScale;
		double xzStep = xzScale / noiseSettings.samplingSettings.xzFactor;
		double yStep = yScale / noiseSettings.samplingSettings.yFactor;

		double minNoise = 0.0D;
		double maxNoise = 0.0D;
		double mainNoise = 0.0D;
		double persistence = 1.0D;

		for(int octave = 0; octave < 16; ++octave) {
			double cellX = maintainPrecision((double)x * xzScale * persistence);
			double cellY = maintainPrecision((double)y * yScale * persistence);
			double cellZ = maintainPrecision((double)z * xzScale * persistence);
			double sy = yScale * persistence;
			minNoise += this.minLimitPerlinNoise.getOctave(octave).sample(cellX, cellY, cellZ, sy, (double)y * sy) / persistence;
			maxNoise += this.maxLimitPerlinNoise.getOctave(octave).sample(cellX, cellY, cellZ, sy, (double)y * sy) / persistence;
			if(octave < 8) {
				mainNoise += this.mainPerlinNoise.getOctave(octave).sample(
					maintainPrecision((double)x * xzStep * persistence),
					maintainPrecision((double)y * yStep * persistence),
					maintainPrecision((double)z * xzStep * persistence),
					yStep * persistence,
					(double)y * yStep * persistence) / persistence;
			}
			persistence /= 2.0D;
		}

		return clampedLerp(minNoise / NoiseSettings.NOISE_SCALE, maxNoise / NoiseSettings.NOISE_SCALE, (mainNoise / 10.0D + 1.0D) / 2.0D);
	}

	/**
	 * This function is super particular as there is a bug where localY was not taken into account in the yloop
	 *
	 * @return a double array of Ysize
	 */
	private static double[] sampleNoiseOnYLevel(OctavePerlinNoiseSampler sampler, int ySize, int x, int z, double noiseScaleX, double noiseScaleY, double noiseScaleZ) {
		// replace octave count with the one in noiseutils
		double[] yArray = new double[ySize];
		double persistence = 1.0D;
		for(int octave = 0; octave < sampler.getCount(); ++octave) {

			double xScale = noiseScaleX * persistence;
			double yScale = noiseScaleY * persistence;
			double zScale = noiseScaleZ * persistence;

			int cx = x >> 4;
			int cz = z >> 4;

			int fx = x & 15;
			int fz = z & 15;

			double X = (double)(cx << 4) * xScale;
			double Z = (double)(cz << 4) * zScale;
			long intX = MathHelper.lfloor(X);
			long intZ = MathHelper.lfloor(Z);
			X = X - (double)intX;
			Z = Z - (double)intZ;
			intX = intX % 16777216L;
			intZ = intZ % 16777216L;
			X = X + (double)intX;
			Z = Z + (double)intZ;
			PerlinNoiseSampler noiseSampler = sampler.getOctave(octave);
			int oldOffset = -1;
			double x1x2 = 0.0D;
			double x3x4 = 0.0D;
			double x5x6 = 0.0D;
			double x7x8 = 0.0D;
			for(int currentY = 0; currentY < ySize; currentY++) {

				double fy = currentY * yScale;
				// the & is not necessary until you mess up with heights
				int currentOffset = (int)(noiseSampler.originY + fy) & 255;

				Triplet<int[], double[], double[]> args = noiseSampler.getArgs(
					X + fx * xScale,
					fy,
					Z + fz * zScale,
					0.0D, 0.0D
				);
				// this part is crucial because without it then you have wrong values because the
				// intern at Mojang forgot that sameOffset doesn't means same fy (or localY) in particular with double
				// this can make a huge difference, this pseudo-optimization makes it hard to do single y values thus we
				// have to precompute the full y range since precomputed values of previous y value could trickle down
				// we could make it pseudo recursive by calling it with y-1 but that seems a bit overboard.
				if(currentY == 0 || currentOffset != oldOffset) {
					oldOffset = currentOffset;
					double[] perms = noiseSampler.samplePermutations(args.getFirst(), args.getSecond());
					double fadeLocalX = args.getThird()[0];
					x1x2 = MathHelper.lerp(fadeLocalX, perms[0], perms[1]);
					x3x4 = MathHelper.lerp(fadeLocalX, perms[2], perms[3]);
					x5x6 = MathHelper.lerp(fadeLocalX, perms[4], perms[5]);
					x7x8 = MathHelper.lerp(fadeLocalX, perms[6], perms[7]);
				}
				double fadeLocalY = args.getThird()[1];
				double fadeLocalZ = args.getThird()[2];
				double noise = MathHelper.lerp2(fadeLocalY, fadeLocalZ, x1x2, x3x4, x5x6, x7x8);
				yArray[currentY] += noise / persistence;
			}
			persistence /= 2.0D;
		}
		return yArray;
	}

	protected void sampleNoiseColumnOld(double[] buffer, int x, int z, double depth, double scale) {

		double xzScale = NoiseSettings.COORDINATE_SCALE * noiseSettings.samplingSettings.xzScale;
		double yScale = NoiseSettings.HEIGHT_SCALE * noiseSettings.samplingSettings.yScale;
		double xzStep = xzScale / noiseSettings.samplingSettings.xzFactor;
		double yStep = yScale / noiseSettings.samplingSettings.yFactor;
		// warning it is very important to do the float casting, this is a bug with old versions with precision
		if(getDimension() == Dimension.OVERWORLD) {
			// 684.412F/80.0F=8.55515
			// 684.412D*0.9999999814507745D/80.0D = 8.555149841308594
			// (float)8.555149841308594=8.55515
			xzStep = (float)xzStep;
			yStep = (float)yStep;
		}
		double[] minNoiseYLevels = sampleNoiseOnYLevel(this.minLimitPerlinNoise, this.noiseSizeY(), x, z, xzScale, yScale, xzScale);
		double[] maxNoiseYLevels = sampleNoiseOnYLevel(this.maxLimitPerlinNoise, this.noiseSizeY(), x, z, xzScale, yScale, xzScale);
		double[] mainNoiseYLevels = sampleNoiseOnYLevel(this.mainPerlinNoise, this.noiseSizeY(), x, z, xzStep, yStep, xzStep);

		int sizeY = (int)this.getMaxNoiseY();

		for(int y = 0; y < this.noiseSizeY(); ++y) {
			double fallOff = computeNoiseFalloff(depth, scale, y);
			double minNoise = minNoiseYLevels[y] / NoiseSettings.NOISE_SCALE;
			double maxNoise = maxNoiseYLevels[y] / NoiseSettings.NOISE_SCALE;
			double mainNoise = mainNoiseYLevels[y];
			mainNoise = (mainNoise / 10.0D + 1.0D) / 2.0D;
			double noise = clampedLerp(minNoise, maxNoise, mainNoise) - fallOff;
			if(y > sizeY) {
				double offset = (float)(y - sizeY) / (float)this.noiseSettings.topSlideSettings.size;
				if(getDimension() == Dimension.END) {
					offset = clamp(offset, 0.0D, 1.0D);
				}
				noise = noise * (1.0D - offset) + this.noiseSettings.topSlideSettings.target * offset;
			}
			if((double)y < getMinNoiseY()) {
				if(getDimension() == Dimension.NETHER) {
					double offset = (getMinNoiseY() - (double)y) / 4.0D;
					offset = clamp(offset, 0.0D, 1.0D);
					noise = noise * (1.0D - offset) - 10.0D * offset;
				} else if(getDimension() == Dimension.END) {
					double offset = (float)((int)getMinNoiseY() - y) / ((float)getMinNoiseY() - 1.0F);
					noise = noise * (1.0D - offset) - 30.0D * offset;
				}
			}

			buffer[y] = noise;
		}
	}

	protected void sampleNoiseColumn(double[] buffer, int x, int z) {
		double[] ds = this.getDepthAndScale(x, z);
		double depth = ds[0];
		double scale = ds[1];

		if(version.isOlderOrEqualTo(MCVersion.v1_13_2)) {
			this.sampleNoiseColumnOld(buffer, x, z, depth, scale);
			return;
		}

		double sizeY = this.getMaxNoiseY();
		double minY = this.getMinNoiseY();
		double randomOffset = this.biomeSource.getDimension() == Dimension.OVERWORLD ? this.sampleNoise(x, z) : 0.0D;
		for(int y = 0; y < this.noiseSizeY(); ++y) {
			// everything below is only for 1.14+
			double noise = this.sampleNoise(x, y, z);
			if(version.isNewerOrEqualTo(MCVersion.v1_16)) {
				double fallOff = 1.0D - (double)y * 2.0D / (double)this.noiseSizeY + randomOffset;
				fallOff = fallOff * densityFactor + densityOffset;
				fallOff = (fallOff + depth) * scale;
				noise = fallOff > 0.0 ? noise + fallOff * 4.0D : noise + fallOff;
				if(this.noiseSettings.topSlideSettings.size > 0.0D) {
					noise = clampedLerp(this.noiseSettings.topSlideSettings.target, noise, ((double)(this.noiseSizeY - y) - this.noiseSettings.topSlideSettings.offset) / this.noiseSettings.topSlideSettings.size);
				}
				if(this.noiseSettings.bottomSlideSettings.size > 0.0D) {
					noise = clampedLerp(this.noiseSettings.bottomSlideSettings.target, noise, ((double)y - this.noiseSettings.bottomSlideSettings.offset) / this.noiseSettings.bottomSlideSettings.size);
				}
			} else {
				noise -= this.computeNoiseFalloff(depth, scale, y);
				if((double)y > sizeY) {
					noise = clampedLerp(noise, this.noiseSettings.topSlideSettings.target, (y - sizeY - this.noiseSettings.topSlideSettings.offset) / (double)this.noiseSettings.topSlideSettings.size);
				} else if((double)y < minY) {
					noise = clampedLerp(noise, this.noiseSettings.bottomSlideSettings.target, (minY - (double)y) / (minY - 1.0D));
				}
			}
			buffer[y] = noise;
		}
	}

	protected double[] sampleNoiseColumn(int x, int z) {
		long key = ((((long)x) & 0xFFFFFFFFL) << 32) | (((long)z) & 0xFFFFFFFFL);
		if(noiseColumnCache.containsKey(key)) {
			return noiseColumnCache.get(key);
		} else {
			double[] ds = new double[this.noiseSizeY + 1];
			this.sampleNoiseColumn(ds, x, z);
			noiseColumnCache.put(key, ds);
			return ds;
		}
	}

	public Block[] getColumnAt(int x, int z) {
		return columnCache.computeIfAbsent(getKey(x, z), k -> {
			assert getWorldHeight() == (this.noiseSizeY * this.chunkHeight);
			Block[] buffer = new Block[this.getWorldHeight()];
			int y = this.generateColumn(buffer, x, z, null, null, null);
			assert y == 0;
			return buffer;
		});
	}

	@Override
	public Block[] getColumnAt(int x, int z, List<Pair<Supplier<Integer>, BlockBox>> jigsawBoxes, List<BPos> jigsawJunction) {
		if(jigsawBoxes == null || jigsawJunction == null) return null;
		return jigsawColumnCache.computeIfAbsent(getKey(x, z), k -> {
			assert getWorldHeight() == (this.noiseSizeY * this.chunkHeight);
			Block[] buffer = new Block[this.getWorldHeight()];
			int y = this.generateColumn(buffer, x, z, null, jigsawBoxes, jigsawJunction);
			assert y == 0;
			return buffer;
		});
	}

	public Block[] getBiomeColumnAt(int x, int z) {
		long key=getKey(x,z);
		if (biomeColumnCache.containsKey(key)){
			return biomeColumnCache.get(key);
		}else{
			Block[] blocks=this.generateBiomeColumnBefore(x, z, this::getColumnAt, this.biomeColumnCache, this.chunkSeeds);
			biomeColumnCache.put(key,blocks);
			return blocks;
		}
	}

	@Override
	public Block[] getBiomeColumnAt(int x, int z, List<Pair<Supplier<Integer>, BlockBox>> jigsawBoxes, List<BPos> jigsawJunction) {
		if(jigsawBoxes == null || jigsawJunction == null) return null;
		long key=getKey(x,z);
		if (biomeJigsawColumnCache.containsKey(key)){
			return biomeJigsawColumnCache.get(key);
		}else{
			Block[] blocks=this.generateBiomeColumnBefore(x, z, (posX, posZ) -> this.getColumnAt(posX, posZ, jigsawBoxes, jigsawJunction), this.biomeJigsawColumnCache, this.jigsawChunkSeeds);
			biomeJigsawColumnCache.put(key,blocks);
			return blocks;
		}
	}

	@Override
	public Block[] getBedrockColumnAt(int x, int z) {
		applyBedrock(x, z, this::getBiomeColumnAt, this.biomeColumnCache, this.chunkSeeds);
		return this.biomeColumnCache.get(this.getKey(x, z));
	}

	@Override
	public Block[] getBedrockColumnAt(int x, int z, List<Pair<Supplier<Integer>, BlockBox>> jigsawBoxes, List<BPos> jigsawJunction) {
		applyBedrock(x, z, this::getBiomeColumnAt, this.jigsawColumnCache, this.jigsawChunkSeeds);
		return this.jigsawColumnCache.get(this.getKey(x, z));
	}

	public void applyBedrock(int x, int z, BiFunction<Integer, Integer, Block[]> columnProvider, Map<Long, Block[]> cacheProvider, Map<Long, Long> seedProvider) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;
		int maxChunkX = (chunkX << 4) + 15;
		int maxChunkZ = (chunkZ << 4) + 15;
		// generate the full chunk
		this.getBiomeColumnAt(maxChunkX, maxChunkZ);
		// get the last seed
		Long seed = seedProvider.get(getKey(maxChunkX, maxChunkZ));
		// should not fail
		if(seed == null) return;
		ChunkRand rand = new ChunkRand(seed, false);
		int maxFloorBedrock = this.getBedrockFloorPosition();
		int maxRoofBedrock = this.worldHeight - 1 - this.getBedrockRoofPosition();
		boolean roofOk = maxRoofBedrock + 4 >= 0 && maxRoofBedrock < this.worldHeight;
		boolean floorOk = maxFloorBedrock + 4 >= 0 && maxFloorBedrock < this.worldHeight;
		// we will generate the full chunk because caching the seeds here makes no sense,
		// the generation of the full chunk takes already 97% of the work
		if(roofOk || floorOk) {
			for(int X = 0; X < 16; X++) {
				for(int Z = 0; Z < 16; Z++) {
					// should be a cache hit only
					Block[] buffer = columnProvider.apply(chunkX + X, chunkZ + Z);
					if(roofOk) {
						for(int y = 0; y < 5; y++) {
							if(y <= rand.nextInt(5)) {
								buffer[maxRoofBedrock - y] = Blocks.BEDROCK;
							}
						}
					}
					if(floorOk) {
						for(int y = 4; y >= 0; y--) {
							if(y <= rand.nextInt(5)) {
								buffer[maxFloorBedrock + y] = Blocks.BEDROCK;
							}
						}
					}
					cacheProvider.put(getKey(chunkX + X, chunkZ + Z), buffer);
				}
			}
		}
	}

	private long getKey(int x, int z) {
		return ((((long)x) & 0xFFFFFFFFL) << 32) | (((long)z) & 0xFFFFFFFFL);
	}

	public Optional<Block> getBlockAt(int x, int y, int z) {
		// long key = ((((long) y) & 0x3fff) << 50)  |((((long) x) & 0x1FFFFFF) << 25) | (((long) z) & 0x1FFFFFF);
		if(y < this.getMinWorldHeight() || y > this.getMaxWorldHeight() - 1) {
			return Optional.empty();
		}
		return Optional.of(getColumnAt(x, z)[y]);
	}

	/**
	 * Replace the block in the column buffer, warning this function only work with a specifically seeded rand
	 *
	 * @param buffer the block buffer from the hulk
	 * @param x      the x world position
	 * @param z      the z world position
	 * @param rand   the specific rand for this column
	 */
	private void replaceBiomeBlocks(Block[] buffer, int x, int z, ChunkRand rand) {
		int y = (int) com.seedfinding.mcterrain.utils.MathHelper.clamp(this.getHeightOnGround(x, z), this.getMinWorldHeight(), this.getMaxWorldHeight() - 1);
		double noise = this.surfaceDepthNoise.sample((double)x * 0.0625D, (double)z * 0.0625D, 0.0625D, (double)(x & 15) * 0.0625D) * 15.0D;
		Biome biome = this.biomeSource.getBiome(x, y, z);
		biome.getSurfaceBuilder().applyToColumn(this.getBiomeSource(), rand, buffer, biome, x, z, y, 0, noise,
			this.getSeaLevel(), this.getDefaultBlock(), this.getDefaultFluid());
	}

	/**
	 * This function generate a full chunk until the position based on a hulk column provider
	 *
	 * @param x              the world x position
	 * @param z              the world z position
	 * @param columnProvider must be non null and provide a hulk column (based on default fluid and block)
	 * @param cacheProvider  a cache provider
	 * @param seedProvider   a seed provider
	 * @return the block column
	 */
	public Block[] generateBiomeColumnBefore(int x, int z, BiFunction<Integer, Integer, Block[]> columnProvider, Map<Long, Block[]> cacheProvider, Map<Long, Long> seedProvider) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;
		ChunkRand rand = new ChunkRand();
		rand.setTerrainSeed(chunkX, chunkZ, this.getVersion());
		int minChunkX = chunkX << 4;
		int minChunkZ = chunkZ << 4;
		for(int X = 0; X < 16; X++) {
			for(int Z = 0; Z < 16; Z++) {
				int posX = minChunkX + X;
				int posZ = minChunkZ + Z;
				long key = this.getKey(posX, posZ);
				Long seed = seedProvider.get(key);
				if(seed == null) {
					assert !cacheProvider.containsKey(key);
					Block[] buffer = columnProvider.apply(x, z);
					this.replaceBiomeBlocks(buffer, x, z, rand);
					cacheProvider.put(key, buffer);
					seedProvider.put(key, rand.getSeed());
				} else {
					rand.setSeed(seed, false);
				}
				// if we reached the desired position
				if(posX == x && posZ == z) {
					return cacheProvider.get(key);
				}
			}
		}
		return cacheProvider.get(getKey(x, z));
	}

	public double applyJigsawToNoise(double noise, BPos bPos, List<Pair<Supplier<Integer>, BlockBox>> jigsawBoxes, List<BPos> jigsawJunction) {
		noise = com.seedfinding.mcterrain.utils.MathHelper.clamp(noise / 200.0D, -1.0D, 1.0D);
		noise = noise / 2.0D - noise * noise * noise / 24.0D;
		for(Pair<Supplier<Integer>, BlockBox> jigsawBox : jigsawBoxes) {
			BlockBox box = jigsawBox.getSecond();
			int localX = Math.max(0, Math.max(box.minX - bPos.getX(), bPos.getX() - box.maxX));
			int localY = bPos.getY() - (box.minY + (jigsawBox.getFirst() != null ? jigsawBox.getFirst().get() : 0));
			int localZ = Math.max(0, Math.max(box.minZ - bPos.getZ(), bPos.getZ() - box.maxZ));
			noise += getContribution(localX, localY, localZ) * 0.8D;
		}
		for(BPos junction : jigsawJunction) {
			int localX = bPos.getX() - junction.getX();
			int localY = bPos.getY() - junction.getY();
			int localZ = bPos.getZ() - junction.getZ();
			noise += getContribution(localX, localY, localZ) * 0.4D;
		}
		return noise;
	}

	public int generateColumn(Block[] buffer, int x, int z, Predicate<Block> blockPredicate, List<Pair<Supplier<Integer>, BlockBox>> jigsawBoxes, List<BPos> jigsawJunction) {
		// those are the coordinates of the region in the grid chosen
		int cellX = Math.floorDiv(x, this.chunkWidth);
		int cellZ = Math.floorDiv(z, this.chunkWidth);
		// those are the coordinates in the chosen region
		int posX = Math.floorMod(x, this.chunkWidth);
		int posZ = Math.floorMod(z, this.chunkWidth);
		double percentX = (double)posX / (double)this.chunkWidth;
		double percentZ = (double)posZ / (double)this.chunkWidth;
		double[][] ds = new double[][] {
			this.sampleNoiseColumn(cellX, cellZ),
			this.sampleNoiseColumn(cellX, cellZ + 1),
			this.sampleNoiseColumn(cellX + 1, cellZ),
			this.sampleNoiseColumn(cellX + 1, cellZ + 1)
		};

		for(int cellY = this.noiseSizeY - 1; cellY >= 0; --cellY) {
			double xyz = ds[0][cellY];
			double xyz1 = ds[1][cellY];
			double x1yz = ds[2][cellY];
			double x1yz1 = ds[3][cellY];
			double xy1z = ds[0][cellY + 1];
			double xy1z1 = ds[1][cellY + 1];
			double x1y1z = ds[2][cellY + 1];
			double x1y1z1 = ds[3][cellY + 1];

			for(int posY = this.chunkHeight - 1; posY >= 0; --posY) {
				double percentY = (double)posY / (double)this.chunkHeight;
				// this is not a bug, mojang does not respect order
				double noise = MathHelper.lerp3(percentY, percentX, percentZ, xyz, xy1z, x1yz, x1y1z, xyz1, xy1z1, x1yz1, x1y1z1);
				int y = cellY * this.chunkHeight + posY;
				if(jigsawBoxes != null && jigsawJunction != null) {
					noise = applyJigsawToNoise(noise, new BPos(x, y, z), jigsawBoxes, jigsawJunction);
				}
				Block block = this.getBlockFromNoise(noise, y);
				// we assume you actually have correctly filled the buffer
				if(buffer != null) {
					buffer[y] = block;
				}
				if(blockPredicate != null && blockPredicate.test(block)) {
					return y + 1;
				}
			}
		}
		return 0;
	}

	@Override
	public int getHeightOnGround(int x, int z) {
		return this.generateColumn(null, x, z, (block) -> block == this.getDefaultBlock(), null, null);
	}

	@Override
	public int getFirstHeightInColumn(int x, int z, Predicate<Block> predicate) {
		return this.generateColumn(null, x, z, predicate, null, null);
	}

	protected double[] getDepthAndScale(int x, int z) {
		double[] depthAndScale = new double[2];
		int sampleRange = 2;
		float weightedScale = 0.0F;
		float weightedDepth = 0.0F;
		float totalWeight = 0.0F;
		Biome biome = this.biomeSource.getBiomeForNoiseGen(x, this.getSeaLevel(), z);
		float depthAtCenter = biome.getDepth();
		for(int rx = -sampleRange; rx <= sampleRange; ++rx) {
			for(int rz = -sampleRange; rz <= sampleRange; ++rz) {
				biome = this.biomeSource.getBiomeForNoiseGen(x + rx, this.getSeaLevel(), z + rz);
				float depth = biome.getDepth();
				float scale = biome.getScale();
				if(this.amplified && depth > 0.0F) {
					depth = 1.0F + depth * 2.0F;
					scale = 1.0F + scale * 4.0F;
				}

				float weight = BIOME_WEIGHT_TABLE[rx + 2 + (rz + 2) * 5] / (depth + 2.0F);
				if(biome.getDepth() > depthAtCenter) {
					weight /= 2.0F;
				}

				weightedScale += scale * weight;
				weightedDepth += depth * weight;
				totalWeight += weight;
			}
		}
		weightedScale /= totalWeight;
		weightedDepth /= totalWeight;
		weightedScale = weightedScale * 0.9F + 0.1F;
		weightedDepth = (weightedDepth * 4.0F - 1.0F) / 8.0F;
		// weightedDepth = (weightedDepth * 0.5F - 0.125F);
		if(this.biomeSource.getVersion().isNewerOrEqualTo(MCVersion.v1_16)) {
			depthAndScale[0] = weightedDepth * 17.0D / 64.0D;
			depthAndScale[1] = 96.0D / weightedScale;
		} else {
			double noise = this.sampleNoise(x, z);
			depthAndScale[0] = (double)weightedDepth + noise;
			if(version.isOlderOrEqualTo(MCVersion.v1_13_2)) {
				depthAndScale[0] = weightedDepth + noise * 0.2D;
			}
			depthAndScale[1] = weightedScale;
		}
		return depthAndScale;
	}

	private double sampleNoise(int x, int z) {
		double noise = this.depthNoise.sample(x * 200, 10.0D, z * 200, 1.0D, 0.0D, true);
		if(version.isOlderThan(MCVersion.v1_16)) {
			if(version.isNewerOrEqualTo(MCVersion.v1_15)) {
				noise *= 65535.0D;
			}
			noise /= 8000.0D;
		}
		noise = noise < 0.0D ? -noise * 0.3D : noise;
		if(version.isNewerOrEqualTo(MCVersion.v1_16)) {
			noise = noise * 3.0D * 65535.0D / 8000.0D - 2.0D;
//			noise = noise * 24.575625D - 2.0D;
			if(noise < 0.0D) {
				// return noise * 0.009486607142857142D;
				return 17.0D * noise / 28.0D / 64.0D;
			}
			//return Math.min(noise, 1.0D) * 0.006640625D;
			return Math.min(noise, 1.0D) * 17.0D / 40.0D / 64.0D;
		}

		noise = noise * 3.0D - 2.0D;
		if(version.isOlderOrEqualTo(MCVersion.v1_13_2)) {
			if(noise < 0.0D) {
				noise = noise / 2.0D;
				if(noise < -1.0D) {
					noise = -1.0D;
				}

				noise = noise / 1.4D;
				noise = noise / 2.0D;
			} else {
				if(noise > 1.0D) {
					noise = 1.0D;
				}
				noise = noise / 8.0D;
			}
			return noise;
		}

		if(noise < 0.0D) {
			return noise / 28.0D;
		}
		return Math.min(noise, 1.0D) / 40.0D;
	}

	protected double computeNoiseFalloff(double depth, double scale, int y) {
		double fallOff = ((double)y - (8.5D + depth * 8.5D / 8.0D * 4.0D)) * 12.0D * 128.0D / 256.0D / scale;
		if(fallOff < 0.0D) {
			fallOff *= 4.0D;
		}
		return fallOff;
	}

	public Block getBlockFromNoise(double noise, int y) {
		Block block;
		if(noise > 0.0D) {
			block = this.getDefaultBlock();
		} else if(y < this.getSeaLevel()) {
			block = this.getDefaultFluid();
		} else {
			block = Blocks.AIR;
		}

		return block;
	}

	private static double getContribution(int x, int y, int z) {
		int offX = x + 12;
		int offY = y + 12;
		int offZ = z + 12;
		if(offX >= 0 && offX < 24) {
			if(offY >= 0 && offY < 24) {
				return offZ >= 0 && offZ < 24 ? (double)BEARD_KERNEL[offZ * 24 * 24 + offX * 24 + offY] : 0.0D;
			} else {
				return 0.0D;
			}
		} else {
			return 0.0D;
		}
	}

	private static double computeContribution(int x, int y, int z) {
		double d0 = x * x + z * z;
		double d1 = (double)y + 0.5D;
		double d2 = d1 * d1;
		double d3 = Math.pow(Math.E, -(d2 / 16.0D + d0 / 16.0D));
		double d4 = -d1 * com.seedfinding.mcterrain.utils.MathHelper.fastInvSqrt(d2 / 2.0D + d0 / 2.0D) / 2.0D;
		return d4 * d3;
	}

}
