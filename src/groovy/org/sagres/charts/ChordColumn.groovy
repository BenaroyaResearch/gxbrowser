package org.sagres.charts

/**
 * Created by IntelliJ IDEA.
 * User: bzeitner
 * Date: 4/27/12
 * Time: 2:33 PM
 * 
 */
class ChordColumn implements Serializable {
	def name
	boolean  isNumber = false
	boolean attemptedNumber = false
	boolean isArc = false
	boolean isLink =false
	def values = []
	def maxNumberValues = 100
	double entries = 0

	def filter = false


	//false = exclude/lower than
	//true = only/greater than

	boolean filterType   = false
	def filterValue

	def addValue(String value) {
		if (value != null && value.size() > 0) {
			entries++
		}
		if ((value != null && !value.toString().equalsIgnoreCase("null") && value.size() >0) &&  !isArc && !isLink && (!attemptedNumber  || isNumber )) {
			try {
				attemptedNumber = true
				double number = Double.parseDouble( value)
				isNumber = true
			} catch (NumberFormatException nfe) {

			} catch (Exception ex) {
				println "unkown exception attempting parsing dbl: ${ex.toString()}"
			}

		}

		if (values.size() < maxNumberValues && !isLink && !isNumber && values != null && value.size() > 0   && !values.contains(value.toString())) {
				values.add(value.toString())
	  }

	}
}
