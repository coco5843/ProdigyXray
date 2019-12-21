package fr.cocoraid.prodigyxray;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Created by cocoraid on 20/06/2017.
 */
public class HitFallingBlockListener {

    public HitFallingBlockListener(Plugin plugin) {



        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Client.USE_ENTITY) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player p = event.getPlayer();
                if(p != null) {
                    if(packet.getEntityUseActions().read(0) == EnumWrappers.EntityUseAction.ATTACK) {
                        int id = packet.getIntegers().read(0);
                        if(Xray.getXrayers().containsKey(p.getUniqueId())) {
                            Xray xray = Xray.getXrayers().get(p.getUniqueId());
                            xray.getEntities().keySet().stream().filter(i -> i == id).findFirst().ifPresent(i -> {
                                NMSPlayer.destroyEntity(p,i);
                                NMSPlayer.destroyEntity(p,xray.getEntities().get(i));
                                xray.getSupport().remove(xray.getEntities().get(i));
                                xray.getEntries().keySet().removeIf(fb -> {
                                    if((int)Reflection.getMethod(Reflection.getMinecraftClass("Entity"),"getId").invoke(fb) == i) {
                                        Location l = ((Entity)Reflection.getMethod(Reflection.getMinecraftClass("Entity"),"getBukkitEntity").invoke(fb)).getLocation();
                                        p.sendBlockChange(l,l.getBlock().getType(),l.getBlock().getData());
                                        return true;
                                    } else
                                        return false;
                                });
                            });

                        }

                    }


                }
            }


        });

    }
}
