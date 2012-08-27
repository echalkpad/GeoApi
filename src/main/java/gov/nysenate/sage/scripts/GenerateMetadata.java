package gov.nysenate.sage.scripts;

import gov.nysenate.sage.util.ApiController;
import gov.nysenate.sage.util.AssemblyScraper;
import gov.nysenate.sage.util.CongressScraper;
import gov.nysenate.sage.util.NYSenateServices;
import gov.nysenate.sage.util.Resource;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class GenerateMetadata {

    public static void main(String[] args) throws Exception {
        ApiController controller = new ApiController();
        Resource APP_CONFIG = new Resource();

        System.out.println("indexing assembly... ");
        new AssemblyScraper().index();
        System.out.println("indexing congress... ");
        new CongressScraper().index();
        System.out.print("indexing senate... ");
        new NYSenateServices().index();
        System.out.println();

        File zoomFile = FileUtils.toFile(ClassLoader.getSystemResource("zoom"));
        File writeDirectory = new File(APP_CONFIG.fetch("json.directory"));
        File writeRawDirectory = new File(APP_CONFIG.fetch("json.raw_directory"));
        System.out.println("Writing JSON");
        controller.writeJson(writeDirectory, zoomFile, true);
        System.out.println("Writing Raw JSON");
        controller.writeJson(writeRawDirectory, zoomFile, false);
    }

}