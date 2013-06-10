package gov.nysenate.sage.client.view;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;

import java.util.ArrayList;
import java.util.List;

public class DistrictView
{
    protected String name;
    protected String district;

    public DistrictView(DistrictType districtType, DistrictInfo districtInfo)
    {
        if (districtInfo != null) {
            this.name = districtInfo.getDistName(districtType);
            this.district = districtInfo.getDistCode(districtType);
        }
    }

    public String getName() {
        return name;
    }

    public String getDistrict() {
        return district;
    }
}
