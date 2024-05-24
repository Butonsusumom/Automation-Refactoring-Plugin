package testData.cyclomaticComplexityInspection;

class CyclomaticComplexityTest {
	public String cyclomaticComplexity11(int score) {
		if (score < 0 || score > 100) {
			throw new IllegalArgumentException("Score must be between 0 and 100.");
		}

		String grade;
		if (score >= 90) {
			grade = "A";
		} else if (score >= 80) {
			grade = "B";
		} else if (score >= 70) {
			grade = "C";
		} else if (score >= 60) {
			grade = "D";
		} else {
			grade = "F";
		}

		if (score % 10 >= 7 && !grade.equals("F")) {
			grade += "+";
		} else if (score % 10 <= 3 && !grade.equals("F")) {
			grade += "-";
		}

		return grade;
	}
}