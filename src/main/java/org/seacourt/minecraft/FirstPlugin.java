package org.seacourt.minecraft;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

// Good reference tutorial here: https://bukkit.fandom.com/wiki/Plugin_Tutorial_(Eclipse).

// Run server as usual, but in paper_our_plugin directory.

// To update the plugin:
//  sudo cp /home/d40cht/Development/Minecraft/first_plugin/first_plugin/target/first_plugin-1.0-SNAPSHOT.jar /home/minecraft/paper_our_plugin/plugins/ && sudo chown minecraft:minecraft /home/minecraft/paper_our_plugin/plugins/first_plugin-1.0-SNAPSHOT.jar

public class FirstPlugin extends JavaPlugin implements Listener {

    public class PlayerData {
        private Player player;
        private int numLives;

        public PlayerData(Player player, int numLives) {
            this.player = player;
            this.numLives = numLives;
        }

        public void updatePlayerState() {
            ChatColor playerColor = getPlayerColor();
            player.setDisplayName(playerColor + ChatColor.stripColor(player.getDisplayName()));

            if (numLives == 0) {
                player.setGameMode(GameMode.SPECTATOR);
            } else {
                player.setGameMode(GameMode.SURVIVAL);
            }
        }

        public void loseLife() {
            numLives = Math.max(0, numLives - 1);
        }

        public int getNumLives() {
            return numLives;
        }

        private ChatColor getPlayerColor() {
            if (numLives == 0) {
                return ChatColor.GRAY;
            } else if (numLives == 1) {
                return ChatColor.RED;
            } else if (numLives == 2) {
                return ChatColor.YELLOW;
            } else if (numLives == 3) {
                return ChatColor.GREEN;
            } else if (numLives < 7) {
                return ChatColor.DARK_GREEN;
            }

            return ChatColor.BLUE;
        }
    }

    HashMap<UUID, PlayerData> allPlayerData;
    Random randomGen;

    public FirstPlugin() {
        allPlayerData = new HashMap<>();
        randomGen = new Random();
    }

    @Override
    public void onEnable() {
        getLogger().info("Hello, SpigotMC from FirstPlugin!");

        // Register our plugin class to get and respond to minecraft events.
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("See you again, SpigotMC!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // (if sender instanceof Player)
        return false;
    }

    // Other listeners (need to be annotated with @EventHandler) are:
    // PlayerMoveEvent, PlayerJoinEvent, EntityDamageEvent, EntityRegenEvent,
    // EntityDeathEvent
    // Events docs here:
    // https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/package-summary.html

    // And to make things change:
    // player.setColor (using ChatColor enum)

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
        getLogger().info("Our plugin has noticed that a player has joined!");
        Player joiningPlayer = playerJoinEvent.getPlayer();

        // Choose a random number of lives.
        int playerLives = 3 + randomGen.nextInt(4);

        PlayerData thisPlayerData = new PlayerData(joiningPlayer, playerLives);
        thisPlayerData.updatePlayerState();
        allPlayerData.put(joiningPlayer.getUniqueId(), thisPlayerData);

        joiningPlayer.sendMessage(String.format("Hello new person - you have %d lives.", playerLives));
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent entityDeathEvent) {
        Entity entity = entityDeathEvent.getEntity();

        getLogger().info(String.format("An entity died: %s", entity));

        if (!(entity instanceof Player)) {
            // Currently - do nothing on non-player death.
            return;
        }

        getLogger().info(String.format("A player died: %s", entity));

        // Get the player data for this player.
        Player thisPlayer = (Player) entity;
        PlayerData playerData = allPlayerData.get(thisPlayer.getUniqueId());

        playerData.loseLife();
        playerData.updatePlayerState();

        thisPlayer
                .sendMessage(String.format("Oh no - you died! You only have %d lives left.", playerData.getNumLives()));
    }
}