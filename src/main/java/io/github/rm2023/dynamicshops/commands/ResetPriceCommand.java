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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import io.github.rm2023.dynamicshops.DynamicShops;
import io.github.rm2023.dynamicshops.listeners.ShopAdjust;
import io.github.rm2023.dynamicshops.listeners.ShopCreate;
import io.github.rm2023.dynamicshops.util.AdjustPriceData;
import io.github.rm2023.dynamicshops.util.CreateShopData;
import io.github.rm2023.dynamicshops.util.Util;

public class ResetPriceCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            Util.message(src, "This command must be executed by a player.", true);
            return CommandResult.empty();
        }
        Player p = (Player) src;
        for (CreateShopData data : ShopCreate.createList) {
            if (p.equals(data.player)) {
                Util.message(src, "You’re already in the process of creating a shop! Finish that first!", true);
                return CommandResult.empty();
            }
        }
        for (AdjustPriceData data : ShopAdjust.adjustList) {
            if (p.equals(data.player)) {
                Util.message(src, "You’re already in the process of adjusting a shop! Finish that first!", true);
                return CommandResult.empty();
            }
        }
        Util.message(src, "You are now resetting a shop to its default price. Please right click the shop you want to adjust, or right click any other block to cancel. This operation will automatically cancel in 30 seconds.", false);
        AdjustPriceData data = new AdjustPriceData(p, -1);
        ShopAdjust.adjustList.add(data);
        Task task = Task.builder().execute(new ShopAdjust.RemoveDataTask(data)).delay(30, TimeUnit.SECONDS).name("ShopAdjust Cancel Task").submit(DynamicShops.container);
        return CommandResult.success();
    }

}
