package in.succinct.defs.extensions;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.Database;
import com.venky.swf.db.extensions.ModelOperationExtension;
import in.succinct.defs.db.model.did.subject.Subject;
import in.succinct.defs.db.model.did.subject.VerificationMethod;
import in.succinct.defs.db.model.did.subject.VerificationMethod.PublicKeyType;
import org.apache.lucene.index.DocIDMerger.Sub;

import java.util.UUID;

public class VerificationMethodExtension extends ModelOperationExtension<VerificationMethod> {
    static  {
        registerExtension(new VerificationMethodExtension());
    }
    
    @Override
    protected void beforeValidate(VerificationMethod instance) {
        super.beforeValidate(instance);
        if (ObjectUtil.isVoid(instance.getName())) {
            instance.setName(UUID.randomUUID().toString());
        }
        Subject controller = instance.getController();

        if (ObjectUtil.isVoid(instance.getDid())){
            instance.setDid(String.format("%s/verification_methods/%s".formatted(controller.getDid(),instance.getName())));
            
            VerificationMethod persisted = Database.getTable(VerificationMethod.class).getRefreshed(instance);
            if (!persisted.getRawRecord().isNewRecord()) {
                instance.getRawRecord().load(persisted.getRawRecord());
            }
        }
        if (instance.isVerified() == null){
            instance.setVerified(false);
        }
        
        VerificationMethod.PublicKeyType keyType = PublicKeyType.valueOf(instance.getType());
        
        if (keyType == PublicKeyType.Dns) {
            instance.setPublicKey(String.format("%s.%s", instance.getName(), controller.getName()));
        }
        
        if (!instance.getRawRecord().isNewRecord() && instance.isDirty()){
            boolean beingVerified =  instance.getReflector().getJdbcTypeHelper().getTypeRef(boolean.class).getTypeConverter().valueOf(instance.getTxnProperty("being.verified"));
            if (instance.isVerified() &&  !beingVerified){
                if (instance.getRawRecord().isFieldDirty("VERIFIED")){
                    switch (keyType){
                        case Email,Phone -> {
                            throw new RuntimeException("To mark verified, you need to call verify api with the OTP sent to your %s".formatted(keyType));
                        }
                        case Dns -> {
                            throw new RuntimeException("To mark verified, you need to call verify api after ensuring TXT record is created for %s = %s".formatted(instance.getPublicKey(),instance.getChallenge()));
                        }
                        case X25519 -> {
                            throw new RuntimeException("To mark verified, you need to decrypt the challenge %s and post it using verify api".formatted(instance.getChallenge()));
                        }
                        case Ed25519 -> {
                            throw new RuntimeException("To mark verified, you need to sign the challenge %s and post it using verify api".formatted(instance.getChallenge()));
                        }
                        
                    }
                    
                }else {
                    throw new RuntimeException("Cannot change verified methods once  have been validated");
                }
            }
        }
        
        
    }
    
    @Override
    protected void beforeCreate(VerificationMethod instance) {
        super.beforeCreate(instance);
        instance.challenge(false);
        
    }
    
    @Override
    protected void beforeUpdate(VerificationMethod instance) {
        if (!instance.isDirty()){
            return;
        }
        super.beforeUpdate(instance);
        incrementModCount(instance);
    }
    
    private void incrementModCount(VerificationMethod instance){
        Subject controller = instance.getController();
        controller.getModCount().increment();
        controller.save();
    }
    
    @Override
    protected void beforeDestroy(VerificationMethod instance) {
        super.beforeDestroy(instance);
        incrementModCount(instance);
    }
}
