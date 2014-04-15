/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package phishphoiler;

import phishphoiler.data.Location;
import phishphoiler.data.State;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

import java.net.HttpURLConnection;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import phishphoiler.cmd.APCommand;


/**
 *
 * @author William Doyle
 */
public class Generator implements Constants, Serializable {
    private ArrayList<Location> locList;
    private ArrayList<String> nameFemaleList;
    private ArrayList<String> nameMaleList;
    private ArrayList<String> dictList;
    private Hashtable bankLists;
    
    public Generator() {
        InputStream is = Generator.class.getResourceAsStream("zip.csv");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            locList = new ArrayList<Location>();
            while (null!=(line=br.readLine()))
                locList.add(new Location(line));
        }
        catch (IOException ioe) { ; }

        is = Generator.class.getResourceAsStream("bank.csv");
        br = new BufferedReader(new InputStreamReader(is));
        try {
            bankLists = new Hashtable();            
            while (null!=(line=br.readLine())) {
                if (0==line.length())
                    continue;
                StringTokenizer st = new StringTokenizer(line.trim(),";");
                String bankName = st.nextToken();
                String bankState = st.nextToken();
                ArrayList stateList = null;
                if (!bankLists.containsKey(bankState)) {
                     stateList = new ArrayList();
                     bankLists.put(bankState,stateList);
                }
                else
                    stateList = (ArrayList)bankLists.get(bankState);
                stateList.add(bankName);                
            }
        }
        catch (IOException ioe) { ; }
        
        is = Generator.class.getResourceAsStream("name_female.csv");
        br = new BufferedReader(new InputStreamReader(is));
        try {
            nameFemaleList = new ArrayList<String>();
            while (null!=(line=br.readLine()))
                nameFemaleList.add(line.trim());
        }
        catch (IOException ioe) { ; }
        is = Generator.class.getResourceAsStream("name_male.csv");
        br = new BufferedReader(new InputStreamReader(is));
        try {
            nameMaleList = new ArrayList<String>();
            while (null!=(line=br.readLine()))
                nameMaleList.add(line.trim());            
        }
        catch (IOException ioe) { ; }
        is = Generator.class.getResourceAsStream("dict.csv");
        br = new BufferedReader(new InputStreamReader(is));
        try {
            dictList = new ArrayList<String>();
            while (null!=(line=br.readLine()))
                dictList.add(line.trim());            
        }
        catch (IOException ioe) { ; }        
        instance = this;
    }
    
    public String getDictionaryWord() {
        Random r = getRandom();
        return dictList.get(r.nextInt(dictList.size()));
    }
    
    public Location getLocation() {
        Random r = getRandom();
        return locList.get(r.nextInt(locList.size()));
    }
    
    public String getBank(String stateAbbreviation) {
        Random r = getRandom();
        ArrayList al = (ArrayList)bankLists.get(stateAbbreviation);
        while (null == al) {
            al = (ArrayList)bankLists.get(getLocation().getAbbreviation());
        }
        return (String)al.get(r.nextInt(al.size()));
    }
    
    public String getName(short sex) {
        Random r = getRandom();
        List nameArray[] = new List[2];
        nameArray[NAME_OP_SEX_MALE] = nameMaleList;
        nameArray[NAME_OP_SEX_FEMALE] = nameFemaleList;
        int idx = r.nextInt(NAME_OP_SEX_EITHER);
        if (NAME_OP_SEX_MALE == sex)
            idx = NAME_OP_SEX_MALE;
        if (NAME_OP_SEX_FEMALE == sex)
            idx = NAME_OP_SEX_FEMALE;            
        return (String)nameArray[idx].get(r.nextInt(nameArray[idx].size()));
        //return APUtils.mixCase();
    }
    
    public String getSocialSecurityNumber(short hyphen) {        
        Random r = getRandom();
        String areaNumber = String.format("%03d", getRandomInt(SOCIAL_SECURITY_AREA_MIN,SOCIAL_SECURITY_AREA_MAX));
        String groupNumber = String.format("%02d", getRandomInt(SOCIAL_SECURITY_GROUP_MIN,SOCIAL_SECURITY_GROUP_MAX));
        String serialNumber = String.format("%04d", getRandomInt(SOCIAL_SECURITY_SERIAL_MIN,SOCIAL_SECURITY_SERIAL_MAX));
        int idx = r.nextInt(2);
        if (SOCIAL_SECURITY_OP_HYPHEN_OFF == hyphen)
            idx = SOCIAL_SECURITY_OP_HYPHEN_OFF;
        if (SOCIAL_SECURITY_OP_HYPHEN_ON == hyphen)
            idx = SOCIAL_SECURITY_OP_HYPHEN_ON;
        String ssn = areaNumber+groupNumber+serialNumber;
        if (SOCIAL_SECURITY_OP_HYPHEN_ON == idx)
            ssn = areaNumber+"-"+groupNumber+"-"+serialNumber;
        return ssn;
    }  
    
    public String getStreetSuffix(short abbreviation, short period) {
        Random r = getRandom();
        int idx = r.nextInt(2);
        if (STREET_SUFFIX_OP_ABBREV_OFF == abbreviation)
            idx = STREET_SUFFIX_OP_ABBREV_OFF;
        if (STREET_SUFFIX_OP_ABBREV_ON == abbreviation) {
            idx = STREET_SUFFIX_OP_ABBREV_ON;            
        }        
        String stsuf = STREET_SUFFIX[r.nextInt(STREET_SUFFIX.length)][idx];
        if (STREET_SUFFIX_OP_ABBREV_ON == idx) {
            int pidx = r.nextInt(2);
            if (STREET_SUFFIX_OP_ABBREVP_OFF == period)
                pidx = STREET_SUFFIX_OP_ABBREVP_OFF;
            if (STREET_SUFFIX_OP_ABBREVP_ON == period)
                pidx = STREET_SUFFIX_OP_ABBREVP_ON;
            if (STREET_SUFFIX_OP_ABBREVP_OFF == pidx && stsuf.indexOf(".")>-1)
                stsuf = stsuf.replace(".","");            
        }
        return stsuf;
    }
    
    public String getDomainSuffix() {
        Random r = getRandom();
        return DOMAIN_SUFFIX[r.nextInt(DOMAIN_SUFFIX.length)];
    }
    
    public State getState() {
        Random r = getRandom();
        return new State(STATE[r.nextInt(STATE.length)]);
    }
    
    public String getYear(boolean shrt,int min, int max) {
        Random r = getRandom();
        String year = String.valueOf(r.nextInt(max-min+1)+min);
        if (shrt)
            return year.substring(2,4);
            //return YEAR[r.nextInt(YEAR.length)].substring(2,4);
        else
            return year;
    }
    
    public String getRandom(int min, int max) {
        return String.valueOf(getRandomInt(min,max));
    }
    
    public int getRandomInt(int min, int max) {
        Random r = getRandom();
        return r.nextInt(max-min+1)+min;   
    }
    
    public String getMonth(boolean lead) {
        Random r = getRandom();
        String month = MONTH[r.nextInt(MONTH.length)];
        if (!lead && '0' == month.charAt(0))
            month = String.valueOf(month.charAt(1));
        return month;
    }
    
    public String getDay(String month, boolean lead) {
        Random r = getRandom();
        int m = Integer.valueOf(month)-1;
        String day = String.valueOf(r.nextInt(DAY[m])+1);
        if (lead && 1 == day.length())
            day = "0" + day;
        return day;
    }
    
    public String getCC(boolean amex, boolean visa, boolean mast, boolean disc) {
        Random r = getRandom();
        List<Integer> listType = new ArrayList<Integer>();
        if (amex)
            listType.add(new Integer(AMEX));
        if (visa)
            listType.add(new Integer(VISA));
        if (mast)
            listType.add(new Integer(MAST));
        if (disc)
            listType.add(new Integer(DISC));
        int type = listType.get(r.nextInt(listType.size())).intValue();

        String prefix = String.valueOf(type);
        String suffix = String.format("%08d", r.nextInt(100000000));
        switch (type) {
            case AMEX:
                return prefix+String.format("%06d", r.nextInt(1000000))+suffix;
            case VISA:
                return prefix+String.format("%07d", r.nextInt(10000000))+suffix;
            case MAST:
                return prefix+String.format("%07d", r.nextInt(10000000))+suffix;
            default:
                return prefix+String.format("%07d", r.nextInt(10000000))+suffix;
        }
    }

    public static Generator getInstance() {
        if (null == instance)
            instance = new Generator();
        return instance;
    }
    
    public static Random getRandom() {
        if (null == random)
            random = new Random();
        return random;
    }
    
    public static Generator instance = null;
    public static Random random = null;
    
    private boolean checkCC(int[] digits)
    {
      int sum = 0;
      boolean alt = false;
      int thedigit;
      for(int i = digits.length - 1; i >= 0; i--)
      {
        thedigit = digits[i];
        if(alt)
        {
          thedigit = 2*thedigit;
          if(thedigit > 9)
          {
            thedigit -= 9; 
          }
        }
        sum += thedigit;
        alt = !alt;
      }
      return sum % 10 == 0;
    }
    
    private void printCC(int[] digits) {
        int sum = 0;
        for (int i=0;i<digits.length;i++) {
            sum+=digits[i];
            System.err.print(digits[i] + " ");
        }        
        System.err.println(" : " + sum);        
    }
    
    public String getCCnew(List<CreditCard> cardType) {
        Random r = getRandom();
        CreditCard type = cardType.get(r.nextInt(cardType.size()));        
        int lengths[] = type.getLengths();
        String prefixes[] = type.getPrefixes();
        int length = lengths[r.nextInt(lengths.length)];
        String prefix = prefixes[r.nextInt(prefixes.length)];
        int idx = prefix.indexOf("-");
        if (idx>-1) {
            int min = Integer.valueOf(prefix.substring(0,idx));
            int max = Integer.valueOf(prefix.substring(idx+1));
            prefix = String.valueOf(r.nextInt(max-min+1)+min);
        }
        //System.err.println("CCP: " + prefix + " " + length);
        return getCC2(prefix,length);
    }
    
    public String getCC2(String prefix, int length) {
        StringBuilder sb = new StringBuilder();
        Random r = getRandom();
        //card length
        int digits[] = new int[length];
        //prefixing
        int prefixCount = prefix.length();
        int sum = 0;
        for (int i=0;i<prefixCount;i++) {
            digits[i] = Integer.parseInt(String.valueOf(prefix.charAt(i)));                
            if (i%2==length%2) digits[i]*=2;
            if (digits[i]>9)
                digits[i] = 1 + (digits[i]-10);
            sum+=digits[i];
        }
        //generate other digits
        for (int i=prefixCount;i<digits.length;i++) {
            digits[i] = r.nextInt(10);
            sum+=digits[i];
        }
        //find number divisible by 10 close to sum
        int nsum = sum;
        if (sum%10!=0) { 
            nsum = sum - (sum%10);
            nsum+=(10*r.nextInt(2));        
        }                
        //System.err.println(" : " + sum + " " + nsum);
        int diff = nsum-sum;
        boolean isNegative = (diff < 0);
        diff = (isNegative) ? -diff : diff;
        while (0 != diff) {
            int digit = r.nextInt(digits.length-prefixCount)+prefixCount;
            int digitValue = digits[digit];
            if (isNegative) {
                int temp = r.nextInt(Math.min(diff,digitValue)+1);
                digits[digit] = digitValue - temp;
                diff-=temp;
            }
            else {
                int temp = r.nextInt(Math.min(diff,9-digitValue)+1);
                digits[digit] = digitValue + temp;
                diff-=temp;
            }        
        }
        // convert to CC number
        for (int i=(digits.length-1);i>=0;i--) {
            int b = digits.length-1-i;
            if (b%2==1 && digits[i]%2!=0)
                digits[i] = 10 + (digits[i])-1;
            if (b%2==1)
                digits[i] /= 2;

        }        

        for (int i=0;i<digits.length;i++) {
            sb.append(digits[i]);
        }        
        //System.err.println(checkCC(digits));
        return sb.toString();        
    }
    
    // TODO: remove after testing
    public static void main(String[] args) {
        //Generator g = Generator.getInstance();
        //List<CreditCard> lcc = new ArrayList<CreditCard>();
        //lcc.add(CreditCard.CC_VISA);
        //System.err.println(g.getCCnew(lcc));
        try {
            URL u = new URL("http://www.google.com/search");
            HttpURLConnection huc = (HttpURLConnection)u.openConnection();
            huc.setFollowRedirects(true);
            huc.setRequestMethod("POST");
            huc.addRequestProperty("hl","en");
            huc.addRequestProperty("ie","ISO-8859-1");
            huc.addRequestProperty("btnG","Google Search");
            //huc.addRequestProperty("btnl","I'm Feeling Lucky");
            huc.addRequestProperty("q","NASA");
            huc.connect();
            System.err.println(huc.getResponseCode());
        }
        catch (Exception mue) { ; }
    }
}
