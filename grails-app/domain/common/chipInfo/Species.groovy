package common.chipInfo

class Species {

    String latin;
    String english;
    int taxId; //NCBI taxonomy ID

    static constraints = {
        latin( nullable: false );
        english( nullable: true );
        taxId( nullable: true );
    }

  String toString() {
    return "${english} (${latin})"
  }
}
