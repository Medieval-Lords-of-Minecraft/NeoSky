package me.neoblade298.neosky;

import java.util.Arrays;
import java.util.List;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

public class NeoSkyBiomeProvider extends BiomeProvider {
    private final static Biome BIOME = Biome.FOREST;
    private final static List<Biome> BIOMES = Arrays.asList(BIOME);

    @Override
    public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
        return BIOME;
    }

    @Override
    public List<Biome> getBiomes(WorldInfo worldInfo) {
        return BIOMES;
    }

}
