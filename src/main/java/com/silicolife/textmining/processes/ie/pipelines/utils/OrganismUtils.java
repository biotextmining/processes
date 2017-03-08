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
	
	private static String ncbitaxonomySourceDefault = "Ncbi Taxonomy";
	private static String brendaSourceDefault = "Brenda";
	private static String chebiSourceDefault = "Chebi";

	
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
		throw new OrganismRetrievelException("NCBI Source are not available");
	}
	
	public static IResourceElement getEnzymeResourceElement(String ecNumber) throws ANoteException
	{
		IResourceElementsFilter filter = new ResourceElementsFilterImpl();
		filter.addSource(getBrendaSource());
		String externalId = String.valueOf(ecNumber);	
		IResourceElementSet<IResourceElement> cadidateResults = InitConfiguration.getDataAccess().getResourceElementsFilteredByExactExternalId(filter, externalId);
		if(cadidateResults.size()==0)
		{
			throw new OrganismRetrievelException("No Enzyme found with ecNumber id"+ecNumber);
		}
		if(cadidateResults.size() > 1)
		{
			throw new OrganismRetrievelException("Mutiples Enzymes found for ecNumber id"+ecNumber);
		}
		return cadidateResults.getResourceElementsOrder().get(0);
	}
	
	private static ISource getBrendaSource() throws ANoteException
	{
		List<ISource> listSources = InitConfiguration.getDataAccess().getAllSources();
		for(ISource source:listSources)
		{
			if(source.getSource().equals(brendaSourceDefault))
			{
				return source;
			}
		}
		throw new OrganismRetrievelException("NCBI Source are not available");
	}
	
	public static IResourceElement getCompoundResourceElement(int chebiID) throws ANoteException
	{
		IResourceElementsFilter filter = new ResourceElementsFilterImpl();
		filter.addSource(getChebiSource());
		String externalId = String.valueOf(chebiID);	
		IResourceElementSet<IResourceElement> cadidateResults = InitConfiguration.getDataAccess().getResourceElementsFilteredByExactExternalId(filter, externalId);
		if(cadidateResults.size()==0)
		{
			throw new OrganismRetrievelException("No compound found for Chebi id"+chebiID);
		}
		if(cadidateResults.size() > 1)
		{
			throw new OrganismRetrievelException("Mutiples compounds found for Chebi id"+chebiID);
		}
		return cadidateResults.getResourceElementsOrder().get(0);
	}
	
	private static ISource getChebiSource() throws ANoteException
	{
		List<ISource> listSources = InitConfiguration.getDataAccess().getAllSources();
		for(ISource source:listSources)
		{
			if(source.getSource().equals(chebiSourceDefault))
			{
				return source;
			}
		}
		throw new OrganismRetrievelException("ChEBI Source are not available");
	}

}
