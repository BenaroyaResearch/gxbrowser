package org.sagres.charts

/**
 * Created by IntelliJ IDEA.
 * User: bzeitner
 * Date: 4/27/12
 * Time: 1:29 PM
 * 
 */
class ChordData {
	def columns = []
	def data=[]

	def print() {

		this.columns.each { ChordColumn cc ->
			print " ${cc.name}\t"
		}
		println ""
		this.data.each { String[] values ->
			values.each {
				print "${it},"
			}
			println ""
		}
	}

}




