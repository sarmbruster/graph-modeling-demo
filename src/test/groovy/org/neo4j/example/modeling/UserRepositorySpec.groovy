package org.neo4j.example.modeling

import org.neo4j.helpers.collection.IteratorUtil

class UserRepositorySpec extends GraphSpec {

    UserRepository repo

    def setup() {
        repo = new UserRepository()
        repo.setCypherExecutor(cypherExecutor)

    }

    def "should create users"() {

        when:
        def userId = repo.addUser("Stefan")

        then: "verify that user has been created using cypher"
        IteratorUtil.single(executionEngine.execute("start n=node({id}) return n.name as name", [id: userId])).name == 'Stefan'

        and: "verify that user has been created using API"
        graphDatabaseService.getNodeById(userId).getProperty("name") == "Stefan"

    }

    def "should find a user"() {
        setup:
        def userId = repo.addUser("Stefan")

        expect:
        repo.findUser("Stefan") == userId
    }

}
