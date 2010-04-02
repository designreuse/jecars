/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.client;

/**
 *
 * @author weert
 */
public class JC_PropertyType {

  public static final int TYPE_STRING    = 1;
  public static final int TYPE_BINARY    = 2;
  public static final int TYPE_LONG      = 3;
  public static final int TYPE_DOUBLE    = 4;
  public static final int TYPE_DATE      = 5;
  public static final int TYPE_BOOLEAN   = 6;
  public static final int TYPE_NAME      = 7;
  public static final int TYPE_PATH      = 8;
  public static final int TYPE_REFERENCE = 9;
  public static final int TYPE_UNDEFINED = 10;
    
  public static final String TYPENAME_STRING = "String";
  public static final String TYPENAME_BINARY = "Binary";
  public static final String TYPENAME_LONG = "Long";
  public static final String TYPENAME_DOUBLE = "Double";
  public static final String TYPENAME_DATE = "Date";
  public static final String TYPENAME_BOOLEAN = "Boolean";
  public static final String TYPENAME_NAME = "Name";
  public static final String TYPENAME_PATH = "Path";
  public static final String TYPENAME_REFERENCE = "Reference";
  public static final String TYPENAME_UNDEFINED = "Undefined";

  private int mType = TYPE_UNDEFINED;
  
  public void setType( int pType ) {
    mType = pType;
    return;
  }
  
  public int getType() {
    return mType;
  }
  
  public static String nameFromValue( int pType ) {
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
    }
    return TYPENAME_UNDEFINED;
  }
  
  public static int valueFromName(String name) {
    if (name.equals(TYPENAME_STRING))      return TYPE_STRING;
    if (name.equals(TYPENAME_BINARY))      return TYPE_BINARY;
    if (name.equals(TYPENAME_BOOLEAN))     return TYPE_BOOLEAN;
    if (name.equals(TYPENAME_LONG))        return TYPE_LONG;
    if (name.equals(TYPENAME_DOUBLE))      return TYPE_DOUBLE;
    if (name.equals(TYPENAME_DATE))        return TYPE_DATE;
    if (name.equals(TYPENAME_NAME))        return TYPE_NAME;
    if (name.equals(TYPENAME_PATH))        return TYPE_PATH;
    if (name.equals(TYPENAME_REFERENCE))   return TYPE_REFERENCE;
    return TYPE_UNDEFINED;
  }

  
  
}
