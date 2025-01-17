package me.jellysquid.mods.sodium.mixin.entity.block_cache;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Caches the block at the entity's feet, reducing the number of calls to the world in situations with excessive entity
 * crowding. This likely won't make any improvement in normal situations.
 */
@Mixin(value = LivingEntity.class, priority = 999)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    private BlockState lastStateAtFeet = null;

    private long lastPosition = Long.MIN_VALUE;

    @Inject(method = "baseTick", at = @At("HEAD"))
    private void clearCache(CallbackInfo ci) {
        this.lastStateAtFeet = null;
        this.lastPosition = Long.MIN_VALUE;
    }

    /**
     * @reason Avoid accessing the block from the world if possible
     * @author JellySquid
     */
    @Overwrite
    public BlockState getFeetBlockState() {
        int x = MathHelper.floor(this.getX());
        int y = MathHelper.floor(this.getY());
        int z = MathHelper.floor(this.getZ());

        long pos = BlockPos.asLong(x, y, z);

        if (this.lastPosition == pos) {
            return this.lastStateAtFeet;
        }

        BlockState state = this.level.getBlockState(new BlockPos(x, y, z));

        this.lastPosition = pos;
        this.lastStateAtFeet = state;

        return state;
    }
}
