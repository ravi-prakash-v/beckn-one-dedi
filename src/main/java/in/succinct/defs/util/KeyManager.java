package in.succinct.defs.util;

import com.venky.core.date.DateUtils;
import com.venky.core.security.Crypt;
import com.venky.swf.db.Database;
import com.venky.swf.db.model.CryptoKey;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.routing.Config;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;
import in.succinct.beckn.Request;
import in.succinct.defs.db.model.did.subject.Subject;
import in.succinct.defs.db.model.did.subject.VerificationMethod;
import in.succinct.defs.db.model.did.subject.VerificationMethod.HashAlgorithm;
import in.succinct.defs.db.model.did.subject.VerificationMethod.PublicKeyType;
import in.succinct.defs.db.model.did.subject.VerificationMethod.Purpose;
import org.apache.lucene.index.DocIDMerger.Sub;

import java.security.KeyPair;
import java.time.Duration;
import java.util.List;

public class KeyManager {
    private static volatile KeyManager sSoleInstance;
    
    //private constructor.
    private KeyManager() {
        //Prevent form the reflection api.
        if (sSoleInstance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }
    
    public static KeyManager getInstance() {
        if (sSoleInstance == null) { //if there is no instance available... create new one
            synchronized (KeyManager.class) {
                if (sSoleInstance == null) sSoleInstance = new KeyManager();
            }
        }
        
        return sSoleInstance;
    }
    
    //Make singleton from serialize and deserialize operation.
    protected KeyManager readResolve() {
        return getInstance();
    }
    
    public void generateInitialKeys() {
        String selfKeyId = "%s.k1".formatted(Config.instance().getHostName());
        
        CryptoKey key = CryptoKey.find(selfKeyId, CryptoKey.PURPOSE_SIGNING);
        
        if (key.getRawRecord().isNewRecord()) {
            KeyPair pair = Crypt.getInstance().generateKeyPair(Request.SIGNATURE_ALGO, Request.SIGNATURE_ALGO_KEY_LENGTH);
            key.setAlgorithm(Request.SIGNATURE_ALGO);
            key.setPrivateKey(Crypt.getInstance().getBase64Encoded(pair.getPrivate()));
            key.setPublicKey(Crypt.getInstance().getBase64Encoded(pair.getPublic()));
            key.save();
        }
        
        CryptoKey encryptionKey = CryptoKey.find(selfKeyId, CryptoKey.PURPOSE_ENCRYPTION);
        if (encryptionKey.getRawRecord().isNewRecord()) {
            KeyPair pair = Crypt.getInstance().generateKeyPair(Request.ENCRYPTION_ALGO, Request.ENCRYPTION_ALGO_KEY_LENGTH);
            encryptionKey.setAlgorithm(Request.ENCRYPTION_ALGO);
            encryptionKey.setPrivateKey(Crypt.getInstance().getBase64Encoded(pair.getPrivate()));
            encryptionKey.setPublicKey(Crypt.getInstance().getBase64Encoded(pair.getPublic()));
            encryptionKey.save();
        }
        
        Subject subject  = Database.getTable(Subject.class).newRecord();
        subject.setName(Config.instance().getHostName());
        subject.save();
        
        VerificationMethod verificationMethod = Database.getTable(VerificationMethod.class).newRecord();
        verificationMethod.setPurpose(Purpose.KeyAgreement.name());
        verificationMethod.setType(PublicKeyType.X25519.name());
        verificationMethod.setPublicKey(encryptionKey.getPublicKey());
        verificationMethod.setName("%s.%s".formatted(key.getAlias(),verificationMethod.getType()));
        verificationMethod.setControllerId(subject.getId());
        verificationMethod.save();
        
        if (!verificationMethod.isVerified()) {
            String resolved = PublicKeyType.X25519.decrypt(verificationMethod.getChallenge(), verificationMethod.getPublicKey());
            verificationMethod.verify(resolved);
        }
        
        verificationMethod = Database.getTable(VerificationMethod.class).newRecord();
        verificationMethod.setPurpose(Purpose.Assertion.name());
        verificationMethod.setType(PublicKeyType.Ed25519.name());
        verificationMethod.setPublicKey(key.getPublicKey());
        verificationMethod.setName("%s.%s".formatted(key.getAlias(),verificationMethod.getType()));
        verificationMethod.setControllerId(subject.getId());
        verificationMethod.setHashingAlgorithm(HashAlgorithm.Blake512.name());
        verificationMethod.save();
        
        if (!verificationMethod.isVerified()) {
            String resolved =
                    Crypt.getInstance().generateSignature(verificationMethod.getChallenge(),PublicKeyType.Ed25519.algo(),
                        Crypt.getInstance().getPrivateKey(PublicKeyType.Ed25519.algo(), KeyManager.getInstance().getLatestKey(CryptoKey.PURPOSE_SIGNING).getPrivateKey()));
            verificationMethod.verify(resolved);
        }
        
        
    }
    public CryptoKey getLatestKey(String purpose){
        List<CryptoKey> latest = new Select().from(CryptoKey.class).
                where(new Expression(ModelReflector.instance(CryptoKey.class).getPool(), "PURPOSE",
                        Operator.EQ, purpose)).
                orderBy("ID DESC").execute(1);
        if (!latest.isEmpty()) {
            return latest.get(0);
        }
        return null;
    }
    public boolean isRevoked(String alias){
        CryptoKey aSigningKey = CryptoKey.find(alias,CryptoKey.PURPOSE_SIGNING);
        
        if (aSigningKey == null){
            return true;
        }
        CryptoKey latestKey = getLatestKey(CryptoKey.PURPOSE_SIGNING);
        if (latestKey == null){
            return true;
        }
        if (aSigningKey.getId() != latestKey.getId()){
            long ageBeyondLatest = DateUtils.compareToMillis(latestKey.getCreatedAt(),aSigningKey.getCreatedAt());
            if (ageBeyondLatest < 0){
                return true;
            }
            return (ageBeyondLatest > Duration.ofMinutes(2).toMillis());
        }
        return false;
    }
    public void generateNewKeys() {
        CryptoKey existingSigningKey = getLatestKey(CryptoKey.PURPOSE_SIGNING);
        CryptoKey existingEncryptionKey = getLatestKey(CryptoKey.PURPOSE_ENCRYPTION);

        KeyPair signPair = Crypt.getInstance().generateKeyPair(Request.SIGNATURE_ALGO, Request.SIGNATURE_ALGO_KEY_LENGTH);
        KeyPair encPair = Crypt.getInstance().generateKeyPair(Request.ENCRYPTION_ALGO, Request.ENCRYPTION_ALGO_KEY_LENGTH);
        
        String keyNumber = existingSigningKey.getAlias().substring(existingSigningKey.getAlias().lastIndexOf('.') + 2);// .k[0-9]*
        int nextKeyNumber = Integer.parseInt(keyNumber) + 1;
        String nextKeyId = String.format("%s.k%d",
                existingSigningKey.getAlias().substring(0, existingSigningKey.getAlias().lastIndexOf('.')),
                nextKeyNumber);
        
        CryptoKey signKey = CryptoKey.find(nextKeyId, CryptoKey.PURPOSE_SIGNING);
        signKey.setAlgorithm(Request.SIGNATURE_ALGO);
        signKey.setPrivateKey(Crypt.getInstance().getBase64Encoded(signPair.getPrivate()));
        signKey.setPublicKey(Crypt.getInstance().getBase64Encoded(signPair.getPublic()));
        signKey.save();
        
        CryptoKey encryptionKey = CryptoKey.find(nextKeyId,CryptoKey.PURPOSE_ENCRYPTION);
        encryptionKey.setAlgorithm(Request.ENCRYPTION_ALGO);
        encryptionKey.setPrivateKey(Crypt.getInstance().getBase64Encoded(encPair.getPrivate()));
        encryptionKey.setPublicKey(Crypt.getInstance().getBase64Encoded(encPair.getPublic()));
        encryptionKey.save();
    }
    
    
}
