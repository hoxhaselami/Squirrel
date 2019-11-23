package org.dice_research.squirrel.deduplication.impl;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.dice_research.squirrel.data.uri.CrawleableUriFactory4Tests;
import org.dice_research.squirrel.data.uri.filter.RDBKnownUriFilter;
import org.dice_research.squirrel.deduplication.hashing.TripleComparator;
import org.dice_research.squirrel.deduplication.hashing.TripleHashFunction;
import org.dice_research.squirrel.deduplication.hashing.UriHashCustodian;
import org.dice_research.squirrel.deduplication.hashing.impl.SimpleTripleComparator;
import org.dice_research.squirrel.deduplication.hashing.impl.SimpleTripleHashFunction;
import org.dice_research.squirrel.sink.impl.sparql.SparqlBasedSink;
import org.dice_research.squirrel.sink.tripleBased.AdvancedTripleBasedSink;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeduplicationImplTest {

    private UriHashCustodian uriHashCustodian;

    private AdvancedTripleBasedSink sink;

    private TripleComparator tripleComparator;

    private TripleHashFunction tripleHashFunction;

    private CrawleableUriFactory4Tests crawleableUriFactory4Tests;

    private UpdateExecutionFactory updateExecutionFactory;


    @Before
    public void setup(){
        uriHashCustodian = new RDBKnownUriFilter("",255,true);
        tripleComparator = new SimpleTripleComparator();
        tripleHashFunction = new SimpleTripleHashFunction();
    }

    @Test
    public void test(){

    }

    @After
    public void finish(){

    }
}
