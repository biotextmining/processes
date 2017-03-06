package com.silicolife.textmining.processes.ie.pipelines.utils;

import java.util.List;

import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.resources.ResourceElementsFilterImpl;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.general.source.ISource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.IResourceElementSet;
import com.silicolife.textmining.core.interfaces.resource.IResourceElementsFilter;
import com.silicolife.textmining.processes.ie.pipelines.utils.exception.OrganismRetrievelException;

public class OrganismUtils {
	
	public static String ncbitaxonomySourceDefault = "Ncbi Taxonomy";

	
	public static IResourceElement getOrganismResourceElement(int ncbiTaxonomy) throws ANoteException
	{
		IResourceElementsFilter filter = new ResourceElementsFilterImpl();
		filter.addSource(getNCBItaxonomySource());
		String externalId = String.valueOf(ncbiTaxonomy);	
		IResourceElementSet<IResourceElement> cadidateResults = InitConfiguration.getDataAccess().getResourceElementsFilteredByExactExternalId(filter, externalId);
		if(cadidateResults.size()==0)
		{
			throw new OrganismRetrievelException("No organism found with taxon id"+ncbiTaxonomy);
		}
		if(cadidateResults.size() > 1)
		{
			throw new OrganismRetrievelException("Mutiples organisms found with taxon id"+ncbiTaxonomy);
		}
		return cadidateResults.getResourceElementsOrder().get(0);
	}
	
	private static ISource getNCBItaxonomySource() throws ANoteException
	{
		List<ISource> listSources = InitConfiguration.getDataAccess().getAllSources();
		for(ISource source:listSources)
		{
			if(source.getSource().equals(ncbitaxonomySourceDefault))
			{
				return source;
			}
		}
		throw new OrganismRetrievelException("Brenda Source not available");
	}

}
