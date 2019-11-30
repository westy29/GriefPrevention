package me.ryanhamshire.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created on 11/28/2019.
 *
 * @author RoboMWM
 */
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
public class TestPlayerCreateClaim
{
    Player player = mock(Player.class);
    PlayerInteractEvent event = mock(PlayerInteractEvent.class);

    @BeforeAll
    public void holdTool()
    {
        when(player.getInventory()).thenReturn(mock(PlayerInventory.class));
        when(player.getInventory().getItemInMainHand()).thenReturn(mock(ItemStack.class));
        when(player.getInventory().getItemInMainHand().getType()).thenReturn(Material.GOLDEN_SHOVEL);
        when(player.getTargetBlock(null, 100)).thenReturn(mock(Block.class));
        when(player.getTargetBlock(null, 100).getLocation()).thenReturn(new Location(null, -100, 25, -100), new Location(null, 100, 95, 100));
    }

    @Test
    public void testPlayerCreateClaim()
    {
        System.out.println(player.getInventory().getItemInMainHand().getType());
    }

}
