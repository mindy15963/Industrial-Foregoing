package com.buuz135.industrial.tile.mob;

import com.buuz135.industrial.tile.CustomColoredItemHandler;
import com.buuz135.industrial.tile.WorkingAreaElectricMachine;
import com.buuz135.industrial.utils.WorkUtils;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class AnimalStockIncreaserTile extends WorkingAreaElectricMachine {

    public ItemStackHandler inFeedItems;

    public AnimalStockIncreaserTile() {
        super(AnimalStockIncreaserTile.class.getName().hashCode());
    }

    @Override
    protected void initializeInventories() {
        super.initializeInventories();
        inFeedItems = new ItemStackHandler(3 * 6) {
            @Override
            protected void onContentsChanged(int slot) {
                AnimalStockIncreaserTile.this.markDirty();
            }
        };
        this.addInventory(new CustomColoredItemHandler(inFeedItems, EnumDyeColor.GREEN, "Food items", 18 * 3, 25, 6, 3) {
            @Override
            public boolean canInsertItem(int slot, ItemStack stack) {
                return true;
            }

            @Override
            public boolean canExtractItem(int slot) {
                return false;
            }

        });
        this.addInventoryToStorage(inFeedItems, "animal_stock_in");
    }

    @Override
    public float work() {
        if (WorkUtils.isDisabled(this.getBlockType())) return 0;

        AxisAlignedBB area = getWorkingArea();
        List<EntityAnimal> animals = this.world.getEntitiesWithinAABB(EntityAnimal.class, area);
        if (animals.size() == 0 || animals.size() > 35) return 0;
        //Removing from the list animals that can't breed
        animals.removeIf(entityAnimal -> entityAnimal.isChild() || entityAnimal.getGrowingAge() != 0 || getFirstBreedingItem(entityAnimal).isEmpty() || entityAnimal.isInLove());
        for (EntityAnimal firstParent : animals) {
            for (EntityAnimal secondParent : animals) {
                if (!firstParent.equals(secondParent) && firstParent.getClass().equals(secondParent.getClass())) {
                    ItemStack stack = getFirstBreedingItem(firstParent);
                    Item item = stack.getItem();
                    stack.setCount(stack.getCount() - 1);
                    stack = getFirstBreedingItem(secondParent);
                    if (stack.isEmpty()) {
                        ItemHandlerHelper.insertItem(inFeedItems, new ItemStack(item, 1), false);
                        return 0;
                    }
                    stack.setCount(stack.getCount() - 1);
                    firstParent.setInLove(null);
                    secondParent.setInLove(null);
                    return 1;
                }
            }
        }
//        EntityAnimal animal1 = animals.get(0);
//        while ((animal1.isChild() || animal1.getGrowingAge() != 0 || getFirstBreedingItem(animal1).isEmpty() || animal1.isInLove()) && animals.indexOf(animal1) + 1 < animals.size())
//            animal1 = animals.get(animals.indexOf(animal1) + 1);
//        if (animal1.isChild() || animal1.getGrowingAge() != 0) return 0;
//        EntityAnimal animal2 = animals.get(0);
//        while ((animal2.equals(animal1) || animal2.isChild() || animal2.getGrowingAge() != 0 || getFirstBreedingItem(animal2).isEmpty() || animal1.isInLove()) && animals.indexOf(animal2) + 1 < animals.size())
//            animal2 = animals.get(animals.indexOf(animal2) + 1);
//        if (animal2.equals(animal1) || animal2.isChild() || animal2.getGrowingAge() != 0) return 0;
//        if (animal1.getClass() != animal2.getClass()) return 0;
//        ItemStack stack = getFirstBreedingItem(animal1);
//        Item item = stack.getItem();
//        stack.setCount(stack.getCount() - 1);
//        stack = getFirstBreedingItem(animal2);
//        if (stack.isEmpty()) {
//            ItemHandlerHelper.insertItem(inFeedItems, new ItemStack(item, 1), false);
//            return 0;
//        }
//        stack.setCount(stack.getCount() - 1);
//        animal1.setInLove(null);
//        animal2.setInLove(null);

        return 0;
    }

    public ItemStack getFirstBreedingItem(EntityAnimal animal) {
        for (int i = 0; i < inFeedItems.getSlots(); ++i) {
            if (animal.isBreedingItem(inFeedItems.getStackInSlot(i))) return inFeedItems.getStackInSlot(i);
        }
        return ItemStack.EMPTY;
    }


}
