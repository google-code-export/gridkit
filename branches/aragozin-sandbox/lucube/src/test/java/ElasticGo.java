import java.io.IOException;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.MapperBuilders;
import org.elasticsearch.index.mapper.object.RootObjectMapper;
import org.elasticsearch.index.settings.IndexSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.Test;


public class ElasticGo {

	@Test
	public void go_go_go() throws IOException {
		Node node = NodeBuilder.nodeBuilder().build();
		
		Client client = node.client();
		
		RootObjectMapper.Builder rom = new RootObjectMapper.Builder()
		DocumentMapper.Builder builder = new DocumentMapper.Builder("test-index", null, builder); 
		
		client.admin().indices().prepareCreate("test-index")
		.
		System.out.println(client/);
	}
	
}
