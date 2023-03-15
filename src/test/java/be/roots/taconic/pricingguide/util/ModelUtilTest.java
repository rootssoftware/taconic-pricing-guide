package be.roots.taconic.pricingguide.util;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModelUtilTest {

    @Test
    public void stripGenderFromCategoryCode() {

        assertNull(ModelUtil.stripGenderFromCategoryCode(null));
        assertNull(ModelUtil.stripGenderFromCategoryCode(""));

        assertEquals("1349-RD1 MPF WT/WT", ModelUtil.stripGenderFromCategoryCode("1349-RD1-M MPF WT/WT").category());
        assertTrue(ModelUtil.stripGenderFromCategoryCode("1349-RD1-M MPF WT/WT").male());

    }

    @Test
    public void getGenderSpecifcFromCategoryCode() {

        assertNull(ModelUtil.getGenderSpecifcFromCategoryCode(null, true));
        assertNull(ModelUtil.getGenderSpecifcFromCategoryCode("", true));

        assertEquals("1349-RD1-M MPF WT/WT", ModelUtil.getGenderSpecifcFromCategoryCode("1349-RD1 MPF WT/WT", true));
        assertEquals("1349-RD1-F MPF WT/WT", ModelUtil.getGenderSpecifcFromCategoryCode("1349-RD1 MPF WT/WT", false));

    }
}