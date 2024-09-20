package me.isra.hgkits.tops;

import java.io.File;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;

import org.apache.commons.lang.StringUtils;
import me.isra.hgkits.utils.IntegerUtils;

public final class TopFiles {
    private final int amountTops;
    private final File folder;
    
    public TopFiles(final File folder, final int amountTops) {
        this.folder = folder;
        this.amountTops = amountTops;
    }
    
    public void start() {
        TopStorage.set(new TopStorage(this.read("kills"), this.read("deaths")));
    }
    
    private Top read(final String name) {
        return this.readTop(new File(this.folder, name + ".csv"));
    }
    
    private void save(final String name, final Top top) {
        this.saveTop(new File(this.folder, name + ".csv"), top);
    }
    
    public void save() {
        if (!this.folder.exists()) {
            return;
        }
        this.save("kills", TopStorage.kills());
        this.save("deaths", TopStorage.deaths());
    }
    
    public void saveTop(final File file, final Top top) {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < this.amountTops && top.getPlayers()[i] != null; ++i) {
                builder.append(top.getPlayers()[i].name);
                builder.append(',');
                builder.append(top.getPlayers()[i].value);
                builder.append('\n');
            }
            writer.write(builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Top readTop(final File file) {
        if (!file.exists()) {
            return new Top(new Top.Player[this.amountTops]);
        }

        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int i = 0;
            final Top top = new Top(new Top.Player[this.amountTops]);
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] split = StringUtils.split(line, ',');
                top.getPlayers()[i++] = new Top.Player(split[0], IntegerUtils.parsePositive(split[1], 0));
            }            
            return top;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}