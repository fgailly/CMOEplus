package ugent.mis.cmoeplus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.cheetahplatform.common.logging.AuditTrailEntry;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public abstract class RecommendationServices {
	
	public OntologyManager getManager() {
		return manager;
	}
	

	protected OntologyManager manager;

	File wordnet;
	
	protected String modelName;
	

	protected Map<IRI,Recommendation> sugList;

	//Set parameters for types of matching
	protected boolean labelBasedRecommendationService = true;					
	protected boolean labelBasedRecommendationServiceSynonyms = true;					
	protected boolean modelLanguageRecommendationService = true;				
	protected boolean ruleBasedRecommendationService = true;		


	//Set weights and scores for recommendation services
	protected double weightLabelBasedRecommendationService = 1.0;				
	protected double weightModelLanguageRecommendationService = 1.0;				
	protected double scoreModelLanguageRecommendationService = 1.0;				

	protected double weightRuleBasedRecommendationService= 1.0;				
	protected double scoreRuleBasedRecommendationService = 1.0;				


	//Make a new ontology
	//True  =Create new model.owl file
	//False =Load from model.owl file
	protected boolean makeNewOntology=true;

	//Make an automatic annotation in the model ontology when a suggestion is clicked
	protected boolean annotateWhenSuggestionClicked=true;

	//The indication-string for candidate-annotations from the feedback ontology
	protected String canAnnQua="*";

	//Show candidate annotation possibility
	public boolean CandidateAnnotationPossiblity=true;

	public String CoO_filename=""; //Filename core ontology
	public String MLO_filename=""; //Filename MLO
	public String ESO_filename =""; //Filename Enterprise-specific Ontology
	public String CoO_MLO_filename=""; //Filename mapping core ontology and MLO
	public String rulesO_filename=""; //Filename rulesfile
	public String modelO_filename=""; //Filename model ontology
	protected String semAnnO_filename ="";
	
	protected String source;



	protected abstract Set<String> getSynonyms(String label);
	protected abstract double getStringDistance(String text1, String text2);
	
	public void initializeSuggestionList(){
		sugList = new HashMap<IRI, Recommendation>();
		Set<OWLNamedIndividual> individuals = manager.getESO().getIndividualsInSignature();
		OWLDataFactory fac = manager.getOWLManager().getOWLDataFactory();
		for (OWLNamedIndividual ind : individuals) {
			OWLAnnotationProperty description = fac.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
			String descriptionValue = "";
			Recommendation.Type type = Recommendation.Type.Class;
			OWLEntity entity = (OWLEntity)ind;
			for (OWLAnnotation annotation : ind.getAnnotations(manager.getESO(), description)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					descriptionValue = val.getLiteral();
				}
			}
			Recommendation sug = new Recommendation(ind.getIRI(), type, ind.getIRI().getFragment(),descriptionValue, "");
			sugList.put(ind.getIRI(), sug);
		}

		// add owl dataproperties to suggestionlist
		Set<OWLDataProperty> dataProperties = manager.getESO().getDataPropertiesInSignature();
		//printOWLDataProperties(dataProperties, "ESO DataProperties");
		for (OWLDataProperty prop : dataProperties) {
			OWLAnnotationProperty description = fac.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
			String descriptionValue = "";
			String domainString = "";
			for (OWLAnnotation annotation : prop.getAnnotations(manager.getESO(), description)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					descriptionValue = val.getLiteral();
				}
			}
			for (OWLClassExpression domainClass : prop.getDomains(manager.getESO())) {
				if (domainClass instanceof OWLClass) {
					domainString = ((OWLClass)domainClass).getIRI().getFragment() + ", ";
				}
			}

			Recommendation sug = new Recommendation(prop.getIRI(), Recommendation.Type.Datatype, prop.getIRI().getFragment(), descriptionValue, domainString);
			sugList.put(prop.getIRI(), sug);
		}

	}
	
	protected  Set<OWLNamedIndividual> modelLanguageRecommendationService(String irimodellingConstruct){
		OWLDataFactory fac = manager.getOWLManager().getOWLDataFactory();
		OWLClass owlClass = fac.getOWLClass(IRI
				.create(irimodellingConstruct));
		//OWLReasoner reasoner = getReasoner(merged);
		org.semanticweb.HermiT.Reasoner reasoner = new Reasoner(manager.getMergedO());
		NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(owlClass, false);
		Set<OWLNamedIndividual> extendedIndividuals = individuals.getFlattened();

		for(Node<OWLNamedIndividual> individual: individuals){
			OWLClass owlClass2 = fac.getOWLClass(IRI
					.create(individual.getRepresentativeElement().getIRI().toString()));
			NodeSet<OWLClass> subClses = reasoner.getSubClasses(owlClass2, false);
			for(OWLClass owlClass3: subClses.getFlattened()){
				extendedIndividuals.add(fac.getOWLNamedIndividual(owlClass3.getIRI()));
			}
		}
		return extendedIndividuals;
	}

	public SortedSet<Recommendation> suggestionList(String irimodellingConstruct, String label){
		resetWeightsSuggestions();


		//Model language Recommendation service
		if (this.modelLanguageRecommendationService){

			Set<OWLNamedIndividual> individuals = modelLanguageRecommendationService(irimodellingConstruct);
			for (OWLNamedIndividual ind : individuals) {
				Recommendation sug = sugList.get(ind.getIRI());
				sug.setScoreModelLanguageRecommendationService(weightModelLanguageRecommendationService * scoreModelLanguageRecommendationService);
				sug.setScore(sug.getScore() + weightModelLanguageRecommendationService * scoreModelLanguageRecommendationService);
			}
		}

		//Rule-based recommendation Ssrvice
		if (ruleBasedRecommendationService){

			Set<OWLNamedIndividual> individuals = filterIndividuals(ruleBasedRecommendationService(irimodellingConstruct));

			for (OWLNamedIndividual ind : individuals) {
				Recommendation sug = sugList.get(ind.getIRI());
				sug.setScore(sug.getScore() + weightRuleBasedRecommendationService *scoreRuleBasedRecommendationService);
				sug.setScoreRuleBasedREcommendationService(weightRuleBasedRecommendationService *scoreRuleBasedRecommendationService);
			}
		}


		//Label-based recommendation Service
		if (labelBasedRecommendationService){
			for (Recommendation sug : sugList.values()) {
				double max = 0.0;
				max = getStringDistance(label,sug.getIri().getFragment().toLowerCase());
				Set<String> synonyms = getSynonyms(label);

				for (String synonym : synonyms) {
					double score = getStringDistance(synonym, label);
					if(score > max)
						max = score;
				}

				sug.setScore(max + sug.getScore() * weightLabelBasedRecommendationService);
				sug.setScoreLabelBasedRecommendationService(max * weightLabelBasedRecommendationService);
			}
		}

		SortedSet<Recommendation> sortedSugList = new TreeSet<Recommendation>(); 
		for (Recommendation sug : sugList.values()) {
			//System.out.println(cls.getIRI());
			sortedSugList.add(sug);
		}


		AuditTrailEntry entry = new AuditTrailEntry("Generate recommendation");
		entry.setAttribute("Model Construct", irimodellingConstruct); 
		entry.setAttribute("Entered label", label);
		//this.log(entry);
		return sortedSugList;
	}

	protected Set<OWLNamedIndividual> ruleBasedRecommendationService(String irimodellingConstruct){
		OWLDataFactory fac = manager.getOWLManager().getOWLDataFactory();
		OWLClass owlClass = fac.getOWLClass(IRI
				.create(irimodellingConstruct));

		OWLNamedIndividual element = fac.getOWLNamedIndividual(IRI.create("http://www.mis.ugent.be/ontologies/model" + "#" + "test2"));
		OWLClassAssertionAxiom classAssertion = fac.getOWLClassAssertionAxiom(owlClass, element);
		AddAxiom addAxiom = new AddAxiom(manager.getModelO(), classAssertion);
		// We now use the manager to apply the change
		manager.getOWLManager().applyChange(addAxiom);

		org.semanticweb.HermiT.Reasoner reasoner = new Reasoner(manager.getModelO());
		reasoner.flush();
		OWLObjectProperty hasOntologyAnnotation = fac.getOWLObjectProperty(IRI
				.create(manager.getSemanticAnnotationProperty()));


		NodeSet<OWLNamedIndividual> individuals3 = reasoner.getObjectPropertyValues(element, hasOntologyAnnotation.getSimplified());
		Set<OWLNamedIndividual> extendedIndividuals = individuals3.getFlattened();

		for(Node<OWLNamedIndividual> individual: individuals3){
			OWLClass owlClass2 = fac.getOWLClass(IRI
					.create(individual.getRepresentativeElement().getIRI().toString()));
			NodeSet<OWLClass> subClses = reasoner.getSubClasses(owlClass2, false);
			for(OWLClass owlClass3: subClses.getFlattened()){
				extendedIndividuals.add(fac.getOWLNamedIndividual(owlClass3.getIRI()));
			}
		}

		OWLEntityRemover remover = new OWLEntityRemover(manager.getOWLManager(), Collections.singleton(manager.getModelO()));
		element.accept(remover);
		manager.getOWLManager().applyChanges(remover.getChanges());
		reasoner.flush();

		return extendedIndividuals;

	}

	public void resetWeightsSuggestions(){
		for(Recommendation sug: sugList.values()){
			sug.setScore(0);
			sug.setScoreModelLanguageRecommendationService(0);
			sug.setScoreRuleBasedREcommendationService(0);
			//sug.setWeightWordnetSynonyms(0);
			sug.setScoreLabelBasedRecommendationService(0);;
			sug.setSuggestionString(sug.getOntologyString());
		}

	}

	public Set<OWLNamedIndividual> filterIndividuals(Set<OWLNamedIndividual> entities){
		Set<OWLNamedIndividual> filteredEntities = new TreeSet<OWLNamedIndividual>();
		for (OWLNamedIndividual entity : entities) {
			if(manager.getESO().containsIndividualInSignature(entity.getIRI()))
				filteredEntities.add(entity);
		}
		return filteredEntities;
	}

	public Properties readPropertiesFile(FileInputStream fileInput) throws Exception{
		Properties properties = new Properties();
		try {
			properties.load(fileInput);
			fileInput.close();
			

			Enumeration enuKeys = properties.keys();
			System.out.println("Read properties-file:");
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = properties.getProperty(key);
				System.out.println(key + ": " + value);

				switch(key){
				case "LabelBasedRecommendationService":
					this.labelBasedRecommendationService = Boolean.parseBoolean(value);
					break;
				case "ModelLanguageRecommendationService":
					this.modelLanguageRecommendationService = Boolean.parseBoolean(value);
					break;
				case "RuleBasedRecommendationService":
					this.ruleBasedRecommendationService = Boolean.parseBoolean(value);
					break;
				case "WeightLabelBasedRecommendationServicee":
					this.weightLabelBasedRecommendationService = Double.parseDouble(value);
					break;	
				case "WeightModelLanguageRecommendationService":
					this.weightModelLanguageRecommendationService = Double.parseDouble(value);
					break;	
				case "ScoreModelLanguageRecommendationService":
					this.scoreModelLanguageRecommendationService = Double.parseDouble(value);
					break;	
				case "WeightRuleBasedRecommendationService":
					this.weightRuleBasedRecommendationService = Double.parseDouble(value);
					break;		
				case "ScoreRuleBasedRecommendationService":
					this.scoreRuleBasedRecommendationService = Double.parseDouble(value);
					break;			
				case "makeNewOntology":
					makeNewOntology = Boolean.parseBoolean(value);
					break;	
				case "AnnotateWhenSuggestionClicked":
					annotateWhenSuggestionClicked = Boolean.parseBoolean(value);
					break;			
				case "AnnotationIndication":
					canAnnQua = value.toString();
					break;
				case "CandidateAnnotationPossiblity":
					CandidateAnnotationPossiblity=Boolean.parseBoolean(value);
					break;
				case "source":
					source=value;
					break;
				case "CoreOntology":
					CoO_filename=value;
					break;
				case "ModelLanguageOntology":
					MLO_filename=value;
					break;
				case "CoO_MLO":
					CoO_MLO_filename=value;
					break;
				case "ESOntology":
					ESO_filename=value;
					break;
				case "ModelOntology":
					modelO_filename=value;
					break;
				case "RulesOntology":
					rulesO_filename=value;
					break;
				case "SemAnnOntology":
					semAnnO_filename =value;
					break;
				}

			}
			return properties;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}
}