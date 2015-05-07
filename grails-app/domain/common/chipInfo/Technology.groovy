package common.chipInfo

class Technology {

    String name;

    static constraints = {
        name( nullable: false )
    }

	String toString()
	{
		return name
	}

}
