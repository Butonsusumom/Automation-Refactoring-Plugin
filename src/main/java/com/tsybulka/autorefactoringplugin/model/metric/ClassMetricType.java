package com.tsybulka.autorefactoringplugin.model.metric;

import lombok.Getter;

/**
 * Types of OOP metrics that are calculated for class
 */
@Getter
public enum ClassMetricType {
	LINES_OF_CODE("LOC"),
	NUMBER_OF_FIELDS("NOF"),
	NUMBER_OF_PUBLIC_FIELDS("NOPF"),
	NUMBER_OF_METHODS("NOM"),
	NUMBER_OF_PUBLIC_METHODS("NOPM"),
	WEIGHT_METHODS("WMC"),
	NUMBER_OF_CHILDREN("NOC"),
	DEPTH_OF_INHERITANCE_TREE("DIT"),
	LACK_OF_COHESION_IN_METHOD("LCOM"),
	FAN_IN("FANIN"),
	FAN_OUT("FANOUT");

	private final String value;

	ClassMetricType(String value) {
		this.value = value;
	}

}
