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
import io.github.rm2023.dynamicshops.shop.CommandShop;
import io.github.rm2023.dynamicshops.shop.Shop;
import io.github.rm2023.dynamicshops.util.AdjustPriceData;
import io.github.rm2023.dynamicshops.util.CreateShopData;
import io.github.rm2023.dynamicshops.util.Util;

public class CreateCommandShopCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            Util.message(src, "This command must be executed by a player.");
            return CommandResult.empty();
        }
        String name = args.<String>getOne("shopName").get();
        double initial = args.<Double>getOne("initialPrice").get();
        if (initial < 0) {
            Util.message(src, "The initial price must be greater than 0!");
            return CommandResult.empty();
        }
        double max = args.<Double>getOne("maxPrice").get();
        if (max < initial) {
            Util.message(src, "The maximum price must be greater than the initial price!");
            return CommandResult.empty();
        }
        double k = args.<Double>getOne("priceChangeRate").get();
        if (k < 0) {
            Util.message(src, "The price change rate must be greater than 0!");
            return CommandResult.empty();
        }
        String command = args.<String>getOne("command").get();
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
        String prefix = DynamicShops.economy.getDefaultCurrency().getSymbol().toPlain();
        Shop shop = new CommandShop(name, null, initial, max, k, command);
        Util.message(p, "You are creating a shop named " + name + " which will initially sell the command " + command + " for " + prefix + initial + " with a maximum price of " + prefix + max + ".");
        Util.message(src, "Right click the sign that you want to set as a shop. To cancel, right click any other block. The operation will automatically cancel in 30 seconds.");
        CreateShopData data = new CreateShopData(p, shop);
        ShopCreate.createList.add(data);
        Task task = Task.builder().execute(new ShopCreate.RemoveDataTask(data)).delay(30, TimeUnit.SECONDS).name("ShopCreation Cancel Task").submit(DynamicShops.container);
        return CommandResult.success();
    }

}