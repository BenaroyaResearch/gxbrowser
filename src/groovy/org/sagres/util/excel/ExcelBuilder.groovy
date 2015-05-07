package org.sagres.util.excel

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory

/**
 * Groovy Builder that extracts data from
 * Microsoft Excel spreadsheets.
 * @author Goran Ehrsson (http://www.technipelago.se/content/technipelago/blog/44)
 *
 * Examples:
 *  new ExcelBuilder("customers.xls").eachLine([labels:true]) {
 *    new Person(name:"$firstname $lastname",
 *      address:address, telephone:phone).save()
 *  }
 *
 *  new ExcelBuilder("customers.xls").eachLine {
 *    println "First column on row ${it.rowNum} = ${cell(0)}"
 *  }
 *
 *  NOTE: Changed to allow for access of xls and xlsx files
 *
 */
class ExcelBuilder {

    def workbook
    def labels
    def row

  ExcelBuilder(String fileName) {
      this(new File(fileName))
    }

  ExcelBuilder(File file) {
        Row.metaClass.getAt = {int idx ->
            def cell = delegate.getCell(idx)
            if(! cell) {
                return null
            }
            def value
            switch(cell.cellType) {
                case Cell.CELL_TYPE_NUMERIC:
                if(DateUtil.isCellDateFormatted(cell)) {
                    value = cell.dateCellValue
                } else {
                    value = cell.numericCellValue
                }
                break
                case Cell.CELL_TYPE_BOOLEAN:
                value = cell.booleanCellValue
                break
                default:
                value = cell.stringCellValue
                break
            }
            return value
        }

        file.withInputStream{is->
            workbook = WorkbookFactory.create(is)
        }
    }

    def getSheet(idx) {
        def sheet
        if(! idx) idx = 0
        if(idx instanceof Number) {
            sheet = workbook.getSheetAt(idx)
        } else if(idx ==~ /^\d+$/) {
            sheet = workbook.getSheetAt(Integer.valueOf(idx))
        } else {
            sheet = workbook.getSheet(idx)
        }
        return sheet
    }

    def cell(idx) {
        if(labels && (idx instanceof String)) {
            idx = labels.indexOf(idx.toLowerCase())
        }
        return row[idx]
    }

    def propertyMissing(String name) {
        cell(name)
    }

    def eachLine(Map params = [:], Closure closure) {
        def offset = params.offset ?: 0
        def max = params.max ?: 9999999
        def sheet = getSheet(params.sheet)
        def rowIterator = sheet.rowIterator()
        def linesRead = 0

        if(params.labels) {
            labels = rowIterator.next().collect{it.toString().toLowerCase()}
        }
        offset.times{ rowIterator.next() }

        closure.setDelegate(this)

        while(rowIterator.hasNext() && linesRead++ < max) {
            row = rowIterator.next()
            closure.call(row)
        }
    }
}