package com.silicolife.textmining.processes.ie.re.kineticre.core;

import java.util.Comparator;

import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;

public class EntityAnnotationListSort implements Comparator<IEntityAnnotation> {
	public int compare(IEntityAnnotation entitie2, IEntityAnnotation entitie1) {
        return  ((Long)entitie2.getStartOffset()).compareTo((Long)entitie1.getStartOffset());
    }

}