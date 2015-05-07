#!/bin/sh

if [ $# -gt 0 ]; then
	HOST=$1
else
	HOST="localhost"
fi

# ExpAnnot.groovy -h $HOST -c "Illumina Hu6 V2" -a ../../data/Annotations/HumanWG-6_V2_0_R4_11223189_A -t illumina_human6_v2 -v -r

# ExpAnnot.groovy -h $HOST -c "Hu6 V3" -a ../../data/Annotations/HumanWG-6_V3_0_R3_11282955_A -t illumina_human6_v3 -v -r

# ExpAnnot.groovy -h $HOST -c "Hu8 V2" -a ../../data/Annotations/HumanRef-8_V2_0_R4_11223162_A -t illumina_human8_v2 -v -r

# ExpAnnot.groovy -h $HOST -c "Hu V3 Ref 8" -a ../../data/Annotations/HumanRef-8_V3_0_R3_11282963_A -t illumina_human8_v3 -v -r

# ExpAnnot.groovy -h $HOST -h $HOST -c "HT12 V3" -a ../../data/Annotations/HumanHT-12_V3_0_R3_11283641_A -t illumina_human12_v3 -v -r

# ExpAnnot.groovy -h $HOST -c "HT12 V4" -a ../../data/Annotations/HumanHT-12_V4_0_R2_15002873_B -t illumina_human12_v4 -v -r

# ExpAnnot.groovy -h $HOST -c "Mouse6 V1" -a ../../data/Annotations/MouseWG-6_V1_1_R4_11234304_A -t illumina_mouse6_v1 -v -r

# ExpAnnot.groovy -h $HOST -c "Mouse6 V2" -a ../../data/Annotations/MouseWG-6_V2_0_R2_11278593_A -t illumina_mouse6_v2 -v -r

# ExpAnnot.groovy -h $HOST -c "Mouse8 V1" -a ../../data/Annotations/MouseRef-8_V1_1_R4_11234312_A -t illumina_mouse8_v1 -v -r

# ExpAnnot.groovy -h $HOST -c "Mouse8 V2" -a ../../data/Annotations/MouseRef-8_V2_0_R2_11278551_A -t illumina_mouse8_v2 -v -r

# ExpAnnot.groovy -h $HOST -c "HG-U133A" -a ../../data/Annotations/HG-U133A.na32.annot.csv -t affymetrix_hg_u133a -v -r

# ExpAnnot.groovy -h $HOST -c "HG-U133B" -a ../../data/Annotations/HG-U133B.na32.annot.csv -t affymetrix_hg_u133b -v -r

# ExpAnnot.groovy -h $HOST -c "HG-U133_Plus_2" -a ../../data/Annotations/HG-U133_Plus_2.na32.annot.csv -t affymetrix_hg_u133_plus_2 -v -r

# ExpAnnot.groovy -h $HOST -c "Phx_Human4" -a ../../data/Annotations/phx-hs-2.1.txt -t phalanx_human_4 -f "probe_id" -v -r

# ExpAnnot.groovy -h $HOST -c "Phx_Human5" -a ../../data/Annotations/phx-HOA5-r1-20100623.txt -t phalanx_human_5 -v -r

# ExpAnnot.groovy -h $HOST -c "Phx_Mouse1" -a ../../data/Annotations/phx-mm-2.1.txt -t phalanx_mouse_1 -f "probe_id" -v -r

# ExpAnnot.groovy -h $HOST -c "Phx_Mouse2" -a ../../data/Annotations/phx-MOA2-r1-20101115.txt -t phalanx_mouse_2 -v -r

