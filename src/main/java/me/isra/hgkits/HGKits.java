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

import lc.schematicapi.data.SchematicFile;
import lc.schematicapi.paste.Schematic;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.isra.hgkits.commands.*;
import me.isra.hgkits.config.Config;
import me.isra.hgkits.config.ConfigManager;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.database.DatabaseManager;
import me.isra.hgkits.database.User;
import me.isra.hgkits.listeners.*;
import me.isra.hgkits.managers.FameManager;
import me.isra.hgkits.managers.FinalBattleManager;
import me.isra.hgkits.managers.KitManager;
import me.isra.hgkits.managers.PlayerAttackManager;
import me.isra.hgkits.tops.TopFiles;
import me.isra.hgkits.tops.TopManager;
import me.isra.hgkits.utils.Constants;
import me.isra.hgkits.translate.TranslateManager;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.yaml.snakeyaml.Yaml;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
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

    private BukkitRunnable finalBattleCountdownTask;
    private boolean finalBattleCountdownRunning = false;

    private BukkitRunnable checkWinnerCountdownTask;
    private boolean checkWinnerCountdownRunning = false;

    private BukkitRunnable borderTask;
    private boolean borderTaskRunning = false;

    private BukkitRunnable removingBorderTask;
    private boolean removingBorderRunning = false;

    private final Map<UUID, Long> cooldownsMedusa = new HashMap<>();
    private final Set<Player> frozenPlayers = new HashSet<>();
    private TopFiles topFiles;

    @Getter
    public World currentWorld;
    @Getter
    private Permission permission;
    @Getter
    private String date;

    private TranslateManager translateManager;
    @Getter
    private FinalBattleManager finalBattleManager;


    @Override
    public void onEnable() {
        if (!(Bukkit.getPluginManager().getPlugin("SlimeWorldManager") instanceof SlimePlugin slimePlugin)) {
            getLogger().warning("Plugin can't start because don't found slimeworldmanager");
            return;
        }

        loadRandomArena(slimePlugin);
        saveDefaultConfig();
        FileConfiguration soundConfig = getConfig();
        soundConfig.addDefault("sounds.anvil_land.volume", 1.0F);
        soundConfig.addDefault("sounds.anvil_land.pitch", 1.0F);
        soundConfig.addDefault("sounds.orb_pickup.volume", 1.0F);
        soundConfig.addDefault("sounds.orb_pickup.pitch", 1.0F);
        soundConfig.addDefault("sounds.note_pling.volume", 1.0F);
        soundConfig.addDefault("sounds.note_pling.pitch", 1.0F);
        soundConfig.addDefault("sounds.startsound.type", "ENDERDRAGON_GROWL");
        soundConfig.addDefault("sounds.startsound.volume", 1.0F);
        soundConfig.addDefault("sounds.startsound.pitch", 2.0F);
        soundConfig.options().copyDefaults(true);
        saveConfig();

        RegisteredServiceProvider<Permission> provider = getServer().getServicesManager().getRegistration(Permission.class);

        if(provider != null)
            permission = provider.getProvider();

        
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
                "medusa",
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

                    lores.replaceAll(s -> ChatColor.GRAY + s + ChatColor.RESET);

                    Kit kit = new Kit(name, items, effects, lores);
                    kitManager.addKit(name, kit);
                }
            }
        }

        new HGExpansion().register();
        finalBattleManager = new FinalBattleManager(this);

        getConfig().addDefault("chat.format", "[%fame%] <%kit% %player%> %message%");
        getConfig().options().copyDefaults(true);
        saveConfig();

        getConfig().addDefault("chat.format", "[%fame%] <%kit% %player%> %message%");
        getConfig().addDefault("chat.hover.text", Arrays.asList("&aFame Rank: &7%fame%", "&aKit: &7%kit%", "&aPlayer: &7%player%"));
        getConfig().addDefault("scoreboard.title", "&6&lCHG");
        getConfig().addDefault("scoreboard.text", Arrays.asList("%date%", "", "&fAsesinatos: &6%kills%", "&fMuertes: &c%deaths%", " ", "&fVictorias: &a%wins%", "  ", "&fKDR: &d%kdr%", "   ", "&fFama: &b%fame%", "    ", "&eplay.mine.lc"));
        getConfig().options().copyDefaults(true);
        saveConfig();

        translateManager = new TranslateManager(this);
        if (translateManager.getMessage("welcome-message") == null) {
            getLogger().severe("Failed to load messages from messages.yml. Plugin will not start.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("welcome-message")));

        getServer().getPluginCommand("kit").setExecutor(new KitCommand(kitManager, translateManager));
        getServer().getPluginCommand("start").setExecutor(new StartCommand(this));
        getServer().getPluginCommand("rank").setExecutor(new RankCommand(translateManager));
        getServer().getPluginCommand("finalbattle").setExecutor(new FinalBattleCommand(translateManager, this));
        getServer().getPluginCommand("stats").setExecutor(new StatsCommand(this));
        final PluginCommand pluginTopCommand = getCommand("top");
        final TopCommand topCommand = new TopCommand(translateManager);
        pluginTopCommand.setExecutor(topCommand);
        pluginTopCommand.setTabCompleter(topCommand);

        TeamManager teamManager = new TeamManager(this);
        getCommand("team").setExecutor(new TeamCommand(this, teamManager));

        ProjectileHitListener projectileHitListener = new ProjectileHitListener(kitManager);

        List<Listener> listeners = Arrays.asList(
                new PlayerItemConsumeListener(this, kitManager),
                new PlayerInteractListener(this, kitManager , teamManager),
                new PlayerDropItemListener(),
                new PlayerRespawnListener(this),
                new PlayerDeathListener(this, kitManager),
                new PlayerJoinListener(this),
                new PlayerQuitListener(this),
                new PlayerMoveListener(this),

                new EntityTargetLivingEntityListener(kitManager, playerAttackManager),
                new EntityDamageByEntityListener(this, kitManager, playerAttackManager, translateManager, projectileHitListener),
                new EntityDamageListener(this, kitManager),

                new AsyncPlayerChatListener(kitManager, this),
                new FoodLevelChangeListener(),
                new InventoryClickListener(kitManager, translateManager),
                projectileHitListener,
                new BreakBlockListener(kitManager),
                new PlaceBlockListener(),
                new MenuListener()
        );

        listeners.forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));

        GAMESTATE = GameState.PREGAME;

        setupFinalBattle();

        
    }

    private void setupFinalBattle() {
        File schemFile = new File(getDataFolder(), "jaula.copy");

        if(!schemFile.exists()) return;

        try {
            Schematic schematic = new SchematicFile().deserialize(schemFile);

            finalBattleManager.update(schematic, 5);
        }catch (Exception e){
            getLogger().severe("No se pudo cargar la schematic de la Final Battle jaula.copy");
        }
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

        if (finalBattleCountdownTask != null) {
            finalBattleCountdownTask.cancel();
        }

        if(borderTask != null){
            borderTask.cancel();
        }

        if(removingBorderTask != null){
            removingBorderTask.cancel();
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
            world.getWorldBorder().setCenter(world.getSpawnLocation());
            world.getWorldBorder().setSize(400);
            world.getWorldBorder().setWarningDistance(10);
            world.getWorldBorder().setDamageAmount(1);
            world.setPVP(true);

            currentWorld = world;
            world.setGameRuleValue("keepInventory", "false");
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
                            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("countdown-start").replace("%seconds%", String.valueOf(seconds))));
                        } else if (seconds == 30 || seconds == 15 || seconds <= 10) {
                            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("countdown-start").replace("%seconds%", String.valueOf(seconds))));
                            if (seconds <= 3) {
                                float orbPickupVolume = (float) getConfig().getDouble("sounds.orb_pickup.volume");
                                float orbPickupPitch = (float) getConfig().getDouble("sounds.orb_pickup.pitch");
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    p.playSound(p.getLocation(), Sound.ORB_PICKUP, orbPickupVolume, orbPickupPitch);
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

            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("waiting-for-players")));
        }
    }

    public void startGame() {
        if(GAMESTATE == GameState.PREGAME) {
            cancelCountdown();
            isInvincibilityCountdownRunning = false;
            isCountdownRunning = false;
            GAMESTATE = GameState.INVICIBILITY;
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("game-started")));

            float startSoundVolume = (float) getConfig().getDouble("sounds.startsound.volume");
            float startSoundPitch = (float) getConfig().getDouble("sounds.startsound.pitch");
            Sound startSound = Sound.valueOf(getConfig().getString("sounds.startsound.type"));
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), startSound, startSoundVolume, startSoundPitch);
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                Location randomLocation = getRandomSpawnLocation();
                p.teleport(randomLocation);
                p.setAllowFlight(false);
                players.add(p);
                User user = DatabaseManager.getDatabase().getCached(p.getUniqueId());
                user.allKits = false;

                if (kitManager.getSelectedKits().stream().noneMatch(entry -> entry.getKey().equals(p))) {
                    removeInventory(p);
                    kitManager.addSelectedKit(p, kitManager.getKit("Default"));
                }
            }
 
            kitManager.loadKits();
            invincibilityCountdown();
            finalBattleCountdown();
            checkBorder();
            checkWinner();
        }
    }

    private void checkBorder() {
        if(!borderTaskRunning) {
            borderTaskRunning = true;

            borderTask = new BukkitRunnable() {
                int ct = 7;
                @Override
                public void run() {
                    if(ct < 4){
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                                getTranslateManager().getMessage("border-countdown").replace("%minutes%", String.valueOf(ct))));
                    }
                    if(ct <= 0){
                        scheduleBorderRemoving(currentWorld.getWorldBorder());
                        cancel();
                        return;
                    }
                    ct--;
                }

            };

            borderTask.runTaskTimer(this, 60 * 20, 60 * 20);
        }
    }

    private void scheduleBorderRemoving(WorldBorder border) {
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                getTranslateManager().getMessage("reducing-border")));
        for (Player onlinePlayer : Bukkit.getOnlinePlayers())
            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENDERDRAGON_HIT, 1f, 1f);

        if(!removingBorderRunning){
            removingBorderRunning = true;
            removingBorderTask = new BukkitRunnable() {
                final double decrement = 50.0 / 60.0;
                final double minSize = 50.0;

                @Override
                public void run() {
                    double currentSize = border.getSize();

                    if (currentSize <= minSize) {
                        border.setSize(minSize);
                        cancel();
                        return;
                    }

                    border.setSize(currentSize - decrement);
                }

            };
            removingBorderTask.runTaskTimer(this, 0L, 40L);
        }
    }


    private void finalBattleCountdown() {
        if(!finalBattleCountdownRunning) {
            finalBattleCountdownRunning = true;

            finalBattleCountdownTask = new BukkitRunnable() {
                int ct = 10;
                @Override
                public void run() {
                    if(ct == 1){
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                                getTranslateManager().getMessage("fb-countdown").replace("%minutes%", String.valueOf(ct))));
                    }
                    if(ct <= 0){
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers())
                            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENDERDRAGON_DEATH, 1f, 1f);

                        finalBattleManager.createBattle();
                        finalBattleManager.teleportGamers();
                        scheduleFinalEffects();
                        cancel();
                        return;
                    }
                    ct--;
                }

            };

            finalBattleCountdownTask.runTaskTimer(this, 60 * 20, 60 * 20);
        }
    }

    private void scheduleFinalEffects() {
        new BukkitRunnable(){
            @Override
            public void run() {
                if(players.size() > 1) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("wither-final")));
                    for (Player player : players) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 0, false, false));
                    }
                }
            }
        }.runTaskLater(this, 3 * 60 * 20);
        new BukkitRunnable(){
            @Override
            public void run() {
                if(players.size() > 1) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("dragons-final")));
                    scheduleDragons();
                }
            }
        }.runTaskLater(this, 5 * 60 * 20);
    }

    private void scheduleDragons() {
        new BukkitRunnable(){
            @Override
            public void run() {
                if(players.size() > 1) {
                    currentWorld.spawnEntity(getRandomSpawnLocation(), EntityType.ENDER_DRAGON);
                }
            }
        }.runTaskTimer(this, 0, 20 * 60);
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
                            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("invincibility-end").replace("%seconds%", String.valueOf(seconds))));
                            if(seconds < 11) {
                                float notePlingVolume = (float) getConfig().getDouble("sounds.note_pling.volume");
                                float notePlingPitch = (float) getConfig().getDouble("sounds.note_pling.pitch");
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    p.setLevel(seconds);
                                    p.playSound(p.getLocation(), Sound.NOTE_PLING, notePlingVolume, notePlingPitch);
                                }
                            }
                        }
                    } else {
                        GAMESTATE = GameState.GAME;
                        isInvincibilityCountdownRunning = false;
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("invincibility-ended")));
                        float anvilLandVolume = (float) getConfig().getDouble("sounds.anvil_land.volume");
                        float anvilLandPitch = (float) getConfig().getDouble("sounds.anvil_land.pitch");
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.ANVIL_LAND, anvilLandVolume, anvilLandPitch);
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
                private User uwinner = null;
                private Location winnerLocation = null;
                private boolean noWinnerMessageSent = false;

                @Override
                public void run() {
                    // Caso cuando queda un solo jugador y aún no se ha declarado un ganador
                    if (players.size() == 1 && winner == null) {
                        winner = players.get(0);
                        winnerLocation = winner.getLocation();
                        uwinner = DatabaseManager.getDatabase().getCached(winner.getUniqueId());
                        TopManager.calculateWins(uwinner);

                        if (count == 8) {
                            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("winner-announcement").replace("%winner%", winner.getName())));
                            uwinner.allKits = true;
                            uwinner.wins++;
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
                            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("no-winner")));
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
                    player.kickPlayer(ChatColor.translateAlternateColorCodes('&', translateManager.getMessage("server-restarting")));
                }

                players.clear();
            }, 100L);
    }
/*
    public void updatePlayerScore(Player player) {
        final User user = DatabaseManager.getDatabase().getCached(player.getUniqueId());
        Sidebar sidebar  = new Sidebar1_8R3();

        String title = ChatColor.translateAlternateColorCodes('&', getConfig().getString("scoreboard.title", "&6&lCHG"));
        List<String> lines = getConfig().getStringList("scoreboard.text");

        int playerCount = Bukkit.getOnlinePlayers().size();
        if (GAMESTATE == GameState.PREGAME || GAMESTATE == GameState.INVICIBILITY || GAMESTATE == GameState.GAME) {
            playerCount = getPlayers().size();
        }

        String rankColor = FameManager.getRankColor(user.fame);
        String rank = FameManager.getRankByFame(user.fame);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i)
                    .replace("%date%", date)
                    .replace("%kills%", String.valueOf(user.kills))
                    .replace("%deaths%", String.valueOf(user.deaths))
                    .replace("%wins%", String.valueOf(user.wins))
                    .replace("%kdr%", user.getFormattedKDR())
                    .replace("%fame%", String.valueOf(user.fame))
                    .replace("%nivel%", rank)
                    .replace("%nivel-color%", rankColor)
                    .replace("%count%", playerCount == 0 ? "-" : String.valueOf(playerCount));

            if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                line = PlaceholderAPI.setPlaceholders(player, line);
            }

            lines.set(i, ChatColor.translateAlternateColorCodes('&', line));
        }

        sidebar.setTitle(title);
        sidebar.setLines(sidebar.createLines(lines.toArray(new String[0])));
        sidebar.sendLines(player);
        sidebar.sendTitle(player);
    }
*/
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

    public TranslateManager getTranslateManager() {
        return translateManager;
    }

    public static HGKits getInstance() {
        return JavaPlugin.getPlugin(HGKits.class);
    }

}
