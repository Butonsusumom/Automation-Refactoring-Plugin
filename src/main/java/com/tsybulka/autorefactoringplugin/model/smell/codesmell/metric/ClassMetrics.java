package com.tsybulka.autorefactoringplugin.model.smell.codesmell.metric;

import com.tsybulka.autorefactoringplugin.model.metric.ClassMetricType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;

/**
 * OOP metrcis for class
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClassMetrics {

	private String packageName;
	private String className;
	private String filePath;
	private HashMap<ClassMetricType, Integer> metrics = new HashMap<>();

}