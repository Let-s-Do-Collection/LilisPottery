package net.satisfy.lilis_pottery.core.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.lilis_pottery.core.registry.EntityTypeRegistry;
import net.satisfy.lilis_pottery.core.util.GeneralUtil;
import org.jetbrains.annotations.NotNull;

public class AbstractStorageBlockEntity extends BlockEntity implements Clearable {

    private int size;
    private NonNullList<ItemStack> inventory;

    private int sideColorRgb;
    private boolean glazed;
    private int glazeColorRgb;
    private float glazeStrength;

    private boolean painted;

    public AbstractStorageBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.STORAGE_BLOCK_ENTITY.get(), pos, state);
        this.size = 0;
        this.inventory = NonNullList.create();
        this.sideColorRgb = 0;
        this.glazed = false;
        this.glazeColorRgb = 0;
        this.glazeStrength = 1.0F;
        this.painted = false;
    }

    public AbstractStorageBlockEntity(BlockPos pos, BlockState state, int size) {
        super(EntityTypeRegistry.STORAGE_BLOCK_ENTITY.get(), pos, state);
        this.size = size;
        this.inventory = NonNullList.withSize(this.size, ItemStack.EMPTY);
        this.sideColorRgb = 0;
        this.glazed = false;
        this.glazeColorRgb = 0;
        this.glazeStrength = 1.0F;
        this.painted = false;
    }

    public void setStack(int slot, ItemStack stack) {
        if (slot >= 0 && slot < inventory.size()) {
            inventory.set(slot, stack);
            setChanged();
        }
    }

    public ItemStack removeStack(int slot) {
        if (slot >= 0 && slot < inventory.size()) {
            ItemStack stack = inventory.set(slot, ItemStack.EMPTY);
            setChanged();
            return stack;
        }
        return ItemStack.EMPTY;
    }

    public int getSideColorRgb() {
        return sideColorRgb;
    }

    public void setSideColorRgb(int sideColorRgb) {
        this.sideColorRgb = sideColorRgb;
        setChanged();
    }

    public boolean isGlazed() {
        return glazed;
    }

    public void setGlazed(boolean glazed) {
        this.glazed = glazed;
        setChanged();
    }

    public int getGlazeColorRgb() {
        return glazeColorRgb;
    }

    public void setGlazeColorRgb(int glazeColorRgb) {
        this.glazeColorRgb = glazeColorRgb;
        setChanged();
    }

    public float getGlazeStrength() {
        return glazeStrength;
    }

    public void setGlazeStrength(float glazeStrength) {
        this.glazeStrength = glazeStrength;
        setChanged();
    }

    public boolean isPainted() {
        return painted;
    }

    public void setPainted(boolean painted) {
        this.painted = painted;
        setChanged();
    }

    @Override
    public void setChanged() {
        if (level instanceof ServerLevel serverLevel) {
            if (!level.isClientSide()) {
                Packet<ClientGamePacketListener> updatePacket = getUpdatePacket();
                for (ServerPlayer player : GeneralUtil.tracking(serverLevel, getBlockPos())) {
                    player.connection.send(updatePacket);
                }
            }
        }
        super.setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        this.size = compoundTag.getInt("size");
        this.inventory = NonNullList.withSize(this.size, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compoundTag, this.inventory, provider);

        this.sideColorRgb = compoundTag.getInt("sideColorRgb");
        this.glazed = compoundTag.getBoolean("glazed");
        this.glazeColorRgb = compoundTag.getInt("glazeColorRgb");
        this.glazeStrength = compoundTag.contains("glazeStrength") ? compoundTag.getFloat("glazeStrength") : 1.0F;

        this.painted = compoundTag.getBoolean("painted");
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ContainerHelper.saveAllItems(compoundTag, this.inventory, provider);
        compoundTag.putInt("size", this.size);

        compoundTag.putInt("sideColorRgb", this.sideColorRgb);
        compoundTag.putBoolean("glazed", this.glazed);
        compoundTag.putInt("glazeColorRgb", this.glazeColorRgb);
        compoundTag.putFloat("glazeStrength", this.glazeStrength);

        compoundTag.putBoolean("painted", this.painted);

        super.saveAdditional(compoundTag, provider);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < inventory.size(); slot++) {
            inventory.set(slot, ItemStack.EMPTY);
        }
        setChanged();
    }
}