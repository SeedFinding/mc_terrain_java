package com.seedfinding.mcterrain.terrain;

import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.block.Block;
import com.seedfinding.mccore.block.Blocks;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcterrain.utils.NoiseSettings;

public class NetherTerrainGenerator extends SurfaceGenerator {

	private final double[] fallOffTable = this.computeFallOffTable();

	public NetherTerrainGenerator(BiomeSource biomeSource) {
		super(biomeSource, 128, 1, 2,
			(biomeSource.getVersion().isNewerOrEqualTo(MCVersion.v1_16)) ?
				NoiseSettings.create(1.0, 3.0, 80.0, 60.0)
					.addTopSlide(120, 3, 0)
					.addBottomSlide(320, 4, -1) :
				NoiseSettings.create(1.0, 3.0, 80.0, 60.0)
				.addTopSlide(-10, 3, 0)
				.addBottomSlide(-30, 1, 0)
			,
			0.0, 0.019921875, false
		);
	}



	@Override
	public int getSeaLevel() {
		return 32;
	}

	@Override
	public Block getDefaultBlock() {
		return Blocks.NETHERRACK;
	}

	@Override
	public Dimension getDimension() {
		return Dimension.NETHER;
	}

	@Override
	public Block getDefaultFluid() {
		return Blocks.LAVA;
	}

	@Override
	public int getBedrockRoofPosition() {
		return 0;
	}

	@Override
	public int getBedrockFloorPosition() {
		return 0;
	}

	@Override
	protected double computeNoiseFalloff(double depth, double scale, int y) {
		return this.getVersion().isNewerOrEqualTo(MCVersion.v1_16) ? super.computeNoiseFalloff(depth, scale, y) : fallOffTable[y];
	}

	private double[] computeFallOffTable() {
		double[] column = new double[this.noiseSizeY()];

		for(int y = 0; y < this.noiseSizeY(); ++y) {
			column[y] = Math.cos((double)y * Math.PI * 6.0D / (double)this.noiseSizeY()) * 2.0D;
			double fallOff = y;
			if(y > this.noiseSizeY() / 2) {
				fallOff = this.noiseSizeY() - 1 - y;
			}

			if(fallOff < 4.0D) {
				fallOff = 4.0D - fallOff;
				column[y] -= fallOff * fallOff * fallOff * 10.0D;
			}
		}

		return column;
	}

	@Override
	protected double[] getDepthAndScale(int x, int z) {
		if(this.version.isNewerOrEqualTo(MCVersion.v1_16)) {
			return super.getDepthAndScale(x, z);
		}
		return new double[] {0.0, 0.0};
	}
}
