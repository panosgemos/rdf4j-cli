package gr.uoa.di.panosgemos.pms509.hw1;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.helpers.AbstractNotifyingSail;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import com.google.common.io.Files;

/**
 * Class for executing SPARQL queries, using data either stored in memory or
 * natively in the hard disk drive, with support for inferencing.
 * 
 * @author panosgemos
 *
 */
public class SPARQLExecutor implements AutoCloseable {
	
	private final AbstractNotifyingSail store;
	private final Repository repository;
	
	private final LinkedList<RDFData> rdfDataList = new LinkedList<>();
	
	/**
	 * Constructs an new {@link SPARQLExecutor} for executing SPARQL queries.
	 * 
	 * @param memoryStore	Whether to store data in memory or natively in the
	 * 						hard disk drive.
	 * @param inferencing 	Whether to support inferencing or not.
	 */
	public SPARQLExecutor(boolean memoryStore, boolean inferencing) {
		if (memoryStore) {
			store = new MemoryStore();
		} else {
			store = new NativeStore(Files.createTempDir());
		}
		
		if (inferencing) {
			ForwardChainingRDFSInferencer inferencer = 
					new ForwardChainingRDFSInferencer(store);
			repository = new SailRepository(inferencer);
		}
		else {
			repository = new SailRepository(store);
		}
		
		repository.initialize();
	}
	
	/**
	 * Same as {@link #SPARQLExecutor(boolean, boolean)} but with the
	 * memoryStore set to {@code true} by default.
	 * 
	 * @param inferencing 	Whether to support inferencing or not.
	 */
	public SPARQLExecutor(boolean inferencing) {
		this(true, inferencing);
	}
	
	/**
	 * Same as {@link #SPARQLExecutor(boolean, boolean)} but with the
	 * memoryStore and inferencing both set to {@code true} by default.
	 * 
	 * @param inferencing 	Whether to support inferencing or not.
	 */
	public SPARQLExecutor() {
		this(true, true);
	}
	
	/**
	 * Adds RDF data to the store.
	 * 
	 * @param url
	 *        The URL of the RDF data.
	 * @param baseURI
	 *        The base URI to resolve any relative URIs that are in the data against. This defaults to the
	 *        value of {@link java.net.URL#toExternalForm() url.toExternalForm()} if the value is set to
	 *        <tt>null</tt>.
	 * @param dataFormat
	 *        The serialization format of the data. If set to <tt>null</tt>, the format will be automatically
	 *        determined by examining the content type in the HTTP response header, and failing that, the file
	 *        name extension of the supplied URL.
	 */
	public void addRdfData(URL url, String baseUri, RDFFormat format) {
		rdfDataList.add(new RDFData(url, baseUri, format));
	}
	
	/**
	 * Adds RDF data to the store.
	 * 
	 * @param path
	 *        String representing the path to the RDF data.
	 * @param baseURI
	 *        The base URI to resolve any relative URIs that are in the data against. This defaults to the
	 *        value of {@link java.net.URL#toExternalForm() url.toExternalForm()} if the value is set to
	 *        <tt>null</tt>.
	 * @param dataFormat
	 *        The serialization format of the data. If set to <tt>null</tt>, the format will be automatically
	 *        determined by examining the content type in the HTTP response header, and failing that, the file
	 *        name extension of the supplied URL.
	 */
	public void addRdfData(String path, String baseUri, RDFFormat format)
			throws MalformedURLException {
		addRdfData(Paths.get(path).toUri().toURL(), baseUri, format);
	}
	
	/**
	 * Executes the given tuple query.
	 * 
	 * It also submits any RDF Data that have been recently added and they have
	 * not been submitted.
	 * 
	 * @param query Query to be executed.
	 * 
	 * 
	 * @throws RDFParseException
	 *         If an error was found while parsing the RDF data.
	 * @throws RepositoryException
	 * 		   If something went wrong during the creation of the connection
	 * 		   with the repository. 
	 * @throws IOException
	 *         If an I/O error occurred while reading from the file.
	 * 
	 * @return The results/bindings as a list of BindingSet objects.
	 */
	public List<BindingSet> tupleQuery(String query) throws RDFParseException, RepositoryException, IOException {
		try (RepositoryConnection conn = repository.getConnection()) {
			
			while (!rdfDataList.isEmpty()) {
				RDFData data = rdfDataList.pop();
				conn.add(data.url, data.baseUri, data.format);
			}
	
			TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
			
			try (TupleQueryResult result = tupleQuery.evaluate()) {
				
				List<BindingSet> bindingSets = new LinkedList<>();
				
				//iterate the result set
				while (result.hasNext()) {
					bindingSets.add(result.next());
				}
				
				return bindingSets;
			}
		}
	}
	
	/**
	 * Closes any resources used by the SPARQLExecutor.
	 * 
	 * More specifically, it shut's down the repository.
	 */
	@Override
	public void close() {
		repository.shutDown();
	}
	
	/**
	 * POJO class for representing RDFData.
	 * @author panosgemos
	 *
	 */
	private class RDFData {
		
		private final URL url;
		private final String baseUri;
		private final RDFFormat format;
		
		private RDFData(URL url, String baseUri, RDFFormat format) {
			this.url = url;
			this.baseUri = baseUri;
			this.format = format;
		}
		
	}

}
