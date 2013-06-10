package gov.nysenate.sage.dao;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.dao.provider.DistrictShapefileDao;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.FormatUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertTrue;

public class DistrictShapefileDaoTest extends TestBase
{
    DistrictShapefileDao dsDao;

    @Before
    public void setUp()
    {
        dsDao = new DistrictShapefileDao();
    }

    @Test
    public void getDistrictMapsTest()
    {
        Map<DistrictType, Map<String, DistrictMap>> distMaps = dsDao.getDistrictMapLookup();
        assertTrue(distMaps.containsKey(DistrictType.SENATE));
        assertTrue(distMaps.get(DistrictType.SENATE).containsKey("44"));
    }

    @Test
    public void getDistrictInfoFromPoint()
    {
        DistrictInfo dinfo = (dsDao.getDistrictInfo(new Point(42.74117729798573, -73.66938646729645), DistrictType.getStandardTypes(), false));
        System.out.println(dinfo);
    }

    @Test
    public void getNearbyDistrictsTest()
    {
        Map<String, DistrictMap> neighbors = dsDao.getNearbyDistricts(DistrictType.SENATE, new Point(40.714920, -73.795158), true, 2);
        assertTrue(neighbors.containsKey("14"));
        FormatUtil.printObject(neighbors);
    }

    @Test
    public void setsDistrictProximityTest()
    {
        DistrictInfo dinfo = (dsDao.getDistrictInfo(new Point(40.712681, -73.797050), DistrictType.getStandardTypes(), false));
        FormatUtil.printObject(dinfo.getDistProximity(DistrictType.SENATE));

    }
}
