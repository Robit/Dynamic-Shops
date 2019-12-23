package io.github.rm2023.dynamicshops.listeners;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;

public class ShopBreak {
    @Listener(order = Order.FIRST)
    public void ShopBreakEvent(ChangeBlockEvent event) {

    }
}
