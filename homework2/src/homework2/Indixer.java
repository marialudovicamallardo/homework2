package homework2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class Indixer {
	Path path = Paths.get("target/idx"); 

	public void indicizza() throws IOException {
		Directory directory = FSDirectory.open(path);


		File dirfiles = new File("Resources");
		File[] files = dirfiles.listFiles();

		//Analyzers
		Analyzer analnome = CustomAnalyzer.builder()
				.withTokenizer(WhitespaceTokenizerFactory.class)
				.addTokenFilter(LowerCaseFilterFactory.class)
				.addTokenFilter(WordDelimiterGraphFilterFactory.class)
				.build();


		Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
		CharArraySet stopWords = new CharArraySet(Arrays.asList("in", "dei", "di", "a", "da", true), 
				true);
		perFieldAnalyzers.put("nome", analnome);
		perFieldAnalyzers.put("contenuto", new StandardAnalyzer(stopWords));
		Analyzer analyzer = new PerFieldAnalyzerWrapper(new ItalianAnalyzer(), 
				perFieldAnalyzers);

		IndexWriterConfig configurazione = new IndexWriterConfig(analyzer);
		configurazione.setCodec(new SimpleTextCodec());
		IndexWriter writer = new IndexWriter(directory, configurazione);
		writer.deleteAll();


		// Fetching all the files
		for (File file : files) {
			if(file.isFile()) {
				BufferedReader inputStream = null;
				String contenuto;
				String nome;
				Document doc;
				try {
					inputStream = new BufferedReader(new FileReader(file));
					while ((contenuto = inputStream.readLine()) != null && (nome=file.getName()) != null) {	
						nome=nome.replace(".txt", "");
						//indicizza qui contenuto e nome
						doc = new Document();
						doc.add(new TextField("nome", nome, Field.Store.YES));
						doc.add(new TextField("contenuto", contenuto, Field.Store.YES));
						writer.addDocument(doc);
						writer.commit();
					}
				}catch(IOException e) {
					System.out.println(e);
				}
				finally {
					if (inputStream != null) {
						inputStream.close();
					}
				}
			}
		}	
		writer.close();
	}
}
