/*  Dynamic Shops: A plugin for the Sponge API
 *   Copyright (C) 2019 rm2023
 *
 *  This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.rm2023.dynamicshops.data;

import java.io.IOException;
import java.util.LinkedList;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.TypeTokens;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.common.reflect.TypeToken;

import io.github.rm2023.dynamicshops.DynamicShops;
import io.github.rm2023.dynamicshops.shop.CommandShop;
import io.github.rm2023.dynamicshops.shop.ItemShop;
import io.github.rm2023.dynamicshops.shop.Shop;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class ShopData {
    LinkedList<Shop> shopList;
    ConfigurationLoader<CommentedConfigurationNode> dbLoader;
    private CommentedConfigurationNode dbNode;
    private int lastSaveAge = 0;
    private static TypeToken<Location<World>> locationToken = new TypeToken<Location<World>>() {
    };

    public ShopData() {
        shopList = new LinkedList<Shop>();
        dbLoader = DynamicShops.dbLoader;
        dbNode = DynamicShops.dbNode.copy();

        for (CommentedConfigurationNode node : dbNode.getChildrenMap().values()) {
            if (node.getNode("type").getString().equals("ItemShop")) {
                try {
                    ItemShop shop = new ItemShop(node.getNode("name").getString(), node.getNode("location").getValue(locationToken), node.getNode("min").getDouble(), node.getNode("max").getDouble(), node.getNode("k").getDouble(), node.getNode("canBuy").getBoolean(), node.getNode("canSell").getBoolean(), node.getNode("items").getValue(TypeTokens.ITEM_SNAPSHOT_TOKEN).createStack());
                    shop.setOffset(node.getNode("offset").getDouble());
                    shop.setSign();
                    shopList.add(shop);
                } catch (ObjectMappingException e) {
                    e.printStackTrace();
                }
            }
            if (node.getNode("type").getString().equals("CommandShop")) {
                try {
                    CommandShop shop = new CommandShop(node.getNode("name").getString(), node.getNode("location").getValue(locationToken), node.getNode("min").getDouble(), node.getNode("max").getDouble(), node.getNode("k").getDouble(), node.getNode("command").getString());
                    shop.setOffset(node.getNode("offset").getDouble());
                    shop.setSign();
                    shopList.add(shop);
                } catch (ObjectMappingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean addShop(Shop shop) {
        if (shopList.add(shop)) {
            save(true);
            return true;
        }
        return false;
    }

    public boolean removeShop(Shop shop) {
        if (shopList.remove(shop)) {
            save(true);
            return true;
        }
        return false;
    }

    public boolean removeShop(Location<World> location) {
        Shop toRemove = getShop(location);
        if (toRemove != null) {
            return removeShop(toRemove);
        }
        return true;
    }

    public Shop getShop(Location<World> location) {
        if (location == null) {
            return null;
        }
        for (Shop shop : shopList) {
            if (shop.getLocation() != null && shop.getLocation().equals(location)) {
                return shop;
            }
        }
        return null;
    }

    public void save(boolean force) {
        if (force || Sponge.getServer().getRunningTimeTicks() - lastSaveAge > 100) {
            dbNode = dbLoader.createEmptyNode();
            int i = 0;
            for (Shop shop : shopList) {
                i++;
                CommentedConfigurationNode node = dbNode.getNode(Integer.toString(i));
                if (shop instanceof ItemShop) {
                    try {
                        node.getNode("type").setValue("ItemShop");
                        node.getNode("name").setValue(shop.getName());
                        node.getNode("location").setValue(locationToken, shop.getLocation());
                        node.getNode("min").setValue(shop.getMin());
                        node.getNode("max").setValue(shop.getMax());
                        node.getNode("k").setValue(shop.getK());
                        node.getNode("canBuy").setValue(shop.getCanBuy());
                        node.getNode("canSell").setValue(shop.getCanSell());
                        node.getNode("offset").setValue(shop.getOffset());
                        node.getNode("items").setValue(TypeTokens.ITEM_SNAPSHOT_TOKEN, ((ItemShop) shop).getItems().createSnapshot());
                    } catch (ObjectMappingException e) {
                        e.printStackTrace();
                    }
                }
                if (shop instanceof CommandShop) {
                    try {
                        node.getNode("type").setValue("CommandShop");
                        node.getNode("name").setValue(shop.getName());
                        node.getNode("location").setValue(locationToken, shop.getLocation());
                        node.getNode("min").setValue(shop.getMin());
                        node.getNode("max").setValue(shop.getMax());
                        node.getNode("offset").setValue(shop.getOffset());
                        node.getNode("command").setValue(((CommandShop) shop).getCommand());
                    } catch (ObjectMappingException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                dbLoader.save(dbNode);
                lastSaveAge = Sponge.getServer().getRunningTimeTicks();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
