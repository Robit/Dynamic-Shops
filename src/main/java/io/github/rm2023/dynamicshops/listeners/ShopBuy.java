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

package io.github.rm2023.dynamicshops.listeners;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;

import io.github.rm2023.dynamicshops.DynamicShops;
import io.github.rm2023.dynamicshops.shop.Shop;

public class ShopBuy {
    @Listener
    public void ShopBuyEvent(InteractBlockEvent.Primary.MainHand event, @First Player player) {
        Shop shop = DynamicShops.data.getShop(event.getTargetBlock().getLocation().orElse(null));
        if (shop != null) {
            if (player.get(Keys.IS_SNEAKING).orElse(false)) {
                shop.bulkBuy(player);
            } else {
                shop.buy(player);
            }
        }
    }
}
