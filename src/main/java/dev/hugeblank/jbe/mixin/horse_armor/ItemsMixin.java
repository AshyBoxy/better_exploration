package dev.hugeblank.jbe.mixin.horse_armor;

import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Items.class)
public class ItemsMixin {

    @ModifyArgs(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/AnimalArmorItem;<init>(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/item/AnimalArmorItem$Type;ZLnet/minecraft/item/Item$Settings;)V"), method = "<clinit>")
    private static void jbe$modifyHorseArmor(Args args) {
        jbe$modifyArmor(args);
    }

    @ModifyArgs(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/AnimalArmorItem;<init>(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/item/AnimalArmorItem$Type;ZLnet/minecraft/item/Item$Settings;)V"), method = "<clinit>")
    private static void jbe$modifyDyeableHorseArmor(Args args) {
        jbe$modifyArmor(args);
    }

    @Unique
    private static void jbe$modifyArmor(Args args) {
        if(args.get(1) != AnimalArmorItem.Type.EQUESTRIAN) return;
        for (ArmorMaterial material : Registries.ARMOR_MATERIAL.stream().toList()) {
            if (material.equals(args.get(1))) {
                Item.Settings settings = args.get(3);
                // no clue where these numbers went
                settings.maxDamage(ArmorItem.Type.LEGGINGS.getMaxDamage(material.defense().getOrDefault(ArmorItem.Type.BODY, 7)));
                break;
            }
        }
    }
}
