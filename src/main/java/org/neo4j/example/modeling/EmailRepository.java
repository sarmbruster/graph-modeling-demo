package org.neo4j.example.modeling;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.server.database.CypherExecutor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Path("/email")
public class EmailRepository {

    @Context
    CypherExecutor cypherExecutor;

    public void setCypherExecutor(CypherExecutor cypherExecutor) {
        this.cypherExecutor = cypherExecutor;
    }

    public long createEmail(String sender, String receiver, String subject) {
        Map<String, Object> params = new HashMap<>();
        params.put("sender", sender);
        params.put("receiver", receiver);
        params.put("subject", subject);
        ExecutionResult result = cypherExecutor.getExecutionEngine().execute(
                "MATCH (s:User), (r:User) " +
                "WHERE s.name={sender} AND r.name={receiver} " +
                "CREATE (s)-[e:EMAILED {subject: {subject}}]->(r) " +
                "RETURN id(e) as id", params);
        return (long) IteratorUtil.single(result.columnAs("id"));
    }

    @GET
    public Collection<String> findAllEmails(@QueryParam("sender") String sender, @QueryParam("receiver") String receiver) {
        Map<String, Object> params = new HashMap<>();
        params.put("sender", sender);
        params.put("receiver", receiver);
        ExecutionResult result = cypherExecutor.getExecutionEngine().execute(
                "MATCH (s:User)-[e:EMAILED]->(r:User) " +
                "WHERE s.name={sender} AND r.name={receiver} " +
                "RETURN e.subject as subject", params);

        Collection<String> subjects = new ArrayList<>();
        for (Map<String, Object> row : result) {
            subjects.add((String) row.get("subject"));
        }
        return subjects;
    }
}
