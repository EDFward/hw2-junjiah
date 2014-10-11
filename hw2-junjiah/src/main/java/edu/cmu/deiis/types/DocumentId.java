

/* First created by JCasGen Wed Oct 01 20:14:05 EDT 2014 */
package edu.cmu.deiis.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Fri Oct 10 17:04:58 EDT 2014
 * XML source: /Users/hejunjia1911/Workspace/11791/hw2-junjiah/hw2-junjiah/src/main/resources/hw2-junjiah-aae.xml
 * @generated */
public class DocumentId extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DocumentId.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected DocumentId() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public DocumentId(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public DocumentId(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public DocumentId(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
  //*--------------*
  //* Feature: Id

  /** getter for Id - gets 
   * @generated
   * @return value of the feature 
   */
  public String getId() {
    if (DocumentId_Type.featOkTst && ((DocumentId_Type)jcasType).casFeat_Id == null)
      jcasType.jcas.throwFeatMissing("Id", "edu.cmu.deiis.types.DocumentId");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DocumentId_Type)jcasType).casFeatCode_Id);}
    
  /** setter for Id - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setId(String v) {
    if (DocumentId_Type.featOkTst && ((DocumentId_Type)jcasType).casFeat_Id == null)
      jcasType.jcas.throwFeatMissing("Id", "edu.cmu.deiis.types.DocumentId");
    jcasType.ll_cas.ll_setStringValue(addr, ((DocumentId_Type)jcasType).casFeatCode_Id, v);}    
  }

    