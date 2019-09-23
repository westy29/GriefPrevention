package me.ryanhamshire.GriefPrevention.config;

import me.ryanhamshire.GriefPrevention.mock.MockConfigNode;
import org.junit.Before;
import org.junit.Test;

/**
 * Created on 9/22/2019.
 *
 * @author RoboMWM
 */
public class TestConfigNode
{
    private MockConfigNode configNode;

    @Before
    public void createConfigNode()
    {
        configNode = new MockConfigNode();
    }

    @Test
    public void testGet()
    {
        assert configNode.INTEGER_TEST.getValue() == 43110;
        assert configNode.STRING_TEST.getValue().equals("hello world");
    }
}
