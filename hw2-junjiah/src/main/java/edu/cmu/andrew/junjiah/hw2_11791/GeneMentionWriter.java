package edu.cmu.andrew.junjiah.hw2_11791;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.collection.base_cpm.CasObjectProcessor;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.deiis.types.Annotation;
import edu.cmu.deiis.types.DocumentId;

/**
 * Writes annotation results to a output file conforming to the format specified in the writeup.
 * 
 * @author junjiah
 *
 */
public class GeneMentionWriter extends CasConsumer_ImplBase {

  /**
   * Wrapper class for Annotation score aggregation
   * 
   * @author junjiah
   *
   */
  class AnnotationEntry {
    /**
     * Wrapped Annotation.
     */
    protected Annotation annotation;

    /**
     * Beginning offset of the annotation.
     */
    protected int begin;

    /**
     * Ending offset of the annotation
     */
    protected int end;

    public AnnotationEntry(Annotation annotation) {
      this.annotation = annotation;
      this.begin = annotation.getBegin();
      this.end = annotation.getEnd();
    }

    /**
     * Simply add up the offsets.
     */
    @Override
    public int hashCode() {
      return this.begin + this.end;
    }

    /**
     * Consider two wrapper equal if their offsets are identical.
     */
    @Override
    public boolean equals(Object o) {
      if (!(o instanceof AnnotationEntry))
        return false;

      AnnotationEntry ae = (AnnotationEntry) o;
      return this.begin == ae.begin && this.end == ae.end;
    }
  }

  /**
   * Only accept annotations with scores higher than this threshold as the final annotation.
   */
  private static final double scoreThreshold = 0.5;

  /**
   * Indicates the location of output file. Mandatory.
   */
  public static final String PARAM_OUTPUTFILE = "OutputFile";

  /**
   * Stores annotated genes. Writes them to a file after process completion.
   */
  private List<String> annotatedResults;

  @Override
  public void initialize() throws org.apache.uima.resource.ResourceInitializationException {
    annotatedResults = new ArrayList<String>();
  };

  /**
   * Takes the NE out from CAS and generates the output string.
   * 
   * @see CasObjectProcessor#processCas(org.apache.uima.cas.CAS)
   */
  @Override
  public void processCas(CAS aCAS) throws ResourceProcessException {
    JCas jcas;
    try {
      jcas = aCAS.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }

    String docText = jcas.getDocumentText();
    FSIterator<?> neIter = jcas.getAnnotationIndex(Annotation.type).iterator();
    // get the document ID
    DocumentId docId = (DocumentId) jcas.getAnnotationIndex(DocumentId.type).iterator().next();

    // aggregate scores from different annotators
    List<Annotation> annotations = aggregateAnnotations(neIter);

    // process the named entity one by one with scores higher than threshold
    // and form the output string
    for (Annotation ne : annotations) {
      int begin = ne.getBegin(), end = ne.getEnd();
      int[] offset = getOffsetWithoutSpace(docText, begin, end);
      // format the string
      String neString = String.format("%s|%d %d|%s", docId.getId(), offset[0], offset[1],
              docText.substring(begin, end));
      annotatedResults.add(neString);
    }
  }

  /**
   * Write results to the output file after completing processing CAS.
   * 
   * @see org.apache.uima.collection.CasConsumer_ImplBase#collectionProcessComplete(org.apache.uima.util
   *      .ProcessTrace)
   */
  @Override
  public void collectionProcessComplete(ProcessTrace arg0) throws IOException,
          UnsupportedEncodingException {
    File outputFile = new File((String) getConfigParameterValue(PARAM_OUTPUTFILE));
    try {
      if (!outputFile.exists())
        outputFile.createNewFile();
    } catch (IOException e) {
      System.err.println("ERROR: cannot write output to the specified file.");
      System.exit(1);
    }

    // perform writing
    PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
    for (String geneString : annotatedResults) {
      writer.println(geneString);
    }
    writer.close();
  }

  /**
   * Counts the white spaces and returns the offsets excluding them.
   * 
   * @param string
   *          The original document text contains the annotated NE.
   * @param start
   *          The original start position of the annotation.
   * @param end
   *          The original end position of the annotation.
   * @return Offset of NE after excluding white spaces.
   */
  private int[] getOffsetWithoutSpace(String string, int start, int end) {
    int countWhitespace = 0, i = 0;
    int[] offset = new int[2];
    for (; i < start; ++i) {
      if (string.charAt(i) == ' ')
        ++countWhitespace;
    }
    offset[0] = start - countWhitespace;

    for (; i < end; ++i) {
      if (string.charAt(i) == ' ')
        ++countWhitespace;
    }
    offset[1] = end - countWhitespace - 1;
    return offset;
  }

  /**
   * Returns the weighted score of an annotation depending on its annotator. The weights are picked
   * as heuristics.
   * 
   * @param Annotation
   *          Source annotation.
   * @return Aggregate scores.
   */
  private double weightedAnnotationScore(Annotation annotation) {
    if (annotation.getCasProcessorId().equals("LingPipeFirstBestNER"))
      return 0.28 * annotation.getConfidence();
    else if (annotation.getCasProcessorId().equals("LingPipeConfidenceNER"))
      return 0.46 * annotation.getConfidence();
    else if (annotation.getCasProcessorId().equals("ABNER"))
      return 0.18 * annotation.getConfidence();
    else if (annotation.getCasProcessorId().equals("StanfordNER"))
      return 0.08 * annotation.getConfidence();
    else {
      System.err.println("ERROR: wrong cas processor ID");
      return 0;
    }

  }

  /**
   * Aggregate annotation scores from different annotators, and only accept those with scores higher
   * than the threshold.
   * 
   * @param neIter
   *          Iterator of the whole annotation.
   * @return Accepted annotations with scores higher than the threshold.
   */
  private List<Annotation> aggregateAnnotations(FSIterator<?> neIter) {
    Map<AnnotationEntry, Double> annotationScores = new HashMap<GeneMentionWriter.AnnotationEntry, Double>();
    List<Annotation> validAnnotations = new ArrayList<Annotation>();

    // iterate each annotation and put into the score dictionary
    while (neIter.hasNext()) {
      Annotation gene = (Annotation) neIter.next();
      AnnotationEntry geneWrapper = new AnnotationEntry(gene);
      double score = 0;
      if (annotationScores.containsKey(geneWrapper)) {
        score = annotationScores.get(geneWrapper);
      }
      // update the score
      annotationScores.put(geneWrapper, weightedAnnotationScore(gene) + score);
    }

    // filter annotations, accept those with scores higher than threshold
    for (Map.Entry<AnnotationEntry, Double> entry : annotationScores.entrySet()) {
      double score = entry.getValue();
      if (score > scoreThreshold)
        validAnnotations.add(entry.getKey().annotation);
    }

    return validAnnotations;
  }
}
