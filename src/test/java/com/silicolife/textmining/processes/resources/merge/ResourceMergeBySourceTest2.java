package com.silicolife.textmining.processes.resources.merge;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.datastructures.resources.ResourceImpl;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.general.source.ISource;
import com.silicolife.textmining.core.interfaces.resource.IResource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.ResourcesTypeEnum;
import com.silicolife.textmining.processes.DatabaseConnectionInit;

public class ResourceMergeBySourceTest2 {

	@Test
	public void test() throws InvalidDatabaseAccess, ANoteException {
		DatabaseConnectionInit.initSisbi("localhost","3306","sisbitmpipeline","root","admin");	
		ResourceMergeBySource resourceMerge = new ResourceMergeBySource();
		String type = ResourcesTypeEnum.dictionary.name();
		IResource<IResourceElement> destiny = new ResourceImpl(3131738670925400260L, "Organisms - Linneaus Species Dictionary", "", type , true);
		IResource<IResourceElement> toMerge = new ResourceImpl(7859449413879087164L, "Organisms - Linneaus Species Proxy Dictionary", "", type , true);;
		ISource source = new SourceImpl(6, "Ncbi Taxonomy");
		resourceMerge.mergeResoures(destiny, toMerge, source);
		assertTrue(true);
	}

}
