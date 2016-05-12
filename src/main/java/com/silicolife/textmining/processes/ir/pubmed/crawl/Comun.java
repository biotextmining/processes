package com.silicolife.textmining.processes.ir.pubmed.crawl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

//clase con metodos estaticos comunes que podran ser reutilizables por distintas clases
public final class Comun
{
  //busca la primera ocurrencia de la segunda cadena dentro de la primera
  //ignorando mayusculas y minusculas
  public static int indexOfIgnoreCase(String cad,String busq)
  {
	return indexOfIgnoreCase(cad,busq,0);
  }
  
  //busca la primera ocurrencia de la segunda cadena dentro de la primera
  //a partir de la posicion indicada, ignorando mayusculas y minusculas
  public static int indexOfIgnoreCase(String cad,String busq,int fromIndex)
  {
	return cad.toLowerCase().indexOf(busq.toLowerCase(), fromIndex);
  }
	  
  //sustituye todas las ocurrencias de la segunda cadena dentro de la primera
  //por la tercera cadena
  public static String sustituir(String cad,String busq,String sust)
  {
	int pos=0;
	int len=busq.length();
	while(true)
	{
	  pos=cad.indexOf(busq);
	  if(pos!=-1)
	  {
	  	if((pos+len)<cad.length())
	  	  cad=cad.substring(0,pos)+sust+cad.substring(pos+len,cad.length());
	  	else cad=cad.substring(0,pos)+sust;
	  }
	  else break;
	}
	return cad;
  }
  
  //elimina todas las ocurrencias de la segunda cadena dentro de la primera
  public static String eliminarSubcadena(String cad, String subcad)
  {
	StringBuilder strb;
	int pos=0;
	int len=subcad.length();
	while(true)
	{
	  pos=indexOfIgnoreCase(cad,subcad,pos);
	  if(pos!=-1)
	  {
		strb = new StringBuilder();
		strb.append(cad.substring(0,pos));
		strb.append(cad.substring(pos+len,cad.length()));
		cad=strb.toString();
	  }
	  else break;
	}
	return cad;
  }
  
  //elimina todas las ocurrencias de las subcadenas que comiencen por la segunda cadena
  //y terminen por la tercera cadena que se encuentren dentro de la primera cadena
  public static String eliminarSeccion(String cad, String desde,String hasta)
  {
    int ini=0;
	int fin=0;
	int len=desde.length();
	StringBuilder strb;
	while(true)
	{
	  ini=indexOfIgnoreCase(cad,desde,ini);
	  if(ini!=-1)
	  {
	  	fin=cad.indexOf(hasta,ini+len);
	  	if(fin!=-1)
	  	{
	  	  strb=new StringBuilder();
	  	  strb.append(cad.substring(0,ini));
	  	  strb.append(cad.substring(fin,cad.length()));
	  	  cad=strb.toString();
	  	}
	  	else break;
	  }
	  else break;
	}
	return cad;
  }
  //devuelve la menor de las dos posiciones
  //(no devolviendo -1 salvo que las dos valgan -1)
  public static int posicionMinima(int pos1, int pos2)
  {
	if((pos2!=-1 && pos2<pos1) || pos1==-1)
	{
	  pos1=pos2;
	}
	return pos1;
  }
  
  //crea la carpeta indicada
  public static void crear_carpeta(File dir)
  {
    while(!dir.exists())
	{
	  dir.mkdirs();
	}
  }
  
  //borra el fichero o carpeta indicado
  	public static void borrar_fichero(File file) {
		try {
			file.delete();
		} catch (Exception e) {
		}
  	}
  	
  	public static void borrarFicheroRecursivo(File file) {
  		if (file.isDirectory()) {
  			for (File subfile:file.listFiles()) {
  				Comun.borrarFicheroRecursivo(subfile);
  			}
  		}
  		file.delete();
  	}
  
  //devuelve el contenido del fichero indicado
  public static String leer_fichero(String fich) throws Exception
  {
	BufferedReader bf=new BufferedReader(new FileReader(fich));
	StringBuilder strb = new StringBuilder();
	String cad_aux=bf.readLine();
	while(cad_aux!=null)
	{
	  strb.append(cad_aux);
	  strb.append('\n');
	  cad_aux=bf.readLine();
	}
	bf.close();
	int len=strb.length();
	if(len>0) return strb.substring(0,len-1);
	return strb.toString();
  }
  
  //indica si la url dada esta contenida en la lista indicada
  public static boolean urlEn(URL url, Vector<URL> urls)
  {
	if(url!=null)
	{
	  for(int i=0;i<urls.size();i++)
	  {
//		try
//		{
		  if(urls.elementAt(i) != null && url.sameFile(urls.elementAt(i)))
		  {
			return true;
		  }
//		}catch(NullPointerException e){}
	  }
	}
	return false;
  }
  
  public static void addUrlEn(String url, Vector<URL> urls) throws MalformedURLException
  {
	if(url!=null)
	{
//	  try
//	  {
		URL uri=new URL(url);
		if(!Comun.urlEn(uri,urls)) urls.add(uri);
//	  }catch(MalformedURLException e)
//	  {
//		"Url mal formada: "+url;
//	  }
	}
  }
  
  //comprueba si una pagina es HTML dado su c�digo fuente
  public static boolean esHTML(String cad)
  {
	return Comun.indexOfIgnoreCase(cad,"<html")!=-1; //los html's contienen "<html"
													 //pueden no tener <html> ni </html>
  }
  
  //comprueba si una pagina es un PDF dado su c�digo fuente
  public static boolean esPDF(String cad)
  {
	if(cad==null || cad.length()<4) return false;
	String ini=cad.substring(0, 4);
	return ini.equalsIgnoreCase("%PDF"); //los pdf's empiezan por "%PDF"
  }
  
  //convierte los caracteres raros de la cadena a UTF para que pueda ser una URL v�lida
  public static String getUTF8String(String cad)
  {
	try
	{
	  String cad2="";
	  String aux;
	  for(int i=0;i<cad.length();i++)
	  {
	    char c=cad.charAt(i);
	    //entre ! y z se encuentran los caracteres mas habituales de UTF-8 y no deberan
	    //dar error al introducirlos tal cual est�n en una URL
	    if(c>='!' && c<='z' && c!='[' && c!=']') cad2+=c;
	    else
	    {
	      byte[] arr=new Character(c).toString().getBytes("UTF-8");
	      for(int j=0;j<arr.length;j++)
	      {
	    	aux=Integer.toHexString(arr[j]|0);
	    	if(aux.length()<2) aux="0"+aux;
	    	else aux=aux.substring(aux.length()-2,aux.length());
	    	cad2+="%"+aux.toUpperCase();
	      }
	    }
	  }
	  return cad2;
	}
	catch(Exception e)
	{
	  e.printStackTrace(System.err);
	  return null;
	}
  }
}