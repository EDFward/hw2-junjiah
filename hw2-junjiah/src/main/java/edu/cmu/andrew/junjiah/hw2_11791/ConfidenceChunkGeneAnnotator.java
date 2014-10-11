package edu.cmu.andrew.junjiah.hw2_11791;

import java.util.Iterator;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.AnalysisComponent_ImplBase;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.deiis.types.Annotation;

/**
 * A confidence chunker from LingPipe library. Used external model to chunk genes.
 * 
 * @author junjiah
 */
public class ConfidenceChunkGeneAnnotator extends JCasAnnotator_ImplBase {

  /**
   * Indicates the location of external model file. Mandatory.
   */
  public static final String PARAM_MODELFILE = "ModelFile";

  /**
   * Confidence chunker used for gene detection.
   */
  private ConfidenceChunker chunker;

  /**
   * 
   * @see AnalysisComponent_ImplBase#initialize(org.apache.uima.UimaContext)
   * 
   *      Initialize the confidence chunker using the provided external model file.
   */
  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    String modelFilePath = (String) aContext.getConfigParameterValue(PARAM_MODELFILE);
    try {
      chunker = (ConfidenceChunker) AbstractExternalizable.readResourceObject(modelFilePath);
    } catch (Exception e) {
      System.err.println("ERROR: cannot initialize the confidence chunker.");
      e.printStackTrace();
      System.exit(1);
    }
  };

  /**
   * 
   * @see JCasAnnotator_ImplBase#process(org.apache.uima.jcas.JCas)
   * 
   *      Read a document/sentence, do chunking and store the probability to the annotation.
   */
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    String docText = aJCas.getDocumentText();
    char[] docChars = docText.toCharArray();
    Iterator<Chunk> it = chunker.nBestChunks(docChars, 0, docChars.length, 6);
    // chunk the document text and add annotation
    while (it.hasNext()) {
      Chunk chunk = it.next();
      double conf = Math.pow(2.0, chunk.score());
      int startOffset = chunk.start();
      int endOffset = chunk.end();
      // add to index
      Annotation geneAnnotation = new Annotation(aJCas);
      geneAnnotation.setCasProcessorId("LingPipeConfidenceNER");
      geneAnnotation.setConfidence(conf);
      geneAnnotation.setBegin(startOffset);
      geneAnnotation.setEnd(endOffset);
      geneAnnotation.addToIndexes();
    }
  }

}
