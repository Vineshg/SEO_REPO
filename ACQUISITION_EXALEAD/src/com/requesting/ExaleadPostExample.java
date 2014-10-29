package com.requesting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class ExaleadPostExample {

	
	public static void main(String[] args) throws ClientProtocolException, IOException{
		String query = "tablette";
		String number = "10";
		CloseableHttpClient httpclient = HttpClients.createDefault();
//		HttpGet httpGet = new HttpGet("http://targethost/homepage");
//		CloseableHttpResponse response1 = httpclient.execute(httpGet);
//		// The underlying HTTP connection is still held by the response object
//		// to allow the response content to be streamed directly from the network socket.
//		// In order to ensure correct deallocation of system resources
//		// the user MUST call CloseableHttpResponse#close() from a finally clause.
//		// Please note that if response content is not fully consumed the underlying
//		// connection cannot be safely re-used and will be shut down and discarded
//		// by the connection manager. 
//		try {
//		    System.out.println(response1.getStatusLine());
//		    HttpEntity entity1 = response1.getEntity();
//		    // do something useful with the response body
//		    // and ensure it is fully consumed
//		    EntityUtils.consume(entity1);
//		} finally {
//		    response1.close();
//		}
		String url_string = "http://exasearchv6.gslb.cdweb.biz:10010/search-api/search";
		HttpPost httpPost = new HttpPost(url_string);		
		//HttpPost httpPost = new HttpPost("http://targethost/login");
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		nvps.add(new BasicNameValuePair("lang", "fr"));
		nvps.add(new BasicNameValuePair("sl", "sl0"));
		nvps.add(new BasicNameValuePair("f.20.field", "facet_mut_technical"));
		nvps.add(new BasicNameValuePair("f.20.in_hits", "False"));
		nvps.add(new BasicNameValuePair("f.20.in_synthesis", "False"));
		nvps.add(new BasicNameValuePair("f.20.max_per_level", "10"));
		nvps.add(new BasicNameValuePair("f.20.root", "Top/ClassProperties/is_best_total_offer"));
		nvps.add(new BasicNameValuePair("f.20.sort", "num"));
		nvps.add(new BasicNameValuePair("f.20.type", "category"));
		nvps.add(new BasicNameValuePair("refine", "+f/20/1"));
		nvps.add(new BasicNameValuePair("use_logic_facets", "false"));
		nvps.add(new BasicNameValuePair("use_logic_hit_metas", "false"));
		nvps.add(new BasicNameValuePair("add_hit_meta", "offer_product_id"));
		nvps.add(new BasicNameValuePair("add_hit_meta", "offer_price"));
		nvps.add(new BasicNameValuePair("add_hit_meta", "offer_seller_id"));
		nvps.add(new BasicNameValuePair("hit_meta.termscore.expr", "100000*@term.score"));
		nvps.add(new BasicNameValuePair("hit_meta.proximity.expr", "@proximity"));
		nvps.add(new BasicNameValuePair("hit_meta.categoryweight.expr", "100000*offer_category_weight"));
		nvps.add(new BasicNameValuePair("hit_meta.ca14.expr", "offer_stats_income14_global"));
		nvps.add(new BasicNameValuePair("hit_meta.ca7.expr", "offer_stats_income7_global"));
		nvps.add(new BasicNameValuePair("hit_meta.ca1.expr", "offer_stats_income1_global"));
		nvps.add(new BasicNameValuePair("output_format", "csv"));
		nvps.add(new BasicNameValuePair("nresults",number));
		nvps.add(new BasicNameValuePair("q", query));
		//http://ldc-exa6-search01.cdweb.biz:10010/search-api/search?lang=fr&sl=sl0&use_logic_facets=false&use_logic_hit_metas=false&add_hit_meta=offer_product_id&add_hit_meta=offer_price:&&hit_meta.proximity.expr=@proximity&hit_meta.categoryweight.expr=100000*offer_category_weight&hit_meta.ca14.expr=offer_stats_income14_global&hit_meta.ca7.expr=offer_stats_income7_global&hit_meta.ca1.expr=offer_stats_income1_global&output_format=csv&nresults=2500&q=canape+angle AND offer_is_best_offer=1
		UrlEncodedFormEntity url_encoding = new UrlEncodedFormEntity(nvps);
		httpPost.setEntity(url_encoding);
		CloseableHttpResponse response2 = httpclient.execute(httpPost);

		try {
		    System.out.println(response2.getStatusLine());
		    HttpEntity entity2 = response2.getEntity();
		    // do something useful with the response body
		    // and ensure it is fully consumed
		    System.out.println(EntityUtils.toString(response2.getEntity()));
		    EntityUtils.consume(entity2);
		} finally {
		    response2.close();
		}
	}
}