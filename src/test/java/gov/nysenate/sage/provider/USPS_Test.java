package gov.nysenate.sage.provider;

import static gov.nysenate.sage.AddressTestBase.*;
import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test the functionality of the USPS adapter by executing a few sample requests and
 * comparing them to the expected responses.
 */
public class USPS_Test extends TestBase
{
    private static Logger logger = Logger.getLogger(USPS_Test.class);
    private USPSAIS uspsais;

    @Before
    public void setUp()
    {
        try {
            this.uspsais = new USPSAIS();
        }
        catch(Exception ex){
            System.err.println(ex.getMessage());
        }
    }

    @Test
    public void singleAddressValidate_ReturnsAddressResult()
    {
        assertSingleAddressValidation(uspsais);
    }

    @Test
    public void multipleAddressValidate_ReturnsAddressResult()
    {
        assertMultiAddressValidation(uspsais);
    }

    @Test
    public void cityStateLookup_ReturnsAddressResult()
    {
        assertCityStateLookup(uspsais);
    }

    @Test
    public void singleInvalidAddressValidate_SetsIsValidatedFalse()
    {
        assertBadAddressValidate(uspsais);
    }

    @Test
    public void test()
    {
        FormatUtil.printObject(uspsais.validate(new Address("200 yellow place", "Rockledge", "FL", "")));
    }

}
