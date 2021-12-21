package be.roots.taconic.pricingguide.pdfdomain;

import be.roots.taconic.pricingguide.domain.HealthStatus;
import be.roots.taconic.pricingguide.domain.Pricing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PDFCategoryTest {

    @Test
    public void test_getNameAsText() {
        final Pricing pricing = new Pricing();
        pricing.setCategory("B6-M MPF");
        pricing.setHealthstatus(HealthStatus.MPF);
        final PDFCategory pdfCategory = new PDFCategory(pricing);

        assertEquals("B6 Male MPF (Murine Pathogen Free<sup>TM</sup>)", pdfCategory.getNameAsTitle());

        pricing.setCategory("B6-F MPF");
        assertEquals("B6 Female MPF (Murine Pathogen Free<sup>TM</sup>)", pdfCategory.getNameAsTitle());
    }

}