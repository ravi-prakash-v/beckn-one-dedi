package in.succinct.defs.extensions;

import com.venky.core.string.StringUtil;
import com.venky.swf.db.extensions.ModelOperationExtension;
import in.succinct.defs.db.model.did.documents.Signature;
import in.succinct.defs.db.model.did.subject.VerificationMethod;
import in.succinct.defs.util.SignatureChallenge;

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
        String hash = SignatureChallenge.getInstance().hash(verificationMethod,data);
        
        boolean valid = SignatureChallenge.getInstance().verify(verificationMethod,hash,instance.getSignature());
        
        if (!valid) {
            throw new RuntimeException("Invalid Signature");
        }
        instance.setVerified(true);
    }
}
