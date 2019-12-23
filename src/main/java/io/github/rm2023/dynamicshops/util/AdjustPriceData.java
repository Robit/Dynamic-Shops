package io.github.rm2023.dynamicshops.util;

import org.spongepowered.api.entity.living.player.Player;

import io.github.rm2023.dynamicshops.shop.Shop;

public class AdjustPriceData {
    public Player player;
    public double price;
    public AdjustPriceData(Player p, double price)
    {
	player = p;
	this.price = price;
    }
}
