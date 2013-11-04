package org.neo4j.example.modeling

import org.neo4j.cypher.javacompat.ExecutionResult
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
        long id = emailRepo.createEmail("Stefan", "Jim", SUBJECT)
        Relationship relationship = graphDatabaseService.getRelationshipById(id)

        then: "email is modeled as relationship"
        relationship.type.name() == "EMAILED"
        relationship.getProperty("subject") == SUBJECT

        and: "relationship has correct direction"
        relationship.startNode.id == stefanId
        relationship.endNode.id == jimId
    }


    def "should create email, check via Cypher"() {
        setup:
        def stefanId = userRepo.addUser("Stefan")
        def jimId = userRepo.addUser("Jim")

        when:
        long id = emailRepo.createEmail("Stefan", "Jim", SUBJECT)

        def result = """MATCH (stefan:User)-[e:EMAILED]->(jim:User)
WHERE stefan.name='Stefan' AND jim.name='Jim'
RETURN e.subject as subject""".cypher from:'Stefan', to:'Jim'

        then:
        result.size() == 1
        result[0].subject == SUBJECT
    }

    def "should find all emails from stefan to jim"() {
        setup: "create reference graph"
        """CREATE (stefan:User {name:'Stefan'}), (jim:User {name:'Jim'}),
(stefan)-[:EMAILED {subject:'Speaking at WJax'}]->(jim),
(stefan)-[:EMAILED {subject:'Need help'}]->(jim),
(jim)-[:EMAILED {subject:'Re: Need help'}]->(stefan)""".cypher()

        when:
        List<String> subjects = emailRepo.findAllEmails("Stefan", "Jim")

        then:
        subjects.containsAll(["Speaking at WJax", "Need help"])
        subjects.size() == 2

    }
}
