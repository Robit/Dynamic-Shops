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

package io.github.rm2023.dynamicshops.shop;

import java.math.BigDecimal;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import io.github.rm2023.dynamicshops.DynamicShops;
import io.github.rm2023.dynamicshops.util.Util;

public class ItemShop extends Shop {

    protected ItemStack items;

    public ItemShop() {
        super();
        this.items = null;
    }

    public ItemShop(String name, Location<World> location, double min, double max, double k, boolean canBuy, boolean canSell, ItemStack items) {
        super(name, location, min, max, k, canBuy, canSell);
        this.items = items.copy();
    }

    @Override
    protected boolean buyOperation(Player p) {
        Account playerAccount = DynamicShops.economy.getOrCreateAccount(p.getUniqueId()).orElse(null);
        BigDecimal price = BigDecimal.valueOf(getPrice());
        if (playerAccount == null || playerAccount.getBalance(DynamicShops.economy.getDefaultCurrency()).compareTo(price) < 0) {
            Util.message(p, "You don't have enough money to purchase this!", true);
            return false;
        }
        Inventory i = p.getInventory();
        if (!i.canFit(items)) {
            Util.message(p, "Clear some space in your inventory first!", true);
            return false;
        }
        if (!Util.withdraw(playerAccount, DynamicShops.economy, price, getName())) {
            Util.message(p, "Error while withdrawing funds. Please contact an admin.", true);
            return false;
        }
        i.offer(items.copy());
        offset += 1;
        return true;
    }

    @Override
    protected boolean sellOperation(Player p) {
        Account playerAccount = DynamicShops.economy.getOrCreateAccount(p.getUniqueId()).orElse(null);
        BigDecimal price = BigDecimal.valueOf(getPrice());
        if (playerAccount == null) {
            Util.message(p, "You don't have a money account! Contact an admin for more information.", true);
            return false;
        }
        Inventory i = p.getInventory();
        if (!i.contains(items)) {
            if (!i.containsAny(items)) {
                Util.message(p, "You don't have any items to sell to this shop!", true);
            } else {
                Util.message(p, "You need more items to sell to this shop!", true);
            }
            return false;
        }
        if (!i.query(QueryOperationTypes.ITEM_STACK_IGNORE_QUANTITY.of(items)).poll(items.getQuantity()).isPresent()) {
            Util.message(p, "Error while retrieving items. Please contact an admin if this error persists.", true);
            return false;
        }
        if (!Util.deposit(playerAccount, DynamicShops.economy, price, getName())) {
            Util.message(p, "Error while depositing " + getPrice() + " into account. Please screenshot this and contact an admin for compensation. Admin, there should be an ERROR in the server log corrosponding to this failure. Please verify this, compensate the player, and contact the dev for support.", true);
            DynamicShops.logger.error("Attempting to put " + getPrice() + " into " + p.getName() + "'s account failed! Please verify that your DynamicShops.economy plugin is working and contact the dev!");
            return false;
        }
        offset -= 1;
        return true;
    }

    public ItemStack getItems() {
        return items.copy();
    }
}
