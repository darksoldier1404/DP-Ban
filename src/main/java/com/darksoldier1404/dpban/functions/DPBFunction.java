package com.darksoldier1404.dpban.functions;

import com.darksoldier1404.dppc.api.inventory.DInventory;
import com.darksoldier1404.dppc.utils.ColorUtils;
import com.darksoldier1404.dpban.obj.Bans;
import com.darksoldier1404.dppc.utils.NBT;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

import static com.darksoldier1404.dpban.Ban.*;

public class DPBFunction {

    public static void syncBans() {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        List<UUID> toSave = new ArrayList<>();
        for (OfflinePlayer p : Bukkit.getBannedPlayers()) {
            String name = p.getName();
            if (name == null) continue;
            BanEntry entry = banList.getBanEntry(name);
            if (entry == null) continue;
            UUID uuid = p.getUniqueId();
            if (isBanned(uuid)) continue;
            String reason = entry.getReason();
            Date expiration = entry.getExpiration();
            boolean isPermanent = (expiration == null);
            int duration = 0;
            if (!isPermanent) {
                long diffSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;
                duration = diffSeconds <= 0 ? 0 : (int) diffSeconds;
            }
            addBan(uuid, reason, duration, isPermanent);
            toSave.add(uuid);
        }
        for (UUID uuid : toSave) {
            data.save(uuid);
        }
    }

    public static void initAllTask() {
        for (Bans ban : data.values()) {
            if (!ban.isPermanent()) {
                ban.initTask();
            }
        }
    }

    public static boolean isBanned(UUID uuid) {
        return data.containsKey(uuid);
    }

    public static boolean isPermanentBan(UUID uuid) {
        if (!isBanned(uuid)) return false;
        return data.get(uuid).isPermanent();
    }

    public static void addBan(UUID uuid, String reason, int duration, boolean isPermanent) {
        data.put(uuid, new Bans(uuid, new Date(), reason, duration, isPermanent));
        if (!isPermanent) {
            data.get(uuid).initTask();
        }
    }

    public static void removeBan(UUID uuid) {
        data.remove(uuid);
    }

    public static ItemStack getPlayerHead(OfflinePlayer p) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        sm.setOwningPlayer(p);
        head.setItemMeta(sm);
        return head;
    }

    public static void openBanGUI(Player p) {
        DInventory inv = new DInventory("Ban GUI", 54, true, true, plugin);
        inv.applyDefaultPageTools();
        Map<Integer, ItemStack[]> pageItems = new HashMap<>();
        ItemStack[] items = new ItemStack[45];
        int index = 0;
        for (Bans ban : data.values()) {
            if (index >= 45) {
                pageItems.put(pageItems.size(), items);
                items = new ItemStack[45];
                index = 0;
            }
            ItemStack head = getPlayerHead(Bukkit.getOfflinePlayer(ban.getUUID()));
            String reason = ban.getReason();
            String duration = ban.isPermanent() ? "Permanent" : ban.getDuration() + "";
            String banDate = String.format("%tF %tT", ban.getBanDate(), ban.getBanDate());
            ItemMeta meta = head.getItemMeta();
            meta.setDisplayName(ColorUtils.applyColor("&c&lBanned Player: &e" + Bukkit.getOfflinePlayer(ban.getUUID()).getName()));
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.applyColor("&7Reason: &f" + reason));
            lore.add(ColorUtils.applyColor("&7Duration: &f" + duration));
            lore.add(ColorUtils.applyColor("&7Ban Date: &f" + banDate));
            meta.setLore(lore);
            head.setItemMeta(meta);
            inv.addItem(NBT.setStringTag(head, "dppc_clickcancel", "true"));
            index++;
        }
        inv.setPageItems(pageItems);
        inv.update();
        inv.applyChanges();
        inv.openInventory(p);
    }

    public static void banPlayer(CommandSender p, String playerName, String reason) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (isBanned(target.getUniqueId())) {
            p.sendMessage(plugin.getPrefix() + "§c해당 플레이어는 이미 밴 상태입니다.");
            return;
        }
        addBan(target.getUniqueId(), reason, 0, true);
        data.save(target.getUniqueId());
        p.sendMessage(plugin.getPrefix() + "§a플레이어 §e" + playerName + "§a님을 밴하였습니다. 사유: §e" + reason);
        Bukkit.getBanList(BanList.Type.NAME).addBan(playerName, reason, null, "DP-Ban");
        if (target.isOnline()) {
            Player onlinePlayer = target.getPlayer();
            onlinePlayer.kickPlayer(ColorUtils.applyColor("&cYou have been permanently banned from this server.\n\n&7Reason: &f" + reason));
        }
    }

    public static void unbanPlayer(CommandSender p, String playerName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (!isBanned(target.getUniqueId())) {
            p.sendMessage(plugin.getPrefix() + "§c해당 플레이어는 밴 상태가 아닙니다.");
            return;
        }
        removeBan(target.getUniqueId());
        data.save(target.getUniqueId());
        p.sendMessage(plugin.getPrefix() + "§a플레이어 §e" + playerName + "§a님의 밴을 해제하였습니다.");
    }

    public static void tempBanPlayer(CommandSender p, String playerName, String duration, String reason) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (isBanned(target.getUniqueId())) {
            p.sendMessage(plugin.getPrefix() + "§c해당 플레이어는 이미 밴 상태입니다.");
            return;
        }
        int durationSeconds = parseDuration(duration);
        if (durationSeconds <= 0) {
            p.sendMessage(plugin.getPrefix() + "§c유효한 기간을 입력해주세요.");
            return;
        }
        addBan(target.getUniqueId(), reason, durationSeconds, false);
        data.save(target.getUniqueId());
        p.sendMessage(plugin.getPrefix() + "§a플레이어 §e" + playerName + "§a님을 임시 밴하였습니다. 사유: §e" + reason + " §a기간: §e" + duration);
        Bukkit.getBanList(BanList.Type.NAME).addBan(playerName, reason, new Date(System.currentTimeMillis() + (durationSeconds * 1000L)), "DP-Ban");
        if (target.isOnline()) {
            Player onlinePlayer = target.getPlayer();
            onlinePlayer.kickPlayer(ColorUtils.applyColor("&cYou have been temporarily banned from this server.\n\n&7Reason: &f" + reason + "\n&7Duration: &f" + duration));
        }
    }

    private static int parseDuration(String duration) {
        int totalSeconds = 0;
        StringBuilder number = new StringBuilder();
        for (char c : duration.toCharArray()) {
            if (Character.isDigit(c)) {
                number.append(c);
            } else {
                if (number.length() == 0) continue;
                int value = Integer.parseInt(number.toString());
                switch (c) {
                    case 'd':
                        totalSeconds += value * 86400;
                        break;
                    case 'h':
                        totalSeconds += value * 3600;
                        break;
                    case 'm':
                        totalSeconds += value * 60;
                        break;
                    case 's':
                        totalSeconds += value;
                        break;
                    default:
                        break;
                }
                number.setLength(0);
            }
        }
        return totalSeconds;
    }
}
