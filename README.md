[![Build Status](https://jenkins.seedfinding.com/buildStatus/icon?job=mc_java%2Fmc_terrain_java)](https://jenkins.seedfinding.com/job/mc_java/job/mc_terrain_java/)

# Minecraft terrain

A dead simple library to simulate Minecraft terrain in pure Java, supports only 1.14+ for now

## Usage

The whole exposed API holds in TerrainGenerator, calling specific things on SurfaceGenerator or lower model is not recommended

```java
package kaptainwutax.terrainutils;

import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mccore.block.Block;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;

public class Main {
	public static void main(String[] args) {
		long worldSeed=1L;
		MCVersion version=MCVersion.v1_16_5;
		Dimension dimension=Dimension.OVERWORLD;
		BiomeSource biomeSource=BiomeSource.of(dimension, version,worldSeed);
		TerrainGenerator terrainGenerator= TerrainGenerator.of(biomeSource);
		assert terrainGenerator != null;
		Block block=terrainGenerator.getBlockAt(0,0,0);
		Block[] column=terrainGenerator.getColumnAt(0,0);
		int surfaceGenHeight=terrainGenerator.getFirstHeightInColumn(0,0, TerrainGenerator.WORLD_SURFACE_WG);
		int oceanGenHeight=terrainGenerator.getFirstHeightInColumn(0,0, TerrainGenerator.OCEAN_FLOOR_WG);
		int surfaceBlockIn=terrainGenerator.getHeightInGround(0,0);
		int surfaceBlockAbove=terrainGenerator.getHeightOnGround(0,0);
	}
}
```

## Legal mentions
Licensed under MIT

Maintained by Neil and KaptainWutax.

NOT OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG.
