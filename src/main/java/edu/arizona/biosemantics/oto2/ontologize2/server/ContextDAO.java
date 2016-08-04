package edu.arizona.biosemantics.oto2.ontologize2.server;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.queryparser.classic.ParseException;

import edu.arizona.biosemantics.common.context.server.FileIndex;
import edu.arizona.biosemantics.common.context.shared.Context;
import edu.arizona.biosemantics.common.ling.know.SingularPluralProvider;
import edu.arizona.biosemantics.common.ling.know.lib.WordNetPOSKnowledgeBase;
import edu.arizona.biosemantics.common.ling.transform.lib.SomeInflector;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.ExtractContext;
import edu.stanford.nlp.util.StringUtils;

public class ContextDAO {
	
	private SingularPluralProvider singularPluralProvider = new SingularPluralProvider();
	private SomeInflector inflector;
	
	public ContextDAO() {
		try {
			WordNetPOSKnowledgeBase wordNetPOSKnowledgeBase = new WordNetPOSKnowledgeBase(Configuration.wordNetSource, false);
			inflector = new SomeInflector(wordNetPOSKnowledgeBase, singularPluralProvider.getSingulars(), singularPluralProvider.getPlurals());
		} catch (Exception e) {
			log(LogLevel.ERROR, "Could not load WordNetPOSKnowledgeBase.", e);
		}
	} 
	
	public void insert(int collectionId, List<Context> contexts) throws IOException {
		FileIndex index = new FileIndex(Configuration.context + File.separator + collectionId);
		index.open();
		for(Context context : contexts)
			index.add(context);
		index.close();
	}
	
	public void insert(int collectionId, Context context) throws IOException {
		FileIndex index = new FileIndex(Configuration.context + File.separator + collectionId);
		index.open();
		index.add(context);
		index.close();
	}

	public List<ExtractContext> get(int collectionId, String secret, String term) throws ParseException, IOException {
		Set<String> searches = new HashSet<String>();
		String searchTerm = term.trim();
		String singularSearchTerm = searchTerm;
		String pluralSearchTerm = searchTerm;
		
		if(inflector.isPlural(searchTerm)) {
			singularSearchTerm = inflector.getSingular(searchTerm);
		} else { 
			pluralSearchTerm = inflector.getPlural(searchTerm);
		}
		
		searches.add(singularSearchTerm);
		searches.add(pluralSearchTerm);
		searches.add(singularSearchTerm.replaceAll(" ", "-"));
		searches.add(singularSearchTerm.replaceAll(" ", "_"));
		searches.add(singularSearchTerm.replaceAll("_", "-"));
		searches.add(pluralSearchTerm.replaceAll(" ", "-"));
		searches.add(pluralSearchTerm.replaceAll(" ", "_"));
		searches.add(pluralSearchTerm.replaceAll("_", "-"));
		
		String query = "";
		for(String search : searches) {
			query += search + " OR ";
		}
		query= query.substring(0, query.length() - 4);
		FileIndex index = new FileIndex(Configuration.context + File.separator + collectionId);
		index.open();
		List<Context> contexts = index.search(query, Configuration.contextMaxHits);
		index.close();

		List<ExtractContext> result = createHighlightedAndShortenedContexts(contexts, searches);
		return result;
	}	

	private List<ExtractContext> createHighlightedAndShortenedContexts(List<Context> contexts, java.util.Collection<String> searches) {
		//shorten the context to be a number of characters before and after ... some text with WORD that appears in text ...
		//could appear multiple times in text, have to split into multiple contexts
		List<ExtractContext> result = new LinkedList<ExtractContext>();
		
		Set<Context> contextsSet = new HashSet<Context>();
		contextsSet.addAll(contexts);
		
		for(Context context : contextsSet) {
			List<ExtractContext> contextResult = new LinkedList<ExtractContext>();
			for(String search : searches) {
				Pattern pattern = Pattern.compile("\\b(?i)" + search + "\\b");
				result.addAll(extract(pattern, search, context));
			}
			
			/*if(contextResult.isEmpty()) {
				//String fullText = context.getText().replaceAll("(?i)" + search.getSearch(), "<b>" + search.getSearch() + "</b>").replaceAll("\n", "</br>");
				String fullText = context.getText();
				
				String idString = String.valueOf(context.getId()) + "-" + type.toString() + "-" + id++;
				TypedContext typedContext = new TypedContext(idString, context.getCollectionId(), context.getSource(), fullText, fullText, type);
		    	result.add(typedContext);
			}*/
			result.addAll(contextResult);
		}
		
		return result;
	}

	private List<ExtractContext> extract(Pattern pattern, String replaceTerm, Context context) {	
		List<ExtractContext> result = new LinkedList<ExtractContext>();
		Matcher matcher = pattern.matcher(context.getText());
		
		int id = 0;
		if(matcher.find()) {
			matcher.reset();
		    while (matcher.find()) {
		    	int startText = matcher.start() - 100;
		    	int endText = matcher.end() + 100;
		    	if(startText < 0)
		    		startText = 0;
		    	if(endText > context.getText().length())
		    		endText = context.getText().length();
		    	String extractedText = "..." + context.getText().substring(startText, endText) + "...";
		    	//extractedText = extractedText.replaceAll(pattern.toString(), "<b>" + replaceTerm + "</b>");
		    	extractedText = extractedText.replaceAll("(?i)" + replaceTerm, "<b>" + replaceTerm + "</b>");
		    	String fullText = context.getText().replaceAll("(?i)" + replaceTerm, "<b>" + replaceTerm + "</b>").replaceAll("\n", "</br>");
		    	
		    	String idString = String.valueOf(context.getId()) + "-" + id++;
		    	ExtractContext extracted = new ExtractContext(idString, context.getSource(), extractedText, fullText);
		    	result.add(extracted);
		    }
		}
	    return result;
	}

}