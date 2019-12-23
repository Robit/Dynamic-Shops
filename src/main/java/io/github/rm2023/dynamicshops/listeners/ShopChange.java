package io.github.rm2023.dynamicshops.listeners;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;

public class ShopChange {
    @Listener(order=Order.FIRST)
    public void ShopChangeEvent(ChangeSignEvent event)
    {
	
    }
}
