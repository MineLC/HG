package me.isra.hgkits.managers;

import com.mongodb.MongoException;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import me.isra.hgkits.HGKits;
import me.isra.hgkits.config.DatabaseConfig;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    private final HGKits plugin;
    private final DatabaseConfig databaseConfig;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    public DatabaseManager(HGKits plugin, DatabaseConfig databaseConfig) {
        this.plugin = plugin;
        this.databaseConfig = databaseConfig;
    }

    public void connectToDatabase() {
        try {
            MongoCredential credential = MongoCredential.createCredential(
                    databaseConfig.getUsername(),
                    databaseConfig.getName(),
                    databaseConfig.getPassword().toCharArray()
            );

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(
                            new ServerAddress(databaseConfig.getHost(), databaseConfig.getPort())
                    )))
                    .credential(credential)
                    .build();

            this.mongoClient = MongoClients.create(settings);
            this.mongoDatabase = mongoClient.getDatabase(databaseConfig.getName());
            plugin.getLogger().info(ChatColor.GREEN + "Conexión a MongoDB exitosa.");

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                createCollectionIfNotExists(databaseConfig.getCollection());
            }, 100L);

        } catch (MongoException e) {
            plugin.getLogger().severe(ChatColor.RED + "Error al conectar a MongoDB: " + e.getMessage());
        }
    }

    public void createCollectionIfNotExists(String collectionName) {
        boolean collectionExists = this.mongoDatabase.listCollectionNames().into(new ArrayList<>()).contains(collectionName);

        if (!collectionExists) {
            this.mongoDatabase.createCollection(collectionName);
            plugin.getLogger().info("Colección '" + collectionName + "' creada.");
        } else {
            plugin.getLogger().info("La colección '" + collectionName + "' ya existe.");
        }
    }

    public void createPlayerInDatabase(Player player) {
        MongoCollection<Document> usersCollection = this.mongoDatabase.getCollection("users");
        String playerUUID = player.getUniqueId().toString();

        Document playerDoc = usersCollection.find(Filters.eq("id", playerUUID)).first();
        if (playerDoc == null) {
            Document newPlayer = new Document("id", playerUUID)
                    .append("name", player.getName())
                    .append("kills", 0)
                    .append("deaths", 0)
                    .append("victories", 0)
                    .append("kdr", 0.0)
                    .append("fame", 0);
            usersCollection.insertOne(newPlayer);
            plugin.getLogger().info("Nuevo jugador añadido a la base de datos: " + player.getName());
        } else {
            plugin.getLogger().info("El jugador " + player.getName() + " ya está en la base de datos");
        }
    }

    public void updateStatsInDatabase(Player playerDead) {
        MongoCollection<Document> collection = this.mongoDatabase.getCollection("users");

        // Actualiza estadísticas del jugador muerto
        CompletableFuture<Void> updateDeadFuture = CompletableFuture.runAsync(() -> {
            try {
                Document playerDeadDoc = collection.find(Filters.eq("id", playerDead.getUniqueId().toString())).first();

                if (playerDeadDoc != null) {
                    collection.updateOne(Filters.eq("id", playerDead.getUniqueId().toString()), Updates.inc("deaths", 1));
                    updateKdrInDatabase(playerDead, collection);
                }
            }catch (MongoException e) {
                plugin.getLogger().severe("Error de MongoDB al actualizar Muertes para " + playerDead.getName() + ": " + e.getMessage());
            } catch (Exception e) {
                plugin.getLogger().severe("Error inesperado al actualizar Muertes para " + playerDead.getName() + ": " + e.getMessage());
            }
        }).thenRun(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.updatePlayerScore(playerDead);
                plugin.updatePlayerScore(playerDead);
            });
        });


        Player killer = playerDead.getKiller();
        if (killer != null) {
            // Actualiza estadísticas del asesino
            CompletableFuture<Void> updateKillerFuture = CompletableFuture.runAsync(() -> {
                try {
                    Document playerKillerDoc = collection.find(Filters.eq("id", killer.getUniqueId().toString())).first();

                    if (playerKillerDoc != null) {
                        collection.updateOne(Filters.eq("id", killer.getUniqueId().toString()), Updates.inc("kills", 1));
                        updateKdrInDatabase(killer, collection);
                    }
                }catch (MongoException e) {
                    plugin.getLogger().severe("Error de MongoDB al actualizar Asesinatos para " + killer.getName() + ": " + e.getMessage());
                } catch (Exception e) {
                    plugin.getLogger().severe("Error inesperado al actualizar Asesinatos para " + killer.getName() + ": " + e.getMessage());
                }
            }).thenRun(() -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.updatePlayerScore(killer);
                    plugin.updatePlayerScore(killer);
                });
            });

            // Espera a que todas las actualizaciones se completen
            CompletableFuture.allOf(updateDeadFuture, updateKillerFuture).join();
        } else {
            // Si no hay asesino, espera a que la actualización del jugador muerto termine
            updateDeadFuture.join();
        }
    }

    public void updateVictoriesInDatabase(Player player) {
        MongoCollection<Document> collection = this.mongoDatabase.getCollection("users");

        CompletableFuture.runAsync(() -> {
            try {
                Document playerWinnerDoc = collection.find(Filters.eq("id", player.getUniqueId().toString())).first();
                if (playerWinnerDoc != null) {
                    collection.updateOne(Filters.eq("id", player.getUniqueId().toString()), Updates.inc("victories", 1));
                }
            } catch (MongoException e) {
                plugin.getLogger().severe("Error de MongoDB al actualizar Victorias para " + player.getName() + ": " + e.getMessage());
            } catch (Exception e) {
                plugin.getLogger().severe("Error inesperado al actualizar Victorias para " + player.getName() + ": " + e.getMessage());
            }
        }).thenRun(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.updatePlayerScore(player);
            });
        }).join(); // Espera a que la actualización se complete
    }

    private void updateKdrInDatabase(Player player, MongoCollection<Document> collection) {
        CompletableFuture.runAsync(() -> {
            try {
                Document playerDoc = collection.find(Filters.eq("id", player.getUniqueId().toString())).first();
                if (playerDoc != null) {
                    int kills = playerDoc.getInteger("kills", 0);
                    int deaths = playerDoc.getInteger("deaths", 0);
                    double kdr = (deaths == 0) ? kills : (double) kills / deaths;
                    collection.updateOne(Filters.eq("id", player.getUniqueId().toString()), Updates.set("kdr", kdr));
                }
            } catch (MongoException e) {
                plugin.getLogger().severe("Error de MongoDB al actualizar KDR para " + player.getName() + ": " + e.getMessage());
            } catch (Exception e) {
                plugin.getLogger().severe("Error inesperado al actualizar KDR para " + player.getName() + ": " + e.getMessage());
            }
        }).exceptionally(ex -> {
            plugin.getLogger().severe("Error en operación asíncrona de KDR: " + ex.getMessage());
            return null;
        }).join(); // Espera a que la actualización se complete
    }

    public void updateFameInDatabase(Player player, int famaTotal) {
        MongoCollection<Document> collection = this.mongoDatabase.getCollection("users");

        CompletableFuture.runAsync(() -> {
            try {
                Document playerDoc = collection.find(Filters.eq("id", player.getUniqueId().toString())).first();
                if (playerDoc != null) {
                    int currentFame = playerDoc.getInteger("fame", 0);
                    int updatedFame = currentFame + famaTotal;
                    collection.updateOne(Filters.eq("id", player.getUniqueId().toString()), Updates.set("fame", updatedFame));
                }
            } catch (MongoException e) {
                plugin.getLogger().severe("Error de MongoDB al actualizar la fama para " + player.getName() + ": " + e.getMessage());
            } catch (Exception e) {
                plugin.getLogger().severe("Error inesperado al actualizar la fama para " + player.getName() + ": " + e.getMessage());
            }
        }).exceptionally(ex -> {
            plugin.getLogger().severe("Error en operación asíncrona de actualización de fama: " + ex.getMessage());
            return null;
        }).join();
    }

    public CompletableFuture<Integer> getFameFromDatabase(Player player) {
        CompletableFuture<Integer> futureFame = new CompletableFuture<>();
        MongoCollection<Document> collection = mongoDatabase.getCollection("users");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Document playerDoc = collection.find(Filters.eq("id", player.getUniqueId().toString())).first();
                if (playerDoc != null) {
                    int fame = playerDoc.getInteger("fame", 0);
                    futureFame.complete(fame);
                } else {
                    futureFame.complete(0);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error al obtener la fama para " + player.getName() + ": " + e.getMessage());
                futureFame.completeExceptionally(e);
            }
        });

        return futureFame;
    }

    public void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }


}
