package edu.cmu.andrew.junjiah.hw2_11791;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import edu.cmu.deiis.types.DocumentId;

/**
 * Extended CollectionReader to process the source document line by line, and separated the ID from
 * the text. Note the ID is passed to the CAS as an annotation.
 * 
 * @author junjiah
 *
 */
public class GeneDocumentReader extends CollectionReader_ImplBase {

  /**
   * Indicates the location of input file. Mandatory.
   */
  public static final String PARAM_INPUTFILE = "InputFile";

  /**
   * Stores the lines read from the input file. Process them one by one during getNext().
   */
  private List<String> documentLines;

  /**
   * Indicates the current index of documentLines.
   */
  private int currentIndex;

  /**
   * Reads the source document and store the lines into documentLines.
   * 
   * @see org.apache.uima.collection.CollectionReader_ImplBase#initialize()
   */
  @Override
  public void initialize() throws ResourceInitializationException {
    File file = new File(((String) getConfigParameterValue(PARAM_INPUTFILE)).trim());
    currentIndex = 0;
    documentLines = new ArrayList<String>();

    try {
      FileInputStream fileInputStream = new FileInputStream(file);
      BufferedReader bReader = new BufferedReader(new InputStreamReader(fileInputStream));
      String line = null;
      while ((line = bReader.readLine()) != null) {
        documentLines.add(line);
      }
    } catch (Exception e) {
      throw new ResourceInitializationException(
              ResourceConfigurationException.RESOURCE_DATA_NOT_VALID, new Object[] {
                  PARAM_INPUTFILE, this.getMetaData().getName(), file.getPath() });
    }

  }

  /**
   * Splits the line into Document ID and Document Text
   * 
   * @see org.apache.uima.collection.CollectionReader#getNext(org.apache.uima.cas.CAS)
   */
  @Override
  public void getNext(CAS aCAS) throws IOException, CollectionException {
    JCas jCas;
    try {
      jCas = aCAS.getJCas();
    } catch (CASException e) {
      throw new CollectionException(e);
    }

    String[] parts = documentLines.get(currentIndex++).split(" ", 2);
    String docId = parts[0], docText = parts[1];

    jCas.setDocumentText(docText);

    DocumentId documentId = new DocumentId(jCas);
    documentId.setId(docId);
    documentId.addToIndexes();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNext() throws IOException, CollectionException {
    return currentIndex < documentLines.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Progress[] getProgress() {
    return new Progress[] { new ProgressImpl(currentIndex, documentLines.size(), Progress.ENTITIES) };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() throws IOException {

  }

}
