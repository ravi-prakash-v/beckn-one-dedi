package in.succinct.defs.db.model.did.subject;

import com.venky.core.security.Crypt;
import com.venky.swf.db.annotations.column.COLUMN_DEF;
import com.venky.swf.db.annotations.column.defaulting.StandardDefault;
import com.venky.swf.db.annotations.column.validations.Enumeration;
import com.venky.swf.db.model.CryptoKey;
import com.venky.swf.db.model.Model;
import in.succinct.defs.db.model.did.identifier.Did;
import in.succinct.defs.util.KeyManager;
import org.bouncycastle.jcajce.spec.EdDSAParameterSpec;
import org.bouncycastle.jcajce.spec.XDHParameterSpec;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import java.security.PrivateKey;

public interface VerificationMethod extends Model, Did {
    
    long getControllerId();
    void setControllerId(long id);
    Subject getController();
    
    
    @Enumeration(enumClass = "in.succinct.defs.db.model.did.subject.VerificationMethod$HashAlgorithm")
    String getHashingAlgorithm();
    void setHashingAlgorithm(String hashingAlgorithm);
    
    
    
    @Enumeration(enumClass = "in.succinct.defs.db.model.did.subject.VerificationMethod$Purpose")
    String getPurpose();
    void setPurpose(String purpose);
    
    enum Purpose {
        Authentication,
        Assertion,
        KeyAgreement,
        CapabilityInvocation,
        CapabilityDelegation,
    }
    
    @Enumeration(enumClass = "in.succinct.defs.db.model.did.subject.VerificationMethod$PublicKeyType")
    String getType();
    void setType(String type);
    
    
    /*
        when type isk
        Mail => Email
        Phone => PhoneNumber
        Ed25519,X25519 => corresponding public key
        Dns = > domain name
        
     */
    String getPublicKey();
    void setPublicKey(String publicKey);
    
    
    @COLUMN_DEF(StandardDefault.BOOLEAN_FALSE)
    boolean isVerified();
    void setVerified(boolean verified);
    
    
    String getChallenge();
    void setChallenge(String challenge);
    
    
    public void challenge();
    public void challenge(boolean save);
    public void verify(String challengeResponse);
    public void verify(String challengeResponse, boolean save);
    
    
    /*
    In case of otp, Challenge and response is same.
     */
    String getResponse();
    void setResponse(String response);
    
    
    enum HashAlgorithm  {
        Blake512("BLAKE2B-512","BLAKE-512");
        
        private final String algoName;
        private final String commonName;
        HashAlgorithm(String algoName, String commonName){
            this.algoName = algoName;
            this.commonName = commonName;
        }
        public String algo(){
            return algoName ;
        }
        public String commonName(){
            return commonName;
        }
        
    }
    enum PublicKeyType {
        
        Ed25519(EdDSAParameterSpec.Ed25519),
        X25519(XDHParameterSpec.X25519) {
            @Override
            public boolean isChallengeEncrypted() {
                return true;
            }
            
            public String encrypt(String challenge, String publicKey){
                    SecretKey symKey = getSecretKey(KeyManager.getInstance().getLatestKey(CryptoKey.PURPOSE_ENCRYPTION).getPrivateKey(),publicKey);
                    return Crypt.getInstance().encrypt(challenge, "AES", symKey);
            }
            private SecretKey getSecretKey(String pv, String pb){
                try {
                    KeyAgreement agreement = KeyAgreement.getInstance(algo());
                    PrivateKey privateKey = Crypt.getInstance().getPrivateKey(algo(), pv);
                    
                    agreement.init(privateKey);
                    agreement.doPhase(Crypt.getInstance().getPublicKey(algo(), pb), true);
                    return agreement.generateSecret("TlsPremasterSecret");
                }catch (Exception ex){
                    throw new RuntimeException(ex);
                }
            }
            public String decrypt(String challenge, String publicKey){
                SecretKey symKey = getSecretKey(KeyManager.getInstance().getLatestKey(CryptoKey.PURPOSE_ENCRYPTION).getPrivateKey(),publicKey);
                return Crypt.getInstance().decrypt(challenge, "AES", symKey);
            }
        },
        Phone {
            @Override
            public boolean isChallengeVerificationInFlight() {
                return true;
            }
        },
        Email {
            @Override
            public boolean isChallengeVerificationInFlight() {
                return true;
            }
        },
        Dns;
        private final String algoName;
        private final String commonName;
        
        
        PublicKeyType(){
            this(null);
        }
        PublicKeyType(String algoName){
            this(algoName,algoName == null ? null : algoName.toLowerCase());
        }
        PublicKeyType(String algoName, String commonName){
            this.algoName = algoName;
            this.commonName = commonName;
        }
        public String algo(){
            return algoName == null ? name() : algoName;
        }
        public String commonName(){
            return commonName == null ? name().toLowerCase() : commonName;
        }
        
        public boolean isChallengeVerificationInFlight(){
            return false;
        }
        
        public boolean isChallengeEncrypted(){
            return false;
        }
     
        public String encrypt(String payload, String publicKey){
            return payload;
        }
        public String decrypt(String payload, String publicKey){
            return payload;
        }
        
    }
    
    
}
