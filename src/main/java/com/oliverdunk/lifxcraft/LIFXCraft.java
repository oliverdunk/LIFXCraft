package com.oliverdunk.lifxcraft;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Iterator;

public class LIFXCraft extends JavaPlugin{

    private int lastLevel;
    private Socket socket;

    public void onEnable(){
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
            public void run() {
                Iterator<? extends Player> playerIterator = Bukkit.getOnlinePlayers().iterator();
                if(!playerIterator.hasNext()) return;
                Player player = playerIterator.next();
                changeLightLevel(player.getLocation().getBlock().getLightLevel());
            }
        }, 0, 20);

        try {
            socket = new Socket("127.0.0.1", 32069);
        }catch(Exception ex){
            this.getLogger().info("Unable to open connection to lighsd.");
            Bukkit.shutdown();
        }

        this.getLogger().info("LIFXCraft has been initialized.");
    }

    public void changeLightLevel(int level){
        if(level == lastLevel) return;
        lastLevel = level;

        //Don't ever set the light level to 0, because this causes the light to turn off.
        if(level == 0) level = 1;
        try{
            //Don't specify an ID parameter to make the payload a notification rather than a request.
            JSONObject payload = new JSONObject().put("jsonrpc", "2.0");
            JSONArray args = new JSONArray().put("*").put(360).put(1).put(((1D / 15D) * level)).put(2500).put(500);
            payload = payload.put("method", "set_light_from_hsbk").put("params", args);

            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            writer.write(payload.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
