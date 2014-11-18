package cron.analytics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.OnUrlLoadPlugin;

public class AjaxCronJob {

	private static String target = "cdiscount.com";
	private static CrawljaxRunner crawljax;

	public static void main(String[] args){
		// setting the path to our phantomjs executable
		Properties props = System.getProperties();
		props.setProperty("phantomjs.binary.path", "/home/sduprey/My_Programs/phantomjs-1.9.8-linux-x86_64/bin/phantomjs");
		try{	
			DataBaseManagement.instantiante_connection();
			int counter = DataBaseManagement.check_alive_run();
			if (counter >= 1){
				System.out.println("Cron : Fatal, another job is running");
				return;
			}
			java.sql.Date current_Date = new java.sql.Date(System.currentTimeMillis());
			long generated_run_id = DataBaseManagement.insertRunById(current_Date);
			String runId = Long.toString(generated_run_id);
			System.out.println("Generated run id for the cron job" + runId);

			// getting our groups
			ResultSet group_resultSet = DataBaseManagement.search_group(args);

			// we here loop over each group 
			while (group_resultSet.next()) {
				String idGroup = group_resultSet.getString("idGroup");
				String name = group_resultSet.getString("name");
				String module = group_resultSet.getString("module");
				String options = group_resultSet.getString("options");
				System.out.println("idGroup: " + idGroup);
				System.out.println("module: " + module);
				System.out.println("name: " + name);
				System.out.println("options: " + options);
				// Getting the targeted site to SERP
				// for the moment we only target cdiscount !
				ResultSet target_resultSet = DataBaseManagement.search_target(idGroup);
				String idTarget = null;
				while (target_resultSet.next()) {
					target=target_resultSet.getString("name");
					idTarget=target_resultSet.getString("idTarget");
					System.out.println("target: " + target);
					System.out.println("idTarget: " + idTarget);
				}

				// inserting a check row in the check table
				long idCheck =DataBaseManagement.insertCheckById(current_Date,idGroup,Long.toString(generated_run_id));
				String checkId = Long.toString(idCheck);
				System.out.println("Generated check id for the cron job" + checkId);
				// select the keywords to update
				ResultSet keyword_resultSet = DataBaseManagement.search_keywords(idGroup);
				while (keyword_resultSet.next()) {
					String idKeyword = keyword_resultSet.getString("idKeyword");			
					String keyword_name = keyword_resultSet.getString("name");
					//System.out.println("idKeyword: " + idKeyword);
					System.out.println("Launching keyword: " + keyword_name);

					// asynchronous launch
					//GoogleSearchSaveTask beep=new GoogleSearchSaveTask(checkId, idTarget,  idKeyword,keyword_name);

					// Synchronous launch but waiting after
					RankInfo loc_info = ajax_ranking_keyword(keyword_name);
					DataBaseManagement.insertKeyword(checkId, idTarget,  idKeyword,loc_info.getPosition(), loc_info.getUrl()); 
				}

				// closing the run by inserting a stopping date !
				DataBaseManagement.close_current_run();
			}	
		} catch (SQLException e){
			e.printStackTrace();
		} finally{
			DataBaseManagement.close();
		}
	}


	public static RankInfo ajax_ranking_keyword(String keyword) {
		final RankInfo info= new RankInfo();
		info.setKeyword(keyword);
		// we must set the geolocalisation for phantomJS
		keyword=keyword.replace(" ","%20");
		String my_url = "https://www.google.fr/search?q="+keyword+"&num=15&hl=FR&lr=lang_FR";
		// if you choose not to get geolocalized
		//String my_url = "https://www.google.fr/search?q="+keyword+"&num=15";
		// if you choose to get less results 
		//String my_url = "https://www.google.fr/search?q="+keyword;
		try {
			CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(my_url);
			// we will follow some of the results
			builder.crawlRules().click("h3").withAttribute("class","r");
			builder.crawlRules().insertRandomDataInInputForms(false);
			builder.setMaximumDepth(1);
			BrowserConfiguration browserConf = new BrowserConfiguration(BrowserType.PHANTOMJS, 1);
			builder.setBrowserConfig(browserConf);
			// we give ourselves time (fetching additional resellers might be long)
			//builder.crawlRules().waitAfterReloadUrl(waiting_times, TimeUnit.SECONDS);
			builder.addPlugin(new OnUrlLoadPlugin() {
				@Override
				public void onUrlLoad(CrawlerContext context) {
					// TODO Auto-generated method stub
					String content_to_parse = context.getBrowser().getStrippedDom();
					org.jsoup.nodes.Document doc = Jsoup.parse(content_to_parse);
					int nb_results = 0;
					int my_rank = 0;
					String my_url="";
					// we do that if the page is a google page
					Elements serps = doc.select("h3[class=r]");
					for (Element serp : serps) {
						Element link = serp.getElementsByTag("a").first();
						//String refclass = link.attr("data-href");
						String linkref = link.attr("href");
						String lclass = link.attr("class");
						if ((!( lclass.equals("l")))&& (!( lclass.equals("sla")))){				
							nb_results++;
							if (linkref.contains(target)){		
								my_rank=nb_results;
								my_url=linkref;
								info.setPosition(my_rank);
								info.setUrl(my_url);
							}			
						}
					}
				}
				@Override
				public String toString() {
					return "Market Place Plugin";
				}
			});
			crawljax = new CrawljaxRunner(builder.build());
			crawljax.call();
		} catch (Exception e){
			e.printStackTrace();
			System.out.println("Trouble fetching URL : "+my_url);
		}
		return info;	
	}

	public static int randInt(int min, int max) {
		// NOTE: Usually this should be a field rather than a method
		// variable so that it is not re-seeded every call.
		Random rand = new Random();
		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}
}
