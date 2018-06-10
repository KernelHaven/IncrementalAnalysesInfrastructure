package net.ssehub.kernel_haven.incremental.evaluation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Result {

	enum ResultQuality {
		EQUIVALENT, SAME, DIFFERENT, BASELINE
	}

	private String resultFileName;


	private ResultQuality resultQuality;
	


	private Map<String, List<Long>> analysisComponentTime = new HashMap<String, List<Long>>();
	
	private LocalDateTime startPreparationPhaseTime = null;
	private LocalDateTime finishPreparationPhasTime = null;
	private LocalDateTime startExtractionPhaseTime = null;
	private LocalDateTime endExtractionPhaseTime = null;
	private LocalDateTime startAnalysisPhaseTime = null;
	private LocalDateTime endAnalysisPhaseTime = null;
	private LocalDateTime endTime = null;
	private LocalDateTime startTime = null;

	public List<Long> getAnalysisComponentTime(String component) {
		return analysisComponentTime.get(component);
	}

	public void addAnalysisComponentTime(String component, long time) {
		this.analysisComponentTime.putIfAbsent(component, new ArrayList<Long>());
		this.analysisComponentTime.get(component).add(time);
	}

	public void setResultFileName(String resultFileName) {
		this.resultFileName = resultFileName;
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
