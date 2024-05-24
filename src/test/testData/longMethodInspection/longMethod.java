package testData.longMethodInspection;

class LongMethodTest {
	public void exceedParametersLimit(int a, int b, int c, int d, int e, int f) {
		// This method has more than 5 parameters.
		int sum = a + b + c + d + e + f;
		System.out.println("Sum is: " + sum);
	}

	public void exampleMethod() {
		for (int i = 0; i < 5; i++) {
			// Level 1
			if (i % 2 == 0) {
				// Level 2
				while (i < 3) {
					// Level 3
					for (int j = 0; j < 3; j++) {
						// Level 4
						System.out.println("Nested Level 4: i = " + i + ", j = " + j);
					}
				}
			}
		}
		exceedParametersLimit(1, 2, 3, 4, 5, 6);
	}

	public void methodExceedingLocLimit(boolean flag) {
		if (flag) {
			System.out.println("Line 1");
			System.out.println("Line 2");
			System.out.println("Line 3");
			System.out.println("Line 4");
			System.out.println("Line 5");
			System.out.println("Line 6");
			System.out.println("Line 7");
			System.out.println("Line 8");
			System.out.println("Line 9");
			System.out.println("Line 10");
		}
		System.out.println("Line 11");
		System.out.println("Line 12");
		System.out.println("Line 13");
		System.out.println("Line 14");
		System.out.println("Line 15");
		System.out.println("Line 16");
		System.out.println("Line 17");
		System.out.println("Line 18");
		System.out.println("Line 19");
		System.out.println("Line 20");
		System.out.println("Line 21");
		System.out.println("Line 22");
		System.out.println("Line 23");
		System.out.println("Line 24");
		System.out.println("Line 25");
		System.out.println("Line 26");
		System.out.println("Line 27");
		System.out.println("Line 28");
		System.out.println("Line 29");
		System.out.println("Line 30");
		System.out.println("Line 31");
		System.out.println("Line 32");
		System.out.println("Line 33");
		System.out.println("Line 34");
		System.out.println("Line 35");
		System.out.println("Line 36");
		System.out.println("Line 37");
		System.out.println("Line 38");
		System.out.println("Line 39");
		System.out.println("Line 40");
		System.out.println("Line 41");
		System.out.println("Line 42");
		System.out.println("Line 43");
		System.out.println("Line 44");
		System.out.println("Line 45");
		System.out.println("Line 46");
		System.out.println("Line 47");
		System.out.println("Line 48");
		System.out.println("Line 49");
		System.out.println("Line 50");
		System.out.println("Line 51");
		System.out.println("Line 52");
		System.out.println("Line 53");
		System.out.println("Line 54");
		System.out.println("Line 55");
		System.out.println("Line 56");
		System.out.println("Line 57");
		System.out.println("Line 58");
		System.out.println("Line 59");
		System.out.println("Line 60");
		System.out.println("Line 61");
		System.out.println("Line 62");
		System.out.println("Line 63");
		System.out.println("Line 64");
		System.out.println("Line 65");
		System.out.println("Line 66");
		System.out.println("Line 67");
		System.out.println("Line 68");
		System.out.println("Line 69");
		System.out.println("Line 70");
		System.out.println("Line 71");
		System.out.println("Line 72");
		System.out.println("Line 73");
		System.out.println("Line 74");
		System.out.println("Line 75");
		System.out.println("Line 76");
		System.out.println("Line 77");
		System.out.println("Line 78");
		System.out.println("Line 79");
		System.out.println("Line 80");
		System.out.println("Line 81");
		System.out.println("Line 82");
		System.out.println("Line 83");
		System.out.println("Line 84");
		System.out.println("Line 85");
		System.out.println("Line 86");
		System.out.println("Line 87");
		System.out.println("Line 88");
		System.out.println("Line 89");
		System.out.println("Line 90");
		System.out.println("Line 91");
		System.out.println("Line 92");
		System.out.println("Line 93");
		System.out.println("Line 94");
		System.out.println("Line 95");
		System.out.println("Line 96");
		///// line
		// comment
		System.out.println("Line 97");
		System.out.println("Line 98");
		System.out.println("Line 99");
		System.out.println("Line 100");
	}
}