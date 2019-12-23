/*  Dynamic Shops: A plugin for the Sponge API
 *   Copyright (C) 2019 rm2023
 *
 *  This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.rm2023.dynamicshops.data;

import java.util.LinkedList;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import io.github.rm2023.dynamicshops.shop.Shop;

public class ShopData {
    LinkedList<Shop> shopList;

    public ShopData() {
        shopList = new LinkedList<Shop>();
        // TODO - Add stuff to load shops from a database
    }

    public boolean addShop(Shop shop) {
        if (shopList.add(shop)) {
            save(true);
            return true;
        }
        return false;
    }

    public boolean removeShop(Shop shop) {
        if (shopList.remove(shop)) {
            save(true);
            return true;
        }
        return false;
    }

    public boolean removeShop(Location<World> location) {
        Shop toRemove = getShop(location);
        if (toRemove != null) {
            return removeShop(toRemove);
        }
        return true;
    }

    public Shop getShop(Location<World> location) {
        if (location == null) {
            return null;
        }
        for (Shop shop : shopList) {
            if (shop.getLocation() != null && shop.getLocation().equals(location)) {
                return shop;
            }
        }
        return null;
    }

    public void save(boolean force) {
        // TODO: Something to save to a database
    }
}
