package xyz.periodic.backpackpreviews;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.LogManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class BackpackDetector {
    public static String getHypixelId(ItemStack stack) {
        NBTTagCompound hypixelData = ItemNBTHelper.getCompound(stack, "ExtraAttributes", true);
        try {
            String itemId = null;
            if (hypixelData != null) {
                itemId = hypixelData.getString("id");
            }
            if (itemId != null) {
                return itemId;
            } else {
                return "NULL";
            }
        } catch (NullPointerException e) {
            return "NULL";
        }
    }
    public static boolean isBackpack(ItemStack stack) {
        String itemId = getHypixelId(stack);
        return itemId.equals("SMALL_BACKPACK") || itemId.equals("MEDIUM_BACKPACK") || itemId.equals("LARGE_BACKPACK");
    }

    public static int getNumSlots(ItemStack stack) {
        if (isBackpack(stack)) {
            String itemId = getHypixelId(stack);
            if (itemId.equals("SMALL_BACKPACK")) {
                return 9;
            } else if (itemId.equals("MEDIUM_BACKPACK")) {
                return 18;
            } else if (itemId.equals("LARGE_BACKPACK")) {
                return 27;
            } else {
                return 9;
            }
        } else {
            return 0;
        }
    }

    public static String getTagName(ItemStack stack) {
        if (isBackpack(stack)) {
            String itemId = getHypixelId(stack);
            if (itemId.equals("SMALL_BACKPACK")) {
                return "small_backpack_data";
            } else if (itemId.equals("MEDIUM_BACKPACK")) {
                return "medium_backpack_data";
            } else if (itemId.equals("LARGE_BACKPACK")) {
                return "large_backpack_data";
            } else {
                return "small_backpack_data";
            }
        } else {
            return "";
        }
    }

    public static ItemStack getStackInSlot(ItemStack stack, int slot) {
        if (isBackpack(stack) && slot < getNumSlots(stack)) {
            NBTTagCompound hypixelData = ItemNBTHelper.getCompound(stack, "ExtraAttributes", true);
            if (hypixelData != null) {
                byte[] compressedData = hypixelData.getByteArray(getTagName(stack));
                try {
                    NBTTagCompound backpackData = CompressedStreamTools.readCompressed(new ByteArrayInputStream(compressedData));
                    NBTTagList items = backpackData.getTagList("i", Constants.NBT.TAG_COMPOUND);
                    NBTTagCompound item = items.getCompoundTagAt(slot);
                    item.setString("id", Short.toString(item.getShort("id")));
                    NBTTagCompound itemTag = item.getCompoundTag("tag");
                    if (itemTag.hasKey("ench")) {
                        NBTTagList tagList = itemTag.getTagList("ench", Constants.NBT.TAG_COMPOUND);
                        NBTTagCompound enchTag = new NBTTagCompound();
                        enchTag.setShort("id", (short) 16);
                        enchTag.setShort("level", (short) 1);
                        tagList.appendTag(enchTag);
                    }
                    ItemStack itemStack = ItemStack.loadItemStackFromNBT(item);
                    return itemStack;
                } catch (IOException e) {
                    // WOOPS
                    LogManager.getLogger().info("I/O Exception");
                }
            }
            return null;
        } else {
            return null;
        }
    }
}
