package in.succinct.defs.db.model.did.subject;

import com.venky.core.security.Crypt;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.table.ModelImpl;
import com.venky.swf.plugins.background.core.TaskManager;
import com.venky.swf.plugins.collab.util.sms.SMSProviderFactory;
import com.venky.swf.plugins.mail.core.MailerTask;
import com.venky.swf.plugins.mail.core.SimpleMailerTask;
import in.succinct.defs.db.model.did.subject.VerificationMethod.HashAlgorithm;
import in.succinct.defs.db.model.did.subject.VerificationMethod.PublicKeyType;
import in.succinct.defs.util.Challenge;

import java.util.UUID;

public class VerificationMethodImpl extends ModelImpl<VerificationMethod> {
    public VerificationMethodImpl(VerificationMethod method){
        super(method);
    }
    public void challenge() {
        this.challenge(true);
    }
    public void challenge(boolean save) {
        VerificationMethod instance = getProxy();
        PublicKeyType keyType = PublicKeyType.valueOf(instance.getType());
        String challenge = Challenge.getInstance().createRandomChallenge();
        if (keyType.isChallengeEncrypted()){
            challenge = keyType.encrypt(challenge,instance.getPublicKey());
        }
        
        if (!keyType.isChallengeVerificationInFlight()){
            instance.setChallenge(challenge);
        }else {
            if (!ObjectUtil.isVoid(instance.getChallenge())){
                Challenge.getInstance().remove(instance.getChallenge());
                //Remove old challenge if any
            }
            String txnId = UUID.randomUUID().toString();
            instance.setChallenge(txnId);
            Challenge.getInstance().put(txnId,challenge);
            if (keyType == PublicKeyType.Phone){
                String phoneNumber = instance.getPublicKey();
                SMSProviderFactory.getInstance().getDefaultProvider().sendOtp(phoneNumber,challenge,true);
            }else if (keyType == PublicKeyType.Email){
                String email = instance.getPublicKey();
                SimpleMailerTask task = new SimpleMailerTask(email,"Your Otp Challenge",challenge);
                TaskManager.instance().executeAsync(task,false);
            }
        }
        instance.setVerified(false);
        if (save){
            instance.save();
        }
    }
    
    
    public void verify(String challengeResponse){
        verify(challengeResponse,true);
    }
    public void verify(String challengeResponse,boolean save){
        VerificationMethod instance = getProxy();
        PublicKeyType keyType = PublicKeyType.valueOf(instance.getType());
        String response = challengeResponse;
        String expectedResponse = instance.getChallenge();
        if (!ObjectUtil.isVoid(instance.getHashingAlgorithm())){
            expectedResponse = Crypt.getInstance().toBase64(Crypt.getInstance().digest(HashAlgorithm.valueOf(instance.getHashingAlgorithm()).algo(),expectedResponse));
        }
        
        if (keyType.isChallengeEncrypted()){
            response = keyType.encrypt(response,instance.getPublicKey());
        }else if (keyType == PublicKeyType.Ed25519){
            if (Crypt.getInstance().verifySignature(expectedResponse,response,keyType.algo(),
                    Crypt.getInstance().getPublicKey(keyType.algo(),instance.getPublicKey()))){
                response = expectedResponse;
            }
        }else {
            if (keyType.isChallengeVerificationInFlight()) {
                expectedResponse = Challenge.getInstance().remove(expectedResponse);
            }
        }
        
        if (ObjectUtil.equals(response,expectedResponse)){
            instance.setTxnProperty("being.verified",true);
            instance.setVerified(true);
            instance.setResponse(challengeResponse);
            if (save) {
                instance.save();
            }
        }else {
            throw new RuntimeException("Verification failed");
        }
    }
}
