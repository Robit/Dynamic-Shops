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

public class CreateStaticShopCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            Util.message(src, "This command must be executed by a player.");
            return CommandResult.empty();
        }
        String name = args.<String>getOne("shopName").get();
        double price = args.<Double>getOne("price").get();
        if (price < 0) {
            Util.message(src, "The price must be greater than 0!");
            return CommandResult.empty();
        }
        String extraArgument = args.<String>getOne("buyOnly/sellOnly").orElse("");
        if (!(extraArgument.equals("") || extraArgument.equals("buyOnly") || extraArgument.equals("sellOnly"))) {
            Util.message(src, "The optional argument must either be buyOnly or sellOnly!");
            return CommandResult.empty();
        }
        Player p = (Player) src;
        for (CreateShopData data : ShopCreate.createList) {
            if (p.equals(data.player)) {
                Util.message(src, "You’re already in the process of creating a shop! Finish that first!");
                return CommandResult.empty();
            }
        }
        for (AdjustPriceData data : ShopAdjust.adjustList) {
            if (p.equals(data.player)) {
                Util.message(src, "You’re already in the process of adjusting a shop! Finish that first!");
                return CommandResult.empty();
            }
        }
        ItemStack hand = p.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
        if (hand == null || hand.getType().equals(ItemTypes.AIR)) {
            Util.message(src, "You must specify the item you want the shop to use by holding it in your hand.");
            Util.message(src, "Holding multiple items will make the shop deal in multiples of that item.");
            return CommandResult.empty();
        }
        String prefix = DynamicShops.economy.getDefaultCurrency().getSymbol().toPlain();
        Shop shop = new ItemShop(name, null, price, price, 0, !extraArgument.equals("sellOnly"), !extraArgument.equals("buyOnly"), hand);
        Util.message(p, "You are creating a shop named " + name + " which will buy/sell the item stack (" + hand.getQuantity() + " " + hand.getType().getName() + ") in your hand for " + prefix + price + ".");
        if (!shop.getCanBuy()) {
            Util.message(src, "However, this shop will only sell items.");
        }
        if (!shop.getCanSell()) {
            Util.message(src, "However, this shop will only buy items.");
        }
        Util.message(src, "Right click the sign that you want to set as a shop. To cancel, right click any other block. The operation will automatically cancel in 30 seconds.");
        CreateShopData data = new CreateShopData(p, shop);
        ShopCreate.createList.add(data);
        Task task = Task.builder().execute(new ShopCreate.RemoveDataTask(data)).delay(30, TimeUnit.SECONDS).name("ShopCreation Cancel Task").submit(DynamicShops.container);
        return CommandResult.success();
    }

}
