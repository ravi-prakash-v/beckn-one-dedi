package in.succinct.defs.extensions;

import com.venky.core.string.StringUtil;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.extensions.ModelOperationExtension;
import in.succinct.defs.db.model.did.documents.Signature;
import in.succinct.defs.db.model.did.subject.VerificationMethod;
import in.succinct.defs.util.SignatureChallenge;

import java.util.UUID;

public class SignatureExtension extends ModelOperationExtension<Signature> {
    static {
        registerExtension(new SignatureExtension());
    }
    
    @Override
    protected void beforeValidate(Signature instance) {
        super.beforeValidate(instance);
        
        byte[] data = StringUtil.readBytes(instance.getDocument().getStream());
        
        VerificationMethod verificationMethod  = instance.getVerificationMethod();
        if (!verificationMethod.isVerified()){
            throw new RuntimeException("Unverified public key cannot be used to sign a document");
        }
        boolean valid = false;
        if (ObjectUtil.isVoid(verificationMethod.getHashingAlgorithm())){
            valid = SignatureChallenge.getInstance().verify(verificationMethod, data, instance.getSignature());
        }else {
            String hash = SignatureChallenge.getInstance().hash(verificationMethod, data);
            valid = SignatureChallenge.getInstance().verify(verificationMethod, hash, instance.getSignature());
        }
        
        if (!valid) {
            throw new RuntimeException("Invalid Signature");
        }
        instance.setVerified(true);
        
        if (ObjectUtil.isVoid(instance.getName())){
            instance.setName(UUID.randomUUID().toString());
        }
        if (ObjectUtil.isVoid(instance.getDid())) {
            instance.setDid(String.format("%s/signatures/%s",instance.getDocument().getDid(),instance.getName()));
        }
        
    }
}
