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

import java.util.Optional;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import io.github.rm2023.dynamicshops.DynamicShops;
import io.github.rm2023.dynamicshops.shop.Shop;
import io.github.rm2023.dynamicshops.util.Util;

public class ShopBreak {
    @Listener(order = Order.FIRST)
    public void ShopBreakEvent(ChangeBlockEvent event) {
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (transaction.isValid()) {
                Shop shop = DynamicShops.data.getShop(transaction.getOriginal().getLocation().orElse(null));
                if (shop != null) {
                    Optional<Player> p = event.getCause().first(Player.class);
                    if (p.isPresent() && p.get().hasPermission("dynamicshops.admin")) {
                        DynamicShops.data.removeShop(shop);
                        Util.message(p.get(), "The shop " + shop.getName() + " was deleted.", false);
                        DynamicShops.logger.info("Removed shop " + shop.getName() + " due to a block break event caused by " + p.get().getName());
                    } else {
                        DynamicShops.logger.debug("Prevented ChangeBlockEvent at location of " + shop.getName());
                        shop.setSign();
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
