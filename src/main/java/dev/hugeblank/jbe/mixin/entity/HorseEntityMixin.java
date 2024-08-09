package dev.hugeblank.jbe.mixin.entity;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.item.AnimalArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(HorseEntity.class)
public abstract class HorseEntityMixin extends AbstractHorseEntityMixin{
    // did this class even do anything? i'm confused

    protected HorseEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

//    @Override
//    public Iterable<ItemStack> getArmorItems() {
//        return List.of(this.getArmorType());
//    }

    @Override
    public void damageArmor(DamageSource source, float amount) {
        if (!(amount <= 0.0F)) {
            amount /= 4.0F;
            if (amount < 1.0F) {
                amount = 1.0F;
            }
            ItemStack armor = this.getBodyArmor();
            // surely there's a better way to do this?
            if ((!source.isIn(DamageTypeTags.IS_FIRE) || !armor.contains(DataComponentTypes.FIRE_RESISTANT)) && (armor.getItem() instanceof AnimalArmorItem animalArmor && animalArmor.getType() == AnimalArmorItem.Type.EQUESTRIAN)) {
                int armorCountBefore = armor.getCount();
                armor.damage((int) amount, this, EquipmentSlot.BODY);
                if (!(armorCountBefore > armor.getCount())) return;
                if (!this.getWorld().isClient) this.playSound(SoundEvents.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            }
        }
    }
}
