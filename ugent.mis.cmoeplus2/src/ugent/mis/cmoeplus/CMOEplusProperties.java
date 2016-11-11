package ugent.mis.cmoeplus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

public class CMOEplusProperties {
		
	protected boolean labelBasedRecommendationService = true;					
	protected boolean labelBasedRecommendationServiceSynonyms = true;					
	protected boolean modelLanguageRecommendationService = true;				
	protected boolean ruleBasedRecommendationService = true;		
	protected double weightLabelBasedRecommendationService = 1.0;				
	protected double weightModelLanguageRecommendationService = 1.0;				
	protected double scoreModelLanguageRecommendationService = 1.0;				
	protected double weightRuleBasedRecommendationService= 1.0;				
	protected double scoreRuleBasedRecommendationService = 1.0;				
	protected boolean makeNewOntology=true;
	protected boolean annotateWhenSuggestionClicked=true;
	protected String canAnnQua="*";
	public boolean CandidateAnnotationPossiblity=true;
	public String CoO_filename=""; //Filename core ontology
	public String MLO_filename=""; //Filename MLO
	public String ESO_filename =""; //Filename Enterprise-specific Ontology
	public String CoO_MLO_filename=""; //Filename mapping core ontology and MLO
	public String rulesO_filename=""; //Filename rulesfile
	public String modelO_filename=""; //Filename model ontology
	protected String semAnnO_filename ="";
	protected String source;
	
	
	public CMOEplusProperties(FileInputStream fileInput) {
		
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
				case "CoO_file":
					CoO_filename=value;
					break;
				case "MLO_file":
					MLO_filename=value;
					break;
				case "CoO_MLO_file":
					CoO_MLO_filename=value;
					break;
				case "ESO_file":
					ESO_filename=value;
					break;
				case "ModelO_file":
					modelO_filename=value;
					break;
				case "RulesO_file":
					rulesO_filename=value;
					break;
				case "SemAnnO_file":
					semAnnO_filename =value;
					break;
				}

			}
		} catch (FileNotFoundException e) {
			System.out.println("test1");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("test2");
		}
	}
	

	public boolean isLabelBasedRecommendationService() {
		return labelBasedRecommendationService;
	}

	public boolean isLabelBasedRecommendationServiceSynonyms() {
		return labelBasedRecommendationServiceSynonyms;
	}

	public boolean isModelLanguageRecommendationService() {
		return modelLanguageRecommendationService;
	}

	public boolean isRuleBasedRecommendationService() {
		return ruleBasedRecommendationService;
	}

	public double getWeightLabelBasedRecommendationService() {
		return weightLabelBasedRecommendationService;
	}

	public double getWeightModelLanguageRecommendationService() {
		return weightModelLanguageRecommendationService;
	}

	public double getScoreModelLanguageRecommendationService() {
		return scoreModelLanguageRecommendationService;
	}

	public double getWeightRuleBasedRecommendationService() {
		return weightRuleBasedRecommendationService;
	}

	public double getScoreRuleBasedRecommendationService() {
		return scoreRuleBasedRecommendationService;
	}

	public boolean isMakeNewOntology() {
		return makeNewOntology;
	}

	public boolean isAnnotateWhenSuggestionClicked() {
		return annotateWhenSuggestionClicked;
	}

	public String getCanAnnQua() {
		return canAnnQua;
	}

	public boolean isCandidateAnnotationPossiblity() {
		return CandidateAnnotationPossiblity;
	}

	public String getCoO_filename() {
		return CoO_filename;
	}

	public String getMLO_filename() {
		return MLO_filename;
	}

	public String getESO_filename() {
		return ESO_filename;
	}

	public String getCoO_MLO_filename() {
		return CoO_MLO_filename;
	}

	public String getRulesO_filename() {
		return rulesO_filename;
	}

	public String getModelO_filename() {
		return modelO_filename;
	}

	public String getSemAnnO_filename() {
		return semAnnO_filename;
	}

	public String getSource() {
		return source;
	}
	

}
