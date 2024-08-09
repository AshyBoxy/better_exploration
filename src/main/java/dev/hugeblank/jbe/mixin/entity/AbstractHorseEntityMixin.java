package dev.hugeblank.jbe.mixin.entity;

import dev.hugeblank.jbe.MainInit;
import dev.hugeblank.jbe.entity.StaminaMount;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.EntityView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHorseEntity.class)
public abstract class AbstractHorseEntityMixin extends AnimalEntity implements StaminaMount {

    @Unique
    private static TrackedData<Integer> HORSE_STAMINA;

    @Unique
    private static TrackedData<Boolean> HORSE_EXHAUSTED;

    @Unique
    private final EntityAttributeInstance HORSE_MOVEMENT_SPEED = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);

    @Shadow protected SimpleInventory items;

    @Shadow @Nullable public abstract LivingEntity getControllingPassenger();

    @Shadow public abstract boolean isTame();

    @Shadow public abstract boolean isSaddled();

    @Shadow protected abstract @Nullable SoundEvent getAngrySound();

    protected AbstractHorseEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean canSprintAsVehicle() {
        return !this.dataTracker.get(HORSE_EXHAUSTED);
    }

    @Inject(at = @At("TAIL"), method = "initDataTracker")
    private void jbe$initTrackedStamina(CallbackInfo ci) {
        // unsure on data trackers
        // this should probably be swapped for data attachment or cardinal components though
        if (this.dataTracker.get(HORSE_STAMINA) == null || this.dataTracker.get(HORSE_EXHAUSTED) == null) {
            this.dataTracker.set(HORSE_STAMINA, this.getWorld().getGameRules().getInt(MainInit.HORSE_STAMINA));
            this.dataTracker.set(HORSE_EXHAUSTED, false);
        }
    }

    // pretty sure this won't work as intended, when the gamerules are changed
    // the attributes won't update until after being removed and added again
    @Inject(at = @At("TAIL"), method = "tickMovement")
    private void jbe$tickStamina(CallbackInfo ci) {
        if (!this.getWorld().isClient() && HORSE_MOVEMENT_SPEED != null) {
            GameRules rules = this.getWorld().getGameRules();
            int stamina = rules.getInt(MainInit.HORSE_STAMINA);
            LivingEntity controller = this.getControllingPassenger();
            int staminaTicks = this.dataTracker.get(HORSE_STAMINA);
            boolean exhausted = this.dataTracker.get(HORSE_EXHAUSTED);
            // if nobody is mounted, or the controller is not sprinting, THEN canRecover is true.
            boolean canRecover = controller == null || !controller.isSprinting();
            if (canRecover && staminaTicks < stamina) {
                this.dataTracker.set(HORSE_STAMINA, Math.min(staminaTicks + rules.getInt(MainInit.HORSE_STAMINA_REGEN), stamina)); // regenerate stamina
                if (HORSE_MOVEMENT_SPEED.hasModifier(MainInit.HORSE_SPRINT_MOD_ID)) {
                    HORSE_MOVEMENT_SPEED.removeModifier(MainInit.HORSE_SPRINT_MOD_ID);
                }
            } else if (!canRecover && staminaTicks > 0 && this.isTame() && this.isSaddled()){
                this.dataTracker.set(HORSE_STAMINA, staminaTicks-1); // decrement stamina
                if (!HORSE_MOVEMENT_SPEED.hasModifier(MainInit.HORSE_SPRINT_MOD_ID)) {
                    HORSE_MOVEMENT_SPEED.addTemporaryModifier(new EntityAttributeModifier(MainInit.HORSE_SPRINT_MOD_ID, MainInit.HORSE_SPRINT_VALUE, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
            }
            if (staminaTicks == 0 && !exhausted) {
                this.dataTracker.set(HORSE_EXHAUSTED, true);
                SoundEvent soundEvent = this.getAngrySound();
                if (soundEvent != null) {
                    this.playSound(soundEvent, this.getSoundVolume(), this.getSoundPitch());
                }
                if (controller != null) {
                    controller.setSprinting(false);
                }
                if (!HORSE_MOVEMENT_SPEED.hasModifier(MainInit.HORSE_EXHAUSTED_MOD_ID)) {
                    HORSE_MOVEMENT_SPEED.addTemporaryModifier(new EntityAttributeModifier(MainInit.HORSE_EXHAUSTED_MOD_ID, MainInit.HORSE_EXHAUSTED_VALUE, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
                if (HORSE_MOVEMENT_SPEED.hasModifier(MainInit.HORSE_SPRINT_MOD_ID)) {
                    HORSE_MOVEMENT_SPEED.removeModifier(MainInit.HORSE_SPRINT_MOD_ID);
                }
            } else if (staminaTicks == stamina && exhausted) {
                this.dataTracker.set(HORSE_EXHAUSTED, false);
                HORSE_MOVEMENT_SPEED.removeModifier(MainInit.HORSE_EXHAUSTED_MOD_ID);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "writeCustomDataToNbt")
    private void jbe$writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("StaminaTicks", this.dataTracker.get(HORSE_STAMINA));
        nbt.putBoolean("Exhausted", this.dataTracker.get(HORSE_EXHAUSTED));
    }

    @Inject(at = @At("HEAD"), method = "readCustomDataFromNbt")
    private void jbe$readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this.dataTracker.get(HORSE_STAMINA) == null || this.dataTracker.get(HORSE_EXHAUSTED) == null) {
            this.dataTracker.set(HORSE_STAMINA, nbt.getInt("StaminaTicks"));
            this.dataTracker.set(HORSE_EXHAUSTED, nbt.getBoolean("Exhausted"));
        } else {
            this.dataTracker.set(HORSE_STAMINA, nbt.getInt("StaminaTicks"));
            this.dataTracker.set(HORSE_EXHAUSTED, nbt.getBoolean("Exhausted"));
        }
    }

    @Unique
    public int jbe$getStamina() {
        return this.dataTracker.get(HORSE_STAMINA);
    }

    @SuppressWarnings("WrongEntityDataParameterClass")
    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void jbe$clinit(CallbackInfo ci) {
        HORSE_STAMINA = DataTracker.registerData(AbstractHorseEntity.class, TrackedDataHandlerRegistry.INTEGER);
        HORSE_EXHAUSTED = DataTracker.registerData(AbstractHorseEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }
}
