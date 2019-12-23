package io.github.rm2023.dynamicshops.listeners;

import java.util.ArrayList;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import io.github.rm2023.dynamicshops.DynamicShops;
import io.github.rm2023.dynamicshops.shop.Shop;
import io.github.rm2023.dynamicshops.util.AdjustPriceData;
import io.github.rm2023.dynamicshops.util.Util;

public class ShopAdjust {
    public static ArrayList<AdjustPriceData> adjustList = new ArrayList<AdjustPriceData>();
    @Listener(order = Order.LATE)
    public void ShopAdjustEvent(InteractBlockEvent.Secondary.MainHand event, @First Player player) {
        for (AdjustPriceData data : adjustList) {
            if (data.player.equals(player)) {
                adjustList.remove(data);
                Location<World> l = event.getTargetBlock().getLocation().get();
                Shop shop = DynamicShops.data.getShop(l);
                if (shop != null) {
                    if (shop.setPrice(data.price)) {
                        Util.message(player, "Price changed successfully.");
                        DynamicShops.logger.info("Shop " + shop.getName() + "'s price was changed to " + data.price + " by " + player.getName());
                        shop.updateSign();
                    } else {
                        Util.message(player, "Error setting price. Price must be between the minimum and maximum prices of the shop. Operation cancelled.");
                    }
                } else {
                    Util.message(player, "Invalid Block! Operation cancelled.");
                }
            }
        }
    }
}
