package org.dice_research.squirrel.components;

import org.junit.*;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class DeduplicatorComponentTest {

    private DeduplicatorComponent deduplicatorComponent = new DeduplicatorComponent();
    @Rule
    public final EnvironmentVariables environmentVariables
        = new EnvironmentVariables();

    @Before
    public void setUp(){
        environmentVariables.set("HOBBIT_RABBIT_HOST","rabbit");

    }

    @Test
    public void testInt() throws Exception{
        deduplicatorComponent.init();
        deduplicatorComponent.run();
        deduplicatorComponent.close();
    }

    @After
    public void finish(){

    }

}
