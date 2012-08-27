package gov.nysenate.sage.api.methods;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.api.exceptions.ApiException;
import gov.nysenate.sage.api.exceptions.ApiFormatException;
import gov.nysenate.sage.api.exceptions.ApiInternalException;
import gov.nysenate.sage.api.exceptions.ApiTypeException;
import gov.nysenate.sage.model.ApiExecution;
import gov.nysenate.sage.model.ErrorResponse;
import gov.nysenate.sage.model.Point;
import gov.nysenate.sage.service.GeoService;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeoCodeMethod extends ApiExecution {
    private final GeoService geoservice;

    public GeoCodeMethod() throws Exception {
        geoservice = new GeoService();
    }

    @Override
    public Object execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> more) throws ApiException {
        String service = request.getParameter("service");
        service = service == null ? "mapquest" : service;
        String type = more.get(RequestCodes.TYPE.code());
        ArrayList<Address> addresses;

        if (type.equals("addr")) {
            addresses = new ArrayList<Address>(Arrays.asList(new Address(more.get(RequestCodes.ADDRESS.code()))));
        } else if (type.equals("extended")) {
            String addr2 = request.getParameter("addr2");
            addr2 = (addr2 == null ? request.getParameter("street") : addr2);
            addr2 = (addr2 == null ? request.getParameter("address") : addr2);
            String number = request.getParameter("number");
            addresses = new ArrayList<Address>(Arrays.asList(new Address(
                request.getParameter("addr1"),
                (number != null ? number + " ":"") + addr2,
                request.getParameter("city"),
                request.getParameter("state"),
                request.getParameter("zip5"),
                request.getParameter("zip4")
            )));
        } else if (type.equals("bulk")) {
            addresses = new ArrayList<Address>();

            try {
             // Parse the json here
                JSONArray jsonAddresses;
                jsonAddresses = new JSONArray(request.getParameter("json"));
                for (int i=0; i<jsonAddresses.length(); i++) {
                    JSONObject jsonAddress = jsonAddresses.getJSONObject(i);
                    String addr2 = jsonAddress.has("addr2") ? jsonAddress.getString("addr2") : "";
                    addr2 = (addr2 == "" && jsonAddress.has("street") ? jsonAddress.getString("street") : addr2);
                    addr2 = (addr2 == "" && jsonAddress.has("address") ? jsonAddress.getString("address") : addr2);
                    String number = jsonAddress.has("number") ? jsonAddress.getString("number"): "";
                    Address newAddress = new Address(
                        jsonAddress.has("addr1") ? jsonAddress.getString("addr1") : "",
                        (number != null ? number + " ":"") + addr2,
                        jsonAddress.has("city") ? jsonAddress.getString("city") : "",
                        jsonAddress.has("state") ? jsonAddress.getString("state") : "",
                        jsonAddress.has("zip5") ? jsonAddress.getString("zip5") : "",
                        jsonAddress.has("zip4") ? jsonAddress.getString("zip4") : ""
                    );
                    addresses.add(newAddress);
                }
            } catch (JSONException e) {
                throw new ApiInternalException("Invalid JSON", e);
            }
        } else {
            throw new ApiTypeException(type);
        }


        try {
            ArrayList<Object> ret = new ArrayList<Object>();
            System.out.println(service);
            ArrayList<Result> results = geoservice.geocode(addresses, service);
            for (Result result : results) {
                if (result == null)
                    ret.add(new ErrorResponse("Internal Geocoding Error."));

                if (!result.status_code.equals("0"))
                    ret.add(new ErrorResponse(result.messages.get(0)));

                Address bestMatch = result.addresses.get(0);
                ret.add(new Point(bestMatch.latitude, bestMatch.longitude, bestMatch.as_raw()));
            }

            if (type.equals("addr") || type.equals("extended")) {
                return ret.get(0);

            } else {
                return ret;
            }

        } catch (UnsupportedEncodingException e) {
            throw new ApiInternalException("UTF-8 encoding not supported.", e);
        }
    }

    /*
	@Override
	public Object execute(HttpServletRequest request,
			HttpServletResponse response, ArrayList<String> more) throws ApiTypeException, ApiInternalException {

		Object ret = null;
		String service = request.getParameter("service");
		String type = more.get(RequestCodes.TYPE.code());

		if(type.equals("addr")) {
			try {
				ret = GeoCode.getApiGeocodeResponse(more.get(RequestCodes.ADDRESS.code()), service);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new ApiInternalException();
			}
		}
		else if(type.equals("extended")) {
			try {
				String addr2 = request.getParameter("addr2");
				addr2 = (addr2 == null ? request.getParameter("street") : addr2);
				addr2 = (addr2 == null ? request.getParameter("address") : addr2);
				String number = request.getParameter("number");
				ret = GeoCode.getApiGeocodeResponse(
						(number != null ? number + " ":"") + addr2,
						request.getParameter("city"),
						request.getParameter("state"),
						request.getParameter("zip4"),
						request.getParameter("zip5"),
						service);
			}
			catch(Exception e) {
				throw new ApiInternalException();
			}
		}
		else if(type.equals("bulk")) {
			try {
				String json = request.getParameter("json");
				return new GeocoderConnect().doBulkParsing(json);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new ApiInternalException();
			}
		}
		else {
			throw new ApiTypeException(type);
		}
		return ret;
	}
    */
	@Override
	public String toOther(Object obj, String format) throws ApiFormatException {
		if(format.equals("csv")) {
			if(obj instanceof Point) {
				return ((Point)obj).lat + "," + ((Point)obj).lon;
			}
			else if(obj instanceof String) {
				return obj.toString();
			}
			else if(obj instanceof Collection<?>) {
				String ret = "";
				for(Object o:(Collection<?>)obj) {
					if(o instanceof Point) {
						ret += ((Point)o).lat + "," + ((Point)o).lon + "\n";
					}
				}
				return ret;
			}
		}
		else {
			throw new ApiFormatException(format);
		}
		return null;
	}
}
