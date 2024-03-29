package com.seedfinding.mcterrain.v1_16.v1_16_0;

import com.seedfinding.mcterrain.TestFramework;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayName("Minecraft v1.16 Overworld")
@Tag("v1.16")
@TestFramework.Overworld
public class Overworld {
	public static final MCVersion VERSION = MCVersion.v1_16;
	public static final Dimension DIMENSION = Dimension.OVERWORLD;
	private static final int[] size16 = {
		63 ,75 ,61 ,69 ,62 ,67 ,55 ,56 ,38 ,69 ,63 ,70 ,68 ,64 ,63 ,68 ,
		70 ,64 ,62 ,70 ,40 ,58 ,65 ,51 ,49 ,64 ,37 ,89 ,40 ,70 ,65 ,63 ,
		75 ,51 ,44 ,74 ,58 ,72 ,47 ,85 ,32 ,71 ,68 ,69 ,63 ,62 ,75 ,64 ,
		38 ,76 ,82 ,45 ,75 ,70 ,48 ,71 ,64 ,59 ,54 ,71 ,33 ,45 ,80 ,61 ,
		63 ,68 ,56 ,71 ,67 ,46 ,64 ,64 ,53 ,63 ,66 ,72 ,64 ,85 ,52 ,41 ,
		42 ,85 ,68 ,68 ,38 ,51 ,70 ,64 ,69 ,33 ,70 ,50 ,76 ,38 ,66 ,46 ,
		63 ,68 ,65 ,67 ,69 ,61 ,68 ,79 ,68 ,70 ,48 ,60 ,40 ,34 ,65 ,77 ,
		63 ,72 ,31 ,41 ,65 ,72 ,68 ,64 ,72 ,79 ,71 ,83 ,62 ,39 ,72 ,42 ,
		72 ,76 ,67 ,41 ,55 ,37 ,41 ,47 ,72 ,66 ,69 ,69 ,64 ,45 ,69 ,78 ,
		67 ,72 ,46 ,69 ,71 ,62 ,35 ,64 ,32 ,45 ,38 ,76 ,62 ,64 ,34 ,83 ,
		62 ,70 ,47 ,70 ,64 ,81 ,70 ,64 ,80 ,78 ,88 ,48 ,48 ,63 ,40 ,85 ,
		68 ,49 ,48 ,63 ,36 ,60 ,58 ,67 ,52 ,66 ,64 ,53 ,77 ,46 ,40 ,71 ,
		90 ,37 ,55 ,64 ,40 ,65 ,47 ,32 ,72 ,32 ,47 ,48 ,45 ,63 ,54 ,64 ,
		66 ,32 ,48 ,74 ,67 ,48 ,67 ,75 ,35 ,64 ,72 ,64 ,79 ,63 ,74 ,43 ,
		67 ,68 ,83 ,33 ,34 ,32 ,37 ,38 ,65 ,72 ,72 ,61 ,71 ,32 ,74 ,68 ,
		50 ,36 ,87 ,65 ,74 ,33 ,52 ,72 ,75 ,33 ,48 ,48 ,45 ,80 ,62 ,67 ,
		};

	private static final int[] size32 = {
		63 ,75 ,61 ,69 ,62 ,67 ,55 ,56 ,38 ,69 ,63 ,70 ,68 ,64 ,63 ,68 ,70 ,64 ,62 ,70 ,40 ,58 ,65 ,51 ,49 ,64 ,37 ,89 ,40 ,70 ,65 ,63 ,
		75 ,51 ,44 ,74 ,58 ,72 ,47 ,85 ,32 ,71 ,68 ,69 ,63 ,62 ,75 ,64 ,38 ,76 ,82 ,45 ,75 ,70 ,48 ,71 ,64 ,59 ,54 ,71 ,33 ,45 ,80 ,61 ,
		63 ,68 ,56 ,71 ,67 ,46 ,64 ,64 ,53 ,63 ,66 ,72 ,64 ,85 ,52 ,41 ,42 ,85 ,68 ,68 ,38 ,51 ,70 ,64 ,69 ,33 ,70 ,50 ,76 ,38 ,66 ,46 ,
		63 ,68 ,65 ,67 ,69 ,61 ,68 ,79 ,68 ,70 ,48 ,60 ,40 ,34 ,65 ,77 ,63 ,72 ,31 ,41 ,65 ,72 ,68 ,64 ,72 ,79 ,71 ,83 ,62 ,39 ,72 ,42 ,
		72 ,76 ,67 ,41 ,55 ,37 ,41 ,47 ,72 ,66 ,69 ,69 ,64 ,45 ,69 ,78 ,67 ,72 ,46 ,69 ,71 ,62 ,35 ,64 ,32 ,45 ,38 ,76 ,62 ,64 ,34 ,83 ,
		62 ,70 ,47 ,70 ,64 ,81 ,70 ,64 ,80 ,78 ,88 ,48 ,48 ,63 ,40 ,85 ,68 ,49 ,48 ,63 ,36 ,60 ,58 ,67 ,52 ,66 ,64 ,53 ,77 ,46 ,40 ,71 ,
		90 ,37 ,55 ,64 ,40 ,65 ,47 ,32 ,72 ,32 ,47 ,48 ,45 ,63 ,54 ,64 ,66 ,32 ,48 ,74 ,67 ,48 ,67 ,75 ,35 ,64 ,72 ,64 ,79 ,63 ,74 ,43 ,
		67 ,68 ,83 ,33 ,34 ,32 ,37 ,38 ,65 ,72 ,72 ,61 ,71 ,32 ,74 ,68 ,50 ,36 ,87 ,65 ,74 ,33 ,52 ,72 ,75 ,33 ,48 ,48 ,45 ,80 ,62 ,67 ,
		71 ,59 ,52 ,36 ,83 ,64 ,58 ,37 ,75 ,65 ,34 ,37 ,34 ,78 ,72 ,68 ,59 ,66 ,80 ,61 ,64 ,72 ,65 ,67 ,64 ,65 ,69 ,51 ,62 ,32 ,34 ,69 ,
		65 ,52 ,70 ,38 ,46 ,47 ,82 ,35 ,33 ,68 ,63 ,70 ,41 ,70 ,69 ,37 ,98 ,71 ,38 ,109 ,64 ,62 ,32 ,38 ,67 ,64 ,64 ,50 ,79 ,69 ,37 ,77 ,
		65 ,78 ,67 ,37 ,70 ,72 ,68 ,72 ,63 ,45 ,47 ,63 ,48 ,60 ,55 ,60 ,63 ,31 ,64 ,64 ,71 ,74 ,57 ,70 ,46 ,40 ,62 ,64 ,30 ,62 ,66 ,70 ,
		66 ,75 ,67 ,76 ,63 ,64 ,64 ,87 ,66 ,64 ,64 ,70 ,61 ,69 ,72 ,38 ,46 ,52 ,67 ,68 ,69 ,40 ,48 ,64 ,45 ,38 ,68 ,80 ,48 ,36 ,67 ,62 ,
		71 ,69 ,79 ,64 ,72 ,30 ,71 ,65 ,58 ,48 ,84 ,68 ,61 ,66 ,45 ,68 ,46 ,92 ,79 ,36 ,73 ,53 ,53 ,39 ,64 ,70 ,63 ,80 ,75 ,70 ,59 ,72 ,
		70 ,64 ,51 ,45 ,58 ,65 ,48 ,73 ,41 ,63 ,61 ,68 ,58 ,61 ,76 ,77 ,65 ,75 ,37 ,54 ,45 ,47 ,69 ,68 ,68 ,66 ,57 ,64 ,53 ,47 ,70 ,46 ,
		62 ,62 ,64 ,45 ,71 ,34 ,71 ,80 ,71 ,66 ,66 ,64 ,64 ,39 ,62 ,39 ,66 ,47 ,37 ,55 ,48 ,67 ,47 ,77 ,37 ,72 ,64 ,48 ,57 ,71 ,42 ,32 ,
		62 ,69 ,73 ,63 ,42 ,80 ,48 ,71 ,60 ,75 ,40 ,32 ,54 ,63 ,42 ,76 ,62 ,32 ,67 ,33 ,35 ,73 ,81 ,68 ,64 ,36 ,45 ,77 ,67 ,62 ,37 ,63 ,
		48 ,48 ,32 ,77 ,64 ,64 ,34 ,88 ,30 ,67 ,72 ,49 ,33 ,70 ,45 ,64 ,37 ,60 ,63 ,69 ,89 ,79 ,63 ,49 ,49 ,90 ,47 ,85 ,68 ,48 ,98 ,65 ,
		62 ,81 ,86 ,50 ,64 ,71 ,60 ,47 ,64 ,72 ,32 ,61 ,34 ,65 ,63 ,63 ,48 ,64 ,47 ,68 ,77 ,66 ,59 ,51 ,63 ,47 ,63 ,66 ,70 ,70 ,67 ,62 ,
		57 ,41 ,68 ,52 ,39 ,68 ,33 ,58 ,63 ,63 ,77 ,55 ,51 ,64 ,67 ,47 ,67 ,41 ,30 ,71 ,72 ,64 ,53 ,74 ,71 ,38 ,63 ,55 ,63 ,64 ,66 ,75 ,
		80 ,64 ,42 ,65 ,32 ,64 ,61 ,32 ,69 ,45 ,80 ,60 ,72 ,65 ,30 ,45 ,50 ,60 ,39 ,65 ,72 ,60 ,63 ,62 ,72 ,31 ,73 ,66 ,71 ,35 ,67 ,72 ,
		62 ,75 ,69 ,63 ,64 ,69 ,37 ,71 ,65 ,64 ,53 ,38 ,64 ,38 ,59 ,50 ,75 ,63 ,60 ,62 ,48 ,56 ,44 ,62 ,85 ,70 ,70 ,64 ,62 ,72 ,49 ,95 ,
		63 ,33 ,72 ,36 ,67 ,64 ,78 ,32 ,64 ,94 ,72 ,64 ,67 ,70 ,33 ,70 ,68 ,47 ,61 ,75 ,65 ,48 ,66 ,69 ,64 ,68 ,65 ,63 ,66 ,72 ,87 ,62 ,
		64 ,45 ,75 ,59 ,45 ,57 ,78 ,89 ,67 ,62 ,62 ,31 ,64 ,61 ,68 ,44 ,87 ,71 ,76 ,64 ,54 ,63 ,49 ,78 ,70 ,72 ,48 ,88 ,69 ,72 ,45 ,63 ,
		70 ,41 ,56 ,64 ,48 ,33 ,47 ,66 ,79 ,87 ,58 ,48 ,63 ,66 ,33 ,38 ,32 ,73 ,70 ,69 ,64 ,64 ,32 ,98 ,65 ,64 ,76 ,72 ,69 ,34 ,59 ,57 ,
		46 ,41 ,52 ,36 ,64 ,51 ,73 ,54 ,48 ,44 ,31 ,70 ,89 ,64 ,67 ,65 ,60 ,38 ,46 ,65 ,91 ,61 ,64 ,67 ,76 ,47 ,65 ,62 ,54 ,64 ,64 ,69 ,
		89 ,64 ,72 ,68 ,37 ,68 ,35 ,68 ,55 ,66 ,31 ,78 ,66 ,47 ,39 ,66 ,35 ,64 ,70 ,64 ,48 ,69 ,67 ,76 ,55 ,47 ,61 ,35 ,73 ,32 ,63 ,77 ,
		63 ,58 ,64 ,64 ,63 ,63 ,71 ,32 ,32 ,57 ,74 ,49 ,64 ,35 ,63 ,71 ,31 ,94 ,63 ,64 ,79 ,33 ,68 ,68 ,66 ,88 ,71 ,78 ,71 ,62 ,59 ,67 ,
		39 ,72 ,66 ,62 ,31 ,47 ,30 ,66 ,64 ,72 ,62 ,71 ,56 ,50 ,79 ,62 ,71 ,77 ,94 ,69 ,31 ,62 ,34 ,31 ,37 ,47 ,85 ,46 ,64 ,60 ,78 ,62 ,
		71 ,34 ,64 ,86 ,73 ,60 ,37 ,71 ,63 ,53 ,63 ,37 ,50 ,68 ,67 ,62 ,68 ,63 ,61 ,59 ,31 ,70 ,55 ,49 ,64 ,47 ,59 ,68 ,62 ,38 ,72 ,45 ,
		64 ,64 ,33 ,45 ,68 ,64 ,65 ,46 ,32 ,66 ,83 ,71 ,82 ,44 ,78 ,30 ,51 ,68 ,54 ,47 ,32 ,71 ,32 ,64 ,37 ,48 ,64 ,63 ,49 ,58 ,64 ,68 ,
		65 ,40 ,64 ,72 ,44 ,74 ,53 ,67 ,47 ,49 ,65 ,71 ,55 ,66 ,91 ,84 ,59 ,61 ,47 ,39 ,48 ,39 ,64 ,57 ,66 ,68 ,34 ,64 ,34 ,68 ,71 ,69 ,
		45 ,72 ,46 ,64 ,67 ,44 ,32 ,46 ,77 ,46 ,64 ,65 ,45 ,67 ,68 ,69 ,80 ,64 ,68 ,68 ,47 ,57 ,51 ,58 ,43 ,79 ,62 ,63 ,71 ,49 ,80 ,80 ,
		};


	@Test
	@DisplayName("Test Height map version 1.16 size 16")
	public void size16() {
		TestFramework.randomChunkGen(VERSION, 213232132139149124L, DIMENSION, 16, 21382138983289132L, size16);
	}

	@Test
	@DisplayName("Test Height version 1.16 map size 32")
	public void size32() {
		TestFramework.randomChunkGen(VERSION, 213232132139149124L, DIMENSION, 32, 21382138983289132L, size32);
	}

	@Test
	@DisplayName("Test Height version 1.16 map size 128")
	public void size128() {
		TestFramework.randomHashGen(VERSION, 213232132139149124L, DIMENSION, 128, 21382138983289132L, 2497443544475995326L);
	}


	// 		Bootstrap.bootStrap();
	//		MutableRegistry<Biome> biomeRegistry = DynamicRegistries.builtin().registryOrThrow(Registry.BIOME_REGISTRY);
	//		Registry<DimensionSettings> dimensionSettingsRegistry = DynamicRegistries.builtin().registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
	//		long seed = 213232132139149124L;
	//		NoiseChunkGenerator noiseChunkGenerator;
	//		int dimension = 0;
	//		if (dimension == 0) {
	//			// OVERWORLD
	//			noiseChunkGenerator = new NoiseChunkGenerator(
	//					new OverworldBiomeProvider(seed, false, false, biomeRegistry), seed,
	//					() -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.OVERWORLD));
	//		} else if (dimension == -1) {
	//			// Nether
	//			noiseChunkGenerator = new NoiseChunkGenerator(
	//					NetherBiomeProvider.Preset.NETHER.biomeSource(biomeRegistry, seed), seed,
	//					() -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.NETHER));
	//		} else {
	//			// End
	//			noiseChunkGenerator = new NoiseChunkGenerator(
	//					new EndBiomeProvider(biomeRegistry, seed), seed,
	//					() -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.END));
	//		}
	//		int size = 8;
	//		Random r = new Random(21382138983289132L);
	//		boolean PRINT = false;
	//		if (PRINT) System.out.println("{");
	//		long hash = 0;
	//		for (int i = 0; i < size; i++) {
	//			for (int j = 0; j < size; j++) {
	//				int x = r.nextInt(512000)-25600;
	//				int z = r.nextInt(512000)-25600;
	//				if (dimension==-1){
	//					Blockreader blockReader= (Blockreader) noiseChunkGenerator.getBaseColumn(x,z);
	//					if (PRINT) System.out.print("{");
	//					int y=0;
	//					for (BlockState blockState:blockReader.column){
	//						String block=Registry.BLOCK.getKey(blockState.getBlock()).getPath().toUpperCase();
	//						if (PRINT)System.out.print(block+",");
	//						hash = hash * 0xFF51AFD7ED558CCDL + 0xC4CEB9FE1A85EC53L | (long) block.hashCode()*y++;
	//					}
	//					if (PRINT)System.out.print("},");
	//					if (PRINT)System.out.println();
	//				}else{
	//					int y = noiseChunkGenerator.getHeightOnGround(x, z, Heightmap.Type.OCEAN_FLOOR_WG);
	//					hash = hash * 0xFF51AFD7ED558CCDL + 0xC4CEB9FE1A85EC53L | y;
	//					if (PRINT) System.out.printf("%d ,", y);
	//				}
	//			}
	//			if (PRINT && dimension!=-1) System.out.println();
	//		}
	//		if (PRINT) System.out.print("};");
	//		if (PRINT) System.out.println();
	//		if (!PRINT) System.out.println(hash+"L");
}
