package edu.cmu.andrew.junjiah.hw2_11791;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.deiis.types.Annotation;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

/**
 * Based on Stanford NER, and trained on English 3-class corpus.
 * 
 * @author junjiah
 *
 */
public class StanfordNerAnnotator extends JCasAnnotator_ImplBase {

  /**
   * Indicates the location of external model file. Mandatory.
   */
  public static final String PARAM_MODELFILE = "ModelFile";

  /*
   * (non-Javadoc)
   * 
   * @see AnalysisComponent_ImplBase#initialize(org.apache.uima.UimaContext)
   */
  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    String modelFilePath = (String) aContext.getConfigParameterValue(PARAM_MODELFILE);
    try {
      classifier = CRFClassifier.getClassifier(modelFilePath);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("ERROR: cannot initialize the classifier.");
      System.exit(1);
    }
  };

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    String docText = aJCas.getDocumentText();
    // classify the document text
    List<Triple<String, Integer, Integer>> classifiedResults = classifier
            .classifyToCharacterOffsets(docText);

    for (Triple<String, Integer, Integer> gene : classifiedResults) {
      Annotation geneAnnotation = new Annotation(aJCas);
      geneAnnotation.setCasProcessorId("StanfordNER");
      geneAnnotation.setConfidence(1d);
      geneAnnotation.setBegin(gene.second);
      geneAnnotation.setEnd(gene.third);
      geneAnnotation.addToIndexes();
    }
  }

  /**
   * Stanford NER classifier.
   */
  private AbstractSequenceClassifier<CoreLabel> classifier;
}
