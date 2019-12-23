package io.github.rm2023.dynamicshops.util;

import org.spongepowered.api.entity.living.player.Player;

import io.github.rm2023.dynamicshops.shop.Shop;

public class CreateShopData {
    public Player player;
    public Shop shop;

    public CreateShopData(Player p, Shop s) {
        player = p;
        shop = s;
    }
}
