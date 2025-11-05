package com.darksoldier1404.dpban;

import com.darksoldier1404.dppc.annotation.DPPCoreVersion;
import com.darksoldier1404.dppc.data.DPlugin;
import com.darksoldier1404.dppc.data.DataContainer;
import com.darksoldier1404.dppc.data.DataType;
import com.darksoldier1404.dppc.utils.PluginUtil;
import com.darksoldier1404.dpban.commands.DPBCommand;
import com.darksoldier1404.dpban.events.DPBEvent;
import com.darksoldier1404.dpban.obj.Bans;

import java.util.UUID;

@DPPCoreVersion(since = "5.3.0")
public class Ban extends DPlugin {
    public static Ban plugin;
    public static DataContainer<UUID, Bans> data;

    public Ban() {
        super(false);
        plugin = this;
        init();
    }

    @Override
    public void onLoad() {
        PluginUtil.addPlugin(plugin, 27745);
        data = loadDataContainer(new DataContainer<>(this, DataType.CUSTOM, "data"), Bans.class);
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new DPBEvent(), plugin);
        getCommand("dpban").setExecutor(new DPBCommand().getExecutor());
    }

    @Override
    public void onDisable() {
        saveAllData();
    }
}
