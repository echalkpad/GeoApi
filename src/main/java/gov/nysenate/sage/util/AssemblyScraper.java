package gov.nysenate.sage.util;

import gov.nysenate.sage.model.districts.Assembly;
import gov.nysenate.sage.model.districts.Member;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

public class AssemblyScraper {
	
	private Logger logger = Logger.getLogger(AssemblyScraper.class);

	static final String ASSEMBLY_DIRECTORY_URL = "http://assembly.state.ny.us/mem/?sh=email";
	static final String ASSEMBLY_URL = "http://assembly.state.ny.us";
	
	public void index() {
		Connect c = new Connect();
		
		List<Assembly> persons = getAssemblyPersons();
		
		if(persons != null && !persons.isEmpty()) {
			try {
				logger.info("Deleting Assembly Member data from the database");
				c.deleteObjectById(Member.class, "type", Member.MemberType.Assembly.value() + "");
				c.deleteObjects(Assembly.class, false);
			} catch (Exception e) {
				logger.warn(e);
			}
			
			logger.info("Persisting new assembly data");
			for(Assembly a:persons) {
				c.persist(a);
			}
		}
				
		c.close();
	}
	
	public List<Assembly> getAssemblyPersons() {
		List<Assembly> ret = new ArrayList<Assembly>();
		
		try {
			Pattern p = Pattern.compile(
					"<div class=\"email1\"><a href=\"(.+?)\">(.+?)</a>.*?email2\">(\\d+)");
			Matcher m = null;
			
			logger.info("Connecting to " + ASSEMBLY_DIRECTORY_URL);
			BufferedReader br = new BufferedReader(
					new InputStreamReader(
							new URL(ASSEMBLY_DIRECTORY_URL).openStream()));
			
			String in = null;
			while((in = br.readLine()) != null) {
				m = p.matcher(in);
				
				if(m.find()) {
					logger.info("Fetching assembly member " + m.group(2));
					Assembly a = new Assembly("Assembly District " + m.group(3),
							new Member(m.group(2),ASSEMBLY_URL+m.group(1),Member.MemberType.Assembly));
					ret.add(a);
				}
			}
			
			br.close();
		}
		catch (IOException ioe) {
			logger.warn(ioe);
		}
		
		return ret;
	}
}