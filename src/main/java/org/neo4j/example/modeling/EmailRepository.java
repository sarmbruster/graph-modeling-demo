package org.neo4j.example.modeling;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.server.database.CypherExecutor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.util.*;

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
                "CREATE (s)-[:SENT]->(e:Email {subject: {subject}})-[:TO]->(r) " +
                "RETURN id(e) as id", params);
        return (long) IteratorUtil.single(result.columnAs("id"));
    }

    @GET
    public List<String> findAllEmails(@QueryParam("sender") String sender, @QueryParam("receiver") String receiver) {
        Map<String, Object> params = new HashMap<>();
        params.put("sender", sender);
        params.put("receiver", receiver);
        ExecutionResult result = cypherExecutor.getExecutionEngine().execute(
                "MATCH (s:User)-[:SENT]->(e:Email)-[:TO]->(r:User) " +
                "WHERE s.name={sender} AND r.name={receiver} " +
                "RETURN e.subject as subject", params);

        List<String> subjects = new ArrayList<>();
        for (Map<String, Object> row : result) {
            subjects.add((String) row.get("subject"));
        }
        return subjects;
    }

    @GET
    @Path("/thread")
    public List<String> findEmailThread(@QueryParam("sender") String sender, @QueryParam("receiver") String receiver, @QueryParam("subject") String subject) {
        Map<String, Object> params = new HashMap<>();
        params.put("sender", sender);
        params.put("receiver", receiver);
        params.put("subject", subject);
        ExecutionResult result = cypherExecutor.getExecutionEngine().execute(
                "MATCH (s:User)-[:SENT]->(e:Email)-[:TO]->(r:User), (e:Email)<-[:IS_REPLY_TO*0..10]-(answer) " +
                "WHERE s.name={sender} AND r.name={receiver} AND e.subject={subject} " +
                "RETURN answer.subject as subject", params);

        List<String> subjects = new ArrayList<>();
        for (Map<String, Object> row : result) {
            subjects.add((String) row.get("subject"));
        }
        return subjects;

    }
}
