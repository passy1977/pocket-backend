import java.util.regex.Pattern;
public class TestPattern {
    public static void main(String[] args) {
        String crypt = "XYBVr0m06G1ruwqeWqdiMR_vz0qY8BKBn5Jt1IkmGF4BBk8nf3_H11T_kKYjmNmw6bb5bGWE8y0QkNAu2GdoULLW-JnljGgefSzAdNmBvbZ00cJ_6Je5yGRewEmPHKLqJTj_DzWyqBVgyuXkoOXj-oe_5fOTITZRaPi3s85nsNdH-yxhKSSkEySmi56uYlsTFLt43_Hm2ShHJ7gIrHRxZ8WrLVPMmt82L94WMHo_H8-jU-kVWBJk4qUh4yrm7LzQbCdw2-P1EVeHDgolQULS708hTqYJ-ikGU5fq6lHIuT8reJk2q9qI9G_fhsSO0q-1GjRyA1L8jR_aZR0DHAjCng==";
        Pattern authPattern = Pattern.compile("^[A-Za-z0-9_-]+={0,2}$");
        Pattern restPattern = Pattern.compile("^[A-Za-z0-9_-]{10,2048}={0,2}$");
        
        System.out.println("String length: " + crypt.length());
        System.out.println("Auth Pattern matches: " + authPattern.matcher(crypt).matches());
        System.out.println("REST Pattern matches: " + restPattern.matcher(crypt).matches());
        System.out.println("String ends with: " + crypt.substring(crypt.length()-3));
    }
}
