/*  DynamicEconomy: A plugin for the Sponge API
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

package io.github.rm2023.shop;

import java.math.BigDecimal;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.service.economy.account.Account;

import io.github.rm2023.util.Util;

public class ItemShop extends Shop {

    protected ItemStack items;
    
    public ItemShop()
    {
	super();
	this.items = null;
    }
    
    @Override
    protected boolean buyOperation(Player p) {
	Account playerAccount = economy.getOrCreateAccount(p.getUniqueId()).orElse(null);
	BigDecimal price = BigDecimal.valueOf(getPrice());
	if(playerAccount == null || playerAccount.getBalance(economy.getDefaultCurrency()).compareTo(price) > 0)
	{
	    Util.message(p, "You don't have enough money to purchase this!");
	    return false;
	}
	Inventory i = p.getInventory();
	if(!i.canFit(items))
	{
	    Util.message(p, "Clear some space in your inventory first!");
	    return false;
	}
	if(!Util.withdraw(playerAccount, economy, price, getName()))
	{
	    Util.message(p, "Error while withdrawing funds. Please contact an admin.");
	    return false;
	}
	i.offer(items);
	return true;
    }

    @Override
    protected boolean sellOperation(Player p) {
	Account playerAccount = economy.getOrCreateAccount(p.getUniqueId()).orElse(null);
	BigDecimal price = BigDecimal.valueOf(getPrice());
	if(playerAccount == null)
	{
	    Util.message(p, "You don't have a money account! Contact an admin for more information.");
	    return false;
	}
	Inventory i = p.getInventory();
	if(i.contains(items))
	{
	    if(i.containsAny(items))
	    {
		Util.message(p, "You don't have any items to sell to this shop!");
	    }
	    else
	    {
		Util.message(p, "You need more items to sell to this shop!");
	    }
	    return false;
	}
	if(!i.query(QueryOperationTypes.ITEM_STACK_IGNORE_QUANTITY.of(items)).poll(items.getQuantity()).isPresent())
	{
	    Util.message(p, "Error while retrieving items. Please contact an admin if this error persists.");
	    return false;
	}
	if(!Util.deposit(playerAccount, economy, price, getName()))
	{
	    Util.message(p, "Error while depositing " + getPrice() + " into account. Please screenshot this and contact an admin for compensation. Admin, there should be an ERROR in the server log corrosponding to this failure. Please verify this, compensate the player, and contact the dev for support.");
	    logger.error("Attempting to put " + getPrice() + " into " + p.getName() + "'s account failed! Please verify that your economy plugin is working and contact the dev!");
	    return false;
	}
	return true;
    }
}
