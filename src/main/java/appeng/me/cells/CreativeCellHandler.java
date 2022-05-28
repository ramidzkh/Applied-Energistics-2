/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.me.cells;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.world.item.ItemStack;

import appeng.api.storage.cells.StorageCell;
import appeng.items.storage.CreativeCellItem;

/**
 * Cell handler for creative storage cells (both fluid and item), which do not allow item insertion.
 */
public class CreativeCellHandler implements ItemApiLookup.ItemApiProvider<StorageCell, ContainerItemContext> {
    public static final CreativeCellHandler INSTANCE = new CreativeCellHandler();

    @Override
    public @Nullable StorageCell find(ItemStack itemStack, ContainerItemContext context) {
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof CreativeCellItem) {
            return new CreativeCellInventory(itemStack);
        }

        return null;
    }
}
