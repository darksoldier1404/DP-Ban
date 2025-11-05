package com.darksoldier1404.dpban.obj;

import com.darksoldier1404.dppc.data.DataCargo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.util.Date;
import java.util.UUID;

import static com.darksoldier1404.dpban.Ban.plugin;

public class Bans implements DataCargo {
    private UUID uuid;
    private Date banDate;
    private String reason;
    private int duration;
    private boolean isPermanent;
    private BukkitTask task;

    public Bans() {
    }

    public Bans(UUID uuid, Date banDate, String reason, int duration, boolean isPermanent) {
        this.uuid = uuid;
        this.banDate = banDate;
        this.reason = reason;
        this.duration = duration;
        this.isPermanent = isPermanent;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public Date getBanDate() {
        return banDate;
    }

    public void setBanDate(Date banDate) {
        this.banDate = banDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isPermanent() {
        return isPermanent;
    }

    public void setPermanent(boolean permanent) {
        isPermanent = permanent;
    }

    public void initTask() {
        if (task != null) {
            task.cancel();
        }
        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (!isPermanent()) {
                duration--;
                if (duration > 0) {
                    plugin.data.remove(uuid);
                    task.cancel();
                    task = null;
                }
            }
        }, 20L, 20L);
    }

    @Override
    public YamlConfiguration serialize() {
        YamlConfiguration data = new YamlConfiguration();
        data.set("uuid", uuid.toString());
        data.set("banDate", banDate.getTime());
        data.set("reason", reason);
        data.set("duration", duration);
        data.set("isPermanent", isPermanent);
        return data;
    }

    @Override
    public Bans deserialize(YamlConfiguration data) {
        UUID uuid = UUID.fromString(data.getString("uuid"));
        Date banDate = new Date(data.getLong("banDate"));
        String reason = data.getString("reason");
        int duration = data.getInt("duration");
        boolean isPermanent = data.getBoolean("isPermanent");
        return new Bans(uuid, banDate, reason, duration, isPermanent);
    }
}
