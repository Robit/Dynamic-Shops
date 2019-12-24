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

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import io.github.rm2023.dynamicshops.DynamicShops;
import io.github.rm2023.dynamicshops.util.CreateShopData;
import io.github.rm2023.dynamicshops.util.Util;

public class ShopCreate {
    public static ArrayList<CreateShopData> createList = new ArrayList<CreateShopData>();

    @Listener(order = Order.EARLY)
    public void ShopCreateEvent(InteractBlockEvent.Secondary.MainHand event, @First Player player) {
        for (CreateShopData data : createList) {
            if (data.player.equals(player)) {
                createList.remove(data);
                Location<World> l = event.getTargetBlock().getLocation().get();
                BlockState b = l.getBlock();
                if (b.getType() != BlockTypes.STANDING_SIGN && b.getType() != BlockTypes.WALL_SIGN) {
                    Util.message(player, "Invalid block type! Operation cancelled.", true);
                    return;
                }
                if (DynamicShops.data.getShop(l) != null) {
                    Util.message(player, "Location is already a shop! Operation cancelled.", true);
                    return;
                }
                data.shop.setLocation(l);
                DynamicShops.data.addShop(data.shop);
                data.shop.setSign();
                DynamicShops.logger.info("Shop " + data.shop.getName() + " added by " + player.getName());
                Util.message(player, "Shop added successfully! To delete it, break the sign.", false);
                event.setCancelled(true);
                return;
            }
        }
    }

    public static class RemoveDataTask implements Consumer<Task> {
        private CreateShopData data;

        public RemoveDataTask(CreateShopData data) {
            this.data = data;
        }

        @Override
        public void accept(Task t) {
            createList.remove(data);
        }
    }
}
