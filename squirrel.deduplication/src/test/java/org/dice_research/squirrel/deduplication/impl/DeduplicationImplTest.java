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
import org.dice_research.squirrel.deduplication.hashing.HashValue;
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
import java.util.HashMap;
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

        List<Triple> triplesBefore1 = sparqlBasedSink.getTriplesForGraph(uri1);
        List<Triple> triplesBefore2 = sparqlBasedSink.getTriplesForGraph(uri2);

        List<CrawleableUri> uris = new ArrayList<>();
        uris.add(uri1);
        uris.add(uri2);

        deduplicationImpl.handleNewUris(uris);

        List<Triple> triplesAfter1 = sparqlBasedSink.getTriplesForGraph(uri1);
        List<Triple> triplesAfter2 = sparqlBasedSink.getTriplesForGraph(uri2);

        Assert.assertEquals(2,triplesBefore1.size());
        Assert.assertEquals(0,triplesAfter1.size());
        Assert.assertEquals(2,triplesBefore2);
        Assert.assertEquals(0,triplesAfter2);

//        Assert.assertEquals(2,sparqlBasedSink.getTriplesForGraph(uri2));
//        Assert.assertEquals(2, activity1.getNumberOfTriples());
//        Assert.assertEquals(2, activity2.getNumberOfTriples());
    }
}
