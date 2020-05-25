package me.ryanhamshire.GriefPrevention;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created on 11/28/2019.
 *
 * @author RoboMWM
 */
public class TestClaimClerkCreate
{
    ClaimClerk claimClerk = mock(ClaimClerk.class, RETURNS_DEEP_STUBS);
    Player player = mock(Player.class, RETURNS_DEEP_STUBS);
    PlayerInteractEvent event;

    @BeforeEach
    public void holdTool()
    {
        System.out.println("before");
        when(player.getInventory().getItemInMainHand().getType()).thenReturn(Material.GOLDEN_SHOVEL);
        when(player.getTargetBlock(null, 100).getLocation()).thenReturn(new Location(null, -100, 25, -100), new Location(null, 100, 95, 100));
    }

    @Test
    public void testPlayerCreateClaim()
    {
        Location firstCorner = player.getTargetBlock(null, 100).getLocation();
        Location secondCorner = player.getTargetBlock(null, 100).getLocation();
        Claim claim = claimClerk.registerNewClaim(player, firstCorner, secondCorner);
    }

}
