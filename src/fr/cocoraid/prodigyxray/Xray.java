package fr.cocoraid.prodigyxray;


import net.minecraft.server.v1_15_R1.EntityArmorStand;
import net.minecraft.server.v1_15_R1.EntityFallingBlock;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.PacketPlayOutSpawnEntity;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Created by cocoraid on 19/06/2017.
 */
public class Xray {


    private static ProdigyXrayConfig config = ProdigyXray.getInstance().getPx();

    private static Map<Material, ChatColor> map = new HashMap<>();
    static {
        map.put(Material.DIAMOND_ORE, ChatColor.AQUA);
        map.put(Material.EMERALD_ORE, ChatColor.GREEN);
        map.put(Material.GOLD_ORE, ChatColor.GOLD);
        map.put(Material.COAL_ORE, ChatColor.BLACK);
        map.put(Material.IRON_ORE, ChatColor.GRAY);
        map.put(Material.LAPIS_ORE, ChatColor.DARK_BLUE);
        map.put(Material.REDSTONE_ORE, ChatColor.DARK_RED);
    }
    private static Class<?> worldClass = Reflection.getMinecraftClass("World");
    private static Class<?> entityClass = Reflection.getMinecraftClass("Entity");
    private static Class<?> entityTypeClass = Reflection.getMinecraftClass("EntityTypes");
    private static Class<?> fbClass = Reflection.getMinecraftClass("EntityFallingBlock");
    private static Class<?> blockClass = Reflection.getMinecraftClass("Block");

    private static Reflection.ConstructorInvoker armorStandCons = Reflection.getConstructor(Reflection.getMinecraftClass("EntityArmorStand"),worldClass, double.class,double.class,double.class);
    private static Reflection.ConstructorInvoker fallingBlockCons = Reflection.getConstructor(fbClass,entityTypeClass,worldClass);


    private static Reflection.MethodInvoker getHandleMethod = Reflection.getMethod("{obc}.CraftWorld","getHandle");
    private static Reflection.MethodInvoker setLocationMethod = Reflection.getMethod(entityClass,"setLocation",double.class,double.class,double.class,float.class,float.class);
    private static Reflection.MethodInvoker setFlagMethod = Reflection.getMethod(entityClass,"setFlag",int.class,boolean.class);
    private static Reflection.MethodInvoker getDataWatcherMethod = Reflection.getMethod(entityClass,"getDataWatcher");
    private static Reflection.MethodInvoker getId = Reflection.getMethod(entityClass,"getId");
    private static Reflection.MethodInvoker setInvisible = Reflection.getMethod(Reflection.getMinecraftClass("EntityArmorStand"),"setInvisible",boolean.class);
    private static Reflection.MethodInvoker getBukkitEntity = Reflection.getMethod(entityClass,"getBukkitEntity");
    private static Reflection.MethodInvoker getBlockData = Reflection.getMethod(blockClass,"getBlockData");
    private static Reflection.MethodInvoker getBlockId = Reflection.getMethod(Reflection.getMinecraftClass("RegistryBlockID"),"getId",Object.class);


    private static Reflection.ConstructorInvoker packetAsCons = Reflection.getConstructor(Reflection.getMinecraftClass("PacketPlayOutSpawnEntityLiving"), Reflection.getMinecraftClass("EntityLiving"));
    private static Reflection.ConstructorInvoker packetFbCons = Reflection.getConstructor(Reflection.getMinecraftClass("PacketPlayOutSpawnEntity"),entityClass, int.class);
    private static Reflection.ConstructorInvoker packetMetaCons = Reflection.getConstructor(Reflection.getMinecraftClass("PacketPlayOutEntityMetadata"), int.class,Reflection.getMinecraftClass("DataWatcher"),boolean.class);
    private static Reflection.ConstructorInvoker mountCons = Reflection.getConstructor(Reflection.getMinecraftClass("PacketPlayOutMount"),entityClass);

    private static Reflection.FieldAccessor passengerField = Reflection.getField(entityClass,"passengers", List.class);
    private static Reflection.FieldAccessor uuidField = Reflection.getField(entityClass,UUID.class, 0);
    private static Reflection.FieldAccessor typeField = Reflection.getField(entityTypeClass,"FALLING_BLOCK", entityTypeClass);
    private static Reflection.FieldAccessor registryID = Reflection.getField(blockClass,"REGISTRY_ID",Reflection.getMinecraftClass("RegistryBlockID"));


    private static Map<UUID, Xray> xrayers = new HashMap<>();

    private Player p;
    private List<Integer> support = new ArrayList<>();
    private Map<Integer, Integer> entities = new HashMap<>();
    private Map<Object,String > entries = new HashMap<>();

    public Xray(Player p) {
        this.p = p;
    }




    private void spawn(Block b) {
        Object w = getHandleMethod.invoke(p.getWorld());
        Location location = b.getLocation().add(0.5,-1.5,0.5);
        Object as = armorStandCons.invoke(w, location.getX(),location.getY(),location.getZ());
        setLocationMethod.invoke(as,location.getX(),location.getY(),location.getZ(),location.getYaw(),location.getPitch());
        setInvisible.invoke(as,true);
        Object metaAs = packetMetaCons.invoke(getId.invoke(as),getDataWatcherMethod.invoke(as),true);
        Object asPacket = packetAsCons.invoke(as);

        Object fb = fallingBlockCons.invoke(typeField.get(entityTypeClass),w);
        setLocationMethod.invoke(fb,location.getX(),location.getY() + 2,location.getZ(),location.getYaw(),location.getPitch());
        setFlagMethod.invoke(fb, 6, true);
        Object block = Reflection.getField(Reflection.getMinecraftClass("Blocks"),b.getType().name(),Reflection.getMinecraftClass("Block")).get(Reflection.getMinecraftClass("Blocks"));
        Object blockdata = getBlockData.invoke(block);
        int id = (int) getBlockId.invoke(registryID.get(blockClass),blockdata);
        Object fbPacket = packetFbCons.invoke(fb,id);
        Object meta = packetMetaCons.invoke(getId.invoke(fb),getDataWatcherMethod.invoke(fb),true);
        ((List)passengerField.get(as)).add(fb);
        Object mount = mountCons.invoke(as);
        NMSPlayer.sendPacket(p,asPacket, fbPacket,mount, meta, metaAs);

        entries.put(fb,b.getType().name());
        support.add((int) getId.invoke(as));
        entities.put((int) getId.invoke(fb), (int) getId.invoke(as));
        ProdigyXray.getInstance().getScoreboard().getTeam(b.getType().name()).addEntry(uuidField.get(fb).toString());

    }

    private void playEffect() {
        xrayers.put(p.getUniqueId(),this);
        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * config.duration, 0, false, false));
        p.playSound(p.getLocation(),Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED,1,2);
    }

    public void start() {
        if(config.buyable && !ProdigyXray.getInstance().economy.has(p,((double)config.cost))) {
            double rest = (double) config.cost - ProdigyXray.getInstance().economy.getBalance(p);
            p.sendMessage(config.money.replace("&","§").replace("%amount",Double.toString(rest)));
            return;
        }

        if(config.buyable) {
            ProdigyXray.getInstance().economy.withdrawPlayer(p,config.cost);
            p.sendMessage(config.withdrawmoney.replace("&","§").replace("%amount",Integer.toString(config.cost)));
        }

        ProdigyXray.getInstance().getCooldown().put(p.getUniqueId(),config.cooldown);
        playEffect();
        UtilBlock.getInRadius(p.getLocation(),config.distance).stream().filter(b -> b.getType() != Material.AIR && map.containsKey(b.getType()) && !config.disabledOre.contains(b.getType().name().toLowerCase().replace("_ore",""))).forEach(b -> {
            spawn(b);
        });


        p.setScoreboard(ProdigyXray.getInstance().getScoreboard());

        new BukkitRunnable() {
            public void run() {
                stop();
                xrayers.remove(p.getUniqueId());
            }
        }.runTaskLater(ProdigyXray.getInstance(),  config.duration * 20L);
    }




    public void start(Material m) {
        if(config.buyable && !ProdigyXray.getInstance().economy.has(p,((double)config.cost))) {
            double rest = (double) config.cost - ProdigyXray.getInstance().economy.getBalance(p);
            p.sendMessage(config.money.replace("&","§").replace("%amount",Double.toString(rest)));
            return;
        }

        if(config.buyable) {
            ProdigyXray.getInstance().economy.withdrawPlayer(p,config.cost);
            p.sendMessage(config.withdrawmoney.replace("&","§").replace("%amount",Integer.toString(config.cost)));
        }


        if(!p.hasPermission("prodigy.xray.cooldown.bypass"))
            ProdigyXray.getInstance().getCooldown().put(p.getUniqueId(),config.cooldown);
        playEffect();


        UtilBlock.getInRadius(p.getLocation(),config.distance).stream().filter(b -> b.getType() != Material.AIR && map.containsKey(b.getType()) && b.getType() == m).forEach(b -> {
            spawn(b);

        });

        p.setScoreboard(ProdigyXray.getInstance().getScoreboard());

        new BukkitRunnable() {
            public void run() {
                stop();
                xrayers.remove(p.getUniqueId());
            }
        }.runTaskLater(ProdigyXray.getInstance(),  config.duration * 20L);
    }


    public void stop() {
        entries.keySet().forEach(fb -> {
            ProdigyXray.getInstance().getScoreboard().getTeam(entries.get(fb)).removeEntry(uuidField.get(fb).toString());
            support.add((int) getId.invoke(fb));
            Location l = ((Entity)getBukkitEntity.invoke(fb)).getLocation();
            p.sendBlockChange(l,l.getBlock().getBlockData());
        });
        int[] array = new int[support.size()];
        for(int i = 0; i < support.size(); i++) array[i] = support.get(i);
        NMSPlayer.destroyEntity(p, array);
    }


    public static Map<Material, ChatColor> getMap() {
        return map;
    }

    public static Map<UUID, Xray> getXrayers() {
        return xrayers;
    }

    public Map<Object, String> getEntries() {
        return entries;
    }

    public List<Integer> getSupport() {
        return support;
    }

    public Map<Integer, Integer> getEntities() {
        return entities;
    }
}
