package com.silicolife.textmining.processes.resources.dictionary.loaders.chemspider;

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import com.silicolife.textmining.core.datastructures.general.ExternalIDImpl;
import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.utils.GenericPairComparable;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;

public class ChemSpiderAPI {

	//	private static String urlGetCompoundInfo = "http://www.chemspider.com/Search.asmx/GetExtendedCompoundInfo";
	private static String urlGetCompoundExtendedInfo = "http://www.chemspider.com/MassSpecAPI.asmx/GetExtendedCompoundInfo";

	private static String urlGetCSIDByInchi  = "http://www.chemspider.com/InChI.asmx/InChIToCSID";
	private static String urlCSID2ExtRefs = "http://www.chemspider.com/Search.asmx/CSID2ExtRefs";

	public static String getCSIDGivenInchi(String accessToken, String inchi) throws ANoteException
	{
		String xml;
		try {
			xml = apiCall(urlGetCSIDByInchi, apiInChIToCSIDData(accessToken,inchi));
			// should return something like:
			// <?xml version="1.0" encoding="utf-8"?><string xmlns="http://www.chemspider.com/">1906</string>
			JSONObject toJSON = XML.toJSONObject(xml);
			// toJSON.string.content should have the ID
			if(toJSON.getJSONObject("string").has("content"))
			{
				Integer id = toJSON.getJSONObject("string").getInt("content");
				return String.valueOf(id);
			}
			else
				return null;
		} catch (IOException e) {
			throw new ANoteException(e);
		}
	}

	/**
	 * Return set of information about compound CSID
	 * 
	 * @param token
	 * @param csid
	 * @return String[0] - InChIKey, String[1] - InChI, String[2] - Smiles,  String[3] - Prefer Name
	 * @throws IOException
	 */
	public static String[] getCompoundInformation(String token,String csid) throws ANoteException
	{
		String[] out = new String[4];
		try {
			String xml = apiCall(urlGetCompoundExtendedInfo, apiCSIDCompoundInfodata(csid, token));
			JSONObject toJSON = XML.toJSONObject(xml);
			if(toJSON.getJSONObject("ExtendedCompoundInfo")!=null)
			{
				JSONObject extendedCompoundInfo = toJSON.getJSONObject("ExtendedCompoundInfo");
				if(extendedCompoundInfo.has("InChIKey"))
					out[0] = extendedCompoundInfo.get("InChIKey").toString();
				if(extendedCompoundInfo.has("InChIKey"))
					out[1] = extendedCompoundInfo.get("InChIKey").toString().toUpperCase();
				if(extendedCompoundInfo.has("SMILES"))
					out[2] = extendedCompoundInfo.get("SMILES").toString();
				if(extendedCompoundInfo.has("CommonName"))
					out[3] = extendedCompoundInfo.get("CommonName").toString();
			}
		} catch (IOException e) {
			throw new ANoteException(e);
		}
		return out;
	}

	public static List<IExternalID> getExternalIdsGivenCSID(String token,String csid) throws ANoteException
	{
		List<IExternalID> out = new ArrayList<>();
		try {
			JSONArray jsonArray = getExternalLinks(token,csid,getExternalInterestSources());
			out.addAll(convertJsonResponseToExternalIdList(jsonArray));
		} catch (IOException e) {
			throw new ANoteException(e);
		}	
		return out;
	}

	public static List<IExternalID> getExternalVendorsIdsGivenCSID(String token,String csid) throws ANoteException
	{
		List<IExternalID> out = new ArrayList<>();
		try {
			JSONArray jsonArray = getExternalLinks(token,csid,getVendors());
			out.addAll(convertJsonResponseToExternalIdList(jsonArray));
		} catch (IOException e) {
			throw new ANoteException(e);
		}	
		return out;
	}

	private static List<IExternalID> convertJsonResponseToExternalIdList(JSONArray jsonArray)
	{
		List<IExternalID> out = new ArrayList<>();
		for(int i=0;i<jsonArray.length();i++)
		{
			JSONObject element = (JSONObject) jsonArray.get(i);
			if(element.has("ds_name")&& element.has("ext_id"))
			{
				String source = element.get("ds_name").toString().toUpperCase();;
				String externalID = element.get("ext_id").toString();
				IExternalID ext = new ExternalIDImpl(externalID, new SourceImpl(source));
				out.add(ext);
			}
		}
		return out;
	}

	private static String apiCall(String endpoint, List<GenericPairComparable<String, String>> data) throws IOException {
		StringBuilder postData = new StringBuilder();
		URL url = new URL(endpoint);
		for (GenericPairComparable<String,String> param : data) {
			if (postData.length() != 0) postData.append('&');
			postData.append(URLEncoder.encode(param.getX(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getY()), "UTF-8"));
		}
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");

		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		conn.setDoOutput(true);
		conn.getOutputStream().write(postDataBytes);
		conn.setConnectTimeout(50000);
		conn.setReadTimeout(50000);
		StringWriter writer = new StringWriter();
		IOUtils.copy(conn.getInputStream(), writer);
		String theString = writer.toString();
		return theString;
	}

	private static List<GenericPairComparable<String, String>> apiInChIToCSIDData(String token,String inchi) {
		List<GenericPairComparable<String, String>> data = new ArrayList<>();
		data.add(new GenericPairComparable<String, String>("inchi", inchi));
		data.add(new GenericPairComparable<String, String>("token", token));
		return data;
	}



	private static JSONArray getExternalLinks(String token,String csid, List<String> externalSources) throws IOException {
		String xml = apiCall(urlCSID2ExtRefs, apiCSID2ExtRefsdata(csid, token,externalSources));
		JSONObject toJSON = XML.toJSONObject(xml);
		if(toJSON.getJSONObject("ArrayOfExtRef")!=null && toJSON.getJSONObject("ArrayOfExtRef").has("ExtRef"))
		{
			Object externalIdslist = toJSON.getJSONObject("ArrayOfExtRef").get("ExtRef");
			JSONArray wrapped = new JSONArray();
			if (externalIdslist instanceof JSONArray) {
				wrapped = (JSONArray)externalIdslist;
			} else if (externalIdslist instanceof JSONObject) {
				wrapped.put(externalIdslist);
			} else {
				throw new IOException(externalIdslist.getClass().getName());
			}
			return wrapped;
		}
		return new JSONArray();
	}

	private static List<GenericPairComparable<String, String>> apiCSIDCompoundInfodata(String csid, String token) {
		List<GenericPairComparable<String, String>> data = new ArrayList<>();
		data.add(new GenericPairComparable<String, String>("CSID", csid.toString()));
		data.add(new GenericPairComparable<String, String>("token", token));
		return data;
	}

	private static List<GenericPairComparable<String, String>> apiCSID2ExtRefsdata(String csid, String token, List<String> externalSources) {
		List<GenericPairComparable<String, String>> data = new ArrayList<>();
		data.add(new GenericPairComparable<String, String>("CSID", csid.toString()));
		data.add(new GenericPairComparable<String, String>("token", token));
		List<String> datasrcs = new ArrayList<>();
		datasrcs.addAll(externalSources);
		for (String datasrc : datasrcs )
			data.add(new GenericPairComparable<String, String>("datasources", datasrc));
		return data;
	}

	private static List<String> getExternalInterestSources()
	{
		List<String> exernalinterestSources = new ArrayList<>();
		exernalinterestSources.add("CHEBI");
		exernalinterestSources.add("DrugBank");
		exernalinterestSources.add("CAS");
		exernalinterestSources.add("BioCyc");
		exernalinterestSources.add("ChEMBL");
		exernalinterestSources.add("MeSH");
		exernalinterestSources.add("PubChem");
		//		datasrcs.add("Royal Society of Chemistry");
		exernalinterestSources.add("Wikipedia");
		exernalinterestSources.add("ZINC");	
		return exernalinterestSources;
	}

	private static List<String> getVendors()
	{
		String[] datasrcs = getVendorList();
		List<String> vendorSources = new ArrayList<>();
		for(String vendor:datasrcs)
		{
			vendorSources.add(vendor);
		}		
		return vendorSources;
	}



	private static String[] getVendorList() {
		// This data comes from act/reachables/src/main/resources/chemspider-vendors
		// See the script vendors_from_inchi.sh and the step{1-5} that extract these
		// sources from ChemSpider. Steps{1-5} result in a file called vendor_names.txt
		// and that file is pasted here...
		String[] datasrcs = new String[] {
				"ASINEX",
				"ChemBridge",
				"Specs",
				"Enamine",
				"AKos",
				"R&D Chemicals",
				"Synthon-Lab",
				"UkrOrgSynthesis",
				"CiVentiChem",
				"SynChem",
				"Ryan Scientific",
				"TOSLab",
				"Bio-Vin",
				"ChemDiv",
				"Otava Chemicals",
				"Aronis",
				"Life Chemicals",
				"Calyx",
				"Activate Scientific",
				"Argus Chemicals",
				"AsisChem",
				"Boron Molecular",
				"ChemPacific",
				"Microsource",
				"Trylead Chemical",
				"Sigma-Aldrich",
				"Afid Therapeutics",
				"Alfa Aesar",
				"Vitas-M",
				"Key Organics",
				"Matrix Scientific",
				"PepTech",
				"Pharmeks",
				"Trans World Chemicals",
				"Astatech",
				"Chess Chemical",
				"JRD Fluorochemicals",
				"Ubichem",
				"AnalytiCon Discovery",
				"MP Biomedicals",
				"Oakwood",
				"Exclusive Chemistry",
				"OmegaChem",
				"HDH Pharma",
				"Rieke Metals",
				"ASDI",
				"Florida Center for Heterocyclic Compounds",
				"Synthonix",
				"Shanghai Sinofluoro Scientific",
				"Hetcat",
				"Borochem",
				"Biosynth",
				"True PharmaChem",
				"Cayman Chemical",
				"Dipharma",
				"ACB Blocks",
				"Chemik",
				"Sequoia Research Products",
				"Apollo Scientific Limited",
				"Spectrum Info",
				"Infarmatik",
				"Rudolf Boehm Institute",
				"Timtec",
				"Tocris Bioscience",
				"Princeton Biomolecular",
				"Hangzhou Sage Chemical Co., Ltd.",
				"Viwit Pharmaceutical",
				"MicroCombiChem",
				"SelectLab Chemicals GmbH",
				"Ramdev Chemicals",
				"Extrasynthese",
				"Gelest",
				"Bridge Organics",
				"Jiangsu WorldChem",
				"Baihua Bio-Pharmaceutical",
				"Szintekon Ltd",
				"Excel Asia",
				"Alinda Chemical",
				"ennopharm",
				"Manchester Organics",
				"Globe Chemie",
				"Shanghai Haoyuan Chemexpress ",
				"Shanghai Elittes organics",
				"Cooper Chemicals",
				"Hangzhou APIChem Technology ",
				"Mizat Chemicals ",
				"Frinton Laboratories",
				"BePharm",
				"HE Chemical",
				"Molport",
				"BioBlocks Inc.",
				"Zerenex Molecular ",
				"Innovapharm",
				"Research Organics",
				"Creasyn Finechem",
				"Alchem Pharmtech",
				"iThemba Pharmaceuticals",
				"Sun BioChem, Inc.",
				"Santa Cruz Biotechnology ",
				"DSL Chemicals",
				"AvaChem Scientific",
				"SynQuest",
				"Evoblocks",
				"CDN Isotopes",
				"Endeavour Speciality Chemicals",
				"Shanghai Race Chemical",
				"Shanghai IS Chemical Technology",
				"DanYang HengAn Chemical Co.,Ltd",
				"ChiroBlock",
				"Platte Valley Scientific",
				"TCI",
				"Finetech Industry",
				"Nagase",
				"Annker Organics",
				"Ark Pharm, Inc.",
				"Aconpharm",
				"Endotherm GmbH",
				"InterBioScreen",
				"Fluorochem ",
				"Accela ChemBio",
				"ChemFuture",
				"Syntide",
				"Paragos",
				"DiverChim",
				"oriBasePharma",
				"Chiralix",
				"AChemo",
				"Selleck Chemicals",
				"Watson International Ltd",
				"Excenen",
				"Shanghai Boyle Chemical Co., Ltd.",
				"Alfa Pyridines",
				"Shanghai Excellent chemical",
				"Chiral Quest",
				"AMRI",
				"Letopharm",
				"Santai Labs",
				"Adesis",
				"AOKChem",
				"Nanjing Pharmaceutical Factory Co., Ltd",
				"DAY Biochem",
				"zealing chem",
				"ABI Chemicals",
				"AOKBIO",
				"Reddy N Reddy Pharmaceuticals",
				"Chengdu D-innovation",
				"Avistron Chemistry",
				"Abacipharm",
				"Centec",
				"Focus Synthesis",
				"Georganics Ltd.",
				"Rare Chem",
				"Annova Chem",
				"Chicago Discovery Solutions",
				"Solaronix",
				"Apeiron Synthesis",
				"Indofine",
				"J and K Scientific",
				"Porse Fine Chemical",
				"Cool Pharm",
				"Livchem",
				"Fragmenta",
				"AEchem Scientific",
				"Mole-Sci.Tech",
				"Irvine Chemistry Laboratory ",
				"Synergy-Scientific",
				"Angene",
				"CoachChem",
				"Abblis Chemicals",
				"Abcam",
				"Jalor-Chem",
				"AK Scientific",
				"Acorn PharmaTech",
				"Zylexa Pharma",
				"Chemren Bio-Engineering",
				"Isosep",
				"Selleck Bio",
				"BOC Chem",
				"Advanced ChemBlocks",
				"Juhua Group",
				"Capot Chemical",
				"LGC Standards",
				"Biochempartner",
				"Adooq Bioscience",
				"Novochemy",
				"Atomole Scientific",
				"Huili Chem",
				"P3 BioSystems",
				"Beijing LYS Chemicals",
				"Hangzhou Chempro",
				"Abmole Bioscience",
				"Watec Laboratories",
				"Apexmol",
				"Conier Chem",
				"Amadis Chemical",
				"Alfa Chemistry",
				"ADVAMACS",
				"Jupiter Sciences",
				"Arking Pharma",
				"Wisdom Pharma",
				"KaironKem",
				"Alchemist-Pharm",
				"Natural Remedies",
				"LeadGen Labs",
				"Acentex Scientific",
				"Anward",
				"Rosewell Industry Co.",
				"Chembo Pharma",
				"Achemica",
				"EDASA Scientific",
				"Sunshine Chemlab",
				"Acesobio",
				"Syncozymes",
				"Chengdu Kaixin",
				"AminoLogics",
				"AldLab Chemicals",
				"ChangChem",
				"ApexBio",
				"BerrChem",
				"Medchem Express",
				"Merck Millipore",
				"ChemScene",
				"Glentham Life Sciences",
				"Viva Corporation",
				"PhyStandard",
				"King Scientific",
				"eNovation Chemicals",
				"Thoreauchem",
				"MolMall",
				"ACINTS",
				"Chemodex",
				"Labseeker",
				"Axon Medchem",
				"BroadPharm",
				"Rosewachem",
				"Renaissance Chemicals",
				"CEG Chemical",
				"GFS Chemicals",
				"OXchem",
				"ACT Chemical",
				"Bide Pharmatech",
				"Arromax",
				"Sinova",
				"Atomax",
				"TOKU-E",
				"Mcule",
				"Active Biopharma",
				"Finornic Chemicals",
				"Apollo Scientific Adopted",
				"LKT Labs",
				"Carbosynth",
				"ChemStep",
				"Wecoochem",
				"Aromalake",
				"W&J PharmaChem, Inc.",
				"Leverton-Clarke",
				"Airedale Chemical",
				"Corvinus Chemicals",
				"Akerr Pharma",
				"Debyesci",
				"Xinyanhe Pharmatech",
				"Megazyme International",
				"Arkema",
				"Advanced Technology & Industrial",
				"Shenzhen Nexconn Pharmatechs Ltd.",
				"Aspira Scientific",
				"Shanghai Pengteng Fine Chemical Co., Ltd. ",
				"Wylton Jinglin",
				"AZEPINE",
				"Attomarker",
				"OlainFarm",
				"TripleBond",
				"Exim",
				"Helix Molecules",
				"Santiago Laboratory Equipment",
				"ChiralStar",
				"Wolves Chemical",
				"Hello Bio",
				"SLI Technologies",
				"A1 BioChem Labs",
				"Tubepharm",
				"A&J Pharmtech",
				"Aoyi International",
				"4C Pharma Scientific",
				"ACO Pharm",
				"Chemcia Scientific",
				"Natural Products Discovery Institute",
				"A2Z Chemical",
				"GuiChem",
				"Acemol",
				"Boerchem",
				"Suntto Chemical",
				"SynInnova",
				"Founder Pharma",
				"Chemspace",
				"OXchem"
		};
		return datasrcs;
	}

}
