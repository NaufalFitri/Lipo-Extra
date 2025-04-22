package dev.lipoteam.lipoExtra;

import dev.lipoteam.lipoExtra.Files.Configurations;
import org.bukkit.entity.Player;
import se.file14.procosmetics.ProCosmetics;
import se.file14.procosmetics.economy.EconomyFailureException;
import se.file14.procosmetics.economy.IEconomyProvider;
import se.file14.procosmetics.user.User;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

public class EconomyProvider implements IEconomyProvider {

    private final LipoExtra plugin;
    private final Configurations config;
    Currency currency;

    public EconomyProvider(LipoExtra plugin, Configurations config) {
        this.plugin = plugin;
        this.config = config;

        currency = CoinsEngineAPI.getCurrency(config.ProcosmeticsCurrency());
        if (currency == null) {
            plugin.getLogger().warning(currency.getName() + "is Null");
        }
    }

    @Override
    public String getPlugin() {
        return plugin.getName();
    }

    @Override
    public void hook(ProCosmetics proCosmetics) throws EconomyFailureException {

    }

    @Override
    public void addCoins(User user, int i) {
        Player p = user.getPlayer();

        if (p != null && currency != null) {
            CoinsEngineAPI.addBalance(p, currency, i);
        }
    }

    @Override
    public void setCoins(User user, int i) {
        Player p = user.getPlayer();

        if (p != null && currency != null) {
            CoinsEngineAPI.setBalance(p, currency, i);
        }
    }

    @Override
    public int getCoins(User user) {
        Player p = user.getPlayer();

        if (p != null && currency != null) {
            return (int) CoinsEngineAPI.getBalance(p, currency);
        }
        return 0;
    }

    @Override
    public boolean hasCoins(User user, int i) {
        Player p = user.getPlayer();
        if (p != null && currency != null) {
            return CoinsEngineAPI.getBalance(p, currency) >= i;
        }
        return false;
    }

    @Override
    public void removeCoins(User user, int i) {
        Player p = user.getPlayer();

        if (p != null && currency != null) {
            CoinsEngineAPI.removeBalance(p, currency, i);
        }
    }

}
