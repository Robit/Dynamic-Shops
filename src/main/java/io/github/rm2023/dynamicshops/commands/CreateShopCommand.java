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

package io.github.rm2023.dynamicshops.commands;

import java.util.concurrent.TimeUnit;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;

import io.github.rm2023.dynamicshops.DynamicShops;
import io.github.rm2023.dynamicshops.listeners.ShopAdjust;
import io.github.rm2023.dynamicshops.listeners.ShopCreate;
import io.github.rm2023.dynamicshops.shop.ItemShop;
import io.github.rm2023.dynamicshops.shop.Shop;
import io.github.rm2023.dynamicshops.util.AdjustPriceData;
import io.github.rm2023.dynamicshops.util.CreateShopData;
import io.github.rm2023.dynamicshops.util.Util;

public class CreateShopCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            Util.message(src, "This command must be executed by a player.", true);
            return CommandResult.empty();
        }
        String name = args.<String>getOne("shopName").get();
        double min = args.<Double>getOne("minPrice").get();
        if (min < 0) {
            Util.message(src, "The minimum price must be greater than 0!", true);
            return CommandResult.empty();
        }
        double max = args.<Double>getOne("maxPrice").get();
        if (max < min) {
            Util.message(src, "The maximum price must be greater than the minimum price!", true);
            return CommandResult.empty();
        }
        double k = args.<Double>getOne("priceChangeRate").get();
        if (k < 0)
        {
            Util.message(src, "The price change rate must be greater than 0!", true);
            return CommandResult.empty();
        }

        String extraArgument = args.<String>getOne("buyOnly/sellOnly/initialPrice").orElse("");
        double initialPrice = Double.NaN;
        if(!(extraArgument.equals("") || extraArgument.equals("buyOnly") || extraArgument.equals("sellOnly")))
        {
            try {
                initialPrice = Double.parseDouble(extraArgument);
            } catch (NumberFormatException e) {
                Util.message(src, "The optional argument must either be buyOnly, sellOnly, or a number specifying the initial price of the shop.", true);
                return CommandResult.empty();
            }
            if (initialPrice < min || initialPrice > max) {
                Util.message(src, "The initial price of the shop must be within the min and max price!", true);
                return CommandResult.empty();
            }
        }
        Player p = (Player) src;
        for(CreateShopData data : ShopCreate.createList) {
            if(p.equals(data.player))
            {
                Util.message(src, "You’re already in the process of creating a shop! Finish that first!", true);
                return CommandResult.empty();
            }
        }
        for(AdjustPriceData data : ShopAdjust.adjustList) {
            if(p.equals(data.player))
            {
                Util.message(src, "You’re already in the process of adjusting a shop! Finish that first!", true);
                return CommandResult.empty();
            }
        }
        ItemStack hand = p.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
        if(hand == null || hand.getType().equals(ItemTypes.AIR))
        {
            Util.message(src, "You must specify the item you want the shop to use by holding it in your hand. Holding multiple items will make the shop deal in multiples of that item.", true);
            return CommandResult.empty();
        }
        String prefix = DynamicShops.economy.getDefaultCurrency().getSymbol().toPlain();
        Shop shop = new ItemShop(name, null, min, max, k, !extraArgument.equals("sellOnly"), !extraArgument.equals("buyOnly"), hand.copy());
        if (!Double.isNaN(initialPrice)) {
            shop.setPrice(initialPrice);
        }
        initialPrice = shop.getPrice();
        Util.message(p, "You are creating a shop named " + name + " which will initially buy/sell the item stack (" + hand.getQuantity() + " " + hand.getType().getName() + ") in your hand for " + prefix + initialPrice + " with a minimum price of " + prefix + min + " and a maximum price of " + prefix + max + ".", false);
        if (!shop.getCanBuy()) {
            Util.message(src, "However, this shop will only sell items.", false);
        }
        if (!shop.getCanSell()) {
            Util.message(src, "However, this shop will only buy items.", false);
        }
        Util.message(src, "Right click the sign that you want to set as a shop. To cancel, right click any other block. The operation will automatically cancel in 30 seconds.", false);
        CreateShopData data = new CreateShopData(p, shop);
        ShopCreate.createList.add(data);
        Task task = Task.builder().execute(new ShopCreate.RemoveDataTask(data)).delay(30, TimeUnit.SECONDS).name("ShopCreation Cancel Task").submit(DynamicShops.container);
        return CommandResult.success();
    }

}
