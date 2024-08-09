package dev.hugeblank.jbe.item;

import dev.hugeblank.jbe.MainInit;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.UnaryOperator;

public class CustomDataComponentTypes {
    // custom data components are a little annoying to add
    public static final ComponentType<Integer> STORED_EXPERIENCE = register("stored_experience", b -> b.codec(Codecs.NONNEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT));

    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builder) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, MainInit.id(id), builder.apply(ComponentType.builder()).build());
    }

    // noop to load the class
    public static void init() {}
}
