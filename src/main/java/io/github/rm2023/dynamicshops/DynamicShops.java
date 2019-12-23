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

import java.util.Optional;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

import com.google.inject.Inject;

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
    public static Logger logger;
    @Inject
    public static PluginContainer container;
    public static ShopData data;
    public static EconomyService economy;

    @Listener
    public void onStart(GameStartedServerEvent event) {
        Optional<EconomyService> economyMaybe = Sponge.getServiceManager().getRegistration(EconomyService.class);
        if (!economyMaybe.isPresent()) {
            logger.error("Dynamic Shops REQUIRES an Economy plugin in order to function. Its functionality has been disabled.");
            return;
        }
        data = new ShopData();

        // Sponge.getCommandManager().register();

        Sponge.getEventManager().registerListeners(this, new ShopAdjust());
        Sponge.getEventManager().registerListeners(this, new ShopBreak());
        Sponge.getEventManager().registerListeners(this, new ShopBuy());
        Sponge.getEventManager().registerListeners(this, new ShopChange());
        Sponge.getEventManager().registerListeners(this, new ShopCreate());
        Sponge.getEventManager().registerListeners(this, new ShopSell());
    }
}