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

package io.github.rm2023.dynamicshops;

import java.util.ArrayList;
import java.util.Optional;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import com.google.inject.Inject;

import io.github.rm2023.dynamicshops.commands.CreateCommandShopCommand;
import io.github.rm2023.dynamicshops.commands.CreateShopCommand;
import io.github.rm2023.dynamicshops.commands.CreateStaticShopCommand;
import io.github.rm2023.dynamicshops.commands.MainCommand;
import io.github.rm2023.dynamicshops.commands.ResetPriceCommand;
import io.github.rm2023.dynamicshops.commands.SetPriceCommand;
import io.github.rm2023.dynamicshops.data.ShopData;
import io.github.rm2023.dynamicshops.listeners.ShopAdjust;
import io.github.rm2023.dynamicshops.listeners.ShopBreak;
import io.github.rm2023.dynamicshops.listeners.ShopBuy;
import io.github.rm2023.dynamicshops.listeners.ShopChange;
import io.github.rm2023.dynamicshops.listeners.ShopCreate;
import io.github.rm2023.dynamicshops.listeners.ShopSell;

@Plugin(id = "dynamicshops", name = "Dynamic Shops", version = "0.0.0", description = "Provides admin shops which follow a logistic function for price setting.")
public class DynamicShops {
    @Inject
    private Logger logger_;
    @Inject
    private PluginContainer container_;
    public static Logger logger;
    public static PluginContainer container;
    public static ShopData data;
    public static EconomyService economy;

    @Listener
    public void onStart(GameStartedServerEvent event) {
        logger = logger_;
        container = container_;
        Optional<ProviderRegistration<EconomyService>> economyMaybe = Sponge.getServiceManager().getRegistration(EconomyService.class);
        if (!economyMaybe.isPresent()) {
            logger.error("Dynamic Shops REQUIRES an Economy plugin in order to function. Its functionality has been disabled.");
            Sponge.getGame().getEventManager().unregisterPluginListeners(this);
            return;
        }
        economy = economyMaybe.get().getProvider();
        data = new ShopData();

        ArrayList<String> suggestions = new ArrayList<String>();
        suggestions.add("buyOnly");
        suggestions.add("sellOnly");

        CommandSpec createShop = CommandSpec.builder().description(Text.of("Creates a dynamic shop whose price changes based on how many items have been bought/sold from it.")).permission("dynamicshops.admin").arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("shopName"))), GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("minPrice"))), GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("maxPrice"))), GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("priceChangeRate"))), GenericArguments.optional(GenericArguments.withSuggestions(GenericArguments.string(Text.of("buyOnly/sellOnly/initialPrice")), suggestions))).executor(new CreateShopCommand()).build();
        CommandSpec createStaticShop = CommandSpec.builder().description(Text.of("Creates a static shop whose price doesn't change.")).permission("dynamicshops.admin").arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("shopName"))), GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("price"))), GenericArguments.optional(GenericArguments.withSuggestions(GenericArguments.string(Text.of("buyOnly/sellOnly")), suggestions))).executor(new CreateStaticShopCommand()).build();
        CommandSpec createCommandShop = CommandSpec.builder().description(Text.of("Creates a buy only shop that executes a command when its run. Use @p in the command whereever you want to specify a player.")).permission("dynamicshops.admin").arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("shopName"))), GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("initialPrice"))), GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("maxPrice"))), GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("priceChangeRate"))), GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of("command")))).executor(new CreateCommandShopCommand()).build();
        CommandSpec setPrice = CommandSpec.builder().description(Text.of("Sets the price of a shop.")).permission("dynamicshops.admin").arguments(GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("price")))).executor(new SetPriceCommand()).build();
        CommandSpec resetPrice = CommandSpec.builder().description(Text.of("Resets the price of a shop")).permission("dynamicshops.admin").executor(new ResetPriceCommand()).build();

        CommandSpec main = CommandSpec.builder().description(Text.of("Main command for DynamicShops")).permission("dynamicshops.admin").child(createShop, "createShop").child(createStaticShop, "createStaticShop").child(setPrice, "setPrice").child(resetPrice, "resetShop").child(createCommandShop, "createCommandShop").executor(new MainCommand()).build();
        Sponge.getCommandManager().register(this, main, "dynamicshops", "ds");

        Sponge.getEventManager().registerListeners(this, new ShopAdjust());
        Sponge.getEventManager().registerListeners(this, new ShopBreak());
        Sponge.getEventManager().registerListeners(this, new ShopBuy());
        Sponge.getEventManager().registerListeners(this, new ShopChange());
        Sponge.getEventManager().registerListeners(this, new ShopCreate());
        Sponge.getEventManager().registerListeners(this, new ShopSell());
        logger.info("Dynamic Shops Started");
    }

    @Listener
    public void onStop(GameStoppingServerEvent event) {
        data.save(true);
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        data = new ShopData();
    }
}