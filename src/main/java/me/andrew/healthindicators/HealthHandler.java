package me.andrew.healthindicators;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

import java.util.concurrent.ThreadLocalRandom;

public class HealthHandler {
    long actionTimer;
    long hitTimer;
    int lastSlot = -1;
    boolean enabled = true;
    public static MinecraftClient mc = MinecraftClient.getInstance();
    public HealthHandler(){
    }
    public void onTick(){
        while (mc.options.loadToolbarActivatorKey.wasPressed()){
            enabled = !enabled;
        }
        while(mc.options.saveToolbarActivatorKey.wasPressed()){
            enabled = !enabled;
        }
        if(!doStuff()){
            reset();
        }
    }
    private boolean nullCheck(){
        return mc.player != null && mc.world != null && mc.interactionManager != null;
    }
    private void reset(){
        if(lastSlot != -1 && nullCheck()){
            if(actionTimer < System.currentTimeMillis()){
                mc.player.getInventory().selectedSlot = lastSlot;
                lastSlot = -1;
            }
        } else {
            actionTimer = System.currentTimeMillis() + actionDelay();
            hitTimer = System.currentTimeMillis() + hitDelay();
        }
    }
    private boolean doStuff(){
        if(!nullCheck()){
            return false;
        }

        if(!enabled){
            return false;
        }

        if(!mc.options.getHideMatchedNames().getValue()){
            return false;
        }

        if(mc.options.pickItemKey.isPressed()){
            return false;
        }

        if(mc.player.isUsingItem()) {
            return false;
        }

        if(!(mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() instanceof PlayerEntity target)){
            return true;
        }

        if(!target.isBlocking()){
            return false;
        }

        int axeSlot = -1;
        double diff = Integer.MAX_VALUE;
        for(int i = 0; i < 9; i++){
            ItemStack is = mc.player.getInventory().getStack(i);
            double currDiff = Math.abs(mc.player.getInventory().selectedSlot - i);
            if(is.getItem() instanceof AxeItem && currDiff < diff){
                axeSlot = i;
                diff = currDiff;
            }
        }

        if(axeSlot == -1){
            return false;
        }

        if(mc.player.getInventory().selectedSlot != axeSlot) {
            if (actionTimer < System.currentTimeMillis()) {
                if(lastSlot == -1){
                    lastSlot = mc.player.getInventory().selectedSlot;
                }
                mc.player.getInventory().selectedSlot = axeSlot;
                actionTimer = System.currentTimeMillis() + actionDelay();
            }
        }else{
            if(hitTimer < System.currentTimeMillis()){
                doAttack();
                hitTimer = System.currentTimeMillis() + hitDelay();
            }
        }
        return true;
    }
    private boolean doAttack(){
        if (mc.attackCooldown > 0) {
            return false;
        }
        if (mc.crosshairTarget == null) {
            if (mc.interactionManager.hasLimitedAttackSpeed()) {
                mc.attackCooldown = 10;
            }
            return false;
        }
        if (mc.player.isRiding()) {
            return false;
        }
        ItemStack itemStack = mc.player.getStackInHand(Hand.MAIN_HAND);
        if (!itemStack.isItemEnabled(mc.world.getEnabledFeatures())) {
            return false;
        }
        boolean bl = false;
        switch (mc.crosshairTarget.getType()) {
            case ENTITY: {
                mc.interactionManager.attackEntity(mc.player, ((EntityHitResult)mc.crosshairTarget).getEntity());
                break;
            }
            case MISS: {
                if (mc.interactionManager.hasLimitedAttackSpeed()) {
                    mc.attackCooldown = 10;
                }
                mc.player.resetLastAttackedTicks();
            }
        }
        mc.player.swingHand(Hand.MAIN_HAND);
        return bl;
    }
    private long actionDelay(){
        return randomLong((long) (mc.options.getChatHeightFocused().getValue() * 180), (long) (mc.options.getChatHeightUnfocused().getValue() * 180));
    }
    private long hitDelay(){
        return randomLong((long) (mc.options.getDarknessEffectScale().getValue() * 150), (long) (mc.options.getDistortionEffectScale().getValue() * 150));
    }
    private long randomLong(long origin, long bound){
        if(bound <= origin){
            return bound;
        }
        return ThreadLocalRandom.current().nextLong(origin, bound);
    }
}
