package org.dice_research.squirrel.deduplication.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.tdb.store.Hash;
import org.dice_research.squirrel.Constants;
import org.dice_research.squirrel.data.uri.CrawleableUri;
import org.dice_research.squirrel.deduplication.hashing.HashValue;
import org.dice_research.squirrel.deduplication.hashing.TripleComparator;
import org.dice_research.squirrel.deduplication.hashing.TripleHashFunction;
import org.dice_research.squirrel.deduplication.hashing.UriHashCustodian;
import org.dice_research.squirrel.deduplication.hashing.impl.IntervalBasedMinHashFunction;
import org.dice_research.squirrel.sink.tripleBased.AdvancedTripleBasedSink;
import org.dice_research.squirrel.vocab.Squirrel;

import java.util.*;

public class DeduplicationImpl {

    private UriHashCustodian uriHashCustodian;

    private AdvancedTripleBasedSink sink;

    private TripleComparator tripleComparator;

    private TripleHashFunction tripleHashFunction;

    private Hashtable<CrawleableUri,HashValue> hashes = new Hashtable<>();

    public DeduplicationImpl(AdvancedTripleBasedSink sink,
                             TripleComparator tripleComparator,TripleHashFunction tripleHashFunction){
        this.uriHashCustodian = uriHashCustodian;
        this.sink = sink;
        this.tripleComparator = tripleComparator;
        this.tripleHashFunction = tripleHashFunction;
    }

    /**
     * Compare the hash values of the uris in  with the hash values of all uris contained
     * in {@link #uriHashCustodian}.
     * @param uris
     */
    private void compareNewUrisWithOldUris(List<CrawleableUri> uris) {
//  FIXME fix this part!
//        if (uriHashCustodian instanceof RDBKnownUriFilter) {
//            ((RDBKnownUriFilter) uriHashCustodian).openConnector();
//        }



//        uriHashCustodian.getUrisWithSameHashValues(hashValuesOfNewUris);

        HashSet<CrawleableUri> oldUrisForComparison = new HashSet<>();
        for(CrawleableUri uri : uris){

        }

        for (CrawleableUri uriNew : uris) {
            for (CrawleableUri uriOld : oldUrisForComparison) {
                if (!uriOld.equals(uriNew)) {
                    // get triples from pair1 and pair2 and compare them
                    List<Triple> listOld = sink.getTriplesForGraph(uriOld);
                    List<Triple> listNew = sink.getTriplesForGraph(uriNew);

                    if (tripleComparator.triplesAreEqual(listOld, listNew)) {
                        // TODO: delete duplicate, this means Delete the triples from the new uris and
                        // replace them by a link to the old uris which has the same content
                        sink.removeTriplesForGraph(uriNew);
                    }
                }
            }
        }
    }


    public void handleNewUris(List<CrawleableUri> uris) {


        HashSet<HashValue> hashValues = new HashSet<>();
        for (CrawleableUri nextUri : uris) {
            List<Triple> triples = sink.getTriplesForGraph(nextUri);
            HashValue value = (new IntervalBasedMinHashFunction(2, tripleHashFunction).hash(triples));
            nextUri.addData(Constants.URI_HASH_KEY, value);

            hashValues.add(value);
        }

        compareNewUrisWithOldUris(uris);

        sink.getMetadataGraphUri().addData(Constants.HASH_VALUES,hashValues);

//        uriHashCustodian.addHashValuesForUris(uris);

    }
}
