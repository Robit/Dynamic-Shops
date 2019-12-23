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

import org.slf4j.Logger;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.inject.Inject;

public abstract class Shop {
    static protected int ROUNDING = 2;
    @Inject
    protected Logger logger;
    
    protected String name;
    protected World world;
    protected Location<World> location;
    protected double offset;
    protected double min;
    protected double max;
    protected double k;
    protected boolean canBuy;
    protected boolean canSell;
    
    public String getName()
    {
	return new String(name);
    }
    
    public Location<World> getLocation()
    {
	return location;
    }
    
    public BigDecimal getPrice()
    {
	return BigDecimal.valueOf(min + ((min + max) / (1 + Math.pow(Math.E, -1 * k * (offset / (0.5 + max + min))))));
    }
    
    public double getPriceAsDouble()
    {
	return min + ((min + max) / (1 + Math.pow(Math.E, -1 * k * (offset / (0.5 + max + min)))));
    }
    
    public boolean setPrice(double price)
    {
	if(price < min || price > max)
	{
	    return false;
	}
	//If the price is with rounding range of min or max, set the price to be rounding range away from the boundary
	price = Math.max(price, (1 + Math.pow(10, ROUNDING)) * min);
	price = Math.min(price, (1 - Math.pow(10, ROUNDING)) * max);
	//Maaaaaaath
	price = (price + min) / (max - min);
	offset = Math.log(price / (1 - price)) / k;
	return true;
    }
    
    public boolean buy(Player p)
    {
	if(canBuy && p.hasPermission("dynamiceconomy.buy." + getName()))
	{
	   if(buyOperation(p))
	   {
	       logger.info(p.getName() + " bought from the shop the shop " + getName());
	       return true;
	   }
	   else
	   {
	       logger.debug(p.getName() + " attempted to buy from the shop " + getName() + " but failed due to a buy operation error");
	       return false;
	   }
	}
	logger.debug(p.getName() + " attempted to buy from the shop " + getName() + " but failed either because they did not have permission or because the shop cannot be bought from.");
	return false;
    }
    
    public boolean sell(Player p)
    {
	if(canSell && p.hasPermission("dynamiceconomy.sell." + getName()))
	{
	   if(sellOperation(p))
	   {
	       logger.info(p.getName() + " sold to the shop " + getName());
	       updateSign();
	       return true;
	   }
	   else
	   {
	       logger.debug(p.getName() + " attempted to sell to the shop " + getName() + " but failed due to a sell operation error");
	       return false;
	   }
	}
	logger.debug(p.getName() + " attempted to sell to the shop " + getName() + " but failed either because they did not have permission or because the shop cannot be sold to.");
	return false;
    }
    
    public void updateSign()
    {
	
    }
    
    protected abstract boolean buyOperation(Player p);
    protected abstract boolean sellOperation(Player p);
}
