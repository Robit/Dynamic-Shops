package io.github.rm2023.dynamicshops.listeners;

import java.util.Optional;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import io.github.rm2023.dynamicshops.DynamicShops;
import io.github.rm2023.dynamicshops.shop.Shop;

public class ShopBreak {
    @Listener(order = Order.FIRST)
    public void ShopBreakEvent(ChangeBlockEvent event) {
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (transaction.isValid()) {
                Shop shop = DynamicShops.data.getShop(transaction.getOriginal().getLocation().orElse(null));
                if (shop != null) {
                    Optional<Player> p = event.getCause().first(Player.class);
                    if (p.isPresent() && p.get().hasPermission("dynamiceconomy.admin")) {
                        DynamicShops.data.removeShop(shop);
                        DynamicShops.logger.info("Removed shop " + shop.getName() + " due to a block break event caused by " + p.get().getName());
                    } else {
                        DynamicShops.logger.debug("Prevented ChangeBlockEvent at location of " + shop.getName());
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
