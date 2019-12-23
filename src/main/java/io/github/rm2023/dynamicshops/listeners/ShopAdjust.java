package io.github.rm2023.dynamicshops.listeners;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;

public class ShopAdjust {
    @Listener(order = Order.LATE)
    public void ShopAdjustEvent(InteractBlockEvent.Secondary event) {

    }
}
