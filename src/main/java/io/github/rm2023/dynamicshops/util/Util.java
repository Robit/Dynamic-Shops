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

package io.github.rm2023.dynamicshops.util;

import java.math.BigDecimal;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import io.github.rm2023.dynamicshops.DynamicShops;

public class Util {
    public static void message(Player p, String m) {
        p.sendMessage(Text.builder("[DynamicShops] " + m).color(TextColors.BLUE).build());
    }

    public static boolean withdraw(Account playerAccount, EconomyService economy, BigDecimal price, String reason) {
        return playerAccount.withdraw(economy.getDefaultCurrency(), price, Cause.builder().append(reason).build(EventContext.builder().add(EventContextKeys.PLUGIN, DynamicShops.container).build())).getResult() == ResultType.SUCCESS;
    }

    public static boolean deposit(Account playerAccount, EconomyService economy, BigDecimal price, String reason) {
        return playerAccount.deposit(economy.getDefaultCurrency(), price, Cause.builder().append(reason).build(EventContext.builder().add(EventContextKeys.PLUGIN, DynamicShops.container).build())).getResult() == ResultType.SUCCESS;
    }
}
