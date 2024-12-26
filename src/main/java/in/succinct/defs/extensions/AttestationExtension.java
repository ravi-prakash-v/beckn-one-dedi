package in.succinct.defs.extensions;

import com.venky.core.string.StringUtil;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.extensions.ModelOperationExtension;
import in.succinct.defs.db.model.did.documents.Attestation;
import in.succinct.defs.db.model.did.subject.Subject;
import in.succinct.defs.db.model.did.subject.VerificationMethod;
import in.succinct.defs.util.SignatureChallenge;

import java.util.UUID;

public class AttestationExtension extends ModelOperationExtension<Attestation> {
    static {
        registerExtension(new AttestationExtension());
    }
    
    @Override
    protected void beforeValidate(Attestation instance) {
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
            instance.setDid(String.format("%s/attestations/%s",instance.getDocument().getDid(),instance.getName()));
        }
        
    }
    
    @Override
    protected void beforeSave(Attestation instance) {
        if (!instance.isDirty()){
            return;
        }
        
        super.beforeSave(instance);
        incrementModCount(instance);
    }
    @Override
    protected void beforeDestroy(Attestation instance) {
        super.beforeDestroy(instance);
        incrementModCount(instance);
    }
    private void incrementModCount(Attestation instance){
        Subject subject = instance.getVerificationMethod().getController();
        subject.getModCount().increment();
        subject.save();
    }
}
