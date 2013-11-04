package org.neo4j.example.modeling

import org.neo4j.cypher.javacompat.ExecutionEngine
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Transaction
import org.neo4j.kernel.impl.util.StringLogger
import org.neo4j.kernel.impl.util.TestLogger
import org.neo4j.server.database.CypherExecutor
import org.neo4j.server.database.WrappedDatabase
import org.neo4j.test.TestGraphDatabaseFactory
import spock.lang.Specification

abstract class GraphSpec extends Specification {

    GraphDatabaseService graphDatabaseService
    ExecutionEngine executionEngine
    CypherExecutor cypherExecutor
    StringLogger logger
    Transaction transaction

    def setup() {
        graphDatabaseService = new TestGraphDatabaseFactory().newImpermanentDatabase()
        logger = new TestLogger()
        cypherExecutor = new CypherExecutor(new WrappedDatabase(graphDatabaseService), logger)
        cypherExecutor.start()
        executionEngine = cypherExecutor.executionEngine
        transaction = graphDatabaseService.beginTx()
    }

    def cleanup() {
        transaction.close()
        cypherExecutor.stop()
        graphDatabaseService.shutdown()
    }

}
