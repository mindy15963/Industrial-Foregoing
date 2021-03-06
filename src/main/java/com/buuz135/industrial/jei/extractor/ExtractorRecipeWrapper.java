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
package com.buuz135.industrial.jei.extractor;

import com.buuz135.industrial.api.extractor.ExtractorEntry;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

public class ExtractorRecipeWrapper implements IRecipeWrapper {

    public final ExtractorEntry extractorEntry;

    public ExtractorRecipeWrapper(ExtractorEntry extractorEntry) {
        this.extractorEntry = extractorEntry;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(ItemStack.class, extractorEntry.getItemStack());
        ingredients.setOutput(FluidStack.class, extractorEntry.getFluidStack());
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        minecraft.fontRenderer.drawString(TextFormatting.DARK_GRAY + "Production: ", 80, 6, 0xFFFFFF);
        minecraft.fontRenderer.drawString(TextFormatting.DARK_GRAY + "" + extractorEntry.getFluidStack().amount + "mb/5ticks", 80, 6 + (minecraft.fontRenderer.FONT_HEIGHT + 2) * 1, 0xFFFFFF);
        minecraft.fontRenderer.drawString(TextFormatting.DARK_GRAY + "" + "Average: ", 80, 6 + (minecraft.fontRenderer.FONT_HEIGHT + 2) * 2, 0xFFFFFF);
        minecraft.fontRenderer.drawString(TextFormatting.DARK_GRAY + "" + ((int) (8 / extractorEntry.getBreakChance()) * extractorEntry.getFluidStack().amount) + "mb", 80, 6 + (minecraft.fontRenderer.FONT_HEIGHT + 2) * 3, 0xFFFFFF);

    }
}
