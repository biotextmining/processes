package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.entities;


public class BingWebResult extends ABingResult {
    private String _displayUrl;

    /**
     *
     * @return
     */
    public String getDisplayUrl() {
        return _displayUrl;
    }

    /**
     *
     * @param _displayUrl
     */
    public void setDisplayUrl(String _displayUrl) {
        this._displayUrl = _displayUrl;
    }

    /**
     *
     */
    protected String _description;

    /**
     *
     * @return
     */
    public String getDescription() {
        return _description;
    }

    /**
     *
     * @param _description
     */
    public void setDescription(String _description) {
        this._description = _description;
    }

    /**
     *
     */
    protected String _url;

    /**
     *
     * @return
     */
    public String getUrl() {
        return _url;
    }

    /**
     *
     * @param _url
     */
    public void setUrl(String _url) {
        this._url = _url;
    }

}
