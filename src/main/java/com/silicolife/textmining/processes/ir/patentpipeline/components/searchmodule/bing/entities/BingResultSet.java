package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.entities;
import java.util.ArrayList;
import java.util.Iterator;

public class BingResultSet <T> implements Iterable <T>{

	private ArrayList<T> _asrs = new ArrayList<T>();

    /*
     *  <m:properties>
     <d:ID m:type="Edm.Guid">2c486003-50f4-4494-add1-1453a34a36a7</d:ID>
     <d:WebTotal m:type="Edm.Int64">900000</d:WebTotal>
     <d:WebOffset m:type="Edm.Int64">0</d:WebOffset>
     <d:ImageTotal m:type="Edm.Int64">113000</d:ImageTotal>
     <d:ImageOffset m:type="Edm.Int64">0</d:ImageOffset>
     <d:VideoTotal m:type="Edm.Int64">10600</d:VideoTotal>
     <d:VideoOffset m:type="Edm.Int64">0</d:VideoOffset>
     <d:NewsTotal m:type="Edm.Int64">4370</d:NewsTotal>
     <d:NewsOffset m:type="Edm.Int64">0</d:NewsOffset>
     <d:SpellingSuggestionsTotal m:type="Edm.Int64" m:null="true"></d:SpellingSuggestionsTotal>
     <d:AlteredQuery m:type="Edm.String"></d:AlteredQuery>
     <d:AlterationOverrideQuery m:type="Edm.String"></d:AlterationOverrideQuery>
        
     */
    private Long _webTotal;
    private Long _webOffset;
    private String _alteredQuery;
    private String _alterationOverrideQuery;

    /**
     * @return the asrs
     */
    public ArrayList<T> getAsrs() {
        return _asrs;
    }

    /**
     * @param asrs the asrs to set
     */
    public void setAsrs(ArrayList<T> asrs) {
        _asrs = asrs;
    }

    /**
     * @return the webTotal
     */
    public Long getWebTotal() {
        return _webTotal;
    }

    /**
     * @param webTotal the webTotal to set
     */
    public void setWebTotal(Long webTotal) {
        _webTotal = webTotal;
    }

    /**
     * @return the webOffset
     */
    public Long getWebOffset() {
        return _webOffset;
    }

    /**
     * @param webOffset the webOffset to set
     */
    public void setWebOffset(Long webOffset) {
        _webOffset = webOffset;
    }


    /**
     * @return the alteredQuery
     */
    public String getAlteredQuery() {
        return _alteredQuery;
    }

    /**
     * @param alteredQuery the alteredQuery to set
     */
    public void setAlteredQuery(String alteredQuery) {
        _alteredQuery = alteredQuery;
    }

    /**
     * @return the alterationOverrideQuery
     */
    public String getAlterationOverrideQuery() {
        return _alterationOverrideQuery;
    }

    /**
     * @param alterationOverrideQuery the alterationOverrideQuery to set
     */
    public void setAlterationOverrideQuery(String alterationOverrideQuery) {
        _alterationOverrideQuery = alterationOverrideQuery;
    }

    /**
     *
     */
    public BingResultSet() {
        super();
    }

    /**
     *
     * @param result
     */
    public void addResult(T result) {
        _asrs.add(result);
    }

    /**
     *
     * @param _asrs
     */
    public BingResultSet(ArrayList<T> _asrs) {
        super();
        this._asrs = _asrs;
    }

   
    public Iterator<T> iterator() {
        // lets us use for/in
        return _asrs.iterator();
    }

}
