package org.dice_research.squirrel.deduplication.impl;

import com.rethinkdb.RethinkDB;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDataset;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryDataset;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.dice_research.squirrel.Constants;
import org.dice_research.squirrel.data.uri.CrawleableUri;
import org.dice_research.squirrel.data.uri.filter.MongoDBKnowUriFilter;
import org.dice_research.squirrel.data.uri.filter.RDBKnownUriFilter;
import org.dice_research.squirrel.deduplication.hashing.HashValue;
import org.dice_research.squirrel.deduplication.hashing.TripleComparator;
import org.dice_research.squirrel.deduplication.hashing.TripleHashFunction;
import org.dice_research.squirrel.deduplication.hashing.UriHashCustodian;
import org.dice_research.squirrel.deduplication.hashing.impl.SimpleTripleComparator;
import org.dice_research.squirrel.deduplication.hashing.impl.SimpleTripleHashFunction;
import org.dice_research.squirrel.metadata.CrawlingActivity;
import org.dice_research.squirrel.model.RDBConnector;
import org.dice_research.squirrel.sink.impl.sparql.SparqlBasedSink;
import org.dice_research.squirrel.sink.tripleBased.AdvancedTripleBasedSink;
import org.dice_research.squirrel.vocab.Squirrel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeduplicationImplTest {

    private UriHashCustodian uriHashCustodian;

    private static AdvancedTripleBasedSink sink;

    private TripleComparator tripleComparator;

    private TripleHashFunction tripleHashFunction;

    private ArrayList<CrawleableUri> uris = new ArrayList();

    private RDBConnector connector;

    private RethinkDB r;

    @Before
    public void setup() throws Exception{
        Dataset dataset = DatasetFactory.create();
        dataset.setDefaultModel(ModelFactory.createDefaultModel());
        QueryExecutionFactory queryExecFactory = new QueryExecutionFactoryDataset(dataset);
        UpdateExecutionFactory updateExecFactory = new UpdateExecutionFactoryDataset(dataset);

//        r = new RethinkDB();
//        //uriHashCustodian = new RDBKnownUriFilter("",255,true);
//        connector = new RDBConnector("localhost",58015 );
//        uriHashCustodian = new RDBKnownUriFilter(connector, r, false);
//        uriHashCustodian  = new RDBKnownUriFilter("localhost",65000,false);
        tripleComparator = new SimpleTripleComparator();
        tripleHashFunction = new SimpleTripleHashFunction();

        CrawleableUri uri = new CrawleableUri(new URI("http://example.org/dataset"));
        uri.addData(Constants.UUID_KEY, "123");
        CrawlingActivity activity = new CrawlingActivity(uri, "http://example.org/testWorker");
        uri.addData(Constants.URI_CRAWLING_ACTIVITY, activity);

//        uriHashCustodian.addHashValuesForUris(uris);

        uris.add(uri);

        sink = new SparqlBasedSink(queryExecFactory, updateExecFactory);
        sink.openSinkForUri(uri);
        sink.addTriple(uri, new Triple(Squirrel.ResultGraph.asNode(), RDF.type.asNode(), RDFS.Class.asNode()));
        sink.addTriple(uri, new Triple(Squirrel.ResultGraph.asNode(), RDF.value.asNode(),
            ResourceFactory.createTypedLiteral("3.14", XSDDatatype.XSDdouble).asNode()));
        sink.closeSinkForUri(uri);



    }

    @Test
    public void test(){
        DeduplicationImpl deduplication = new DeduplicationImpl(uriHashCustodian,sink,tripleComparator,tripleHashFunction);
        deduplication.handleNewUris(uris);

    }

    @After
    public void finish(){
    }
}
