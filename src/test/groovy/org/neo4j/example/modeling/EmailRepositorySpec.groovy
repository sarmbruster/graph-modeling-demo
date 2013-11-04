package org.neo4j.example.modeling

import org.neo4j.cypher.javacompat.ExecutionResult
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.DynamicRelationshipType
import org.neo4j.graphdb.Relationship
import org.neo4j.helpers.collection.IteratorUtil

class EmailRepositorySpec extends GraphSpec {

    public static final SUBJECT = "Speaking at WJax"
    UserRepository userRepo
    EmailRepository emailRepo

    def setup() {
        userRepo = new UserRepository()
        userRepo.setCypherExecutor(cypherExecutor)
        emailRepo = new EmailRepository()
        emailRepo.setCypherExecutor(cypherExecutor)

    }

    def "should create email, check via API"() {
        setup:
        def stefanId = userRepo.addUser("Stefan")
        def jimId = userRepo.addUser("Jim")

        when:
        long emailId = emailRepo.createEmail("Stefan", "Jim", SUBJECT)

        then: "Email as a incoming SENT relationship to Stefan"
        graphDatabaseService.getNodeById(emailId)
                .getSingleRelationship(DynamicRelationshipType.withName("SENT"), Direction.INCOMING)
                .startNode == graphDatabaseService.getNodeById(stefanId)

        and: "Email has a outgoing TO relationship to Jim"
        graphDatabaseService.getNodeById(emailId)
                        .getSingleRelationship(DynamicRelationshipType.withName("TO"), Direction.OUTGOING)
                        .endNode == graphDatabaseService.getNodeById(jimId)
    }


    def "should create email, check via Cypher"() {
        setup:
        def stefanId = userRepo.addUser("Stefan")
        def jimId = userRepo.addUser("Jim")

        when:
        long id = emailRepo.createEmail("Stefan", "Jim", SUBJECT)

        def result = """MATCH (stefan:User)-[:SENT]->(e:Email)-[:TO]->(jim:User)
WHERE stefan.name='Stefan' AND jim.name='Jim'
RETURN e.subject as subject""".cypher from:'Stefan', to:'Jim'

        then:
        result.size() == 1
        result[0].subject == SUBJECT
    }

    def "should find all emails from stefan to jim"() {
        setup: "create reference graph"
        """CREATE (stefan:User {name:'Stefan'}), (jim:User {name:'Jim'}),
(stefan)-[:SENT]->(:Email {subject:'Speaking at WJax'})-[:TO]->(jim),
(stefan)-[:SENT]->(helpEmail:Email {subject:'Need help'})-[:TO]->(jim),
(jim)-[:SENT]->(helpEmailReply:Email {subject:'Re: Need help'})-[:TO]->(stefan),
(helpEmailReply)-[:IS_REPLY_TO]->(helpEmail)""".cypher()

        when:
        List<String> subjects = emailRepo.findAllEmails("Stefan", "Jim")

        then:
        subjects.containsAll(["Speaking at WJax", "Need help"])
        subjects.size() == 2

    }

    def "should list a email thread"() {
        setup: "create reference graph"
        """CREATE (stefan:User {name:'Stefan'}), (jim:User {name:'Jim'}),
(stefan)-[:SENT]->(:Email {subject:'Speaking at WJax'})-[:TO]->(jim),
(stefan)-[:SENT]->(helpEmail:Email {subject:'Need help'})-[:TO]->(jim),
(jim)-[:SENT]->(helpEmailReply:Email {subject:'Re: Need help'})-[:TO]->(stefan),
(helpEmailReply)-[:IS_REPLY_TO]->(helpEmail)""".cypher()

        when:
        List<String> subjects = emailRepo.findEmailThread("Stefan", "Jim", "Need help")

        then:
        subjects == ["Need help", "Re: Need help"]

    }
}
