package dev.hugeblank.jbe.village;

import dev.hugeblank.jbe.MainInit;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.item.map.MapState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SellCustomMapTradeFactory implements TradeOffers.Factory {
    private final int price;
    private final TagKey<Structure> structure;
    private final String nameKey;
    private final RegistryEntry<MapDecorationType> decorationType;
    private final int maxUses;
    private final int experience;

    public SellCustomMapTradeFactory(int price, TagKey<Structure> structure, String nameKey, RegistryEntry<MapDecorationType> decorationType,
                                     int maxUses, int experience) {
        this.price = price;
        this.structure = structure;
        this.nameKey = nameKey;
        this.decorationType = decorationType;
        this.maxUses = maxUses;
        this.experience = experience;
    }

    @Nullable
    @Override
    public TradeOffer create(Entity entity, Random random) {
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return null;
        } else {
            BlockPos blockPos = serverWorld.locateStructure(this.structure, entity.getBlockPos(), 100, true);
            if (blockPos != null) {
                ItemStack itemStack = createMap(serverWorld, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
                FilledMapItem.fillExplorationMap(serverWorld, itemStack);
                MapState.addDecorationsNbt(itemStack, blockPos, "+", this.decorationType);
                itemStack.set(DataComponentTypes.ITEM_NAME, Text.translatable(this.nameKey));
                return new TradeOffer(new TradedItem(Items.EMERALD, this.price), Optional.of(new TradedItem(Items.COMPASS)),
                        itemStack, this.maxUses, this.experience, 0.2F);
            } else {
                return null;
            }
        }
    }

    private static ItemStack createMap(World world, int x, int z, byte scale, boolean showIcons, boolean unlimitedTracking) {
        ItemStack itemStack = new ItemStack(MainInit.FILLED_CAVE_MAP);
//        FilledMapItemAccessor.callCreateMapState(itemStack, world, x, z, scale, showIcons, unlimitedTracking, world.getRegistryKey());
        // i *think* this is the same thing
        FilledMapItem.createMap(world, x, z, scale, showIcons, unlimitedTracking);
        return itemStack;
    }
}
