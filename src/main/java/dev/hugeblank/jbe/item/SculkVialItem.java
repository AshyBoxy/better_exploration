package dev.hugeblank.jbe.item;

import dev.hugeblank.jbe.MainInit;
import net.minecraft.block.MapColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.List;

public class SculkVialItem extends Item {

    public static final int MAX_XP = 1395;

    public SculkVialItem(Settings settings) {
        super(settings);
    }

    protected int getVialExperience(ItemStack itemStack) {
        return itemStack.getOrDefault(CustomDataComponentTypes.STORED_EXPERIENCE, 0);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        int vialXP = getVialExperience(stack);
        stack.set(CustomDataComponentTypes.STORED_EXPERIENCE, 0);
        world.playSound(null, user.getBlockPos(), MainInit.SCULK_VIAL_WITHDRAW, SoundCategory.PLAYERS, 1F, world.random.nextFloat() * 0.1F + 0.9F);
        if (user instanceof PlayerEntity player) {
            player.addExperience(vialXP);
        }
        return stack;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        int vialXP = getVialExperience(itemStack);
        int playerXP = getPlayerTotalExperience(user);
        if (user.isSneaking() && vialXP <= MAX_XP && (playerXP > 0 || user.isCreative())) {
            int freeSpace = MAX_XP-vialXP;
            int addXp = user.isCreative() ? freeSpace : Math.min(playerXP, freeSpace);
            if (addXp > 0) {
                if (user instanceof ServerPlayerEntity && !user.isCreative()) {
                    user.addExperience(-addXp);
                }
                itemStack.set(CustomDataComponentTypes.STORED_EXPERIENCE, vialXP + addXp);
                world.playSound(null, user.getBlockPos(), MainInit.SCULK_VIAL_DEPOSIT, SoundCategory.PLAYERS, 1F, world.random.nextFloat() * 0.1F + 0.9F);
                return new TypedActionResult<>(ActionResult.SUCCESS, itemStack);
            }
        } else if (!user.isSneaking() && vialXP > 0) {
            return ItemUsage.consumeHeldItem(world, user, hand);
        }
        return new TypedActionResult<>(ActionResult.PASS, itemStack);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return getVialExperience(stack) > 0 ? UseAction.DRINK : UseAction.NONE;
    }

    public SoundEvent getDrinkSound() {
        return SoundEvents.ENTITY_GENERIC_DRINK;
    }

    public SoundEvent getEatSound() {
        return SoundEvents.ENTITY_GENERIC_DRINK;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return getVialExperience(stack) > 0;
    }

    @Override
    public void appendTooltip(ItemStack itemStack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(itemStack, context, tooltip, type);
        tooltip.add(Text.translatable("item.jbe.sculk_vial.levels",
                        Text.literal(getVialExperience(itemStack) + "/" + MAX_XP).withColor(MapColor.LIGHT_GRAY.color))
                .withColor(MapColor.GRAY.color)
        );
        tooltip.add(Text.translatable("item.jbe.sculk_vial.usage.fill", Text.keybind("key.sneak"), Text.keybind("key.use"))
                .withColor(MapColor.GRAY.color)
        );
        tooltip.add(Text.translatable("item.jbe.sculk_vial.usage.drain", Text.keybind("key.use"))
                .withColor(MapColor.GRAY.color)
        );
    }

    private static int getLevelUpExperience(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else {
            return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
        }
    }

    private static int getPlayerTotalExperience(PlayerEntity user) {
        int levels = user.experienceLevel;
        int points = 0;
        while (levels > 0) {
            points += getLevelUpExperience(--levels);
        }
        points += Math.round(((float) user.getNextLevelExperience()) * user.experienceProgress);
        return points;
    }
}
