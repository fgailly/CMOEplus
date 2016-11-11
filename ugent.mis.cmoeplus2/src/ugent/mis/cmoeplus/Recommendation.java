package ugent.mis.cmoeplus;

import org.semanticweb.owlapi.model.IRI;



public class Recommendation implements Comparable<Recommendation> {
	private IRI iri;
	private double score;
	private String suggestionString;
	private String ontologyString;
	private Type type;
	public enum Type {Class, Datatype};
	private String description;
	private String classes;
	private double scoreModelLanguageRecommendationService;
	private double scoreRuleBasedRecommendationService;
	private double scoreWordnetSynonyms;
	private double scoreLabelBasedRecommendationService;
	private int order;
	
	
	public Recommendation(IRI iri, double weight, Type type, String suggestionString, String description, String classes){
		this.iri = iri;
		this.score = weight;
		this.setType(type);
		this.setSuggestionString(suggestionString);
		this.description = description;
		this.classes = classes;
		this.ontologyString = suggestionString;
	}
	
	public Recommendation(IRI iri, Type type, String suggestionString, String description, String classes){
		this.iri = iri;
		this.score = 0;
		this.setType(type);
		this.setSuggestionString(suggestionString);
		this.description = description;
		this.classes = classes;
		this.ontologyString = suggestionString;
	}

	@Override
	public int compareTo(Recommendation sug) {
		
		if(score > sug.score) {
			return -1;
		}
		else if (score == sug.score) {
			return getSuggestionString().compareTo(sug.getSuggestionString());
		} else {
			return 1;
		}
	}
	
	public boolean equals(Recommendation sug){
		return iri.equals(sug);
	}

	public IRI getIri() {
		return iri;
	}

	public void setIri(IRI iri) {
		this.iri = iri;
	}

	public double getScore() {
		return score;
	}
	

	public void setScore(double score) {
		this.score = score;
	}

	public String getSuggestionString() {
		return suggestionString;
	}

	public void setSuggestionString(String suggestionString) {
		this.suggestionString = suggestionString;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getClasses() {
		return classes;
	}

	public void setClasses(String classes) {
		this.classes = classes;
	}

	public String getOntologyString() {
		return ontologyString;
	}

	public void setOntologyString(String ontologyString) {
		this.ontologyString = ontologyString;
	}

	public double getScoreModelLanguageRecommendationService() {
		return scoreModelLanguageRecommendationService;
	}

	public void setScoreModelLanguageRecommendationService(double scoreModelLanguageRecommendationService) {
		this.scoreModelLanguageRecommendationService = scoreModelLanguageRecommendationService;
	}

	

	public double getScoreRuleBasedRecommendationService() {
		return scoreRuleBasedRecommendationService;
	}

	public void setScoreRuleBasedREcommendationService(double scoreRuleBasedRecommendationService) {
		this.scoreRuleBasedRecommendationService = scoreRuleBasedRecommendationService;
	}

	public double getWeightWordnetSynonyms() {
		return scoreWordnetSynonyms;
	}

	public void setWeightWordnetSynonyms(double weightWordnetSynonyms) {
		this.scoreWordnetSynonyms = weightWordnetSynonyms;
	}

	public double getScoreLabelBasedRecommendationService() {
		return scoreLabelBasedRecommendationService;
	}

	public void setScoreLabelBasedRecommendationService(double scoreLabelBasedRecommendationService) {
		this.scoreLabelBasedRecommendationService = scoreLabelBasedRecommendationService;
	}

	public void setOrder(int i) {
		order = i;
		
	}

	public int getOrder() {
		return order;
	}

}
