package edu.cmu.andrew.junjiah.hw2_11791;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import abner.Tagger;
import edu.cmu.deiis.types.Annotation;

/**
 * 
 * @author junjiah
 *
 */
public class AbnerGeneAnnotator extends JCasAnnotator_ImplBase {

  /**
   * Named tagger for DNA/RNA/Protein.
   */
  private Tagger tagger;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    tagger = new Tagger();
  }

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    String docText = aJCas.getDocumentText();
    // tag the document text
    String[][] ents = tagger.getEntities(docText);
    for (int i = 0; i < ents[0].length; i++) {
      String ent = ents[0][i];
      int startOffset = docText.indexOf(ent);
      // tokenization may cause string match failure, ignore such case
      if (startOffset == -1)
        continue;

      int endOffset = startOffset + ent.length();
      // now add annotation
      Annotation geneAnnotation = new Annotation(aJCas);
      geneAnnotation.setCasProcessorId("ABNER");
      geneAnnotation.setConfidence(1d);
      geneAnnotation.setBegin(startOffset);
      geneAnnotation.setEnd(endOffset);
      geneAnnotation.addToIndexes();
    }
  }
}
