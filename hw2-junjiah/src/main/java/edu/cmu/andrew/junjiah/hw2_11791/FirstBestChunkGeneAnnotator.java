package edu.cmu.andrew.junjiah.hw2_11791;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.AnalysisComponent_ImplBase;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.deiis.types.Annotation;

/**
 * A First-Best Named Entity Annotator from LingPipe library. Used external model to chunk named
 * entities. Faster and more accurate than the provided NER.
 * 
 * @author junjiah
 *
 */
public class FirstBestChunkGeneAnnotator extends JCasAnnotator_ImplBase {

  /**
   * Indicates the location of external model file. Mandatory.
   */
  public static final String PARAM_MODELFILE = "ModelFile";

  /**
   * Chunker used for gene detection.
   */
  private Chunker chunker;

  /**
   * 
   * @see AnalysisComponent_ImplBase#initialize(org.apache.uima.UimaContext)
   * 
   *      Initialize the chunker using the provided external model file.
   */
  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    String modelFilePath = (String) aContext.getConfigParameterValue(PARAM_MODELFILE);
    try {
      chunker = (Chunker) AbstractExternalizable.readResourceObject(modelFilePath);
    } catch (Exception e) {
      System.err.println("ERROR: cannot initialize the first best chunker.");
      e.printStackTrace();
      System.exit(1);
    }
  };

  /**
   * 
   * @see JCasAnnotator_ImplBase#process(org.apache.uima.jcas.JCas)
   * 
   *      Read a document/sentence and chunk it. Store every chunk as an annotation with confidence
   *      1.0d.
   */
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    String docText = aJCas.getDocumentText();
    // chunk the document text and add annotation
    for (Chunk geneChunk : chunker.chunk(docText).chunkSet()) {
      Annotation geneAnnotation = new Annotation(aJCas);
      geneAnnotation.setCasProcessorId("LingPipeFirstBestNER");
      // score is provided by the chunker
      geneAnnotation.setConfidence(1d);
      geneAnnotation.setBegin(geneChunk.start());
      geneAnnotation.setEnd(geneChunk.end());
      geneAnnotation.addToIndexes();
    }
  }
}
