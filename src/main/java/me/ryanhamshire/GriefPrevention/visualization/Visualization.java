/*
    GriefPrevention Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ryanhamshire.GriefPrevention.visualization;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;

import java.util.ArrayList;
import java.util.ListIterator;

//represents a visualization sent to a player
//FEATURE: to show players visually where claim boundaries are, we send them fake block change packets
//the result is that those players see new blocks, but the world hasn't been changed.  other players can't see the new blocks, either.
public class Visualization
{
    private ArrayList<VisualizationElement> elements = new ArrayList<VisualizationElement>();

    public ArrayList<VisualizationElement> getElements()
    {
        return elements;
    }

    public World getWorld()
    {
        if (elements.isEmpty())
            return null;
        return elements.get(0).getLocation().getWorld();
    }

    //adds a claim's visualization to the current visualization
    //handy for combining several visualizations together, as when visualization a top level claim with several subdivisions inside
    //locality is a performance consideration.  only create visualization blocks for around 100 blocks of the locality
    public void addClaimElements(Claim claim, VisualizationType visualizationType, Location locality)
    {
        Location smallXsmallZ = claim.getLesserBoundaryCorner();
        Location bigXbigZ = claim.getGreaterBoundaryCorner();
        World world = smallXsmallZ.getWorld();
        boolean waterIsTransparent = locality.getBlock().getType() == Material.WATER;

        int smallx = smallXsmallZ.getBlockX();
        int smallz = smallXsmallZ.getBlockZ();
        int bigx = bigXbigZ.getBlockX();
        int bigz = bigXbigZ.getBlockZ();

        BlockData cornerBlockData;
        BlockData accentBlockData;

        ArrayList<VisualizationElement> newElements = new ArrayList<VisualizationElement>();

        switch (visualizationType)
        {
            case Claim:
                cornerBlockData = Material.GLOWSTONE.createBlockData();
                accentBlockData = Material.GOLD_BLOCK.createBlockData();
                break;
            case AdminClaim:
                cornerBlockData = Material.GLOWSTONE.createBlockData();
                accentBlockData = Material.PUMPKIN.createBlockData();
                break;
            default:
                cornerBlockData = Material.REDSTONE_ORE.createBlockData();
                ((Lightable)cornerBlockData).setLit(true);
                accentBlockData = Material.NETHERRACK.createBlockData();
                break;
        }

        //initialize visualization elements without Y values and real storage
        //that will be added later for only the visualization elements within visualization range

        //locality
        int minx = locality.getBlockX() - 75;
        int minz = locality.getBlockZ() - 75;
        int maxx = locality.getBlockX() + 75;
        int maxz = locality.getBlockZ() + 75;

        final int STEP = 10;

        //top line
        newElements.add(new VisualizationElement(new Location(world, smallx, 0, bigz), cornerBlockData, Material.AIR.createBlockData()));
        newElements.add(new VisualizationElement(new Location(world, smallx + 1, 0, bigz), accentBlockData, Material.AIR.createBlockData()));
        for (int x = smallx + STEP; x < bigx - STEP / 2; x += STEP)
        {
            if (x > minx && x < maxx)
                newElements.add(new VisualizationElement(new Location(world, x, 0, bigz), accentBlockData, Material.AIR.createBlockData()));
        }
        newElements.add(new VisualizationElement(new Location(world, bigx - 1, 0, bigz), accentBlockData, Material.AIR.createBlockData()));

        //bottom line
        newElements.add(new VisualizationElement(new Location(world, smallx + 1, 0, smallz), accentBlockData, Material.AIR.createBlockData()));
        for (int x = smallx + STEP; x < bigx - STEP / 2; x += STEP)
        {
            if (x > minx && x < maxx)
                newElements.add(new VisualizationElement(new Location(world, x, 0, smallz), accentBlockData, Material.AIR.createBlockData()));
        }
        newElements.add(new VisualizationElement(new Location(world, bigx - 1, 0, smallz), accentBlockData, Material.AIR.createBlockData()));

        //left line
        newElements.add(new VisualizationElement(new Location(world, smallx, 0, smallz), cornerBlockData, Material.AIR.createBlockData()));
        newElements.add(new VisualizationElement(new Location(world, smallx, 0, smallz + 1), accentBlockData, Material.AIR.createBlockData()));
        for (int z = smallz + STEP; z < bigz - STEP / 2; z += STEP)
        {
            if (z > minz && z < maxz)
                newElements.add(new VisualizationElement(new Location(world, smallx, 0, z), accentBlockData, Material.AIR.createBlockData()));
        }
        newElements.add(new VisualizationElement(new Location(world, smallx, 0, bigz - 1), accentBlockData, Material.AIR.createBlockData()));

        //right line
        newElements.add(new VisualizationElement(new Location(world, bigx, 0, smallz), cornerBlockData, Material.AIR.createBlockData()));
        newElements.add(new VisualizationElement(new Location(world, bigx, 0, smallz + 1), accentBlockData, Material.AIR.createBlockData()));
        for (int z = smallz + STEP; z < bigz - STEP / 2; z += STEP)
        {
            if (z > minz && z < maxz)
                newElements.add(new VisualizationElement(new Location(world, bigx, 0, z), accentBlockData, Material.AIR.createBlockData()));
        }
        newElements.add(new VisualizationElement(new Location(world, bigx, 0, bigz - 1), accentBlockData, Material.AIR.createBlockData()));
        newElements.add(new VisualizationElement(new Location(world, bigx, 0, bigz), cornerBlockData, Material.AIR.createBlockData()));

        //remove any out of range elements
        this.removeElementsOutOfRange(newElements, minx, minz, maxx, maxz);

        //remove any elements outside the claim
        for (int i = 0; i < newElements.size(); i++)
        {
            VisualizationElement element = newElements.get(i);
            if (!ClaimUtils.isWithin(claim, element.getLocation(), true))
            {
                newElements.remove(i--);
            }
        }

        //set Y values and real block information for any remaining visualization blocks
        for (VisualizationElement element : newElements)
        {
            element.setLocation(getVisibleLocation(element.getLocation(), waterIsTransparent));
            element.setRealBlock(element.getLocation().getBlock().getBlockData());
        }

        this.elements.addAll(newElements);
    }

    //removes any elements which are out of visualization range
    public void removeElementsOutOfRange(ArrayList<VisualizationElement> elements, int minx, int minz, int maxx, int maxz)
    {
        ListIterator<VisualizationElement> iterator = elements.listIterator();

        while (iterator.hasNext())
        {
            Location location = iterator.next().getLocation();
            if (location.getX() < minx || location.getX() > maxx || location.getZ() < minz || location.getZ() > maxz)
            {
                iterator.remove();
            }
        }
    }

    //finds a block the player can probably see.  this is how visualizations "cling" to the ground or ceiling
    private Location getVisibleLocation(Location location, boolean waterIsTransparent)
    {
        Block block = location.getBlock();
        BlockFace direction = (isTransparent(block, waterIsTransparent)) ? BlockFace.DOWN : BlockFace.UP;

        while (block.getY() >= 1 &&
                block.getY() < location.getWorld().getMaxHeight() - 1 &&
                (!isTransparent(block.getRelative(BlockFace.UP), waterIsTransparent) || isTransparent(block, waterIsTransparent)))
        {
            block = block.getRelative(direction);
        }

        return block.getLocation();
    }

    //helper method for above.  allows visualization blocks to sit underneath partly transparent blocks like grass and fence
    private boolean isTransparent(Block block, boolean waterIsTransparent)
    {
        Material material = block.getType();

        if (Tag.CARPETS.isTagged(material))
            return false;
        switch (block.getType())
        {
            //Blacklist
            case SNOW:
                return false;
            case WATER:
                return waterIsTransparent;
        }
        return block.getType().isTransparent();
    }
}
