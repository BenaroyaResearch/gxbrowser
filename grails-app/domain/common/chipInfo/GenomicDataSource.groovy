package common.chipInfo

/** identifies where the data being imported came from; which core produced the results */
class GenomicDataSource
{

	static constraints = {
	}

	String name
  String displayName
  String iconName

    String toString( )
    {
        return displayName;
    }
}
