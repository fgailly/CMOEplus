package ugent.mis.cmoeplus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cheetahplatform.common.logging.AuditTrailEntry;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

public class OntologyManager {
	protected OWLOntology CoO; // Foundational ontology 
	protected OWLOntology MLO; // Modeling Language ontology
	protected OWLOntology CoO_MLO; //Ontological analysis Modeling langauge ontology with foundational ontology
	protected OWLOntology ESO; //Enterprise-specific ontology

	protected OWLOntologyManager owlManager;
	protected OWLOntology mergedO; //Ontology that merges all ontologies
	protected OWLOntology modelO; // Model Ontology
	protected OWLOntology rulesO; // Rules Ontology
	protected OWLOntology semAnnO; // Rules Ontology
	
	protected final String semanticAnnotationProperty 
	= "http://www.mis.ugent.be/ontologies/SemanticAnnotation.owl#representationClassIsAnnotedByDomainClass";
	
	
	public OntologyManager() {
		
	}
	public OWLOntology getCoO() {
		return CoO;
	}
	public OWLOntology getMLO() {
		return MLO;
	}
	public OWLOntology getCoO_MLO() {
		return CoO_MLO;
	}
	public OWLOntology getESO() {
		return ESO;
	}
	public OWLOntologyManager getOWLManager() {
		return owlManager;
	}
	public OWLOntology getMergedO() {
		return mergedO;
	}
	public OWLOntology getModelO() {
		return modelO;
	}
	public OWLOntology getRulesO() {
		return rulesO;
	}
	public OWLOntology getSemAnnO() {
		return semAnnO;
	}
	public String getSemanticAnnotationProperty() {
		return semanticAnnotationProperty;
	}
	public void mergeOntology(String uriMergedOntology){
		OWLOntologyMerger merger = new OWLOntologyMerger(owlManager);
		// We merge all of the loaded ontologies. Since an OWLOntologyManager is
		// an OWLOntologySetProvider we just pass this in. We also need to
		// specify the URI of the new ontology that will be created.
		IRI mergedOntologyIRI = IRI.create("http://www.mis.ugent.be/ontologies/mymerge");

		try {
			mergedO = merger.createMergedOntology(owlManager, mergedOntologyIRI);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadModelOntology(File file){
		try {

			modelO = owlManager.loadOntologyFromOntologyDocument(file);

			System.out.println("Loaded ontology: " + modelO);
		} catch (OWLOntologyCreationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

		}
	}

	public void makeModelOntology(String filename){

		try {
			IRI ontologyIRI = IRI.create("www.mis.ugent.be/ontologies/" + filename);
			modelO = owlManager.createOntology(ontologyIRI);
			OWLDataFactory fac = owlManager.getOWLDataFactory();
			OWLImportsDeclaration importBPMNDeclaraton =
					fac.getOWLImportsDeclaration(MLO.getOntologyID().getOntologyIRI());
			owlManager.applyChange(new AddImport(modelO, importBPMNDeclaraton));
			OWLImportsDeclaration importSemAnnDeclaraton =
					fac.getOWLImportsDeclaration(semAnnO.getOntologyID().getOntologyIRI());
			owlManager.applyChange(new AddImport(modelO, importSemAnnDeclaraton));
			OWLImportsDeclaration importESODeclaraton =
					fac.getOWLImportsDeclaration(ESO.getOntologyID().getOntologyIRI());
			owlManager.applyChange(new AddImport(modelO, importESODeclaraton));
			OWLImportsDeclaration importRulesDeclaraton =
					fac.getOWLImportsDeclaration(rulesO.getOntologyID().getOntologyIRI());
			owlManager.applyChange(new AddImport(modelO, importRulesDeclaraton));



		} catch (OWLOntologyCreationException e1 ) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Created ontology: " + modelO);
	}
	
	public IRI addModelInstance(String iriConstruct, String iriIndividual, String label){
		OWLDataFactory fac = owlManager.getOWLDataFactory();
		OWLClass constructClass = fac.getOWLClass(IRI
				.create(iriConstruct));

		//OWLNamedIndividual element = fac.getOWLNamedIndividual(IRI.create("http://www.mis.ugent.be/ontologies/model" + "#" + id));
		OWLNamedIndividual element = fac.getOWLNamedIndividual(IRI.create(iriIndividual));

		
		OWLClassAssertionAxiom classAssertion = fac.getOWLClassAssertionAxiom(constructClass, element);
		AddAxiom addAxiom = new AddAxiom(modelO, classAssertion);
		// We now use the manager to apply the change
		owlManager.applyChange(addAxiom);


		OWLAnnotation labelAnno = fac.getOWLAnnotation(fac.getRDFSLabel(), fac.getOWLLiteral(label));
		OWLAxiom ax = fac.getOWLAnnotationAssertionAxiom(element.getIRI(), labelAnno);
		AddAxiom addAxiom2 = new AddAxiom(modelO, ax);
		owlManager.applyChange(addAxiom2);

		AuditTrailEntry entry = new AuditTrailEntry("Add Model Element");
		entry.setAttribute("Element Type", iriConstruct); 
		entry.setAttribute("Element ", element.getIRI().toString()); 
		//this.log(entry);


		System.out.println("Updated ontology: " + modelO);
		return element.getIRI();

	}

	public OWLObjectPropertyAssertionAxiom addModelRelationship(String iriConstructRelationship, String iriElement1, String iriElement2){
		OWLDataFactory fac = owlManager.getOWLDataFactory();

		//OWLNamedIndividual element = fac.getOWLNamedIndividual(IRI.create("iriElement1"));


		OWLIndividual element1 = fac.getOWLNamedIndividual(IRI.create(iriElement1));
		OWLIndividual element2 = fac.getOWLNamedIndividual(IRI.create(iriElement2));
		// We want to link the subject and object with the hasFather property,
		// so use the data factory to obtain a reference to this object
		// property.
		OWLObjectProperty relationship = fac.getOWLObjectProperty(IRI.create(iriConstructRelationship));
		// Now create the actual assertion (triple), as an object property
		// assertion axiom matthew --> hasFather --> peter
		OWLObjectPropertyAssertionAxiom assertion = fac.getOWLObjectPropertyAssertionAxiom(relationship, element1, element2);
		// Finally, add the axiom to our ontology and save
		AddAxiom addAxiomChange = new AddAxiom(modelO, assertion);
		owlManager.applyChange(addAxiomChange);

		AuditTrailEntry entry = new AuditTrailEntry("Add Model Relationship");
		entry.setAttribute("Element Type", iriConstructRelationship); 
		entry.setAttribute("Element1 ", element1.toString()); 
		entry.setAttribute("Element2 ", element2.toString()); 
		//this.log(entry);

		System.out.println("Updated ontology: " + modelO);
		return assertion;

	}

	public IRI addModelAnnotation(String iriModelElement, String iriOntologyElement, Recommendation suggestion){
		OWLDataFactory fac = owlManager.getOWLDataFactory();

		OWLNamedIndividual modelElement = fac.getOWLNamedIndividual(IRI.create(iriModelElement));
		OWLNamedIndividual ontologyElement = fac.getOWLNamedIndividual(IRI.create(iriOntologyElement));

		OWLObjectProperty relationship = fac.getOWLObjectProperty(IRI.create(semanticAnnotationProperty));
		// Now create the actual assertion (triple), as an object property
		// assertion axiom matthew --> hasFather --> peter
		OWLObjectPropertyAssertionAxiom assertion = fac.getOWLObjectPropertyAssertionAxiom(relationship, modelElement, ontologyElement);
		// Finally, add the axiom to our ontology and save
		AddAxiom addAxiomChange = new AddAxiom(modelO, assertion);
		owlManager.applyChange(addAxiomChange);

		AuditTrailEntry entry = new AuditTrailEntry("Add Model Annotation");

		entry.setAttribute("Model Element", iriModelElement.toString()); 
		entry.setAttribute("Ontology Element ", ontologyElement.toString()); 
		entry.setAttribute("Score",Double.toString(suggestion.getScore()));
		entry.setAttribute("Score ModelLanguageRecommendationService",Double.toString(suggestion.getScoreModelLanguageRecommendationService()));
		entry.setAttribute("Score LabelMatchingRecommendationService",Double.toString(suggestion.getWeightWordnetSynonyms()));
		entry.setAttribute("Order in Suggestion List",Integer.toString(suggestion.getOrder()));

		//this.log(entry);

		System.out.println("Updated ontology: " + modelO);


		return modelElement.getIRI();

	}

	public IRI removeModelAnnotation(String iriElement){
		OWLDataFactory fac = owlManager.getOWLDataFactory();

		OWLNamedIndividual modelElement = fac.getOWLNamedIndividual(IRI.create(modelO.getOntologyID().getOntologyIRI().toString() + "#" +iriElement));
		OWLObjectProperty relationship = fac.getOWLObjectProperty(IRI.create(semanticAnnotationProperty));

		Set<OWLIndividual> domainElements = modelElement.getObjectPropertyValues(relationship, modelO);
		//OWLAxiomVisitor remover = new OWLAxiomVisitor(m,Collections.singleton(modelOntology));

		Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		for (OWLIndividual domainElement : domainElements)
		{
			OWLObjectPropertyAssertionAxiom assertion =
					fac.getOWLObjectPropertyAssertionAxiom(relationship, modelElement, domainElement);
			RemoveAxiom remover = new RemoveAxiom(modelO, assertion);
			changes.add(remover);

		}
		List<OWLOntologyChange> list = new ArrayList<OWLOntologyChange>(changes);
		owlManager.applyChanges(list);

		AuditTrailEntry entry = new AuditTrailEntry("Remove ELement Annotation");
		entry.setAttribute("Element", iriElement.toString());  
		//this.log(entry);


		System.out.println("Updated ontology: " + modelO);
		return modelElement.getIRI();

	}
	public void printOWLClasses(Set<OWLClass> clses, String title){
		System.out.println();
		System.out.println(title + "(size=" + clses.size() + ")");
		System.out.println("--------------------------");
		for(OWLClass ocl: clses)
			System.out.println(ocl.getIRI());
		System.out.println();

	}

	public void printOWLDataProperties(Set<OWLDataProperty> dataProperties, String title){
		System.out.println();
		System.out.println(title + "(size=" + dataProperties.size() + ")");
		System.out.println("--------------------------");
		for(OWLDataProperty prop: dataProperties)
			System.out.println(prop.getIRI());
		System.out.println();

	}
	

}
