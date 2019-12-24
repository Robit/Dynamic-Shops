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
    public static Text helpMessage;

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

        // TODO:Make this less of a mess. Probably paginate it.
        helpMessage = Text.of("DynamicShop allows you to make admin shops that can buy/sell items or sell command executions and with a price that changes depending on how often it is bought from or sold to. It uses the default currency and rounding of your economy system." 
        , Text.NEW_LINE, "/ds is a shorthand command that is an alias for dynamicshop. Only players with dynamicshops.admin (or OP) can use either command." 
        , Text.NEW_LINE, "/dyanmicshop createShop [name] [minPrice] [maxPrice] [priceChangeRate] [optional]."
        , Text.NEW_LINE, "   This command creates a dynamic shop that buys/sells the item stack in your hand. For example, if you do /ds createShop getyourglowstone 1 2 0.05, while holding 32 glowstone, it will buy and sell 32 glowstone for a minimum of 1 currency and maximum of 2 currency, starting at 1.5 currency. It also preserves item metadata. priceChangeRate does not directly change the price, but determines the slope of a logistic curve. Use the graph at https://tinyurl.com/dynamicshop1 to explore how modifying minPrice, maxPrice, and priceChangeRate will change the price. It is reccomended that priceChangeRate is below 0.05 for best results. By default, the initial price of a shop will the average of its minimum and maximum prices. However, if [optional] is a number, then that number will be the inital price. [optional] can also be buyOnly or sellOnly. If the shop is buyOnly, the initial price is set to 1 rounding unit (1 cent for dollar economies) above minPrice and players can only left click the shop to buy. Vice versa for sellOnly."
        , Text.NEW_LINE, "   ex) Running /dynamicshop createShop GlowstoneEmporium 1 10 0.05 while holding a glowstone in your hand will make a shop named GlowstoneEmporium that players can buy/sell a glowstone from/to for 6.5 currency initially, but the price will increase as players buy from it and decrease as players sell to it."
        , Text.NEW_LINE, "   ex) Running /dynamicshop createShop GiveMeGlowstoneee 1 10 0.05 sellOnly while holding a glowstone in your hand will make a shop named GiveMeGlowstoneee that players can sell a glowstone to for 10 currency initially, but the sell price will eventually decrease to 1 currency"
        , Text.NEW_LINE, "/dynamicshop createStaticShop name price [optional]"
        , Text.NEW_LINE, "   This command works similarly to createShop. However, the price of the shop does not change. [optional] can only be used to reference buyOnly and sellOnly"
        , Text.NEW_LINE, "   ex) Running /dynamicshop createStaticShop unchangingGlowstone 6 while holding 8 glowstone in your hand will make a shop that always buys/sells 8 glowstone for 6."
        , Text.NEW_LINE, "/dynamicshop createCommandShop name min max priceChangeRate command"
        , Text.NEW_LINE, "   This command creates a dynamic command shop. It is similar to createShop but it is buyOnly and runs the specified command from console whenever its bought from, rather than giving an item. Use @p wherever you want to specify the buyer's name"
        , Text.NEW_LINE, "   ex) /dynamicstop createCommandShop healmeplz 1 10 0.05 heal @p will create a shop named healmeplz that costs 1 currency initially but will eventually go up to 10 currency. When bought, it runs the command heal [buyer] from console."
        , Text.NEW_LINE, "/dynamicshop setPrice price"
        , Text.NEW_LINE, "   This command allows an admin to set the price of a shop manually. It will not allow you to set the price less than the minimum price of a shop or the maximum price of a shop."
        , Text.NEW_LINE, "   ex) /dynamicshop setPrice 5 will allow you to set the price of any shop with a min < 5 and a max > 5 to 5."
        , Text.NEW_LINE, "/dynamicshop resetPrice"
        , Text.NEW_LINE, "   This command allows an admin to set the price of a shop back to its defaults. This is avg(maxPrice,minPrice) for a normal shop, almost minPrice for a sellOnly shop, and almost maxPrice for a buyOnly shop."
        , Text.NEW_LINE, "PLAYERS NEED THE PERMISSION dynamicshops.[shopname].buy TO BUY FROM SHOPS AND dynamicshops.[shopname].sell TO SELL TO THEM. IT IS SUGGESTING YOU GIVE THEM dynamicshops.sell.* and dynamicshops.buy.* AND NEGATE SPECIFIC PERMISSIONS FOR SHOPS YOU DONT WANT PLAYERS USING. Please consult the help page for your permissions plugin for more information."
        , Text.NEW_LINE, "Need more help? Please consult the official plugin documentation on Sponge Ore or contact the dev on Ore or Github for support");
        
        CommandSpec createShop = CommandSpec.builder().description(Text.of("Creates a dynamic shop whose price changes based on how many items have been bought/sold from it.")).permission("dynamicshops.admin").arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("shopName"))), GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("minPrice"))), GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("maxPrice"))), GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("priceChangeRate"))), GenericArguments.optional(GenericArguments.withSuggestions(GenericArguments.string(Text.of("buyOnly/sellOnly/initialPrice")), suggestions))).executor(new CreateShopCommand()).build();
        CommandSpec createStaticShop = CommandSpec.builder().description(Text.of("Creates a static shop whose price doesn't change.")).permission("dynamicshops.admin").arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("shopName"))), GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("price"))), GenericArguments.optional(GenericArguments.withSuggestions(GenericArguments.string(Text.of("buyOnly/sellOnly")), suggestions))).executor(new CreateStaticShopCommand()).build();
        CommandSpec createCommandShop = CommandSpec.builder().description(Text.of("Creates a buy only shop that executes a command when its run. Use @p in the command whereever you want to specify a player.")).permission("dynamicshops.admin").arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("shopName"))), GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("initialPrice"))), GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("maxPrice"))), GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("priceChangeRate"))), GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of("command")))).executor(new CreateCommandShopCommand()).build();
        CommandSpec setPrice = CommandSpec.builder().description(Text.of("Sets the price of a shop.")).permission("dynamicshops.admin").arguments(GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("price")))).executor(new SetPriceCommand()).build();
        CommandSpec resetPrice = CommandSpec.builder().description(Text.of("Resets the price of a shop")).permission("dynamicshops.admin").executor(new ResetPriceCommand()).build();

        CommandSpec main = CommandSpec.builder().description(Text.of("Main command for DynamicShops")).extendedDescription(helpMessage).permission("dynamicshops.admin").child(createShop, "createShop").child(createStaticShop, "createStaticShop").child(setPrice, "setPrice").child(resetPrice, "resetShop").child(createCommandShop, "createCommandShop").executor(new MainCommand()).build();
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