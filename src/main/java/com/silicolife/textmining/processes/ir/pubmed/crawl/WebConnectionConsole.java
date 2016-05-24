package com.silicolife.textmining.processes.ir.pubmed.crawl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy.Type;
import java.net.URL;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.silicolife.textmining.core.datastructures.documents.PDFtoText;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;

//clase usada para la bsqueda y descarga del pdf de un art�culo dados sus datos
//(pmid, enlaces de partida en la bsqueda y un trozo de texto del articulo usado para validaci�n)
public class WebConnectionConsole {
	
	static{
		Logger.getLogger(HttpMethodBase.class).setLevel(Level.OFF);	
	}
	
	private static final String DEFAULT_DOWNLOAD_DIR = "./Docs/";
	private static int profundidad_max = 2; // profundidad maxima de enlaces
											// HTML a mirar
	// partiendo de la profundidad 0
	private static Vector<URL> urls_antiguas = new Vector<URL>();	// url's descargadas
	private static ArticleData art;	// datos del articulo necesarios en la
										// bsqueda del pdf
	private static String ruta_id;		// ruta de la carpeta donde se almacenar el
										// pdf del articulo
	private static HttpClient client;
	private static int downloaded = 0;
	private static int total;

	static {
		// Create an instance of HttpClient.
		client = new HttpClient();
		// setting property
		if(InitConfiguration.getProxy()!=null && !InitConfiguration.getProxy().type().equals(Type.DIRECT))
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,InitConfiguration.getProxy());	
		// para aumentar la compatibilidad con scripts CGI mal escritos se ponen
		// todas las cookies en una nica peticion
		client.getParams().setParameter("http.protocol.single-cookie-header", true);
	}
	
	
	
//	public static boolean buscarPDF(String idArticulo) {
//		return WebConnectionConsole.buscarPDF(WebConnectionConsole.DEFAULT_DOWNLOAD_DIR, idArticulo);
//	}
	
	public static boolean buscarPDF(IPublication pub, String ruta_descarga, String idArticulo,boolean ignorePDFArticleContent) {
		try {
			ArticleData datosArticulo = new ArticleData(pub.getId(),idArticulo);
			return WebConnectionConsole.buscarPDF(ruta_descarga, datosArticulo,ignorePDFArticleContent);
		} catch (Exception e) {
			return false;
		}
	}

	// busca el pdf (y lo descarga si es posible) a partir de los datos del
	// articulo dados (indicando finalmente si se ha encontrado o no el pdf)
	public static boolean buscarPDF(ArticleData artic,boolean ignorePDFArticleContent) {
		return WebConnectionConsole.buscarPDF(WebConnectionConsole.DEFAULT_DOWNLOAD_DIR, artic,ignorePDFArticleContent);
	}

	// busca el pdf (y lo descarga si es posible sobre la ruta indicada) a partir de los
	// datos del articulo dados (indicando finalmente si se ha encontrado o no el pdf)
	public static synchronized boolean buscarPDF(String ruta_descargas, ArticleData artic,boolean ignorePDFArticleContent) {
		if (ruta_descargas == null) ruta_descargas = WebConnectionConsole.DEFAULT_DOWNLOAD_DIR;
		
		art = artic;
		
		// si no hay url's a descargar se finaliza
		Set<String> urls = art.getEnlacesPDF();
		if (urls.isEmpty()) {
			return false;
		}
		
		// si no se dispone del abstract o el titulo del articulo se termina
		// porque no se puede comprobar que lo que se descargue sea el texto
		// completo
		if (!art.tieneTrozoTexto()) {
			return false; // no se han descargado pdf's
		}
		if (art.getTrozo_texto().isEsAbstract()) {
//			Usando abstract;
		} else {
//			Usando title:;
		}
		//ruta_id = ruta_descargas;

		ruta_id = ruta_descargas;
		//ruta_id = ruta_descargas + art.getPmid() + "/";

		File dir = new File(ruta_id);
		try {
			// crea la carpeta en la que se almacenar�n las descargas
			Comun.crear_carpeta(dir);

			
			// para cada url de partida se busca el pdf del art�culo
			for (String url:urls) {
				try {
					WebPageAnnalyses.getUrls_a_mirar().clear();
					// las url's que muestran listas de webs que contienen el
					// pdf
					// deben tener una profundidad m�xima superior en 1 a la
					// habitual
					if (url.indexOf("elsevier.com") != -1
							|| url.indexOf("ebscohost.com") != -1)
						profundidad_max = 3;
					else
						profundidad_max = 2;
					url = Comun.sustituir(url, "&amp;", "&");
					// si se ha encontrado el pdf se para
					if (algoritmo(new URL(url),ignorePDFArticleContent)) {
						return true;
					}
				} catch (Exception e) {
//					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			
		} finally {
			WebPageAnnalyses.getUrls_a_mirar().clear();
			urls_antiguas.clear();
		}

		// si no se ha encontrado el pdf se borra la carpeta en la que se
		// descargarda
		return false;
	}

	// dada la url inicial realiza la bsqueda y descarga de ficheros para
	// obtener el pdf
	// (devuelve true o false segan haya encontrado o no el pdf del art�culo)
	private static boolean algoritmo(URL url_inicial,boolean ignorePDFArticleContent) throws Exception {
		int profundidad = 0; // profundidad actual
		int nuevo_nivel = 1; // n de urls que faltan por descargar en la
								// profundidad actual
//		profundidad " + profundidad;
		WebPageAnnalyses.getUrls_a_mirar().add(url_inicial);

		// Se descarga y analiza el c�digo de las url's a descargar mientras
		// haya url's que
		// descargar y no se haya alcanzado la profundidad m�xima y no se haya
		// encontrado el pdf
		while (WebPageAnnalyses.getUrls_a_mirar().size() > 0) {
			// si se miraron todas las url's de la profundidad actual se avanza
			// en profundidad
			if (nuevo_nivel == 0) {
				nuevo_nivel = WebPageAnnalyses.getUrls_a_mirar().size();
				profundidad++;
				if (profundidad > profundidad_max || nuevo_nivel == 0)
					break;
//				profundidad " + profundidad;
			}
			// se toma la primera url de la lista se descarga y se analiza
			// (para ver si es el pdf o contiene posibles enlaces al pdf)
			URL url = WebPageAnnalyses.getUrls_a_mirar().remove(0);
			nuevo_nivel--;
			if (url != null) {
				// si ya se hab�a analizado la p�gina de la url no se vuelve a
				// descargar
				if (Comun.urlEn(url, urls_antiguas))
					continue;
				urls_antiguas.add(url);
				String fich = ruta_id + art.getPublicationID() + ".pdf";
				URL nueva_url = descargarPagina(url, fich);
				// si la url final de la pagina descargada es distinta de la
				// inicial
				// se aade a urls_antiguas si no estaba y si ya estaba no se
				// analizar�
				if (nueva_url != null && !url.sameFile(nueva_url)) {
					// se pone a null, para no entrar en el bloque de an�lisis,
					// pero s� borrar el fichero de la p�gina descargada
					if (Comun.urlEn(nueva_url, urls_antiguas))
						nueva_url = null;
					else
						urls_antiguas.add(nueva_url);
				}
				File file = new File(fich);
				if (file.exists()) {
					if (nueva_url != null) {
						String cad = Comun.leer_fichero(fich);
						// si el fichero descargado es un pdf (no tiene porqu�
						// ser el texto completo)
						if (Comun.esPDF(cad)) {
							// no se avanzar� m�s en profundidad porque se
							// supone que el pdf del art�culo
							// deber�a estar al mismo nivel o menor que el de
							// cualquier otro pdf
							profundidad_max = profundidad;
							// si el trozo de texto disponible est� contenido en
							// el pdf descargado
							// se ha encontrado el texto completo y se finaliza
							if (tieneTrozoTexto(fich,ignorePDFArticleContent)) {
								return true;
							}
						}
						// si el fichero descargado es HTML se analizar� la
						// p�gina en busca de
						// posibles enlaces que lleven al pdf
						else if (Comun.esHTML(cad)) {
							if (profundidad != profundidad_max) {
								WebPageAnnalyses.addEnlaces(nueva_url.toString(), cad);
							}
						}
					}
					// se eliminan los ficheros que no sean el pdf
					Comun.borrar_fichero(file);
				}
			}
		}
		// si se han mirado todos los enlaces no se ha encontrado el pdf
		return false;
	}

	// descarga el c�digo fuente de la url indicada en el fichero indicado
	private static URL descargarPagina(URL url, String fich) {
		URL nueva_url = null;
		String url_cad = Comun.getUTF8String(url.toString());
		
//		"Bajando " + url_cad + " a " + fich;

		GetMethod method;
		
		try {
			// se crea una solicitud de tipo get con la url
			method = new GetMethod(url_cad);
		} catch (Exception e) {
			return null;
		}

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(3, false));
		// para manejar cookies con compatibilidad para distintos tipos de
		// cookies
		method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

		try {
			FileOutputStream out = new FileOutputStream(fich);
			try {
				// se ejecuta la solicitud
				int statusCode = client.executeMethod(method);
				// si se obtiene alg�n c�digo de error se devuelve null
				if (statusCode != HttpStatus.SC_OK) {
//					System.err.println("Method failed: " + method.getStatusLine());
					if (statusCode == HttpStatus.SC_NOT_FOUND
							|| statusCode == HttpStatus.SC_FORBIDDEN
							|| statusCode == HttpStatus.SC_UNAUTHORIZED
							|| statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR)
						return null;
				}

				// se va descargando la p�gina y guard�ndola en el fichero
				// indicado
				InputStream in = method.getResponseBodyAsStream();
				total = (int) (method.getResponseContentLength() / 4096);
				if (method.getResponseContentLength() % 4096 != 0)
					total++;
				if (in != null) {
					byte[] buffer = new byte[4096];
					int bytes_read;
					downloaded = 0;
					while ((bytes_read = in.read(buffer)) != -1) {
						out.write(buffer, 0, bytes_read);
						downloaded++;
					}
					nueva_url = new URL(method.getURI().toString());
					in.close();
				}
			} catch (HttpException e) {
//				System.err.println("Fatal protocol violation: " + e.getMessage());
			} catch (IOException e) {
//				System.err.println("Fatal transport error: " + e.getMessage());
			} finally {
				// Release the connection.
				method.releaseConnection();
				out.close();
			}
		} catch (Exception e) {
//			System.err.println("No existe el fichero o no se tiene acceso para escribir");
//			e.printStackTrace(System.err);
		}

		return nueva_url;
	}

	// comprueba si el trozo de texto disponible est� contenido en el texto del
	// fichero pdf
	// indicado dada la ruta en la que se encuentra almacenado localmente
	private static boolean tieneTrozoTexto(String archivo,	boolean ignoretextcontn) {
		String cad = art.getTrozo_texto().getTexto();
		String text = TryPDFBox.getTextoPDF(archivo);

		if (text == null || text.isEmpty())
		{
			try {
				text =  PDFtoText.fileOCR(archivo);
			} catch (Exception e) {
			}
		}
		if((text == null || text.isEmpty()) && ignoretextcontn)
		{
			System.out.print("... ignore text content..");
			return true;
		}

		// si se dispone del abstract se buscar� dicho abstract en los 10000
		// primeros
		// caracteres del texto (que es aproximadamente lo que mide la primera
		// p�gina)
		if (art.getTrozo_texto().isEsAbstract()) {
			int max = 10000;
			if (max > text.length())
				max = text.length();
			text = text.substring(0, max);
		}
		// si se dispone del t�tulo se buscar� dicho t�tulo en los 200 primeros
		// caracteres del
		// texto (o si el t�tulo es muy largo en un espacio de 4 veces la
		// longitud del t�tulo)
		else {
			int max = 4 * cad.length();
			if (max < 200)
				max = 200;
			if (max > text.length())
				max = text.length();
			text = text.substring(0, max);
		}
		// se eliminan los caracteres que separan palabras en el texto del pdf
		// (" ,.:-\t\n\r\f")
		text = text.replaceAll("[ ,.:\\x2D\t\n\r\f]", "");
		StringTokenizer st_cad = new StringTokenizer(cad, " ,.:-\t\n\r\f");
		int n = st_cad.countTokens();
		int good_tokens = 0;
		// se comprueban las palabras del trozo del texto que se encuentran en
		// el texto
		while (st_cad.hasMoreTokens()) {
			String tok1 = st_cad.nextToken();
			if (Comun.indexOfIgnoreCase(text, tok1) != -1)
				good_tokens++;
		}
		
//		good_tokens + "/" + ;
		// si el porcentaje de palabras encontradas es 0.8 o superior el texto
		// contiene el trozo de texto y posiblemente sea el texto completo del
		// art�culo
		if (((double) good_tokens / n) >= 0.8)
			return true;
		return false;
	}

	public static MyProgress getProgress() {
		return new MyProgress();
	}

	public static class MyProgress {
		// devuelve el progreso de la descarga
		public float getTotal() {
			return (float) downloaded / (float) total;
		}
	}
	
}