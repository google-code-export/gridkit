import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Optimize;
import org.basex.data.MemData;
import org.basex.io.IOContent;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.junit.Test;

public class XPathPerfTest {

	@Test
	public void go() throws IOException, QueryException {

		Prop prop = new Prop();
//		String query = "/node[id = 2 and @id = 2 and props/prop[key='A']/val = 2]";
		String query = "/node[props/prop[key='A']/val = 2 and id = 2 and @id = 2]";
//		String query = "node[id = 2]";

		MemData data = MemBuilder.build("test-db", Parser.emptyParser(prop));

		Context ctx = new Context();
		ctx.openDB(data);

		for (int i = 0; i != 200000; ++i) {
			uoloadData(data, i);
		}
		ctx.update();

		Optimize.optimize(data, null);
		
		{
			QueryProcessor qp = new QueryProcessor(query, ctx);
			String res = qp.execute().serialize().toString();
		}

		long start = System.nanoTime();
		QueryProcessor qp = new QueryProcessor(query, ctx);
		String res = qp.execute().serialize().toString();
		long time = System.nanoTime() - start;

		System.out.println(res);
		System.out.println("Exec time for " + data.meta.size + " docs - "
				+ TimeUnit.NANOSECONDS.toMillis(time) + "ms");
	}

	private void uoloadData(MemData data, int id) throws IOException {
		Prop prop = new Prop();
		String xml = "<node id=\"" + id + "\"><id>" + id
				+ "</id><props><prop><key>A</key><val>" + id
				+ "</val></prop><prop><key>B</key>123</prop></props></node>";
		data.startUpdate();
		MemData clip = MemBuilder.build("clip",
				Parser.singleParser(new IOContent(xml), prop, ""));
		data.insert(data.meta.size, -1, clip);
		data.finishUpdate();
	}
}
