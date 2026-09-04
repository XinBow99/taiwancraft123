package com.xinbow99.taiwan.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * 把機車放到地上。
 *
 * <p>只認「對著方塊上表面按右鍵」：對著側面放的話車會半個埋進牆裡，然後被擠出來彈飛。
 * 原版的船是丟在水面上，但機車要停在地上，所以用 {@link UseOnContext} 而不是
 * {@code use()} 的射線。
 */
public class ScooterItem extends Item {

    public ScooterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos on = context.getClickedPos().relative(context.getClickedFace());

        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;

        Scooter scooter = TaiwanEntities.SCOOTER.create(server, EntitySpawnReason.SPAWN_ITEM_USE);
        if (scooter == null) return InteractionResult.FAIL;

        scooter.snapTo(on.getX() + 0.5, on.getY(), on.getZ() + 0.5,
                context.getHorizontalDirection().toYRot(), 0.0f);
        if (!server.noCollision(scooter)) return InteractionResult.FAIL;

        server.addFreshEntity(scooter);
        level.playSound(null, scooter, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.7f, 1.4f);

        ItemStack stack = context.getItemInHand();
        if (context.getPlayer() != null && !context.getPlayer().hasInfiniteMaterials()) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
