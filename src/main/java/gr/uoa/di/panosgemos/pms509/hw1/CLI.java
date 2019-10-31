package gr.uoa.di.panosgemos.pms509.hw1;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;

/**
 * Class containing the code for the Command Line Interface of Homework I, of
 * the postgraduate course Knowledge Technologies, which is taught at the
 * Departemnt of Informatics and Telecomunications of the National and
 * Kapodistrian University of Athens.
 * 
 * @author panosgemos
 *
 */
public class CLI {

	private static final String USAGE = 
			"USAGE: java -jar hw1.jar query data...";
	
	private static final List<RDFFormat> RDF_FORMATS =
		Arrays.asList(new RDFFormat[] {
			RDFFormat.JSONLD,
			RDFFormat.N3,
			RDFFormat.NQUADS,
			RDFFormat.NTRIPLES,
			RDFFormat.RDFA,
			RDFFormat.RDFJSON,
			RDFFormat.RDFXML,
			RDFFormat.TRIG,
			RDFFormat.TRIX,
			RDFFormat.TURTLE,
	});
	
	public static void main(String[] args) {
		
		// Argument check
		if (args.length < 2) {
			throw new IllegalArgumentException(String.format(
					"Wrong number of arguments%n%s", USAGE));
		}
		
		List<String> dataFilePaths = 
				Arrays.asList(args).subList(1, args.length);
		String queryPath = args[0];
		List<RDFFormat> dataRDFFormats = parseRDFFormats(dataFilePaths);
	
		try (SPARQLExecutor exec = new SPARQLExecutor()) {
			for (int i = 0; i < dataFilePaths.size(); ++i) {
				exec.addRdfData(
						dataFilePaths.get(i), null, dataRDFFormats.get(i));
			}
			
			String query = Utils.readTextFile(queryPath);
			
			List<BindingSet> results = exec.tupleQuery(query);
			
			for (BindingSet result : results) {
				System.out.println(result);
			}
			
		} catch (RDFParseException e) {
				System.err.println("Cannot parse RDF data: " + e.getMessage());
			} catch (RepositoryException e) {
				System.err.println("Repository Error: " + e.getMessage());
			} catch (IOException e) {
				System.err.println("Input/Output Error: " + e.getMessage());
			}

	}
	
	/**
	 * Extracts the file names from the given list of file paths.
	 * 
	 * @param filePaths list of file paths
	 * 
	 * @return a list of file names
	 */
	public static List<String> extractFileNames(List<String> filePaths) {
		List<String> fileNames = new ArrayList<>(filePaths.size());
		for (int i = 0; i < filePaths.size(); ++i) {
			String fileName = Paths.get(filePaths.get(i)).getFileName().toString();
			fileNames.add(fileName);
		}
		return fileNames;
	}
	
	/**
	 * Extracts the file endings from the given list of file paths.
	 * 
	 * @param filePaths list of file paths
	 * 
	 * @return a list of file endings
	 */
	public static List<String> extractFileEndings(List<String> filePaths) {
		List<String> fileEndings = new ArrayList<>(filePaths.size());
		for (int i = 0; i < filePaths.size(); ++i) {
			String ending = filePaths.get(i).split(".")[1];
			fileEndings.add(ending);
		}
		return fileEndings;
	}
	
	/**
	 * Parses the RDFFormat, based on the given list of file paths or
	 * file names.
	 * 
	 * @param filePaths list of file formats
	 * 
	 * @return a list of RDF Formats
	 */
	public static List<RDFFormat> parseRDFFormats(List<String> filePaths) {
		List<RDFFormat> rdfFormats = new ArrayList<>(filePaths.size());
		for (int i = 0; i < filePaths.size(); ++i) {
			String fileName = Paths.get(filePaths.get(i)).getFileName().toString();
			try {
				rdfFormats.add(
						RDFFormat.matchFileName(fileName, RDF_FORMATS).get());
			} catch (NoSuchElementException e) {
				throw new IllegalArgumentException(
						"Unkown file extension: " + fileName.split(".")[1]);
			}
		}
		return rdfFormats;
	}
	
}
