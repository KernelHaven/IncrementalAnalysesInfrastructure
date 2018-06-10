package net.ssehub.kernel_haven.incremental.evaluation;

public class QualityResult {

	enum ResultQuality {
		EQUIVALENT, SAME, DIFFERENT, BASELINE
	}

	private String resultFileName;


	private ResultQuality resultQuality;
	
	

	public QualityResult(String resultFileName) {
		super();
		this.setResultFileName(resultFileName);
	}

	public ResultQuality getResultQuality() {
		return resultQuality;
	}

	public void setResultQuality(ResultQuality resultQuality) {
		this.resultQuality = resultQuality;
	}

	public String getResultFileName() {
		return resultFileName;
	}

	public void setResultFileName(String resultFileName) {
		this.resultFileName = resultFileName;
	}


}
