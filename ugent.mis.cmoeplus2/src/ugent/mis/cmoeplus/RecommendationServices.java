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

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public abstract class RecommendationServices {
	
	public OntologyManager getManager() {
		return manager;
	}
	
	protected OntologyManager manager;
	protected CMOEplusProperties properties;

	File wordnet;
	
	protected String modelName;
	

	protected Map<IRI,Recommendation> sugList;

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
		manager.printOWLDataProperties(dataProperties, "ESO DataProperties");
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
		System.out.println("Type of construct;" + irimodellingConstruct);
		OWLDataFactory fac = manager.getOWLManager().getOWLDataFactory();
		OWLClass owlClass = fac.getOWLClass(IRI
				.create(irimodellingConstruct));
		//OWLReasoner reasoner = getReasoner(merged);
		org.semanticweb.HermiT.Reasoner reasoner = new Reasoner(manager.getMergedO());
		NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(owlClass, false);
		Set<OWLNamedIndividual> extendedIndividuals = individuals.getFlattened();

		/*for(Node<OWLNamedIndividual> individual: individuals){
			OWLClass owlClass2 = fac.getOWLClass(IRI
					.create(individual.getRepresentativeElement().getIRI().toString()));
			NodeSet<OWLClass> subClses = reasoner.getSubClasses(owlClass2, false);
			for(OWLClass owlClass3: subClses.getFlattened()){
				extendedIndividuals.add(fac.getOWLNamedIndividual(owlClass3.getIRI()));
			}
		}*/
		printOWLIndividuals(filterIndividuals(extendedIndividuals), "Resultaat ML RS");
		return filterIndividuals(extendedIndividuals);
	}

	public SortedSet<Recommendation> suggestionList(String irimodellingConstruct, String label){
		resetWeightsSuggestions();


		//Model language Recommendation service
		if (properties.isModelLanguageRecommendationService()){
			System.out.println("ML RS active");

			Set<OWLNamedIndividual> individuals = modelLanguageRecommendationService(irimodellingConstruct);
			System.out.println("Number of recommendations:" + individuals.size());
			for (OWLNamedIndividual ind : individuals) {
				Recommendation sug = sugList.get(ind.getIRI());
				sug.setScoreModelLanguageRecommendationService(properties.getWeightRuleBasedRecommendationService() 
						* properties.getScoreModelLanguageRecommendationService());
				sug.setScore(sug.getScore() + properties.getWeightRuleBasedRecommendationService() 
						* properties.getScoreModelLanguageRecommendationService());
			}
		}

		/*//Rule-based recommendation Service
		if (properties.isRuleBasedRecommendationService()){

			Set<OWLNamedIndividual> individuals = filterIndividuals(ruleBasedRecommendationService(irimodellingConstruct));

			for (OWLNamedIndividual ind : individuals) {
				Recommendation sug = sugList.get(ind.getIRI());
				sug.setScore(sug.getScore() + properties.getWeightRuleBasedRecommendationService() *
						properties.scoreRuleBasedRecommendationService);
				sug.setScoreRuleBasedREcommendationService(properties.getWeightRuleBasedRecommendationService() *
						properties.scoreRuleBasedRecommendationService);
			}
		}*/
		
		//Rule-based recommendation Service
		if (properties.isRuleBasedRecommendationService()){
			for(OWLAxiom owlAxiom: getManager().getRulesO().getAxioms()){
				Set<OWLNamedIndividual> individuals = filterIndividuals(ruleBasedRecommendationService2(irimodellingConstruct, owlAxiom));
				for (OWLNamedIndividual ind : individuals) {
					Recommendation sug = sugList.get(ind.getIRI());
					sug.setScore(sug.getScore() + properties.getWeightRuleBasedRecommendationService() *
								properties.scoreRuleBasedRecommendationService);
						sug.setScoreRuleBasedREcommendationService(sug.getScoreRuleBasedRecommendationService() + properties.getWeightRuleBasedRecommendationService() *
								properties.scoreRuleBasedRecommendationService);
					}
				}
		}


		//Label-based recommendation Service
		if (properties.isLabelBasedRecommendationService()){
			for (Recommendation sug : sugList.values()) {
				double max = 0.0;
				max = getStringDistance(label,sug.getIri().getFragment().toLowerCase());
				Set<String> synonyms = getSynonyms(label);

				for (String synonym : synonyms) {
					double score = getStringDistance(synonym, label);
					if(score > max)
						max = score;
				}

				sug.setScore(sug.getScore() + properties.getWeightLabelBasedRecommendationService() * max);
				sug.setScoreLabelBasedRecommendationService(max * properties.getWeightLabelBasedRecommendationService() * max);
			}
		}

		SortedSet<Recommendation> sortedSugList = new TreeSet<Recommendation>(); 
		for (Recommendation sug : sugList.values()) {
			//System.out.println(cls.getIRI());
			sortedSugList.add(sug);
		}


		//AuditTrailEntry entry = new AuditTrailEntry("Generate recommendation");
		//entry.setAttribute("Model Construct", irimodellingConstruct); 
		//entry.setAttribute("Entered label", label);
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
		
		printOWLIndividuals(filterIndividuals(extendedIndividuals), "Resultaat Rule RS");

		return extendedIndividuals;

	}
	

	protected Set<OWLNamedIndividual> ruleBasedRecommendationService2(String irimodellingConstruct, OWLAxiom axiom){
		
		Set<OWLNamedIndividual> extendedIndividuals = null;
		
		try {
		IRI ontologyIRI = IRI.create("www.mis.ugent.be/ontologies/temp3.owl");
		OWLOntology tempO;
		
			tempO = manager.getOWLManager().createOntology(ontologyIRI);
		
		OWLDataFactory fac = manager.getOWLManager().getOWLDataFactory();
		
		System.out.println(manager.getModelO().getOntologyID().getOntologyIRI());
		OWLImportsDeclaration importModelDeclaraton =
				fac.getOWLImportsDeclaration(manager.getModelO().getOntologyID().getOntologyIRI());
		manager.getOWLManager().applyChange(new AddImport(tempO, importModelDeclaraton));
		
		for(OWLImportsDeclaration imports : manager.getRulesO().getImportsDeclarations()){
			manager.getOWLManager().applyChange(new AddImport(tempO, imports));
		}
		
		OWLClass owlClass = fac.getOWLClass(IRI
				.create(irimodellingConstruct));
		
		System.out.println("Before: " + tempO.getAxiomCount());

		OWLNamedIndividual element = fac.getOWLNamedIndividual(IRI.create("http://www.mis.ugent.be/ontologies/model" + "#" + "test2"));
		OWLClassAssertionAxiom classAssertion = fac.getOWLClassAssertionAxiom(owlClass, element);
		AddAxiom addIndividual = new AddAxiom(tempO, classAssertion);
		// We now use the manager to apply the change
		manager.getOWLManager().applyChange(addIndividual);
		
		AddAxiom addAxiom = new AddAxiom(tempO, axiom);
		manager.getOWLManager().applyChange(addAxiom);
		System.out.println("While: " + tempO.getAxiomCount());


		org.semanticweb.HermiT.Reasoner reasoner = new Reasoner(tempO);
		reasoner.flush();
		OWLObjectProperty hasOntologyAnnotation = fac.getOWLObjectProperty(IRI
				.create(manager.getSemanticAnnotationProperty()));


		NodeSet<OWLNamedIndividual> individuals3 = reasoner.getObjectPropertyValues(element, hasOntologyAnnotation.getSimplified());
		extendedIndividuals = individuals3.getFlattened();

		for(Node<OWLNamedIndividual> individual: individuals3){
			OWLClass owlClass2 = fac.getOWLClass(IRI
					.create(individual.getRepresentativeElement().getIRI().toString()));
			NodeSet<OWLClass> subClses = reasoner.getSubClasses(owlClass2, false);
			for(OWLClass owlClass3: subClses.getFlattened()){
				extendedIndividuals.add(fac.getOWLNamedIndividual(owlClass3.getIRI()));
			}
		}
		
		manager.getOWLManager().removeOntology(tempO);

		/*OWLEntityRemover remover1 = new OWLEntityRemover(manager.getOWLManager(), Collections.singleton(tempO));
		element.accept(remover1);
		manager.getOWLManager().applyChanges(remover1.getChanges());
		
		
		manager.getOWLManager().removeAxiom(tempO, axiom);
		System.out.println("After: " + manager.getModelO().getAxiomCount());*/
		
		reasoner.flush();
		
		printOWLIndividuals(filterIndividuals(extendedIndividuals), "Resultaat Rule RS for axiom " + axiom.toString());
		
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
	
	public void printOWLIndividuals(Set<OWLNamedIndividual> clses, String title){
		System.out.println();
		System.out.println(title + "(size=" + clses.size() + ")");
		System.out.println("--------------------------");
		for(OWLNamedIndividual ocl: clses)
			System.out.println(ocl.getIRI().toString());
		System.out.println();

	}

}