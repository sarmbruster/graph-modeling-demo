package org.neo4j.example.modeling

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
        "start n=node({id}) return n.name as name".cypher(id: userId)[0].name == 'Stefan'

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
