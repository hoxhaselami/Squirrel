package org.dice_research.squirrel.deduplication.impl;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDataset;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryDataset;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.dice_research.squirrel.Constants;
import org.dice_research.squirrel.data.uri.CrawleableUri;
import org.dice_research.squirrel.deduplication.hashing.impl.SimpleTripleComparator;
import org.dice_research.squirrel.deduplication.hashing.impl.SimpleTripleHashFunction;
import org.dice_research.squirrel.metadata.CrawlingActivity;
import org.dice_research.squirrel.sink.impl.sparql.QueryGenerator;
import org.dice_research.squirrel.sink.impl.sparql.SparqlBasedSink;
import org.dice_research.squirrel.vocab.Squirrel;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class DeduplicationImplTest {
    private SparqlBasedSink sparqlBasedSink;
    QueryExecutionFactory queryExecFactory;
    UpdateExecutionFactory updateExecFactory;
    Dataset dataset;

    @Before
    public void init() throws IOException, InterruptedException {
        dataset = DatasetFactory.create();
        dataset.setDefaultModel(ModelFactory.createDefaultModel());
        queryExecFactory = new QueryExecutionFactoryDataset(dataset);
        updateExecFactory = new UpdateExecutionFactoryDataset(dataset);
        sparqlBasedSink = new SparqlBasedSink(queryExecFactory, updateExecFactory);

    }

    @After
    public void teardown() throws IOException, InterruptedException {
    }

    @Test
    public void testHandlingNewUris() throws URISyntaxException {

        DeduplicationImpl deduplicationImpl = new DeduplicationImpl(sparqlBasedSink, new SimpleTripleComparator(), new SimpleTripleHashFunction());

        CrawleableUri uri1 = new CrawleableUri(new URI("http://example.org/dataset1"));
        uri1.addData(Constants.UUID_KEY, "123");

        CrawleableUri uri2 = new CrawleableUri(new URI("http://example.org/dataset2"));
        uri2.addData(Constants.UUID_KEY, "124");

        CrawlingActivity activity1 = new CrawlingActivity(uri1, "http://example.org/testWorker1");
        uri1.addData(Constants.URI_CRAWLING_ACTIVITY, activity1);

        CrawlingActivity activity2 = new CrawlingActivity(uri2, "http://example.org/testWorker2");
        uri2.addData(Constants.URI_CRAWLING_ACTIVITY, activity2);

        Triple triple1 = new Triple(Squirrel.ResultGraph.asNode(), RDF.type.asNode(), RDFS.Class.asNode());
        Triple triple2 = new Triple(Squirrel.ResultGraph.asNode(), RDF.value.asNode(),
            ResourceFactory.createTypedLiteral("3.14", XSDDatatype.XSDdouble).asNode());

        Triple triple3 = new Triple(Squirrel.ResultGraph.asNode(), RDF.value.asNode(),
            ResourceFactory.createTypedLiteral("3.1434", XSDDatatype.XSDdouble).asNode());


        sparqlBasedSink.openSinkForUri(uri1);
        sparqlBasedSink.addTriple(uri1, triple1);
        sparqlBasedSink.addTriple(uri1, triple2);
        sparqlBasedSink.closeSinkForUri(uri1);

        sparqlBasedSink.openSinkForUri(uri2);
        sparqlBasedSink.addTriple(uri2, triple1);
        sparqlBasedSink.addTriple(uri2, triple3);
        sparqlBasedSink.closeSinkForUri(uri2);

        sparqlBasedSink.getTriplesForGraph(uri1);

        List<CrawleableUri> uris = new ArrayList<>();
        uris.add(uri1);
        uris.add(uri2);

        deduplicationImpl.handleNewUris(uris);

//        List<Triple> triplesURI1 = sparqlBasedSink.getTriplesForGraph(uri1);
//        List<Triple> triplesURI2 = sparqlBasedSink.getTriplesForGraph(uri2);

        /**Here we make the attempt to delete the data*/
//        Query delete = QueryGenerator.getInstance().getSelectQuery(SparqlBasedSink.getGraphId(uri1));
//        String graphID = SparqlBasedSink.getGraphId(uri1);//
//        String querybuilder = "DROP GRAPH <"+ SparqlBasedSink.getGraphId(uri1) +"> ;";
//        UpdateRequest request = UpdateFactory.create(querybuilder.toString());
//        UpdateAction.execute(request, dataset) ;



/**
 *
 * Here is the query that sees if the data was deleted
 * */
        Query selectQuery = QueryGenerator.getInstance().getSelectQuery(SparqlBasedSink.getGraphId(uri1));
        QueryExecution qe = queryExecFactory.createQueryExecution(selectQuery);
        ResultSet rs = qe.execSelect();
        List<Triple> triplesFound = new ArrayList<>();
        while (rs.hasNext()) {
            QuerySolution sol = rs.nextSolution();
            RDFNode subject = sol.get("subject");
            RDFNode predicate = sol.get("predicate");
            RDFNode object = sol.get("object");
            triplesFound.add(Triple.create(subject.asNode(), predicate.asNode(), object.asNode()));
        }
        qe.close();

/**
 * Here is a query that shows if the graph exists anymore
 * */
//        String askquery = "ASK WHERE { GRAPH <"+ SparqlBasedSink.getGraphId(uri1)+"> { ?s ?p ?o } }";
//
//        Query query = QueryFactory.create(askquery) ;
//        QueryExecution qexec = queryExecFactory.createQueryExecution(query) ;
//        boolean result = qexec.execAsk() ;
//        qexec.close();






//      Assert.assertTrue(!result);

        Assert.assertEquals(0,triplesFound.size());
//
//        Assert.assertEquals(2,sparqlBasedSink.getTriplesForGraph(uri2));
//        Assert.assertEquals(2, activity1.getNumberOfTriples());
//        Assert.assertEquals(2, activity2.getNumberOfTriples());
    }
}
