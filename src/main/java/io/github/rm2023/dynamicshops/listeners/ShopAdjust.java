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

import java.util.ArrayList;
import java.util.function.Consumer;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import io.github.rm2023.dynamicshops.DynamicShops;
import io.github.rm2023.dynamicshops.shop.Shop;
import io.github.rm2023.dynamicshops.util.AdjustPriceData;
import io.github.rm2023.dynamicshops.util.Util;

public class ShopAdjust {
    public static ArrayList<AdjustPriceData> adjustList = new ArrayList<AdjustPriceData>();

    @Listener(order = Order.EARLY)
    public void ShopAdjustEvent(InteractBlockEvent.Secondary.MainHand event, @First Player player) {
        for (AdjustPriceData data : adjustList) {
            if (data.player.equals(player)) {
                adjustList.remove(data);
                Location<World> l = event.getTargetBlock().getLocation().get();
                Shop shop = DynamicShops.data.getShop(l);
                if (shop != null) {
                    event.setCancelled(true);
                    if (data.price < 0) {
                        shop.setOffset(shop.getInitial());
                        shop.updateSign();
                        DynamicShops.logger.info("Shop " + shop.getName() + " was reset by " + player.getName());
                        Util.message(player, "Price reset successfully.", true);
                        return;
                    }
                    if (shop.setPrice(data.price)) {
                        Util.message(player, "Price changed successfully.", true);
                        DynamicShops.logger.info("Shop " + shop.getName() + "'s price was changed to " + data.price + " by " + player.getName());
                        shop.updateSign();
                        return;
                    } else {
                        Util.message(player, "Error setting price. Price must be between the minimum and maximum prices of the shop. Operation cancelled.", false);
                        return;
                    }
                } else {
                    Util.message(player, "Invalid Block! Operation cancelled.", false);
                    return;
                }
            }
        }
    }

    public static class RemoveDataTask implements Consumer<Task> {
        private AdjustPriceData data;

        public RemoveDataTask(AdjustPriceData data) {
            this.data = data;
        }

        @Override
        public void accept(Task t) {
            adjustList.remove(data);
        }
    }
}
