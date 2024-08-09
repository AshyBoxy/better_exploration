package dev.hugeblank.jbe.mixin.horse_armor;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.item.AnimalArmorItem;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnimalArmorItem.class)
public abstract class AnimalArmorItemMixin extends ArmorItem {
    @Shadow @Final private AnimalArmorItem.Type type;

    public AnimalArmorItemMixin(RegistryEntry<ArmorMaterial> material, Type type, Settings settings) {
        super(material, type, settings);
    }

    @ModifyReturnValue(method = "isEnchantable", at = @At("RETURN"))
    public boolean isEnchantable(boolean original) {
        if (this.type .equals(AnimalArmorItem.Type.EQUESTRIAN)) return true;
        return original;
    }
}
