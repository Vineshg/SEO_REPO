package crawl4j.daemon.links;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class Neo4jLinksDaemon {

	private static Map<String, URI> node_locator = new HashMap<String, URI>(); 
	private static Map<String, Set<String>> in_links_map = new HashMap<String, Set<String>>();
	private static Map<String, Set<String>> out_links_map = new HashMap<String, Set<String>>();
	private static final String SERVER_ROOT_URI = "http://localhost:7474/db/data/";
	private static int counter = 0;
	private static int insertion_counter = 0;
	private static String update_statement ="UPDATE CRAWL_RESULTS SET IN_LINKS_SIZE=?, IN_LINKS=? WHERE URL=?";

	private static Connection con; 

	public static void main(String[] args){

		try {
			instantiate_connection();
		} catch (SQLException e1) {
			e1.printStackTrace();
			System.out.println("Trouble with the POSTGRESQL database");
			System.exit(0);
		}

		//we here check the Neo4j database is up
		if (!(checkDatabaseIsRunning() == 200)){
			System.out.println("Trouble with the NEO4J graph database");
			System.exit(0);
		}
		//we here empty the Neo4j database that we'll be filling up with brand new data
		cleanUpDatabase();

		try{
			// fetching data from the Postgresql data base and looping over
			looping_over_urls();
		} catch (SQLException e){
			e.printStackTrace();
			System.out.println("Trouble with the POSTGRESQL database");
			System.exit(0);
		}

		savingIncomingLinks();

		// creating the relationships in neo4j		
		building_relationships();
	}

	private static void instantiate_connection() throws SQLException{
		// instantiating database connection
		String url="jdbc:postgresql://localhost/CRAWL4J";
		String user="postgres";
		String passwd="mogette";
		con = DriverManager.getConnection(url, user, passwd);
	}

	private static void savingIncomingLinks(){
		System.out.println("Saving the incoming links in the POSTGRESQL database \n");
		Iterator it = in_links_map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String url =(String)pairs.getKey();
			Set<String> incoming_links = (Set<String>)pairs.getValue();
			System.out.println("Incoming links : "+incoming_links);
			try {
				insert_links_row(url,incoming_links);
			} catch (SQLException e) {
				System.out.println("Trouble inserting URL : "+url);
			}
		}	
	}

	private static void insert_links_row(String url,Set<String> incoming_links) throws SQLException{
		insertion_counter++;
		System.out.println("Inserting URL number " + insertion_counter + url);
		PreparedStatement st = con.prepareStatement(update_statement);
		// preparing the statement
		st.setInt(1,incoming_links.size());
		st.setString(2,incoming_links.toString());
		st.setString(3,url);
		st.executeUpdate();
	}

	public static void looping_over_urls() throws SQLException{
		// here is the links daemon starting point
		// getting all URLS and out.println links for each URL
		System.out.println("Getting all URLs and outside links from the crawl results database");
		PreparedStatement pst = con.prepareStatement("SELECT URL, LINKS FROM CRAWL_RESULTS WHERE DEPTH >0 ORDER BY DEPTH LIMIT 100000");
		ResultSet rs = pst.executeQuery();
		while (rs.next()) {
			counter++;
			String url_node = rs.getString(1);
			String output_links = rs.getString(2);
			System.out.println("Dealing with URL number :"+counter);
			manage_input(url_node,output_links);
		}
	}


	private static int checkDatabaseIsRunning()
	{
		// START SNIPPET: checkServer
		WebResource resource = Client.create()
				.resource( SERVER_ROOT_URI );
		ClientResponse response = resource.get( ClientResponse.class );

		System.out.println( String.format( "GET on [%s], status code [%d]",
				SERVER_ROOT_URI, response.getStatus() ) );
		response.close();
		return response.getStatus();
	}

	public static void manage_input(String url_node, String output_links){
		// creating the nodes in neo4j
		create_node(url_node);
		// populating the outgoing map
		populate_out_links(url_node, output_links);		
		// populating the incoming map
		populate_in_links(url_node, output_links);
	}

	private static void building_relationships(){
		System.out.println("Inserting relationships in the Neo4j graph db \n");
		Iterator it = out_links_map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String url =(String)pairs.getKey();
			Set<String> outgoing_links = (Set<String>)pairs.getValue();
			System.out.println("Outgoing links : "+outgoing_links);
			
			try {
				relations_insertion(url,outgoing_links);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Trouble building relationships for URL  : "+url);
			}
		}	
	}

	private static void relations_insertion(String url,Set<String> outgoing_links) throws URISyntaxException{

		URI beginningNode = node_locator.get(url);
		for (String ending_Node_URL : outgoing_links){
			URI endingNode = node_locator.get(ending_Node_URL);
			if (endingNode != null){
			URI relationshipUri = addRelationship( beginningNode, endingNode, "link","{}");
			System.out.println("First relationship URI : "+relationshipUri);
			} else {
				System.out.println("Trouble with url : "+url);
				System.out.println("One node has not been found : "+url);
			}
		}
	}
	private static URI addRelationship( URI startNode, URI endNode,
			String relationshipType, String jsonAttributes )
					throws URISyntaxException
	{
		URI fromUri = new URI( startNode.toString() + "/relationships" );
		String relationshipJson = generateJsonRelationship( endNode,
				relationshipType, jsonAttributes );

		WebResource resource = Client.create()
				.resource( fromUri );
		// POST JSON to the relationships URI
		ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
				.type( MediaType.APPLICATION_JSON )
				.entity( relationshipJson )
				.post( ClientResponse.class );

		final URI location = response.getLocation();
		System.out.println( String.format(
				"POST to [%s], status code [%d], location header [%s]",
				fromUri, response.getStatus(), location.toString() ) );

		response.close();
		return location;
	}

	private static String generateJsonRelationship( URI endNode,
			String relationshipType, String... jsonAttributes )
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "{ \"to\" : \"" );
		sb.append( endNode.toString() );
		sb.append( "\", " );

		sb.append( "\"type\" : \"" );
		sb.append( relationshipType );
		if ( jsonAttributes == null || jsonAttributes.length < 1 )
		{
			sb.append( "\"" );
		}
		else
		{
			sb.append( "\", \"data\" : " );
			for ( int i = 0; i < jsonAttributes.length; i++ )
			{
				sb.append( jsonAttributes[i] );
				if ( i < jsonAttributes.length - 1 )
				{ // Miss off the final comma
					sb.append( ", " );
				}
			}
		}

		sb.append( " }" );
		return sb.toString();
	}
	
	private static void populate_out_links(String url_node, String output_links){
		// we here compute the output links
		output_links = output_links.replace("[", "");
		output_links = output_links.replace("]", "");
		String[] url_outs = output_links.split(",");
		Set<String> outputSet = new HashSet<String>();
		for (String url_out : url_outs){
			outputSet.add(url_out);
		}
		out_links_map.put(url_node, outputSet);
	}

	private static void populate_in_links(String url_node, String output_links){
		// we here compute the output links
		output_links = output_links.replace("[", "");
		output_links = output_links.replace("]", "");
		String[] url_outs = output_links.split(",");
		for (String url_out : url_outs){
			Set<String> incomingSet = in_links_map.get(url_out);
			if ( incomingSet == null){
				// we don't have any entries yes
				incomingSet  = new HashSet<String>();
				in_links_map.put(url_out, incomingSet);
			}
			// we add the currently parsed URL
			incomingSet.add(url_node);
		}
	}

	private static void cleanUpDatabase(){
		sendTransactionalCypherQuery("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n,r");
	}

	private static void sendTransactionalCypherQuery(String query) {
		final String txUri = SERVER_ROOT_URI + "transaction/commit";
		WebResource resource = Client.create().resource( txUri );

		String payload = "{\"statements\" : [ {\"statement\" : \"" +query + "\"} ]}";
		ClientResponse response = resource
				.accept( MediaType.APPLICATION_JSON )
				.type( MediaType.APPLICATION_JSON )
				.entity( payload )
				.post( ClientResponse.class );

		System.out.println( String.format(
				"POST [%s] to [%s], status code [%d], returned data: "
						+ System.getProperty( "line.separator" ) + "%s",
						payload, txUri, response.getStatus(),
						response.getEntity( String.class ) ) );

		response.close();
	}

	private static URI createNode()
	{
		final String nodeEntryPointUri = SERVER_ROOT_URI + "node";
		// http://localhost:7474/db/data/node

		WebResource resource = Client.create()
				.resource( nodeEntryPointUri );
		// POST {} to the node entry point URI
		ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
				.type( MediaType.APPLICATION_JSON )
				.entity( "{}" )
				.post( ClientResponse.class );

		final URI location = response.getLocation();
		System.out.println( String.format(
				"POST to [%s], status code [%d], location header [%s]",
				nodeEntryPointUri, response.getStatus(), location.toString() ) );
		response.close();

		return location;
	}

	private static void create_node(String url_node){
		URI node_location = createNode();
		addProperty(node_location,"name",url_node);		
		node_locator.put(url_node, node_location);
	}

	private static void addProperty( URI nodeUri, String propertyName,
			String propertyValue )
	{
		String propertyUri = nodeUri.toString() + "/properties/" + propertyName;
		// http://localhost:7474/db/data/node/{node_id}/properties/{property_name}

		WebResource resource = Client.create()
				.resource( propertyUri );
		ClientResponse response = resource.accept( MediaType.APPLICATION_JSON )
				.type( MediaType.APPLICATION_JSON )
				.entity( "\"" + propertyValue + "\"" )
				.put( ClientResponse.class );

		System.out.println( String.format( "PUT to [%s], status code [%d]",
				propertyUri, response.getStatus() ) );
		response.close();
	}

}
