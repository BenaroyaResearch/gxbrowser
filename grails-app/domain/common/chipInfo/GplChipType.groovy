package common.chipInfo

class GplChipType {

	String gplId;
	ChipType chipType;

    static constraints = {
		gplId();
		chipType();
    }

    String toString( )
    {
        return gplId;
    }
}
