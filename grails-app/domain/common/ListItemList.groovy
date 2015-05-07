package common

/**
 * Created by IntelliJ IDEA.
 * User: charliequinn
 * Date: 8/29/11
 * Time: 15:20
 * To change this template use File | Settings | File Templates.
 */
class ListItemList {
	String uiLabel
	String paramName

	ArrayList items = new ArrayList()

	def getItems()
	{
		return items
	}
}
