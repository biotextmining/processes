package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.entities;


public abstract class ABingResult {
	/**
    *
    * @return
    */
   public String getId() {
       return _id;
   }

   /**
    *
    * @param _id
    */
   public void setId(String _id) {
       this._id = _id;
   }

   /**
    *
    * @return
    */
   public String getTitle() {
       return _title;
   }

   /**
    *
    * @param _title
    */
   public void setTitle(String _title) {
       this._title = _title;
   }

   /**
    *
    */
   protected String _id;

   /**
    *
    */
   protected String _title;

}