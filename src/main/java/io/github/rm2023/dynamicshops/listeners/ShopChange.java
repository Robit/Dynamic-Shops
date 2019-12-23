package io.github.rm2023.dynamicshops.listeners;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;

import io.github.rm2023.dynamicshops.DynamicShops;
import io.github.rm2023.dynamicshops.shop.Shop;

public class ShopChange {
    @Listener(order = Order.FIRST)
    public void ShopChangeEvent(ChangeSignEvent event) {
        Shop shop = DynamicShops.data.getShop(event.getTargetTile().getLocation());
        if (shop != null) {
            if (!event.getCause().getContext().equals(DynamicShops.container)) {
                DynamicShops.logger.debug("Cancelled ChangeSignEvent at location of shop " + shop.getName());
                event.setCancelled(true);
            }
        }
    }
}
