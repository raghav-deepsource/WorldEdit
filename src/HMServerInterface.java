// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseItemStack;
import java.util.logging.Logger;
import java.util.Random;

/**
 *
 * @author sk89q
 */
public class HMServerInterface extends ServerInterface {
    /**
     * Logger.
     */
    private final Logger logger = Logger.getLogger("Minecraft.WorldEdit");
    /**
     * Random generator.
     */
    private Random random = new Random();
    
    /**
     * Set block type.
     *
     * @param pt
     * @param type
     * @return
     */
    public boolean setBlockType(LocalWorld world, Vector pt, int type) {
        // Can't set colored cloth or crash
        if ((type >= 21 && type <= 34) || type == 36) {
            return false;
        }
        return etc.getServer().setBlockAt(type, pt.getBlockX(), pt.getBlockY(),
                pt.getBlockZ());
    }
    
    /**
     * Get block type.
     *
     * @param pt
     * @return
     */
    public int getBlockType(LocalWorld world, Vector pt) {
        return etc.getServer().getBlockIdAt(pt.getBlockX(), pt.getBlockY(),
                pt.getBlockZ());
    }

    /**
     * Set block data.
     *
     * @param pt
     * @param data
     * @return
     */
    public void setBlockData(LocalWorld world, Vector pt, int data) {
        etc.getServer().setBlockData(pt.getBlockX(), pt.getBlockY(),
                        pt.getBlockZ(), data);
    }

    /**
     * Get block data.
     *
     * @param pt
     * @return
     */
    public int getBlockData(LocalWorld world, Vector pt) {
        return etc.getServer().getBlockData(pt.getBlockX(), pt.getBlockY(),
                pt.getBlockZ());
    }
    
    /**
     * Set sign text.
     *
     * @param pt
     * @param text
     */
    public void setSignText(LocalWorld world, Vector pt, String[] text) {
        Sign signData = (Sign)etc.getServer().getComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (signData == null) {
            return;
        }
        for (byte i = 0; i < 4; i++) {
            signData.setText(i, text[i]);
        }
        signData.update();
    }
    
    /**
     * Get sign text.
     *
     * @param pt
     * @return
     */
    public String[] getSignText(LocalWorld world, Vector pt) {
        Sign signData = (Sign)etc.getServer().getComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (signData == null) {
            return new String[]{"", "", "", ""};
        }
        String[] text = new String[4];
        for (byte i = 0; i < 4; i++) {
            text[i] = signData.getText(i);
        }
        return text;
    }

    /**
     * Gets the contents of chests. Will return null if the chest does not
     * really exist or it is the second block for a double chest.
     *
     * @param pt
     * @return
     */
    public BaseItemStack[] getChestContents(LocalWorld world, Vector pt) {
        ComplexBlock cblock = etc.getServer().getOnlyComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

        BaseItemStack[] items;
        Item[] nativeItems;

        if (cblock instanceof Chest) {
            Chest chest = (Chest)cblock;
            nativeItems = chest.getContents();
        } else {
            return null;
        }

        items = new BaseItemStack[nativeItems.length];

        for (byte i = 0; i < nativeItems.length; i++) {
            Item item = nativeItems[i];
            
            if (item != null) {
                items[i] = new BaseItemStack((short)item.getItemId(),
                        item.getAmount(), (short)item.getDamage());
            }
        }

        return items;
    }

    /**
     * Sets a chest slot.
     *
     * @param pt
     * @param contents
     * @return
     */
    public boolean setChestContents(LocalWorld world, Vector pt,
            BaseItemStack[] contents) {
        
        ComplexBlock cblock = etc.getServer().getOnlyComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

        if (cblock instanceof Chest) {
            Chest chest = (Chest)cblock;
            Item[] nativeItems = new Item[contents.length];
            
            for (int i = 0; i < contents.length; i++) {
                BaseItemStack item = contents[i];
                
                if (item != null) {
                    Item nativeItem =
                        new Item(item.getID(), item.getAmount());
                    nativeItem.setDamage(item.getDamage());
                    nativeItems[i] = nativeItem;
                }
            }
            
            setContents(chest, nativeItems);
        }

        return false;
    }

    /**
     * Clear a chest's contents.
     * 
     * @param pt
     */
    public boolean clearChest(LocalWorld world, Vector pt) {
        ComplexBlock cblock = etc.getServer().getOnlyComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

        if (cblock instanceof Chest) {
            Chest chest = (Chest)cblock;
            chest.clearContents();
            chest.update();
            return true;
        }


        return false;
    }
    
    /**
     * Set the contents of an ItemArray.
     * 
     * @param itemArray
     * @param contents
     */
    private void setContents(ItemArray<?> itemArray, Item[] contents) {
        int size = contents.length;

        for (int i = 0; i < size; i++) {
            if (contents[i] == null) {
                itemArray.removeItem(i);
            } else {
                itemArray.setSlot(contents[i].getItemId(),
                        contents[i].getAmount(), contents[i].getDamage(), i);
            }
        }
    }

    /**
     * Checks if a mob type is valid.
     * 
     * @param type
     * @return
     */
    public boolean isValidMobType(String type) {
        return Mob.isValid(type);
    }

    /**
     * Set mob spawner mob type.
     *
     * @param pt
     * @param mobType
     */
    public void setMobSpawnerType(LocalWorld world, Vector pt, String mobType) {
        ComplexBlock cblock = etc.getServer().getComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

        if (!(cblock instanceof MobSpawner)) {
            return;
        }

        MobSpawner mobSpawner = (MobSpawner)cblock;
        mobSpawner.setSpawn(mobType);
        mobSpawner.update();
    }

    /**
     * Get mob spawner mob type. May return an empty string.
     *
     * @param pt
     * @param mobType
     */
    public String getMobSpawnerType(LocalWorld world, Vector pt) {
        try {
            return MinecraftServerInterface.getMobSpawnerType(pt);
        } catch (Throwable t) {
            logger.severe("Failed to get mob spawner type (do you need to update WorldEdit due to a Minecraft update?): "
                    + t.getMessage());
            return "";
        }
    }

    /**
     * Generate a tree at a location.
     * 
     * @param pt
     * @return
     */
    public boolean generateTree(EditSession editSession, LocalWorld world, Vector pt) {
        try {
            return MinecraftServerInterface.generateTree(editSession, pt);
        } catch (Throwable t) {
            logger.severe("Failed to create tree (do you need to update WorldEdit due to a Minecraft update?): "
                    + t.getMessage());
            return false;
        }
    }

    /**
     * Drop an item.
     *
     * @param pt
     * @param type
     * @param count
     * @param times
     */
    public void dropItem(LocalWorld world, Vector pt, int type, int count, int times) {
        for (int i = 0; i < times; i++) {
            etc.getServer().dropItem(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(),
                    type, count);
        }
    }

    /**
     * Drop an item.
     *
     * @param pt
     * @param type
     * @param count
     * @param times
     */
    public void dropItem(LocalWorld world, Vector pt, int type, int count) {
        etc.getServer().dropItem(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(),
                type, count);
    }

    /**
     * Drop an item.
     *
     * @param pt
     * @param type
     * @param count
     * @param times
     */
    public void dropItem(LocalWorld world, Vector pt, int type) {
        etc.getServer().dropItem(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(),
                type, 1);
    }

    /**
     * Simulate a block being mined.
     * 
     * @param pt
     */
    public void simulateBlockMine(LocalWorld world, Vector pt) {
        int type = getBlockType(world, pt);
        //setBlockType(world, pt, 0);

        if (type == 1) { dropItem(world, pt, 4); } // Stone
        else if (type == 2) { dropItem(world, pt, 3); } // Grass
        else if (type == 7) { } // Bedrock
        else if (type == 8) { } // Water
        else if (type == 9) { } // Water
        else if (type == 10) { } // Lava
        else if (type == 11) { } // Lava
        else if (type == 13) { // Gravel
            dropItem(world, pt, type);

            if (random.nextDouble() >= 0.9) {
                dropItem(world, pt, 318);
            }
        }
        else if (type == 16) { dropItem(world, pt, 263); } // Coal ore
        else if (type == 18) { // Leaves
            if (random.nextDouble() > 0.95) {
                dropItem(world, pt, 6);
            }
        }
        else if (type == 20) { } // Glass
        else if (type == 43) { dropItem(world, pt, 44); } // Double step
        else if (type == 47) { } // Bookshelves
        else if (type == 51) { } // Fire
        else if (type == 52) { } // Mob spawner
        else if (type == 53) { dropItem(world, pt, 5); } // Wooden stairs
        else if (type == 55) { dropItem(world, pt, 331); } // Redstone wire
        else if (type == 56) { dropItem(world, pt, 264); } // Diamond ore
        else if (type == 59) { dropItem(world, pt, 295); } // Crops
        else if (type == 60) { dropItem(world, pt, 3); } // Soil
        else if (type == 62) { dropItem(world, pt, 61); } // Furnace
        else if (type == 63) { dropItem(world, pt, 323); } // Sign post
        else if (type == 64) { dropItem(world, pt, 324); } // Wood door
        else if (type == 67) { dropItem(world, pt, 4); } // Cobblestone stairs
        else if (type == 68) { dropItem(world, pt, 323); } // Wall sign
        else if (type == 71) { dropItem(world, pt, 330); } // Iron door
        else if (type == 73) { dropItem(world, pt, 331, 1, 4); } // Redstone ore
        else if (type == 74) { dropItem(world, pt, 331, 1, 4); } // Glowing redstone ore
        else if (type == 75) { dropItem(world, pt, 76); } // Redstone torch
        else if (type == 78) { } // Snow
        else if (type == 79) { } // Ice
        else if (type == 82) { dropItem(world, pt, 337, 1, 4); } // Clay
        else if (type == 83) { dropItem(world, pt, 338); } // Reed
        else if (type == 89) { dropItem(world, pt, 348); } // Lightstone
        else if (type == 90) { } // Portal
        else if (type != 0) {
            dropItem(world, pt, type);
        }
    }
    
    /**
     * Resolves an item name to its ID.
     * 
     * @param name
     * @return
     */
    public int resolveItem(String name) {
        return etc.getDataSource().getItem(name);
    }
    
    /**
     * Kill mobs in an area.
     * 
     * @param origin
     * @param radius
     * @return
     */
    public int killMobs(LocalWorld world, Vector origin, int radius) {
        int killed = 0;
        
        for (Mob mob : etc.getServer().getMobList()) {
            Vector mobPos = new Vector(mob.getX(), mob.getY(), mob.getZ());
            if (mob.getHealth() > 0
                    && (radius == -1 || mobPos.distance(origin) <= radius)) {
                mob.setHealth(0);
                killed++;
            }
        }
        
        return killed;
    }
}
