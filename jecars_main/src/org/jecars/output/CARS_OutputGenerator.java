/*
 * Copyright 2007 NLR - National Aerospace Laboratory
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jecars.output;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.jecars.CARS_ActionContext;

/**
 * CARS_OutputGenerator
 *
 * @version $Id: CARS_OutputGenerator.java,v 1.2 2008/08/01 15:22:38 weertj Exp $
 */
public interface CARS_OutputGenerator {
 
  /** Is this output a RSS/Atom feed type
   * @return true for yes
   */
  public boolean isFeed();
  
  /** Create the header of the message
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   */
  public void createHeader( CARS_ActionContext pContext, StringBuilder pMessage );
  
  /** Create the footer of the message
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   */
  public void createFooter( CARS_ActionContext pContext, StringBuilder pMessage );

  /** Add node entry, the current (this) node
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   * @param pThisNode the this node
   * @param pFromNode from node number
   * @param pToNode to node number
   */
  public void addThisNodeEntry( CARS_ActionContext pContext, StringBuilder pMessage, Node pThisNode,
                                long pFromNode, long pToNode ) throws RepositoryException, Exception;

  /** Add child node entry
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   * @param pChildNode the this node
   * @param pNodeNo
   * @throws java.lang.Exception
   * @throws javax.jcr.RepositoryException
   */
  public void addChildNodeEntry( CARS_ActionContext pContext, StringBuilder pMessage, Node pChildNode, long pNodeNo ) throws Exception, RepositoryException;
  
}
