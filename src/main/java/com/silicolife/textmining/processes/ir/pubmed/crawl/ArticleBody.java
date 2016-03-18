package com.silicolife.textmining.processes.ir.pubmed.crawl;

//clase que representa el trozo de texto del art�culo (abstract o titulo)
//que se dispone de entrada para el algoritmo
public class ArticleBody
{
  private String texto; //trozo de texto disponible
  private boolean esAbstract; //indica si es el abstract o no (titulo)
  
  public ArticleBody(String text, boolean abs)
  {
	texto=text;
	esAbstract=abs;
  }
  
  public String getTexto() {
	return texto;
  }

  public boolean isEsAbstract() {
	return esAbstract;
  }
}