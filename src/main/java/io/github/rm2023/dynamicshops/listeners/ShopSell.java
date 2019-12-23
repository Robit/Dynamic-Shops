package io.github.rm2023.dynamicshops.listeners;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;

import io.github.rm2023.dynamicshops.DynamicShops;
import io.github.rm2023.dynamicshops.shop.Shop;

public class ShopSell {
    @Listener
    public void ShopSellEvent(InteractBlockEvent.Primary event, @First Player player) {
        Shop shop = DynamicShops.data.getShop(event.getTargetBlock().getLocation().orElse(null));
        if (shop != null) {
            shop.buy(player);
        }
    }
}
