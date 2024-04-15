public class ObjectComparisonTest {

    public boolean method1(Object s1, Object s2) {
        return s1.equals(s2);
    }

    public boolean method2(Object s1, Object s2) {
        return !s1.equals(s2);
    }

    public boolean method3(Object s1, Object s2) {
        return s1.equals(s2);
    }

    public boolean method4(Object s1, Object s2) {
        return !s1.equals(s2);
    }

    public boolean method5(int s1, int s2) {
        return s1 == s2;
    }

    public boolean method6(ObjectComparisonEnum s1, ObjectComparisonEnum s2) {
        return s1 == s2;
    }

}

enum  ObjectComparisonEnum {
    VALUE_1,
    VALUE_2,
    VALUE_3
}
