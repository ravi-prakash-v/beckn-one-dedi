package in.succinct.defs.util;

import com.venky.core.security.Crypt;
import in.succinct.defs.db.model.did.subject.VerificationMethod;
import in.succinct.defs.db.model.did.subject.VerificationMethod.HashAlgorithm;
import in.succinct.defs.db.model.did.subject.VerificationMethod.PublicKeyType;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;

public class SignatureChallenge {
    private static volatile SignatureChallenge sSoleInstance;
    
    //private constructor.
    private SignatureChallenge() {
        //Prevent form the reflection api.
        if (sSoleInstance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }
    
    public static SignatureChallenge getInstance() {
        if (sSoleInstance == null) { //if there is no instance available... create new one
            synchronized (SignatureChallenge.class) {
                if (sSoleInstance == null)
                    sSoleInstance = new SignatureChallenge();
            }
        }
        
        return sSoleInstance;
    }
    
    //Make singleton from serialize and deserialize operation.
    protected SignatureChallenge readResolve() {
        return getInstance();
    }
    
    public String hash(VerificationMethod method, byte[] data){
        HashAlgorithm hashAlgorithm = HashAlgorithm.valueOf(method.getHashingAlgorithm());
        return Crypt.getInstance().toBase64(Crypt.getInstance().digest(hashAlgorithm.algo(),data));
    }
    
    public boolean verify(VerificationMethod verificationMethod, String signedString, String signature){
        return verify(verificationMethod,signedString.getBytes(StandardCharsets.UTF_8),signature);
    }
    public boolean verify(VerificationMethod verificationMethod, byte[] payload, String signature){
        
        PublicKeyType publicKeyType = PublicKeyType.valueOf(verificationMethod.getType());
        PublicKey publicKey  = Crypt.getInstance().getPublicKey(publicKeyType.algo(),verificationMethod.getPublicKey());
        return Crypt.getInstance().verifySignature(payload,signature,publicKeyType.algo(),publicKey);
    }
}
