package me.xa5.smoothbedrock.mixin;

import me.xa5.smoothbedrock.SmoothBedrock;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Random;
import java.util.function.Supplier;

@Mixin(NoiseChunkGenerator.class)
public abstract class MixinFlatBedrock extends ChunkGenerator {
    @Shadow
    @Final
    protected Supplier<ChunkGeneratorSettings> settings;

    @Shadow
    @Final
    private int worldHeight;

    public MixinFlatBedrock(BiomeSource biomeSource_1, StructuresConfig chunkGeneratorConfig_1) {
        super(biomeSource_1, chunkGeneratorConfig_1);
    }

    @Inject(method = "buildSurface", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/chunk/NoiseChunkGenerator;buildBedrock(Lnet/minecraft/world/chunk/Chunk;Ljava/util/Random;)V"), cancellable = true)
    public void buildSurface(ChunkRegion region, Chunk chunk, CallbackInfo info) {
        Identifier dimId = region.getRegistryManager().get(Registry.DIMENSION_TYPE_KEY).getId(region.getDimension());

        if (SmoothBedrock.getInstance().shouldModifyBedrock(dimId)) {
            buildFlatBedrock(chunk);
            info.cancel();
        }
    }

    private void buildFlatBedrock(Chunk chunk) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int chunkStartX = chunk.getPos().getStartX();
        int chunkStartZ = chunk.getPos().getStartZ();
        ChunkGeneratorSettings chunkGeneratorSettings = this.settings.get();
        int bedrockFloor = chunkGeneratorSettings.getBedrockFloorY();
        int bedrockRoof = this.worldHeight - 1 - chunkGeneratorSettings.getBedrockCeilingY();
        boolean generateRoof = bedrockRoof + 4 >= 0 && bedrockRoof < this.worldHeight;
        boolean generateFloor = bedrockFloor + 4 >= 0 && bedrockFloor < this.worldHeight;

        if (generateRoof) {
            for (BlockPos blockPos : BlockPos.iterate(chunkStartX, 0, chunkStartZ, chunkStartX + 15, 0, chunkStartZ + 15)) {
                chunk.setBlockState(mutable.set(blockPos.getX(), bedrockRoof, blockPos.getZ()), Blocks.BEDROCK.getDefaultState(), false);
            }
        }

        if (generateFloor) {
            for (BlockPos blockPos : BlockPos.iterate(chunkStartX, 0, chunkStartZ, chunkStartX + 15, 0, chunkStartZ + 15)) {
                chunk.setBlockState(mutable.set(blockPos.getX(), bedrockFloor, blockPos.getZ()), Blocks.BEDROCK.getDefaultState(), false);
            }
        }
    }
}