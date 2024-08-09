package dev.hugeblank.jbe;

import dev.hugeblank.jbe.item.CustomDataComponentTypes;
import dev.hugeblank.jbe.item.SculkVialItem;
import dev.hugeblank.jbe.network.JbeStateChangeS2CPacket;
import dev.hugeblank.jbe.village.SellCustomMapTradeFactory;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.item.*;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainInit implements ModInitializer {
	public static final String ID = "jbe";
	public static final TagKey<Structure> ON_ANCIENT_CITY_MAPS;

	public static final SoundEvent SCULK_VIAL_DEPOSIT;
	public static final SoundEvent SCULK_VIAL_WITHDRAW;

	public static final GameRules.Key<GameRules.BooleanRule> ALLOW_ICE_BOAT_SPEED;
	public static final GameRules.Key<GameRules.IntRule> HORSE_STAMINA;
	public static final GameRules.Key<GameRules.IntRule> HORSE_EXHAUST_DEBUFF;
	public static final GameRules.Key<GameRules.IntRule> HORSE_SPRINT_BUFF;
	public static final GameRules.Key<GameRules.IntRule> HORSE_STAMINA_REGEN;

	public static final Identifier HORSE_EXHAUSTED_MOD_ID = id("horse_exhaust");
	public static final Identifier HORSE_SPRINT_MOD_ID = id("horse_sprint");
	public static double HORSE_EXHAUSTED_VALUE = 0;
	public static double HORSE_SPRINT_VALUE = 0;

	public static final SculkVialItem SCULK_VIAL;
	public static final FilledMapItem FILLED_CAVE_MAP;
	public static final PoweredRailBlock POWERED_RAIL;

	public static FeatureSet FORCE_TRADE_REBALANCE;
	private static final TradeOffers.Factory SELL_ANCIENT_CITY_MAP_TRADE;

	// what?
	public static final RegistryEntry<MapDecorationType> ANCIENT_CITY_DECORATION = Registry.registerReference(Registries.MAP_DECORATION_TYPE, RegistryKey.of(RegistryKeys.MAP_DECORATION_TYPE, id("ancient_city")), new MapDecorationType(id("ancient_city"), true, 0x236e86, true, false));

	private static boolean registeredTrade = false;

	@Override
	public void onInitialize() {
		Registry.register(Registries.SOUND_EVENT, SCULK_VIAL_DEPOSIT.getId(), SCULK_VIAL_DEPOSIT);
		Registry.register(Registries.SOUND_EVENT, SCULK_VIAL_WITHDRAW.getId(), SCULK_VIAL_WITHDRAW);

		Registry.register(Registries.ITEM, id("sculk_vial"), SCULK_VIAL);
		Registry.register(Registries.ITEM, id("filled_cave_map"), FILLED_CAVE_MAP);
		Registry.register(Registries.BLOCK, id("powered_rail"), POWERED_RAIL);
		Registry.register(Registries.ITEM, Registries.BLOCK.getId(POWERED_RAIL), new BlockItem(POWERED_RAIL, new Item.Settings()));

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register((content) -> content.addAfter(Items.POWERED_RAIL, POWERED_RAIL));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((content) -> content.addAfter(Items.POWERED_RAIL, POWERED_RAIL));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((content) -> content.add(SCULK_VIAL));

		ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
			// Remove unlockable trades
			Int2ObjectMap<TradeOffers.Factory[]> cartographer = TradeOffers.REBALANCED_PROFESSION_TO_LEVELED_TRADE.get(VillagerProfession.CARTOGRAPHER);
			List<TradeOffers.Factory> factories = new ArrayList<>(List.of(cartographer.get(5)));
			factories.remove(SELL_ANCIENT_CITY_MAP_TRADE);
			registeredTrade = false;
			cartographer.put(5, factories.toArray(new TradeOffers.Factory[0]));
		});

		ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
			// Set sprint and exhaustion modifiers
			HORSE_SPRINT_VALUE = (double) server.getGameRules().getInt(HORSE_SPRINT_BUFF) / 100;
			HORSE_EXHAUSTED_VALUE = (double) server.getGameRules().getInt(HORSE_EXHAUST_DEBUFF) / 100;
			// Add unlockable trades if conditions are met
			ServerWorld end = Objects.requireNonNull(server.getWorld(World.END));
			EnderDragonFight enderDragonFight = end.getEnderDragonFight();
			if (enderDragonFight == null || enderDragonFight.hasPreviouslyKilled()) {
				MainInit.registerAncientCityMapTrade();
			}
		});

		ServerLifecycleEvents.SERVER_STARTING.register(
				(server) -> FORCE_TRADE_REBALANCE = server.getSaveProperties().getEnabledFeatures().combine(FeatureSet.of(FeatureFlags.TRADE_REBALANCE))
		);

		PayloadTypeRegistry.playS2C().register(JbeStateChangeS2CPacket.PACKET_ID, JbeStateChangeS2CPacket.PACKET_CODEC);

		CustomDataComponentTypes.init();
	}

	static {
		SCULK_VIAL = new SculkVialItem(new Item.Settings().maxCount(1).component(CustomDataComponentTypes.STORED_EXPERIENCE, 0));

		SCULK_VIAL_DEPOSIT = SoundEvent.of(id("item.sculk_vial.deposit"));
		SCULK_VIAL_WITHDRAW = SoundEvent.of(id("item.sculk_vial.withdraw"));

		FILLED_CAVE_MAP = new FilledMapItem(new Item.Settings());

		POWERED_RAIL = new PoweredRailBlock(AbstractBlock.Settings.copy(Blocks.POWERED_RAIL));

		ON_ANCIENT_CITY_MAPS = TagKey.of(RegistryKeys.STRUCTURE, Identifier.ofVanilla("on_ancient_city_maps"));
		SELL_ANCIENT_CITY_MAP_TRADE = new SellCustomMapTradeFactory(14, MainInit.ON_ANCIENT_CITY_MAPS, "filled_map.ancient_city", ANCIENT_CITY_DECORATION, 12, 5);

		ALLOW_ICE_BOAT_SPEED = GameRuleRegistry.register("allowIceBoatSpeed", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false, (minecraftServer, booleanRule) -> {
            for (ServerPlayerEntity serverPlayerEntity : minecraftServer.getPlayerManager().getPlayerList()) {
				ServerPlayNetworking.send(serverPlayerEntity, new JbeStateChangeS2CPacket(JbeStateChangeS2CPacket.ALLOW_ICE_BOAT_SPEED, booleanRule.get() ? 1.0F : 0.0F));
            }
		}));

		HORSE_STAMINA = GameRuleRegistry.register(
				"horseStaminaTicks",
				GameRules.Category.MOBS,
				GameRuleFactory.createIntRule(400, 0, (int) Float.MAX_VALUE, (minecraftServer, intRule) -> {
					for (ServerPlayerEntity serverPlayerEntity : minecraftServer.getPlayerManager().getPlayerList()) {
						ServerPlayNetworking.send(serverPlayerEntity, new JbeStateChangeS2CPacket(JbeStateChangeS2CPacket.HORSE_STAMINA, intRule.get()));
					}
				})
		);
		HORSE_STAMINA_REGEN = GameRuleRegistry.register(
				"horseStaminaRegenTicks",
				GameRules.Category.MOBS,
				GameRuleFactory.createIntRule(1, 0, Integer.MAX_VALUE)
		);
		HORSE_SPRINT_BUFF = GameRuleRegistry.register(
				"horseSprintBuff",
				GameRules.Category.MOBS,
				GameRuleFactory.createIntRule(10, 0, Integer.MAX_VALUE, (server, intRule) -> HORSE_SPRINT_VALUE = (double) intRule.get() / 100)
		);
		HORSE_EXHAUST_DEBUFF = GameRuleRegistry.register(
				"horseExhaustDebuff",
				GameRules.Category.MOBS,
				GameRuleFactory.createIntRule(-40, -100, 0, (server, intRule) -> HORSE_EXHAUSTED_VALUE = (double) intRule.get() / 100)
		);

		MainDataModifications.init();
	}

	public static void registerAncientCityMapTrade() {
		if (!registeredTrade) {
			TradeOfferHelper.registerVillagerOffers(VillagerProfession.CARTOGRAPHER, 5, (trades, rebalance) -> {
				if (rebalance) {
					trades.add(SELL_ANCIENT_CITY_MAP_TRADE);
				}
			});
			registeredTrade = true;
		}
	}

	public static Identifier id(String path) {
		return Identifier.of(ID, path);
	}
}
