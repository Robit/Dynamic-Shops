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

package io.github.rm2023.dynamicshops.shop;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import io.github.rm2023.dynamicshops.DynamicShops;
import io.github.rm2023.dynamicshops.util.Util;

public abstract class Shop {
    protected String name;
    protected Location<World> location;
    protected double initial;
    protected double offset;
    protected double min;
    protected double max;
    protected double k;
    protected boolean canBuy;
    protected boolean canSell;
    protected ArrayList<Player> recentlyUsed = new ArrayList<Player>();

    public Shop() {
        name = "UNDEFINED";
        location = null;
        offset = 0;
        min = 0;
        max = 0;
        k = 0;
        canBuy = false;
        canSell = false;
    }

    public Shop(String name, Location<World> location, double min, double max, double k, boolean canBuy, boolean canSell) {
        this.name = name;
        this.location = location;
        this.min = min;
        this.max = max;
        this.k = k;
        this.canBuy = canBuy;
        this.canSell = canSell;
        if (!canBuy) {
            setPrice(max);
        }
        if (!canSell) {
            setPrice(min);
        }
        initial = getOffset();
    }

    public String getName() {
        return new String(name);
    }

    public double getInitial() {
        return initial;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getK() {
        return k;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public boolean getCanBuy() {
        return canBuy;
    }

    public boolean getCanSell() {
        return canSell;
    }

    public void setLocation(Location<World> location) {
        this.location = location;
    }

    public Location<World> getLocation() {
        return location;
    }


    public double getSellPrice() {
        if (k == 0) {
            return min;
        }
        if (!canBuy) {
            return ((double) (Math.round((min + ((max - min) / (1 + Math.pow(Math.E, -1 * k * offset)))) * Math.pow(10, DynamicShops.economy.getDefaultCurrency().getDefaultFractionDigits())))) / Math.pow(10, DynamicShops.economy.getDefaultCurrency().getDefaultFractionDigits());
        }
        return ((double) (Math.round((min + ((max - min) / (1 + Math.pow(Math.E, -1 * k * (offset - 1))))) * Math.pow(10, DynamicShops.economy.getDefaultCurrency().getDefaultFractionDigits())))) / Math.pow(10, DynamicShops.economy.getDefaultCurrency().getDefaultFractionDigits());
    }

    public double getBuyPrice() {
        if (k == 0) {
            return min;
        }
        if (!canSell) {
            return ((double) (Math.round((min + ((max - min) / (1 + Math.pow(Math.E, -1 * k * offset)))) * Math.pow(10, DynamicShops.economy.getDefaultCurrency().getDefaultFractionDigits())))) / Math.pow(10, DynamicShops.economy.getDefaultCurrency().getDefaultFractionDigits());
        }
        return ((double) (Math.round((min + ((max - min) / (1 + Math.pow(Math.E, -1 * k * (offset + 1))))) * Math.pow(10, DynamicShops.economy.getDefaultCurrency().getDefaultFractionDigits())))) / Math.pow(10, DynamicShops.economy.getDefaultCurrency().getDefaultFractionDigits());
    }

    public boolean setPrice(double price) {
        if (k == 0) {
            min = price;
            return true;
        }
        if (price < min || price > max) {
            return false;
        }
        // If the price is with rounding range of min or max, set the price to be
        // rounding range away from the boundary
        price = Math.max(price, (1 + Math.pow(10, -1 * DynamicShops.economy.getDefaultCurrency().getDefaultFractionDigits())) * min);
        price = Math.min(price, (1 - Math.pow(10, -1 * DynamicShops.economy.getDefaultCurrency().getDefaultFractionDigits())) * max);
        // Maaaaaaath
        price = (price - min) / (max - min);
        offset = Math.log(price / (1 - price)) / k;
        updateSign();
        return true;
    }

    public boolean buy(Player p) {
        if (canBuy && p.hasPermission("dynamicshops.buy." + getName())) {
            if (recentlyUsed.contains(p)) {
                DynamicShops.logger.trace("Antispam prevented " + p.getName() + " buying from shop " + getName());
                return false;
            }
            Task task = Task.builder().execute(new RemoveFromListTask(p, recentlyUsed)).delay(250, TimeUnit.MILLISECONDS).name("Shop " + getName() + " antispam task for player" + p.getName()).submit(DynamicShops.container);
            DynamicShops.data.save(false);
            double oldPrice = getBuyPrice();
            if (buyOperation(p)) {
                DynamicShops.logger.info(p.getName() + " bought from the shop " + getName());
                updateSign();
                Util.message(p, "Purchase Successful.", false);
                if (oldPrice != getBuyPrice()) {
                    Util.message(p, "The price has changed to " + DynamicShops.economy.getDefaultCurrency().getSymbol().toPlain() + getBuyPrice(), false);
                }
                recentlyUsed.add(p);
                return true;
            } else {
                DynamicShops.logger.debug(p.getName() + " attempted to buy from the shop " + getName() + " but failed due to a buy operation error");
                return false;
            }
        }
        DynamicShops.logger.debug(p.getName() + " attempted to buy from the shop " + getName() + " but failed either because they did not have permission or because the shop cannot be bought from.");
        return false;
    }

    public boolean sell(Player p) {
        if (canSell && p.hasPermission("dynamicshops.sell." + getName())) {
            if (recentlyUsed.contains(p)) {
                DynamicShops.logger.trace("Antispam prevented " + p.getName() + " selling to shop " + getName());
                return false;
            }
            Task task = Task.builder().execute(new RemoveFromListTask(p, recentlyUsed)).delay(250, TimeUnit.MILLISECONDS).name("Shop " + getName() + " antispam task for player" + p.getName()).submit(DynamicShops.container);
            DynamicShops.data.save(false);
            double oldPrice = getSellPrice();
            if (sellOperation(p)) {
                DynamicShops.logger.info(p.getName() + " sold to the shop " + getName());
                updateSign();
                Util.message(p, "Sell Successful.", false);
                if (oldPrice != getSellPrice()) {
                    Util.message(p, "The price has changed to " + DynamicShops.economy.getDefaultCurrency().getSymbol().toPlain() + getSellPrice(), false);
                }
                recentlyUsed.add(p);
                return true;
            } else {
                DynamicShops.logger.debug(p.getName() + " attempted to sell to the shop " + getName() + " but failed due to a sell operation error");
                return false;
            }
        }
        DynamicShops.logger.debug(p.getName() + " attempted to sell to the shop " + getName() + " but failed either because they did not have permission or because the shop cannot be sold to.");
        return false;
    }

    public boolean bulkBuy(Player p) {
        if (canBulk() && canBuy && p.hasPermission("dynamicshops.buy." + getName())) {
            if (recentlyUsed.contains(p)) {
                DynamicShops.logger.trace("Antispam prevented " + p.getName() + " bulk buying from shop " + getName());
                return false;
            }
            Task task = Task.builder().execute(new RemoveFromListTask(p, recentlyUsed)).delay(250, TimeUnit.MILLISECONDS).name("Shop " + getName() + " antispam task for player" + p.getName()).submit(DynamicShops.container);
            DynamicShops.data.save(false);
            double oldPrice = getBuyPrice();
            if (bulkBuyOperation(p)) {
                DynamicShops.logger.info(p.getName() + " bulk bought from the shop " + getName());
                updateSign();
                Util.message(p, "Purchase Successful.", false);
                if (oldPrice != getBuyPrice()) {
                    Util.message(p, "The price has changed to " + DynamicShops.economy.getDefaultCurrency().getSymbol().toPlain() + getBuyPrice(), false);
                }
                recentlyUsed.add(p);
                return true;
            } else {
                DynamicShops.logger.debug(p.getName() + " attempted to bulk buy from the shop " + getName() + " but failed due to a buy operation error (or requires confirmation).");
                return false;
            }
        }
        DynamicShops.logger.debug(p.getName() + " attempted to bulk buy from the shop " + getName() + " but failed either because they did not have permission or because the shop cannot be bought from.");
        return false;
    }

    public boolean bulkSell(Player p) {
        if (canBulk() && canSell && p.hasPermission("dynamicshops.sell." + getName())) {
            if (recentlyUsed.contains(p)) {
                DynamicShops.logger.trace("Antispam prevented " + p.getName() + " bulk selling to shop " + getName());
                return false;
            }
            Task task = Task.builder().execute(new RemoveFromListTask(p, recentlyUsed)).delay(250, TimeUnit.MILLISECONDS).name("Shop " + getName() + " antispam task for player" + p.getName()).submit(DynamicShops.container);
            DynamicShops.data.save(false);
            double oldPrice = getSellPrice();
            if (bulkSellOperation(p)) {
                DynamicShops.logger.info(p.getName() + " bulk sold to the shop " + getName());
                updateSign();
                Util.message(p, "Purchase Successful.", false);
                if (oldPrice != getSellPrice()) {
                    Util.message(p, "The price has changed to " + DynamicShops.economy.getDefaultCurrency().getSymbol().toPlain() + getSellPrice(), false);
                }
                recentlyUsed.add(p);
                return true;
            } else {
                DynamicShops.logger.debug(p.getName() + " attempted to bulk sell to the shop " + getName() + " but failed due to a sell operation error (or requires confirmation).");
                return false;
            }
        }
        DynamicShops.logger.debug(p.getName() + " attempted to bulk sell to the shop " + getName() + " but failed either because they did not have permission or because the shop cannot be bought from.");
        return false;
    }

    protected class RemoveFromListTask implements Consumer<Task> {
        private Player p;
        private ArrayList<Player> l;

        public RemoveFromListTask(Player p, ArrayList<Player> l) {
            this.p = p;
            this.l = l;
        }

        @Override
        public void accept(Task t) {
            l.remove(p);
        }
    }

    public void setSign() {
        if (location == null) {
            return;
        }
        TileEntity signTile = location.getTileEntity().orElse(null);
        if (signTile == null || !signTile.get(SignData.class).isPresent()) {
            DynamicShops.logger.error("Could not set the sign of shop " + getName() + ". It seems like it isn't a sign entity!");
            return;
        }
        SignData data = signTile.get(SignData.class).get();
        data.setElement(0, Text.builder("[" + getName() + "]").color(TextColors.BLUE).build());
        if (canBuy) {
            if (canSell) {
                data.setElement(1, Text.of("Buy/Sell"));
            } else {
                data.setElement(1, Text.of("Buy"));
            }
        } else {
            if (canSell) {
                data.setElement(1, Text.of("Sell"));
            }
        }
        if (!signTile.offer(data).isSuccessful()) {
            DynamicShops.logger.error("Could not set the sign of shop " + getName() + ". Data transaction failed (perhaps the price is too high to render on a sign?)");
        }
        updateSign();
    }

    public void updateSign() {
        if (location == null) {
            return;
        }
        TileEntity signTile = location.getTileEntity().orElse(null);
        if (signTile == null || !signTile.get(SignData.class).isPresent()) {
            DynamicShops.logger.error("Could not set the sign of shop " + getName() + ". It seems like it isn't a sign entity!");
            return;
        }
        SignData data = signTile.get(SignData.class).get();
        if (canBuy) {
            if (canSell) {
                if (getBuyPrice() < 9999) {
                    data.setElement(2, Text.of(DynamicShops.economy.getDefaultCurrency().getSymbol().toPlain() + getBuyPrice() + "/" + DynamicShops.economy.getDefaultCurrency().getSymbol().toPlain() + getSellPrice()));
                } else {
                    data.setElement(2, Text.of("B " + DynamicShops.economy.getDefaultCurrency().getSymbol().toPlain() + getBuyPrice()));
                    data.setElement(3, Text.of("S " + DynamicShops.economy.getDefaultCurrency().getSymbol().toPlain() + getSellPrice()));
                }
            } else {
                data.setElement(2, Text.of(DynamicShops.economy.getDefaultCurrency().getSymbol().toPlain() + getBuyPrice()));
            }
        } else {
            if (canSell) {
                data.setElement(2, Text.of(DynamicShops.economy.getDefaultCurrency().getSymbol().toPlain() + getSellPrice()));
            }
        }
        if (!signTile.offer(data).isSuccessful()) {
            DynamicShops.logger.error("Could not set the sign of shop " + getName() + ". Data transaction failed (perhaps the price is too high to render on a sign?)");
        }
    }

    protected abstract boolean buyOperation(Player p);

    protected abstract boolean bulkBuyOperation(Player p);

    protected abstract boolean sellOperation(Player p);

    protected abstract boolean bulkSellOperation(Player p);

    protected abstract boolean canBulk();
}
