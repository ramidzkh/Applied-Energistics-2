/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.storage.cells;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;

import appeng.api.storage.MEStorage;
import appeng.core.AppEng;

/**
 * Represents the most general possible cell inventory. Register a {@link StorageCell#ITEM} to provide custom
 * subclasses.
 */
public interface StorageCell extends MEStorage {

    ItemApiLookup<StorageCell, ContainerItemContext> ITEM = ItemApiLookup.get(
            AppEng.makeId("storage_cell"), StorageCell.class, ContainerItemContext.class);

    /**
     * Return the current status of the cell.
     */
    CellState getStatus();

    /**
     * Return the idle drain of the cell: how many AE/t it uses passively.
     */
    double getIdleDrain();

    /**
     * Tells the cell to persist to NBT.
     */
    void persist();
}
