package demo.json2tpboot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TPGraphMain {

    public static String getParent(JsonNode rootNode) {

        if (rootNode.isObject()) {
            return rootNode.fields().next().toString();
        }
        return "";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ResponseEntity<Object> tpAddToGraph(@RequestBody Request request) {
        ObjectMapper mapper = new ObjectMapper();
        TPUtils.DBTYPE target = TPUtils.DBTYPE.CASSANDRA;
        try {
            JsonNode rootNode = request.getRequestMapNode();
            String rootName = getParent(rootNode);
            System.out.println("Parent Name = " + rootName);
            // Check the rootVertex
            Vertex rootVertex;
            // Just to check if we could have a valid connection
            try (Graph graph = TPUtils.getGraph(target)) {
                try (Transaction tx = graph.tx()) {
                    rootVertex = TPUtils.createParentVertex(graph, rootName + "_GROUP");
                    tx.commit();
                }
            }
            CreateRecord cr = new CreateRecord(rootNode, rootName, target, rootVertex);
            cr.insert();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Can't close autocloseable " + e);
        }

        return new ResponseEntity(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/addcassandra", method = RequestMethod.POST)
    public ResponseEntity<Object> addToCassandra(@RequestBody Request request) {
        ObjectMapper mapper = new ObjectMapper();
        TPUtils.DBTYPE target = TPUtils.DBTYPE.CASSANDRA;
        try {
            CassandraImpl cassandra = new CassandraImpl();
            cassandra.addToCassandra(request.getRequestMap());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Can't close autocloseable " + e);
        }

        return new ResponseEntity(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/readcassandra", method = RequestMethod.POST)
    public ResponseEntity<Object> readFromCassandra(@RequestBody Request request) {
        Map<String, Object> responseMap = null;
        TPUtils.DBTYPE target = TPUtils.DBTYPE.CASSANDRA;
        try {
            CassandraImpl cassandra = new CassandraImpl();
            Map<String, Object> idMap = request.getRequestMap();
            responseMap = cassandra.readFromCassandra(idMap);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Can't close autocloseable " + e);
        }

        return new ResponseEntity(responseMap, HttpStatus.CREATED);
    }
}
