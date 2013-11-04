package org.neo4j.example.modeling

import org.neo4j.helpers.collection.IteratorUtil

class UserRepositorySpec extends GraphSpec {

    def "can create users"() {

        setup:
        def repo = new UserRepository()
        repo.setCypherExecutor(cypherExecutor)

        when:
        def userId = repo.addUser("Stefan")

        then: "verify that user has been created using cypher"
        IteratorUtil.single(executionEngine.execute("start n=node({id}) return n.name as name", [id: userId])).name == 'Stefan'

        and: "verify that user has been created using API"
        graphDatabaseService.getNodeById(userId).getProperty("name") == "Stefan"

    }
}
