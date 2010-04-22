/*
 * Copyright 2010 NLR - National Aerospace Laboratory
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

package org.jecars.client;

import javax.jcr.PropertyType;

/** JC_PropertyType
 *
 */
public class JC_PropertyType {

  public static final int TYPE_STRING    = PropertyType.STRING;
  public static final int TYPE_BINARY    = PropertyType.BINARY;
  public static final int TYPE_LONG      = PropertyType.LONG;
  public static final int TYPE_DOUBLE    = PropertyType.DOUBLE;
  public static final int TYPE_DATE      = PropertyType.DATE;
  public static final int TYPE_BOOLEAN   = PropertyType.BOOLEAN;
  public static final int TYPE_NAME      = PropertyType.NAME;
  public static final int TYPE_PATH      = PropertyType.PATH;
  public static final int TYPE_REFERENCE = PropertyType.REFERENCE;
  public static final int TYPE_WEAKREFERENCE = PropertyType.WEAKREFERENCE;
  public static final int TYPE_URI       = PropertyType.URI;
  public static final int TYPE_UNDEFINED = PropertyType.UNDEFINED;
    
  public static final String TYPENAME_STRING = PropertyType.TYPENAME_STRING;
  public static final String TYPENAME_BINARY = PropertyType.TYPENAME_BINARY;
  public static final String TYPENAME_LONG = PropertyType.TYPENAME_LONG;
  public static final String TYPENAME_DOUBLE = PropertyType.TYPENAME_DOUBLE;
  public static final String TYPENAME_DATE = PropertyType.TYPENAME_DATE;
  public static final String TYPENAME_BOOLEAN = PropertyType.TYPENAME_BOOLEAN;
  public static final String TYPENAME_NAME = PropertyType.TYPENAME_NAME;
  public static final String TYPENAME_PATH = PropertyType.TYPENAME_PATH;
  public static final String TYPENAME_REFERENCE = PropertyType.TYPENAME_REFERENCE;
  public static final String TYPENAME_WEAKREFERENCE = PropertyType.TYPENAME_WEAKREFERENCE;
  public static final String TYPENAME_URI = PropertyType.TYPENAME_URI;
  public static final String TYPENAME_UNDEFINED = PropertyType.TYPENAME_UNDEFINED;

  private int mType = TYPE_UNDEFINED;

  /** setType
   *
   * @param pType
   */
  public void setType( final int pType ) {
    mType = pType;
    return;
  }

  /** getType
   *
   * @return
   */
  public int getType() {
    return mType;
  }

  /** nameFromValue
   *
   * @param pType
   * @return
   */
  public static String nameFromValue( final int pType ) {
    switch (pType) {
        case TYPE_STRING:            return TYPENAME_STRING;
        case TYPE_BINARY:            return TYPENAME_BINARY;
        case TYPE_BOOLEAN:           return TYPENAME_BOOLEAN;
        case TYPE_LONG:              return TYPENAME_LONG;
        case TYPE_DOUBLE:            return TYPENAME_DOUBLE;
        case TYPE_DATE:              return TYPENAME_DATE;
        case TYPE_NAME:              return TYPENAME_NAME;
        case TYPE_PATH:              return TYPENAME_PATH;
        case TYPE_REFERENCE:         return TYPENAME_REFERENCE;
        case TYPE_WEAKREFERENCE:     return TYPENAME_WEAKREFERENCE;
        case TYPE_URI:               return TYPENAME_URI;
    }
    return TYPENAME_UNDEFINED;
  }

  /** valueFromName
   *
   * @param pName
   * @return
   */
  public static int valueFromName( final String pName ) {
    if (pName.equals(TYPENAME_STRING))    {   return TYPE_STRING; }
    if (pName.equals(TYPENAME_BINARY))    {   return TYPE_BINARY; }
    if (pName.equals(TYPENAME_BOOLEAN))   {   return TYPE_BOOLEAN; }
    if (pName.equals(TYPENAME_LONG))      {   return TYPE_LONG; }
    if (pName.equals(TYPENAME_DOUBLE))    {   return TYPE_DOUBLE; }
    if (pName.equals(TYPENAME_DATE))      {   return TYPE_DATE; }
    if (pName.equals(TYPENAME_NAME))      {   return TYPE_NAME; }
    if (pName.equals(TYPENAME_PATH))      {   return TYPE_PATH; }
    if (pName.equals(TYPENAME_REFERENCE)) {   return TYPE_REFERENCE; }
    if (pName.equals(TYPENAME_WEAKREFERENCE)) {   return TYPE_WEAKREFERENCE; }
    if (pName.equals(TYPENAME_URI))       {   return TYPE_URI; }
    return TYPE_UNDEFINED;
  }

  
  
}
