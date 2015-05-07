package common.chipInfo

class ChipData
{
	static constraints = {
		name(blank: false, maxSize: 32)
		speciesId( nullable: true )
		manufacturer(blank: false, maxSize: 64)
		model( nullable: true, maxSize: 250 )
		chipVersion( nullable: true, maxSize: 250 )
	}

	String name
	Integer speciesId
	String manufacturer
	String model
	String chipVersion

	String toString()
	{
		return name
	}

}
