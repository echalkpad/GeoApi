package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Parallel geocoding for use when a GeocodeService implementation does not provide
 * native batch methods.
 */
public abstract class ParallelGeocodeService
{
    private static Logger logger = Logger.getLogger(ParallelGeocodeService.class);
    private static int THREAD_COUNT = 3;

    /**
    * Callable for parallel geocoding requests
    */
    private static class ParallelGeocode implements Callable<GeocodeResult>
    {
        public final GeocodeService geocodeService;
        public final Address address;
        public ParallelGeocode(GeocodeService geocodeService, Address address)
        {
            this.geocodeService = geocodeService;
            this.address = address;
        }

        @Override
        public GeocodeResult call()
        {
            return geocodeService.geocode(address);
        }
    }

    public static ArrayList<GeocodeResult> geocode(GeocodeService geocodeService, List<Address> addresses)
    {
        ArrayList<GeocodeResult> geocodeResults = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        ArrayList<Future<GeocodeResult>> futureGeocodeResults = new ArrayList<>();

        logger.debug("Geocoding using " + THREAD_COUNT + " threads");
        for (Address address : addresses) {
            futureGeocodeResults.add(executor.submit(new ParallelGeocode(geocodeService, address)));
        }

        for (Future<GeocodeResult> geocodeResult : futureGeocodeResults) {
            try {
                geocodeResults.add(geocodeResult.get());
            }
            catch (InterruptedException ex) {
                logger.error(ex.getMessage());
            }
            catch (ExecutionException ex) {
                logger.error(ex.getMessage());
            }
        }
        executor.shutdown();
        return geocodeResults;
    }
}