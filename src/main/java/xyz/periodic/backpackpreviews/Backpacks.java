package xyz.periodic.backpackpreviews;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
//import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod(name=Backpacks.MOD_NAME, modid=Backpacks.MOD_ID, version="0.7", clientSideOnly=true, acceptedMinecraftVersions="[1.8,1.9)")
public class Backpacks {

    public static final String MOD_NAME = "Backpack Previews";
    public static final String MOD_ID = "backpackpreviews";

    public static final ResourceLocation WIDGET_RESOURCE = new ResourceLocation("backpacks", "textures/misc/shulker_widget.png");

    public static boolean useColors, requireShift;

    private ScaledResolution resolution = null;

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        MinecraftForge.EVENT_BUS.register(this);
    }


    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void makeTooltip(ItemTooltipEvent event) {
        if(BackpackDetector.isBackpack(event.itemStack) && GuiScreen.isShiftKeyDown()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (resolution == null) {
                resolution = new ScaledResolution(mc);
            }
            ItemStack currentBox = event.itemStack;

            //IItemHandler capability = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            //assert capability != null;

            int size = BackpackDetector.getNumSlots(currentBox);
            int[] dims = {Math.min(size, 9), Math.max(size / 9, 1)};
            for (int[] testAgainst : TARGET_RATIOS) {
                if (testAgainst[0] * testAgainst[1] == size) {
                    dims = testAgainst;
                    break;
                }
            }
            //LogManager.getLogger().info(Mouse.getX() + " / " + Mouse.getY() + " x" + resolution.getScaleFactor());
            //int currentX = (Mouse.getX() - 5) / resolution.getScaleFactor();
            //int currentY = ((mc.displayHeight - Mouse.getY()) - 5 - (event.toolTip.size() * 10) - (dims[1] * 20)) / resolution.getScaleFactor();
            //int currentX = (int) Math.floor(Mouse.getX() * (1.0 /resolution.getScaleFactor()));
            double protoX = Mouse.getX();
            //protoX += (9.0 / resolution.getScaleFactor());

            double protoY = mc.displayHeight - Mouse.getY();

            //int currentY = (int) Math.ceil((mc.displayHeight - Mouse.getY()) * (1.0 / resolution.getScaleFactor()));
            //currentY -= (event.toolTip.size() * 10) * (1.0 / resolution.getScaleFactor());
            //currentY -= (dims[1] * 20) * (1.0 / resolution.getScaleFactor());

            int currentX = (int) Math.floor(protoX * (1.0 / resolution.getScaleFactor()));
            int currentY = (int) Math.ceil(protoY * (1.0 / resolution.getScaleFactor()));

            currentX += 8;
            //currentY -= event.toolTip.size() * 10;
            currentY -= 27;
            currentY -= dims[1] * 18;


            int texWidth = CORNER * 2 + EDGE * dims[0];

            if (currentY < 0) {
                //currentY =  Mouse.getY() + 4 * 10 + 5;
                //currentY = 0;
                currentY = (int) Math.ceil(protoY * (1.0 / resolution.getScaleFactor()));
                currentY += event.toolTip.size() * 10;
                currentY -= 9;
            }

            int longestLineWidth = 0;
            Iterator tooltipLines = event.toolTip.iterator();

            int currentLine;
            while(tooltipLines.hasNext()) {
                String s = (String)tooltipLines.next();
                currentLine = mc.fontRendererObj.getStringWidth(s);
                //k = mc.g.getStringWidth(s);
                if (currentLine > longestLineWidth) {
                    longestLineWidth = currentLine;
                }
            }

            int l1 = (int) Math.floor((double) Mouse.getX() / resolution.getScaleFactor()) + 12;
            //int i2 = p_drawHoveringText_3_ - 12;
            currentLine = 8;
            if (event.toolTip.size() > 1) {
                currentLine += 2 + (event.toolTip.size() - 1) * 10;
            }

            if (l1 + longestLineWidth > resolution.getScaledWidth()) {
                l1 -= 28 + longestLineWidth;
                currentX = l1 - 5;
            }

            ScaledResolution res = new ScaledResolution(mc);
            int right = currentX + texWidth;
            if (right > res.getScaledWidth())
                currentX -= (right - res.getScaledWidth());

            GlStateManager.pushMatrix();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.color(1F, 1F, 1F);
            GlStateManager.translate(0, 0, 700);
            mc.getTextureManager().bindTexture(WIDGET_RESOURCE);

            RenderHelper.disableStandardItemLighting();

            int color = -1;

            /*if (useColors && ((ItemBlock) currentBox.getItem()).getBlock() instanceof BlockShulkerBox) {
                EnumDyeColor dye = ((BlockShulkerBox) ((ItemBlock) currentBox.getItem()).getBlock()).getColor();
                color = ItemDye.DYE_COLORS[dye.getDyeDamage()];
            }*/


            renderTooltipBackground(mc, currentX, currentY, dims[0], dims[1], color);

            RenderItem render = mc.getRenderItem();

            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableDepth();
            for (int i = 0; i < size; i++) {
                ItemStack itemstack = BackpackDetector.getStackInSlot(currentBox, i);
                int xp = currentX + 6 + (i % 9) * 18;
                int yp = currentY + 6 + (i / 9) * 18;

                if (itemstack != null) {
                    render.renderItemAndEffectIntoGUI(itemstack, xp, yp);
                    render.renderItemOverlays(mc.fontRendererObj, itemstack, xp, yp);
                }

                /*if (!ChestSearchBar.namesMatch(itemstack, ChestSearchBar.text)) {
                    GlStateManager.disableDepth();
                    Gui.drawRect(xp, yp, xp + 16, yp + 16, 0xAA000000);
                }*/
            }

            GlStateManager.disableDepth();
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void renderTooltip(RenderGameOverlayEvent event) {
        //event.type == RenderGameOverlayEvent.ElementType.HOTBAR;
        resolution = event.resolution;
    }

    private static final int[][] TARGET_RATIOS = new int[][] {
            { 1, 1 },
            { 9, 3 },
            { 9, 5 },
            { 9, 6 },
            { 9, 8 },
            { 9, 9 },
            { 12, 9 }
    };

    private static final int CORNER = 5;
    private static final int BUFFER = 1;
    private static final int EDGE = 18;


    public static void renderTooltipBackground(Minecraft mc, int x, int y, int width, int height, int color) {
        mc.getTextureManager().bindTexture(WIDGET_RESOURCE);
        GlStateManager.color(((color & 0xFF0000) >> 16) / 255f,
                ((color & 0x00FF00) >> 8) / 255f,
                (color & 0x0000FF) / 255f);

        RenderHelper.disableStandardItemLighting();

        Gui.drawModalRectWithCustomSizedTexture(x, y,
                0, 0,
                CORNER, CORNER, 256, 256);
        Gui.drawModalRectWithCustomSizedTexture(x + CORNER + EDGE * width, y + CORNER + EDGE * height,
                CORNER + BUFFER + EDGE + BUFFER, CORNER + BUFFER + EDGE + BUFFER,
                CORNER, CORNER, 256, 256);
        Gui.drawModalRectWithCustomSizedTexture(x + CORNER + EDGE * width, y,
                CORNER + BUFFER + EDGE + BUFFER, 0,
                CORNER, CORNER, 256, 256);
        Gui.drawModalRectWithCustomSizedTexture(x, y + CORNER + EDGE * height,
                0, CORNER + BUFFER + EDGE + BUFFER,
                CORNER, CORNER, 256, 256);
        for (int row = 0; row < height; row++) {
            Gui.drawModalRectWithCustomSizedTexture(x, y + CORNER + EDGE * row,
                    0, CORNER + BUFFER,
                    CORNER, EDGE, 256, 256);
            Gui.drawModalRectWithCustomSizedTexture(x + CORNER + EDGE * width, y + CORNER + EDGE * row,
                    CORNER + BUFFER + EDGE + BUFFER, CORNER + BUFFER,
                    CORNER, EDGE, 256, 256);
            for (int col = 0; col < width; col++) {
                if (row == 0) {
                    Gui.drawModalRectWithCustomSizedTexture(x + CORNER + EDGE * col, y,
                            CORNER + BUFFER, 0,
                            EDGE, CORNER, 256, 256);
                    Gui.drawModalRectWithCustomSizedTexture(x + CORNER + EDGE * col, y + CORNER + EDGE * height,
                            CORNER + BUFFER, CORNER + BUFFER + EDGE + BUFFER,
                            EDGE, CORNER, 256, 256);
                }

                Gui.drawModalRectWithCustomSizedTexture(x + CORNER + EDGE * col, y + CORNER + EDGE * row,
                        CORNER + BUFFER, CORNER + BUFFER,
                        EDGE, EDGE, 256, 256);
            }
        }

        GlStateManager.color(1F, 1F, 1F);
    }

}