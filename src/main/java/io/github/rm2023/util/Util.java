package io.github.rm2023.util;

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

import io.github.rm2023.DynamicEconomy;

public class Util {
    public static void message(Player p, String m)
    {
	p.sendMessage(Text.builder("[DynamicEconomy]" + m).color(TextColors.BLUE).build());
    }
    
    public static boolean withdraw(Account playerAccount, EconomyService economy, BigDecimal price, String reason)
    {
	return playerAccount.withdraw(economy.getDefaultCurrency(), price, Cause.builder().append(reason).build(EventContext.builder().add(EventContextKeys.PLUGIN, DynamicEconomy.container).build())).getResult() == ResultType.SUCCESS;
    }
    
    public static boolean deposit(Account playerAccount, EconomyService economy, BigDecimal price, String reason)
    {
	return playerAccount.deposit(economy.getDefaultCurrency(), price, Cause.builder().append(reason).build(EventContext.builder().add(EventContextKeys.PLUGIN, DynamicEconomy.container).build())).getResult() == ResultType.SUCCESS;
    }
}
