package me.ryanhamshire.GriefPrevention.claim;

import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * Created on 11/25/2019.
 *
 * @author RoboMWM
 */
public class TestClaim
{
    private Location lowerBoundry = new Location(null, -100, 25, -100);
    private Location upperBoundry = new Location(null, 100, 95, 100);
    private Location inside = new Location(null, 0, 40, 0);
    private UUID uuid = UUID.randomUUID();
    private Long time = System.currentTimeMillis();
    private Claim claim = new Claim(lowerBoundry, upperBoundry, uuid, null, time);

    @Test
    private void testCreateClaim()
    {
        assert claim.getLesserBoundaryCorner().equals(lowerBoundry);
        assert claim.getGreaterBoundaryCorner().equals(upperBoundry);
        assert claim.getOwnerUUID().equals(uuid);
        assert claim.getID().equals(time);
        assert claim.contains(inside);
    }
}
