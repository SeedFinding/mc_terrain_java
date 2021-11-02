package com.seedfinding.mcterrain.terrain;


import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mcbiome.source.EndBiomeSource;
import com.seedfinding.mccore.block.Block;
import com.seedfinding.mccore.block.Blocks;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcterrain.utils.NoiseSettings;

public class EndTerrainGenerator extends SurfaceGenerator {

	public EndTerrainGenerator(BiomeSource biomeSource) {
		super(biomeSource, 128, 2, 1,
			biomeSource.getVersion().isNewerOrEqualTo(MCVersion.v1_16) ?
				NoiseSettings.create(2.0, 1.0, 80.0, 160.0)
					.addTopSlide(-3000, 64, -46)
					.addBottomSlide(-30, 7, 1) :
				NoiseSettings.create(2.0, 1.0, 80.0,160.0 )
					.addTopSlide(-3000, 64, 0)
					.addBottomSlide(-30, 1, 0)
			,
			0.0, 0.0, true
		);
	}

	@Override
	public Block getDefaultBlock() {
		return Blocks.END_STONE;
	}

	@Override
	public Block getDefaultFluid() {
		return Blocks.AIR;
	}

	@Override
	public int getBedrockRoofPosition() {
		return -10;
	}

	@Override
	public int getBedrockFloorPosition() {
		return -10;
	}

	@Override
	public Dimension getDimension() {
		return Dimension.END;
	}

	@Override
	protected double[] getDepthAndScale(int x, int z) {
		double height = ((EndBiomeSource)this.biomeSource).height.getNoiseValueAt(x, z);
		if(this.getVersion().isNewerOrEqualTo(MCVersion.v1_16)) {
			double[] depthAndScale = new double[2];
			depthAndScale[0] = height - 8.0f;
			depthAndScale[1] = depthAndScale[0] > 0.0 ? 0.25 : 1.0;
			return depthAndScale;
		}
		return new double[] {height, 0.0D};
	}

	@Override
	protected double computeNoiseFalloff(double depth, double scale, int y) {
		return this.getVersion().isNewerOrEqualTo(MCVersion.v1_16) ? super.computeNoiseFalloff(depth, scale, y) : this.getMinNoiseY() - depth;
	}


	@Override
	public double getMaxNoiseY() {
		return this.getVersion().isNewerOrEqualTo(MCVersion.v1_16) ? super.getMaxNoiseY() : (double)((int)super.getMaxNoiseY() / 2);
	}

	@Override
	public double getMinNoiseY() {
		return this.getVersion().isNewerOrEqualTo(MCVersion.v1_16) ? super.getMinNoiseY() : 8.0D;
	}

	@Override
	public int getSeaLevel() {
		return 0;
	}
}
