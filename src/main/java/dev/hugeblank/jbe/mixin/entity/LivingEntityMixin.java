package dev.hugeblank.jbe.mixin.entity;

import dev.hugeblank.jbe.MainInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

//    @Shadow @Nullable public abstract @Nullable EntityAttributeInstance getAttributeInstance(RegistryEntry<EntityAttribute> attribute);

//    @Unique
//    private static Identifier UNHINGED_MODIFIER = MainInit.id("unhinged_speed");

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    // where is this actually supposed to run?
//    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"), method = "addSoulSpeedBoostIfNeeded", cancellable = true)
//    private void jbe$horsePOWER(CallbackInfo ci) {
//        if ((Object) this instanceof HorseEntity horse) {
//            horse.getArmorItems().iterator().next().damage(1, horse, horseEntity -> horseEntity.sendEquipmentBreakStatus(EquipmentSlot.CHEST));
//            ci.cancel();
//        }
//    }

    // decent chance this does something different now
//    @Inject(at = @At("HEAD"), method = "applyMovementEffects")
//    private void jbe$unhingedHorseSpeed(ServerWorld world, BlockPos pos, CallbackInfo ci) {
//        if ((Object) this instanceof HorseEntity horse) {
//            EntityAttributeInstance movementSpeed = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
//            if (horse.hasPassengers()) {
////                double level = (((double)args.get(2)/0.03f)-1.0f)/0.35;
////                args.set(2, 0.30+(level*0.105));
////                args.set(3, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
//                movementSpeed.addTemporaryModifier(new EntityAttributeModifier(UNHINGED_MODIFIER, , EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
//            } else movementSpeed.removeModifier(UNHINGED_MODIFIER);
//        }
//    }
    // i realised part way through implementing it that that probably makes soul speed faster
    // that should be implemented by checking if the entity with the enchantment is a horse
    // in the soul speed enchantment definition itself
}
