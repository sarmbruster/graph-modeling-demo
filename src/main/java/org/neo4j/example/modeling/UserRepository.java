package org.neo4j.example.modeling;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.server.database.CypherExecutor;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.util.Collections;
import java.util.Map;

@Path("/user")
class UserRepository {

    @Context
    CypherExecutor cypherExecutor;

    @PUT
    public long addUser(@QueryParam("name") String name) {

        Map<String,Object> params = Collections.singletonMap("name", (Object)name);
        ExecutionResult result = cypherExecutor.getExecutionEngine().execute("CREATE (u:User {name: {name}}) RETURN id(u)", params);

        return (long) IteratorUtil.single(result.columnAs("id(u)"));
    }

    void setCypherExecutor(CypherExecutor cypherExecutor) {
        this.cypherExecutor = cypherExecutor;
    }
}
