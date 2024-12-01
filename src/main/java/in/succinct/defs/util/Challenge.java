package in.succinct.defs.util;

import com.venky.core.random.Randomizer;
import com.venky.core.util.ObjectUtil;

import java.time.Duration;

public class Challenge {
    private static volatile Challenge sSoleInstance;
    
    //private constructor.
    private Challenge() {
        //Prevent form the reflection api.
        if (sSoleInstance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }
    
    public static Challenge getInstance() {
        if (sSoleInstance == null) { //if there is no instance available... create new one
            synchronized (Challenge.class) {
                if (sSoleInstance == null) sSoleInstance = new Challenge();
            }
        }
        
        return sSoleInstance;
    }
    
    //Make singleton from serialize and deserialize operation.
    protected Challenge readResolve() {
        return getInstance();
    }
    public String createRandomChallenge() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0 ; i< 6 ; i++){
            builder.append(Randomizer.getRandomNumber((i == 0 ? 1 : 0),9));
        }
        return builder.toString();
    }
    private final TimeSensitiveMap<String,String> timeSensitiveMap = new TimeSensitiveMap<>(Duration.ofMinutes(10));
    public void put(String txn, String challenge){
        timeSensitiveMap.put(txn,challenge);
    }
    public String remove(String txn){
        return timeSensitiveMap.remove(txn);
    }
    public String get(String txn){
        return timeSensitiveMap.get(txn);
    }
    public void clearExpiredChallenges(){
        timeSensitiveMap.gc();
    }
}
