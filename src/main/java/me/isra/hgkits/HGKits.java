package me.isra.hgkits;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;

import io.github.ichocomilk.lightsidebar.Sidebar;
import io.github.ichocomilk.lightsidebar.nms.v1_8R3.Sidebar1_8R3;
import me.isra.hgkits.commands.KitCommand;
import me.isra.hgkits.commands.RankCommand;
import me.isra.hgkits.commands.StartCommand;
import me.isra.hgkits.commands.TopCommand;
import me.isra.hgkits.config.Config;
import me.isra.hgkits.config.ConfigManager;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.database.User;
import me.isra.hgkits.listeners.*;
import me.isra.hgkits.managers.KitManager;
import me.isra.hgkits.managers.PlayerAttackManager;
import me.isra.hgkits.tops.TopFiles;
import me.isra.hgkits.utils.Constants;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class HGKits extends JavaPlugin {
    public static GameState GAMESTATE;

    private final List<Player> players = new ArrayList<>();

    private KitManager kitManager;

    private BukkitRunnable countdownTask;
    private boolean isCountdownRunning = false;

    private BukkitRunnable invincibilityCountdownTask;
    private boolean isInvincibilityCountdownRunning = false;

    private BukkitRunnable checkWinnerCountdownTask;
    private boolean checkWinnerCountdownRunning = false;

    private final Map<UUID, Long> cooldownsMedusa = new HashMap<>();
    private Set<Player> frozenPlayers = new HashSet<>();
    private TopFiles topFiles;

    public World currentWorld;
    private String date;

    @Override
    public void onEnable() {
        if (!(Bukkit.getPluginManager().getPlugin("SlimeWorldManager") instanceof SlimePlugin slimePlugin)) {
            getLogger().warning("Plugin can't start because don't found slimeworldmanager");
            return;
        }

        loadRandomArena(slimePlugin);
        saveDefaultConfig();
    
        Instant instant = Instant.now();
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        date = "§8"+ldt.getDayOfMonth() + '-' + ldt.getMonthValue() + '-' + ldt.getYear();

        Bukkit.setWhitelist(false);

        DatabaseManager.getDatabase();

        File topFolder = new File(getDataFolder(), "tops");
        if (!topFolder.exists()) {
            topFolder.mkdir(); 
        }
        this.topFiles = new TopFiles(topFolder, getConfig().getInt("max-tops"));
        this.topFiles.start();

        kitManager = new KitManager(this);
        PlayerAttackManager playerAttackManager = new PlayerAttackManager();

        final ConfigManager configManager = new ConfigManager(new Yaml(), this);
        final File kitsFolder = new File(getDataFolder(), "kits");

        if (!kitsFolder.exists()) {
            kitsFolder.mkdirs();
        }

        configManager.createIfAbsent("kits",
                "asesino",
                "arquero",
                "barbaro",
                "camaleon",
                "encantador",
                "hulk",
                "sonic",
                "minero",
                "enderman",
                "domabestias",
                "brujo",
                "piromano",
                "guerrero",
                "curandero",
                "orco",
                "ladron",
                "creeper",
                "explorador",
                "ultracreeper",
                "canibal",
                "matasanos",
                "caballero",
                "proladron",
                "coloso",
                "kratos",
                "ironman",
                "prominero",
                "escudero",
                "tanque",
                "pyro",
                "troll",
                "domabestiaspro",
                "proarquero",
                "spiderman",
                "headshooter",
                "elite",
                "flash",
                "thor",
                "saltamontes",
                "meduza",
                "default");


        final File[] kitFiles = kitsFolder.listFiles();
        if (kitFiles != null) {
            for (final File kitFile : kitFiles) {
                Config config = configManager.of(kitFile);
                if (config != null) {
                    String name = config.getOrDefault("name", "No name");
                    List<String> items = config.getOrDefault("items", Arrays.asList("No items"));
                    List<String> effects = config.getOrDefault("effects", Arrays.asList("No effects"));
                    List<String> lores = config.getOrDefault("lore", Arrays.asList("No lore"));

                    lores.replaceAll(s -> ChatColor.GRAY + "- " + s + ChatColor.RESET);

                    Kit kit = new Kit(name, items, effects, lores);
                    kitManager.addKit(name, kit);
                }
            }
        }

        getServer().getPluginCommand("kit").setExecutor(new KitCommand(kitManager));
        getServer().getPluginCommand("start").setExecutor(new StartCommand(this));
        getServer().getPluginCommand("rank").setExecutor(new RankCommand());
        final PluginCommand pluginTopCommand = getCommand("top");
        final TopCommand topCommand = new TopCommand();
        pluginTopCommand.setExecutor(topCommand);
        pluginTopCommand.setTabCompleter(topCommand);

        List<Listener> listeners = Arrays.asList(
                new PlayerItemConsumeListener(this, kitManager),
                new PlayerInteractListener(this, kitManager),
                new PlayerDropItemListener(),
                new PlayerRespawnListener(this),
                new PlayerDeathListener(this, kitManager),
                new PlayerJoinListener(this),
                new PlayerQuitListener(this),
                new PlayerMoveListener(this),

                new EntityTargetLivingEntityListener(kitManager, playerAttackManager),
                new EntityDamageByEntityListener(kitManager, playerAttackManager),
                new EntityDamageListener(kitManager),

                new AsyncPlayerChatListener(kitManager),
                new FoodLevelChangeListener(),
                new InventoryClickListener(kitManager),
                new ProjectileHitListener(kitManager),
                new BreakBlockListener(kitManager),
                new PlaceBlockListener()
        );

        listeners.forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));

        GAMESTATE = GameState.PREGAME;
    }

    @Override
    public void onDisable() {
        DatabaseManager.getDatabase().close();

        if (countdownTask != null) {
            countdownTask.cancel();
        }

        if (invincibilityCountdownTask != null) {
            invincibilityCountdownTask.cancel();
        }

        if (checkWinnerCountdownTask != null) {
            checkWinnerCountdownTask.cancel();
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            removeInventory(p);
        }
        topFiles.save();
    }

    private void loadRandomArena(SlimePlugin plugin) {
        final SlimeLoader fileLoader = plugin.getLoader("file");

        try {
            final List<String> worldsName = fileLoader.listWorlds();

            String worldName;
            do {
                worldName = worldsName.get(Constants.RANDOM.nextInt(worldsName.size()));    
            } while (worldName.equals("world"));
            
            final SlimePropertyMap properties = new SlimePropertyMap();
            properties.setString(SlimeProperties.DIFFICULTY, "normal");
            final SlimeWorld slimeWorld = plugin.loadWorld(fileLoader, worldName, false, properties);
            plugin.generateWorld(slimeWorld);
            
            final World world = Bukkit.getWorld(worldName);
            world.setAutoSave(false);
            currentWorld = world;
        } catch (UnknownWorldException | CorruptedWorldException | NewerFormatException | WorldInUseException | IOException e) {
            e.printStackTrace();
        }
    }

    public void startCountdown() {
        if (!isCountdownRunning) {
            isCountdownRunning = true;
            countdownTask = new BukkitRunnable() {
                private int seconds = 120;

                @Override
                public void run() {
                    if (Bukkit.getOnlinePlayers().size() < 2) {
                        cancelCountdown();
                        return;
                    }

                    if (seconds > 0) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.setLevel(seconds);
                        }

                        if (seconds == 120 || seconds == 60) {
                            Bukkit.broadcastMessage(ChatColor.GREEN + "La partida comienza en " + seconds + " segundos.");
                        } else if (seconds == 30 || seconds == 15 || seconds <= 10) {
                            Bukkit.broadcastMessage(ChatColor.GREEN + "La partida comienza en " + seconds + " segundos.");
                            if (seconds <= 3) {
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);
                                }
                            }
                        }
                        seconds--;
                    } else {
                        startGame();
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.setLevel(0); // Restablecer la barra de experiencia a 0
                        }
                        cancel();
                    }
                }
            };
            countdownTask.runTaskTimer(this, 0, 20);
        }
    }

    public void cancelCountdown() {
        if (isCountdownRunning) {
            isCountdownRunning = false;
            if (countdownTask != null) {
                countdownTask.cancel();
                countdownTask = null;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setLevel(0);
            }

            Bukkit.broadcastMessage(ChatColor.RED + "Esperando a más jugadores.");
        }
    }

    public void startGame() {
        if(GAMESTATE == GameState.PREGAME) {
            cancelCountdown();
            isInvincibilityCountdownRunning = false;
            isCountdownRunning = false;
            GAMESTATE = GameState.INVICIBILITY;
            Bukkit.broadcastMessage(ChatColor.GREEN + "¡El juego ha comenzado!");

            for (Player p : Bukkit.getOnlinePlayers()) {
                Location randomLocation = getRandomSpawnLocation();
                p.teleport(randomLocation);
                p.setAllowFlight(false);
                players.add(p);

                if (kitManager.getSelectedKits().stream().noneMatch(entry -> entry.getKey().equals(p))) {
                    removeInventory(p);
                    kitManager.addSelectedKit(p, kitManager.getKit("Default"));
                }
            }

            kitManager.loadKits();
            invincibilityCountdown();
            checkWinner();
        }
    }

    public Location getRandomSpawnLocation() {
        int radius = 10;
        Location spawnLocation = currentWorld.getSpawnLocation();

        double angle = Constants.RANDOM.nextDouble() * 2 * Math.PI; // RADIANES
        double distance = Constants.RANDOM.nextDouble() * radius;

        double newX = spawnLocation.getX() + distance * Math.cos(angle);
        double newZ = spawnLocation.getZ() + distance * Math.sin(angle);

        int highestY = currentWorld.getHighestBlockYAt((int) newX, (int) newZ);

        while (currentWorld.getBlockAt((int) newX, highestY, (int) newZ).getType() == Material.WATER ||
            currentWorld.getBlockAt((int) newX, highestY, (int) newZ).getType() == Material.LAVA) {
            highestY--;
        }

        return new Location(currentWorld, newX, highestY, newZ);
    }


    private void invincibilityCountdown() {
        if(!isInvincibilityCountdownRunning) {
            isInvincibilityCountdownRunning = true;

            invincibilityCountdownTask = new BukkitRunnable() {
                private int seconds = 50;

                @Override
                public void run() {
                    if(players.size() == 1) {
                        cancel();
                    }

                    if(seconds > 0) {
                        if (seconds == 50 || seconds == 40 || seconds == 30 || seconds == 20 || seconds < 11) {
                            Bukkit.broadcastMessage(ChatColor.RED + "La invencibilidad termina en " + seconds + " segundos.");
                            if(seconds < 11) {
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    p.setLevel(seconds);
                                    p.playSound(p.getLocation(), Sound.NOTE_PLING, 1F, 1F);
                                }
                            }
                        }
                    } else {
                        GAMESTATE = GameState.GAME;
                        isInvincibilityCountdownRunning = false;
                        Bukkit.broadcastMessage(ChatColor.RED + "¡La invencibilidad ha terminado!");
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.ANVIL_LAND, 1F, 1F);
                        }
                        cancel();
                    }
                    seconds--;
                }
            };
            invincibilityCountdownTask.runTaskTimer(this, 0, 20);
        }
    }

    private void checkWinner() {
        if (!checkWinnerCountdownRunning) {
            checkWinnerCountdownRunning = true;

            checkWinnerCountdownTask = new BukkitRunnable() {
                private int count = 8;
                private Player winner = null;
                private Location winnerLocation = null;
                private boolean noWinnerMessageSent = false;

                @Override
                public void run() {
                    // Caso cuando queda un solo jugador y aún no se ha declarado un ganador
                    if (players.size() == 1 && winner == null) {
                        winner = players.get(0);
                        winnerLocation = winner.getLocation();

                        if (count == 8) {
                            Bukkit.broadcastMessage(ChatColor.GOLD + "¡" + winner.getName() + " es el ganador!");
                            DatabaseManager.getDatabase().getCached(winner.getUniqueId()).wins++;
                            DatabaseManager.getDatabase().saveAll(Bukkit.getOnlinePlayers());

                        }
                    }

                    // Si hay un ganador, lanzamos fuegos artificiales
                    if (winner != null) {
                        if (winnerLocation != null) {
                            launchFirework(winnerLocation);
                        }

                        if (count > 0) {
                            count--;
                        } else {
                            endGame();
                            cancel();
                        }
                    }

                    // Caso donde no hay jugadores conectados
                    if (players.isEmpty()) {
                        if (winner == null && !noWinnerMessageSent) {
                            Bukkit.broadcastMessage(ChatColor.RED + "No hay un ganador.");
                            noWinnerMessageSent = true;
                        }

                        if (count > 0) {
                            count--;
                        } else {
                            endGame();
                            cancel();
                        }
                    }
                }
            };

            checkWinnerCountdownTask.runTaskTimer(this, 0, 20);
        }
    }

    private void endGame() {
        GAMESTATE = GameState.PREGAME;
        Bukkit.setWhitelist(true);
        checkWinnerCountdownRunning = false;
        players.clear();

        Bukkit.getScheduler().runTaskLater(
            this,
            () -> {
                final Collection<? extends Player> online = Bukkit.getOnlinePlayers();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");

                for (Player player : online) {
                    player.kickPlayer(ChatColor.RED + "El servidor se está reiniciando. ¡Gracias por jugar!");
                }

                players.clear();
            }, 100L);
    }

    public void updatePlayerScore(Player player) {
        final User user = DatabaseManager.getDatabase().getCached(player.getUniqueId());
        final Sidebar sidebar = new Sidebar1_8R3();

        sidebar.setTitle("§6§lCHG");
        sidebar.setLines(sidebar.createLines(new String[] {
            date,
            "",
            "§fAsesinatos: §6" + user.kills,
            "§fMuertes: §c" + user.deaths,
            " ",
            "§fVictorias: §a" + user.wins,
            "  ",
            "§fKDR: §d" + String.format("%.2f", user.getKdr()),
            "   ",
            "§eplay.mine.lc"
        }));
        sidebar.sendLines(player);
        sidebar.sendTitle(player);
    }

    private void launchFirework(Location location) {
        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        FireworkEffect effect = FireworkEffect.builder()
                .withColor(Color.RED, Color.ORANGE, Color.YELLOW) // Colores del fuego artificial
                .with(FireworkEffect.Type.BALL_LARGE) // Tipo de explosion
                .withFlicker() // Añade parpadeo
                .withTrail() // Añade estela
                .build();

        meta.addEffect(effect);
        meta.setPower(1);
        firework.setFireworkMeta(meta);
    }

    public void removeInventory(Player p) {
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        p.setExp(0);

        for (PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean isCountdownRunning() {
        return isCountdownRunning;
    }

    public Map<UUID, Long> getCooldownsMedusa() {
        return cooldownsMedusa;
    }

    public Set<Player> getFrozenPlayers() {
        return frozenPlayers;
    }

    public void removeAllFrozenPlayers() {
        frozenPlayers.clear();
    }
}
