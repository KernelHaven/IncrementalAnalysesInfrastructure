package net.ssehub.kernel_haven.incremental.evaluation;

public class Result {

	enum ResultQuality {
		EQUIVALENT, SAME, DIFFERENT, BASELINE
	}

	private String resultFileName;

	private int timeForAnalysis;
	private int timeForExtraction;

	private int analysisExtractionOverlap;

	private int timeForPreparation;
	private int timeForPostExtraction;

	private ResultQuality resultQuality;
	
	public int getTimeForAnalysis() {
		return timeForAnalysis;
	}

	public void setTimeForAnalysis(int timeForAnalysis) {
		this.timeForAnalysis = timeForAnalysis;
	}

	public int getTimeForExtraction() {
		return timeForExtraction;
	}

	public void setTimeForExtraction(int timeForExtraction) {
		this.timeForExtraction = timeForExtraction;
	}

	public int getTimeForPreparation() {
		return timeForPreparation;
	}

	public void setTimeForPreparation(int timeForPreparation) {
		this.timeForPreparation = timeForPreparation;
	}

	public int getTimeForPostExtraction() {
		return timeForPostExtraction;
	}

	public void setTimeForPostExtraction(int timeForPostExtraction) {
		this.timeForPostExtraction = timeForPostExtraction;
	}

	public int getAnalysisExtractionOverlap() {
		return analysisExtractionOverlap;
	}

	public void setAnalysisExtractionOverlap(int analysisExtractionOverlap) {
		this.analysisExtractionOverlap = analysisExtractionOverlap;
	}

	public String getResultFileName() {
		return resultFileName;
	}

	public Result(String resultFileName) {
		super();
		this.resultFileName = resultFileName;
	}

	public ResultQuality getResultQuality() {
		return resultQuality;
	}

	public void setResultQuality(ResultQuality resultQuality) {
		this.resultQuality = resultQuality;
	}


}
