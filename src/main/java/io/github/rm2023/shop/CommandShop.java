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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import io.github.rm2023.util.Util;

public class CommandShop extends Shop {
    protected String command;
    
    public CommandShop()
    {
	super();
	this.command = "";
    }
    
    public CommandShop(String name, Location<World> location, double min, double max, double k, String command)
    {
	super(name, location, min, max, k, true, false);
	this.command = command;
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
	if(!Util.withdraw(playerAccount, economy, price, getName()))
	{
	    Util.message(p, "Error while withdrawing funds. Please contact an admin.");
	    return false;
	}
	logger.debug(Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command.replaceAll("@p", p.getName())).toString());
	offset += 1;
	return true;
    }

    @Override
    protected boolean sellOperation(Player p) {
	return false;
    }

}
