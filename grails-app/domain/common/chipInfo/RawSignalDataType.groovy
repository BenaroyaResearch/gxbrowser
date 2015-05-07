package common.chipInfo

class RawSignalDataType {

  static constraints = {
    name(nullable: false)
    displayName(nullable: false)
  }

  String name
  String displayName

    String toString( )
    {
        return display_name;
    }
}
