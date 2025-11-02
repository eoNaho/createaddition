package com.mrh0.createaddition.energy;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import team.reborn.energy.api.EnergyStorage;

public class InternalEnergyStorage extends SnapshotParticipant<Long> implements EnergyStorage {
    protected long energy = 0; // Changed from 'amount' to 'energy' to match Forge
    protected long capacity;
    public final long maxReceive, maxExtract;

    public InternalEnergyStorage(long capacity) {
        this(capacity, capacity, capacity, 0);
    }

    public InternalEnergyStorage(long capacity, long maxTransfer) {
        this(capacity, maxTransfer, maxTransfer, 0);
    }

    public InternalEnergyStorage(long capacity, long maxReceive, long maxExtract) {
        this(capacity, maxReceive, maxExtract, 0);
    }

    public InternalEnergyStorage(long capacity, long maxReceive, long maxExtract, long energy) {
        StoragePreconditions.notNegative(capacity);
        StoragePreconditions.notNegative(maxReceive);
        StoragePreconditions.notNegative(maxExtract);

        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.energy = energy;
    }

    @Override
    protected Long createSnapshot() {
        return energy;
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        energy = snapshot;
    }

    public CompoundTag write(CompoundTag nbt) {
        nbt.putLong("energy", energy);
        return nbt;
    }

    public void read(CompoundTag nbt) {
        setEnergy(nbt.getLong("energy"));
    }

    public CompoundTag write(CompoundTag nbt, String name) {
        nbt.putLong("energy_"+name, energy);
        return nbt;
    }

    public void read(CompoundTag nbt, String name) {
        setEnergy(nbt.getLong("energy_"+name));
    }

    public long getSpace() {
        return Math.max(getMaxEnergyStored() - getEnergyStored(), 0);
    }

    // Forge-style methods
    public boolean canExtract() {
        return maxExtract > 0;
    }

    public boolean canReceive() {
        return maxReceive > 0;
    }

    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive()) return 0;

        long received = Math.min(this.maxReceive, Math.min(maxReceive, capacity - energy));

        if (!simulate && received > 0) {
            energy += received;
        }

        return (int) received;
    }

    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract()) return 0;

        long extracted = Math.min(this.maxExtract, Math.min(maxExtract, energy));

        if (!simulate && extracted > 0) {
            energy -= extracted;
        }

        return (int) extracted;
    }

    public int getEnergyStored() {
        return (int) energy;
    }

    public int getMaxEnergyStored() {
        return (int) capacity;
    }

    // Fabric API methods (kept for compatibility)
    @Override
    public boolean supportsExtraction() {
        return maxExtract > 0;
    }

    @Override
    public boolean supportsInsertion() {
        return maxReceive > 0;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        long inserted = Math.min(maxReceive, Math.min(maxAmount, capacity - energy));

        if (inserted > 0) {
            updateSnapshots(transaction);
            energy += inserted;
            return inserted;
        }

        return 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        long extracted = Math.min(maxExtract, Math.min(maxAmount, energy));

        if (extracted > 0) {
            updateSnapshots(transaction);
            energy -= extracted;
            return extracted;
        }

        return 0;
    }

    public long simulateExtract(long maxAmount) {
        try (Transaction t = TransferUtil.getTransaction()) {
            StoragePreconditions.notNegative(maxAmount);

            long extracted = Math.min(maxExtract, Math.min(maxAmount, energy));

            if (extracted > 0) {
                updateSnapshots(t);
                energy -= extracted;
                return extracted;
            }

            return 0;
        }
    }

    public long internalConsumeEnergy(long consume) {
        long oenergy = energy;
        energy = Math.max(0, energy - consume);
        return oenergy - energy;
    }

    public long internalProduceEnergy(long produce) {
        long oenergy = energy;
        energy = Math.min(capacity, energy + produce);
        return energy - oenergy;
    }

    public void setEnergy(long energy) {
        this.energy = energy;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    @Override
    public long getAmount() {
        return energy;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Deprecated
    public void outputToSide(Level world, BlockPos pos, Direction side, int max) {
        EnergyStorage ies = EnergyStorage.SIDED.find(world, pos.relative(side), side.getOpposite());
        if(ies == null) return;
        try(Transaction t = Transaction.openOuter()) {
            long ext = this.extract(max, t);
            this.insert(ext - ies.insert(ext, t), t);
            t.commit();
        }
    }

    @Override
    public String toString() {
        return getEnergyStored() + "/" + getMaxEnergyStored() + " <-" + maxExtract + " ->" + maxReceive;
    }
}