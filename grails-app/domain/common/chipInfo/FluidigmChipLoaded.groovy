package common.chipInfo

class FluidigmChipLoaded {

    ChipsLoaded chipsLoaded;
    String chipRunInfo;
    String applicationVersion;
    String applicationBuild;
    String exportType;
    Float qualityThreshold;
    String baselineCorrectionMethod;
    String ctThresholdMethod;

    static constraints = {
        chipsLoaded( );
        chipRunInfo( nullable: true );
        applicationVersion( nullable: true );
        applicationBuild( nullable: true );
        exportType( nullable: true );
        qualityThreshold( nullable: true );
        baselineCorrectionMethod( nullable: true );
        ctThresholdMethod( nullable: true );
    }
}
