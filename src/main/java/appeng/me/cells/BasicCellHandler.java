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

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.IncludeExclude;
import appeng.api.storage.cells.StorageCell;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.util.Platform;

/**
 * Cell handler that manages all normal storage cells (items, fluids).
 */
public class BasicCellHandler implements ItemApiLookup.ItemApiProvider<StorageCell, ContainerItemContext> {

    public static final BasicCellHandler INSTANCE = new BasicCellHandler();

    public void addCellInformationToTooltip(ItemStack is, List<Component> lines) {
        var handler = BasicCellInventory.createInventory(is, null);
        if (handler == null) {
            return;
        }

        lines.add(Tooltips.bytesUsed(handler.getUsedBytes(), handler.getTotalBytes()));
        lines.add(Tooltips.typesUsed(handler.getStoredItemTypes(), handler.getTotalItemTypes()));

        if (handler.isPreformatted()) {
            var list = (handler.getPartitionListMode() == IncludeExclude.WHITELIST ? GuiText.Included
                    : GuiText.Excluded)
                            .text();

            if (handler.isFuzzy()) {
                lines.add(GuiText.Partitioned.withSuffix(" - ").append(list).append(" ").append(GuiText.Fuzzy.text()));
            } else {
                lines.add(
                        GuiText.Partitioned.withSuffix(" - ").append(list).append(" ").append(GuiText.Precise.text()));
            }
        }
    }

    @Override
    public @Nullable StorageCell find(ItemStack itemStack, ContainerItemContext context) {
        return BasicCellInventory.createInventory(itemStack, () -> {
            try (var tx = Platform.openOrJoinTx()) {
                context.exchange(ItemVariant.of(itemStack), context.getAmount(), tx);
                tx.commit();
            }
        });
    }
}
