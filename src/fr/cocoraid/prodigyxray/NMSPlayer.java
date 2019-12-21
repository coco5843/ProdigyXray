package fr.cocoraid.prodigyxray;



import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by cocoraid on 01/07/2016.
 */
public class NMSPlayer {


    private static Class<?> packetDestroyClass = Reflection.getMinecraftClass("PacketPlayOutEntityDestroy");

    private static Reflection.FieldAccessor<?> playerConnectionField = Reflection.getField("{nms}.EntityPlayer", "playerConnection", Object.class);
    private static Reflection.FieldAccessor<?> packetDestroyIDField = Reflection.getField(packetDestroyClass, Object.class, 0);

    private static Reflection.MethodInvoker getHandleMethod = Reflection.getMethod("{obc}.entity.CraftPlayer", "getHandle");
    private static Reflection.MethodInvoker sendPacket = Reflection.getMethod("{nms}.PlayerConnection","sendPacket", Reflection.getMinecraftClass("Packet"));

    private static Reflection.ConstructorInvoker packetDestroyConstructor = Reflection.getConstructor(packetDestroyClass);
    /**
     * Send packets
     */
    public static void sendPacket(Player player, Object... packets) {
        for(Object packet : packets) {
            if(packet != null)
                sendPacket.invoke(playerConnectionField.get(getHandleMethod.invoke(player)), packet);
        }
    }

    public static void sendPacket(Object... packets) {
        for(Object packet : packets) {
            if(packet != null) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    sendPacket.invoke(playerConnectionField.get(getHandleMethod.invoke(player)), packet);
                });
            }
        }
    }


    public static void destroyEntity(Player player, int... id) {
        Object packet = packetDestroyConstructor.invoke();
        packetDestroyIDField.set(packet, id);
        sendPacket(player, packet);
    }


}
