package com.ghgande.j2mod.modbus;

import com.ghgande.j2mod.modbus.procimg.DefaultProcessImageFactory;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.procimg.ProcessImageFactory;
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;
import org.junit.Assert;
import org.junit.Test;

public final class ModbusCouplerTest {
    private static final int NOIMAGE_UNITID = 102;
    private static final int TESTIMAGE1_UNITID = 5;
    private static final int TESTIMAGE2_UNITID = 10;
    private static final int TESTIMAGE3_UNITID = 82;

    @Test
    public void testIsInitializedAndGetReference() {
        ModbusCoupler test = ModbusCoupler.getReference();
        Assert.assertNotNull("Test instance of ModbusCoupler should not be null.", test);
        Assert.assertTrue("ModbusCoupler instance should be initialized.", ModbusCoupler.isInitialized());
    }

    @Test
    public void testSetGetProcessImageFactory() {
        ProcessImageFactory piFactory = new DefaultProcessImageFactory();
        ModbusCoupler.getReference().setProcessImageFactory(piFactory);
        ProcessImageFactory returnedFactory = ModbusCoupler.getReference().getProcessImageFactory();
        Assert.assertEquals("Test process image factory is not the same as the original.", piFactory, returnedFactory);
    }

    @Test
    public void testSetGetProcessImage() {
        ProcessImage testImage1 = new SimpleProcessImage(TESTIMAGE1_UNITID);
        ProcessImage testImage2 = new SimpleProcessImage(TESTIMAGE2_UNITID);
        ProcessImage testImage3 = new SimpleProcessImage(TESTIMAGE3_UNITID);
        ModbusCoupler modbusCoupler = ModbusCoupler.getReference();
        Assert.assertNull("Should get null as process image when it has not been set.",
                modbusCoupler.getProcessImage(NOIMAGE_UNITID));

        // Test getting/setting a few process images
        modbusCoupler.setProcessImage(testImage1);
        Assert.assertEquals(testImage1, modbusCoupler.getProcessImage(TESTIMAGE1_UNITID));
        modbusCoupler.setProcessImage(testImage2);
        Assert.assertEquals(testImage2, modbusCoupler.getProcessImage(TESTIMAGE2_UNITID));
        modbusCoupler.setProcessImage(testImage3);
        Assert.assertEquals(testImage3, modbusCoupler.getProcessImage(TESTIMAGE3_UNITID));

        // Is the first process image still available?
        Assert.assertEquals("Second attempt to fetch first test image failed.",
                testImage1, modbusCoupler.getProcessImage(TESTIMAGE1_UNITID));

        // Overwrite the second process image with a new instance
        ProcessImage testImageTwo = new SimpleProcessImage(TESTIMAGE2_UNITID);
        modbusCoupler.setProcessImage(testImageTwo);
        Assert.assertEquals(testImageTwo, modbusCoupler.getProcessImage(TESTIMAGE2_UNITID));
    }

}
