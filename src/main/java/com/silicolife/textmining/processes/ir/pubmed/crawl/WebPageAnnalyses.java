package com.silicolife.textmining.processes.ir.pubmed.crawl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

//clase usada para obtener las url's a descargar
//entre los posibles enlaces HTML de una p�gina web dada
public class WebPageAnnalyses
{
  private static Vector<URL> urls_a_mirar=new Vector<URL>(); //url's por descargar
  private static Vector<URL> urls_con_pdf=new Vector<URL>(); //posibles url's a descargar con la palabra pdf
  private static Vector<URL> urls_sin_pdf=new Vector<URL>(); //posibles url's a descargar sin la palabra pdf
  private static String [] tipos_enlaces={"href=","src="}; //tipos de enlaces html que se atraviesan
  private static int estado=1; //estado=1 -> no hay enlaces con "pdf"
  							   //estado=2 -> hay enlaces con "pdf" en su url
  							   //estado=3 -> hay enlaces con "pdf" en su nombre
  private static String url_base; //url de la pagina web de partida
  private static final String elsevier="elsevier.com"; //pagina que hay que tratar de forma especial
  
  //devuelve las url's por descargar
  public static Vector<URL> getUrls_a_mirar()
  {
	return urls_a_mirar;
  }
  
  //dada la url de una p�gina web (para hallar las rutas absolutas) y su cedigo fuente,
  //aade a urls_a_mirar las url's de sus enlaces HTML con mayor probabilidad de llevar al pdf
  public static void addEnlaces(String urlBase,String cad)
  {
	if(Comun.indexOfIgnoreCase(cad,"<title>Page Not Found</title>")!=-1) return;
	url_base=urlBase;
	StringBuilder strb;
  	int pdfs_anteriores=urls_a_mirar.size();
  	estado=1;
  	
  	//elimina secciones innecesarias t�picas de HTML (que suelen contener enlaces no �tiles)
  	cad=Comun.eliminarSeccion(cad,"<!doctype","<");
  	cad=Comun.eliminarSeccion(cad,"<img",">"); //c�digo que se salta las im�genes
  	cad=Comun.eliminarSeccion(cad,"<style","<");
  	cad=Comun.eliminarSeccion(cad,"<link","<");
  	cad=Comun.eliminarSeccion(cad,"<script","<");
  	cad=Comun.eliminarSeccion(cad,"<option","<");
  	cad=Comun.eliminarSeccion(cad,"<area","<");
  	
  	//eliminar secciones con art�culos relacionados o otros pdf's
  	//ejemplo: http://linkinghub.elsevier.com/retrieve/pii/S0398-7620(08)00810-9
  	cad=Comun.eliminarSeccion(cad,"Related Articles in ScienceDirect","</tbody>");
  	//ejemplo: http://dx.doi.org/10.1021/bc800258v
  	cad=Comun.eliminarSeccion(cad,"<ul id=\"nav\">","<ul ");
  	cad=Comun.eliminarSeccion(cad,"<ul id=\"relatedArticles\">","<ul ");
  	//ejemplo: http://dx.doi.org/10.1080/10408360600552587
  	cad=Comun.eliminarSeccion(cad,"<tr id=\"main_copyrightbar\"","</tr>");
  	
  	//elimina cadenas de HTML innecesarias
  	//(que pueden provocar un mal reconocimiento de los nombres de los enlaces)
  	cad=Comun.eliminarSubcadena(cad,"</img");
  	cad=Comun.eliminarSubcadena(cad,"<strong>");
  	cad=Comun.eliminarSubcadena(cad,"</strong>");
  	cad=Comun.eliminarSubcadena(cad,"<b>");
  	cad=Comun.eliminarSubcadena(cad,"</b>");
  	cad=Comun.eliminarSubcadena(cad,"<br>");
  	
  	for(int i=0;i<tipos_enlaces.length;i++)
	{
	  int pos=-1;
	  String tipo_enlace=tipos_enlaces[i];
	  int len=tipo_enlace.length();
	  while(true)
	  {
		//busca las distintas etiquetas que van antes de una url en HTML
		//(con el fin de obtener todos los enlaces de la p�gina web)
		pos=Comun.indexOfIgnoreCase(cad,tipo_enlace,pos+1);
		if(pos!=-1)
		{
		  //se obtiene la url del enlace
		  char delim=0;
		  int pos2=pos+len;
		  if(pos2<cad.length())
			delim=cad.charAt(pos2);
		  if(delim!='"' && delim!='\'') continue;
		  int pos3=cad.indexOf(delim, pos2+1);
		  if(pos3==-1) break;
		  String url=cad.substring(pos2+1,pos3);
		  
		  //se eliminan las urls con extensiones que no interesan
	  	  if(extensionErronea(url)) continue;
	  	  
	  	  //se obtiene el nombre del enlace
		  int pos_final=Comun.posicionMinima(cad.indexOf("</",pos3),cad.indexOf("/>",pos3));
		  pos_final=Comun.posicionMinima(pos_final,cad.indexOf(" href=",pos3));
		  pos_final=Comun.posicionMinima(pos_final,cad.indexOf(" src=",pos3));
		  String nombre=null;
		  if(pos_final!=-1) nombre=cad.substring(pos3,pos_final);
		  else pos_final=pos3;
		  
		  //se elimina el c�digo HTML del enlace del resto de codigo fuente de la pagina web
		  //(para poder buscar despu�s el resto de url's que no estan en enlaces HTML)
		  strb=new StringBuilder();
	  	  strb.append(cad.substring(0,pos));
	  	  strb.append(cad.substring(pos_final,cad.length()));
	  	  cad=strb.toString();
		  
		  //se busca una url dentro de la url (hay url's con urls dentro de sus par�metros)
		  pos2=url.indexOf("http://",1);
		  if(pos2==-1) pos2=url.indexOf("https://",1);
		  if(pos2!=-1)
		  {
			//si se encontr� una url interna, se a�ade dicha url a la lista correspondiente
			//seg�n el nombre del enlace y dicha url -> MIRAR metodo addEnlace
			pos3=getFinURL(url.substring(pos2,url.length()));
			addEnlace(nombre,url.substring(pos2,pos2+pos3));
		  }
		  
		  //se aade la url del enlace encontrado a la lista correspondiente
		  //segan su nombre y su url -> MIRAR m�todo addEnlace
		  addEnlace(nombre,url);
		}
		else break;
	  }
	}
  	//si no se han encontrado enlaces con la palabra "pdf" en el nombre
  	if(estado<3)
  	{
  	  //se a�aden las url's del texto que no aparezcan en enlaces HTML (sin nombre)
  	  //a las listas correspondientes seg�n sus urls -> MIRAR m�todo addEnlace
  	  while(true)
  	  {
  	    int pos=Comun.posicionMinima(cad.indexOf("http://"),cad.indexOf("https://"));
  	    if(pos!=-1)
  	    {
  	      cad=cad.substring(pos,cad.length());
  	      int i=getFinURL(cad);
  	      int fin_seccion=Comun.posicionMinima(cad.indexOf("</"),cad.indexOf(">"));
  	      i=Comun.posicionMinima(i,fin_seccion);
  	      String url=cad.substring(0,i);
  	      cad=cad.substring(i,cad.length());
  	      if(extensionErronea(url)) continue;
	      addEnlace(null,url);
  	    }
  	    else break;
  	  }
  	  //si no hay enlaces con la palabra "pdf" en el nombre,
  	  //se a�aden a urls_a_mirar los enlaces que tengan "pdf" en la url
  	  if(estado==2)
  	  {
  	    for(int i=0;i<urls_con_pdf.size();i++)
  	    {
  		  urls_a_mirar.add(urls_con_pdf.elementAt(i));
  	    }
  	  }
  	}
  	int pdfs_fichero=urls_a_mirar.size()-pdfs_anteriores;
    //si de la pagina web se pasaron mas de 3 enlaces a urls_a_mirar
  	//(enlaces que pueden llevar al pdf),se dejan en urls_a_mirar selo los 3 primeros
	if(pdfs_fichero>3)
	{
	  //System.err.println("La url "+url_base+" tiene demasiados posibles PDF's (se probar� selo con los tres primeros)");
	  for(int i=0;i<pdfs_fichero-3;i++) urls_a_mirar.remove(urls_a_mirar.size()-1);
	}
	
    //si de la p�gina no se pasaron enlaces a urls_a_mirar (enlaces que pueden llevar al pdf),
  	//y la p�gina tiene 3 o menos enlaces (enlaces que quedaron en urls_sin_pdf),
	//se pasan los enlaces de urls_sin_pdf a urls_a_mirar
	else if(pdfs_fichero==0 && (urls_sin_pdf.size()<=3||url_base.indexOf(elsevier)!=-1))
	{
	  for(int i=0;i<urls_sin_pdf.size();i++)
	  {
		urls_a_mirar.add(urls_sin_pdf.elementAt(i));
	  }
	}
	//se vac�an las listas urls_con_pdf y urls_sin_pdf
	//porque s�lo eran auxiliares para calcular las nuevas urls de urls_a_mirar
	urls_con_pdf.clear();
	urls_sin_pdf.clear();
  }
  
  //dado el nombre y la url de un enlace, a�ade la url a la lista adecuada:
  //- Si el nombre contiene la palabra "pdf" se a�ade a urls_a_mirar
  //- Sino si la url contiene la palabra "pdf" se a�ade a urls_con_pdf
  //- En el resto de casos se a�ade a urls_sin_pdf
  private static void addEnlace(String nombre, String url)
  { 
	//si el nombre contiene "pdf" (o otras palabras de casos especiales)
	//se a�ade la url a urls_a_mirar y se establece estado=3
	if(nombre!=null)
	{
	  if(Comun.indexOfIgnoreCase(nombre,"pdf")!=-1 || 
		//casos especiales y webs en las que suceden
		nombre.indexOf("Article via")!=-1 ||         //Elsevier
		nombre.indexOf("View article")!=-1 ||        //EBSCO
		nombre.indexOf("Free Preview")!=-1 ||        //SpringerLink
		nombre.indexOf("Article gratuit")!=-1 ||     //John Libbey Eurotext
		nombre.indexOf("click here to start the article download")!=-1) //e-MED
	  {
		url=obtenerRutaAbsolutaLarga(url_base,url);
		try {
			Comun.addUrlEn(url,urls_a_mirar);
			estado=3;
			return;
		} catch (MalformedURLException e) {}
	  }
	  //si se detecta que el enlace es una trampa (para restringir el accedo a personas)
	  //no se debe guardar en ninguna lista
	  if(nombre.indexOf("Spider trap")!=-1) return;
	}
	//si alg�n enlace cumpli� la condici�n anterior referente a su nombre,
	//s�lo se buscar�n enlaces que cumplan dicha condici�n
	if(estado==3) return;
	
	//si la url contiene "pdf" se a�ade a urls_con_pdf y se establece estado=2
	if(Comun.indexOfIgnoreCase(url,"pdf")!=-1)
	{
	  url=obtenerRutaAbsolutaLarga(url_base,url);
	  try {
		Comun.addUrlEn(url,urls_con_pdf);
	} catch (MalformedURLException e) {}
	  estado=2;
	  return;
	}
	//si alg�n enlace cumpli� la condici�n anterior referente a la url, s�lo se buscar�n
	//enlaces que cumplan las condiciones anteriores referentes al nombre y a la url
	if(estado==2) return;
	
	//si no se cumpli� ninguna de las condiciones anteriores se a�ade la url a urls_sin_pdf
	url=obtenerRutaAbsolutaLarga(url_base,url);
	if(url!=null && url.indexOf(elsevier)!=-1) return;
	try {
		Comun.addUrlEn(url,urls_sin_pdf);
	} catch (MalformedURLException e) {}
  }
  
  //indica si la url dada tiene una extensi�n que indica que no puede llevar al pdf
  private static boolean extensionErronea(String url)
  {
	return (url.endsWith(".jpg") || url.endsWith(".gif") || url.endsWith(".png") || 
			url.endsWith(".bmp") || url.endsWith(".css") || url.endsWith(".js") || 
			url.endsWith(".xml") || url.endsWith(".ico") || url.endsWith(".dtl") || 
			url.endsWith(".rss") || url.endsWith(".dtd"));
  }
  
  //suponiendo que la cadena indicada empieza con una url
  //devuelve la longitud de dicha url (que no tiene porqu� ser la longitud de la cadena)
  private static int getFinURL(String url)
  {
  	char [] arr=new char[]{'"','\'',' ','\t','\n','\r','\f'};
  	int i=0;
  	boolean finURL=false;
  	for(i=0;i<url.length();i++)
  	{
  	  char c=url.charAt(i);
  	  for(int j=0;j<arr.length;j++)
  	  {
  	    if(c==arr[j])
  	    {
  	      finURL=true;
  	      break;
  	    }
  	  }
  	  if(finURL) break;
  	}
  	if(i>url.length()) i=url.length();
  	return i;
  }

  //obtiene la url base de la url indicada, es decir, el directorio superior a todos
  private static String getUrlPpal(String url)
  {
  	int i=0;
	if(url.length()>=8 && url.substring(0,8).equals("https://")) i=8;
	else if(url.length()>=7 && url.substring(0,7).equals("http://")) i=7;
	for(int j=i;j<url.length();j++)
	{
	  if(url.charAt(j)=='/')
	  {
	  	return url.substring(0,j);
	  }
	}
	return url;
  }
  
  //obtiene la url padre de la url indicada, es decir, su directorio inmediatamente superior
  private static String getUrlPadre(String url)
  {
  	int i=0;
	if(url.length()>=8 && url.substring(0,8).equals("https://")) i=8;
	else if(url.length()>=7 && url.substring(0,7).equals("http://")) i=7;
  	for(int j=url.length()-1;j>=i;j--)
	{
	  if(url.charAt(j)=='/')
	  {
		return url.substring(0,j);
	  }
	}
	return null;
  }
  
  //dada la url en la que se encuentra un enlace y la url del enlace
  //(que puede ser relativa a la anterior) genera la url absoluta del enlace
  private static String obtenerRutaAbsoluta(String url_raiz,String url)
  {
	url=url.trim(); //elimina los espacios y otros caracteres de control ASCII
					//del inicio y fin de la url
  	url=Comun.sustituir(url,"&amp;","&");
  	if(url.startsWith("https://") || url.startsWith("http://")) return url;
  	if(url.startsWith("www.")) return "http://"+url;
  	if(url.startsWith("../"))
  	{
  	  String padre=getUrlPadre(url_raiz);
  	  if(padre==null) return null;
  	  else return obtenerRutaAbsoluta(padre,url.substring(3,url.length()));
  	}
  	else if(url.startsWith("./"))
  	{
  	  return url_raiz+"/"+url.substring(2,url.length());
  	}
  	else
  	{
  	  if(url.length()>=1)
  	  {
  	  	//si la url es una ancla sobre la misma p�gina se devuelve null
  		if(url.substring(0,1).equals("#")) return null;
  		else if(url.charAt(0)=='/')
  		{
  		  String ppal=getUrlPpal(url_raiz);
	  	  return ppal+url;
	  	}
	  }
  	}
  	String subdir=getUrlPpal(url);
  	int pos=url_raiz.indexOf("/"+subdir+"/");
  	if(pos!=-1)
  	{
  	  return url_raiz.substring(0, pos)+"/"+url;
  	}
  	else
  	{
  	  String padre=getUrlPadre(url_raiz);
  	  if(padre==null) return null;
  	  else return padre+"/"+url;
  	}
  }
  
  //dada la url en la que se encuentra un enlace y la url del enlace
  //(que puede ser relativa a la anterior) genera la url absoluta del enlace,
  //eliminando url's resultantes que sean principales o antecesoras de la url ra�z
  private static String obtenerRutaAbsolutaLarga(String url_raiz,String url)
  {
	String url_absol=obtenerRutaAbsoluta(url_raiz,url);
	if(url_absol==null) return null;
	String ppal=getUrlPpal(url_absol);
	if(ppal.length()==url_absol.length()) return null;
	if(!url_raiz.endsWith("/")) url_raiz+="/";
	int tam_absol=url_absol.length();
	if(tam_absol<=url_raiz.length() && url_absol.equals(url_raiz.substring(0, tam_absol))) return null;
	return url_absol;
  }
}
