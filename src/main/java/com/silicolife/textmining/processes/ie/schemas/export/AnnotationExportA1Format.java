package com.silicolife.textmining.processes.ie.schemas.export;

public class AnnotationExportA1Format {
	
	private String startOffset;
	private String endOffset;
	private String tagEntity;
	private String classEntity;
	private String dbNormalization;
	
	
	
	public AnnotationExportA1Format(String startOffset, String endOffset, String tagEntity, String classEntity,
			String dbNormalization) {
		super();
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.tagEntity = tagEntity;
		this.classEntity = classEntity;
		this.dbNormalization = dbNormalization;
	}
	
	public String getStartOffset() {
		return startOffset;
	}
	public void setStartOffset(String startOffset) {
		this.startOffset = startOffset;
	}
	public String getEndOffset() {
		return endOffset;
	}
	public void setEndOffset(String endOffset) {
		this.endOffset = endOffset;
	}
	public String getTagEntity() {
		return tagEntity;
	}
	public void setTagEntity(String tagEntity) {
		this.tagEntity = tagEntity;
	}
	public String getClassEntity() {
		return classEntity;
	}
	public void setClassEntity(String classEntity) {
		this.classEntity = classEntity;
	}
	public String getDbNormalization() {
		return dbNormalization;
	}
	public void setDbNormalization(String dbNormalization) {
		this.dbNormalization = dbNormalization;
	}

}
