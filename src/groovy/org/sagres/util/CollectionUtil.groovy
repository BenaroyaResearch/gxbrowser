/*
  CollectionUtil.groovy

  Utility functions involving collections
*/

package org.sagres.util;


//*****************************************************************************


class CollectionUtil
{                                                              //CollectionUtil
//-----------------------------------------------------------------------------

    static
    Map< String, Object > flattenMap( Map< String, Object > map,
                                      int depth = -1,
                                      String prefix = "" )
    {
        Map< String, Object > flattened = [:];
        map.each { key, value ->
            String fullKey = (prefix ? (prefix + ".") : "") + key;
            if ( (value instanceof Map< String, Object >) && (depth != 0) )
            {
                Map< String, Object > f =
                        flattenMap( value, (depth - 1), fullKey );
                f.each { k, v ->
                     flattened[ k ] = v;
                }
            }
            else
            {
                flattened[ fullKey ] = value;
            }
        }
        return flattened;
    }

//-----------------------------------------------------------------------------
}                                                              //CollectionUtil


//*****************************************************************************
