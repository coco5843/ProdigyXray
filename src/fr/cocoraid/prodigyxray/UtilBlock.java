package fr.cocoraid.prodigyxray;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cocoraid on 11/11/2016.
 */
public class UtilBlock {

    public static List<Block> getInRadius(Location loc, double radius) {
        List<Block> blockList = new ArrayList<>();
        int iR = (int)radius + 1;

        for (int x = -iR; x <= iR; x++) {
            for (int z = -iR; z <= iR; z++)
                for (int y = -iR; y <= iR; y++)
                {
                    Block curBlock = loc.getBlock().getRelative(x, y, z);
                    double offset =offset(loc, curBlock.getLocation());
                    if (offset <= radius)
                        blockList.add(curBlock);
                }
        }
        return blockList;
    }

    public static double offset(Location a, Location b) {
        return offset(a.toVector(), b.toVector());
    }

    public static double offset(Vector a, Vector b) {
        return a.subtract(b).length();
    }


}
