/*
 * This file is part of Hot or Not.
 *
 * Copyright 2018, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.buuz135.industrial.tile.misc;

import com.buuz135.industrial.tile.CustomSidedTileEntity;
import com.buuz135.industrial.utils.WorkUtils;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.ndrei.teslacorelib.gui.BasicTeslaGuiContainer;
import net.ndrei.teslacorelib.gui.IGuiContainerPiece;
import net.ndrei.teslacorelib.gui.LockedInventoryTogglePiece;
import net.ndrei.teslacorelib.inventory.BoundingRectangle;
import net.ndrei.teslacorelib.inventory.ColoredItemHandler;
import net.ndrei.teslacorelib.inventory.LockableItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

import static com.buuz135.industrial.proxy.BlockRegistry.blackHoleUnitBlock;

public class BlackHoleControllerTile extends CustomSidedTileEntity {

    private LockableItemHandler input;
    private ItemStackHandler storage;
    private LockableItemHandler output;
    private BlackHoleControllerHandler itemHandler = new BlackHoleControllerHandler(this);

    public BlackHoleControllerTile() {
        super(BlackHoleControllerTile.class.getName().hashCode());
    }

    @Override
    protected void initializeInventories() {
        super.initializeInventories();
        input = new LockableItemHandler(9) {
            @Override
            protected void onContentsChanged(int slot) {
                if (slot < 0) return;
                ItemStack in = input.getStackInSlot(slot);
                int amount = blackHoleUnitBlock.getAmount(storage.getStackInSlot(slot));
                if (!in.isEmpty() && in.getCount() + amount < Integer.MAX_VALUE) {
                    blackHoleUnitBlock.setItemStack(storage.getStackInSlot(slot), in);
                    blackHoleUnitBlock.setAmount(storage.getStackInSlot(slot), amount + in.getCount());
                    in.setCount(0);
                }
                BlackHoleControllerTile.this.markDirty();
            }
        };
        this.addInventory(new ColoredItemHandler(input, EnumDyeColor.BLUE, "Input items", new BoundingRectangle(15, 18, 9 * 18, 18)) {
            @Override
            public boolean canInsertItem(int slot, ItemStack stack) {
                if (storage.getStackInSlot(slot).isEmpty() || stack.getItem().equals(Item.getItemFromBlock(blackHoleUnitBlock)))
                    return false;
                if (input.getLocked() && input.getFilterStack(slot).isItemEqual(stack)) return true;
                if (!output.getStackInSlot(slot).isEmpty() && !output.getStackInSlot(slot).isItemEqual(stack))
                    return false;
                ItemStack contained = blackHoleUnitBlock.getItemStack(storage.getStackInSlot(slot));
                if (stack.isItemEqual(contained)) return true;
                if (!input.getLocked() && contained.isEmpty()) return true;
                return false;
            }

            @Override
            public boolean canExtractItem(int slot) {
                return super.canExtractItem(slot);
            }
        });
        this.addInventoryToStorage(input, "input");
        storage = new ItemStackHandler(9) {
            @Override
            protected void onContentsChanged(int slot) {
                BlackHoleControllerTile.this.markDirty();
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        this.addInventory(new ColoredItemHandler(storage, EnumDyeColor.YELLOW, "Black hole units", new BoundingRectangle(15, 22 + 18, 9 * 18, 18)) {
            @Override
            public boolean canInsertItem(int slot, ItemStack stack) {
                return stack.getItem().equals(Item.getItemFromBlock(blackHoleUnitBlock));
            }

            @Override
            public boolean canExtractItem(int slot) {
                return super.canExtractItem(slot);
            }
        });
        this.addInventoryToStorage(storage, "storage");
        output = new LockableItemHandler(9) {
            @Override
            protected void onContentsChanged(int slot) {
                BlackHoleControllerTile.this.markDirty();
            }
        };
        this.addInventory(new ColoredItemHandler(output, EnumDyeColor.ORANGE, "Output items", new BoundingRectangle(15, 27 + 18 * 2, 9 * 18, 18)) {
            @Override
            public boolean canInsertItem(int slot, ItemStack stack) {
                return false;
            }

            @Override
            public boolean canExtractItem(int slot) {
                return true;
            }
        });
        this.addInventoryToStorage(output, "output");

    }

    @Override
    protected boolean supportsAddons() {
        return false;
    }

    @Override
    protected void innerUpdate() {
        if (WorkUtils.isDisabled(this.getBlockType())) return;
        input.setLocked(output.getLocked());
        input.setFilter(output.getFilter());
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = storage.getStackInSlot(i);
            if (!stack.isEmpty()) {
                int amount = blackHoleUnitBlock.getAmount(stack);
                ItemStack s = blackHoleUnitBlock.getItemStack(stack);
                if (!s.isEmpty()) {
                    ItemStack out = output.getStackInSlot(i);
                    if (out.isEmpty()) { // Slot is empty
                        out = s.copy();
                        out.setCount(Math.min(amount, s.getMaxStackSize()));
                        blackHoleUnitBlock.setAmount(stack, amount - out.getCount());
                        output.setStackInSlot(i, out);
                        if (blackHoleUnitBlock.getAmount(stack) <= 0 && !output.getLocked()) {
                            stack.setTagCompound(null);
                        }
                        continue;
                    }
                    if (out.getCount() < out.getMaxStackSize()) {
                        int increase = Math.min(amount, out.getMaxStackSize() - out.getCount());
                        out.setCount(out.getCount() + increase);
                        blackHoleUnitBlock.setAmount(stack, amount - increase);
                        if (blackHoleUnitBlock.getAmount(stack) <= 0 && !output.getLocked()) {
                            stack.setTagCompound(null);
                        }
                        continue;
                    }
                }
            } else if (!output.getStackInSlot(i).isEmpty() && !this.world.isRemote) {
                float f = 0.7F;
                float d0 = world.rand.nextFloat() * f + (1.0F - f) * 0.5F;
                float d1 = world.rand.nextFloat() * f + (1.0F - f) * 0.5F;
                float d2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5F;
                EntityItem entityitem = new EntityItem(world, pos.getX() + d0, pos.getY() + d1, pos.getZ() + d2, output.getStackInSlot(i).copy());
                output.setStackInSlot(i, ItemStack.EMPTY);
                world.spawnEntity(entityitem);
            }
        }
    }

    public ItemStackHandler getInput() {
        return input;
    }

    public ItemStackHandler getStorage() {
        return storage;
    }

    public ItemStackHandler getOutput() {
        return output;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == null) return false;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return (T) itemHandler;
        return super.getCapability(capability, facing);
    }

    public void dropItems() {
        for (ItemStackHandler items : new ItemStackHandler[]{input, storage, output}) {
            for (int i = 0; i < items.getSlots(); ++i) {
                ItemStack stack = items.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    InventoryHelper.spawnItemStack(this.getWorld(), pos.getX(), pos.getY(), pos.getZ(), stack);
                }
            }
        }
    }

    @Override
    public List<IGuiContainerPiece> getGuiContainerPieces(BasicTeslaGuiContainer<?> container) {
        List<IGuiContainerPiece> pieces = super.getGuiContainerPieces(container);
        pieces.add(new LockedInventoryTogglePiece(18 * 8 + 9, 83, this, EnumDyeColor.ORANGE));
        return pieces;
    }

    @Override
    public boolean getAllowRedstoneControl() {
        return false;
    }

    @Override
    protected boolean getShowPauseDrawerPiece() {
        return false;
    }

    private class BlackHoleControllerHandler implements IItemHandler {

        private BlackHoleControllerTile tile;

        public BlackHoleControllerHandler(BlackHoleControllerTile tile) {
            this.tile = tile;
        }

        @Override
        public int getSlots() {
            return 9;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (!storage.getStackInSlot(slot).isEmpty()) {
                ItemStack stack = blackHoleUnitBlock.getItemStack(storage.getStackInSlot(slot)).copy();
                stack.setCount(blackHoleUnitBlock.getAmount(storage.getStackInSlot(slot)) + output.getStackInSlot(slot).getCount());
                return stack;
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            for (int i = 0; i < 9; ++i) {
                ItemStack contained = blackHoleUnitBlock.getItemStack(storage.getStackInSlot(i));
                if (stack.isItemEqual(contained)) {
                    return tile.getInput().insertItem(i, stack, simulate);
                }
            }
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (storage.getStackInSlot(slot).isEmpty() || amount == 0) return ItemStack.EMPTY;
            ItemStack existing = blackHoleUnitBlock.getItemStack(storage.getStackInSlot(slot)).copy();
            if (existing.isEmpty()) return ItemStack.EMPTY;
            int visualAmount = blackHoleUnitBlock.getAmount(storage.getStackInSlot(slot)) + output.getStackInSlot(slot).getCount();
            if (visualAmount <= amount) {
                if (!simulate) {
                    blackHoleUnitBlock.setAmount(storage.getStackInSlot(slot), 0);
                    output.setStackInSlot(slot, ItemStack.EMPTY);
                }
                return ItemHandlerHelper.copyStackWithSize(existing, visualAmount);
            } else {
                if (!simulate) {
                    blackHoleUnitBlock.setAmount(storage.getStackInSlot(slot), Math.max(0, blackHoleUnitBlock.getAmount(storage.getStackInSlot(slot)) - amount));
                    output.setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(existing, Math.min(64, visualAmount - amount)));
                }
                return ItemHandlerHelper.copyStackWithSize(existing, amount);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }
    }

}
