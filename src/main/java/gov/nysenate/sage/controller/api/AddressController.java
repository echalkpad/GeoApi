package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.address.BatchValidateResponse;
import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.address.CityStateResponse;
import gov.nysenate.sage.client.response.address.ValidateResponse;
import gov.nysenate.sage.client.response.address.ZipcodeResponse;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;

import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.util.AddressUtil;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
 * Address API controller handles the various AddressService requests including
 *  - Address Validation
 *  - City State Lookup
 *  - ZipCode Lookup
 */
public final class AddressController extends BaseApiController
{
    private Logger logger = Logger.getLogger(AddressController.class);
    private static AddressServiceProvider addressProvider;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        addressProvider = ApplicationFactory.getAddressServiceProvider();
        logger.debug("Initialized " + this.getClass().getSimpleName());
    }

    /** Proxies to doGet() */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        this.doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Object addressResponse = new ApiError(this.getClass(), RESPONSE_ERROR);

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        String provider = apiRequest.getProvider();

        Boolean usePunctuation = Boolean.parseBoolean(request.getParameter("punct"));

        /**
         * If provider is specified then make sure it matches the available providers. Send an
         * api error and return if the provider is not supported.
         */
        if (provider != null && !provider.isEmpty()) {
            if (!addressProvider.isRegistered(provider)) {
                addressResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
                setApiResponse(addressResponse, request);
                return;
            }
        }

        logger.info("--------------------------------------");
        logger.info(String.format("Address Request | Mode: %s", apiRequest.getRequest()));
        logger.info("--------------------------------------");

        /** Handle single request */
        if (!apiRequest.isBatch()) {
            Address address = getAddressFromParams(request);
            if (address != null && !address.isEmpty()) {
                switch (apiRequest.getRequest()) {
                    case "validate": {
                        addressResponse = new ValidateResponse(addressProvider.validate(address, provider, usePunctuation));
                        break;
                    }
                    case "citystate" : {
                        addressResponse = new CityStateResponse(addressProvider.lookupCityState(address, provider));
                        break;
                    }
                    case "zipcode" : {
                        addressResponse = new ZipcodeResponse(addressProvider.lookupZipcode(address, provider));
                        break;
                    }
                    default: {
                        addressResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
                    }
                }
            }
            else {
                addressResponse = new ApiError(this.getClass(), MISSING_ADDRESS);
            }
        }
        else {
            String batchJsonPayload = IOUtils.toString(request.getInputStream(), "UTF-8");
            ArrayList<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
            if (addresses != null && !addresses.isEmpty()) {
                switch (apiRequest.getRequest()) {
                    case "validate": {
                        AddressService addressService = addressProvider.newInstance(provider, "usps");
                        addressResponse = new BatchValidateResponse(addressService.validate(addresses));
                        break;
                    }
                    default : {
                        addressResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
                    }
                }
            }
            else {
                addressResponse = new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);
            }
        }

        /** Set response */
        setApiResponse(addressResponse, request);
    }
}
