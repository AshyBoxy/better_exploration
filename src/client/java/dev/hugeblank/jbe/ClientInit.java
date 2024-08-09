package dev.hugeblank.jbe;

import dev.hugeblank.jbe.item.CustomDataComponentTypes;
import dev.hugeblank.jbe.item.SculkVialItem;
import dev.hugeblank.jbe.network.JbeStateChangeS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapColorComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class ClientInit implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelPredicateProviderRegistry.register(MainInit.SCULK_VIAL, Identifier.ofVanilla("experience"), (itemStack, clientWorld, livingEntity, seed) -> {
			int xp = itemStack.getOrDefault(CustomDataComponentTypes.STORED_EXPERIENCE, 0);
			return xp == 0 ? 0 : (float) xp / SculkVialItem.MAX_XP;
		});

		BlockRenderLayerMap.INSTANCE.putBlock(MainInit.POWERED_RAIL, RenderLayer.getCutout());

		ClientPlayNetworking.registerGlobalReceiver(JbeStateChangeS2CPacket.PACKET_ID, (packet, player) -> {
            if (packet.getReason() == JbeStateChangeS2CPacket.ALLOW_ICE_BOAT_SPEED) {
                player.player().getWorld().getGameRules().get(MainInit.ALLOW_ICE_BOAT_SPEED).set(packet.getValue() == 1.0F, null);
            }
		});

		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 0 ? -1 : stack.getOrDefault(DataComponentTypes.MAP_COLOR, MapColorComponent.DEFAULT).rgb(), MainInit.FILLED_CAVE_MAP);
	}
}
