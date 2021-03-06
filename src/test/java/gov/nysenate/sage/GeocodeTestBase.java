package gov.nysenate.sage;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.geo.GeocodeService;
import gov.nysenate.sage.service.geo.RevGeocodeService;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gov.nysenate.sage.AddressTestBase.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.*;

public abstract class GeocodeTestBase
{
    private static Logger logger = Logger.getLogger(GeocodeTestBase.class);
    /** Indicates how far apart two lat or lon values can be and yet considered similar. */
    private static double GEOCODE_EPSILON = 0.01;

    public static ArrayList<Address> addresses = new ArrayList<>(Arrays.asList(
            new Address("214 8th Street", "", "Troy", "NY", "12180", ""),
            new Address("101 East State Street", "", "Olean", "NY", "14760", ""),
            new Address("2012 E Rivr Road", "", "Olean", "NY", "14760", ""),
            new Address("44 Fairlawn Ave", "Apt 2B", "Albany", "NY", "12203", ""),
            new Address("18 Greenhaven Dr", "" ,"Port Jefferson Station", "NY", "11776", ""),
            new Address("479 Deer Park AVE","", "Babylon", "NY", "11702", "")));

    public static ArrayList<Geocode> expectedGeocode = new ArrayList<>(Arrays.asList(
            new Geocode(new Point(42.7352408, -73.6828174), GeocodeQuality.POINT, "Test"),
            new Geocode(new Point(42.0775849, -78.4298556), GeocodeQuality.POINT, "Test"),
            new Geocode(new Point(42.0685706, -78.4138262), GeocodeQuality.POINT, "Test"),
            new Geocode(new Point(42.6716696, -73.7985770), GeocodeQuality.POINT, "Test"),
            new Geocode(new Point(40.9144780, -73.0568423), GeocodeQuality.POINT, "Test"),
            new Geocode(new Point(40.7056276, -73.3219653), GeocodeQuality.POINT, "Test")));

    public static void assertSingleAddressGeocode(GeocodeService geocodeService)
    {
        Address address = new Address("44 Fairlawn Ave", "", "Albany", "NY", "12203", "");
        Point latlon = new Point(42.671669, -73.798577);

        assertSingleAddressGeocode(geocodeService, address, latlon);
    }

    public static void assertSingleAddressGeocode(GeocodeService geocodeService, Address address, Point latlon)
    {
        Geocode expectedCode = new Geocode(latlon, GeocodeQuality.POINT, "TEST");
        GeocodeResult geocodeResult = geocodeService.geocode(address);

        assertNotNull(geocodeResult);
        Geocode actualCode = geocodeResult.getGeocodedAddress().getGeocode();

        /** Make sure the quality is not NOMATCH */
        assertNotSame(GeocodeQuality.NOMATCH, actualCode.getQuality());

        /** Check that the method is not null */
        assertNotNull(actualCode.getMethod());

        /** Check that the latlon pairs are similar to the expected result */
        assertGeocodesAreSimilar(expectedCode, actualCode);

        logger.debug(FormatUtil.toJsonString(geocodeResult));
    }

    public static void assertMultipleAddressGeocode(GeocodeService geocodeService)
    {
        ArrayList<Address> addresses = new ArrayList<>(Arrays.asList(
                new Address("214 8th Street", "", "Troy", "NY", "12180", ""),
                new Address("101 East State Street", "", "Olean", "NY", "14760", ""),
                new Address("2012 E Rivr Road", "", "Olean", "NY", "14760", ""),
                new Address("44 Fairlawn Ave", "Apt 2B", "Albany", "NY", "12203", ""),
                new Address("18 Greenhaven Dr", "" ,"Port Jefferson Station", "NY", "11776", ""),
                new Address("100 Nyroy Dr","", "", "", "", "")));

        ArrayList<Geocode> expectedGeocode = new ArrayList<>(Arrays.asList(
                new Geocode(new Point(42.7352408, -73.6828174), null, null),
                new Geocode(new Point(42.0775849, -78.4298556), null, null),
                new Geocode(new Point(42.0685706, -78.4138262), null, null),
                new Geocode(new Point(42.6716696, -73.7985770), null, null),
                new Geocode(new Point(40.9144780, -73.0568423), null, null),
                new Geocode(new Point(40.7056276, -73.3219653), null, null)));

        ArrayList<GeocodeResult> geocodeResults = geocodeService.geocode(addresses);
        logger.debug(FormatUtil.toJsonString(geocodeResults));

        assertNotNull(geocodeResults);
        assertEquals(6, geocodeResults.size());

        for (int i = 0; i < addresses.size(); i++){
            Geocode geocode = geocodeResults.get(i).getGeocodedAddress().getGeocode();
            assertFalse(GeocodeQuality.NOMATCH == geocode.getQuality());
            assertGeocodesAreSimilar(expectedGeocode.get(i), geocode);
        }
    }

    public static void assertSingleReverseGeocode(RevGeocodeService revGeocodeService)
    {
        /** This is not an accuracy test, but rather a check to see if the address returned is reasonable */
        GeocodeResult geocodeResult = revGeocodeService.reverseGeocode(new Point(42.6716696, -73.7985770));
        assertNotNull(geocodeResult);

        Address expected = new Address("44 Fairlawn Ave", "", "Albany", "NY", "12203", "");
        Address address = geocodeResult.getGeocodedAddress().getAddress();
        assertNotNull(address);
        assertCityStateEquals(expected, address);
        assertZipEquals(expected, address);
    }

    /** Test Error Cases */
    public static void assertNoResultReturnsNoGeocodeResultStatus(GeocodeService geocodeService)
    {
        GeocodeResult geocodeResult = geocodeService.geocode(new Address("BLAH"));
        FormatUtil.printObject(geocodeResult);
        assertEquals(ResultStatus.NO_GEOCODE_RESULT, geocodeResult.getStatusCode());
    }

    public static void assertNullAddressReturnsMissingAddressStatus(GeocodeService geocodeService)
    {
        Address nullAddress = null;
        GeocodeResult geocodeResult = geocodeService.geocode(nullAddress);
        assertEquals(ResultStatus.MISSING_ADDRESS, geocodeResult.getStatusCode());
    }

    public static void assertEmptyAddressReturnsInsufficientAddressStatus(GeocodeService geocodeService)
    {
        Address emptyAddress = new Address();
        GeocodeResult geocodeResult = geocodeService.geocode(emptyAddress);
        assertEquals(ResultStatus.INSUFFICIENT_ADDRESS, geocodeResult.getStatusCode());
    }

    /** Test if geocodes are returning reasonable results but they do not necessarily have
     *  to be exact.
     * @param expected
     * @param actual
     */
    public static void assertGeocodesAreSimilar(Geocode expected, Geocode actual)
    {
        assertTrue((expected.getLat() - actual.getLat()) < GEOCODE_EPSILON);
        assertTrue((expected.getLon() - actual.getLon()) < GEOCODE_EPSILON);
    }
}
