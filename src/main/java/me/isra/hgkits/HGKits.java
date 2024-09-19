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
import com.mongodb.client.*;
import me.isra.hgkits.commands.KitCommand;
import me.isra.hgkits.commands.RankCommand;
import me.isra.hgkits.commands.StartCommand;
import me.isra.hgkits.config.Config;
import me.isra.hgkits.config.ConfigManager;
import me.isra.hgkits.config.DatabaseConfig;
import me.isra.hgkits.enums.GameState;
import me.isra.hgkits.data.Kit;
import me.isra.hgkits.listeners.*;
import me.isra.hgkits.managers.DatabaseManager;
import me.isra.hgkits.managers.KitManager;
import me.isra.hgkits.managers.PlayerAttackManager;
import org.bson.Document;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class HGKits extends JavaPlugin {
    public static GameState GAMESTATE;

    private final List<Player> players = new ArrayList<>();

    private final Map<UUID, Scoreboard> scoreboards = new HashMap<>();

    private KitManager kitManager;

    private BukkitRunnable countdownTask;
    private boolean isCountdownRunning = false;

    private BukkitRunnable invincibilityCountdownTask;
    private boolean isInvincibilityCountdownRunning = false;

    private BukkitRunnable checkWinnerCountdownTask;
    private boolean checkWinnerCountdownRunning = false;

    private DatabaseManager databaseManager;

    private ScoreboardManager manager;

    private final Map<UUID, Long> cooldownsMedusa = new HashMap<>();
    private Set<Player> frozenPlayers = new HashSet<>();

    private HashMap<UUID, Integer> asesinatos = new HashMap<>();
    private HashMap<UUID, Integer> fama = new HashMap<>();


    @Override
    public void onEnable() {
        Bukkit.setWhitelist(false);
        DatabaseConfig databaseConfig = new DatabaseConfig(this);
        databaseManager = new DatabaseManager(this, databaseConfig);
        databaseManager.connectToDatabase();
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
        getServer().getPluginCommand("rank").setExecutor(new RankCommand(this));

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

                new AsyncPlayerChatListener(this, kitManager),
                new FoodLevelChangeListener(),
                new InventoryClickListener(kitManager),
                new ProjectileHitListener(kitManager),
                new BreakBlockListener(kitManager),
                new PlaceBlockListener()
        );

        listeners.forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));

        GAMESTATE = GameState.PREGAME;
        loadRandomArena();
        //loadRandomArenaaaa();
    }

    @Override
    public void onDisable() {
        databaseManager.disconnect();

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
    }

    private void loadRandomArenaaaa() {
        //SlimePlugin plugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        //SlimeLoader sqlLoader = plugin.getLoader("file");

        //try {

            //SlimePropertyMap properties = new SlimePropertyMap();

            //properties.setString(SlimeProperties.DIFFICULTY, "normal");

            // Note that this method should be called asynchronously
            //SlimeWorld world = plugin.loadWorld(sqlLoader, "01", properties);

            // This method must be called synchronously
            //plugin.generateWorld(world);
        //} catch (UnknownWorldException | IOException | CorruptedWorldException | NewerFormatException ){
                 //WorldInUseException ex) {
            //this.getLogger().warning(ex.toString());
        //}
    }

    private void loadRandomArena() {
        File pluginWorldsFolder = new File(getDataFolder(), "worlds");
        File[] worldDirs = pluginWorldsFolder.listFiles(File::isDirectory);

        if (worldDirs != null && worldDirs.length > 0) {
            Random random = new Random();
            File selectedWorldDir = worldDirs[random.nextInt(worldDirs.length)];
            File serverWorldFolder = new File(Bukkit.getWorldContainer(), "world1");

            if (serverWorldFolder.exists()) {
                try {
                    deleteDirectoryRecursively(serverWorldFolder);
                    this.getLogger().info(ChatColor.GREEN + "Mundo existente 'world1' eliminado.");
                } catch (IOException e) {
                    this.getLogger().warning("No se pudo eliminar el mundo existente 'world1'.");
                    e.printStackTrace();
                    return;
                }
            }

            try {
                copyWorldFolder(selectedWorldDir, serverWorldFolder);
                this.getLogger().info(ChatColor.GREEN + "Copia del mundo realizada: " + selectedWorldDir.getName());
                World newWorld = Bukkit.createWorld(new WorldCreator("world1").generateStructures(false));
                newWorld.setAutoSave(false);
                this.getLogger().info(ChatColor.GREEN + "Arena cargada: " + selectedWorldDir.getName());

            } catch (Exception e) {
                this.getLogger().warning("No se pudo copiar o cargar el mundo: " + selectedWorldDir.getName());
                e.printStackTrace();
            }
        } else {
            Bukkit.getLogger().warning("No se encontraron mundos en la carpeta HGKits/worlds.");
        }
    }

    private void copyWorldFolder(File source, File target) throws IOException {
        if (!target.exists()) {
            target.mkdirs();
        }

        // Copiar cada archivo y subcarpeta
        for (File file : source.listFiles()) {
            File targetFile = new File(target, file.getName());

            if (file.isDirectory()) {
                // Si es una carpeta, realizar la copia recursiva
                copyWorldFolder(file, targetFile);
            } else {
                // Si es un archivo, copiarlo
                Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void deleteDirectoryRecursively(File directory) throws IOException {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectoryRecursively(file);
                }
            }
        }
        Files.delete(directory.toPath());
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
        World world = Bukkit.getWorld("world1");
        Random random = new Random();
        Location spawnLocation = world.getSpawnLocation();

        double angle = random.nextDouble() * 2 * Math.PI; // RADIANES
        double distance = random.nextDouble() * radius;

        double newX = spawnLocation.getX() + distance * Math.cos(angle);
        double newZ = spawnLocation.getZ() + distance * Math.sin(angle);

        int highestY = world.getHighestBlockYAt((int) newX, (int) newZ);

        while (world.getBlockAt((int) newX, highestY, (int) newZ).getType() == Material.WATER ||
                world.getBlockAt((int) newX, highestY, (int) newZ).getType() == Material.LAVA) {
            highestY--;
        }

        return new Location(world, newX, highestY, newZ);
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
                            updateVictoriesInDatabase(winner);
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

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer(ChatColor.RED + "El servidor se está reiniciando. ¡Gracias por jugar!");
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
        }, 100L);
    }

    public void updatePlayerScore(Player player) {
        MongoCollection<Document> collection = getMongoDatabase().getCollection("users");
        Document query = new Document("id", player.getUniqueId().toString());
        Document playerData = collection.find(query).first();

        if (playerData != null) {
            Scoreboard scoreboard = scoreboards.get(player.getUniqueId());

            if (scoreboard != null) {
                Objective objective = scoreboard.getObjective(ChatColor.GREEN + "Estadisticas");

                if (objective != null) {
                    // Elimina las puntuaciones antiguas relacionadas con el jugador
                    for (String entry : scoreboard.getEntries()) {
                        scoreboard.resetScores(entry);
                    }

                    setScore(objective, "Asesinatos", playerData.getInteger("kills"), 3);
                    setScore(objective, "Muertes", playerData.getInteger("deaths"), 2);
                    setScore(objective, "Victorias", playerData.getInteger("victories"), 1);
                    setScore(objective, "KDR", String.format("%.2f", playerData.getDouble("kdr")), 0);

                    player.setScoreboard(scoreboard);
                }
            }
        }
    }

    private void setScore(Objective objective, String name, Object value, int score) {
        objective.getScore(ChatColor.AQUA + name + ": " + ChatColor.RED + value).setScore(score);
    }

    public void setupScoreboard(Player player) {
        MongoCollection<Document> collection = getMongoDatabase().getCollection("users");

        Document query = new Document("id", player.getUniqueId().toString());
        Document playerData = collection.find(query).first();

        if (playerData != null) {
            manager = Bukkit.getScoreboardManager();
            Scoreboard scoreboard = manager.getNewScoreboard();
            Objective objective = scoreboard.registerNewObjective(ChatColor.GREEN + "Estadisticas", "dummy");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.getScore(ChatColor.AQUA + "Asesinatos: " + ChatColor.RED + playerData.getInteger("kills")).setScore(3);
            objective.getScore(ChatColor.AQUA + "Muertes: " + ChatColor.RED + playerData.getInteger("deaths")).setScore(2);
            objective.getScore(ChatColor.AQUA + "Victorias: " + ChatColor.RED + playerData.getInteger("victories")).setScore(1);
            String kdrFormatted = ChatColor.RED + String.format("%.3f", playerData.getDouble("kdr")); // Limitar a 2 decimales
            objective.getScore(ChatColor.AQUA + "KDR: " + kdrFormatted).setScore(0);
            player.setScoreboard(scoreboard);
            scoreboards.put(player.getUniqueId(), scoreboard);
        }
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

    public void readyToStart() {
        startGame();
    }

    public void createPlayerInDatabase(Player player) {
        databaseManager.createPlayerInDatabase(player);
    }

    public void updateStatsInDatabase(Player playerDead) {
        databaseManager.updateStatsInDatabase(playerDead);
    }

    private void updateVictoriesInDatabase(Player winner) {
        databaseManager.updateVictoriesInDatabase(winner);
    }

    public void updateFameInDatabase(Player player, int famaTotal) {
        databaseManager.updateFameInDatabase(player,famaTotal);
    }

    public CompletableFuture<Integer> getFameFromDatabase(Player player) {
        return databaseManager.getFameFromDatabase(player);
    }

    private MongoDatabase getMongoDatabase() {
        return databaseManager.getMongoDatabase();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean isCountdownRunning() {
        return isCountdownRunning;
    }

    public Map<UUID, Scoreboard> getScoreboards() {
        return scoreboards;
    }

    public Map<UUID, Long> getCooldownsMedusa() {
        return cooldownsMedusa;
    }

    public Set<Player> getFrozenPlayers() {
        return frozenPlayers;
    }

    public HashMap<UUID, Integer> getAsesinatos() {
        return asesinatos;
    }

    public HashMap<UUID, Integer> getFama() {
        return fama;
    }

    public String getCurrentWorld() {
        return "world1";
    }

    public void removeAllFrozenPlayers() {
        frozenPlayers.clear();
    }
}
